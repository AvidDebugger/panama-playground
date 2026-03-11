package net.szumigaj.java.panama.ffm.tutorial.strings;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import net.szumigaj.java.panama.ffm.support.FfmExceptionHelper;
import net.szumigaj.java.panama.ffm.support.NativeLib;

/**
 * Chapter 2: Strings and downcalls.
 * <p>
 * Uses {@code Linker.nativeLinker()} and {@code Arena.allocateFrom(String)} for
 * UTF-8 C strings. Symbol lookup via {@code SymbolLookup.loaderLookup()} after
 * {@link net.szumigaj.java.panama.ffm.support.NativeLib#load()}.
 */
public final class StringDowncalls {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOOKUP;

    static {
        NativeLib.load();
        LOOKUP = SymbolLookup.loaderLookup();
    }

    private static final MethodHandle STRLEN = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_strlen"),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
    );

    private StringDowncalls() {}

    /**
     * Call ffm_strlen on a UTF-8 encoded string.
     */
    public static long strlen(String s) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment utf8 = arena.allocateFrom(s);
            return (long) STRLEN.invoke(utf8);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    /**
     * Call ffm_strlen with a pre-allocated segment (for benchmarking).
     * <p>
     * <b>Contract:</b> The segment must contain a NUL-terminated UTF-8 string.
     * Passing a segment without a NUL terminator causes unbounded reads in native code.
     *
     * @param utf8 segment containing a NUL-terminated C string (e.g. from {@link #allocateUtf8})
     * @return byte length of the string excluding the NUL terminator
     */
    public static long strlenSegment(MemorySegment utf8) {
        try {
            return (long) STRLEN.invoke(utf8);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    /**
     * Allocate a UTF-8 string in the given arena.
     */
    public static MemorySegment allocateUtf8(Arena arena, String s) {
        return arena.allocateFrom(s);
    }
}
