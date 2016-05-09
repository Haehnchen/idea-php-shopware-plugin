package de.espend.idea.shopware.reference.provider;

import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.completion.SmartyFileCompletionProvider;
import de.espend.idea.shopware.util.TemplateUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SmartyTemplateProvider extends PsiPolyVariantReferenceBase<PsiElement> {

    final private String valueName;
    private String[] extensions = new String[] {"tpl", "js"};

    public SmartyTemplateProvider(@NotNull StringLiteralExpression element) {
        super(element);
        this.valueName = element.getContents();
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {

        final List<ResolveResult> results = new ArrayList<>();


        TemplateUtil.collectFiles(getElement().getProject(), (virtualFile, fileName) -> {

            if (!fileName.equals(valueName)) {
                return;
            }

            PsiFile psiFile = PsiManager.getInstance(getElement().getProject()).findFile(virtualFile);
            if (psiFile != null) {
                results.add(new PsiElementResolveResult(psiFile));
            }
        }, extensions);

        return results.toArray(new ResolveResult[results.size()]);

    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return SmartyFileCompletionProvider.getTemplateCompletion(getElement().getProject(), extensions).toArray();
    }
}
