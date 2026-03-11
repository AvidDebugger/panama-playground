package net.szumigaj.java.panama.ffm.bench;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import net.szumigaj.java.panama.ffm.support.NativeLib;
import net.szumigaj.java.panama.ffm.tutorial.strings.StringDowncalls;
import net.szumigaj.java.panama.ffm.tutorial.downcalls.ScalarDowncalls;

/**
 * Measures steady-state cost of tiny downcalls and string marshalling.
 *
 * Hypothesis:
 * - A tiny FFM downcall should be slower than a plain Java baseline call.
 * - Reusing a pre-allocated UTF-8 segment should be materially cheaper than
 *   marshalling a Java {@link String} on every invocation.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(2)
public class DowncallBenchmark {

    private int leftOperand;
    private int rightOperand;
    private String testString;
    private Arena arena;

    private MemorySegment utf8Reused;

    @Setup(Level.Trial)
    public void setup() {
        NativeLib.load();
        leftOperand = 10;
        rightOperand = 20;
        testString = "hello world";
        arena = Arena.ofConfined();
        utf8Reused = arena.allocateFrom(testString);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        arena.close();
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static int javaAddBaseline(int left, int right) {
        return left + right;
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static int javaStringLengthBaseline(String value) {
        return value.length();
    }

    /**
     * Plain Java baseline for adding two ints.
     *
     * Hypothesis: much cheaper than crossing the native boundary.
     */
    @Benchmark
    public int javaAdd() {
        return javaAddBaseline(leftOperand, rightOperand);
    }

    /**
     * Tiny FFM downcall with two int arguments and one int return value.
     *
     * Hypothesis: small but measurable overhead compared to {@link #javaAdd()}.
     */
    @Benchmark
    public int scalarDowncall_addI32() {
        return ScalarDowncalls.addI32(leftOperand, rightOperand);
    }

    /**
     * Tiny FFM downcall that does no useful work in native code.
     *
     * Hypothesis: this approximates the minimum native call overhead in this repo.
     */
    @Benchmark
    public void scalarDowncall_noop() {
        ScalarDowncalls.noop();
    }

    /**
     * Plain Java baseline for asking a {@link String} for its length.
     *
     * Hypothesis: dramatically cheaper than a native strlen path.
     */
    @Benchmark
    public int javaStringLength() {
        return javaStringLengthBaseline(testString);
    }

    /**
     * Measures UTF-8 marshalling plus the downcall to native strlen.
     *
     * Hypothesis: slower than {@link #strlen_reusedSegment()} because each invocation
     * must allocate and encode the Java string first.
     */
    @Benchmark
    public long strlen_perCallMarshalling() {
        return StringDowncalls.strlen(testString);
    }

    /**
     * Measures the native strlen call after setup has already created the UTF-8 segment.
     *
     * Hypothesis: this isolates downcall cost much better than per-call marshalling.
     */
    @Benchmark
    public long strlen_reusedSegment() {
        return StringDowncalls.strlenSegment(utf8Reused);
    }
}
