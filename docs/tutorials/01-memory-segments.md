# 01 – Memory Segments

`MemorySegment` is the core data abstraction in the FFM API. If you understand segments, layouts, and byte offsets, the rest of the API becomes much easier to reason about.

## What You Learn

- the difference between heap-backed and native segments
- why a segment is "just bytes" until you read it with a layout
- how slicing lets you work with sub-regions safely
- why arenas matter even in the first examples

## Core Ideas

- `MemorySegment` represents a contiguous region of memory.
- `ValueLayout` tells Java how to interpret bytes at a given offset.
- offsets are in **bytes**, not in element indices.
- native segments have an explicit lifetime; heap segments do not.

In this repository, the entry point for this chapter is `net.szumigaj.java.panama.ffm.tutorial.segments.ArenaBasics`.

## Walkthrough

### 1. Allocate a single value

The simplest example is:

```java
try (Arena arena = Arena.ofConfined()) {
    MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_INT, 42);
    int value = segment.get(ValueLayout.JAVA_INT, 0);
}
```

This teaches the basic pattern:

1. choose an arena
2. allocate memory
3. read or write through a layout
4. close the arena

### 2. Allocate an array

`ArenaBasics.allocateIntArrayAndSum(...)` uses:

```java
MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_INT, values);
```

Then it walks the segment with byte offsets:

```java
segment.get(ValueLayout.JAVA_INT, (long) i * Integer.BYTES);
```

That multiplication is important. FFM offsets are byte positions, so `i` alone would be wrong.

### 3. Slice a larger block

`ArenaBasics.sliceAndRead(...)` shows:

```java
block.asSlice(offset, ValueLayout.JAVA_INT.byteSize())
```

Use slices when a large segment contains smaller logical objects such as:

- headers plus payloads
- arrays of structs
- subranges of a mapped file

### 4. Heap vs native segments

This chapter also contrasts:

- `MemorySegment.ofArray(arr)` for heap-backed storage
- `arena.allocateFrom(ValueLayout.JAVA_INT, arr)` for native storage

Both can be accessed with the same `get(...)` and `set(...)` style APIs, which is one of the nicest parts of FFM. The big difference is ownership:

- heap-backed segments are managed by the JVM
- native segments are managed by your arena

## Why This Matters

Most of the later tutorials build on these same ideas:

- downcalls pass `MemorySegment`s to native code
- struct layouts describe how segment bytes are organized
- pointers often arrive as `MemorySegment`s too
- file mappings are just another kind of segment

If this chapter feels too low-level, that is normal. The payoff is that Java can now work with native memory using regular APIs instead of JNI glue.

## JEP 424 Bridge

- `MemorySession` became `Arena`
- the core idea of "segments plus layouts plus scoped lifetime" stayed the same
- Java 25 uses the final API in `java.lang.foreign`, so there is no preview syntax here

## Common Mistakes

- treating the offset as an element index instead of a byte offset
- using a native segment after the arena was closed
- assuming heap-backed and native segments are interchangeable for every foreign call
- forgetting that `asSlice(...)` creates a view, not a copy

## Try Next

- continue with `02-arenas-and-lifetimes.md` to understand when segments become invalid
- compare `heapSegmentFromInts(...)` and `nativeSegmentFromInts(...)` in the code and think about which one should be passed to native functions
