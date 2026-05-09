package com.aijsondb;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

/**
 * Java wrapper for the aijsondbc native library using Java 22 FFM API.
 * Uses the Foreign Function & Memory API (JEP 454, standardized in Java 22).
 */
public class AIJsonDBC {

    static {
        try {
            // Try to load the native library from the bin directory
            System.loadLibrary("aijsondbc");
        } catch (UnsatisfiedLinkError e) {
            // Fallback: try loading from bin/ subdirectory
            try {
                String libPath = System.getProperty("aijsondb.lib.path", "bin/");
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    System.load(libPath + "aijsondbc.dll");
                } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    System.load(libPath + "libaijsondbc.dylib");
                } else {
                    System.load(libPath + "libaijsondbc.so");
                }
            } catch (UnsatisfiedLinkError ex) {
                throw new ExceptionInInitializerError("Failed to load aijsondbc native library: " + ex.getMessage());
            }
        }
    }

    private static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();
    
    private static final MethodHandle ffi_aijsondb_load_data;
    private static final MethodHandle ffi_aijsondb_query;
    private static final MethodHandle ffi_aijsondb_free_data;
    private static final MethodHandle ffi_aijsondb_last_error;

    static {
        try {
            Linker linker = Linker.nativeLinker();
            
            ffi_aijsondb_load_data = linker.downcallHandle(
                LOOKUP.find("ffi_aijsondb_load_data").orElseThrow(),
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS
                )
            );
            
            ffi_aijsondb_query = linker.downcallHandle(
                LOOKUP.find("ffi_aijsondb_query").orElseThrow(),
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT
                )
            );
            
            ffi_aijsondb_free_data = linker.downcallHandle(
                LOOKUP.find("ffi_aijsondb_free_data").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
            );
            
            ffi_aijsondb_last_error = linker.downcallHandle(
                LOOKUP.find("ffi_aijsondb_last_error").orElseThrow(),
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT
                )
            );
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Loads data from a JSON file with the given schema.
     *
     * @param filename Path to the JSON file
     * @param schema   Schema definition
     * @return 0 on success, non-zero on error
     */
    public static int loadData(String filename, String schema) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment filenameSeg = arena.allocateFrom(filename, StandardCharsets.UTF_8);
            MemorySegment schemaSeg = arena.allocateFrom(schema, StandardCharsets.UTF_8);
            return (int) ffi_aijsondb_load_data.invoke(filenameSeg, schemaSeg);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load data", e);
        }
    }

    /**
     * Executes a query and returns the result as a String.
     *
     * @param query The query string
     * @return The query result as a String, or null on error
     */
    public static String query(String query) {
        int bufferSize = 1024 * 1024; // 1MB buffer
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment querySeg = arena.allocateFrom(query, StandardCharsets.UTF_8);
            MemorySegment buffer = arena.allocate(bufferSize);
            
            int result = (int) ffi_aijsondb_query.invoke(querySeg, buffer, bufferSize);
            if (result != 0) {
                return null;
            }
            return buffer.getString(0);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    /**
     * Frees loaded data and releases resources.
     *
     * @return 0 on success, non-zero on error
     */
    public static int freeData() {
        try {
            return (int) ffi_aijsondb_free_data.invoke();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to free data", e);
        }
    }

    /**
     * Gets the last error message from the library.
     *
     * @return The error message as a String, or null if no error
     */
    public static String getLastError() {
        int bufferSize = 1024;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment buffer = arena.allocate(bufferSize);
            
            int result = (int) ffi_aijsondb_last_error.invoke(buffer, bufferSize);
            if (result != 0) {
                return null;
            }
            return buffer.getString(0);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get last error", e);
        }
    }
}
