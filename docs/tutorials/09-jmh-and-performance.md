# JMH and Performance

This repository includes JMH benchmarks to answer specific interop questions, not to produce one giant "FFM is faster/slower" headline.

Good FFM benchmarking separates several costs that are easy to accidentally mix together:

- Java baseline cost
- native memory access cost
- marshalling cost
- boundary crossing cost
- callback/upcall cost

## What The Benchmark Module Covers

The benchmark classes in `ffm-benchmarks` are intentionally small and focused:

- `AllocationBenchmark` – tiny off-heap allocation strategies
- `MemoryReadBenchmark` – sequential reads from heap, native memory, and direct buffers
- `MemoryCopyBenchmark` – bulk copy comparisons
- `DowncallBenchmark` – tiny scalar calls and string marshalling
- `StructAccessBenchmark` – VarHandles vs manual offsets
- `QSortUpcallBenchmark` – Java sort vs native `qsort` with a Java callback

Each class now documents:

- what it measures
- what baseline it compares against
- what hypothesis the result should support

## How To Think About FFM Benchmarks

Ask a narrow question first.

Good questions:

- how much does UTF-8 marshalling add on top of a `strlen` call?
- how close is `MemorySegment.copy(...)` to `System.arraycopy(...)`?
- how much slower is a callback-heavy upcall path than pure Java sorting?

Bad questions:

- is FFM fast?
- is off-heap always better?
- is native code always better than Java?

## Benchmarking Pitfalls

### 1. Setup placement

- **Do not** link symbols or create `MethodHandle`s inside the benchmarked method.
- **Do** perform all setup in `@Setup` methods.
- Linker lookup and downcall handle creation are expensive; do them once.
- The same applies to input preparation such as copying arrays before sorting if that is not the thing you want to measure.

### 2. Marshaling vs call cost

- Separate marshaling cost from native call cost.
- Benchmark both **reused native inputs** and **allocate-per-call** inputs.
- In this repository, `strlen_perCallMarshalling` and `strlen_reusedSegment` exist specifically to show that difference.

### 3. Dead-code elimination

- Return values from benchmarks or use JMH `Blackhole`.
- Avoid benchmarks that compute nothing useful.
- Be suspicious of impossibly tiny baselines such as constant expressions or foldable code paths.

### 4. Layout correctness

- Wrong layouts or padding can produce undefined behavior.
- A fast benchmark can still be invalid if the layout does not match the native ABI.
- Verify offsets and sizes against native expectations when in doubt.

### 5. Arena lifetime

- `Arena.ofAuto()` includes GC-driven lifetime effects; compare it separately from explicit arenas.
- Shared arenas add synchronization costs; do not compare them directly to confined arenas without saying so.

### 6. Shared arena synchronization

- Shared arena scans add synchronization overhead.
- Use `@State(Scope.Thread)` vs `@State(Scope.Benchmark)` intentionally.

### 7. Warmup and iteration counts

- FFM and `MethodHandle` paths need real warmup.
- Report warmup iterations, measurement iterations, and fork count with your results.

### 8. Batched work vs reported units

- If one invocation performs 100 allocations or 1024 reads, raw `ns/op` describes the whole batch.
- Use `@OperationsPerInvocation(...)` so JMH normalizes results to the logical unit you care about.
- This repository now does that for the batched allocation, read, copy, and struct-scan benchmarks.

## Reading The Benchmarks In This Repo

### Allocation

- `allocate_confinedArena` asks: "What if I create a fresh confined arena for each tiny allocation?"
- `allocate_reusedConfinedArena` asks: "What if one arena serves many tiny allocations?"
- `allocate_slicingAllocator` asks: "What if I pre-allocate one block and just carve slices from it?"
- `allocate_directBuffer` is a non-FFM off-heap baseline.

### Memory Read

- `read_heapArray` is the plain Java baseline.
- `read_heapSegment` shows the cost of a segment view over heap memory.
- `read_nativeSegment` shows native off-heap access via FFM.
- `read_directByteBuffer` provides an older off-heap comparison point.

### Memory Copy

- `copy_arrayCopy` is the strongest heap baseline.
- `copy_segmentCopy` shows FFM bulk-copy cost.
- `copy_directBufferPut` shows direct-buffer bulk copy cost.

### Downcalls

- `javaAdd` and `javaStringLength` are lower-bound Java baselines.
- `scalarDowncall_noop` approximates minimal native boundary cost.
- `scalarDowncall_addI32` adds a tiny amount of real native work.
- `strlen_perCallMarshalling` vs `strlen_reusedSegment` isolates marshalling overhead.

### Struct Access

- `struct_scanVarHandles` measures the cleaner, layout-driven access style.
- `struct_manualOffsets` measures direct offset reads.
- In simple hot loops, the difference is often small enough that readability should matter more.

### Upcalls

- `arraysSort` is the JDK baseline.
- `qsortUpcall` adds a callback-heavy native path.
- This is a "callbacks are expensive" teaching benchmark, not a recommendation for production sorting.

## Benchmark Matrix

| Family | Variants | Primary metric |
|--------|----------|----------------|
| Memory read | `int[]`, heap segment, native segment, direct ByteBuffer | ns/element |
| Memory copy | `System.arraycopy`, `MemorySegment.copy`, direct buffer | ns/element |
| Allocation | confined arena, reused arena, slicing allocator, direct buffer | ns/allocation |
| Downcall | Java method, FFM scalar, FFM strlen | ns/op |
| Struct access | VarHandles vs manual offsets | ns/point |
| Upcall | Java sort vs native `qsort` with Java comparator | us/op |

## Interpreting Results

Use these benchmarks for:

- relative comparison inside one family
- understanding which cost dominates a path
- checking whether marshalling or setup is dwarfing the foreign call

Do **not** use them for:

- sweeping statements about all FFM workloads
- comparisons across unrelated benchmark families
- production claims without validating your own workload

## Signs A Result Is Trustworthy

- the result ordering matches the benchmark hypothesis
- repeated reruns are stable
- error bars are small enough for the claim you want to make
- the baseline looks plausible for the amount of work performed

If a benchmark claims that sorting 1000 integers takes far less than a microsecond, that is a sign to inspect benchmark structure before trusting the number.

## Running Benchmarks

```bash
./gradlew :ffm-benchmarks:jmh
```

Results: `ffm-benchmarks/build/reports/jmh/`.

To run a specific benchmark family:

```bash
./gradlew :ffm-benchmarks:jmh -Pjmh.includes=".*MemoryReadBenchmark.*"
```

To focus on one question, rerun one family several times instead of trusting one giant table immediately.

## Suggested Learning Path

1. Run the full suite once.
2. Choose one benchmark family.
3. Read the corresponding example class in `ffm-examples`.
4. Write down a hypothesis before looking at the numbers.
5. Change one variable at a time and rerun.

That approach teaches much more than passively collecting output.
