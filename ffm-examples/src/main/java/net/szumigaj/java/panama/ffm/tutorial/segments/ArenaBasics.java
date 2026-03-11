package net.szumigaj.java.panama.ffm.tutorial.segments;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Chapter 1: Arenas and memory segments.
 * <p>
 * The FFM API was finalized in JDK 22 (JEP 454).
 * <ul>
 *   <li>{@code Arena.ofConfined()} – manually closed, single-threaded</li>
 *   <li>{@code Arena.ofShared()} – manually closed, multi-threaded</li>
 *   <li>{@code Arena.ofAuto()} – GC-managed, multi-threaded</li>
 * </ul>
 */
public final class ArenaBasics {

    private ArenaBasics() {}

    /**
     * Allocate a single int in a confined arena and read it back.
     */
    public static int allocateAndReadInt(int value) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_INT, value);
            return segment.get(ValueLayout.JAVA_INT, 0);
        }
    }

    /**
     * Allocate an array of ints and sum them.
     */
    public static long allocateIntArrayAndSum(int[] values) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_INT, values);
            long sum = 0;
            for (int i = 0; i < values.length; i++) {
                sum += segment.get(ValueLayout.JAVA_INT, (long) i * Integer.BYTES);
            }
            return sum;
        }
    }

    /**
     * Demonstrate slicing: read an int from a slice of the given block at offset.
     */
    public static int sliceAndRead(MemorySegment block, long offset) {
        return block.asSlice(offset, ValueLayout.JAVA_INT.byteSize())
                .get(ValueLayout.JAVA_INT, 0);
    }

    /**
     * Create a heap-backed segment from a Java array (no native allocation).
     */
    public static MemorySegment heapSegmentFromInts(int[] arr) {
        return MemorySegment.ofArray(arr);
    }

    /**
     * Create a native segment with the same layout as the heap segment.
     */
    public static MemorySegment nativeSegmentFromInts(Arena arena, int[] arr) {
        return arena.allocateFrom(ValueLayout.JAVA_INT, arr);
    }
}
