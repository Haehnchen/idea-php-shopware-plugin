package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.TemplateUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SmartyFileGoToDeclarationHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {

        final List<PsiElement> targets = new ArrayList<PsiElement>();

        // {link file='frontend/_resources/styles/framework.css'}
        if(SmartyPattern.getLinkFilePattern().accepts(sourceElement)) {
            attachLinkFileTagGoto(sourceElement, targets);
        }

        // {extends file="frontend/register/index.tpl"}
        if(SmartyPattern.getFilePattern().accepts(sourceElement)) {
            attachExtendsFileGoto(sourceElement, targets);
        }

        return targets.toArray(new PsiElement[targets.size()]);
    }
    private void attachExtendsFileGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {
        final Project project = sourceElement.getProject();

        final String finalText = normalizeFilename(sourceElement.getText());
        TemplateUtil.collectFiles(project, new TemplateUtil.SmartyTemplateVisitor() {
            @Override
            public void visitFile(VirtualFile virtualFile, String fileName) {

                if (!fileName.equals(finalText)) {
                    return;
                }

                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile != null) {
                    psiElements.add(psiFile);
                }
            }
        });

    }

    private void attachLinkFileTagGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        final Project project = sourceElement.getProject();

        final String finalText = normalizeFilename(sourceElement.getText());
        TemplateUtil.collectFiles(sourceElement.getProject(), new TemplateUtil.SmartyTemplateVisitor() {
            @Override
            public void visitFile(VirtualFile virtualFile, String fileName) {

                if (!fileName.equals(finalText)) {
                    return;
                }

                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile != null) {
                    psiElements.add(psiFile);
                }
            }
        }, SmartyPattern.TAG_LINK_FILE_EXTENSIONS);


    }

    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

    private String normalizeFilename(String text) {

        if(text.startsWith("parent:")) {
            text = text.substring(7);
        }

        if(text.startsWith("./")) {
            text = text.substring(2);
        }

        return text;
    }

}
