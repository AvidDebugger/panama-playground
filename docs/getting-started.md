# Getting Started

## Prerequisites

- **JDK 25** – The FFM API is final; no preview flags needed.
- **Gradle 9.4.0** – Use the included wrapper (`./gradlew`).
- **CMake** – For building the native `ffmplayground` library.
- **C compiler** – gcc/clang on Linux/macOS, MSVC on Windows.

## Build the Native Library

```bash
./gradlew buildNative
```

Or build manually:

```bash
cd native
mkdir -p build && cd build
cmake ..
cmake --build .
```

The library is produced as `native/build/libffmplayground.so` (Linux), `libffmplayground.dylib` (macOS), or `ffmplayground.dll` (Windows).

## Run Tests

```bash
# Unit tests (no native required)
./gradlew :ffm-examples:test

# Integration tests (require native library)
./gradlew :ffm-examples:integrationTest
```

## Run Benchmarks

```bash
./gradlew :ffm-benchmarks:jmh
```

Results are in `ffm-benchmarks/build/reports/jmh/`.

## JVM Flags

The FFM API requires `--enable-native-access=ALL-UNNAMED` for restricted methods (downcalls, upcalls, etc.). The build configures this for tests and benchmarks automatically.

## Read Next

- [Cheatsheet](reference/cheatsheet.md) for the everyday API reference
- [Troubleshooting](reference/troubleshooting.md) for common setup and runtime issues
- [JEP 424 to java 22 Migration Guide](migration/jep-424-to-java-22.md) for older preview-era examples
