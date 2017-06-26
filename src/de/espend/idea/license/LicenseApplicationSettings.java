package de.espend.idea.license;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name = "LicensePlugin", storages = @Storage(id = "license", file = "$APP_CONFIG$/sw-license.xml"))
public class LicenseApplicationSettings implements PersistentStateComponent<LicenseApplicationSettings> {
    public String token = null;

    @Nullable
    @Override
    public LicenseApplicationSettings getState() {
        return this;
    }

    @Override
    public void loadState(LicenseApplicationSettings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }

    public static LicenseApplicationSettings getInstance() {
        return ServiceManager.getService(LicenseApplicationSettings.class);
    }
}
