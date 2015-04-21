package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.ThemeUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.jetbrains.annotations.NotNull;
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

        List<PsiElement> psiElements = new ArrayList<PsiElement>();

        if(ShopwareUtil.getBootstrapPathPattern().accepts(psiElement)) {
            attachBootstrapFiles(psiElement, psiElements);
        }

        if(ThemeUtil.getJavascriptClassFieldPattern().accepts(psiElement)){
            attachThemeJsFieldReferences(psiElement, psiElements);
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    private void attachThemeJsFieldReferences(final PsiElement psiElement, final List<PsiElement> psiElements) {

        final PsiElement parent = psiElement.getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return;
        }

        final String contents = ((StringLiteralExpression) parent).getContents();

        ThemeUtil.collectThemeJsFieldReferences((StringLiteralExpression) parent, new ThemeUtil.ThemeAssetVisitor() {
            @Override
            public boolean visit(@NotNull VirtualFile virtualFile, @NotNull String path) {
                if(path.equals(contents)) {
                    PsiFile psiFile = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
                    if(psiFile != null) {
                        psiElements.add(psiFile);
                    }

                    return false;
                }

                return true;
            }
        });

    }

    private void attachBootstrapFiles(final PsiElement psiElement, final List<PsiElement> psiElements) {

        final PsiElement parent = psiElement.getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return;
        }

        ShopwareUtil.collectBootstrapFiles((StringLiteralExpression) parent, new ShopwareUtil.BootstrapFileVisitor() {
            @Override
            public void visitVariable(VirtualFile virtualFile, String relativePath) {

                String contents = ((StringLiteralExpression) parent).getContents();
                if(contents.endsWith("\\") || contents.endsWith("/")) {
                    contents = contents.substring(0, contents.length() - 1);
                }

                if(!contents.equalsIgnoreCase(relativePath)) {
                    return;
                }

                PsiFile file = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
                if(file != null) {
                    psiElements.add(file);
                }

            }
        });

    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }
}
