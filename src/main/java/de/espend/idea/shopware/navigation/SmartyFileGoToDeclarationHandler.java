package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.lang.psi.SmartyTag;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.SnippetUtil;
import de.espend.idea.shopware.util.TemplateUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SmartyFileGoToDeclarationHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {

        if(!ShopwareProjectComponent.isValidForProject(sourceElement)) {
            return new PsiElement[0];
        }

        final List<PsiElement> targets = new ArrayList<>();

        // {link file='frontend/_resources/styles/framework.css'}
        if(SmartyPattern.getLinkFilePattern().accepts(sourceElement)) {
            attachLinkFileTagGoto(sourceElement, targets);
        }

        // {extends file="frontend/register/index.tpl"}
        if(SmartyPattern.getFilePattern().accepts(sourceElement)) {
            attachExtendsFileGoto(sourceElement, targets);
        }

        // {url controller=Account
        if(SmartyPattern.getUrlControllerPattern().accepts(sourceElement)) {
            attachControllerNameGoto(sourceElement, targets);
        }

        // {url controller=Account action=foobar
        if(SmartyPattern.getControllerActionPattern().accepts(sourceElement)) {
            attachControllerActionNameGoto(sourceElement, targets);
        }

        // {$foobar
        if(SmartyPattern.getVariableReference().accepts(sourceElement)) {
            attachControllerVariableGoto(sourceElement, targets);
        }

        // {s namespace="frontend/foo<caret>"}
        if(SmartyPattern.getNamespacePattern().accepts(sourceElement)) {
            attachSnippetNamespaceTagGoto(sourceElement, targets);
        }

        // {s name="foobar<caret>" namespace="frontend/foo"}
        if(SmartyPattern.getTagAttributePattern("s", "name").accepts(sourceElement)) {
            attachSnippetNameTagGoto(sourceElement, targets);
        }

        // {action controller=Account
        if(SmartyPattern.getActionControllerPattern().accepts(sourceElement)) {
            attachWidgetControllerNameGoto(sourceElement, targets);
        }

        // {action controller=Account action=foobar
        if(SmartyPattern.getActionActionPattern().accepts(sourceElement)) {
            attachWidgetsControllerActionNameGoto(sourceElement, targets);
        }

        return targets.toArray(new PsiElement[0]);
    }

    private void attachSnippetNameTagGoto(@NotNull PsiElement psiElement, @NotNull Collection<PsiElement> targets) {
        String contents = psiElement.getText();
        if(StringUtils.isBlank(contents)) {
            return;
        }

        PsiElement parent = psiElement.getParent();
        if(!(parent instanceof SmartyTag)) {
            return;
        }

        String namespace = TemplateUtil.getSnippetNamespaceByScope((SmartyTag) parent);
        if(namespace == null) {
            return;
        }

        targets.addAll(SnippetUtil.getSnippetNameTargets(psiElement.getProject(), namespace, contents));
    }

    private void attachControllerVariableGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        final String finalText = normalizeFilename(sourceElement.getText());

        PsiFile psiFile = sourceElement.getContainingFile();
        if(!(psiFile instanceof SmartyFile)) {
            return;
        }

        Method method = ShopwareUtil.getControllerActionOnSmartyFile((SmartyFile) psiFile);
        if(method == null) {
            return;
        }

        ShopwareUtil.collectControllerViewVariable(method, (variableName, sourceType, typeElement) -> {
            if (variableName.equals(finalText)) {
                psiElements.add(typeElement != null ? typeElement : sourceType);
            }
        });

    }

    private void attachControllerNameGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {
        final Project project = sourceElement.getProject();

        final String finalText = normalizeFilename(sourceElement.getText()).toLowerCase();
        ShopwareUtil.collectControllerClass(project, (phpClass, moduleName, controllerName) -> {
            if (controllerName.toLowerCase().equals(finalText)) {
                psiElements.add(phpClass);
            }
        });

    }

    private void attachWidgetControllerNameGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {
        final Project project = sourceElement.getProject();

        final String finalText = normalizeFilename(sourceElement.getText()).toLowerCase();
        ShopwareUtil.collectControllerClass(project, (phpClass, moduleName, controllerName) -> {
            if (controllerName.toLowerCase().equals(finalText)) {
                psiElements.add(phpClass);
            }
        }, "Widgets");

    }

    private void attachControllerActionNameGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        final String finalText = normalizeFilename(sourceElement.getText()).toLowerCase();
        ShopwareUtil.collectControllerActionSmartyWrapper(sourceElement, (method, methodStripped, moduleName, controllerName) -> {
            if(methodStripped.toLowerCase().equals(finalText)) {
                psiElements.add(method);
            }
        });

    }

    private void attachWidgetsControllerActionNameGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        final String finalText = normalizeFilename(sourceElement.getText()).toLowerCase();
        ShopwareUtil.collectControllerActionSmartyWrapper(sourceElement, (method, methodStripped, moduleName, controllerName) -> {
            if(methodStripped.toLowerCase().equals(finalText)) {
                psiElements.add(method);
            }
        }, "Widgets");

    }

    private void attachExtendsFileGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        final Project project = sourceElement.getProject();
        final VirtualFile currentFile = sourceElement.getContainingFile().getVirtualFile();

        final String finalText = normalizeFilename(sourceElement.getText());
        TemplateUtil.collectFiles(project, new TemplateUtil.SmartyTemplatePreventSelfVisitor(currentFile) {
            @Override
            public void visitNonSelfFile(VirtualFile virtualFile, String fileName) {

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

    private void attachSnippetNamespaceTagGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        final Project project = sourceElement.getProject();

        String namespace = sourceElement.getText();
        if(StringUtils.isBlank(namespace)) {
            return;
        }

        final String finalText = normalizeFilename(namespace);
        TemplateUtil.collectFiles(sourceElement.getProject(), (virtualFile, fileName) -> {

            if (!fileName.replaceFirst("[.][^.]+$", "").equals(finalText)) {
                return;
            }

            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile != null) {
                psiElements.add(psiFile);
            }
        }, "tpl");

        psiElements.addAll(SnippetUtil.getSnippetNamespaceTargets(sourceElement.getProject(), namespace));
    }

    private void attachLinkFileTagGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        final Project project = sourceElement.getProject();

        final String finalText = normalizeFilename(sourceElement.getText());
        TemplateUtil.collectFiles(sourceElement.getProject(), (virtualFile, fileName) -> {

            if (!fileName.equals(finalText)) {
                return;
            }

            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile != null) {
                psiElements.add(psiFile);
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
