package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.lookup.TemplateLookupElement;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.SmartyBlockUtil;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.TemplateUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SmartyFileCompletionProvider extends CompletionContributor  {

    public SmartyFileCompletionProvider() {
        extend(
            CompletionType.BASIC, SmartyPattern.getFilePattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {
                    TemplateUtil.collectFiles(parameters.getPosition().getProject(), new TemplateUtil.SmartyTemplateVisitor() {
                        @Override
                        public void visitFile(VirtualFile virtualFile, String fileName) {
                            result.addAllElements(getTemplateCompletion(parameters.getPosition().getProject(), "tpl"));
                        }
                    });
                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getLinkFilePattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {
                    TemplateUtil.collectFiles(parameters.getPosition().getProject(), new TemplateUtil.SmartyTemplateVisitor() {
                        @Override
                        public void visitFile(VirtualFile virtualFile, String fileName) {
                            PsiFile psiFile = PsiManager.getInstance(parameters.getPosition().getProject()).findFile(virtualFile);
                            LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(fileName);
                            if(psiFile != null) {
                                lookupElementBuilder.withIcon(psiFile.getFileType().getIcon());
                            }
                            result.addElement(lookupElementBuilder);
                        }
                    }, SmartyPattern.TAG_LINK_FILE_EXTENSIONS);
                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getBlockPattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {
                    final Map<VirtualFile, String> map = new HashMap<VirtualFile, String>();

                    TemplateUtil.collectFiles(parameters.getPosition().getProject(), new TemplateUtil.SmartyTemplateVisitor() {
                        @Override
                        public void visitFile(VirtualFile virtualFile, String fileName) {
                            map.put(virtualFile, fileName);
                        }
                    });

                    List<SmartyBlockUtil.SmartyBlock> blockNameSet = new ArrayList<SmartyBlockUtil.SmartyBlock>();
                    SmartyBlockUtil.collectFileBlocks(parameters.getOriginalFile(), map, blockNameSet, 0);

                    for(SmartyBlockUtil.SmartyBlock smartyBlock: blockNameSet) {
                        result.addElement(LookupElementBuilder.create(smartyBlock.getName()).withTypeText(smartyBlock.getElement().getContainingFile().getName()).withIcon(ShopwarePluginIcons.SHOPWARE));
                    }

                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getControllerPattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement psiElement = parameters.getOriginalPosition();

                    ShopwareUtil.collectControllerClass(psiElement.getProject(), new ShopwareUtil.ControllerClassVisitor() {
                        @Override
                        public void visitClass(PhpClass phpClass, String moduleName, String controllerName) {
                            result.addElement(LookupElementBuilder.create(controllerName).withTypeText(moduleName).withIcon(PhpIcons.METHOD_ICON));
                        }
                    });

                }
            }
        );

        extend(
            CompletionType.BASIC, SmartyPattern.getControllerActionPattern(),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement psiElement = parameters.getOriginalPosition();

                    ShopwareUtil.collectControllerActionSmartyWrapper(psiElement, new ShopwareUtil.ControllerActionVisitor() {
                        @Override
                        public void visitMethod(Method method, String methodStripped, String moduleName, String controllerName) {
                            result.addElement(LookupElementBuilder.create(method.getName().substring(0, method.getName().length() - 6)).withTypeText(moduleName + ":" + controllerName).withIcon(PhpIcons.METHOD_ICON));
                        }
                    });

                }
            }
        );

    }

    public static List<LookupElement> getTemplateCompletion(Project project, String... extensions) {

        final List<LookupElement> lookupElements = new ArrayList<LookupElement>();
        final Set<String> uniqueList = new HashSet<String>();

        TemplateUtil.collectFiles(project, new TemplateUtil.SmartyTemplateVisitor() {
            @Override
            public void visitFile(VirtualFile virtualFile, String fileName) {
                if(!uniqueList.contains(fileName)) {
                    lookupElements.add(new TemplateLookupElement(fileName));
                    uniqueList.add(fileName);
                }
            }
        }, extensions);

        return lookupElements;
    }




}
