package de.espend.idea.shopware.util;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

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
                controllerClassVisitor.visitFile(phpClass, moduleName, controller);
            }

        }

    }

    public interface ControllerClassVisitor {
        public void visitFile(PhpClass phpClass, String moduleName, String controllerName);
    }

}
