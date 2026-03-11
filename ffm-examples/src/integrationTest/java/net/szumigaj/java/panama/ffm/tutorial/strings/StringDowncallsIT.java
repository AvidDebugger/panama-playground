package net.szumigaj.java.panama.ffm.tutorial.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class StringDowncallsIT {

    @Test
    void strlen_returnsCorrectLength() {
        Assertions.assertThat(StringDowncalls.strlen("hello")).isEqualTo(5L);
        Assertions.assertThat(StringDowncalls.strlen("")).isEqualTo(0L);
        Assertions.assertThat(StringDowncalls.strlen("a")).isEqualTo(1L);
    }

    @Test
    void strlen_utf8ByteLength_notJavaCharCount() {
        Assertions.assertThat(StringDowncalls.strlen("ą")).isEqualTo(2L);
        Assertions.assertThat(StringDowncalls.strlen("café")).isEqualTo(5L);
    }

    @Test
    void strlenSegment_withAllocateUtf8_matchesStrlen() {
        try (var arena = java.lang.foreign.Arena.ofConfined()) {
            String s = "hello";
            var utf8 = StringDowncalls.allocateUtf8(arena, s);
            Assertions.assertThat(StringDowncalls.strlenSegment(utf8)).isEqualTo(StringDowncalls.strlen(s));
        }
    }
}
