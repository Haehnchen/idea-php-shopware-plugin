package de.espend.idea.shopware.action.generator;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.action.generator.ui.PluginGeneratorDialog;
import de.espend.idea.shopware.action.generator.utils.PluginGeneratorUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;

public class PluginGeneratorAction extends DumbAwareAction {

    public PluginGeneratorAction() {
        super("Create Plugin", "Create Plugin", ShopwarePluginIcons.SHOPWARE);
    }

    public void update(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if (project == null || !Symfony2ProjectComponent.isEnabled(project)) {
            setStatus(event, false);
        }
    }

    private void setStatus(AnActionEvent event, boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        if(project == null) {
            return;
        }

        PluginGeneratorDialog.createAndShow(null, settings -> PluginGeneratorUtil.installPlugin(project, settings));
    }
}
