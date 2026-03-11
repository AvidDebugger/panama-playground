# Troubleshooting

## Native library not found

**Symptom**: `UnsatisfiedLinkError` or `NoClassDefFoundError` when loading `ffmplayground`.

**Fix**:
1. Build the native library: `./gradlew buildNative` (requires CMake).
2. Ensure `java.library.path` or `LD_LIBRARY_PATH` (Linux) / `DYLD_LIBRARY_PATH` (macOS) includes `native/build/`.
3. The Gradle test and JMH tasks set this automatically.

## Restricted method warnings

**Symptom**: Warnings when calling `downcallHandle`, `upcallStub`, `reinterpret`, etc.

**Fix**: Add `--enable-native-access=ALL-UNNAMED` to JVM args. The build configures this for tests and benchmarks.

## CMake not found

**Symptom**: `buildNative` fails with "cmake: not found".

**Fix**: Install CMake and a C compiler. On Ubuntu: `sudo apt install cmake build-essential`. On macOS: `brew install cmake`.

## Java 25 not found

**Symptom**: Gradle reports "Could not find Java 25".

**Fix**: Install JDK 25. The project uses the Java toolchain; Gradle will auto-download if configured, or use SDKMAN: `sdk install java 25.0.2-tem`.
