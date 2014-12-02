package de.espend.idea.shopware.reference;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.HookSubscriberUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.Symfony2InterfacesUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class LazySubscriberReferenceProvider extends CompletionContributor implements GotoDeclarationHandler {

    public static final List<String> DOCTRINE_LIFECYCLES = Arrays.asList("prePersist", "postPersist", "preUpdate", "postUpdate", "preRemove", "postRemove");
    private static List<String> HOOK_EVENTS = Arrays.asList("after", "before", "replace");

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
                                result.addElement(LookupElementBuilder.create(String.format("%s::%s::%s", phpClass.getPresentableFQN(), method.getName(), hookName)).withIcon(PhpIcons.METHOD_ICON).withTypeText("Hook", true));
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


                    final Symfony2InterfacesUtil symfony2InterfacesUtil = new Symfony2InterfacesUtil();

                    HookSubscriberUtil.visitDoctrineQueryBuilderClasses(originalPosition.getProject(), new Processor<PhpClass>() {
                        @Override
                        public boolean process(PhpClass phpClass) {

                            for(Method method: phpClass.getOwnMethods()) {

                                String presentableFQN = phpClass.getPresentableFQN();
                                if(presentableFQN == null || (presentableFQN.endsWith("Proxy") || symfony2InterfacesUtil.isInstanceOf(phpClass, "\\Enlight_Hook_Proxy"))) {
                                    continue;
                                }

                                if(method.getAccess().isPublic() || method.getAccess().isProtected()) {
                                    String name = method.getName();
                                    if(!name.startsWith("__")) {
                                        for (String hookName : new String[]{"after", "before", "replace"}) {
                                            result.addElement(LookupElementBuilder.create(String.format("%s::%s::%s", presentableFQN, name, hookName)).withIcon(Symfony2Icons.DOCTRINE).withTypeText("QueryBuilder", true));
                                        }
                                    }
                                }
                            }

                            return true;
                        }
                    });


                }
            }
        );

        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(
                PlatformPatterns.psiElement(ParameterList.class)
            )
        ),
            new CompletionProvider<CompletionParameters>() {

                private String toCamelCase(String value, boolean startWithLowerCase) {
                    String[] strings = org.apache.commons.lang.StringUtils.split(value.toLowerCase(), "_");
                    for (int i = startWithLowerCase ? 1 : 0; i < strings.length; i++){
                        strings[i] = org.apache.commons.lang.StringUtils.capitalize(strings[i]);
                    }
                    return org.apache.commons.lang.StringUtils.join(strings);
                }

                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition == null || !ShopwareProjectComponent.isValidForProject(originalPosition)) {
                        return;
                    }

                    MethodMatcher.MethodMatchParameter match = new MethodMatcher.StringParameterMatcher(originalPosition.getContext(), 1)
                        .withSignature("\\Shopware_Components_Plugin_Bootstrap", "subscribeEvent")
                        .match();

                    if(match == null) {
                        return;
                    }

                    PsiElement[] psiElements = match.getMethodReference().getParameters();
                    if(psiElements.length < 2 || !(psiElements[0] instanceof StringLiteralExpression)) {
                        return;
                    }

                    String contents = toCamelCase(((StringLiteralExpression) psiElements[0]).getContents().replaceAll("[:]+", "_"), false);
                    String content = contents.replace("_", "");

                    Set<String> stringSet = new HashSet<String>();
                    stringSet.add((content));

                    if(contents.startsWith("Enlight")) {
                        stringSet.add(contents.substring("Enlight".length()));
                    }

                    for(String value: stringSet) {
                        result.addElement(LookupElementBuilder.create("on" + value).withIcon(ShopwarePluginIcons.SHOPWARE));
                    }

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

        return getHookTargets(psiElement.getProject(), hookNameContent);
    }

    @NotNull
    public static PsiElement[] getHookTargets(@NotNull Project project, @NotNull final String hookNameContent) {

        if(!hookNameContent.contains(":")) {
            return new PsiElement[0];
        }

        final Collection<PsiElement> psiElements = new ArrayList<PsiElement>();

        String[] parts = hookNameContent.split("::");

        // Enlight_Controller_Action::dispatch::replace
        if(parts.length == 3) {
            if(HOOK_EVENTS.contains(parts[2])) {
                Method method = PhpElementsUtil.getClassMethod(project, parts[0], parts[1]);
                if(method != null) {
                    psiElements.add(method);
                }
            }
        }

        // Shopware\Models\Attribute\Order::postRemove
        if(parts.length == 2) {
            if(DOCTRINE_LIFECYCLES.contains(parts[1])) {
                PhpClass phpClass = PhpElementsUtil.getClass(project, parts[0]);
                if(phpClass != null) {
                    psiElements.add(phpClass);
                }
            }
        }

        // Enlight_Controller_Action_PostDispatchSecure_Frontend_Payment
        Pattern pattern = Pattern.compile("Enlight_Controller_Action_\\w+_(Frontend|Backend|Core|Widgets)_(\\w+)");
        Matcher matcher = pattern.matcher(hookNameContent);

        if(matcher.find()) {
            PhpClass phpClass = PhpElementsUtil.getClass(project, String.format("Shopware_Controllers_%s_%s", matcher.group(1), matcher.group(2)));
            if(phpClass != null) {
                psiElements.add(phpClass);
            }
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    private static boolean isEqualHookClass(PhpClass phpClass, String hookNamePreFilter) {
        String presentableFQN = phpClass.getPresentableFQN();
        return !(presentableFQN == null || !presentableFQN.startsWith(hookNamePreFilter));
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }

}
