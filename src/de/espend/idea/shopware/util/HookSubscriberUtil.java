package de.espend.idea.shopware.util;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.containers.ConcurrentHashSet;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.shopware.reference.EventSubscriberReferenceContributor;
import de.espend.idea.shopware.reference.LazySubscriberReferenceProvider;
import fr.adrienbrault.idea.symfony2plugin.Symfony2InterfacesUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HookSubscriberUtil {

    public static Set<String> NOTIFY_EVENTS = new ConcurrentHashSet<String>();

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
        Symfony2InterfacesUtil symfony2InterfacesUtil = new Symfony2InterfacesUtil();

        for(PhpClass phpClass: phpClasses) {

            // dont use proxy classes
            String presentableFQN = phpClass.getPresentableFQN();
            if(presentableFQN == null || (presentableFQN.endsWith("Proxy") && symfony2InterfacesUtil.isInstanceOf(phpClass, "\\Enlight_Hook_Proxy"))) {
                continue;
            }

            for(Method method: phpClass.getMethods()) {
                if(!method.getAccess().isPrivate() && !method.isStatic() && !method.isAbstract() && !method.getName().startsWith("_")) {
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

    public static void visitDoctrineQueryBuilderClasses(Project project, Processor<PhpClass> processor) {
        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Model\\ModelRepository")) {
            if(!processor.process(phpClass)) {
                return;
            }
        }
    }

    public static interface DoctrineLifecycleHooksVisitor {
        public boolean visitLifecycleHooks(PhpClass phpClass);
    }

    public static interface HookVisitor {
        public boolean visitHook(PhpClass phpClass, Method method);
    }

    @NotNull
    public static Collection<PsiElement> getAllHookTargets(Project project, final String contents) {

        final Collection<PsiElement> psiElements = new HashSet<PsiElement>();
        Collections.addAll(psiElements, LazySubscriberReferenceProvider.getHookTargets(project, contents));

        // Enlight_Controller_Action_PostDispatchSecure_Frontend_Payment
        Pattern pattern = Pattern.compile("Enlight_Controller_Action_\\w+_(Frontend|Backend|Core|Widgets)_(\\w+)");
        Matcher matcher = pattern.matcher(contents);

        if(matcher.find()) {
            PhpClass phpClass = PhpElementsUtil.getClass(project, String.format("Shopware_Controllers_%s_%s", matcher.group(1), matcher.group(2)));
            if(phpClass != null) {
                psiElements.add(phpClass);
            }
        }

        return psiElements;
    }

}
