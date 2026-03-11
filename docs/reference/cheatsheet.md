# FFM API Cheatsheet

Practical quick reference for the final java 22 `java.lang.foreign` API.

## Mental Model

- `Arena`: owns lifetime and thread-access rules for native memory.
- `MemorySegment`: a view over memory, either heap, native, pointer-based, or file-backed.
- `MemoryLayout`: describes the shape of native data.
- `ValueLayout`: describes scalar values such as `int`, `long`, `double`, and pointers.
- `Linker`: creates method handles for downcalls and upcalls.
- `SymbolLookup`: finds native symbols by name.
- `FunctionDescriptor`: describes a C function signature.

## Arena

### Pick the right arena

| API | Use it for | Notes |
|---|---|---|
| `Arena.ofConfined()` | Most examples, tests, request-scoped work | Single-threaded, explicit close |
| `Arena.ofShared()` | Multi-threaded access to native memory | Explicit close, shared access |
| `Arena.ofAuto()` | Convenience when explicit close is awkward | GC-managed lifetime |
| `Arena.global()` | Long-lived process-wide data | Never closes |

### Common arena operations

```java
Arena.ofConfined()
Arena.ofShared()
Arena.ofAuto()
Arena.global()

arena.allocate(bytes)
arena.allocate(bytes, alignment)
arena.allocateFrom(layout, values...)
arena.allocateFrom(String)
```

### Typical patterns

```java
try (Arena arena = Arena.ofConfined()) {
    MemorySegment value = arena.allocateFrom(ValueLayout.JAVA_INT, 42);
}
```

```java
try (Arena arena = Arena.ofConfined()) {
    MemorySegment utf8 = arena.allocateFrom("hello");
}
```

### Rules of thumb

- Prefer `try-with-resources` for `ofConfined()` and `ofShared()`.
- Use `Arena.global()` only for truly long-lived memory or symbol lookup.
- Do not compare `ofAuto()` directly with explicit arenas in benchmarks without calling out GC effects.

## MemorySegment

### Core operations

```java
segment.get(layout, offset)
segment.set(layout, offset, value)
segment.asSlice(offset, size)
segment.byteSize()
segment.reinterpret(size)

MemorySegment.ofArray(arr)
MemorySegment.copy(src, srcOff, dst, dstOff, size)
```

### Heap vs native segments

```java
MemorySegment heap = MemorySegment.ofArray(intArray);
```

```java
try (Arena arena = Arena.ofConfined()) {
    MemorySegment nativeSeg = arena.allocateFrom(ValueLayout.JAVA_INT, intArray);
}
```

### Read/write scalars

```java
int value = segment.get(ValueLayout.JAVA_INT, 0);
segment.set(ValueLayout.JAVA_INT, 0, 123);
```

```java
long offset = index * (long) Integer.BYTES;
int element = segment.get(ValueLayout.JAVA_INT, offset);
```

### Slice a region

```java
MemorySegment header = segment.asSlice(0, 16);
MemorySegment payload = segment.asSlice(16, segment.byteSize() - 16);
```

### Convert to Java arrays

```java
int[] ints = segment.toArray(ValueLayout.JAVA_INT);
byte[] bytes = segment.toArray(ValueLayout.JAVA_BYTE);
```

### Important pointer note

- Pointer values often arrive as zero-length `MemorySegment`s.
- Use `reinterpret(size)` before reading from such a pointer.

```java
int value = pointer.reinterpret(ValueLayout.JAVA_INT.byteSize())
        .get(ValueLayout.JAVA_INT, 0);
```

## ValueLayout

### Common scalar layouts

```java
ValueLayout.JAVA_BYTE
ValueLayout.JAVA_SHORT
ValueLayout.JAVA_INT
ValueLayout.JAVA_LONG
ValueLayout.JAVA_FLOAT
ValueLayout.JAVA_DOUBLE
ValueLayout.ADDRESS
```

### Pointer with target layout

```java
ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT)
```

Use target layouts when you want the descriptor or layout to say what a pointer points to.

## MemoryLayout and Structs

### Common layout factories

```java
MemoryLayout.structLayout(...)
MemoryLayout.sequenceLayout(count, elementLayout)
MemoryLayout.paddingLayout(bytes)
```

### Define a struct

```java
StructLayout POINT = MemoryLayout.structLayout(
    ValueLayout.JAVA_INT.withName("x"),
    ValueLayout.JAVA_INT.withName("y")
);
```

### Access struct fields with VarHandles

```java
VarHandle X = POINT.varHandle(MemoryLayout.PathElement.groupElement("x"));
VarHandle Y = POINT.varHandle(MemoryLayout.PathElement.groupElement("y"));
```

```java
X.set(segment, 0L, 10);
Y.set(segment, 0L, 20);
int x = (int) X.get(segment, 0L);
```

### Manual offset access

```java
int x = segment.get(ValueLayout.JAVA_INT, 0);
int y = segment.get(ValueLayout.JAVA_INT, 4);
```

Use manual offsets only when you are sure about ABI layout and padding.

## SymbolLookup

### Load a library and resolve symbols

```java
SymbolLookup lookup = SymbolLookup.libraryLookup(
    System.mapLibraryName("ffmplayground"),
    Arena.global()
);
```

```java
MemorySegment symbol = lookup.findOrThrow("ffm_add_i32");
```

### Other lookup styles

```java
SymbolLookup.libraryLookup(path, arena)
Linker.nativeLinker().defaultLookup()
SymbolLookup.loaderLookup()
```

### Rule of thumb

- Use `libraryLookup(..., Arena.global())` for repo-wide reusable handles.
- Use a confined arena when you want the library lifetime tied to a short scope.

## FunctionDescriptor

### Basic forms

```java
FunctionDescriptor.of(returnLayout, argLayouts...)
FunctionDescriptor.ofVoid(argLayouts...)
```

### Typical examples

```java
FunctionDescriptor.of(
    ValueLayout.JAVA_INT,
    ValueLayout.JAVA_INT,
    ValueLayout.JAVA_INT
)
```

```java
FunctionDescriptor.of(
    ValueLayout.JAVA_LONG,
    ValueLayout.ADDRESS
)
```

```java
FunctionDescriptor.ofVoid(
    ValueLayout.ADDRESS,
    ValueLayout.JAVA_LONG
)
```

### Struct rules

- Use a struct layout directly for struct-by-value parameters and returns.
- Use `ValueLayout.ADDRESS` for pointer parameters such as `Point*` or `int32_t*`.
- For struct-by-value returns, the downcall method handle receives a `SegmentAllocator` or `Arena` as the first argument.

## Linker

### Core APIs

```java
Linker.nativeLinker()
linker.downcallHandle(address, descriptor)
linker.downcallHandle(address, descriptor, options...)
linker.upcallStub(handle, descriptor, arena)
```

### Downcall recipe

```java
Linker linker = Linker.nativeLinker();
SymbolLookup lookup = SymbolLookup.libraryLookup(
    System.mapLibraryName("ffmplayground"),
    Arena.global()
);

MethodHandle add = linker.downcallHandle(
    lookup.findOrThrow("ffm_add_i32"),
    FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT
    )
);
```

### Upcall recipe

```java
MethodHandle callback = MethodHandles.lookup().findStatic(
    MyCallbacks.class,
    "compareInts",
    MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class)
);

MemorySegment stub = linker.upcallStub(
    callback,
    FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS
    ),
    arena
);
```

### Important restricted methods

These typically require native access to be enabled:

- `Linker.downcallHandle(...)`
- `Linker.upcallStub(...)`
- `MemorySegment.reinterpret(...)`
- `AddressLayout.withTargetLayout(...)`

## File-Backed Segments

```java
try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
     Arena arena = Arena.ofConfined()) {
    MemorySegment mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size(), arena);
}
```

Use file-backed segments for large read-heavy workloads such as scans, checksums, or parsing.

## Native Access Flag

```bash
--enable-native-access=ALL-UNNAMED
```

This repository configures the flag automatically for tests and benchmarks.

## Common C-to-FFM Mappings

| C concept | Java FFM |
|---|---|
| `int32_t` | `ValueLayout.JAVA_INT` |
| `int64_t` | `ValueLayout.JAVA_LONG` |
| `double` | `ValueLayout.JAVA_DOUBLE` |
| `char*` | `ValueLayout.ADDRESS` plus UTF-8 segment |
| `T*` | `ValueLayout.ADDRESS` |
| `struct T` by value | `StructLayout` |
| `void` return | `FunctionDescriptor.ofVoid(...)` |

## JEP 424 Quick Map

| JEP 424 | java 22 |
|---|---|
| `MemorySession` | `Arena` |
| `CLinker` | `Linker.nativeLinker()` |
| `MemoryAddress` | pointer-like `MemorySegment` |
| `allocateUtf8String` | `Arena.allocateFrom(String)` |

## Repo Examples

- Segments and arenas: `ffm-examples/src/main/java/net/szumigaj/java/panama/ffm/tutorial/segments/`
- Strings and `strlen`: `ffm-examples/src/main/java/net/szumigaj/java/panama/ffm/tutorial/strings/`
- Scalar downcalls: `ffm-examples/src/main/java/net/szumigaj/java/panama/ffm/tutorial/downcalls/`
- Struct layouts: `ffm-examples/src/main/java/net/szumigaj/java/panama/ffm/tutorial/structs/`
- Upcalls: `ffm-examples/src/main/java/net/szumigaj/java/panama/ffm/tutorial/upcalls/`

## Common Pitfalls

- Using a struct layout for a pointer parameter. Use `ValueLayout.ADDRESS`.
- Reading directly from a zero-length pointer segment. Use `reinterpret(size)` first.
- Forgetting to close confined/shared arenas.
- Benchmarking setup work inside the benchmark method body.
- Comparing `Arena.ofAuto()` directly against explicit arenas without mentioning GC behavior.
