package de.espend.idea.shopware.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.index.dict.SubscriberInfo;
import de.espend.idea.shopware.index.utils.SubscriberIndexUtil;
import de.espend.idea.shopware.reference.LazySubscriberReferenceProvider;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HookSubscriberUtil {

    public static Set<String> NOTIFY_EVENTS = ContainerUtil.newHashSet();

    final private static Set<String> CORE_CLASSES = new HashSet<String>() {{
        addAll(Arrays.asList("sCms", "sCore", "sAdmin", "sOrder", "sBasket", "sExport", "sSystem", "sArticles", "sMarketing", "sCategories", "sCategories", "sNewsletter", "sConfigurator", "sRewriteTable"));
    }};

    public static Map<String, Collection<String>> NOTIFY_EVENTS_MAP = new ConcurrentHashMap<>();

    public static void collectHooks(Project project, HookVisitor hookVisitor) {

        Collection<PhpClass> phpClasses = new ArrayList<>();

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

            // dont use proxy classes
            String presentableFQN = phpClass.getPresentableFQN();
            if((presentableFQN.endsWith("Proxy") && PhpElementsUtil.isInstanceOf(phpClass, "\\Enlight_Hook_Proxy"))) {
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

    public interface DoctrineLifecycleHooksVisitor {
        boolean visitLifecycleHooks(PhpClass phpClass);
    }

    public interface HookVisitor {
        boolean visitHook(PhpClass phpClass, Method method);
    }

    @NotNull
    public static Collection<PsiElement> getAllHookTargets(Project project, final String contents) {
        return Arrays.asList(LazySubscriberReferenceProvider.getHookTargets(project, contents));
    }

    public interface SubscriberEventsVisitor {
        void visit(@NotNull String event, @NotNull String methodName, @NotNull StringLiteralExpression key);
    }

    public static void visitSubscriberEvents(@NotNull Method method, @NotNull SubscriberEventsVisitor visitor) {
        method.acceptChildren(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(element instanceof PhpReturn) {
                    visitSubscriberEvents((PhpReturn) element, visitor);
                }
                super.visitElement(element);
            }
        });
    }

    public static void visitSubscriberEvents(@NotNull PhpReturn phpReturn, @NotNull SubscriberEventsVisitor visitor) {
        ArrayCreationExpression arrayCreationExpression = ObjectUtils.tryCast(phpReturn.getArgument(), ArrayCreationExpression.class);
        if(arrayCreationExpression == null) {
            return;
        }

        for (ArrayHashElement entry : arrayCreationExpression.getHashElements()) {
            StringLiteralExpression keyString = ObjectUtils.tryCast(entry.getKey(), StringLiteralExpression.class);
            if(keyString == null) {
                continue;
            }

            String fullEvent = keyString.getContents();
            if(StringUtils.isBlank(fullEvent)) {
                continue;
            }

            PhpPsiElement value = entry.getValue();
            if(value == null) {
                continue;
            }

            String methodName = SubscriberIndexUtil.getMethodNameForEventValue(value);
            if(methodName == null || StringUtils.isBlank(methodName)) {
                continue;
            }

            visitor.visit(fullEvent, methodName, keyString);
        }
    }
}
