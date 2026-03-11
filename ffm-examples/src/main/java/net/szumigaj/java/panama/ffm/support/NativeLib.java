package net.szumigaj.java.panama.ffm.support;

import java.nio.file.Path;

/**
 * Loads the ffmplayground native library for FFM examples and benchmarks.
 * <p>
 * Uses {@link System#load(String)} or {@link System#loadLibrary(String)} so that
 * {@link java.lang.foreign.SymbolLookup#loaderLookup()} can resolve symbols.
 */
public final class NativeLib {

    private static final String LIB_NAME = "ffmplayground";
    private static volatile boolean loaded;

    private NativeLib() {}

    /**
     * Load the ffmplayground native library.
     * <ul>
     *   <li>If system property {@code ffmplayground.lib.path} is set: treat it as a
     *       directory path and load {@code libffmplayground.so} (or platform equivalent) from it.</li>
     *   <li>Otherwise: use {@code java.library.path} via {@link System#loadLibrary(String)}.</li>
     * </ul>
     */
    public static void load() {
        if (loaded) return;
        synchronized (NativeLib.class) {
            if (loaded) return;
            String explicitPath = System.getProperty("ffmplayground.lib.path");
            if (explicitPath != null && !explicitPath.isBlank()) {
                System.load(Path.of(explicitPath).resolve(System.mapLibraryName(LIB_NAME)).toAbsolutePath().toString());
            } else {
                System.loadLibrary(LIB_NAME);
            }
            loaded = true;
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
