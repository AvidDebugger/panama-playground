# Native Companion Library (ffmplayground)

A small C library for the Java 25 Foreign Function & Memory API playground.

## Build

```bash
mkdir -p build && cd build
cmake ..
cmake --build .
```

Or use the platform scripts:

- Linux: `./scripts/build-linux.sh`
- macOS: `./scripts/build-macos.sh`
- Windows: `.\scripts\build-windows.ps1`

## Output

- Linux: `build/libffmplayground.so`
- macOS: `build/libffmplayground.dylib`
- Windows: `build/ffmplayground.dll`

## Usage from Java

Set `java.library.path` or `LD_LIBRARY_PATH` (Linux) / `DYLD_LIBRARY_PATH` (macOS) to include the `build/` directory.
