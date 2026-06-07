package io.github.awenzel67;
import io.github.awenzel67.AIJsonDBC;
//import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import java.nio.file.Files;
import java.nio.file.Path;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.agent.tool.*;





/**
 * Main Agent class for AijsonDB Agent Library.
 * 
 * This class provides helper methods to create and interact with AI agents 
 * that can "talk to your data" using AijsonDB and langchain4j.
 * It simplifies the process of loading data, creating agents with appropriate 
 * system prompts, and querying data using natural language.
 */
public class Agent {
    
    /**
   * Enumeration of available prompt templates for the agent.
   * Different templates can be used to optimize the agent's behavior 
   * for specific AI models or use cases.
   */
   public enum E_PROMPT_TEMPLATE { 
       /** Generic prompt template suitable for most AI models */
       GENERIC,
       /** Prompt template optimized for Mistral AI models */
       MISTRAL
   }
    /**
   * Interface for a basic AIJson agent that can chat with users.
   * This interface defines the contract for agents that process user messages
   * and return responses based on the loaded data.
   */
   public interface AIJsonAgent {
        /**
         * Processes a user message and returns a response.
         * 
         * @param userMessage The message from the user in natural language
         * @return The agent's response to the user message
         */
        String chat(String userMessage);
    }

    /**
   * Interface for an AIJson agent that supports dynamic system messages and memory.
   * This interface extends the basic agent functionality with memory management
   * capabilities, allowing for conversation history and context preservation.
   */
   public interface AIJsonAgentDynamicSystemMessage {
        /**
         * Processes a user message with memory context and returns a response.
         * 
         * @param memoryId The memory identifier for tracking conversation history
         * @param userMessage The message from the user in natural language
         * @return The agent's response to the user message with memory context
         */
        String chat(@MemoryId String memoryId, @UserMessage String userMessage);
    }

    /**
     * Tool class that provides the query functionality for the AI agent.
     * This class contains the tool methods that the agent can use to 
     * query and manipulate JSON data using JavaScript expressions.
     */
    public class AIJsonTool {
        /**
         * Executes a JavaScript query on the loaded JSON data.
         * 
         * This tool allows the AI agent to query the data using JavaScript expressions.
         * The data is available in a variable named 'data', and the result should be 
         * stored in a variable named 'result'.
         * 
         * @param queryString The JavaScript query expression to execute
         * @return The result of the query as a JSON string, or an error message if the query fails
         */
        @Tool("Run a JavaScript expression as query on the provided data.")
        String query_json_javascript(@P("JavaScript query") String queryString) {
            try {
                //System.out.println("Running JavaScript query: " + queryString);
                com.google.gson.JsonElement result = AIJsonDBC.query(queryString);
                return result.toString();
            } catch (Exception e) {
                return "Error running the JavaScript expression: " + e.getMessage();
            }            
        }
    }

   private static String agentTemplateGeneral = """
I have a JSON data object.

The JSON schema for the data object you will work with is:
%s

Fetch data from the JSON object using javascript to query the data.

Use the tool: 
query_json_javascript: returns the entities using a given javascript programm as query.

The data are in a javascript variable named: data.
The final result is always saved in variable result: eg. var result.

Always use the available tools to fetch data.
Only use the data from tools defined above to answer the question.
""";

    public Agent() {
        // Default constructor
    }

    private String systemprompt;
    
    /**
     * Gets the current system prompt for the agent.
     * The system prompt is automatically generated based on the loaded schema 
     * and the selected prompt template.
     * 
     * @return The current system prompt as a string
     */
    public String getAgentSystemPrompt() {
        return systemprompt;
    }
    private  AIJsonTool aiJsonTool = new AIJsonTool();
    
    /**
     * Gets the AIJsonTool instance used by this agent.
     * The tool provides query capabilities for the loaded JSON data.
     * 
     * @return The AIJsonTool instance
     */
    public AIJsonTool getAiJsonTool() {
        return aiJsonTool;
    }
    /**
     * Loads JSON data and schema files into the agent.
     * This method reads the data and schema files, loads them into the AijsonDB database,
     * and generates a system prompt based on the schema and the selected template type.
     * 
     * @param fileNameData The path to the JSON data file
     * @param fileNameSchema The path to the JSON schema file
     * @param templateType The prompt template type to use (GENERIC or MISTRAL)
     * @throws java.io.IOException If there is an error reading the files
     * @throws RuntimeException If there is an error loading the data into AijsonDB
     */
    public void loadData(String fileNameData, String fileNameSchema,E_PROMPT_TEMPLATE templateType) throws java.io.IOException {
        try {
            AIJsonDBC.loadData(fileNameData,fileNameSchema);
            Path filePath = Path.of(fileNameSchema);
            //System.out.println(filePath);
            String jSchema = Files.readString(filePath);
            String agentTemplate=getPromptTemplate(templateType);
            String prompt = String.format(agentTemplate, jSchema);
            this.systemprompt = prompt;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error loading data: " + e.getMessage());
        }
    }
    
    /**
     * Imports or loads JSON data and schema files into the agent.
     * Similar to loadData, but uses the importOrLoadData method which can handle 
     * both import and load operations. This method reads the files, loads them into 
     * the AijsonDB database, and generates a system prompt based on the schema.
     * 
     * @param fileNameImport The path to the import file (if applicable)
     * @param fileNameData The path to the JSON data file
     * @param fileNameSchema The path to the JSON schema file
     * @param templateType The prompt template type to use (GENERIC or MISTRAL)
     * @throws java.io.IOException If there is an error reading the files
     * @throws RuntimeException If there is an error importing/loading the data into AijsonDB
     */
    public void importData(String fileNameImport,String fileNameData, String fileNameSchema,E_PROMPT_TEMPLATE templateType) throws java.io.IOException {
        try {
            AIJsonDBC.importOrLoadData(fileNameImport, fileNameData, fileNameSchema);
            Path filePath = Path.of(fileNameSchema);
            //System.out.println(filePath);
            String jSchema = Files.readString(filePath);
            String agentTemplate=getPromptTemplate(templateType);
            String prompt = String.format(agentTemplate, jSchema);
            this.systemprompt = prompt;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Gets the prompt template based on the template type.
     * This method returns the appropriate template string that will be used 
     * to generate the system prompt for the agent.
     * 
     * @param templateType The type of prompt template to retrieve
     * @return The prompt template string
     */
    private String getPromptTemplate(E_PROMPT_TEMPLATE templateType) {
        switch (templateType) {
            case MISTRAL:
                return agentTemplateGeneral; // For now, we use the same template. Can be customized for Mistral if needed.
            case GENERIC:
            default:
                return agentTemplateGeneral;
        }
    }   
}
