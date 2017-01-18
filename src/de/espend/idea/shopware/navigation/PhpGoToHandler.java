package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.completion.ShopwarePhpCompletion;
import de.espend.idea.shopware.util.ConfigUtil;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.ThemeUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
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

        if(PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)).accepts(psiElement)) {
            attachNamespaceNavigation(psiElement, psiElements);
        }

        if(PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)).accepts(psiElement)) {
            attachNamespaceValueNavigation(psiElement, psiElements);
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    private void attachNamespaceNavigation(@NotNull PsiElement psiElement, @NotNull List<PsiElement> psiElements) {
        PsiElement parent = psiElement.getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return;
        }

        String contents = ((StringLiteralExpression) parent).getContents();
        if(StringUtils.isBlank(contents)) {
            return;
        }

        MethodMatcher.MethodMatchParameter match = MethodMatcher.getMatchedSignatureWithDepth(parent, ShopwarePhpCompletion.CONFIG_NAMESPACE);
        if(match == null) {
            return;
        }

        ConfigUtil.visitNamespace(psiElement.getProject(), pair -> {
            if(contents.equalsIgnoreCase(pair.getFirst())) {
                PhpClass phpClass = pair.getSecond();
                PsiElement target = phpClass;

                // PhpClass or "Resources/config.xml" as target
                PsiDirectory pluginDir = phpClass.getContainingFile().getParent();
                if(pluginDir != null) {
                    VirtualFile resources = VfsUtil.findRelativeFile(pluginDir.getVirtualFile(), "Resources", "config.xml");
                    if(resources != null) {
                        PsiFile file = PsiManager.getInstance(phpClass.getProject()).findFile(resources);
                        if(file instanceof XmlFile) {
                            target = file;
                        }
                    }
                }

                psiElements.add(target);
            }
        });
    }

    private void attachNamespaceValueNavigation(@NotNull PsiElement psiElement, @NotNull List<PsiElement> psiElements) {
        PsiElement parent = psiElement.getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return;
        }

        String contents = ((StringLiteralExpression) parent).getContents();
        if(StringUtils.isBlank(contents)) {
            return;
        }

        String namespace = ConfigUtil.getNamespaceFromConfigValueParameter((StringLiteralExpression) parent);
        if (namespace == null) {
            return;
        }

        ConfigUtil.visitNamespaceConfigurations(psiElement.getProject(), namespace, pair -> {
            if(contents.equalsIgnoreCase(pair.getFirst())) {
                psiElements.add(pair.getSecond());
            }
        });
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
