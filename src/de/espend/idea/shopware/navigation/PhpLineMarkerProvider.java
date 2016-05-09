package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.HookSubscriberUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhpLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, final @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {

        if(psiElements.size() == 0 || !ShopwareProjectComponent.isValidForProject(psiElements.get(0))) {
            return;
        }


        PsiFile containingFile = psiElements.get(0).getContainingFile();
        if(!containingFile.getName().contains("Bootstrap")) {
            return;
        }



        final Map<String, Method> methods = new HashMap<>();

        // we dont want multiple line markers, so wrap all into one here
        final Map<String, Set<PsiElement>> methodTargets = new HashMap<>();

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

                lineMarkerInfos.add(builder.createLineMarkerInfo(methods.get(method.getKey())));
            }
        }

    }
}
