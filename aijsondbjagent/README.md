# aijsondb Agent Library

**aijsondbjagent** is a Java library that contains helper methods to make it easy to create an AI agent that can **"talk to your data"** using [aijsondb](https://github.com/awenzel67/aijsondb) and [langchain4j](https://github.com/langchain4j/langchain4j).

This library simplifies the process of loading data, creating agents with appropriate system prompts, and querying data using natural language questions.

## Features

- **Easy Agent Creation**: Quickly create AI agents that can understand and query your JSON data
- **Natural Language Queries**: Ask questions in plain English and get answers from your data
- **Flexible Data Loading**: Load data from JSON or Excel files with schema support
- **Multiple Prompt Templates**: Support for different AI models (OpenAI, Mistral and Ollama for local inference)
- **Integration with langchain4j**: Built on top of the powerful langchain4j framework

## Installation

### Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.awenzel67</groupId>
    <artifactId>aijsondb-agent</artifactId>
    <version>0.0.3</version>
</dependency>
```

## Quick Start

aijsondbjagent make it easy to use aijsondb with langchain4j as AI agent framework. 

First load your JSON data file and JSON schema file:
```java
  Agent aiJsonAgent = new Agent();
  aiJsonAgent.loadData("data.json", "schema.json", Agent.E_PROMPT_TEMPLATE.GENERIC);     
```

Instead you can also use a XLSX file (Excel) directly as datasource. JSON data and schema are than created automatically:
```java
  Agent aiJsonAgent = new Agent();
  aiJsonAgent.loadData("data.json", "schema.json", E_PROMPT_TEMPLATE.GENERIC);    
  aiJsonAgent.importData("excel.xslx","excel_temp.json","excel_temp_schema.json",Agent.E_PROMPT_TEMPLATE.GENERIC);
       
```

Now you can create your langchain4j  AI agent using the tools for aijsondb and the corresponding systemprompt:
```java
 Agent.AIJsonAgent agent = AiServices.builder(Agent.AIJsonAgent.class)
                .chatModel(model)
                .tools(aiJsonAgent.getAiJsonTool())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .systemMessage(aiJsonAgent.getAgentSystemPrompt())
                .build();      
```

A full working example is aijsondbjlangchain.java in Scripts. 


## Main Classes and Methods

### Agent Class

The main class that provides all the helper functionality for using aijsondb with langchain4j AI agents.

#### Key Methods

- **`getAiJsonTool()`**: Gets the AIJsonTool instance to use in langchain4j agent.
  - Returns: `AIJsonTool` - The tool that provides query capabilities for the loaded JSON data.

- **`loadData(String fileNameData, String fileNameSchema, E_PROMPT_TEMPLATE templateType)`**: Loads JSON data and schema files.
  - Parameters:
    - `fileNameData`: Path to the JSON data file
    - `fileNameSchema`: Path to the JSON schema file
    - `templateType`: The prompt template type (GENERIC or MISTRAL)
  - Throws: `IOException`, `RuntimeException`

- **`importData(String fileNameImport, String fileNameData, String fileNameSchema, E_PROMPT_TEMPLATE templateType)`**: Import data e.g. excel files or loads JSON data and schema files.
  - Parameters:
    - `fileNameImport`: Path to the import file (e.g. excel file)
    - `fileNameData`: Path to the generated JSON data file
    - `fileNameSchema`: Path to the generated JSON schema file
    - `templateType`: The prompt template type (GENERIC or MISTRAL)
  - Throws: `IOException`, `RuntimeException`

- **`getAgentSystemPrompt()`**: Gets the current system prompt for the agent.
  - Returns: `String` - The generated system prompt based on the loaded schema

#### Interfaces

- **`AIJsonAgent`**: Interface for a basic langchain4j agent that can chat with users.
  - Method: `chat(String userMessage)` - Processes a user message and returns a response

- **`AIJsonAgentDynamicSystemMessage`**: Interface for an langchain4j agent that supports dynamic system messages and memory.
  - Method: `chat(@MemoryId String memoryId, @UserMessage String userMessage)` - Processes a user message with memory context

#### Enums

- **`E_PROMPT_TEMPLATE`**: Enumeration of available prompt templates.
  - `GENERIC`: Generic prompt template suitable for most AI models
  - `MISTRAL`: Prompt template optimized for Mistral AI models

#### Inner Classes

- **`AIJsonTool`**: Tool class that provides query functionality for the AI agent.
  - Method: `query_json_javascript(String queryString)` - Executes a JavaScript query on the loaded JSON data

## AIJsonTool

The `AIJsonTool` class provides the core query capability for the agent. It allows the AI to query the data using JavaScript expressions.

### Usage

The tool is automatically available to your agent. When you call `getAiJsonTool()`, you get access to:

- **`query_json_javascript(String queryString)`**: Executes a JavaScript query on the loaded JSON data.
  - The data is available in a variable named `data`
  - The result should be stored in a variable named `result`
  - Returns the result as a JSON string, or an error message if the query fails

## Dependencies

This library depends on:
- [aijsondb-java](https://github.com/awenzel67/aijsondb-java) (io.github.awenzel67:aijsondb-java:0.0.3)
- [langchain4j](https://github.com/langchain4j/langchain4j) (dev.langchain4j:langchain4j:1.14.1)

## Building

To build this project from source:

```bash
mvn compile
mvn package
```