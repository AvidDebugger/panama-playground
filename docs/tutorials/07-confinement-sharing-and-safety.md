# 07 – Confinement, Sharing, and Safety

One of the biggest improvements in the FFM API over older native interop styles is that memory access is not just "unsafe raw pointers". The API encodes ownership, bounds, and thread rules.

## Three Safety Dimensions

### 1. Spatial safety

You cannot legally read past the end of a segment. The segment knows its size.

### 2. Temporal safety

You cannot legally use native memory after its arena closes.

### 3. Thread-confinement safety

Confined arenas enforce that only the creating thread can access their segments.

## Confined vs Shared

- **Confined arena**: Only the creating thread may access segments.
- **Shared arena**: Any thread may access segments; synchronization is still the caller's responsibility.

## Repository Example

See `net.szumigaj.java.panama.ffm.tutorial.threading.SharedArenaExample`.

This class demonstrates two useful ideas:

- creating a shared segment filled with data
- splitting a read workload across multiple Java threads

The method:

```java
parallelSum(Arena arena, MemorySegment segment, int count)
```

partitions the buffer and lets several threads read disjoint ranges.

## What `Arena.ofShared()` Actually Means

`Arena.ofShared()` means:

- multiple threads may access the segment without a confinement violation

It does **not** mean:

- automatic locking
- atomic updates
- race-free writes

If several threads write to the same region, normal concurrency rules still apply.

## Why This Matters For Performance

Shared access is safer and clearer than raw native pointers, but it is not free. Shared arenas may introduce extra synchronization costs, which is why the benchmark docs warn against comparing them directly with confined arenas without explanation.

## How To Choose

Use `Arena.ofConfined()` when:

- one thread owns the work
- you want the simplest and safest model
- you are building most examples and tests

Use `Arena.ofShared()` when:

- multiple threads genuinely need one native region
- copying would be more expensive or less clear

Use `Arena.global()` only for very long-lived shared state.

## JEP 424 Bridge

The same ideas existed in the preview API:

- confined scope -> confined arena
- shared scope -> shared arena

The names changed, but the conceptual model is the same.

## Common Mistakes

- using a confined segment from another thread
- choosing shared memory when simple copying would be clearer
- assuming "shared" also means "thread-safe writes"
- forgetting that safety checks can change benchmark results if you compare unlike scenarios

## Related Topics

- `01-memory-segments.md` explains the segment abstraction itself
- `02-arenas-and-lifetimes.md` explains when segments become invalid
- `09-jmh-and-performance.md` explains why shared and confined setups should be benchmarked carefully
