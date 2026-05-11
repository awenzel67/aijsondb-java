package com.aijsondb.agent;
import com.aijsondb.AIJsonDBC;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import java.nio.file.Files;
import java.nio.file.Path;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.agent.tool.*;





/**
 * Main Agent class for AijsonDB Agent Library.
 */
public class Agent {
    
    public enum E_PROMPT_TEMPLATE { GENERIC,MISTRAL};
    public interface AIJsonAgent {
        String chat(String userMessage);
    }

    public class AIJsonTool {
    @Tool("Run a JavaScript expression as query on the provided data.") // function description
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

    private Agent() {
        // Default constructor
    }

    private String systemprompt;
    public String getAgentSystemPrompt() {
        return systemprompt;
    }
    private  AIJsonTool aiJsonTool = new AIJsonTool();
    public AIJsonTool getAiJsonTool() {
        return aiJsonTool;
    }
    public static Agent create(String fileNameData, String fileNameSchema,E_PROMPT_TEMPLATE templateType) throws java.io.IOException {
        try {
            AIJsonDBC.loadData(fileNameData,fileNameSchema);
            Path filePath = Path.of(fileNameSchema);
            //System.out.println(filePath);
            String jSchema = Files.readString(filePath);
            String agentTemplate=getPromptTemplate(templateType);
            String prompt = String.format(agentTemplate, jSchema);
            Agent agent =  new Agent();
            agent.systemprompt = prompt;
            return agent;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error loading data: " + e.getMessage());
        }
    }
    
    public static Agent createImporter(String fileNameImport,String fileNameData, String fileNameSchema,E_PROMPT_TEMPLATE templateType) throws java.io.IOException {
        try {
            AIJsonDBC.importOrLoadData(fileNameImport, fileNameData, fileNameSchema);
            Path filePath = Path.of(fileNameSchema);
            //System.out.println(filePath);
            String jSchema = Files.readString(filePath);
            String agentTemplate=getPromptTemplate(templateType);
            String prompt = String.format(agentTemplate, jSchema);
            Agent agent =  new Agent();
            agent.systemprompt = prompt;
            return agent;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error loading data: " + e.getMessage());
        }
    }

    private static String getPromptTemplate(E_PROMPT_TEMPLATE templateType) {
        switch (templateType) {
            case MISTRAL:
                return agentTemplateGeneral; // For now, we use the same template. Can be customized for Mistral if needed.
            case GENERIC:
            default:
                return agentTemplateGeneral;
        }
    }   
}
