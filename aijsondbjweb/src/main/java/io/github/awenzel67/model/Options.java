package io.github.awenzel67.model;

public class Options {

    public enum ModelProvider {
        MISTRAL, OPENAI, OLLAMA
    }

    private ModelProvider modelProvider;
    private String apiKey;
    private String modelName;

    public Options() {
        this.modelProvider = ModelProvider.MISTRAL;
        this.apiKey = System.getenv("MISTRAL_API_KEY");
        this.modelName = "";
    }

    public Options(ModelProvider modelProvider, String apiKey, String modelName) {
        this.modelProvider = modelProvider;
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    public ModelProvider getModelProvider() {
        return modelProvider;
    }

    public void setModelProvider(ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
