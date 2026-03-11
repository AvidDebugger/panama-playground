package net.szumigaj.java.panama.ffm.bench;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import net.szumigaj.java.panama.ffm.tutorial.structs.PointLayout;

/**
 * Measures scanning an array of off-heap structs using two access styles.
 *
 * Hypothesis:
 * - Manual offset loads may have a small edge because they skip VarHandle indirection.
 * - In practice, both approaches should stay close for a simple hot loop.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(2)
public class StructAccessBenchmark {

    private static final int COUNT = 100;

    private MemorySegment segment;
    private Arena arena;

    @Setup(Level.Trial)
    public void setupTrial() {
        arena = Arena.ofConfined();
        segment = arena.allocate(PointLayout.byteSize() * COUNT);
        for (int i = 0; i < COUNT; i++) {
            PointLayout.setPoint(segment, (long) i * PointLayout.byteSize(), i, i * 2);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        arena.close();
    }

    /**
     * Reads struct fields through the layout-defined VarHandles.
     *
     * Hypothesis: safer and more expressive, with performance close to manual offsets.
     */
    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long struct_scanVarHandles() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            long offset = (long) i * PointLayout.byteSize();
            sum += PointLayout.getX(segment, offset);
            sum += PointLayout.getY(segment, offset);
        }
        return sum;
    }

    /**
     * Reads the same struct fields with explicit byte offsets.
     *
     * Hypothesis: may be marginally faster, but the difference should be small.
     */
    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long struct_manualOffsets() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            long offset = (long) i * PointLayout.byteSize();
            sum += segment.get(ValueLayout.JAVA_INT, offset);
            sum += segment.get(ValueLayout.JAVA_INT, offset + 4);
        }
        return sum;
    }
}
