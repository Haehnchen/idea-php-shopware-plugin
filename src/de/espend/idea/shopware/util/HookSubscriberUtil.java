package de.espend.idea.shopware.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.*;

public class HookSubscriberUtil {

    final private static Set<String> CORE_CLASSES = new HashSet<String>() {{
        addAll(Arrays.asList("sCms", "sCore", "sAdmin", "sOrder", "sBasket", "sExport", "sSystem", "sArticles", "sMarketing", "sCategories", "sCategories", "sNewsletter", "sConfigurator", "sRewriteTable"));
    }};

    public static void collectHooks(Project project, HookVisitor hookVisitor) {

        Collection<PhpClass> phpClasses = new ArrayList<PhpClass>();

        // directly use core classes
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        for(String coreClass: CORE_CLASSES) {
            phpClasses.addAll(phpIndex.getClassesByName(coreClass));
        }

        // fallback: search on directory
        VirtualFile virtualFile = VfsUtil.findRelativeFile(project.getBaseDir(), "engine", "core", "class");
        if(virtualFile != null) {
            for(VirtualFile coreClassFile: VfsUtil.getChildren(virtualFile)) {
                String name = coreClassFile.getName();
                if(name.contains(".")) name = name.substring(0, name.lastIndexOf('.'));
                if(!CORE_CLASSES.contains(name)) {
                    phpClasses.addAll(phpIndex.getClassesByName(name));
                }
            }
        }

        phpClasses.addAll(phpIndex.getAllSubclasses("\\Enlight_Hook"));

        for(PhpClass phpClass: phpClasses) {
            for(Method method: phpClass.getMethods()) {
                if(!method.isStatic() && !method.isAbstract() && !method.getName().startsWith("_")) {
                    boolean returnValue = hookVisitor.visitHook(phpClass, method);
                    if(!returnValue) {
                        return;
                    }
                }
            }
        }

    }

    public static void collectDoctrineLifecycleHooks(Project project, DoctrineLifecycleHooksVisitor hookVisitor) {
        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Model\\ModelEntity")) {
            hookVisitor.visitLifecycleHooks(phpClass);
        }
    }

    public static interface DoctrineLifecycleHooksVisitor {
        public boolean visitLifecycleHooks(PhpClass phpClass);
    }

    public static interface HookVisitor {
        public boolean visitHook(PhpClass phpClass, Method method);
    }

}
