package de.espend.idea.shopware.util;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.shopware.navigation.SmartyTemplateLineMarkerProvider;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopwareUtil {

    public static void writeShopwareMagicFile(String outputString, String outputPath) {

        File file = new File(outputPath);

        // create .idea folder, should not occur
        File folder = new File(file.getParent());
        if(!folder.exists() && !folder.mkdir()) {
            return;
        }

        FileWriter fw;
        try {
            fw = new FileWriter(file);
            fw.write(outputString);
            fw.close();
        } catch (IOException ignored) {
        }

    }

    public static void collectControllerClass(Project project, ControllerClassVisitor controllerClassVisitor) {

        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<PhpClass> phpClasses = phpIndex.getAllSubclasses("\\Enlight_Controller_Action");

        Pattern pattern = Pattern.compile(".*_(Frontend|Backend|Core)_(\\w+)");

        for (PhpClass phpClass : phpClasses) {

            String className = phpClass.getName();
            Matcher matcher = pattern.matcher(className);

            if(matcher.find()) {
                String moduleName = matcher.group(1);
                String controller = matcher.group(2);
                controllerClassVisitor.visitClass(phpClass, moduleName, controller);
            }

        }

    }

    public interface ControllerClassVisitor {
        public void visitClass(PhpClass phpClass, String moduleName, String controllerName);
    }

    public static void collectControllerActionSmartyWrapper(PsiElement psiElement, ControllerActionVisitor visitor) {

        Pattern pattern = Pattern.compile("controller=[\"|']*(\\w+)[\"|']*");
        Matcher matcher = pattern.matcher(psiElement.getParent().getText());
        if(!matcher.find()) {
            return;
        }

        String controllerName = SmartyTemplateLineMarkerProvider.toCamelCase(matcher.group(1), false);
        collectControllerAction(psiElement.getProject(), controllerName, visitor);
    }

    public static void collectControllerAction(Project project, String controllerName, ControllerActionVisitor visitor) {

        for(String moduleName: new String[] {"Frontend", "Backend", "Core"}) {
            PhpClass phpClass = PhpElementsUtil.getClass(project, String.format("Shopware_Controllers_%s_%s", moduleName, controllerName));
            if(phpClass != null) {
                for(Method method: phpClass.getMethods()) {
                    if(method.getAccess().isPublic() && method.getName().endsWith("Action")) {
                        visitor.visitMethod(method, method.getName().substring(0, method.getName().length() - 6), moduleName, controllerName);
                    }
                }
            }
        }

    }

    public interface ControllerActionVisitor {
        public void visitMethod(Method method, String methodStripped, String moduleName, String controllerName);
    }

}
