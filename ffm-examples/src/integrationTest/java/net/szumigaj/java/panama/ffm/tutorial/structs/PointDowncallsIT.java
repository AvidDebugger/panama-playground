package net.szumigaj.java.panama.ffm.tutorial.structs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PointDowncallsIT {

    @Test
    void pointSet_and_pointGet_roundtrip() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment p = arena.allocate(PointLayout.LAYOUT);
            PointDowncalls.pointSet(p, 42, 99);

            MemorySegment xOut = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment yOut = arena.allocate(ValueLayout.JAVA_INT);
            PointDowncalls.pointGet(p, xOut, yOut);

            Assertions.assertThat(xOut.get(java.lang.foreign.ValueLayout.JAVA_INT, 0)).isEqualTo(42);
            Assertions.assertThat(yOut.get(java.lang.foreign.ValueLayout.JAVA_INT, 0)).isEqualTo(99);
        }
    }

    @Test
    void pointAdd_returnsSumOfPoints() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment a = arena.allocate(PointLayout.LAYOUT);
            MemorySegment b = arena.allocate(PointLayout.LAYOUT);
            PointLayout.setPoint(a, 0, 1, 2);
            PointLayout.setPoint(b, 0, 3, 4);

            MemorySegment result = PointDowncalls.pointAdd(arena, a, b);
            Assertions.assertThat(PointLayout.getX(result, 0)).isEqualTo(4);
            Assertions.assertThat(PointLayout.getY(result, 0)).isEqualTo(6);
        }
    }

    @Test
    void pointGet_xOutTooSmall_throws() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment p = arena.allocate(PointLayout.LAYOUT);
            PointDowncalls.pointSet(p, 42, 99);
            MemorySegment xOut = arena.allocate(2);
            MemorySegment yOut = arena.allocate(ValueLayout.JAVA_INT);
            Assertions.assertThatThrownBy(() -> PointDowncalls.pointGet(p, xOut, yOut))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("xOut too small");
        }
    }
}
