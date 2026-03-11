package net.szumigaj.java.panama.ffm.tutorial.upcalls;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class QSortUpcallsIT {

    @Test
    void qsortInt_sortsAscending() {
        int[] arr = {3, 1, 4, 1, 5, 9, 2, 6};
        QSortUpcalls.qsortInt(arr);
        Assertions.assertThat(arr).containsExactly(1, 1, 2, 3, 4, 5, 6, 9);
    }

    @Test
    void qsortInt_emptyArray() {
        int[] arr = {};
        QSortUpcalls.qsortInt(arr);
        Assertions.assertThat(arr).isEmpty();
    }

    @Test
    void qsortInt_singleElement() {
        int[] arr = {42};
        QSortUpcalls.qsortInt(arr);
        Assertions.assertThat(arr).containsExactly(42);
    }
}
