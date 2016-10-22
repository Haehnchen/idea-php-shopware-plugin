package de.espend.idea.shopware.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.AssignmentExpressionImpl;
import com.jetbrains.smarty.SmartyFile;
import de.espend.idea.shopware.index.InitResourceServiceIndex;
import de.espend.idea.shopware.index.dict.BootstrapResource;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.index.utils.SubscriberIndexUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopwareUtil {

    public static Set<String> PLUGIN_CONFIGS = ContainerUtil.newHashSet();

    final public static String[] PLUGIN_CONFIG_TYPES = new String[] {
        "text", "color", "datetime", "html", "interval", "mediaselection", "number", "select", "combo", "textarea", "time"
    };

    final public static String[] PLUGIN_CONFIG_OPTIONS = new String[] {
        "label", "value", "scope", "description", "required", "attributes", "store", "inputType"
    };

    final public static String[] PLUGIN_INFO = new String[] {
        "version", "author", "label", "license", "copyright", "support", "link"
    };

    final public static String[] CONTAINER_SERVICE_PREFIX = new String[] {
        "Enlight_Bootstrap_AfterInitResource_", "Enlight_Bootstrap_AfterRegisterResource_", "Enlight_Bootstrap_InitResource_"
    };

    // Get the attribute tables from MYSQL with
    /*
        SELECT TABLE_NAME
        FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = SCHEMA()
        AND TABLE_NAME LIKE '%attributes'
     */
    final public static String[] MODEL_STATIC_ATTRIBUTES = new String[] {
        "s_article_configurator_template_prices_attributes",
        "s_article_configurator_templates_attributes",
        "s_articles_attributes",
        "s_articles_downloads_attributes",
        "s_articles_esd_attributes",
        "s_articles_img_attributes",
        "s_articles_information_attributes",
        "s_articles_prices_attributes",
        "s_articles_supplier_attributes",
        "s_blog_attributes",
        "s_categories_attributes",
        "s_cms_static_attributes",
        "s_cms_support_attributes",
        "s_core_auth_attributes",
        "s_core_config_mails_attributes",
        "s_core_countries_attributes",
        "s_core_countries_states_attributes",
        "s_core_customergroups_attributes",
        "s_core_paymentmeans_attributes",
        "s_emarketing_banners_attributes",
        "s_emarketing_vouchers_attributes",
        "s_emotion_attributes",
        "s_export_attributes",
        "s_filter_attributes",
        "s_filter_options_attributes",
        "s_filter_values_attributes",
        "s_media_attributes",
        "s_order_attributes",
        "s_order_basket_attributes",
        "s_order_billingaddress_attributes",
        "s_order_details_attributes",
        "s_order_documents_attributes",
        "s_order_shippingaddress_attributes",
        "s_premium_dispatch_attributes",
        "s_product_streams_attributes",
        "s_user_addresses_attributes",
        "s_user_attributes",
        "s_user_billingaddress_attributes",
        "s_user_shippingaddress_attributes"
    };

    final public static String[] MODEL_STATIC_ATTRIBUTE_TYPES = new String[] {
        "int(11)", "int(1)", "varchar(255)", "datetime", "mediumtext", "date", "decimal(10,2)", "double", "text"
    };

    final public static String[] ROUTE_ASSEMBLE = new String[] {
        "module", "controller", "appendSession", "action", "fullPath", "module"
    };

    final public static String[] ATTRIBUTE_DATA_TYPES = new String[] {
        "string", "text", "html", "integer", "float", "boolean", "date", "datetime", "combobox", "single_selection", "multi_selection"
    };

    final public static String[] ATTRIBUTE_BACKEND_VIEWS = new String[] {
        "label", "supportText", "helpText", "translatable", "displayInBackend", "entity", "position", "custom", "arrayStore"
    };

    public static void collectControllerClass(Project project, ControllerClassVisitor controllerClassVisitor) {
        collectControllerClass(project, controllerClassVisitor, "Frontend" , "Backend", "Core", "Widgets");
    }

    public static void collectControllerClass(Project project, ControllerClassVisitor controllerClassVisitor, String... modules) {

        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<PhpClass> phpClasses = phpIndex.getAllSubclasses("\\Enlight_Controller_Action");

        Pattern pattern = Pattern.compile(".*_(" + StringUtils.join(modules, "|") + ")_(\\w+)", Pattern.CASE_INSENSITIVE);

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
        void visitClass(PhpClass phpClass, String moduleName, String controllerName);
    }

    public static void collectControllerActionSmartyWrapper(PsiElement psiElement, ControllerActionVisitor visitor) {
        collectControllerActionSmartyWrapper(psiElement, visitor, "Frontend", "Backend", "Core");
    }

    public static void collectControllerActionSmartyWrapper(PsiElement psiElement, ControllerActionVisitor visitor, String... modules) {

        Pattern pattern = Pattern.compile("controller=[\"|']*(\\w+)[\"|']*");
        Matcher matcher = pattern.matcher(psiElement.getParent().getText());
        if(!matcher.find()) {
            return;
        }

        String controllerName = toCamelCase(matcher.group(1), false);
        collectControllerAction(psiElement.getProject(), controllerName, visitor, modules);
    }

    public static void collectControllerAction(Project project, String controllerName, ControllerActionVisitor visitor, String... modules) {

        for(String moduleName: modules) {
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
        void visitMethod(Method method, String methodStripped, String moduleName, String controllerName);
    }

    @Nullable
    public static Method getControllerActionOnSmartyFile(SmartyFile smartyFile) {
        return getControllerActionOnSmartyFile(smartyFile, "frontend", "backend", "core");
    }

    @Nullable
    public static Method getControllerActionOnSmartyFile(SmartyFile smartyFile, String... modules) {

        String relativeFilename = TemplateUtil.getTemplateName(smartyFile.getProject(), smartyFile.getVirtualFile());
        if(relativeFilename == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(".*[/]*(" + StringUtils.join(modules, "|") + ")/(\\w+)/(\\w+)\\.tpl");
        Matcher matcher = pattern.matcher(relativeFilename);

        if(!matcher.find()) {
            return null;
        }

        // Shopware_Controllers_Frontend_Account
        String moduleName = toCamelCase(matcher.group(1), false);
        String controller = toCamelCase(matcher.group(2), false);
        String action = toCamelCase(matcher.group(3), true);

        // build class name
        String className = String.format("\\Shopware_Controllers_%s_%s", moduleName, controller);
        PhpClass phpClass = PhpElementsUtil.getClassInterface(smartyFile.getProject(), className);
        if(phpClass == null) {
            return null;
        }

        return phpClass.findMethodByName(action + "Action");

    }

    public static String toCamelCase(String value, boolean startWithLowerCase) {
        String[] strings = StringUtils.split(value.toLowerCase(), "_");
        for (int i = startWithLowerCase ? 1 : 0; i < strings.length; i++){
            strings[i] = StringUtils.capitalize(strings[i]);
        }
        return StringUtils.join(strings);
    }

    public static void collectControllerViewVariable(Method method, ControllerViewVariableVisitor controllerViewVariableVisitor) {

        // Views()->test;
        for(FieldReference fieldReference: PsiTreeUtil.collectElementsOfType(method, FieldReference.class)) {
            PsiElement methodReference = fieldReference.getFirstChild();
            if(methodReference instanceof MethodReference) {
                if(((MethodReference) methodReference).getCanonicalText().equals("View")) {

                    PsiElement parentElement = fieldReference.getParent();
                    if(parentElement instanceof AssignmentExpressionImpl) {
                        controllerViewVariableVisitor.visitVariable(fieldReference.getName(), methodReference, ((AssignmentExpressionImpl) parentElement).getValue());
                    } else {
                        // need this ???
                        //controllerViewVariableVisitor.visitVariable(fieldReference.getName(), null);
                    }

                }
            }
        }

        // Views()->assign('test'); // Views()->assign(['test' => 'test'])
        for(MethodReference methodReference: PsiTreeUtil.collectElementsOfType(method, MethodReference.class)) {

            if("assign".equals(methodReference.getName())) {
                PsiElement firstParameter = PsiElementUtils.getMethodParameterPsiElementAt(methodReference, 0);
                if(firstParameter instanceof ArrayCreationExpression) {
                    for(String keyName: PhpElementsUtil.getArrayCreationKeys((ArrayCreationExpression) firstParameter)) {
                        // @TODO: add source and type resolve
                        controllerViewVariableVisitor.visitVariable(keyName, firstParameter, null);
                    }
                } else {
                    String value = PhpElementsUtil.getStringValue(firstParameter);
                    if(value != null) {
                        // @TODO: add source and type resolve
                        controllerViewVariableVisitor.visitVariable(value, firstParameter, null);
                    }
                }
            }

        }
    }

    public interface ControllerViewVariableVisitor {
        void visitVariable(String variableName, @NotNull PsiElement sourceType, @Nullable PsiElement typeElement);
    }

    public static Map<String, PhpClass> getResourceClasses(Project project) {

        Map<String, PhpClass> phpClassMap = new HashMap<>();

        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Api\\Resource\\Resource")) {
            phpClassMap.put(phpClass.getName(), phpClass);
        }

        return phpClassMap;
    }

    @Nullable
    public static PhpClass getResourceClass(Project project, String resourceName) {

        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Api\\Resource\\Resource")) {
            if(resourceName.equalsIgnoreCase(phpClass.getName())) {
                return phpClass;
            }
        }

        return null;
    }

    public static PsiElementPattern.Capture<PsiElement> getBootstrapPathPattern() {
        // @TODO add method reference filter
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression.class).afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                PlatformPatterns.psiElement().withText(".")
            )
        );
    }

    public static void collectBootstrapFiles(StringLiteralExpression literalExpression, final BootstrapFileVisitor visitor) {

        MethodReference methodReference = PsiElementUtils.getPrevSiblingOfType(literalExpression, PlatformPatterns.psiElement(MethodReference.class).withText(PlatformPatterns.string().contains("Path()")));
        if(methodReference == null) {
            return;
        }

        PsiFile currentFile = literalExpression.getContainingFile();
        final PsiDirectory psiDirectory = currentFile.getParent();
        if(psiDirectory == null) {
            return;
        }

        VfsUtil.processFilesRecursively(psiDirectory.getVirtualFile(), virtualFile -> {

            if(virtualFile.isDirectory() || "php".equalsIgnoreCase(virtualFile.getExtension())) {
                String relativePath = VfsUtil.getRelativePath(virtualFile, psiDirectory.getVirtualFile(), '/');
                if(StringUtils.isNotBlank(relativePath)) {
                    visitor.visitVariable(virtualFile, relativePath);
                }
            }

            return true;
        });
    }

    public interface BootstrapFileVisitor {
        void visitVariable(VirtualFile virtualFile, String relativePath);
    }


    public static Set<String> getLookupHooks(String content) {
        return getHookCompletionNameCleanup(getCamelizeHook(content));
    }

    public static String getCamelizeHook(String content) {

        content = content.replaceAll("_+", "_").replaceAll(":+", "_").replaceAll("\\\\+", "_");

        List<String> items = new ArrayList<>();
        for(String s: content.split("(?=\\p{Lu})")) {
            if(s.length() > 1) {
                items.add(s);
            }
        }

        return toCamelCase(StringUtils.join(items, "_").toLowerCase(), true);
    }

    public static Set<String> getHookCompletionNameCleanup(String content) {

        Set<String> set = new HashSet<>();
        set.add("on" + ucfirst(content));

        for(String startCleanup: new String[] {"shopware", "shopwareControllers", "enlightController", "enlight", "shopwareControllers"}) {
            if(content.startsWith(startCleanup) && content.length() - startCleanup.length() > 1) {
                set.add("on" + ucfirst(content.substring(startCleanup.length())));
            }
        }

        return set;
    }

    private static String ucfirst(String subject) {
        return Character.toUpperCase(subject.charAt(0)) + subject.substring(1);
    }

    /**
     * "Enlight_Controller_Action_PostDispatchSecure_Frontend_Payment" _> Shopware_Controllers_Frontend_Payment
     */
    @Nullable
    public static PhpClass getControllerOnActionSubscriberName(@NotNull Project project, @NotNull String subscriberName) {
        Pattern pattern = Pattern.compile("Enlight_Controller_Action_\\w+_(Frontend|Backend|Core|Widgets)_(\\w+)");
        Matcher matcher = pattern.matcher(subscriberName);

        if(matcher.find()) {
            PhpClass phpClass = PhpElementsUtil.getClass(project, String.format("Shopware_Controllers_%s_%s", matcher.group(1), matcher.group(2)));
            if(phpClass != null) {
                return phpClass;
            }
        }

        return null;
    }
}
