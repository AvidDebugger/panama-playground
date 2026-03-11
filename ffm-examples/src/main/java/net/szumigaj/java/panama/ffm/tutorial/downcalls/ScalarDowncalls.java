package net.szumigaj.java.panama.ffm.tutorial.downcalls;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import net.szumigaj.java.panama.ffm.support.FfmExceptionHelper;
import net.szumigaj.java.panama.ffm.support.NativeLib;

/**
 * Chapter 3: Scalar downcalls.
 * <p>
 * Uses {@code Linker.nativeLinker()} and {@code downcallHandle} with
 * {@code FunctionDescriptor} for simple scalar calls.
 */
public final class ScalarDowncalls {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOOKUP;

    static {
        NativeLib.load();
        LOOKUP = SymbolLookup.loaderLookup();
    }

    private static final MethodHandle ADD_I32 = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_add_i32"),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
    );

    private static final MethodHandle ADD_I64 = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_add_i64"),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
    );

    private static final MethodHandle ADD_F64 = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_add_f64"),
            FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE)
    );

    private static final MethodHandle NOOP = LINKER.downcallHandle(
            LOOKUP.findOrThrow("ffm_noop"),
            FunctionDescriptor.ofVoid()
    );

    private ScalarDowncalls() {}

    public static int addI32(int a, int b) {
        try {
            return (int) ADD_I32.invoke(a, b);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    public static long addI64(long a, long b) {
        try {
            return (long) ADD_I64.invoke(a, b);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    public static double addF64(double a, double b) {
        try {
            return (double) ADD_F64.invoke(a, b);
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }

    public static void noop() {
        try {
            NOOP.invoke();
        } catch (Throwable e) {
            throw FfmExceptionHelper.rethrow(e);
        }
    }
}
