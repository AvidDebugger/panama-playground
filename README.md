# Java 25 Foreign Function & Memory API Playground

A learning repository for the **Foreign Function & Memory (FFM) API** in java 22, with performance benchmarks and educational examples. The API was finalized in Java 22 (JEP 454); this playground uses the final `java.lang.foreign` package and maps concepts back to the original JEP 424 preview.

## Prerequisites

- **JDK 25** (or later)
- **Gradle 9.4.0** (wrapper included)
- **CMake** (for building the native companion library)
- **C toolchain** (gcc/clang on Linux/macOS, MSVC on Windows)

## Quick Start

```bash
# Build the native library (requires CMake)
./gradlew buildNative

# Run unit tests
./gradlew :ffm-examples:test

# Run integration tests (require native library)
./gradlew :ffm-examples:integrationTest

# Run JMH benchmarks
./gradlew :ffm-benchmarks:jmh
```

## Project Structure

```
java-panama/
├── ffm-examples/          # Tutorial examples and teaching code
│   └── src/main/java/net/szumigaj/java/panama/ffm/
│       ├── support/       # NativeLib loader
│       └── tutorial/      # Chapter-style examples
│           ├── segments/   # Arenas, segments, layouts
│           ├── strings/    # String marshalling, strlen
│           ├── downcalls/  # Scalar downcalls
│           ├── structs/    # Struct layouts, Point
│           └── perf/       # Sum, copy, fill
├── ffm-benchmarks/       # JMH microbenchmarks
│   └── src/jmh/java/net/szumigaj/java/panama/ffm/bench/
├── native/               # C companion library (ffmplayground)
│   ├── include/ffmplayground/
│   ├── src/
│   └── scripts/
└── docs/                 # Tutorials and migration guide
```

## Learning Path

1. **Arenas and segments** – `ArenaBasics`, `Arena.ofConfined/ofShared/ofAuto`
2. **Strings** – `StringDowncalls`, `Arena.allocateFrom(String)`, `strlen`
3. **Downcalls** – `ScalarDowncalls`, `Linker.downcallHandle`, `FunctionDescriptor`
4. **Structs** – `PointLayout`, `MemoryLayout.structLayout`, VarHandles
5. **Performance** – `PerfDowncalls`, memory copy, allocation strategies

See [Tutorials index](docs/tutorials/index.md) for detailed walkthroughs and [docs/migration/jep-424-to-java-25.md](docs/migration/jep-424-to-java-25.md) for JEP 424 → java 22 mapping.

## Documentation

- [Getting Started](docs/getting-started.md)
- [FFM Cheatsheet](docs/reference/cheatsheet.md)
- [Troubleshooting](docs/reference/troubleshooting.md)
- [JEP 424 to java 22 Migration Guide](docs/migration/jep-424-to-java-25.md)
- [Benchmark Notes](docs/tutorials/09-jmh-and-performance.md)

## Benchmarks

| Family | Benchmarks |
|--------|------------|
| Memory read | `int[]`, heap segment, native segment, direct ByteBuffer |
| Memory copy | `System.arraycopy`, `MemorySegment.copy`, direct buffer |
| Allocation | confined arena, reused arena, slicing allocator, direct buffer |
| Downcalls | Java add, FFM add, noop, strlen (marshalled vs reused) |
| Struct access | VarHandles vs manual offsets |

Run with:

```bash
./gradlew :ffm-benchmarks:jmh
```

Results are written to `ffm-benchmarks/build/reports/jmh/`.

## JEP 424 → java 22 Mapping

| JEP 424 (Preview) | java 22 (Final) |
|-------------------|-----------------|
| `MemorySession` | `Arena` |
| `CLinker` | `Linker.nativeLinker()` |
| `allocateUtf8String` | `Arena.allocateFrom(String)` |
| `MemoryAddress` | `MemorySegment` (pointer-like) |
| `SymbolLookup.libraryLookup(name, loader)` | `SymbolLookup.libraryLookup(name, Arena)` |

See [docs/migration/jep-424-to-java-25.md](docs/migration/jep-424-to-java-25.md) for the full mapping.

## License

MIT
