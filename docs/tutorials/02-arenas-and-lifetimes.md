# 02 – Arenas and Lifetimes

An arena decides two things:

- **when** native memory becomes invalid
- **which threads** are allowed to touch that memory

This is the main replacement for the old `MemorySession` mental model from JEP 424.

## Arena Types

| Arena | Thread access | Lifetime |
|-------|---------------|----------|
| `ofConfined()` | Creating thread only | Until `close()` |
| `ofShared()` | Any thread | Until `close()` |
| `ofAuto()` | Any thread | GC-managed |
| `global()` | Any thread | Never |

## The Most Important Rule

When an arena closes, every native segment allocated from it becomes invalid.

That is what gives FFM **temporal safety**. Instead of silently using freed memory, the API can reject bad access patterns.

## Basic Pattern

```java
try (Arena arena = Arena.ofConfined()) {
    MemorySegment seg = arena.allocateFrom(ValueLayout.JAVA_INT, 42);
    // use seg
} // seg is no longer valid here
```

This is why short-lived work naturally fits `try-with-resources`.

## When To Use Each Arena

### `Arena.ofConfined()`

Use this by default.

It is ideal for:

- one request
- one method
- one benchmark setup scope
- simple examples and tests

It is the safest choice because only the creating thread can access the memory.

### `Arena.ofShared()`

Use this when multiple threads genuinely need the same native memory.

This repository demonstrates that idea in `net.szumigaj.java.panama.ffm.tutorial.threading.SharedArenaExample`.

Shared access does **not** mean automatic synchronization. It only means the segment is legally accessible from multiple threads.

### `Arena.ofAuto()`

Use this when explicit close is awkward and you can tolerate GC-driven cleanup.

It is convenient, but it is a poor default for performance-sensitive code because you lose precise lifetime control.

### `Arena.global()`

Use this only for process-wide state such as:

- long-lived symbol lookups
- shared immutable buffers
- objects intentionally kept for the life of the JVM

This repository uses `Arena.global()` for long-lived symbol lookup handles in the downcall examples.

## How This Shows Up In The Code

In `ArenaBasics.allocateAndReadInt(...)`, the arena is local and short-lived:

```java
try (Arena arena = Arena.ofConfined()) {
    MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_INT, value);
    return segment.get(ValueLayout.JAVA_INT, 0);
}
```

That is a good "beginner default" because it makes ownership obvious.

## Safety Model

FFM gives you several kinds of safety:

- **temporal safety**: use-after-close is rejected
- **thread-confinement safety**: confined arenas reject access from the wrong thread
- **spatial safety**: out-of-bounds access is checked against the segment size

You can still write incorrect interop code, but the API removes many classes of native-memory mistakes that were common in JNI-style code.

## JEP 424 Bridge

- `MemorySession.openConfined()` became `Arena.ofConfined()`
- `MemorySession.openShared()` became `Arena.ofShared()`
- `MemorySession.openImplicit()` became `Arena.ofAuto()`

The design goal stayed the same: explicit, scoped ownership of native memory.

## Common Mistakes

- returning a segment from a method after its confined arena was closed
- using `Arena.global()` just to avoid thinking about ownership
- benchmarking `ofAuto()` against explicit arenas without explaining the GC trade-off
- assuming `ofShared()` removes the need for synchronization when threads write

## Try Next

- read `03-layouts-varhandles-and-structs.md` to see how memory bytes become meaningful native structures
- compare this chapter with the JEP 424 mapping in `docs/migration/jep-424-to-java-25.md`
