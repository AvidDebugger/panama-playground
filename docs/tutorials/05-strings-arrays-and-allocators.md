# 05 – Strings, Arrays, and Allocators

Most real native calls do not just pass scalars. They pass pointers to strings, arrays, and buffers. This chapter is about preparing those inputs correctly and efficiently.

## What You Learn

- how Java strings become native UTF-8 memory
- how Java arrays become native contiguous buffers
- how allocator choice affects lifetime and performance
- why "per call marshalling" and "reused native memory" are different workloads

## Strings

The repository uses `net.szumigaj.java.panama.ffm.tutorial.strings.StringDowncalls` as the main example.

The core operation is:

```java
MemorySegment utf8 = arena.allocateFrom("hello");
```

That gives you a native UTF-8 segment suitable for pointer-style C APIs such as `strlen`.

### Important properties

- the bytes live in the arena you used
- the native side receives a pointer-like segment
- the segment must outlive the foreign call that uses it

In practice that means:

- one-off calls can allocate inside a local confined arena
- repeated calls in a benchmark often reuse one pre-allocated segment

## Arrays

For arrays, the common pattern is:

```java
MemorySegment ints = arena.allocateFrom(ValueLayout.JAVA_INT, values);
```

Now the array is in native memory, laid out as a flat contiguous block.

To read it back element by element:

```java
int value = ints.get(ValueLayout.JAVA_INT, index * (long) Integer.BYTES);
```

To copy data back to Java:

```java
int[] copy = ints.toArray(ValueLayout.JAVA_INT);
```

## Reused Memory vs Per-Call Marshalling

This repository intentionally exposes both:

- `StringDowncalls.strlen(String)` allocates a fresh UTF-8 segment every call
- `StringDowncalls.strlenSegment(MemorySegment)` reuses a prepared segment

That difference matters a lot in performance work:

- the first path measures allocation + encoding + downcall
- the second path measures mostly the downcall itself

The benchmark suite uses both so the cost model stays honest.

## Allocators

Every `Arena` is also a `SegmentAllocator`, which means it can hand out memory. In addition, you can build specialized allocators on top of a larger block.

The benchmark module uses:

```java
SegmentAllocator.slicingAllocator(block)
```

That is useful when:

- you need many tiny temporary allocations
- one larger block can be reserved up front
- you want to amortize allocation cost

## Repository Example Beyond Strings

`net.szumigaj.java.panama.ffm.tutorial.perf.PerfDowncalls` shows classic pointer-plus-count functions:

- `sumI32(segment, count)`
- `copyI32(dst, src, count)`
- `fillI32(segment, count, value)`

That is a very common C interop pattern:

- a pointer to a buffer
- a length
- sometimes an output buffer plus input buffer

## JEP 424 Bridge

- `allocateUtf8String(...)` became `Arena.allocateFrom(String)`
- the general pointer + segment + lifetime model stayed the same

## Common Mistakes

- forgetting that a native string or array dies when its arena closes
- reusing a segment after the arena went out of scope
- confusing element count with byte count when building offsets
- benchmarking allocation and call overhead together without saying so

## Try Next

- continue with `06-upcalls-and-callbacks.md` to see the opposite direction: native code calling back into Java
- inspect `DowncallBenchmark` to see why this chapter matters for performance interpretation
