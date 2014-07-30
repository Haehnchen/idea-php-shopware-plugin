package de.espend.idea.shopware.util;

import com.intellij.lang.javascript.psi.impl.JSArgumentListImpl;
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Method;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtJsUtil {

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

        final List<PsiElement> psiElements = new ArrayList<PsiElement>();

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

        ShopwareUtil.collectControllerAction(sourceElement.getProject(), jsControllerName, new ShopwareUtil.ControllerActionVisitor() {
            @Override
            public void visitMethod(Method method, String methodStripped, String moduleName, String controllerName) {
                if (jsActionName.equalsIgnoreCase(methodStripped)) {
                    psiElements.add(method);
                }
            }
        }, "backend");

        return psiElements;

    }

}
