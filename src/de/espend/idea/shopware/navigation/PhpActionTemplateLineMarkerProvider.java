package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.smarty.SmartyFile;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.TemplateUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhpActionTemplateLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {

        for(PsiElement psiElement: psiElements) {
            if(psiElement instanceof Method) {
                attachController((Method) psiElement, lineMarkerInfos);
            }
        }
    }

    public void attachController(Method method, Collection<LineMarkerInfo> lineMarkerInfos) {

        if(!ShopwareProjectComponent.isValidForProject(method)) {
            return;
        }

        String methodName = method.getName();
        if(!methodName.endsWith("Action")) {
            return;
        }

        PhpClass phpClass = method.getContainingClass();
        if(phpClass == null) {
            return;
        }

        String name = phpClass.getName();
        Pattern pattern = Pattern.compile(".*_(Frontend|Backend|Core)_(\\w+)");
        Matcher matcher = pattern.matcher(name);

        if(!matcher.find()) {
            return;
        }

        // Shopware_Controllers_Frontend_Account
        String moduleName = underscore(matcher.group(1));
        String controller = underscore(matcher.group(2));

        // ajaxLoginAction > ajax_login
        String methodNameNormalize = underscore(methodName.substring(0, methodName.length() - 6));

        final String templateName = String.format("%s/%s/%s.tpl", moduleName, controller, methodNameNormalize);
        final Project project = method.getProject();
        final List<PsiFile> psiFiles = new ArrayList<PsiFile>();

        TemplateUtil.collectFiles(project, new TemplateUtil.SmartyTemplateVisitor() {
            @Override
            public void visitFile(VirtualFile virtualFile, String fileName) {

                if (!fileName.equals(templateName)) {
                    return;
                }

                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile != null) {
                    psiFiles.add(psiFile);
                }
            }
        });

        if(psiFiles.size() == 0) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(ShopwarePluginIcons.SHOPWARE_LINEMARKER).
            setTargets(psiFiles).
            setTooltipText("Navigate to template");

        lineMarkerInfos.add(builder.createLineMarkerInfo(method));


    }

    public static String underscore(String str){
        return StringUtils.capitalize(str).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }


}
