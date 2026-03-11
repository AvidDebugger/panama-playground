package net.szumigaj.java.panama.ffm.tutorial.file;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileBackedSegmentExampleIT {

    @Test
    void mapFile_returnsCorrectContent(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.bin");
        byte[] content = {1, 2, 3, 4, 5};
        Files.write(file, content);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = FileBackedSegmentExample.mapFile(file, arena);
            Assertions.assertThat(segment.byteSize()).isEqualTo(content.length);
            long sum = FileBackedSegmentExample.checksumBytes(segment);
            Assertions.assertThat(sum).isEqualTo(15L);
        }
    }

    @Test
    void countByte_countsCorrectly(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("count.bin");
        byte[] content = {1, 2, 2, 3, 2, 4};
        Files.write(file, content);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = FileBackedSegmentExample.mapFile(file, arena);
            Assertions.assertThat(FileBackedSegmentExample.countByte(segment, (byte) 2)).isEqualTo(3L);
            Assertions.assertThat(FileBackedSegmentExample.countByte(segment, (byte) 5)).isEqualTo(0L);
        }
    }

    @Test
    void mapFile_nonexistentFile_throws() {
        Path nonexistent = Path.of("/nonexistent/path/to/file.bin");
        try (Arena arena = Arena.ofConfined()) {
            Assertions.assertThatThrownBy(() -> FileBackedSegmentExample.mapFile(nonexistent, arena))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to map file");
        }
    }
}
