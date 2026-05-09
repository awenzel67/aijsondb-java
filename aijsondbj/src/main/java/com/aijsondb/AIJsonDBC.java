package com.aijsondb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Java wrapper for the aijsondbc native library using Java 22 FFM API.
 * Uses the Foreign Function & Memory API (JEP 454, standardized in Java 22).
 */
public class AIJsonDBC {

    private static final String LIB_NAME = "aijsondbc";
    private static final String WIN_LIB = "aijsondbc.dll";
    private static final String MAC_LIB = "libaijsondbc.dylib";
    private static final String LINUX_LIB = "libaijsondbc.so";

    static {
            // Fallback: try loading from JAR resources
            try {
                String libFileName = getLibraryFileName();
                System.out.println("Attempting to load native library from JAR: " + libFileName);
                loadLibraryFromJar(libFileName);
            } catch (UnsatisfiedLinkError ex) {
                // Final fallback: try loading from bin/ subdirectory
            try {
                    String libPath = System.getProperty("aijsondb.lib.path", "bin/");
                    System.load(libPath + getLibraryFileName());
            } catch (UnsatisfiedLinkError ex2) {
                    throw new ExceptionInInitializerError("Failed to load aijsondbc native library: " + ex2.getMessage());
            }
        }
    }

    private static String getLibraryFileName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return WIN_LIB;
        } else if (os.contains("mac")) {
            return MAC_LIB;
        } else {
            return LINUX_LIB;
        }
    }

    private static void loadLibraryFromJar(String libFileName) throws UnsatisfiedLinkError {
        String libResource = "/" + libFileName;
        try (InputStream in = AIJsonDBC.class.getResourceAsStream(libResource)) {
            if (in == null) {
                throw new UnsatisfiedLinkError("Native library " + libFileName + " not found in JAR");
            }
            Path tempDir = Files.createTempDirectory("aijsondb-");
            Path libPath = tempDir.resolve(libFileName);
            Files.copy(in, libPath, StandardCopyOption.REPLACE_EXISTING);
            libPath.toFile().setReadable(true, false);
            libPath.toFile().setExecutable(true, false);
            libPath.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();
            System.load(libPath.toAbsolutePath().toString());
        } catch (IOException | NullPointerException e) {
            throw new UnsatisfiedLinkError("Failed to extract native library from JAR: " + e.getMessage());
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
            int result = (int) ffi_aijsondb_load_data.invoke(filenameSeg, schemaSeg);
            if (result != 0) {
                throw new RuntimeException("Failed to load data");
            }
            return result;
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
                String errorMessage = buffer.getString(0);
                throw new RuntimeException("Failed to execute query: " + errorMessage);
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
