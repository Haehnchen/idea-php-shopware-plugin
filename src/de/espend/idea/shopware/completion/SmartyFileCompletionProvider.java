package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.TemplateUtil;
import org.jetbrains.annotations.NotNull;

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
    }

}
