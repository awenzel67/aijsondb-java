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

## Related Projects

- [aijsondb (C library)](https://github.com/awenzel67/aijsondb) - Core database engine
- [aijsondb-py](https://github.com/awenzel67/aijsondb-py) - Python wrapper

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests on GitHub.

## Support

For questions or issues, please open an issue on the [GitHub repository](https://github.com/awenzel67/aijsondb-java).

## License

Apache License 2.0