package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlText;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.XmlPatternUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class XmlGoToHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement psiElement, int i, Editor editor) {

        PsiElement parent = psiElement.getParent();
        if (XmlPatternUtil.getMenuControllerPattern().accepts(psiElement) && parent instanceof XmlText) {
            Collection<PsiElement> controllerElements = this.getControllerElements(psiElement.getProject(), ((XmlText) parent).getValue());

            return controllerElements.toArray(new PsiElement[controllerElements.size()]);
        }

        return null;
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }

    private Collection<PsiElement> getControllerElements(Project project, String controller)
    {
        Collection<PsiElement> elements = new ArrayList<>();

        ShopwareUtil.collectControllerClass(project, (phpClass, moduleName, controllerName) -> {
            if (controller.equals(controllerName)) {
                elements.add(phpClass);
            }
        }, "Backend");

        return elements;
    }
}
