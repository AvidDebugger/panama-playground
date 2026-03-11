package net.szumigaj.java.panama.ffm.tutorial.upcalls;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.szumigaj.java.panama.ffm.support.FfmExceptionHelper;
import net.szumigaj.java.panama.ffm.support.NativeLib;

/**
 * Chapter 6: Upcalls – passing a Java callback to native code.
 * <p>
 * Uses ffm_qsort_int with a Java comparator. The comparator is an "upcall" from C back into Java.
 * The arena manages the upcall stub lifetime.
 */
public final class QSortUpcalls {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOOKUP;

    static {
        NativeLib.load();
        LOOKUP = SymbolLookup.loaderLookup();
    }

    /**
     * Comparator for qsort: (const void* a, const void* b) -> int
     * Negative if a < b, 0 if equal, positive if a > b.
     */
    private static final FunctionDescriptor COMPAR_DESC = FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS
    );

    private static final MethodHandle QSORT_INT = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_qsort_int"),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
    );

    private QSortUpcalls() {}

    /**
     * Sort an int array in place using native qsort with a Java comparator.
     */
    public static void qsortInt(int[] arr) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_INT, arr);
            MethodHandle comparHandle = MethodHandles.lookup().findStatic(
                    QSortUpcalls.class, "compareInts",
                    MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class)
            );
            MethodHandle comparAdapted = comparHandle.asType(MethodType.methodType(
                    int.class, MemorySegment.class, MemorySegment.class
            ));
            MemorySegment upcallStub = LINKER.upcallStub(comparAdapted, COMPAR_DESC, arena);
            QSORT_INT.invoke(segment, (long) arr.length, upcallStub);
            for (int i = 0; i < arr.length; i++) {
                arr[i] = segment.get(ValueLayout.JAVA_INT, (long) i * Integer.BYTES);
            }
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    /**
     * Comparator implementation: reads two ints from pointers and compares.
     * Pointers from upcalls arrive as zero-length segments; reinterpret to allow reading.
     */
    public static int compareInts(MemorySegment a, MemorySegment b) {
        int va = a.reinterpret(ValueLayout.JAVA_INT.byteSize()).get(ValueLayout.JAVA_INT, 0);
        int vb = b.reinterpret(ValueLayout.JAVA_INT.byteSize()).get(ValueLayout.JAVA_INT, 0);
        return Integer.compare(va, vb);
    }

    /**
     * Sort ascending (convenience alias).
     */
    public static void qsortIntAscending(int[] arr) {
        qsortInt(arr);
    }
}
