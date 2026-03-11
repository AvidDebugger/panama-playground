package net.szumigaj.java.panama.ffm.bench;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
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
 * Measures sequential int reads across heap, off-heap, and direct-buffer storage.
 *
 * Hypothesis:
 * - Heap arrays should be the best pure-Java baseline.
 * - Heap-backed and native {@link MemorySegment} reads should be competitive for
 *   simple sequential access.
 * - Direct {@link ByteBuffer} reads should be somewhat slower because of the buffer API.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(2)
public class MemoryReadBenchmark {

    private static final int SIZE = 1024;

    private int[] heapArray;
    private MemorySegment nativeSegment;
    private MemorySegment heapSegment;
    private ByteBuffer directBuffer;
    private Arena arena;

    @Setup(Level.Trial)
    public void setupTrial() {
        heapArray = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            heapArray[i] = i;
        }
        arena = Arena.ofConfined();
        nativeSegment = arena.allocateFrom(ValueLayout.JAVA_INT, heapArray);
        heapSegment = MemorySegment.ofArray(heapArray);
        directBuffer = ByteBuffer.allocateDirect(SIZE * Integer.BYTES);
        for (int i = 0; i < SIZE; i++) {
            directBuffer.putInt(i * 4, i);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        arena.close();
    }

    /**
     * Heap array baseline for sequential primitive reads.
     */
    @Benchmark
    @OperationsPerInvocation(SIZE)
    public long read_heapArray() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) {
            sum += heapArray[i];
        }
        return sum;
    }

    /**
     * Sequential reads from native off-heap memory using the FFM API.
     */
    @Benchmark
    @OperationsPerInvocation(SIZE)
    public long read_nativeSegment() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) {
            sum += nativeSegment.get(ValueLayout.JAVA_INT, (long) i * Integer.BYTES);
        }
        return sum;
    }

    /**
     * Sequential reads from a heap-backed segment view over a Java array.
     */
    @Benchmark
    @OperationsPerInvocation(SIZE)
    public long read_heapSegment() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) {
            sum += heapSegment.get(ValueLayout.JAVA_INT, (long) i * Integer.BYTES);
        }
        return sum;
    }

    /**
     * Sequential reads from a direct buffer baseline.
     */
    @Benchmark
    @OperationsPerInvocation(SIZE)
    public long read_directByteBuffer() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) {
            sum += directBuffer.getInt(i * 4);
        }
        return sum;
    }
}
