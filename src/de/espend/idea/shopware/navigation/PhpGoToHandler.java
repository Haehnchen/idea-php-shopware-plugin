package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.ThemeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpGoToHandler implements GotoDeclarationHandler {
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {

        List<PsiElement> psiElements = new ArrayList<>();

        if(ShopwareUtil.getBootstrapPathPattern().accepts(psiElement)) {
            attachBootstrapFiles(psiElement, psiElements);
        }

        if(ThemeUtil.getJavascriptClassFieldPattern().accepts(psiElement)){
            attachThemeJsFieldReferences(psiElement, psiElements);
        }

        if(ThemeUtil.getThemeExtendsPattern().accepts(psiElement)){
            attachThemeExtend(psiElement, psiElements);
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    private void attachThemeJsFieldReferences(final PsiElement psiElement, final List<PsiElement> psiElements) {

        final PsiElement parent = psiElement.getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return;
        }

        final String contents = ((StringLiteralExpression) parent).getContents();
        if(StringUtils.isBlank(contents)) {
            return;
        }

        ThemeUtil.collectThemeJsFieldReferences((StringLiteralExpression) parent, (virtualFile, path) -> {
            if(path.equals(contents)) {
                PsiFile psiFile = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
                if(psiFile != null) {
                    psiElements.add(psiFile);
                }

                return false;
            }

            return true;
        });

    }
    private void attachThemeExtend(PsiElement psiElement, List<PsiElement> psiElements) {

        final PsiElement parent = psiElement.getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return;
        }

        final String contents = ((StringLiteralExpression) parent).getContents();
        if(StringUtils.isBlank(contents)) {
            return;
        }

        for(PhpClass phpClass: PhpIndex.getInstance(parent.getProject()).getAllSubclasses("\\Shopware\\Components\\Theme")) {
            String name = phpClass.getContainingFile().getContainingDirectory().getName();
            if(contents.equals(name)) {
                psiElements.add(phpClass.getContainingFile().getContainingDirectory());
            }
        }

    }

    private void attachBootstrapFiles(final PsiElement psiElement, final List<PsiElement> psiElements) {

        final PsiElement parent = psiElement.getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return;
        }

        ShopwareUtil.collectBootstrapFiles((StringLiteralExpression) parent, (virtualFile, relativePath) -> {
            String contents = ((StringLiteralExpression) parent).getContents();
            if(contents.endsWith("\\") || contents.endsWith("/")) {
                contents = contents.substring(0, contents.length() - 1);
            }

            if(!StringUtils.stripStart(contents, "/\\").equalsIgnoreCase(relativePath)) {
                return;
            }

            ContainerUtil.addIfNotNull(psiElements, PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile));
            ContainerUtil.addIfNotNull(psiElements, PsiManager.getInstance(psiElement.getProject()).findDirectory(virtualFile));
        });
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }
}
