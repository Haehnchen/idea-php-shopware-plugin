package de.espend.idea.shopware.index.dict;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public enum BootstrapResource {
    INIT_RESOURCE("Enlight_Bootstrap_InitResource"),
    AFTER_INIT_RESOURCE("Enlight_Bootstrap_AfterInitResource"),
    AFTER_REGISTER_RESOURCE("Enlight_Bootstrap_AfterRegisterResource");

    private String text;

    BootstrapResource(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static BootstrapResource fromString(String text) {
        if (text != null) {
            for (BootstrapResource value : BootstrapResource.values()) {
                if (text.equalsIgnoreCase(value.text)) {
                    return value;
                }
            }
        }
        return null;
    }
}
