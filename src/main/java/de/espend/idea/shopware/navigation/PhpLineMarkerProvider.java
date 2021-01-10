package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.HookSubscriberUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpLineMarkerProvider implements LineMarkerProvider {
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> psiElements, @NotNull Collection<? super LineMarkerInfo<?>> lineMarkerInfos) {
        if(psiElements.size() == 0 || !ShopwareProjectComponent.isValidForProject(psiElements.get(0))) {
            return;
        }

        PsiFile containingFile = psiElements.get(0).getContainingFile();
        if(containingFile.getName().contains("Bootstrap")) {
            collectBootstrapSubscriber(psiElements, lineMarkerInfos, containingFile);
            return;
        }

        // Enlight\Event\SubscriberInterface::getSubscribedEvents
        collectSubscriberTargets(psiElements, lineMarkerInfos);
    }

    private void collectBootstrapSubscriber(@NotNull List<? extends PsiElement> psiElements, @NotNull Collection<? super LineMarkerInfo<?>> lineMarkerInfos, PsiFile containingFile) {
        Map<String, Method> methods = new HashMap<>();

        // we dont want multiple line markers, so wrap all into one here
        Map<String, Set<PsiElement>> methodTargets = new HashMap<>();

        for(PsiElement psiElement: psiElements) {
            if(psiElement instanceof Method && ((Method) psiElement).getModifier().isPublic()) {
                methods.put(((Method) psiElement).getName(), (Method) psiElement);
            }
        }

        if(methods.size() == 0) {
            return;
        }

        final Project project = containingFile.getProject();
        containingFile.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(!(element instanceof MethodReference)) {
                    super.visitElement(element);
                    return;
                }

                MethodReference methodReference = (MethodReference) element;
                String name = methodReference.getName();
                if(name != null && (name.equals("subscribeEvent") || name.equals("createEvent"))) {
                    PsiElement[] parameters = methodReference.getParameters();
                    if(parameters.length >= 2 && parameters[0] instanceof StringLiteralExpression && parameters[1] instanceof StringLiteralExpression) {
                        String subscriberName = ((StringLiteralExpression) parameters[0]).getContents();
                        String methodName = ((StringLiteralExpression) parameters[1]).getContents();

                        if(StringUtils.isNotBlank(subscriberName) && StringUtils.isNotBlank(methodName)) {

                            if(methods.containsKey(methodName)) {

                                if(!methodTargets.containsKey(methodName)) {
                                    methodTargets.put(methodName, new HashSet<>());
                                }

                                methodTargets.get(methodName).add(parameters[1]);
                                methodTargets.get(methodName).addAll(HookSubscriberUtil.getAllHookTargets(project, subscriberName));
                            }
                        }
                    }
                }

                super.visitElement(element);
            }
        });

        // we allow multiple events
        for(Map.Entry<String, Set<PsiElement>> method: methodTargets.entrySet()) {
            if(methods.containsKey(method.getKey())) {

                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(ShopwarePluginIcons.SHOPWARE_LINEMARKER).
                    setTargets(method.getValue()).
                    setTooltipText("Related Targets");

                Method psiMethod = methods.get(method.getKey());

                // attach linemarker to leaf item which is our function name for performance reasons
                ASTNode node = psiMethod.getNode().findChildByType(PhpTokenTypes.IDENTIFIER);
                if(node != null) {
                    lineMarkerInfos.add(builder.createLineMarkerInfo(node.getPsi()));
                }
            }
        }
    }

    private void collectSubscriberTargets(@NotNull List<? extends PsiElement> psiElements, final @NotNull Collection<? super LineMarkerInfo<?>> lineMarkerInfos) {
        Collection<PhpClass> phpClasses = new ArrayList<>();

        for (PsiElement psiElement : psiElements) {
            if(psiElement instanceof PhpClass && PhpElementsUtil.isInstanceOf((PhpClass) psiElement, "Enlight\\Event\\SubscriberInterface")) {
                phpClasses.add((PhpClass) psiElement);
            }
        }

        for (PhpClass phpClass : phpClasses) {
            Method getSubscribedEvents = phpClass.findOwnMethodByName("getSubscribedEvents");
            if(getSubscribedEvents == null) {
                continue;
            }

            Map<String, Pair<String, PsiElement>> methodEvent = new HashMap<>();

            HookSubscriberUtil.visitSubscriberEvents(getSubscribedEvents, (event, methodName, key) ->
                methodEvent.put(methodName, Pair.create(event, key))
            );

            for (Method method : phpClass.getOwnMethods()) {
                if(!methodEvent.containsKey(method.getName()) || !method.getAccess().isPublic()) {
                    continue;
                }

                Pair<String, PsiElement> result = methodEvent.get(method.getName());
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(ShopwarePluginIcons.SHOPWARE_LINEMARKER).
                    setTargets(new MyCollectionNotNullLazyValue(result.getSecond(), result.getFirst())).
                    setTooltipText("Related Targets");

                // attach linemarker to leaf item which is our function name for performance reasons
                ASTNode node = method.getNode().findChildByType(PhpTokenTypes.IDENTIFIER);
                if(node != null) {
                    lineMarkerInfos.add(builder.createLineMarkerInfo(node.getPsi()));
                }
            }
        }
    }

    private static class MyCollectionNotNullLazyValue extends NotNullLazyValue<Collection<? extends PsiElement>> {
        @NotNull
        private final PsiElement event;

        @NotNull
        private final String eventName;

        MyCollectionNotNullLazyValue(@NotNull PsiElement event, @NotNull String eventName) {
            this.event = event;
            this.eventName = eventName;
        }

        @NotNull
        @Override
        protected Collection<? extends PsiElement> compute() {
            Collection<PsiElement> targets = new HashSet<>();

            targets.add(event);
            targets.addAll(HookSubscriberUtil.getAllHookTargets(event.getProject(), eventName));

            return targets;
        }
    }
}
