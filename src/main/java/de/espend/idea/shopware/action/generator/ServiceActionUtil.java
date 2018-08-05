package de.espend.idea.shopware.action.generator;

import com.intellij.ide.IdeView;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.yaml.YAMLFileType;

import java.io.IOException;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class ServiceActionUtil {

    public static void buildFile(AnActionEvent event, final Project project, String templatePath, String fileName) {
        DataContext dataContext = event.getDataContext();
        IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return;
        }

        PsiDirectory[] directories = view.getDirectories();
        if(directories.length == 0) {
            return;
        }

        final PsiDirectory initialBaseDir = directories[0];
        if (initialBaseDir == null) {
            return;
        }

        if(initialBaseDir.findFile(fileName) != null) {
            Messages.showInfoMessage("File exists", "Error");
            return;
        }

        String content;
        try {
            content = StreamUtil.readText(ServiceActionUtil.class.getResourceAsStream(templatePath), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        final PsiFileFactory factory = PsiFileFactory.getInstance(project);

        final PsiFile file = factory.createFileFromText(fileName, XmlFileType.INSTANCE, content);

        ApplicationManager.getApplication().runWriteAction(() -> {
            CodeStyleManager.getInstance(project).reformat(file);
            initialBaseDir.add(file);
        });

        PsiFile psiFile = initialBaseDir.findFile(fileName);
        if(psiFile != null) {
            view.selectElement(psiFile);
        }

    }
}