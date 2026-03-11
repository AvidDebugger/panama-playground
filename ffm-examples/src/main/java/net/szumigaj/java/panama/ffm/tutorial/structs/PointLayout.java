package net.szumigaj.java.panama.ffm.tutorial.structs;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * Chapter 4: Struct layouts and VarHandles.
 * <p>
 * {@code MemoryLayout.structLayout} defines C struct layout; {@code PathElement}
 * and {@code VarHandle} provide type-safe access.
 */
public final class PointLayout {

    /**
     * C struct: typedef struct { int32_t x; int32_t y; } Point;
     */
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("x"),
            ValueLayout.JAVA_INT.withName("y")
    );

    private static final VarHandle X = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("x"));
    private static final VarHandle Y = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("y"));

    private PointLayout() {}

    public static void setPoint(MemorySegment segment, long offset, int x, int y) {
        MemorySegment slice = segment.asSlice(offset, LAYOUT.byteSize());
        X.set(slice, 0L, x);
        Y.set(slice, 0L, y);
    }

    public static int getX(MemorySegment segment, long offset) {
        MemorySegment slice = segment.asSlice(offset, LAYOUT.byteSize());
        return (int) X.get(slice, 0L);
    }

    public static int getY(MemorySegment segment, long offset) {
        MemorySegment slice = segment.asSlice(offset, LAYOUT.byteSize());
        return (int) Y.get(slice, 0L);
    }

    public static long byteSize() {
        return LAYOUT.byteSize();
    }
}
