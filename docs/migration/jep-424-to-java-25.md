# JEP 424 to Java 25 Migration Guide

This guide maps JEP 424 (Foreign Function & Memory API, Preview) concepts to the final Java 25 API.

## Overview

The FFM API was introduced as a preview in JEP 424 (JDK 19) and finalized in JEP 454 (JDK 22). Java 25 uses the final API without preview flags.

## Type Mapping

| JEP 424 (Preview) | Java 25 (Final) |
|------------------|-----------------|
| `MemorySession` | `Arena` |
| `MemorySession.openConfined()` | `Arena.ofConfined()` |
| `MemorySession.openShared()` | `Arena.ofShared()` |
| `MemorySession.openImplicit()` | `Arena.ofAuto()` |
| `CLinker` | `Linker.nativeLinker()` |
| `MemoryAddress` | `MemorySegment` (pointer-like, zero-length) |
| `Addressable` | Largely removed; use `MemorySegment` |
| `SymbolLookup.libraryLookup(path, loader)` | `SymbolLookup.libraryLookup(path, Arena)` |
| `allocateUtf8String(String)` | `Arena.allocateFrom(String)` |
| `toArray(ValueLayout)` | `MemorySegment.toArray(ValueLayout)` | 

## Arena Lifecycle

- **JEP 424**: `MemorySession` controlled lifetime; `close()` deallocated.
- **Java 25**: `Arena` does the same. Use `try-with-resources` for confined/shared arenas.

## Symbol Lookup

- **JEP 424**: `SymbolLookup.libraryLookup(Path, ClassLoader)` or similar.
- **Java 25**: `SymbolLookup.libraryLookup(String name, Arena arena)` or `libraryLookup(Path path, Arena arena)`. The library is unloaded when the arena is closed. Use `Arena.global()` for long-lived lookups.

## Downcalls

- **JEP 424**: `CLinker.downcallHandle(address, ...)`.
- **Java 25**: `Linker.downcallHandle(segment, FunctionDescriptor)` or `(segment, FunctionDescriptor, options)`. For struct-by-value returns, the allocator is inserted as the first parameter.

## Upcalls

- **JEP 424**: `CLinker.upcallStub(handle, descriptor, session)`.
- **Java 25**: `Linker.upcallStub(handle, descriptor, arena)`.

## Removed / Renamed

- `MemoryAddress` – no separate type; use `MemorySegment` for pointers.
- `ValueLayout.OfAddress` – use `ValueLayout.ADDRESS`.
- `CLinker` – use `Linker.nativeLinker()`.

## No Preview Flags

Java 25 FFM is final. Do **not** use `--enable-preview` for FFM.

## Native Access

Restricted methods still require:

```
--enable-native-access=ALL-UNNAMED
```

This is needed for `downcallHandle`, `upcallStub`, `reinterpret`, and certain layout operations.
