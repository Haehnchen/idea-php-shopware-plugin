package de.espend.idea.shopware.installer.project;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.platform.WebProjectGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareInstallerGeneratorPeer implements WebProjectGenerator.GeneratorPeer<ShopwareInstallerSettings> {

    private ShopwareInstallerForm symfonyInstallerForm;

    public ShopwareInstallerGeneratorPeer() {
        symfonyInstallerForm = new ShopwareInstallerForm();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return symfonyInstallerForm.getContentPane();
    }

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {
        settingsStep.addSettingsComponent(symfonyInstallerForm.getContentPane());
    }

    @NotNull
    @Override
    public ShopwareInstallerSettings getSettings() {
        return new ShopwareInstallerSettings(
            symfonyInstallerForm.getVersion(),
            symfonyInstallerForm.getInterpreter()
        );
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
        return null;
    }

    @Override
    public boolean isBackgroundJobRunning() {
        return false;
    }

    @Override
    public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener settingsStateListener) {

    }
}
