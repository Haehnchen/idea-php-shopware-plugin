package de.espend.idea.shopware.action.generator;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import fr.adrienbrault.idea.symfony2plugin.action.AbstractProjectDumbAwareAction;

public class NewMenuXmlAction extends AbstractProjectDumbAwareAction {

    public NewMenuXmlAction() {
        super("Create a menu.xml", "Create a new menu.xml", AllIcons.FileTypes.Xml);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        ServiceActionUtil.buildFile(event, project, "/fileTemplates/menu.xml", "menu.xml");
    }
}
