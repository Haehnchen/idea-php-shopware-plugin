package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ShopwareUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
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

                    if(new MethodMatcher.StringParameterRecursiveMatcher(originalPosition.getContext(), 0).withSignature("\\Shopware_Components_Config", "get").match() != null) {
                        for(String type: ShopwareUtil.PLUGIN_CONFIGS) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(Symfony2Icons.CONFIG_VALUE));
                        }
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

        extend(CompletionType.BASIC, ShopwareUtil.getBootstrapPathPattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition == null || !ShopwareProjectComponent.isValidForProject(originalPosition)) {
                        return;
                    }

                    PsiElement parent = originalPosition.getParent();
                    if(!(parent instanceof StringLiteralExpression)) {
                        return;
                    }

                    ShopwareUtil.collectBootstrapFiles((StringLiteralExpression) parent, new ShopwareUtil.BootstrapFileVisitor() {
                        @Override
                        public void visitVariable(VirtualFile virtualFile, String relativePath) {
                            if (virtualFile.isDirectory()) {
                                result.addElement(LookupElementBuilder.create(relativePath + "/").withIcon(PhpIcons.FILE_ICON));
                            } else if ("php".equalsIgnoreCase(virtualFile.getExtension())) {
                                result.addElement(LookupElementBuilder.create(relativePath).withIcon(PhpIcons.PHP_FILE));
                            }
                        }
                    });

                }
            }
        );

        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression.class).inside(
                PlatformPatterns.psiElement(ParameterList.class)
            )),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition == null || !ShopwareProjectComponent.isValidForProject(originalPosition)) {
                        return;
                    }

                    PsiElement parent = originalPosition.getParent();
                    if(!(parent instanceof StringLiteralExpression)) {
                        return;
                    }

                    if(new MethodMatcher.ArrayParameterMatcher(originalPosition.getContext(), 0).withSignature("\\Enlight_Controller_Router", "assemble").match() != null) {
                        for(String type: ShopwareUtil.ROUTE_ASSEMBLE) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }

                }
            }
        );


    }

}
