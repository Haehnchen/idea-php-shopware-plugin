package de.espend.idea.shopware.action.generator.dict;

import org.jetbrains.annotations.NotNull;

public class PluginGeneratorSettings {

    @NotNull
    private final String pluginName;

    @NotNull
    private final String namespace;

    private final boolean addDummyFilter;

    private final boolean addDummyFrontendController;

    private final boolean addDummyBackendController;

    private final boolean addDummyModels;

    private final boolean addDummyCommand;

    private final boolean addDummyWidget;

    private final boolean addDummyApi;

    @NotNull
    private final String interpreter;

    private final boolean legacyStructure;

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
            @NotNull String interpreter,
            @NotNull Boolean legacyStructure
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
        this.legacyStructure = legacyStructure;
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

    public boolean isLegacyStructure() {
        return legacyStructure;
    }
}
