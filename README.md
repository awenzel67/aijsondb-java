# AIJsonDB-Java

Java bindings for aijsondb - a thin wrapper on the [aijsondb C++ library](https://github.com/awenzel67/aijsondb).

## Overview

AIJsonDB-Java provides Java bindings for the aijsondb library, enabling JSON data querying from Java applications. It uses Java 22's Foreign Function & Memory API (FFM API, JEP 454) to interface with the native aijsondb C++ library, offering a seamless integration experience.

## Installation

Add AIJsonDB-Java as a Maven dependency to your project:

```xml
<dependency>
    <groupId>io.github.awenzel67</groupId>
    <artifactId>aijsondb-java</artifactId>
    <version>0.0.3</version>
</dependency>
```

## Usage Example

The following example demonstrates how to use the library:

```java
import io.github.awenzel67.AIJsonDBC;
import com.google.gson.JsonElement;

public class Example {
    public static void main(String[] args) {
        try {
            // Load data with schema
            AIJsonDBC.loadData("data/500 KB_V2.json", "data/employeeSchemaDescription_V2.json");
            
            // Execute a query
            JsonElement result = AIJsonDBC.query("var result=data.employees.length;");
            System.out.println("Query result: " + result);
            
            // Free resources when done
            AIJsonDBC.freeData();
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

## API Reference

### `AIJsonDBC.loadData(String filename, String schema)`

Loads data from a JSON file with the given schema.

**Parameters:**
- `filename` - Path to the JSON file
- `schema` - Path to the JSON Schema definition

**Returns:**
- `0` on success, non-zero on error (throws RuntimeException on failure)

**Example:**
```java
AIJsonDBC.loadData("../data/500 KB_V2.json", "../data/employeeSchemaDescription_V2.json");
```

---

### `AIJsonDBC.importOrLoadData(String filename, String filenameJson, String schema)`

Loads data from an import file, creating the JSON file and JSON schema file if they don't exist.

**Parameters:**
- `filename` - Path to the import file
- `filenameJson` - Path to the JSON file (created from import file if it doesn't exist)
- `schema` - JSON Schema definition file (created from import file if it doesn't exist)

**Returns:**
- `0` on success, non-zero on error (throws RuntimeException on failure)

**Example:**
```java
AIJsonDBC.importOrLoadData("data/import.xlsx", "data/output.json", "data/schema.json");
```

### `AIJsonDBC.query(String query)`

Executes a JavaScript query on the loaded data and returns the result as a JsonElement.

**Parameters:**
- `query` - JavaScript query string to execute

**Returns:**
- `JsonElement` - The query result parsed as a Gson JsonElement

**Throws:**
- `RuntimeException` - If the query fails

**Example:**
```java
JsonElement result = AIJsonDBC.query("var result=data.employees.length;");
System.out.println("Query result: " + result);
```

---

### `AIJsonDBC.freeData()`

Frees loaded data and releases allocated resources.

**Returns:**
- `0` on success, non-zero on error (throws RuntimeException on failure)

**Example:**
```java
AIJsonDBC.freeData();
```

---

### `AIJsonDBC.getLastError()`

Retrieves the last error message from the library.

**Returns:**
- `String` - The error message, or `null` if no error

**Example:**
```java
try {
    AIJsonDBC.loadData("invalid.json", "schema.json");
} catch (RuntimeException e) {
    String errorDetails = AIJsonDBC.getLastError();
    System.err.println("Error details: " + errorDetails);
}
```

## Requirements

- Java 22 or later (required for FFM API)
- Native aijsondb library (automatically loaded from JAR resources or from `bin/` directory)


## Project Structure

- `aijsondbj/` - Main library module (Java wrapper for aijsondb C library)
- `aijsondbjagent/` - Agent module containing helpers to create Agent with langchain4j
- `aijsondbjweb/` - Spring Boot WebApplication for uploading Excel files and analyzing them with natural language questions. Can be started with `jbang aijsondbjweb@awenzel67` and accessed at `localhost:8080`
- `scripts/` - Example scripts including `aijsondbjcli.java`
- `data/` - Sample data files

## Related Projects

- [aijsondb C++ Library](https://github.com/awenzel67/aijsondb) - The core C++ library that this Java wrapper interfaces with

## License

Apache License 2.0
