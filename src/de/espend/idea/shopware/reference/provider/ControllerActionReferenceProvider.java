package de.espend.idea.shopware.reference.provider;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.resolve.PhpResolveResult;
import de.espend.idea.shopware.util.ShopwareUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ControllerActionReferenceProvider extends PsiPolyVariantReferenceBase<PsiElement>  {

    final private String moduleName;
    final private String content;
    final private String controllerName;

    public ControllerActionReferenceProvider(StringLiteralExpression stringLiteralExpression, String controllerName, @Nullable String moduleName) {
        super(stringLiteralExpression);
        this.content = stringLiteralExpression.getContents();
        this.controllerName = controllerName;
        this.moduleName = moduleName != null ? moduleName : "frontend";
    }


    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        final List<PsiElement> targets = new ArrayList<>();

        ShopwareUtil.collectControllerAction(getElement().getProject(), this.controllerName, (method, methodStripped, moduleName1, controllerName1) -> {
            if (methodStripped.equalsIgnoreCase(content)) {
                targets.add(method);
            }
        }, moduleName);

        return PhpResolveResult.createResults(targets);
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        final List<LookupElement> lookupElements = new ArrayList<>();

        ShopwareUtil.collectControllerAction(getElement().getProject(), this.controllerName, (method, methodStripped, moduleName1, controllerName1) -> lookupElements.add(attachTypeText(LookupElementBuilder.create(methodStripped).withIcon(method.getIcon()), method)), moduleName);

        return lookupElements.toArray();
    }

    private LookupElementBuilder attachTypeText(LookupElementBuilder builder, Method method) {
        PhpClass phpClass = method.getContainingClass();
        if(phpClass == null) {
            return builder;
        }

        return builder.withTypeText(phpClass.getPresentableFQN(), true);
    }

}

