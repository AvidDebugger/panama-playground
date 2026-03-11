package net.szumigaj.java.panama.ffm.tutorial.perf;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import net.szumigaj.java.panama.ffm.support.FfmExceptionHelper;
import net.szumigaj.java.panama.ffm.support.NativeLib;

/**
 * Chapter 5: Performance-oriented downcalls (sum, copy, fill).
 */
public final class PerfDowncalls {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOOKUP;

    static {
        NativeLib.load();
        LOOKUP = SymbolLookup.loaderLookup();
    }

    private static final MethodHandle SUM_I32 = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_sum_i32"),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );

    private static final MethodHandle COPY_I32 = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_copy_i32"),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );

    private static final MethodHandle FILL_I32 = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_fill_i32"),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT)
    );

    private static final long INT_BYTES = ValueLayout.JAVA_INT.byteSize();

    private PerfDowncalls() {}

    /**
     * Sum the first {@code count} int32 elements. Segment must have at least
     * {@code count * 4} bytes.
     */
    public static long sumI32(MemorySegment arr, long count) {
        long required = count * INT_BYTES;
        if (arr.byteSize() < required) {
            throw new IllegalArgumentException(
                    "segment too small: " + arr.byteSize() + " bytes for " + count + " ints");
        }
        try {
            return (long) SUM_I32.invoke(arr, count);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    /**
     * Copy {@code count} int32 elements from src to dst. Both segments must have
     * at least {@code count * 4} bytes.
     */
    public static void copyI32(MemorySegment dst, MemorySegment src, long count) {
        long required = count * INT_BYTES;
        if (dst.byteSize() < required) {
            throw new IllegalArgumentException("dst too small: " + dst.byteSize() + " bytes for " + count + " ints");
        }
        if (src.byteSize() < required) {
            throw new IllegalArgumentException("src too small: " + src.byteSize() + " bytes for " + count + " ints");
        }
        try {
            COPY_I32.invoke(dst, src, count);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    /**
     * Fill the first {@code count} int32 elements with value. Segment must have
     * at least {@code count * 4} bytes.
     */
    public static void fillI32(MemorySegment arr, long count, int value) {
        long required = count * INT_BYTES;
        if (arr.byteSize() < required) {
            throw new IllegalArgumentException(
                    "segment too small: " + arr.byteSize() + " bytes for " + count + " ints");
        }
        try {
            FILL_I32.invoke(arr, count, value);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    /**
     * Allocate and fill an array via native call.
     * Uses layout-based allocation for correct int alignment.
     */
    public static MemorySegment allocateAndFill(Arena arena, int count, int value) {
        MemorySegment seg = arena.allocate(ValueLayout.JAVA_INT, count);
        fillI32(seg, count, value);
        return seg;
    }
}
