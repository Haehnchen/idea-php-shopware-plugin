package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ShopwareUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.jetbrains.annotations.NotNull;

public class ShopwarePhpCompletion extends CompletionContributor{

    public ShopwarePhpCompletion() {

        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression.class).inside(
                PlatformPatterns.psiElement(ParameterList.class)
            )
        ),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition == null || !ShopwareProjectComponent.isValidForProject(originalPosition)) {
                        return;
                    }

                    if(new MethodMatcher.StringParameterRecursiveMatcher(originalPosition.getContext(), 0).withSignature("\\Shopware\\Models\\Config\\Form", "setElement").match() != null) {
                        for(String type: ShopwareUtil.PLUGIN_CONFIG_TYPES) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }

                    if(new MethodMatcher.ArrayParameterMatcher(originalPosition.getContext(), 2).withSignature("\\Shopware\\Models\\Config\\Form", "setElement").match() != null) {
                        for(String type: ShopwareUtil.PLUGIN_CONFIG_OPTIONS) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }

                    if(new MethodMatcher.StringParameterRecursiveMatcher(originalPosition.getContext(), 0).withSignature("\\Shopware\\Components\\Model\\ModelManager", "addAttribute").withSignature("\\Shopware\\Components\\Model\\ModelManager", "removeAttribute").match() != null) {
                        for(String type: ShopwareUtil.MODEL_STATIC_ATTRIBUTES) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }

                    if(new MethodMatcher.ArrayParameterMatcher(originalPosition.getContext(), 0).withSignature("\\Shopware\\Components\\Model\\ModelManager", "generateAttributeModels").match() != null) {
                        for(String type: ShopwareUtil.MODEL_STATIC_ATTRIBUTES) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }

                    if(new MethodMatcher.StringParameterRecursiveMatcher(originalPosition.getContext(), 3).withSignature("\\Shopware\\Components\\Model\\ModelManager", "addAttribute").match() != null) {
                        for(String type: ShopwareUtil.MODEL_STATIC_ATTRIBUTE_TYPES) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }

                }
            }
        );

        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
                PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(
                    PlatformPatterns.psiElement(PhpElementTypes.ARRAY_KEY).inside(
                        PlatformPatterns.psiElement(PhpReturn.class)
                )
            )
        ),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition == null || !ShopwareProjectComponent.isValidForProject(originalPosition)) {
                        return;
                    }

                    PsiElement string = originalPosition.getParent();
                    if(string != null) {
                        PsiElement arrayKey = string.getParent();
                        if(arrayKey != null) {
                            PsiElement hashElement = arrayKey.getParent();
                            if(hashElement instanceof ArrayHashElement) {
                                PsiElement arrayCreation = hashElement.getParent();
                                if(arrayCreation instanceof ArrayCreationExpression) {
                                    PsiElement phpReturn = arrayCreation.getParent();
                                    if(phpReturn instanceof PhpReturn) {
                                        Method method = PsiTreeUtil.getParentOfType(phpReturn, Method.class);
                                        if(method != null && "getInfo".equals(method.getName())) {
                                            for(String type: ShopwareUtil.PLUGIN_INFO) {
                                                result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                }
            }
        );


    }

}
