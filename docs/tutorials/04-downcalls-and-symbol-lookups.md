# 04 – Downcalls and Symbol Lookups

A downcall is a call from Java into native code.

This is the heart of the FFM API: find a native symbol, describe its signature, turn it into a `MethodHandle`, and invoke it like a normal JVM call site.

## The Basic Pipeline

Every downcall in this repository follows the same steps:

1. load or expose a native library
2. resolve a symbol by name
3. describe the C signature with a `FunctionDescriptor`
4. ask the `Linker` for a downcall handle
5. invoke the resulting `MethodHandle`

## Key APIs

- `Linker.nativeLinker()` – platform-specific linker
- `SymbolLookup.libraryLookup(name, Arena)` – load a native library and expose its symbols
- `lookup.findOrThrow(symbol)` – resolve one symbol by name
- `Linker.downcallHandle(...)` – produce a `MethodHandle`
- `FunctionDescriptor.of(...)` / `ofVoid(...)` – describe the native signature

## Scalar Example

See `net.szumigaj.java.panama.ffm.tutorial.downcalls.ScalarDowncalls`.

The `int add(int, int)` case looks like this:

```java
MethodHandle add = linker.downcallHandle(
    lookup.findOrThrow("ffm_add_i32"),
    FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT
    )
);
```

This is the cleanest possible downcall:

- all arguments are scalars
- the return value is a scalar
- no pointers, no structs, no lifetime issues beyond the library handle itself

## String Example

See `net.szumigaj.java.panama.ffm.tutorial.strings.StringDowncalls`.

The `strlen` example uses:

```java
FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
```

That is a good example of a common interop rule:

- `char*` maps to `ValueLayout.ADDRESS`
- the actual bytes come from a `MemorySegment` that contains a UTF-8 string

## Struct Example

See `net.szumigaj.java.panama.ffm.tutorial.structs.PointDowncalls`.

This example is useful because it shows both kinds of native parameter:

- `Point` by value uses the struct layout directly
- `Point*` and `int32_t*` use `ValueLayout.ADDRESS`

That distinction is extremely important. If a native function takes a pointer, the descriptor should say "pointer", not "struct".

## Struct-By-Value Return

`PointDowncalls.pointAdd(...)` also shows a special FFM rule:

- when a native function returns a struct by value
- the downcall handle receives an allocator as the first Java argument

That is why the Java method signature is:

```java
pointAdd(Arena arena, MemorySegment a, MemorySegment b)
```

instead of just `(a, b)`.

## Symbol Lookup Lifetime

This repository uses:

```java
SymbolLookup.libraryLookup(System.mapLibraryName("ffmplayground"), Arena.global())
```

That is deliberate:

- symbol handles are reused across calls
- the library stays available for the lifetime of the process
- the tutorial code stays simple

In shorter-lived tools, you could bind the lookup to a confined arena instead.

## Native Access

These calls use restricted FFM operations, so Java requires:

```bash
--enable-native-access=ALL-UNNAMED
```

The Gradle tasks in this repository already add that flag for tests and benchmarks.

## JEP 424 Bridge

- `CLinker` became `Linker.nativeLinker()`
- modern code uses `SymbolLookup.libraryLookup(..., Arena)` instead of older loader-based patterns
- the overall idea of "symbol + descriptor + method handle" stayed the same

## Common Mistakes

- using a struct layout where the C API actually expects a pointer
- forgetting to keep the library/symbol lookup alive long enough
- building descriptors with the wrong return type or argument order
- benchmarking symbol lookup or handle construction inside the measured method

## Try Next

- continue with `05-strings-arrays-and-allocators.md` for more realistic pointer-backed arguments
- compare `ScalarDowncalls` and `PointDowncalls` to see how complexity grows from scalars to structs
