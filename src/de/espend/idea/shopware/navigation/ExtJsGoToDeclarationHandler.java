package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ExtJsUtil;
import de.espend.idea.shopware.util.SnippetUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ExtJsGoToDeclarationHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {

        if(!ShopwareProjectComponent.isValidForProject(sourceElement)) {
            return new PsiElement[0];
        }

        final List<PsiElement> targets = new ArrayList<>();

        if(ExtJsUtil.getStringLiteralPattern().accepts(sourceElement)) {
            // {link file='frontend/_resources/styles/framework.css'}
            attachControllerActionNameGoto(sourceElement, targets);

            // {s name='foobar' namespace='foobar/ns'}
            attachSnippets(sourceElement, targets);
        }

        if(PlatformPatterns.psiElement(PsiComment.class).accepts(sourceElement)) {
            attachSnippetAsComment(sourceElement, targets);
        };

        return targets.toArray(new PsiElement[targets.size()]);
    }

    private void attachSnippetAsComment(@NotNull PsiElement psiElement, @NotNull List<PsiElement> targets) {
        if(!(psiElement instanceof PsiComment)) {
            return;
        }

        String text = psiElement.getText();
        Matcher matcher = Pattern.compile("\\{.*namespace.*name=['|\"]*([^=]*)['|\"]*[\\s]*.*}").matcher(text);
        if(!matcher.find()) {
            return;
        }

        String namespace = StringUtils.trim(matcher.group(1));
        if(StringUtils.isBlank(namespace)) {
            return;
        }

        targets.addAll(SnippetUtil.getSnippetNamespaceTargets(psiElement.getProject(), namespace));
    }

    private void attachSnippets(@NotNull PsiElement sourceElement, @NotNull List<PsiElement> targets) {
        PsiElement parent = sourceElement.getParent();
        if(!(parent instanceof JSLiteralExpression)) {
            return;
        }

        Object value = ((JSLiteralExpression) parent).getValue();
        if(!(value instanceof String) || StringUtils.isBlank((String) value) || !((String) value).startsWith("{s")) {
            return;
        }

        String name = ExtJsUtil.getAttributeTagValueFromSmartyString("s", "name", (String) value);
        if(name == null) {
            return;
        }

        String namespace = ExtJsUtil.getNamespaceFromStringLiteral((JSLiteralExpression) parent);
        if(namespace == null) {
            return;
        }

        targets.addAll(SnippetUtil.getSnippetNameTargets(parent.getProject(), namespace, name));
    }

    private void attachControllerActionNameGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        String text = PsiElementUtils.trimQuote(sourceElement.getText());
        if(text.startsWith("{") && text.endsWith("}")) {
            psiElements.addAll(ExtJsUtil.getControllerTargets(sourceElement, text));
        }

    }


    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

}
