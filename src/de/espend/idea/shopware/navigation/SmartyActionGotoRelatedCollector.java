package de.espend.idea.shopware.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.TemplateUtil;
import fr.adrienbrault.idea.symfony2plugin.extension.ControllerActionGotoRelatedCollector;
import fr.adrienbrault.idea.symfony2plugin.extension.ControllerActionGotoRelatedCollectorParameter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.adrienbrault.idea.symfony2plugin.dic.RelatedPopupGotoLineMarker.PopupGotoRelatedItem;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SmartyActionGotoRelatedCollector implements ControllerActionGotoRelatedCollector {
    private static String underscore(String str){
        return StringUtils.capitalize(str).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @Override
    public void collectGotoRelatedItems(ControllerActionGotoRelatedCollectorParameter parameter) {
        if(!ShopwareProjectComponent.isValidForProject(parameter.getProject())) {
            return;
        }

        PhpClass phpClass = parameter.getMethod().getContainingClass();
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

        String methodName = parameter.getMethod().getName();

        // ajaxLoginAction > ajax_login
        String methodNameNormalize = underscore(methodName.substring(0, methodName.length() - 6));

        final String templateName = String.format("%s/%s/%s.tpl", moduleName, controller, methodNameNormalize);
        final Project project = parameter.getProject();
        final List<PsiFile> psiFiles = new ArrayList<>();

        TemplateUtil.collectFiles(project, (virtualFile, fileName) -> {

            if (!fileName.equals(templateName)) {
                return;
            }

            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile != null) {
                psiFiles.add(psiFile);
            }
        });

        if(psiFiles.size() == 0) {
            return;
        }

        for(PsiFile psiFile: psiFiles) {
            parameter.add(new PopupGotoRelatedItem(psiFile, psiFile.getVirtualFile().getUrl()).withIcon(ShopwarePluginIcons.SHOPWARE, ShopwarePluginIcons.SHOPWARE_LINEMARKER));
        }

    }

}
