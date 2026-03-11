package net.szumigaj.java.panama.ffm.tutorial.structs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PointLayoutTest {

    @Test
    void pointLayout_byteSize_is8() {
        Assertions.assertThat(PointLayout.byteSize()).isEqualTo(8L);
    }

    @Test
    void setPoint_and_getX_getY_roundtrip() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment seg = arena.allocate(PointLayout.LAYOUT);
            PointLayout.setPoint(seg, 0, 100, 200);
            Assertions.assertThat(PointLayout.getX(seg, 0)).isEqualTo(100);
            Assertions.assertThat(PointLayout.getY(seg, 0)).isEqualTo(200);
        }
    }

    @Test
    void pointLayout_matchesNativeStructSize() {
        Assertions.assertThat(PointLayout.LAYOUT.byteSize()).isEqualTo(8L);
    }

    @Test
    void pointLayout_hasCorrectAlignment() {
        Assertions.assertThat(PointLayout.LAYOUT.byteAlignment()).isEqualTo(4L);
    }

    @Test
    void pointLayout_hasCorrectFieldOffsets() {
        Assertions.assertThat(PointLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("x"))).isEqualTo(0L);
        Assertions.assertThat(PointLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("y"))).isEqualTo(4L);
    }
}
