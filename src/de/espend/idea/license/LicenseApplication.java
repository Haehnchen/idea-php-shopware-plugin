package de.espend.idea.license;

import com.intellij.openapi.components.ApplicationComponent;
import de.espend.idea.license.impl.ApplicationImplementation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LicenseApplication implements ApplicationComponent, ApplicationImplementation {
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private boolean isLicenseValid = false;

    @Override
    public void initComponent() {
        executor.scheduleAtFixedRate(() -> {
            LicenseUtil.validate("de.espend.idea.shopware", "foobar", this);
        }, 250, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void disposeComponent() {
        executor.shutdownNow();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Plugin License";
    }

    @Override
    public void validatedLicense() {
        this.isLicenseValid = true;
    }

    @Override
    public void invalidedLicense() {
        this.isLicenseValid = false;
    }

    public boolean isLicensed() {
        return isLicenseValid;
    }

    @Override
    public void persistToken(@NotNull String token) {
        LicenseApplicationSettings.getInstance().token = token;
    }

    @Nullable
    public String loadToken() {
        return LicenseApplicationSettings.getInstance().token;
    }
}