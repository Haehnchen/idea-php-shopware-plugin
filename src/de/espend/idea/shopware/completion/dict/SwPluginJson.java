package de.espend.idea.shopware.completion.dict;

import java.util.HashMap;
import java.util.Map;

public class SwPluginJson {

    private String name;
    private String type;
    private Map<String, SwPluginProperty> properties = new HashMap<>();

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<String, SwPluginProperty> getProperties() {
        return properties;
    }

}
