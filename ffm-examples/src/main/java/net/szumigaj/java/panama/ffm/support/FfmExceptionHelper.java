package net.szumigaj.java.panama.ffm.support;

/**
 * Helper for propagating exceptions from MethodHandle.invoke and similar.
 * Rethrows {@link Error} (e.g. OutOfMemoryError) and wraps other throwables.
 */
public final class FfmExceptionHelper {

    private FfmExceptionHelper() {}

    public static RuntimeException rethrow(Throwable e) {
        if (e instanceof Error) {
            throw (Error) e;
        }
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException(e);
    }
}
