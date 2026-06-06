package io.github.awenzel67.service;

import io.github.awenzel67.Agent;
import io.github.awenzel67.model.Options;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
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
                System.out.println("Creating agent with dynamic system message...");
                Options.ModelProvider modelType=options.getModelProvider();
                System.out.println("Using model: " + modelType);
                ChatModel model = null;
                options.setModelName("");
                if (modelType==Options.ModelProvider.MISTRAL) {
                    String apiKey = System.getenv("MISTRAL_API_KEY");
                    if (apiKey == null || apiKey.isEmpty()) {
                        System.err.println("Error: Please set MISTRAL_API_KEY environment variable.");
                        return;
                    }
                    model= MistralAiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName("mistral-large-latest") // or "mistral-small", "mistral-large"
                        .temperature(0.0) // deterministic output
                        .maxTokens(2000)
                        .build();
                    options.setModelName("mistral-large-latest");
                
                } 
                else if (modelType==Options.ModelProvider.OPENAI) {
                    String apiKey = System.getenv("OPENAI_API_KEY");
                    if (apiKey == null || apiKey.isEmpty()) {
                        System.err.println("Error: Please set OPENAI_API_KEY environment variable.");
                        return;
                    }
                    // Example for GPT4All local model
                model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gpt-5.4-mini")
                    .build();
                    options.setModelName("gpt-5.4-mini");
                }
                else if (modelType==Options.ModelProvider.OLLAMA) {
                    // Example for GPT4All local model
                    String ollamaUrl = System.getenv("OLLAMA_URL");
                    if (ollamaUrl == null || ollamaUrl.isEmpty()) {
                        ollamaUrl = "http://localhost::11434"; // default URL
                    }
                    model = OllamaChatModel.builder()
                    .baseUrl(ollamaUrl)
                    .temperature(0.0)
                    //.logRequests(true)
                    //.logResponses(true)
                    .numCtx(2000)
                    .think(false) // enables the THINK tool which allows the model to decide when to call tools and when to think
                    .modelName("qwen3.6:35b-a3b")
                    .build();
                    options.setModelName("qwen3.6:35b-a3b");
                }
                else {
                    System.err.println("Unsupported model type: " + modelType);
                    return;
                }

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
