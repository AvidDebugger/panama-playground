# 08 – File-Backed Segments

`MemorySegment` is not limited to heap arrays or manually allocated native memory. It can also represent a memory-mapped region of a file.

That is useful when you want:

- large-file scans
- parsing without reading the whole file into heap memory
- random access to file content
- OS-managed paging instead of manual buffering

## Repository Example

See `net.szumigaj.java.panama.ffm.tutorial.file.FileBackedSegmentExample`.

The core mapping method is:

```java
try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
    long size = channel.size();
    return channel.map(FileChannel.MapMode.READ_ONLY, 0, size, arena);
}
```

## What You Learn

- how to map a file into a `MemorySegment`
- how file-backed segments still use the same `get(...)` and `asSlice(...)` APIs
- why arena lifetime matters even for mapped files
- when mapping is a better fit than buffered reads

## Why This Fits The FFM Mental Model

A file-backed segment is still just a `MemorySegment`.

That means the same operations you already learned still apply:

- `segment.byteSize()`
- `segment.get(ValueLayout.JAVA_BYTE, offset)`
- `segment.asSlice(...)`

The difference is where the bytes come from:

- heap segment -> JVM heap
- native segment -> off-heap allocation
- file-backed segment -> mapped file region

## Lifetime Rules

The mapped region is tied to the arena used in `FileChannel.map(...)`.

So the same lifetime rule still applies:

- when the arena closes, the mapped segment becomes invalid

That consistency is one of the nicest design wins of the FFM API.

## Repository Utilities

The example class includes two simple file-style workloads:

### `checksumBytes(...)`

Walks the mapped region and sums the bytes.

This is a good first benchmark-style workload because it is easy to verify and obviously read-heavy.

### `countByte(...)`

Counts occurrences of a single byte value.

This is a good example of scanning logic that does not need to copy file content into a heap array first.

## When To Use File Mapping

Good use cases:

- large sequential scans
- repeated random access to one file
- parsing binary formats
- zero-copy-style pipelines where copying would dominate

Less attractive use cases:

- tiny files
- one-shot reads where ordinary I/O is simpler
- workloads dominated by parsing logic rather than data movement

## JEP 424 Bridge

The modern FFM API integrates nicely with file-backed segments through `FileChannel.map(..., Arena)`. The important conceptual point is unchanged: the file region becomes a memory segment, so the rest of your code can use the same memory-access model as native allocations.

## Common Mistakes

- forgetting that the mapped segment lifetime is still arena-scoped
- assuming a mapped file becomes a heap array automatically
- reading with the wrong layout or offset size
- using mapping for tiny or purely one-shot I/O where ordinary reads would be simpler

## Try Next

- compare this chapter with `01-memory-segments.md` to see how many APIs are shared across all segment kinds
- pair this with `09-jmh-and-performance.md` if you want to benchmark file scans against buffered I/O or `MappedByteBuffer`
