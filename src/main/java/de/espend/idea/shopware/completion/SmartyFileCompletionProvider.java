package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.SmartyFileType;
import com.jetbrains.smarty.lang.SmartyTokenTypes;
import com.jetbrains.smarty.lang.psi.SmartyTag;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.index.SmartyBlockStubIndex;
import de.espend.idea.shopware.lookup.TemplateLookupElement;
import de.espend.idea.shopware.util.*;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.stubs.SymfonyProcessors;
import fr.adrienbrault.idea.symfony2plugin.templating.util.TwigTypeResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SmartyFileCompletionProvider extends CompletionContributor  {

    public SmartyFileCompletionProvider() {
        extend(
            CompletionType.BASIC, SmartyPattern.getFilePattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {
                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    CompletionResultSet myResultSet = result;

                    // strip prefix to provide completion:
                    // {extends file="parent:<caret>"}
                    PsiElement position = parameters.getPosition();
                    if(position.getNode().getElementType() == SmartyTokenTypes.STRING_LITERAL) {
                        String text = position.getText();
                        if(text.toLowerCase().startsWith("parent:")) {
                            myResultSet = result.withPrefixMatcher("");
                        }
                    }

                    myResultSet.addAllElements(getTemplateCompletion(position.getProject(), "tpl"));
                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getLinkFilePattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    TemplateUtil.collectFiles(parameters.getPosition().getProject(), (virtualFile, fileName) -> {
                        PsiFile psiFile = PsiManager.getInstance(parameters.getPosition().getProject()).findFile(virtualFile);

                        LookupElementBuilder lookupElementBuilder;

                        if(psiFile != null) {
                            lookupElementBuilder = LookupElementBuilder.create(psiFile, fileName).withIcon(psiFile.getIcon(0));
                        } else {
                            lookupElementBuilder = LookupElementBuilder.create(fileName);
                        }

                        result.addElement(lookupElementBuilder);
                    }, SmartyPattern.TAG_LINK_FILE_EXTENSIONS);
                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getBlockPattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    PsiFile containingFile = parameters.getOriginalFile();
                    if(TemplateUtil.isExtendsTemplate(containingFile)) {
                        collectExtendsBlockNames(containingFile.getProject(), containingFile, result);
                        return;
                    }

                    // weak block on file include; use index here
                    for(String s: SymfonyProcessors.createResult(containingFile.getProject(), SmartyBlockStubIndex.KEY)) {
                        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(SmartyBlockStubIndex.KEY, s, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(containingFile.getProject()), SmartyFileType.INSTANCE));

                        LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(s).withIcon(ShopwarePluginIcons.SHOPWARE_WEAK);

                        String templateName;
                        if(files.size() > 0) {
                            templateName = TemplateUtil.getTemplateName(containingFile.getProject(), files.iterator().next());
                            if(templateName != null) {

                                if(files.size() > 1) {
                                    templateName = "(" + files.size() + ") " + templateName;
                                }

                                lookupElementBuilder = lookupElementBuilder.withTypeText(templateName, true);
                            }

                        }

                        result.addElement(lookupElementBuilder);
                    }

                }

                private void collectExtendsBlockNames(Project project, PsiFile psiFile, final @NotNull CompletionResultSet result) {

                    final Map<VirtualFile, String> map = new HashMap<>();

                    TemplateUtil.collectFiles(project, map::put);

                    List<SmartyBlockUtil.SmartyBlock> blockNameSet = new ArrayList<>();
                    SmartyBlockUtil.collectFileBlocks(psiFile, map, blockNameSet, 0);

                    for(SmartyBlockUtil.SmartyBlock smartyBlock: blockNameSet) {
                        result.addElement(LookupElementBuilder.create(smartyBlock.getName()).withTypeText(smartyBlock.getElement().getContainingFile().getName(), true).withIcon(ShopwarePluginIcons.SHOPWARE));
                    }

                }

            }

        );

        extend(
            CompletionType.BASIC, SmartyPattern.getControllerPattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    PsiElement psiElement = parameters.getOriginalPosition();

                    ShopwareUtil.collectControllerClass(psiElement.getProject(), (phpClass, moduleName, controllerName) -> result.addElement(LookupElementBuilder.create(controllerName).withTypeText(moduleName).withIcon(PhpIcons.METHOD_ICON)));

                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getNamespacePattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {
                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    PsiElement psiElement = parameters.getOriginalPosition();
                    if(psiElement == null) {
                        return;
                    }

                    Set<String> strong = new HashSet<>();
                    for (String s : SnippetUtil.getSnippetNamespaces(psiElement.getProject())) {
                        strong.add(s);
                        result.addElement(LookupElementBuilder.create(s).withIcon(Symfony2Icons.TRANSLATION).withTypeText("Snippet", true));
                    }

                    TemplateUtil.collectFiles(psiElement.getProject(), (VirtualFile virtualFile, String fileName) -> {
                        String lookupString = fileName.replaceFirst("[.][^.]+$", "");

                        if(!strong.contains(lookupString)) {
                            result.addElement(LookupElementBuilder.create(lookupString).withIcon(ShopwarePluginIcons.SHOPWARE).withTypeText("Template", true));
                        }
                    }, "tpl");
                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getControllerActionPattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    PsiElement psiElement = parameters.getOriginalPosition();

                    ShopwareUtil.collectControllerActionSmartyWrapper(psiElement, (method, methodStripped, moduleName, controllerName) -> result.addElement(LookupElementBuilder.create(method.getName().substring(0, method.getName().length() - 6)).withTypeText(moduleName + ":" + controllerName, true).withIcon(PhpIcons.METHOD_ICON)));

                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getVariableReference(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    PsiElement psiElement = parameters.getOriginalPosition();
                    if(psiElement == null) {
                        return;
                    }

                    final PsiFile psiFile = psiElement.getContainingFile();
                    if(!(psiFile instanceof SmartyFile)) {
                        return;
                    }

                    Method method = ShopwareUtil.getControllerActionOnSmartyFile((SmartyFile) psiFile);
                    if(method == null) {
                        return;
                    }

                    ShopwareUtil.collectControllerViewVariable(method, (variableName, sourceElement, typeElement) -> {
                        if (typeElement instanceof PhpTypedElement) {
                            result.addElement(LookupElementBuilder.create(variableName).withTypeText(TwigTypeResolveUtil.getTypeDisplayName(psiFile.getProject(), ((PhpTypedElement) typeElement).getType().getTypes())));
                        } else {
                            result.addElement(LookupElementBuilder.create(variableName));
                        }

                    });

                }
            }
        );


        // {action controller=Emotion action=getAvailability articleId=$sArticle.articleID}
        extend(
            CompletionType.BASIC, SmartyPattern.getControllerPattern("action"),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    PsiElement psiElement = parameters.getOriginalPosition();

                    ShopwareUtil.collectControllerClass(psiElement.getProject(), (phpClass, moduleName, controllerName) -> result.addElement(LookupElementBuilder.create(controllerName).withTypeText(moduleName).withIcon(PhpIcons.METHOD_ICON)), "Widgets");

                }
            }
        );


        // {config name=Foo}
        extend(
            CompletionType.BASIC, SmartyPattern.getConfigPattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    for(String config: ShopwareUtil.PLUGIN_CONFIGS) {
                        result.addElement(LookupElementBuilder.create(config).withIcon(Symfony2Icons.CONFIG_VALUE));
                    }

                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getControllerActionPattern("action"),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    if(!ShopwareProjectComponent.isValidForProject(parameters.getOriginalPosition())) {
                        return;
                    }

                    PsiElement psiElement = parameters.getOriginalPosition();

                    ShopwareUtil.collectControllerActionSmartyWrapper(psiElement, (method, methodStripped, moduleName, controllerName) -> result.addElement(LookupElementBuilder.create(method.getName().substring(0, method.getName().length() - 6)).withTypeText(moduleName + ":" + controllerName).withIcon(PhpIcons.METHOD_ICON)), "Widgets");

                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getTagAttributePattern("s", "name"),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {
                    PsiElement psiElement = parameters.getOriginalPosition();
                    if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
                        return;
                    }

                    PsiElement smartyTag = psiElement.getParent();
                    if(!(smartyTag instanceof SmartyTag)) {
                        return;
                    }

                    String namespace = TemplateUtil.getSnippetNamespaceByScope((SmartyTag) smartyTag);
                    if(namespace == null) {
                        return;
                    }

                    for (String s : SnippetUtil.getSnippetKeysByNamespace(psiElement.getProject(), namespace)) {
                        result.addElement(LookupElementBuilder.create(s).withIcon(Symfony2Icons.TRANSLATION).withTypeText(namespace, true));
                    }
                }
            }
        );
    }

    public static List<LookupElement> getTemplateCompletion(Project project, String... extensions) {

        final List<LookupElement> lookupElements = new ArrayList<>();
        final Set<String> uniqueList = new HashSet<>();

        TemplateUtil.collectFiles(project, (virtualFile, fileName) -> {
            if(!uniqueList.contains(fileName)) {
                lookupElements.add(new TemplateLookupElement(fileName));
                uniqueList.add(fileName);
            }
        }, extensions);

        return lookupElements;
    }
}
