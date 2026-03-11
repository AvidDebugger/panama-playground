package net.szumigaj.java.panama.ffm.tutorial.structs;

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
 * Chapter 4: Struct-by-value and struct-by-reference downcalls.
 * <p>
 * Calls ffm_point_add (by value) and ffm_point_set/ffm_point_get (by reference).
 * For struct-by-value return, the linker inserts a SegmentAllocator as the first parameter.
 */
public final class PointDowncalls {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOOKUP;

    static {
        NativeLib.load();
        LOOKUP = SymbolLookup.loaderLookup();
    }

    private static final MethodHandle POINT_ADD = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_point_add"),
            FunctionDescriptor.of(PointLayout.LAYOUT, PointLayout.LAYOUT, PointLayout.LAYOUT)
    );

    private static final MethodHandle POINT_SET = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_point_set"),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
    );

    private static final MethodHandle POINT_GET = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_point_get"),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    private static final long POINT_SIZE = PointLayout.byteSize();
    private static final long INT_SIZE = ValueLayout.JAVA_INT.byteSize();

    private PointDowncalls() {}

    /**
     * Call ffm_point_add: Point ffm_point_add(Point a, Point b).
     * The allocator (arena) is the first parameter for struct-by-value return.
     * Segments a and b must have at least PointLayout.byteSize() bytes.
     */
    public static MemorySegment pointAdd(Arena arena, MemorySegment a, MemorySegment b) {
        if (a.byteSize() < POINT_SIZE) {
            throw new IllegalArgumentException("segment a too small: " + a.byteSize() + " bytes for Point");
        }
        if (b.byteSize() < POINT_SIZE) {
            throw new IllegalArgumentException("segment b too small: " + b.byteSize() + " bytes for Point");
        }
        try {
            return (MemorySegment) POINT_ADD.invoke(arena, a, b);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    /**
     * Call ffm_point_set: void ffm_point_set(Point* p, int32_t x, int32_t y).
     * Segment p must have at least PointLayout.byteSize() bytes.
     */
    public static void pointSet(MemorySegment p, int x, int y) {
        if (p.byteSize() < POINT_SIZE) {
            throw new IllegalArgumentException("segment p too small: " + p.byteSize() + " bytes for Point");
        }
        try {
            POINT_SET.invoke(p, x, y);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    /**
     * Call ffm_point_get: void ffm_point_get(const Point* p, int32_t* x, int32_t* y).
     * Segment p must have PointLayout.byteSize() bytes; xOut and yOut must have at least 4 bytes each.
     */
    public static void pointGet(MemorySegment p, MemorySegment xOut, MemorySegment yOut) {
        if (p.byteSize() < POINT_SIZE) {
            throw new IllegalArgumentException("segment p too small: " + p.byteSize() + " bytes for Point");
        }
        if (xOut.byteSize() < INT_SIZE) {
            throw new IllegalArgumentException("xOut too small: " + xOut.byteSize() + " bytes for int");
        }
        if (yOut.byteSize() < INT_SIZE) {
            throw new IllegalArgumentException("yOut too small: " + yOut.byteSize() + " bytes for int");
        }
        try {
            POINT_GET.invoke(p, xOut, yOut);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }
}
