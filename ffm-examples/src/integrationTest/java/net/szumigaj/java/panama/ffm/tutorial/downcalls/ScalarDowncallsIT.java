package net.szumigaj.java.panama.ffm.tutorial.downcalls;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ScalarDowncallsIT {

    @Test
    void addI32_callsNativeAndReturnsSum() {
        Assertions.assertThat(ScalarDowncalls.addI32(10, 20)).isEqualTo(30);
    }

    @Test
    void addI64_callsNativeAndReturnsSum() {
        Assertions.assertThat(ScalarDowncalls.addI64(100L, 200L)).isEqualTo(300L);
    }

    @Test
    void addF64_callsNativeAndReturnsSum() {
        Assertions.assertThat(ScalarDowncalls.addF64(1.5, 2.5)).isEqualTo(4.0);
    }

    @Test
    void noop_callsNativeWithoutError() {
        Assertions.assertThatCode(ScalarDowncalls::noop).doesNotThrowAnyException();
    }
}
