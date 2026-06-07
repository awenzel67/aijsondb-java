# aijsondb-java

Java bindings for aijsondb - a thin wrapper on the [aijsondb C++ library](https://github.com/awenzel67/aijsondb).

## Overview

**aijsondb** is an analytical database that allows you to query your data using JavaScript instead of SQL. The core idea is inspired by DuckDB but optimized for AI/LLM use cases:

- **Single JSON document** as the data structure
- **JavaScript** as the query language
- **JSON Schema** to describe and validate the JSON document

While these choices may not be ideal for humans writing queries, they are well-suited for **LLMs** generating queries based on natural language input. In comparative tests, aijsondb-based agents achieved **95% correct answers** compared to 81% for SQL-based agents.

Supported data sources are:

* JSON data
* XLSX files (Excel)

## Project Structure

Two Java modules are available:

- `aijsondbj/` - Main library module (Java wrapper for aijsondb C library)
- `aijsondbjagent/` - Agent module containing helpers to create Agent with langchain4j

See the corresponding README files for details.

Samples and sample data can be found here:
- `aijsondbjweb/` - Spring Boot WebApplication for uploading Excel files and analyzing them with natural language questions. 
- `scripts/` - Example scripts including `aijsondbjcli.java`
- `data/` - Sample data files

## Installation

### Maven Dependency

Add the following dependency to your `pom.xml` to use aijsondbj:

```xml
<dependency>
    <groupId>io.github.awenzel67</groupId>
    <artifactId>aijsondb-java</artifactId>
    <version>0.0.3</version>
</dependency>
```

Or use JBang with the following directive:

```java
//DEPS io.github.awenzel67:aijsondb-java:0.0.3
```

### Prerequisites

- Java 22+ (required for Foreign Function & Memory API)
- The native aijsondbc library must be available (loaded from JAR resources, `bin/` directory, or system library path)

## API Reference

The `AIJsonDBC` class provides the following methods:

### `loadData(String filename, String schema)`

Loads data from a JSON file with the given schema and validates it.

**Parameters:**
- `filename`: Path to the JSON file containing the data
- `schema`: Path to the JSON Schema file for validation

**Returns:** `int` - 0 on success, non-zero on error

**Example:**
```java
AIJsonDBC.loadData("data/employees.json", "data/employeeSchema.json");
```

### `importOrLoadData(String filename, String filenameJson, String schema)`

Imports data from a file (e.g., XLSX/Excel) into a JSON object and JSON Schema. If the JSON file and schema don't exist, they are created from the import file.

**Parameters:**
- `filename`: Path to the import file (e.g., Excel XLSX)
- `filenameJson`: Path to the JSON file (created if doesn't exist)
- `schema`: Schema definition (created if doesn't exist)

**Returns:** `int` - 0 on success, non-zero on error

**Example:**
```java
AIJsonDBC.importOrLoadData("data/employees.xlsx", 
                          "data/employees.json", 
                          "data/employeeSchema.json");
```

### `query(String query)`

Executes a JavaScript query on the loaded JSON data.

**Parameters:**
- `query`: JavaScript expression to query the data. By convention, the result should be saved in a variable named `result`.

**Returns:** `JsonElement` - The query result as a Gson JsonElement, or throws RuntimeException on error

**Example:**
```java
JsonElement result = AIJsonDBC.query("var result = data.employees.length;");
System.out.println("Number of employees: " + result);

// Get all employee names
JsonElement names = AIJsonDBC.query("var result = data.employees.map(e => e.name);");
```

### `freeData()`

Frees the loaded data and releases associated resources.

**Returns:** `int` - 0 on success, non-zero on error

**Example:**
```java
AIJsonDBC.freeData();
```

### `getLastError()`

Retrieves the last error message from the library.

**Returns:** `String` - The error message, or null if no error

**Example:**
```java
try {
    AIJsonDBC.loadData("invalid.json", "schema.json");
} catch (RuntimeException e) {
    String error = AIJsonDBC.getLastError();
    System.err.println("Error: " + error);
}
```

## Usage Example

The `scripts/aijsondbjcli.java` file demonstrates how to use the library:

```java
// file: scripts/aijsondbjcli.java
///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//DEPS io.github.awenzel67:aijsondb-java:0.0.3

import io.github.awenzel67.AIJsonDBC;
import com.google.gson.JsonElement;

void main(String... args) {
    try {
        // Load JSON data with schema
        AIJsonDBC.loadData("../data/500_KB_V2.json", "../data/employeeSchemaDescription_V2.json");
        
        // Query the data - get employee count
        JsonElement result = AIJsonDBC.query("var result = data.employees.length;");
        System.out.println("Query result: " + result);
        
        // Query the data - get all employee names
        result = AIJsonDBC.query("var result = data.employees.map(x => x.name);");
        System.out.println("Query result: " + result);
        
    } catch (RuntimeException e) {
        System.err.println("Error loading data: " + e.getMessage());
    }
}
```

To run the example:

```bash
jbang scripts/aijsondbjcli.java
```

## Building

To build the project:

```bash
cd aijsondbj
mvn clean package
```

This will create a JAR file with the Java wrapper and the native library embedded as a resource.

## Native Library

The wrapper automatically attempts to load the native library in the following order:

1. From JAR resources (embedded in the JAR)
2. From the `bin/` directory relative to the current working directory
3. From the system library path

The native library is named:
- Windows: `aijsondbc.dll`
- macOS: `libaijsondbc.dylib`
- Linux: `libaijsondbc.so`

## Technology Stack

- **Java 22+** - Uses the Foreign Function & Memory API (JEP 454)
- **Gson** - For JSON parsing in Java
- **aijsondb C library** - Core database engine using quickjs-ng and jsoncons

## Related Projects

- [aijsondb (C library)](https://github.com/awenzel67/aijsondb) - Core database engine
- [aijsondb-py](https://github.com/awenzel67/aijsondb-py) - Python wrapper

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests on GitHub.

## Support

For questions or issues, please open an issue on the [GitHub repository](https://github.com/awenzel67/aijsondb-java).

## License

Apache License 2.0