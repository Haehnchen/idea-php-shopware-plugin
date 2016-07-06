package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.XmlPatternUtil;
import org.jetbrains.annotations.NotNull;

public class ShopwareXmlCompletion extends CompletionContributor {

    public ShopwareXmlCompletion() {
        extend(CompletionType.BASIC, XmlPatternUtil.getMenuControllerPattern(), new MenuControllerProvider());
    }

    private class MenuControllerProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
            ShopwareUtil.collectControllerClass(completionParameters.getPosition().getProject(), (phpClass, moduleName, controllerName) ->
                    completionResultSet.addElement(LookupElementBuilder.create(controllerName)),
            "Backend");
        }
    }
}
