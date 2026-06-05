///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//DEPS io.github.awenzel67:aijsondb-java:0.0.3
//DEPS dev.langchain4j:langchain4j-mistral-ai:1.14.1
//DEPS dev.langchain4j:langchain4j-open-ai:1.14.1
//DEPS dev.langchain4j:langchain4j-ollama:1.14.1
//DEPS dev.langchain4j:langchain4j:1.14.1
//DEPS io.github.awenzel67:aijsondb-agent:0.0.3

import io.github.awenzel67.AIJsonDBC;
import io.github.awenzel67.Agent;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
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
        String FILE_URL="https://www.berlin.de/sen/bildung/service/daten/kitaliste-nov-2025.xlsx";
        String FILE_NAME="kitaliste-nov-2025.xlsx";
        //InputStream in = new URI(FILE_URL).toURL().openStream();
        //Files.copy(in, Paths.get(FILE_NAME), StandardCopyOption.REPLACE_EXISTING);

        Agent aiJsonAgent = new Agent();
        aiJsonAgent.importData(FILE_NAME,"excel_temp_kita.json","excel_temp_kita_schema.json",Agent.E_PROMPT_TEMPLATE.GENERIC);
        System.out.println("Data loaded successfully.");
        String apiKey = null;
        String modelType="Ollama";
        ChatModel model = null;
       System.out.println("Using model: " + modelType);

        if (modelType.equals("mistral")) {
            apiKey = System.getenv("MISTRAL_API_KEY");
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
        
        } 
        else if (modelType.equals("OpenAI")) {
            // Example for GPT4All local model
           model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-5.4-mini")
            .build();
        }
        else if (modelType.equals("Ollama")) {
            // Example for GPT4All local model
            String ollamaUrl = "http://192.168.0.124:11434";
            model = OllamaChatModel.builder()
            .baseUrl(ollamaUrl)
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .numCtx(2000)
            .think(false) // enables the THINK tool which allows the model to decide when to call tools and when to think
            .modelName("qwen3.6:35b-a3b")
            .build();
        }
        else {
            System.err.println("Unsupported model type: " + modelType);
            return;
        }

        Agent.AIJsonAgent agent = AiServices.builder(Agent.AIJsonAgent.class)
                .chatModel(model)
                .tools(aiJsonAgent.getAiJsonTool())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .systemMessage(aiJsonAgent.getAgentSystemPrompt())
                .build();
        
    
       {
        String userMessage = "How many daycares are listed in the table of daycares?"; //"Wieviel Kitas sind in der Tabelle mit Kitas aufgeführt?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
       {
        String userMessage = "Which daycare center from the table offers the most places?"; //"Welche Kita aus der Tabelle stellt die meisten Plätze zur Verfügung?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
       {
        String userMessage = "How many daycare centers are there in Pankow?"; //"Wieviele Kitas gibt es in Pankow?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
       {
        String userMessage = "How many daycare places are there in Spandau?";//"Wieviele Kitaplätze gibt es in Spandau?";
         System.out.println("User message: " + userMessage);
        String answer = agent.chat(userMessage);
        System.out.println("Answer: " + answer);
       }
      {
        String userMessage = "Which district in the table has the most daycare places?";//"Welcher Stadtbezirk aus der Tabelle hat die meisten Kitaplätze?";
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
