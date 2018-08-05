package de.espend.idea.shopware.action.generator;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import fr.adrienbrault.idea.symfony2plugin.action.AbstractProjectDumbAwareAction;

public class NewCronjobXmlAction extends AbstractProjectDumbAwareAction {

    public NewCronjobXmlAction() {
        super("Create a cronjob.xml", "Create a new cronjob.xml", AllIcons.FileTypes.Xml);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        ServiceActionUtil.buildFile(event, project, "/fileTemplates/cronjob.xml", "cronjob.xml");
    }
}
