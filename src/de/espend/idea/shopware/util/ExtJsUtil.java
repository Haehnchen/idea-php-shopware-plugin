package de.espend.idea.shopware.util;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.impl.JSArgumentListImpl;
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtJsUtil {

    @RegExp
    public static String JS_NAMESPACE_PATTERN = "^[^\n]*\\{.*namespace.*name=['|\"]*([^'\"=]*)['|\"]*[\\s]*.*}\\s*$";

    public static PsiElementPattern.Capture<PsiElement> getStringLiteralPattern() {
        return PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement(JSLiteralExpressionImpl.class));
    }

    public static PsiElementPattern.Capture<PsiElement> getStringApp() {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(JSLiteralExpressionImpl.class).withParent(
                PlatformPatterns.psiElement(JSArgumentListImpl.class)
            )
        );
    }

    public static PsiElementPattern.Capture<PsiElement> getStringProperty() {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(JSLiteralExpressionImpl.class).withParent(
                PlatformPatterns.psiElement(JSPropertyImpl.class)
            )
        );
    }

    @Nullable
    public static String getControllerOnPath(PsiFile psiFile) {

        String relativeFilename = TemplateUtil.getTemplateName(psiFile.getProject(), psiFile.getVirtualFile(), "backend");
        if(relativeFilename == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(".*/backend/(\\w+)/");
        Matcher matcher = pattern.matcher(relativeFilename);

        if(!matcher.find()) {
            return null;
        }

        // Shopware_Controllers_Frontend_Account
        return ShopwareUtil.toCamelCase(matcher.group(1), false);
    }

    public static List<PsiElement> getControllerTargets(PsiElement sourceElement, String text) {

        final List<PsiElement> psiElements = new ArrayList<>();

        //{url controller="form" action="getFields"}
        Pattern pattern = Pattern.compile("controller=['|\"]*(\\w+)['|\"]*");
        Matcher matcher = pattern.matcher(text);

        String controller;
        if(!matcher.find()) {

            // if no controller, we use backend app context
            controller = ExtJsUtil.getControllerOnPath(sourceElement.getContainingFile());
            if(controller == null) {
                return psiElements;
            }
        } else {
            controller = matcher.group(1);
        }

        final String jsControllerName = ShopwareUtil.toCamelCase(controller, false);

        //{url controller="form" action="getFields"}
        pattern = Pattern.compile("action=['|\"]*(\\w+)['|\"]*");
        matcher = pattern.matcher(text);
        if(!matcher.find()) {
            return psiElements;
        }

        final String jsActionName = ShopwareUtil.toCamelCase(matcher.group(1), false);

        ShopwareUtil.collectControllerAction(sourceElement.getProject(), jsControllerName, (method, methodStripped, moduleName, controllerName) -> {
            if (jsActionName.equalsIgnoreCase(methodStripped)) {
                psiElements.add(method);
            }
        }, "backend");

        return psiElements;

    }

    /**
     * {namespace name=swag-last-registrations/date}
     */
    @Nullable
    public static String getSnippetNamespaceFromFile(@NotNull PsiFile psiFile) {
        Pattern pattern = Pattern.compile("\\{namespace[^}]*name\\s*=\\s*['|\"]*([^'\\s\"}]*)['|\"]*\\s*");

        for (PsiElement psiElement : psiFile.getChildren()) {
            if(!(psiElement instanceof PsiComment)) {
                continue;
            }

            String text = psiElement.getText();

            // name="foo", name='foo', name=foo
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String group = matcher.group(1);
                if(StringUtils.isBlank(group)) {
                    return null;
                }

                return group;
            }
        }

        return null;
    }

    /**
     * {s name=swag-last-registrations/date}
     */
    @Nullable
    public static String getAttributeTagValueFromSmartyString(@NotNull String tag, @NotNull String attribute, @NotNull String contents) {
        Pattern pattern = Pattern.compile("\\{" + tag + "[^}]*" + attribute + "\\s*=\\s*['|\"]*([^'\\s\"}]*)['|\"]*\\s*");
        Matcher matcher = pattern.matcher(contents);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * {header: '{s name=swag-last-registrations/date}{/s}'}
     */
    @Nullable
    public static String getNamespaceFromStringLiteral(@NotNull JSLiteralExpression element) {
        Object contents = element.getValue();
        if(!(contents instanceof String) || StringUtils.isBlank((String) contents)) {
            return null;
        }

        String namespace = getAttributeTagValueFromSmartyString("s", "namespace", (String) contents);
        if(namespace != null) {
            return namespace;
        }

        return getSnippetNamespaceFromFile(element.getContainingFile());
    }
}
