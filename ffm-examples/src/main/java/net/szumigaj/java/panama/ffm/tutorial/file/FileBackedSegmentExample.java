package net.szumigaj.java.panama.ffm.tutorial.file;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Chapter 8: File-backed memory segments.
 * <p>
 * Map a file (or region) into memory for efficient access.
 * Uses FileChannel.map with Arena (JDK 22+).
 */
public final class FileBackedSegmentExample {

    private FileBackedSegmentExample() {}

    /**
     * Map the entire file into a memory segment.
     *
     * @param path  path to the file
     * @param arena arena for the segment lifetime
     * @return segment backed by the file (read-only)
     */
    public static MemorySegment mapFile(Path path, Arena arena) {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long size = channel.size();
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, size, arena);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map file: " + path, e);
        }
    }

    /**
     * Simple checksum: sum of bytes in the segment (for benchmarking).
     */
    public static long checksumBytes(MemorySegment segment) {
        long sum = 0;
        for (long i = 0; i < segment.byteSize(); i++) {
            sum += segment.get(ValueLayout.JAVA_BYTE, i);
        }
        return sum;
    }

    /**
     * Count occurrences of a byte value.
     */
    public static long countByte(MemorySegment segment, byte value) {
        long count = 0;
        for (long i = 0; i < segment.byteSize(); i++) {
            if (segment.get(ValueLayout.JAVA_BYTE, i) == value) {
                count++;
            }
        }
        return count;
    }
}
