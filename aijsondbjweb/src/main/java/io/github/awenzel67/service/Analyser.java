package io.github.awenzel67.service;

import io.github.awenzel67.Agent;
import io.github.awenzel67.model.Options;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.service.AiServices;

import java.util.Random;
import java.util.function.Function;

public class Analyser {
    private Agent aiJsonAgent = new Agent();    
    Agent.AIJsonAgentDynamicSystemMessage dynamicSystemMessageAgent = null;

    private Random random = new Random();
    public Analyser() {   
    }

    public void importData(String fileNameImport) throws Exception {
        // Dummy implementation - will be implemented later
        System.out.println("Importing data from: " + fileNameImport);
         int randomInt = random.nextInt(1000);
         String baseDir="uploads/";
         String dataFile=baseDir+"excel_temp_kita_"+randomInt+".json";
        String schemaFile=baseDir+"excel_temp_kita_schema_"+randomInt+".json";
        aiJsonAgent.importData(fileNameImport, dataFile, schemaFile, Agent.E_PROMPT_TEMPLATE.GENERIC);
    }

    public String analyse(String question, Options options) throws Exception {
            createAgent(options);
             String sanswer = dynamicSystemMessageAgent.chat("1", question); 
             return sanswer;
    }
    private void createAgent(Options options) {
        if (this.dynamicSystemMessageAgent==null) {
         ChatModel model = MistralAiChatModel.builder()
                .apiKey(options.getApiKey())
                .modelName(options.getModelName()) // or "mistral-small", "mistral-large"
                .temperature(0.0) // deterministic output
                .maxTokens(2000)
                .build();

            Function<Object, String> systemMessageProvider = (memoryId) -> {
                // You can customize the system message based on the memoryId or other context if needed

                String systemMessage = aiJsonAgent.getAgentSystemPrompt();
                //System.out.println(systemMessage);
                return systemMessage;
            };


        this.dynamicSystemMessageAgent = AiServices.builder(Agent.AIJsonAgentDynamicSystemMessage.class)
                .chatModel(model)
                .tools(aiJsonAgent.getAiJsonTool())
                //.chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15)) // so the agent remembers what he proposed already
                //.systemMessage(aiJsonAgent.getAgentSystemPrompt())
                .systemMessageProvider(systemMessageProvider)
                .build();
            // Dummy implementation - will be implemented later
            //System.out.println("Creating agent with dynamic system message: " + dynamicSystemMessage);
        }
    }
}
