package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import de.espend.idea.shopware.ShopwarePluginIcons;
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
                protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {
                    TemplateUtil.collectFiles(parameters.getPosition().getProject(), new TemplateUtil.SmartyTemplateVisitor() {
                        @Override
                        public void visitFile(VirtualFile virtualFile, String fileName) {
                            result.addElement(LookupElementBuilder.create(fileName).withIcon(ShopwarePluginIcons.SHOPWARE_SMARTY));
                        }
                    });
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
                        result.addElement(LookupElementBuilder.create(smartyBlock.getName()).withTypeText(smartyBlock.getElement().getContainingFile().getName()));
                    }

                }
            }
        );


    }


}
