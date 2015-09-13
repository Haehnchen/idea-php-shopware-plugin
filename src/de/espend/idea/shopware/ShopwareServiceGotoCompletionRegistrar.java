package de.espend.idea.shopware;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.index.InitResourceServiceIndex;
import de.espend.idea.shopware.index.SmartyBlockStubIndex;
import de.espend.idea.shopware.util.ShopwareUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.stubs.SymfonyProcessors;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareServiceGotoCompletionRegistrar implements GotoCompletionRegistrar {
    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@NotNull PsiElement psiElement) {

                PsiElement context = psiElement.getContext();
                if (!(context instanceof StringLiteralExpression)) {
                    return null;
                }

                MethodMatcher.MethodMatchParameter match = new MethodMatcher.StringParameterRecursiveMatcher(context, 0)
                        .withSignature("\\Symfony\\Component\\DependencyInjection\\ContainerInterface", "get")
                        .match();

                if(match == null) {
                    return null;
                }

                return new MyGotoCompletionProvider(psiElement);
            }

        });


    }

    private static class MyGotoCompletionProvider extends GotoCompletionProvider {

        public MyGotoCompletionProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            SymfonyProcessors.CollectProjectUniqueKeys uniqueKeys = new SymfonyProcessors.CollectProjectUniqueKeys(getProject(), InitResourceServiceIndex.KEY);
            FileBasedIndex.getInstance().processAllKeys(InitResourceServiceIndex.KEY, uniqueKeys, getProject());

            Collection<LookupElement> lookupElements = new ArrayList<LookupElement>();

            for (String s : uniqueKeys.getResult()) {
                lookupElements.add(LookupElementBuilder.create(s).withIcon(Symfony2Icons.SERVICE).withTypeText("InitResource", true));
            }

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(PsiElement psiElement) {

            PsiElement parent = psiElement.getParent();
            if(!(parent instanceof StringLiteralExpression)) {
                return Collections.emptyList();
            }

            String contents = ((StringLiteralExpression) parent).getContents();
            if(StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }

            Collection<PsiElement> methods = new ArrayList<PsiElement>();
            for(Method method : ShopwareUtil.getInitResourceServiceClass(getProject(), contents)) {
                methods.add(method);
            }

            return methods;
        }
    }

}
