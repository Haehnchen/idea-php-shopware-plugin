package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.smarty.lang.SmartyTokenTypes;
import com.jetbrains.smarty.lang.psi.SmartyCompositeElementTypes;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.TemplateUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SmartyFileGoToDeclarationHandler implements GotoDeclarationHandler {
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {

        if(!SmartyPattern.getFilePattern().accepts(sourceElement)) {
            return new PsiElement[0];
        }

        String text = sourceElement.getText();
        if(text.startsWith("parent:")) {
            text = text.substring(7);
        }

        final List<PsiFile> psiFiles = new ArrayList<PsiFile>();
        final Project project = sourceElement.getProject();

        final String finalText = text;
        TemplateUtil.collectFiles(project, new TemplateUtil.SmartyTemplateVisitor() {
            @Override
            public void visitFile(VirtualFile virtualFile, String fileName) {

                if (!fileName.equals(finalText)) {
                    return;
                }

                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile != null) {
                    psiFiles.add(psiFile);
                }
            }
        });

        return psiFiles.toArray(new PsiElement[psiFiles.size()]);
    }

    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

}
