# 06 – Upcalls and Callbacks

An upcall is the reverse of a downcall: native code calls back into Java.

This is one of the most powerful parts of the FFM API, because it lets Java participate in native algorithms instead of only invoking them.

## What You Learn

- how to export a Java method as a native callback
- how callback lifetime is tied to an arena
- why pointer arguments often arrive as zero-length segments
- why upcalls are usually much more expensive than plain downcalls

## Repository Example

See `net.szumigaj.java.panama.ffm.tutorial.upcalls.QSortUpcalls`.

The example uses native `qsort` with a Java comparator:

1. Java allocates a native array
2. Java builds a callback stub with `Linker.upcallStub(...)`
3. native `qsort` repeatedly calls back into Java to compare elements

That means one sort performs many boundary crossings.

## Callback Descriptor

The comparator descriptor is:

```java
FunctionDescriptor.of(
    ValueLayout.JAVA_INT,
    ValueLayout.ADDRESS,
    ValueLayout.ADDRESS
)
```

This matches the C comparator shape:

```c
int compare(const void* a, const void* b)
```

Notice the parameters are pointers, so the descriptor uses `ValueLayout.ADDRESS`.

## Building The Stub

The example follows this pattern:

```java
MethodHandle callback = MethodHandles.lookup().findStatic(...);
MemorySegment stub = linker.upcallStub(callback, descriptor, arena);
```

The arena is important. It owns the callback stub. Once that arena closes, the native side must stop using the stub.

## Zero-Length Pointer Segments

Inside the comparator implementation:

```java
int va = a.reinterpret(ValueLayout.JAVA_INT.byteSize()).get(ValueLayout.JAVA_INT, 0);
```

This is necessary because pointer arguments passed into upcalls often arrive as zero-length segments. They represent an address, but Java does not automatically know how many bytes may be safely read through that pointer.

So the pattern is:

1. receive pointer-like `MemorySegment`
2. reinterpret it to the expected readable size
3. access through the right `ValueLayout`

## Why Upcalls Are Expensive

Upcalls are normally much slower than pure Java logic because each comparison or callback involves:

- native code entering Java
- argument adaptation
- possible pointer reinterpretation
- returning back into native code

That is why the benchmark suite compares `Arrays.sort(...)` against the `qsort` upcall path. It is a good teaching example, not a recommendation for high-throughput sorting.

## JEP 424 Bridge

- `CLinker.upcallStub(...)` became `Linker.upcallStub(...)`
- arenas now manage stub lifetime instead of sessions
- the high-level idea stayed the same

## Common Mistakes

- closing the arena while native code still holds the callback pointer
- reading directly from a zero-length pointer segment
- using a struct descriptor where the callback actually receives pointers
- assuming a native algorithm with frequent callbacks will outperform pure Java

## Try Next

- read `07-confinement-sharing-and-safety.md` to understand how arena thread rules interact with native memory access
- compare `QSortUpcalls` with `QSortUpcallBenchmark` to see how correctness and performance differ
