package de.espend.idea.shopware.reference;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.HookSubscriberUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class LazySubscriberReferenceProvider extends CompletionContributor implements GotoDeclarationHandler {

    public LazySubscriberReferenceProvider() {

        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
                PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(
                    PlatformPatterns.psiElement(ParameterList.class)
                )
            ),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition == null || !ShopwareProjectComponent.isValidForProject(originalPosition)) {
                        return;
                    }

                    MethodMatcher.MethodMatchParameter match = new MethodMatcher.StringParameterMatcher(originalPosition.getContext(), 0)
                        .withSignature("\\Shopware_Components_Plugin_Bootstrap", "subscribeEvent")
                        .match();

                    if(match == null) {
                        return;
                    }

                    HookSubscriberUtil.collectHooks(originalPosition.getProject(), new HookSubscriberUtil.HookVisitor() {
                        @Override
                        public boolean visitHook(PhpClass phpClass, Method method) {
                            for (String hookName : new String[]{"after", "before", "replace"}) {
                                result.addElement(LookupElementBuilder.create(String.format("%s:%s:%s", phpClass.getPresentableFQN(), method.getName(), hookName)).withIcon(PhpIcons.METHOD_ICON).withTypeText("Hook", true));
                            }

                            return true;
                        }
                    });


                    HookSubscriberUtil.collectDoctrineLifecycleHooks(originalPosition.getProject(), new HookSubscriberUtil.DoctrineLifecycleHooksVisitor() {
                        @Override
                        public boolean visitLifecycleHooks(PhpClass phpClass) {

                            for (String lifecycleName : new String[]{"prePersist", "postPersist", "preUpdate", "postUpdate", "preRemove", "postRemove"}) {
                                result.addElement(LookupElementBuilder.create(String.format("%s::%s", phpClass.getPresentableFQN(), lifecycleName)).withIcon(Symfony2Icons.DOCTRINE).withTypeText("Doctrine", true));
                            }

                            return true;
                        }
                    });


                }
            }
        );


    }

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {

        if(psiElement == null || !(psiElement.getContext() instanceof StringLiteralExpression)) {
            return new PsiElement[0];
        }

        MethodMatcher.MethodMatchParameter match = new MethodMatcher.StringParameterMatcher(psiElement.getContext(), 0)
            .withSignature("\\Shopware_Components_Plugin_Bootstrap", "subscribeEvent")
            .match();

        if(match == null) {
            return new PsiElement[0];
        }

        final String hookNameContent = ((StringLiteralExpression) psiElement.getContext()).getContents();
        if(!hookNameContent.contains(":")) {
            return new PsiElement[0];
        }

        final String hookNamePreFilter = hookNameContent.substring(0, hookNameContent.indexOf(":"));

        final Collection<PsiElement> psiElements = new ArrayList<PsiElement>();
        HookSubscriberUtil.collectHooks(psiElement.getProject(), new HookSubscriberUtil.HookVisitor() {
            @Override
            public boolean visitHook(PhpClass phpClass, Method method) {
                if (!isEqualHookClass(phpClass, hookNamePreFilter)) return true;

                for (String hookName : new String[]{"after", "before", "replace"}) {
                    if (String.format("%s:%s:%s", phpClass.getPresentableFQN(), method.getName(), hookName).equals(hookNameContent)) {
                        psiElements.add(method);
                        return false;
                    }
                }

                return true;
            }
        });

        HookSubscriberUtil.collectDoctrineLifecycleHooks(psiElement.getProject(), new HookSubscriberUtil.DoctrineLifecycleHooksVisitor() {
            @Override
            public boolean visitLifecycleHooks(PhpClass phpClass) {
                if (!isEqualHookClass(phpClass, hookNamePreFilter)) return true;

                for (String lifecycleName : new String[]{"prePersist", "postPersist", "preUpdate", "postUpdate", "preRemove", "postRemove"}) {
                    if (String.format("%s::%s", phpClass.getPresentableFQN(), lifecycleName).equals(hookNameContent)) {
                        psiElements.add(phpClass);
                        return false;
                    }
                }

                return true;
            }
        });

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    private boolean isEqualHookClass(PhpClass phpClass, String hookNamePreFilter) {
        String presentableFQN = phpClass.getPresentableFQN();
        return !(presentableFQN == null || !presentableFQN.startsWith(hookNamePreFilter));
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }

}
