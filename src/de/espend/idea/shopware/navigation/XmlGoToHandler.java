package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.completion.ShopwareXmlCompletion;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.XmlPatternUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class XmlGoToHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement psiElement, int i, Editor editor) {
        if(psiElement == null || !ShopwareProjectComponent.isValidForProject(psiElement)) {
            return new PsiElement[0];
        }

        PsiElement parent = psiElement.getParent();
        if (PlatformPatterns.or(XmlPatternUtil.getMenuControllerPattern(), XmlPatternUtil.getMenuControllerByParentPattern()).accepts(psiElement) && parent instanceof XmlText) {
            Collection<PsiElement> controllerElements = this.getControllerElements(psiElement.getProject(), ((XmlText) parent).getValue());
            return controllerElements.toArray(new PsiElement[controllerElements.size()]);
        }

        if (XmlPatternUtil.getMenuControllerActionPattern().accepts(psiElement) && parent instanceof XmlText) {
            Collection<PsiElement> controllerElements = this.getControllerActionElements(((XmlText) parent));
            return controllerElements.toArray(new PsiElement[controllerElements.size()]);
        }

        return null;
    }

    @NotNull
    private Collection<PsiElement> getControllerActionElements(@NotNull XmlText xmlText) {
        PsiElement xmlTag = xmlText.getParent();
        if(!(xmlTag instanceof XmlTag)) {
            return Collections.emptyList();
        }

        String controllerName = ShopwareXmlCompletion.getControllerOnScope((XmlTag) xmlTag);
        if (controllerName == null) {
            return Collections.emptyList();
        }

        String value = xmlText.getValue();

        Collection<PsiElement> targets = new HashSet<>();

        ShopwareUtil.collectControllerAction(xmlText.getProject(), controllerName, (method, methodStripped, moduleName, controllerName1) -> {
            if(value.equalsIgnoreCase(methodStripped)) {
                targets.add(method);
            }
        }, "Backend");

        return targets;
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
