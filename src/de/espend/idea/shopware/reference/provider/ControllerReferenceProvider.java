package de.espend.idea.shopware.reference.provider;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.resolve.PhpResolveResult;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.util.ShopwareUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ControllerReferenceProvider extends PsiPolyVariantReferenceBase<PsiElement> {

    final private String content;
    final private String moduleName;

    public ControllerReferenceProvider(StringLiteralExpression stringLiteralExpression, @Nullable String moduleName) {
        super(stringLiteralExpression);
        this.content = stringLiteralExpression.getContents();
        this.moduleName = moduleName != null ? moduleName : "frontend";
    }


    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        final List<PsiElement> targets = new ArrayList<>();

        ShopwareUtil.collectControllerClass(getElement().getProject(), (phpClass, moduleName1, controllerName) -> {
            if (controllerName.equalsIgnoreCase(content)) {
                targets.add(phpClass);
            }
        }, moduleName);

        return PhpResolveResult.create(targets);
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        final List<LookupElement> lookupElements = new ArrayList<>();

        ShopwareUtil.collectControllerClass(getElement().getProject(), (phpClass, moduleName1, controllerName) -> lookupElements.add(LookupElementBuilder.create(ShopwareUtil.toCamelCase(controllerName, true))
            .withIcon(ShopwarePluginIcons.SHOPWARE)
            .withTypeText(phpClass.getPresentableFQN(), true)
        ), moduleName);

        return lookupElements.toArray();
    }
}

