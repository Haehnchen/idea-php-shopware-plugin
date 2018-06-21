package de.espend.idea.shopware.util.dict;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareSnippet {
    @NotNull
    private final PsiElement psiElement;

    @NotNull
    private final String namespace;

    @NotNull
    private final String name;

    public ShopwareSnippet(@NotNull PsiElement psiElement, @NotNull String namespace, @NotNull String name) {
        this.psiElement = psiElement;
        this.namespace = namespace;
        this.name = name;
    }

    @NotNull
    public PsiElement getPsiElement() {
        return psiElement;
    }

    @NotNull
    public String getNamespace() {
        return namespace;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
