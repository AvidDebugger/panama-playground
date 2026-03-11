package net.szumigaj.java.panama.ffm.tutorial.perf;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PerfDowncallsIT {

    @Test
    void sumI32_returnsCorrectSum() {
        try (Arena arena = Arena.ofConfined()) {
            int[] arr = {1, 2, 3, 4, 5};
            MemorySegment seg = arena.allocateFrom(ValueLayout.JAVA_INT, arr);
            long sum = PerfDowncalls.sumI32(seg, arr.length);
            Assertions.assertThat(sum).isEqualTo(15L);
        }
    }

    @Test
    void copyI32_copiesCorrectly() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment src = arena.allocateFrom(ValueLayout.JAVA_INT, 10, 20, 30);
            MemorySegment dst = arena.allocate(ValueLayout.JAVA_INT, 3);
            PerfDowncalls.copyI32(dst, src, 3);
            Assertions.assertThat(dst.get(ValueLayout.JAVA_INT, 0)).isEqualTo(10);
            Assertions.assertThat(dst.get(ValueLayout.JAVA_INT, 4)).isEqualTo(20);
            Assertions.assertThat(dst.get(ValueLayout.JAVA_INT, 8)).isEqualTo(30);
        }
    }

    @Test
    void fillI32_fillsCorrectly() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment seg = arena.allocate(ValueLayout.JAVA_INT, 4);
            PerfDowncalls.fillI32(seg, 4, 7);
            for (int i = 0; i < 4; i++) {
                Assertions.assertThat(seg.get(ValueLayout.JAVA_INT, (long) i * 4)).isEqualTo(7);
            }
        }
    }

    @Test
    void sumI32_segmentTooSmall_throws() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment seg = arena.allocate(ValueLayout.JAVA_INT, 2);
            Assertions.assertThatThrownBy(() -> PerfDowncalls.sumI32(seg, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("segment too small");
        }
    }

    @Test
    void copyI32_dstTooSmall_throws() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment src = arena.allocateFrom(ValueLayout.JAVA_INT, 1, 2, 3);
            MemorySegment dst = arena.allocate(ValueLayout.JAVA_INT, 1);
            Assertions.assertThatThrownBy(() -> PerfDowncalls.copyI32(dst, src, 3))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dst too small");
        }
    }
}
