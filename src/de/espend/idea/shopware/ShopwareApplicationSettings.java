package de.espend.idea.shopware;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name = "ShopwarePlugin", storages = @Storage(id = "shopware-plugin", file = "$APP_CONFIG$/shopware.app.xml"))
public class ShopwareApplicationSettings implements PersistentStateComponent<ShopwareApplicationSettings> {

    public static final String DEFAULT_CLI_URL = "http://shopwarelabs.github.io/sw-cli-tools/sw.phar";
    public String cliToolsPharUrl = DEFAULT_CLI_URL;

    @Nullable
    @Override
    public ShopwareApplicationSettings getState() {
        return this;
    }

    @Override
    public void loadState(ShopwareApplicationSettings shopwareApplicationSettings) {
        XmlSerializerUtil.copyBean(shopwareApplicationSettings, this);
    }

    public static ShopwareApplicationSettings getInstance() {
        return ServiceManager.getService(ShopwareApplicationSettings.class);
    }

}
