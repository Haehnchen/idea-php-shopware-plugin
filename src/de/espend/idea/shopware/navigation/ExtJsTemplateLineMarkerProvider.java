package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ExtJsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ExtJsTemplateLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {

        for(PsiElement psiElement: psiElements) {

            if(ExtJsUtil.getStringApp().accepts(psiElement)) {
                attachDefineTargets(psiElement, lineMarkerInfos);
            }

            if(ExtJsUtil.getStringLiteralPattern().accepts(psiElement)) {
                attachControllerAction(psiElement, lineMarkerInfos);
            }

        }
    }

    private void attachDefineTargets(PsiElement psiElement, Collection<LineMarkerInfo> lineMarkerInfos) {

        if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
            return;
        }

        String text = PsiElementUtils.trimQuote(psiElement.getText());
        if(!text.startsWith("Shopware.apps.")) {
            return;
        }

        String[] namespaces = StringUtils.split(text, ".");
        if(namespaces.length < 3) {
            return;
        }

        List<PsiElement> psiElementList = new ArrayList<PsiElement>();

        attachController(psiElement.getProject(), namespaces, psiElementList);
        attachModels(psiElement.getProject(), namespaces, psiElementList);

        if(psiElementList.size() == 0) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(ShopwarePluginIcons.SHOPWARE_LINEMARKER).
            setTargets(psiElementList).
            setTooltipText("Navigate");

        lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement));

    }

    private void attachModels(Project project, String[] namespaces, List<PsiElement> targets) {

        if(namespaces.length < 5) {
            return;
        }

        // only show on controller context
        if(!"model".equalsIgnoreCase(namespaces[3])) {
            return;
        }

        String classNameCore = String.format("Shopware\\Models\\%s\\%s", namespaces[2], namespaces[4]);

        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Model\\ModelEntity")) {

            String className = phpClass.getPresentableFQN();

            if(className != null && className.equalsIgnoreCase(classNameCore)) {
                targets.add(phpClass);
            }

        }

    }

    private void attachController(Project project, String[] namespaces, List<PsiElement> targets) {

        if(namespaces.length < 4) {
            return;
        }

        // only show on some context
        if(!Arrays.asList("controller", "store", "model", "view").contains(namespaces[3])) {
            return;
        }

        String controller = namespaces[2];

        // build class name
        String className = String.format("\\Shopware_Controllers_%s_%s", "Backend", controller);
        PhpClass phpClass = PhpElementsUtil.getClassInterface(project, className);
        if(phpClass != null) {
            targets.add(phpClass);
        }

    }

    private void attachControllerAction(PsiElement sourceElement, Collection<LineMarkerInfo> lineMarkerInfos) {

        if(!ShopwareProjectComponent.isValidForProject(sourceElement)) {
            return;
        }

        String text = PsiElementUtils.trimQuote(sourceElement.getText());
        if(text.startsWith("{") && text.endsWith("}")) {

            List<PsiElement> controllerTargets = ExtJsUtil.getControllerTargets(sourceElement, text);
            if(controllerTargets.size() > 0) {
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(ShopwarePluginIcons.SHOPWARE_LINEMARKER).
                    setTargets(controllerTargets).
                    setTooltipText("Navigate to action");

                lineMarkerInfos.add(builder.createLineMarkerInfo(sourceElement));
            }
        }

    }

}
