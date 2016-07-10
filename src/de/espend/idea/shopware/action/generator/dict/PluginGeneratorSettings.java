package de.espend.idea.shopware.action.generator.dict;

import org.jetbrains.annotations.NotNull;

public class PluginGeneratorSettings {

    @NotNull
    private final String pluginName;

    @NotNull
    private final String namespace;

    @NotNull
    private final Boolean addDummyFilter;

    @NotNull
    private final Boolean addDummyFrontendController;

    @NotNull
    private final Boolean addDummyBackendController;

    @NotNull
    private final Boolean addDummyModels;

    @NotNull
    private final Boolean addDummyCommand;

    @NotNull
    private final Boolean addDummyWidget;

    @NotNull
    private final Boolean addDummyApi;

    @NotNull
    private final String interpreter;

    public PluginGeneratorSettings(
            @NotNull String pluginName,
            @NotNull String namespace,
            @NotNull Boolean addDummyFilter,
            @NotNull Boolean addDummyFrontendController,
            @NotNull Boolean addDummyBackendController,
            @NotNull Boolean addDummyModels,
            @NotNull Boolean addDummyCommand,
            @NotNull Boolean addDummyWidget,
            @NotNull Boolean addDummyApi,
            @NotNull String interpreter
    ) {
        this.pluginName = pluginName;
        this.namespace = namespace;
        this.addDummyFilter = addDummyFilter;
        this.addDummyFrontendController = addDummyFrontendController;
        this.addDummyBackendController = addDummyBackendController;
        this.addDummyModels = addDummyModels;
        this.addDummyCommand = addDummyCommand;
        this.addDummyWidget = addDummyWidget;
        this.addDummyApi = addDummyApi;
        this.interpreter = interpreter;
    }

    @NotNull
    public String getPluginName() {
        return pluginName;
    }

    @NotNull
    public Boolean getAddDummyFilter() {
        return addDummyFilter;
    }

    @NotNull
    public Boolean getAddDummyFrontendController() {
        return addDummyFrontendController;
    }

    @NotNull
    public Boolean getAddDummyBackendController() {
        return addDummyBackendController;
    }

    @NotNull
    public Boolean getAddDummyModels() {
        return addDummyModels;
    }

    @NotNull
    public Boolean getAddDummyCommand() {
        return addDummyCommand;
    }

    @NotNull
    public Boolean getAddDummyWidget() {
        return addDummyWidget;
    }

    @NotNull
    public Boolean getAddDummyApi() {
        return addDummyApi;
    }

    @NotNull
    public String getNamespace() {
        return namespace;
    }

    @NotNull
    public String getInterpreter() {
        return interpreter;
    }
}
