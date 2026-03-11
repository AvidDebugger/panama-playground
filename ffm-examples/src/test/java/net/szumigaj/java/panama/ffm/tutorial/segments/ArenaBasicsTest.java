package net.szumigaj.java.panama.ffm.tutorial.segments;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ArenaBasicsTest {

    @Test
    void allocateAndReadInt_returnsValue() {
        int result = ArenaBasics.allocateAndReadInt(42);
        Assertions.assertThat(result).isEqualTo(42);
    }

    @Test
    void allocateIntArrayAndSum_sumsCorrectly() {
        long sum = ArenaBasics.allocateIntArrayAndSum(new int[]{1, 2, 3, 4, 5});
        Assertions.assertThat(sum).isEqualTo(15L);
    }

    @Test
    void sliceAndRead_readsCorrectOffset() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment block = arena.allocate(ValueLayout.JAVA_INT, 4);
            block.set(ValueLayout.JAVA_INT, 0, 10);
            block.set(ValueLayout.JAVA_INT, 4, 20);
            block.set(ValueLayout.JAVA_INT, 8, 30);
            block.set(ValueLayout.JAVA_INT, 12, 40);

            Assertions.assertThat(ArenaBasics.sliceAndRead(block, 0)).isEqualTo(10);
            Assertions.assertThat(ArenaBasics.sliceAndRead(block, 4)).isEqualTo(20);
            Assertions.assertThat(ArenaBasics.sliceAndRead(block, 8)).isEqualTo(30);
            Assertions.assertThat(ArenaBasics.sliceAndRead(block, 12)).isEqualTo(40);
        }
    }

    @Test
    void heapSegmentFromInts_hasCorrectContent() {
        int[] arr = {1, 2, 3};
        MemorySegment seg = ArenaBasics.heapSegmentFromInts(arr);
        Assertions.assertThat(seg.byteSize()).isEqualTo(arr.length * (long) Integer.BYTES);
        Assertions.assertThat(seg.get(ValueLayout.JAVA_INT, 0)).isEqualTo(1);
        Assertions.assertThat(seg.get(ValueLayout.JAVA_INT, 4)).isEqualTo(2);
        Assertions.assertThat(seg.get(ValueLayout.JAVA_INT, 8)).isEqualTo(3);
    }

    @Test
    void nativeSegmentFromInts_matchesHeapContent() {
        int[] arr = {7, 8, 9};
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nativeSeg = ArenaBasics.nativeSegmentFromInts(arena, arr);
            Assertions.assertThat(nativeSeg.get(ValueLayout.JAVA_INT, 0)).isEqualTo(7);
            Assertions.assertThat(nativeSeg.get(ValueLayout.JAVA_INT, 4)).isEqualTo(8);
            Assertions.assertThat(nativeSeg.get(ValueLayout.JAVA_INT, 8)).isEqualTo(9);
        }
    }
}
