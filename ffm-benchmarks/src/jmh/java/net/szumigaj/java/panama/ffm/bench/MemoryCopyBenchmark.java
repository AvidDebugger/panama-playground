package net.szumigaj.java.panama.ffm.bench;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.util.Arrays;
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

/**
 * Measures bulk copy cost across Java and off-heap APIs.
 *
 * Hypothesis:
 * - {@code System.arraycopy} should be the strongest pure-Java baseline.
 * - {@code MemorySegment.copy} should stay competitive for off-heap copies.
 * - Direct {@link ByteBuffer} bulk copy should be in the same ballpark but may pay
 *   extra position/limit bookkeeping costs.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(2)
public class MemoryCopyBenchmark {

    private static final int SIZE = 1024;
    private static final long COPY_BYTES = SIZE * (long) Integer.BYTES;
    private static final int LAST_INDEX = SIZE - 1;
    private static final long LAST_OFFSET = LAST_INDEX * (long) Integer.BYTES;

    private int[] srcArray;
    private int[] dstArray;
    private MemorySegment srcSegment;
    private MemorySegment dstSegment;
    private ByteBuffer srcBuffer;
    private ByteBuffer dstBuffer;
    private Arena arena;

    @Setup(Level.Trial)
    public void setupTrial() {
        srcArray = new int[SIZE];
        dstArray = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            srcArray[i] = i;
        }
        arena = Arena.ofConfined();
        srcSegment = arena.allocateFrom(ValueLayout.JAVA_INT, srcArray);
        dstSegment = arena.allocate(COPY_BYTES);
        srcBuffer = ByteBuffer.allocateDirect(SIZE * Integer.BYTES);
        dstBuffer = ByteBuffer.allocateDirect(SIZE * Integer.BYTES);
        for (int i = 0; i < SIZE; i++) {
            srcBuffer.putInt(i * 4, i);
        }
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        Arrays.fill(dstArray, Integer.MIN_VALUE);
        srcBuffer.position(0);
        srcBuffer.limit(srcBuffer.capacity());
        dstBuffer.clear();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        arena.close();
    }

    /**
     * Java heap-to-heap bulk copy baseline.
     *
     * Hypothesis: this should remain the best baseline for heap arrays.
     */
    @Benchmark
    @OperationsPerInvocation(SIZE)
    public int copy_arrayCopy() {
        System.arraycopy(srcArray, 0, dstArray, 0, SIZE);
        return dstArray[LAST_INDEX];
    }

    /**
     * FFM bulk copy between off-heap memory segments.
     *
     * Hypothesis: close to the Java baseline while staying fully off-heap.
     */
    @Benchmark
    @OperationsPerInvocation(SIZE)
    public int copy_segmentCopy() {
        MemorySegment.copy(srcSegment, 0, dstSegment, 0, COPY_BYTES);
        return dstSegment.get(ValueLayout.JAVA_INT, LAST_OFFSET);
    }

    /**
     * Direct buffer bulk copy baseline.
     *
     * Hypothesis: competitive, but slightly noisier than the other two variants due
     * to buffer state management.
     */
    @Benchmark
    @OperationsPerInvocation(SIZE)
    public int copy_directBufferPut() {
        dstBuffer.put(srcBuffer);
        return dstBuffer.getInt(LAST_INDEX * Integer.BYTES);
    }
}
