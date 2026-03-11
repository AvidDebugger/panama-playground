package net.szumigaj.java.panama.ffm.bench;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures the amortized cost of tiny off-heap allocations using different APIs.
 *
 * Hypothesis:
 * - A slicing allocator should be the cheapest once a backing block already exists.
 * - Reusing a confined arena should beat opening a fresh arena per tiny allocation.
 * - Direct {@link ByteBuffer} allocation should be the slowest baseline here.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(2)
public class AllocationBenchmark {

    private static final int COUNT = 100;

    /**
     * Measures the cost of creating a fresh confined arena for each tiny allocation.
     *
     * Hypothesis: this is the slowest FFM allocation strategy in this class because
     * arena creation/close dominates the tiny payload allocation.
     */
    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long allocate_confinedArena() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment seg = arena.allocateFrom(ValueLayout.JAVA_INT, i);
                sum += seg.get(ValueLayout.JAVA_INT, 0);
            }
        }
        return sum;
    }

    /**
     * Measures the cost of repeated tiny allocations from the same confined arena.
     *
     * Hypothesis: cheaper than creating a fresh arena each time because lifecycle cost
     * is amortized across all allocations in the invocation.
     */
    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long allocate_reusedConfinedArena() {
        try (Arena arena = Arena.ofConfined()) {
            long sum = 0;
            for (int i = 0; i < COUNT; i++) {
                MemorySegment seg = arena.allocateFrom(ValueLayout.JAVA_INT, i);
                sum += seg.get(ValueLayout.JAVA_INT, 0);
            }
            return sum;
        }
    }

    /**
     * Measures tiny allocations from a pre-allocated backing block via a slicing allocator.
     *
     * Hypothesis: this should be the cheapest FFM strategy because each allocation is
     * just a slice/view into a larger block.
     */
    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long allocate_slicingAllocator() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment block = arena.allocate(COUNT * (long) Integer.BYTES);
            SegmentAllocator slicing = SegmentAllocator.slicingAllocator(block);
            long sum = 0;
            for (int i = 0; i < COUNT; i++) {
                MemorySegment seg = slicing.allocateFrom(ValueLayout.JAVA_INT, i);
                sum += seg.get(ValueLayout.JAVA_INT, 0);
            }
            return sum;
        }
    }

    /**
     * Uses direct {@link ByteBuffer} allocation as a non-FFM off-heap baseline.
     *
     * Hypothesis: direct buffer allocation should be materially slower than the FFM
     * allocation strategies because it carries heavier allocation and cleanup costs.
     */
    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long allocate_directBuffer() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            ByteBuffer buf = ByteBuffer.allocateDirect(Integer.BYTES);
            buf.putInt(0, i);
            sum += buf.getInt(0);
        }
        return sum;
    }
}
