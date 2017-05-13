package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.XmlPatternUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopwareXmlCompletion extends CompletionContributor {

    public ShopwareXmlCompletion() {
        extend(CompletionType.BASIC, PlatformPatterns.or(XmlPatternUtil.getMenuControllerPattern(), XmlPatternUtil.getMenuControllerByParentPattern()), new MenuControllerProvider());
        extend(CompletionType.BASIC, XmlPatternUtil.getMenuControllerActionPattern(), new MenuControllerActionProvider());
    }

    private class MenuControllerProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
            ShopwareUtil.collectControllerClass(completionParameters.getPosition().getProject(), (phpClass, moduleName, controllerName) -> {
                    LookupElementBuilder lookupElement = LookupElementBuilder.create(controllerName)
                        .withIcon(phpClass.getIcon())
                        .withTypeText(phpClass.getPresentableFQN(), true);

                    completionResultSet.addElement(lookupElement);
                }, "Backend");
        }
    }

    private class MenuControllerActionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
            PsiElement psiElement = completionParameters.getOriginalPosition();
            if(psiElement == null || !ShopwareProjectComponent.isValidForProject(psiElement)) {
                return;
            }

            PsiElement parent = psiElement.getParent();
            if(!(parent instanceof XmlTag)) {
                return;
            }

            String controllerName = getControllerOnScope((XmlTag) parent);
            if (controllerName == null) {
                return;
            }

            ShopwareUtil.collectControllerAction(psiElement.getProject(), controllerName, (method, methodStripped, moduleName, controllerName1) -> {
                LookupElementBuilder lookupElement = LookupElementBuilder.create(methodStripped)
                    .withIcon(method.getIcon())
                    .withTypeText(method.getName(), true);

                completionResultSet.addElement(lookupElement);
            }, "Backend");
        }
    }

    @Nullable
    public static String getControllerOnScope(@NotNull XmlTag xmlTag) {
        PsiElement menuTag = xmlTag.getParent();
        if(!(menuTag instanceof XmlTag)) {
            return null;
        }

        String name = ((XmlTag) menuTag).getName();
        if(!"entry".equals(name)) {
            return null;
        }

        XmlTag controller = ((XmlTag) menuTag).findFirstSubTag("controller");
        if(controller == null) {
            return null;
        }

        String controllerName = controller.getValue().getTrimmedText();
        if(StringUtils.isBlank(controllerName)) {
            return null;
        }

        return controllerName;
    }
}
