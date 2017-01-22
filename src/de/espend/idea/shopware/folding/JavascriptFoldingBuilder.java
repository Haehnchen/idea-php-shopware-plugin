package de.espend.idea.shopware.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import de.espend.idea.shopware.util.ExtJsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class JavascriptFoldingBuilder extends FoldingBuilderEx {
    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement psiElement, @NotNull Document document, boolean b) {
        Collection<FoldingDescriptor> foldingDescriptors = new ArrayList<>();

        for (PsiElement element : psiElement.getChildren()) {
            if(element instanceof PsiComment) {
                String text = element.getText();
                Matcher matcher = Pattern.compile(ExtJsUtil.JS_NAMESPACE_PATTERN).matcher(text);
                if(!matcher.find()) {
                    return new FoldingDescriptor[0];
                }

                String namespace = StringUtils.trim(matcher.group(1));
                if(StringUtils.isBlank(namespace)) {
                    return new FoldingDescriptor[0];
                }

                foldingDescriptors.add(
                    new MyFoldingDescriptor(String.format("{s namespace=%s}", namespace), element, element.getTextRange())
                );
            }
        }

        return foldingDescriptors.toArray(new FoldingDescriptor[foldingDescriptors.size()]);
    }

    private static class MyFoldingDescriptor extends FoldingDescriptor {
        @NotNull
        private final String placeHolder;

        MyFoldingDescriptor(@NotNull String placeHolder, @NotNull PsiElement psiElement, @NotNull TextRange range) {
            super(psiElement, range);
            this.placeHolder = placeHolder;
        }

        @Nullable
        @Override
        public String getPlaceholderText() {
            return this.placeHolder;
        }
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode astNode) {
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return true;
    }
}
