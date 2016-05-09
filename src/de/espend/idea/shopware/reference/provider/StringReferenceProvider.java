package de.espend.idea.shopware.reference.provider;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.util.ShopwareUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class StringReferenceProvider extends PsiPolyVariantReferenceBase<PsiElement> {

    final private String[] values;

    public StringReferenceProvider(StringLiteralExpression stringLiteralExpression, String... values) {
        super(stringLiteralExpression);
        this.values = values;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        return new ResolveResult[0];
    }

    @NotNull
    @Override
    public Object[] getVariants() {

        final List<LookupElement> lookupElements = new ArrayList<>();

        for(String value: values) {
            lookupElements.add(LookupElementBuilder.create(ShopwareUtil.toCamelCase(value, true))
                .withIcon(ShopwarePluginIcons.SHOPWARE)
            );
        }

        return lookupElements.toArray();
    }
}

