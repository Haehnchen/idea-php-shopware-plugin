package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.index.ConfigIndex;
import de.espend.idea.shopware.util.ConfigUtil;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.ThemeUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import icons.ShopwareIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwarePhpCompletion extends CompletionContributor{

    private static MethodMatcher.CallToSignature[] ATTRIBUTE_SERVICE_SIGNATURE = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService", "update"),
    };

    private static MethodMatcher.CallToSignature[] ATTRIBUTE_SERVICE_SIGNATURE_TABLES = new MethodMatcher.CallToSignature[] {
        ATTRIBUTE_SERVICE_SIGNATURE[0],
        new MethodMatcher.CallToSignature("\\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService", "update"),
        new MethodMatcher.CallToSignature("\\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService", "delete"),
        new MethodMatcher.CallToSignature("\\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService", "get"),
        new MethodMatcher.CallToSignature("\\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService", "getList"),
        new MethodMatcher.CallToSignature("\\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService", "createAttribute"),
        new MethodMatcher.CallToSignature("\\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService", "changeAttribute"),
    };

    public static MethodMatcher.CallToSignature[] CONFIG_NAMESPACE = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Shopware_Components_Config", "getByNamespace"),
    };

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

                        for (Set<String> configValues : FileBasedIndex.getInstance().getValues(ConfigIndex.KEY, "all", GlobalSearchScope.allScope(originalPosition.getProject()))) {
                            for (String config : configValues) {
                                result.addElement(LookupElementBuilder.create(config).withIcon(ShopwareIcons.SHOPWARE));
                            }
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

                    ShopwareUtil.collectBootstrapFiles((StringLiteralExpression) parent, (virtualFile, relativePath) -> {
                        if (virtualFile.isDirectory()) {
                            result.addElement(LookupElementBuilder.create(relativePath + "/").withIcon(PhpIcons.FILE_ICON));
                        } else if ("php".equalsIgnoreCase(virtualFile.getExtension())) {
                            result.addElement(LookupElementBuilder.create(relativePath).withIcon(PhpIcons.PHP_FILE));
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

        extend(CompletionType.BASIC, ThemeUtil.getJavascriptClassFieldPattern(),
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

                    ThemeUtil.collectThemeJsFieldReferences((StringLiteralExpression) parent, (virtualFile, path) -> {
                        result.addElement(LookupElementBuilder.create(path).withIcon(AllIcons.FileTypes.JavaScript));
                        return true;
                    });

                }
            }
        );

        extend(CompletionType.BASIC, ThemeUtil.getThemeExtendsPattern(),
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

                    for(PhpClass phpClass: PhpIndex.getInstance(parent.getProject()).getAllSubclasses("\\Shopware\\Components\\Theme")) {
                        String name = phpClass.getContainingFile().getContainingDirectory().getName();
                        result.addElement(LookupElementBuilder.create(name).withIcon(ShopwarePluginIcons.SHOPWARE));
                    }

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

                    if(MethodMatcher.getMatchedSignatureWithDepth(originalPosition.getContext(), ATTRIBUTE_SERVICE_SIGNATURE_TABLES) != null) {
                        for(String type: ShopwareUtil.MODEL_STATIC_ATTRIBUTES) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }

                    if(MethodMatcher.getMatchedSignatureWithDepth(originalPosition.getContext(), ATTRIBUTE_SERVICE_SIGNATURE, 2) != null) {
                        for(String type: ShopwareUtil.ATTRIBUTE_DATA_TYPES) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }

                    if(new MethodMatcher.ArrayParameterMatcher(originalPosition.getContext(), 3).withSignature(ATTRIBUTE_SERVICE_SIGNATURE).match() != null) {
                        for(String type: ShopwareUtil.ATTRIBUTE_BACKEND_VIEWS) {
                            result.addElement(LookupElementBuilder.create(type).withIcon(ShopwarePluginIcons.SHOPWARE));
                        }
                    }
                }
            }
        );

        // $this->config->getByNamespace('<caret>', 'foobar');
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)),
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

                    MethodMatcher.MethodMatchParameter match = MethodMatcher.getMatchedSignatureWithDepth(parent, CONFIG_NAMESPACE);
                    if(match == null) {
                        return;
                    }

                    ConfigUtil.visitNamespace(originalPosition.getProject(), pair ->
                        result.addElement(LookupElementBuilder.create(pair.getFirst())
                            .withTypeText(pair.getSecond().getPresentableFQN(), true)
                            .withIcon(ShopwareIcons.SHOPWARE)
                        )
                    );
                }
            }
        );

        // $this->config->getByNamespace('MyNamesapce', '<caret>');
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)),
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

                    String namespace = ConfigUtil.getNamespaceFromConfigValueParameter((StringLiteralExpression) parent);
                    if (namespace == null) {
                        return;
                    }

                    ConfigUtil.visitNamespaceConfigurations(originalPosition.getProject(), namespace, pair -> {
                        XmlTag parentTag = pair.getSecond().getParentTag();

                        // <element required="true" type="text"><name>foobar</name></element>
                        String type = null;
                        if (parentTag != null) {
                            type = parentTag.getAttributeValue("type");
                        }

                        result.addElement(LookupElementBuilder.create(pair.getFirst()).withTypeText(type, true).withIcon(ShopwareIcons.SHOPWARE));
                    });
                }
            }
        );
    }
}
