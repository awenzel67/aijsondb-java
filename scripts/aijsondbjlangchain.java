///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//DEPS io.github.awenzel67:aijsondb-java:0.0.2-SNAPSHOT
//DEPS dev.langchain4j:langchain4j-mistral-ai:1.14.1
//DEPS dev.langchain4j:langchain4j:1.14.1
//DEPS io.github.awenzel67:aijsondb-agent:0.0.2-SNAPSHOT

import io.github.awenzel67.AIJsonDBC;
import io.github.awenzel67.Agent;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import java.nio.file.Files;
import java.nio.file.Path;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.agent.tool.*;


void main(String... args) {
    try {
        //Agent aiJsonAgent = Agent.create("../data/500 KB_V2.json","../data/employeeSchemaDescription_V2.json",Agent.E_PROMPT_TEMPLATE.GENERIC);
       
        Agent aiJsonAgent = new Agent();
        aiJsonAgent.importData("C:/NHKI/data/talktodataexcel/kitaliste-nov-2025.xlsx","../data/excel_temp_kita.json","../data/excel_temp_kita_schema.json",Agent.E_PROMPT_TEMPLATE.GENERIC);
        System.out.println("Data loaded successfully.");
        String apiKey = System.getenv("MISTRAL_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: Please set MISTRAL_API_KEY environment variable.");
            return;
        }

        ChatModel model = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("mistral-large-latest") // or "mistral-small", "mistral-large"
                .temperature(0.0) // deterministic output
                .maxTokens(2000)
                .build();

        Agent.AIJsonAgent agent = AiServices.builder(Agent.AIJsonAgent.class)
                .chatModel(model)
                .tools(aiJsonAgent.getAiJsonTool())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .systemMessage(aiJsonAgent.getAgentSystemPrompt())
                .build();
        
        // STEP 3: User gets the final response from the agent
        /* 
        String userMessage = "How many employees are in the company?";
        String answer = agent.chat(userMessage);
        System.out.println(answer);
        userMessage = "Which employees have the greatest experience?";
        answer = agent.chat(userMessage);
        System.out.println(answer);
        */
       {
        String userMessage = "Wieviel Kitas sind in der Tabelle mit Kitas aufgeführt?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
       {
        String userMessage = "Welche Kita aus der Tabelle stellt die meisten Plätze zur Verfügung?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
       {
        String userMessage = "Wieviele Kitas gibt es in Pankow?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
       {
        String userMessage = "Wieviele Kitaplätze gibt es in Spandau?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
    {
        String userMessage = "Welcher Stadtbezirk aus der Tabelle hat die meisten Kitaplätze?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
    } 
    catch (Exception e) {
        System.err.println("Error loading data: " + e.getMessage());
        return;
    }
}
