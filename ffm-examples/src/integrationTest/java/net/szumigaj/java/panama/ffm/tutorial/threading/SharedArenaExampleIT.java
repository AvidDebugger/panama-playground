package net.szumigaj.java.panama.ffm.tutorial.threading;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SharedArenaExampleIT {

    @Test
    void parallelSum_withSharedArena_returnsCorrectSum() throws InterruptedException {
        try (Arena arena = Arena.ofShared()) {
            int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            MemorySegment seg = arena.allocateFrom(ValueLayout.JAVA_INT, values);
            long sum = SharedArenaExample.parallelSum(arena, seg, values.length);
            Assertions.assertThat(sum).isEqualTo(55L);
        }
    }

    @Test
    void createSharedSegment_and_parallelSum_roundtrip() throws InterruptedException {
        try (Arena arena = Arena.ofShared()) {
            MemorySegment seg = SharedArenaExample.createSharedSegment(arena, 100);
            long sum = SharedArenaExample.parallelSum(arena, seg, 100);
            Assertions.assertThat(sum).isBetween(Long.MIN_VALUE, Long.MAX_VALUE);
        }
    }

    @Test
    void parallelSum_segmentTooSmall_throws() {
        try (Arena arena = Arena.ofShared()) {
            MemorySegment seg = arena.allocate(ValueLayout.JAVA_INT, 3);
            Assertions.assertThatThrownBy(() -> SharedArenaExample.parallelSum(arena, seg, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("segment too small");
        }
    }
}
