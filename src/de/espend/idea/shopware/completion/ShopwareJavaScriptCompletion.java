package de.espend.idea.shopware.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import de.espend.idea.shopware.util.ExtJsUtil;
import de.espend.idea.shopware.util.SnippetUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareJavaScriptCompletion extends CompletionContributor {
    public ShopwareJavaScriptCompletion() {

        // '{s name=<caret> namespace="foobar"}{/s}'
        extend(CompletionType.BASIC, getSnippetPattern(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
                PsiElement position = parameters.getOriginalPosition();
                if(!Symfony2ProjectComponent.isEnabled(position)) {
                    return;
                }

                PsiElement parent = position.getParent();
                if(!(parent instanceof JSLiteralExpression)) {
                    return;
                }

                Object value = ((JSLiteralExpression) parent).getValue();
                if(!(value instanceof String) || !((String) value).matches("^.*name=[']*([^=]*)$")) {
                    return;
                }

                String namespace = ExtJsUtil.getNamespaceFromStringLiteral(((JSLiteralExpression) parent));
                if(namespace == null) {
                    return;
                }

                String blockNamePrefix = resultSet.getPrefixMatcher().getPrefix();

                // prefix strip. we not only char after "="
                // "{s name='fo<caret>'"
                String prefix = "";
                int i = blockNamePrefix.lastIndexOf("=");
                if(i > 0) {
                    prefix = PsiElementUtils.trimQuote(blockNamePrefix.substring(i + 1));
                }

                CompletionResultSet myResultSet = resultSet.withPrefixMatcher(prefix);
                for (String s : SnippetUtil.getSnippetKeysByNamespace(position.getProject(), namespace)) {
                    myResultSet.addElement(LookupElementBuilder.create(s).withIcon(Symfony2Icons.TRANSLATION).withTypeText(namespace, true));
                }
            }
        });

        // '{s namespace=<caret>}{/s}'
        extend(CompletionType.BASIC, getSnippetPattern(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
                PsiElement position = parameters.getOriginalPosition();
                if(!Symfony2ProjectComponent.isEnabled(position)) {
                    return;
                }

                PsiElement parent = position.getParent();
                if(!(parent instanceof JSLiteralExpression)) {
                    return;
                }

                Object value = ((JSLiteralExpression) parent).getValue();
                if(!(value instanceof String) || !((String) value).matches("^.*namespace=[']*([^=]*)$")) {
                    return;
                }

                String blockNamePrefix = resultSet.getPrefixMatcher().getPrefix();

                // prefix strip. we not only char after "="
                // "{s name='fo<caret>'"
                String prefix = "";
                int i = blockNamePrefix.lastIndexOf("=");
                if(i > 0) {
                    prefix = PsiElementUtils.trimQuote(blockNamePrefix.substring(i + 1));
                }

                CompletionResultSet myResultSet = resultSet.withPrefixMatcher(prefix);
                for (String s : SnippetUtil.getSnippetNamespaces(position.getProject())) {
                    myResultSet.addElement(LookupElementBuilder.create(s).withIcon(Symfony2Icons.TRANSLATION));
                }
            }
        });

        // "//{namespace name=backend/update_wizard/translation}"
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
                PsiElement position = parameters.getOriginalPosition();
                if(!Symfony2ProjectComponent.isEnabled(position)) {
                    return;
                }

                String text = position.getText();

                int startOffset = position.getTextRange().getStartOffset();
                int offset = parameters.getOffset();

                // comment element stores full content around caret
                // extract prefix by caret position
                int endIndex = offset - startOffset;
                if(endIndex <= 0) {
                    return;
                }

                String substring = text.substring(0, endIndex);
                Matcher matcher = Pattern.compile("^[^\n]*\\{.*namespace.*name=[']*([^=]*)$").matcher(substring);
                if(!matcher.find()) {
                    return;
                }

                // prefix strip. we not only char after "="
                // "{s name='fo<caret>'"
                String prefix = StringUtils.trim(matcher.group(1));

                CompletionResultSet myResultSet = resultSet.withPrefixMatcher(prefix);
                for (String s : SnippetUtil.getSnippetNamespaces(position.getProject())) {
                    myResultSet.addElement(LookupElementBuilder.create(s).withIcon(Symfony2Icons.TRANSLATION));
                }
            }
        });
    }

    /**
     * String must start with "{s"
     */
    private PsiElementPattern.Capture<PsiElement> getSnippetPattern() {
        return PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(JSLiteralExpression.class)
                .with(new PatternCondition<JSLiteralExpression>("Snippet Start") {
                    @Override
                    public boolean accepts(@NotNull JSLiteralExpression jsLiteralExpression, ProcessingContext processingContext) {
                        Object value = jsLiteralExpression.getValue();
                        return value instanceof String && (((String) value).startsWith("{s"));
                    }
                }));
    }
}
