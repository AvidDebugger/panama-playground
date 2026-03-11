# 03 – Layouts, VarHandles, and Structs

Native memory is just bytes. Layouts describe what those bytes mean.

This chapter is where the API starts feeling like "real interop" instead of raw memory access. Once you can model C structs correctly, you can pass meaningful data to native code.

## What You Learn

- how to describe a native struct with `MemoryLayout.structLayout(...)`
- how named fields make layouts easier to work with
- how `VarHandle` gives safer field access than hand-written offsets
- when manual offsets are still useful

## Key Types

- `MemoryLayout.structLayout(...)` – define a struct shape
- `ValueLayout` – define scalar field types
- `MemoryLayout.PathElement.groupElement(...)` – select a named field
- `VarHandle` – read or write fields through the layout

## Repository Example

The main example is `net.szumigaj.java.panama.ffm.tutorial.structs.PointLayout`.

It models this C type:

```c
typedef struct {
    int32_t x;
    int32_t y;
} Point;
```

The Java side is:

```java
StructLayout POINT = MemoryLayout.structLayout(
    ValueLayout.JAVA_INT.withName("x"),
    ValueLayout.JAVA_INT.withName("y")
);
```

## Why Names Matter

By naming the fields, the code can create field handles like this:

```java
VarHandle X = POINT.varHandle(MemoryLayout.PathElement.groupElement("x"));
VarHandle Y = POINT.varHandle(MemoryLayout.PathElement.groupElement("y"));
```

That makes the access code easier to read and less brittle than remembering `"field 0"` and `"field 1"`.

## Working With Arrays Of Structs

`PointLayout.setPoint(...)` and `getX(...)` / `getY(...)` use:

```java
MemorySegment slice = segment.asSlice(offset, LAYOUT.byteSize());
```

This is an important pattern.

If one segment contains many structs, then:

- `offset` chooses which struct
- `asSlice(...)` narrows the view to just that struct
- the `VarHandle` operates within that smaller view

That is much easier to reason about than indexing into one giant block by hand every time.

## VarHandles vs Manual Offsets

Both are valid:

### VarHandle style

```java
X.set(slice, 0L, x);
int value = (int) X.get(slice, 0L);
```

### Manual offset style

```java
int x = segment.get(ValueLayout.JAVA_INT, offset);
int y = segment.get(ValueLayout.JAVA_INT, offset + 4);
```

Use `VarHandle` when:

- you want clearer code
- field names matter
- you are teaching or reviewing layout logic

Use manual offsets when:

- the layout is trivial
- you are doing low-level performance experiments
- you already proved the offsets are correct

## Alignment and Padding

This `Point` struct is simple, so there is no explicit padding in the example. Real native structs often include:

- alignment requirements
- inserted padding
- ABI-dependent size rules

That is why it is better to encode the struct shape explicitly in Java instead of assuming field positions from memory.

## JEP 424 Bridge

This part of the API barely changed conceptually:

- layouts are still layouts
- `VarHandle` remains the preferred field-access tool
- the big change in modern examples is around arenas and linker APIs, not around struct modeling

## Common Mistakes

- forgetting that offsets are byte offsets
- using manual offsets without checking struct size and alignment
- modeling `T*` as a struct layout instead of as `ValueLayout.ADDRESS`
- skipping named fields, then losing readability in later code

## Try Next

- open `04-downcalls-and-symbol-lookups.md` to see these layouts used in foreign calls
- compare `PointLayout` with `PointDowncalls` to see the difference between struct-by-value and pointer parameters
