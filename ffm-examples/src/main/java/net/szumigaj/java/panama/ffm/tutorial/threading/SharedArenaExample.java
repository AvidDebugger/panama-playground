package net.szumigaj.java.panama.ffm.tutorial.threading;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Chapter 7: Shared arenas and multi-threaded access.
 * <p>
 * {@code Arena.ofShared()} allows multiple threads to access segments.
 * Confined arenas restrict access to the creating thread.
 */
public final class SharedArenaExample {

    private SharedArenaExample() {}

    /**
     * Multiple threads read from a shared segment.
     * <p>
     * <b>Contract:</b> The segment must be accessible from multiple threads
     * (e.g. allocated with {@code Arena.ofShared()}). Passing a confined-arena
     * segment can cause {@code WrongThreadException}.
     *
     * @param arena    arena that owns the segment (must be shared for multi-thread)
     * @param segment  segment containing at least {@code count} ints
     * @param count    number of int elements to sum
     * @return sum of the first {@code count} ints
     * @throws InterruptedException if any worker thread is interrupted
     * @throws RuntimeException    if any worker thread throws (e.g. WrongThreadException)
     */
    public static long parallelSum(Arena arena, MemorySegment segment, int count) throws InterruptedException {
        if (segment.byteSize() < (long) count * Integer.BYTES) {
            throw new IllegalArgumentException(
                    "segment too small: " + segment.byteSize() + " bytes for " + count + " ints");
        }
        int numThreads = Runtime.getRuntime().availableProcessors();
        long[] partialSums = new long[numThreads];
        AtomicReference<Throwable> workerError = new AtomicReference<>();
        Thread[] threads = new Thread[numThreads];

        int chunk = (count + numThreads - 1) / numThreads;
        for (int t = 0; t < numThreads; t++) {
            final int tid = t;
            final int start = t * chunk;
            final int end = Math.min(start + chunk, count);
            threads[t] = new Thread(() -> {
                try {
                    long sum = 0;
                    for (int i = start; i < end; i++) {
                        sum += segment.get(ValueLayout.JAVA_INT, (long) i * Integer.BYTES);
                    }
                    partialSums[tid] = sum;
                } catch (Throwable e) {
                    workerError.set(e);
                }
            });
            threads[t].start();
        }

        for (Thread t : threads) {
            t.join();
        }
        Throwable err = workerError.get();
        if (err != null) {
            throw new RuntimeException("Worker thread failed", err);
        }
        long total = 0;
        for (long s : partialSums) {
            total += s;
        }
        return total;
    }

    /**
     * Allocate a segment in the given arena and fill it with random ints.
     * Use {@code Arena.ofShared()} when the segment will be accessed from multiple threads.
     */
    public static MemorySegment createSharedSegment(Arena arena, int count) {
        MemorySegment seg = arena.allocate(ValueLayout.JAVA_INT, count);
        for (int i = 0; i < count; i++) {
            seg.set(ValueLayout.JAVA_INT, (long) i * Integer.BYTES, ThreadLocalRandom.current().nextInt());
        }
        return seg;
    }
}
