package net.szumigaj.java.panama.ffm.bench;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import net.szumigaj.java.panama.ffm.support.NativeLib;
import net.szumigaj.java.panama.ffm.tutorial.upcalls.QSortUpcalls;

/**
 * Measures sort cost when comparing the JDK's in-process sort against a native qsort
 * that calls back into Java via an FFM upcall.
 *
 * Hypothesis:
 * - {@link Arrays#sort(int[])} should clearly win for this data size.
 * - The FFM qsort path should be much slower because each comparison crosses the
 *   native/Java boundary through an upcall.
 *
 * Preparation is deliberately moved to {@code @Setup(Level.Invocation)} so the
 * reported numbers focus on sorting, not on cloning the input array.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(2)
public class QSortUpcallBenchmark {

    private static final int SIZE = 1000;

    private int[] sourceData;
    private int[] javaWork;
    private int[] nativeWork;

    @Setup(Level.Trial)
    public void setupTrial() {
        NativeLib.load();
        sourceData = new int[SIZE];
        javaWork = new int[SIZE];
        nativeWork = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            sourceData[i] = SIZE - i;
        }
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        System.arraycopy(sourceData, 0, javaWork, 0, SIZE);
        System.arraycopy(sourceData, 0, nativeWork, 0, SIZE);
    }

    /**
     * JDK primitive-array sort baseline.
     *
     * Hypothesis: best result in this class because no native boundary is involved.
     */
    @Benchmark
    public int arraysSort() {
        Arrays.sort(javaWork);
        return javaWork[0] + javaWork[SIZE - 1];
    }

    /**
     * Native qsort that calls a Java comparator through an FFM upcall.
     *
     * Hypothesis: much slower than {@link #arraysSort()} because comparisons bounce
     * between native code and Java many times.
     */
    @Benchmark
    public int qsortUpcall() {
        QSortUpcalls.qsortInt(nativeWork);
        return nativeWork[0] + nativeWork[SIZE - 1];
    }
}
