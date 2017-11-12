package de.espend.idea.shopware.reference;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.reference.provider.ControllerActionReferenceProvider;
import de.espend.idea.shopware.reference.provider.ControllerReferenceProvider;
import de.espend.idea.shopware.reference.provider.SmartyTemplateProvider;
import de.espend.idea.shopware.reference.provider.StringReferenceProvider;
import de.espend.idea.shopware.util.HookSubscriberUtil;
import de.espend.idea.shopware.util.ShopwareUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class EventSubscriberReferenceContributor extends PsiReferenceContributor {

    public static MethodMatcher.CallToSignature[] EVENT_SIGNATURES = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Shopware_Components_Plugin_Bootstrap", "subscribeEvent"),
        new MethodMatcher.CallToSignature("\\Shopware_Components_Plugin_Bootstrap", "createEvent"),
    };

    public static MethodMatcher.CallToSignature[] TEMPLATE = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Enlight_Template_Default", "extendsTemplate"),
        new MethodMatcher.CallToSignature("\\Enlight_View_Default", "loadTemplate"),
    };

    public static MethodMatcher.CallToSignature[] RESOURCE = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Shopware\\Components\\Api\\Manager", "getResource"),
    };

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar) {

        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                    if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
                        return new PsiReference[0];
                    }

                    if (MethodMatcher.getMatchedSignatureWithDepth(psiElement, EVENT_SIGNATURES) == null) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[]{ new EventReference((StringLiteralExpression) psiElement) };

                }
            }
        );

        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                    if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
                        return new PsiReference[0];
                    }

                    if (MethodMatcher.getMatchedSignatureWithDepth(psiElement, EVENT_SIGNATURES, 1) == null) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[]{ new MethodReferenceProvider((StringLiteralExpression) psiElement) };

                }
            }
        );

        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                    if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
                        return new PsiReference[0];
                    }

                    if (MethodMatcher.getMatchedSignatureWithDepth(psiElement, TEMPLATE) == null) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[]{ new SmartyTemplateProvider((StringLiteralExpression) psiElement) };

                }
            }
        );

        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                    if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
                        return new PsiReference[0];
                    }

                    if (MethodMatcher.getMatchedSignatureWithDepth(psiElement, RESOURCE) == null) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[]{ new ResourcesReference((StringLiteralExpression) psiElement) };

                }
            }
        );

        // for your lazy developers to get unknown "extendsTemplate" calls
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                    if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
                        return new PsiReference[0];
                    }

                    if (!(psiElement instanceof StringLiteralExpression) || MethodMatcher.getMatchedSignatureWithDepth(psiElement, TEMPLATE) != null) {
                        return new PsiReference[0];
                    }

                    String tplName = ((StringLiteralExpression) psiElement).getContents();
                    if(StringUtils.isBlank(tplName) || !tplName.endsWith(".tpl")) {
                        return new PsiReference[0];
                    }

                    tplName = tplName.toLowerCase();
                    if(tplName.startsWith("\\")) {
                        tplName = tplName.substring(1);
                    }

                    if(!tplName.startsWith("frontend") && !tplName.startsWith("backend") && !tplName.startsWith("widgets") && !tplName.startsWith("documents")) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[]{ new SmartyTemplateProvider((StringLiteralExpression) psiElement) };

                }
            }
        );

        // for your lazy developers to get unknown "extendsTemplate" calls
        psiReferenceRegistrar.registerReferenceProvider(
                PlatformPatterns.psiElement(StringLiteralExpression.class).inside(
                    PlatformPatterns.psiElement(ParameterList.class)
                )
            ,
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                    if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
                        return new PsiReference[0];
                    }

                    if(!(psiElement instanceof StringLiteralExpression)) {
                        return new PsiReference[0];
                    }

                    ParameterList parameterList = PsiTreeUtil.getParentOfType(psiElement, ParameterList.class);
                    if (parameterList == null) {
                        return new PsiReference[0];
                    }

                    if(!(parameterList.getContext() instanceof MethodReference)) {
                        return new PsiReference[0];
                    }

                    if(PhpElementsUtil.isMethodReferenceInstanceOf((MethodReference) parameterList.getContext(), "\\Enlight_Controller_Router", "assemble")) {
                        return new PsiReference[0];
                    }

                    ArrayCreationExpression arrayCreation = PsiTreeUtil.getParentOfType(psiElement, ArrayCreationExpression.class);

                    if(arrayCreation != null) {
                        PsiElement arrayValue = psiElement.getParent();
                        if(arrayValue.getNode().getElementType() == PhpElementTypes.ARRAY_VALUE) {
                            PsiElement arrayHashElement = arrayValue.getParent();
                            if(arrayHashElement instanceof ArrayHashElement) {
                                PhpPsiElement key = ((ArrayHashElement) arrayHashElement).getKey();
                                if(key instanceof StringLiteralExpression) {
                                    String contents = ((StringLiteralExpression) key).getContents();

                                    if("controller".equals(contents)) {
                                        PsiElement arrayCreationElement = arrayHashElement.getParent();
                                        String module = null;
                                        if(arrayCreationElement instanceof ArrayCreationExpression) {
                                            module = PhpElementsUtil.getArrayHashValue((ArrayCreationExpression) arrayCreationElement, "module");
                                        }

                                        return new PsiReference[] { new ControllerReferenceProvider((StringLiteralExpression) psiElement, module) };
                                    }

                                    if("action".equals(contents)) {

                                        PsiElement arrayCreationElement = arrayHashElement.getParent();
                                        if(arrayCreationElement instanceof ArrayCreationExpression) {
                                            String controller = PhpElementsUtil.getArrayHashValue((ArrayCreationExpression) arrayCreationElement, "controller");
                                            String module = PhpElementsUtil.getArrayHashValue((ArrayCreationExpression) arrayCreationElement, "module");
                                            if(controller != null) {
                                                return new PsiReference[] { new ControllerActionReferenceProvider((StringLiteralExpression) psiElement, controller, module) };
                                            }
                                        }
                                    }

                                    if("module".equals(contents)) {
                                        return new PsiReference[] { new StringReferenceProvider((StringLiteralExpression) psiElement, "backend", "frontend", "widgets") };
                                    }

                                }
                            }
                        }

                        return new PsiReference[0];
                    }

                    return new PsiReference[0];
                }
            }
        );

        /**
         * public static function getSubscribedEvents() {
         *   return [
         *     'sBasket::sGetBasket::before' => '<caret>',
         *     'sBasket::sGetBasket::before' => ['<caret>', 123],
         *   ];
         * }
         */
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class)
                .withParent(PlatformPatterns.psiElement().withElementType(PhpElementTypes.ARRAY_VALUE))
                .inside(PlatformPatterns.psiElement(Method.class).withName("getSubscribedEvents"))
                .withLanguage(PhpLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                    if(!(psiElement instanceof StringLiteralExpression) || !ShopwareProjectComponent.isValidForProject(psiElement)) {
                        return new PsiReference[0];
                    }

                    PsiElement parent = psiElement.getParent();
                    if(parent == null || parent.getNode().getElementType() != PhpElementTypes.ARRAY_VALUE) {
                        return new PsiReference[0];
                    }

                    PsiElement arrayHashElement = parent.getContext();
                    if(arrayHashElement instanceof ArrayHashElement) {
                        // 'foo' => 'method'
                        PhpPsiElement arrayKey = ((ArrayHashElement) arrayHashElement).getKey();
                        if(arrayKey instanceof StringLiteralExpression) {
                            PsiElement arrayCreationExpression = arrayHashElement.getContext();
                            if(arrayCreationExpression instanceof ArrayCreationExpression) {
                                return new PsiReference[]{ new MethodReferenceProvider((StringLiteralExpression) psiElement) };
                            }
                        }
                    } else if(arrayHashElement instanceof ArrayCreationExpression) {
                        // 'foo' => ['method', 123]
                        PhpPsiElement firstPsiChild = ((ArrayCreationExpression) arrayHashElement).getFirstPsiChild();
                        if(firstPsiChild != null && firstPsiChild.getNode().getElementType() == PhpElementTypes.ARRAY_VALUE) {
                            StringLiteralExpression stringLiteral = ObjectUtils.tryCast(firstPsiChild.getFirstPsiChild(), StringLiteralExpression.class);
                            if(stringLiteral != null) {
                                return new PsiReference[]{ new MethodReferenceProvider(stringLiteral) };
                            }
                        }
                    }

                    return new PsiReference[0];
                }
            }
        );

    }

    public static class MethodReferenceProvider extends PsiPolyVariantReferenceBase<PsiElement> {

        final private String valueName;

        public MethodReferenceProvider(@NotNull StringLiteralExpression element) {
            super(element);
            this.valueName = element.getContents();
        }

        @NotNull
        @Override
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            final List<ResolveResult> results = new ArrayList<>();

            PhpClass phpClass = PsiTreeUtil.getParentOfType(getElement(), PhpClass.class);
            if(phpClass == null) {
                return results.toArray(new ResolveResult[results.size()]);
            }

            for(Method method: phpClass.getMethods()) {
                if(method.getModifier().isPublic() && !method.getName().startsWith("_") && method.getName().equals(this.valueName)) {
                    results.add(new PsiElementResolveResult(method));
                }
            }

            return results.toArray(new ResolveResult[results.size()]);

        }

        @NotNull
        @Override
        public Object[] getVariants() {

            final List<LookupElement> lookupElements = new ArrayList<>();

            PhpClass phpClass = PsiTreeUtil.getParentOfType(getElement(), PhpClass.class);
            if(phpClass == null) {
                return lookupElements.toArray();
            }

            for(Method method: phpClass.getMethods()) {
                if(method.getModifier().isPublic() && !method.getName().startsWith("_")) {
                    lookupElements.add(LookupElementBuilder.create(method.getName()).withIcon(PhpIcons.METHOD).withTypeText("Method", true));
                }
            }

            return lookupElements.toArray();
        }
    }

    public static class EventReference extends PsiPolyVariantReferenceBase<PsiElement> {

        final private String valueName;

        public EventReference(@NotNull StringLiteralExpression element) {
            super(element);
            this.valueName = element.getContents();
        }

        @NotNull
        @Override
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            final List<ResolveResult> results = new ArrayList<>();

            collectEvents(getElement().getProject(), (psiElement, value) -> {
                if(value.equals(valueName)) {
                    results.add(new PsiElementResolveResult(psiElement));
                }
            });

            return results.toArray(new ResolveResult[results.size()]);

        }

        @NotNull
        @Override
        public Object[] getVariants() {

            final Set<String> events = new HashSet<>(HookSubscriberUtil.NOTIFY_EVENTS_MAP.keySet());

            final List<LookupElement> lookupElements = new ArrayList<>();
            collectEvents(getElement().getProject(), (psiElement, value) -> events.add(value));

            for(String event: events) {
                lookupElements.add(LookupElementBuilder.create(event).withIcon(ShopwarePluginIcons.SHOPWARE).withTypeText("Event", true));
            }

            return lookupElements.toArray();
        }
    }

    public static class ResourcesReference extends PsiPolyVariantReferenceBase<PsiElement> {

        final private String valueName;

        public ResourcesReference(@NotNull StringLiteralExpression element) {
            super(element);
            this.valueName = element.getContents();
        }

        @NotNull
        @Override
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            List<ResolveResult> results = new ArrayList<>();

            PhpClass phpClass = ShopwareUtil.getResourceClass(getElement().getProject(), valueName);
            if(phpClass != null) {
                results.add(new PsiElementResolveResult(phpClass));
            }

            return results.toArray(new ResolveResult[results.size()]);

        }

        @NotNull
        @Override
        public Object[] getVariants() {

            List<LookupElement> lookupElements = new ArrayList<>();

            for(Map.Entry<String, PhpClass> entry: ShopwareUtil.getResourceClasses(getElement().getProject()).entrySet()) {
                lookupElements.add(LookupElementBuilder.createWithIcon(entry.getValue()).withLookupString(entry.getKey()).withTypeText(entry.getValue().getPresentableFQN(), true));
            }

            return lookupElements.toArray();
        }
    }

    public static void collectControllerEvents(Project project, Collector collector) {

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        Collection<PhpClass> phpClasses = phpIndex.getAllSubclasses("\\Enlight_Controller_Action");

        Pattern pattern = Pattern.compile(".*_(Frontend|Backend|Core|Widgets)_(\\w+)");


        for (PhpClass phpClass : phpClasses) {

            String className = phpClass.getName();
            Matcher matcher = pattern.matcher(className);

            if(matcher.find()) {
                String moduleName = matcher.group(1);
                String controller = matcher.group(2);


                // http://wiki.shopware.de/Shopware-4-Event-Auflistung-System-Events_detail_988.html
                // http://wiki.shopware.de/Shopware-4.1-Upgrade-Guide-fuer-Entwickler_detail_1297.html
                collector.collect(phpClass, String.format("Enlight_Controller_Action_PostDispatch_%s_%s", moduleName, controller));
                collector.collect(phpClass, String.format("Enlight_Controller_Action_PostDispatchSecure_%s_%s", moduleName, controller));
                collector.collect(phpClass, String.format("Enlight_Controller_Action_PreDispatch_%s_%s", moduleName, controller));

                collector.collect(phpClass, String.format("Enlight_Controller_Dispatcher_ControllerPath_%s_%s", moduleName, controller));
            }

        }
    }

    public static void collectEvents(Project project, Collector collector) {
        collectControllerEvents(project, collector);

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        Collection<PhpClass> phpClasses = phpIndex.getAllSubclasses("\\Shopware_Components_Plugin_Bootstrap");

        for (PhpClass phpClass : phpClasses) {
            for (String methodName : new String[]{"install", "update"}) {
                Method method = phpClass.findMethodByName(methodName);

                if (method != null) {

                    PsiElement[] methodReferences = PsiTreeUtil.collectElements(method, element -> {
                        if (element instanceof MethodReference) {
                            if ("subscribeEvent".equals(((MethodReference) element).getName())) {
                                return true;
                            }
                        }
                        return false;
                    });

                    for (PsiElement methodReference : methodReferences) {
                        if (methodReference instanceof MethodReference) {
                            PsiElement parameter = PsiElementUtils.getMethodParameterPsiElementAt((MethodReference) methodReference, 0);
                            String parameterString = PhpElementsUtil.getStringValue(parameter);
                            if (parameterString != null) {
                                collector.collect(parameter, parameterString);
                            }
                        }
                    }

                }
            }

        }

        for (final Map.Entry<String, Collection<String>> entry : HookSubscriberUtil.NOTIFY_EVENTS_MAP.entrySet()) {
            for (String value : entry.getValue()) {
                String[] split = value.split("\\.");
                Method classMethod = PhpElementsUtil.getClassMethod(project, split[0], split[1]);
                if(classMethod == null) {
                    continue;
                }

                final PsiElement[] count = {classMethod};
                classMethod.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                           @Override
                           public void visitElement(PsiElement element) {
                               if ((element instanceof StringLiteralExpression) && ((StringLiteralExpression) element).getContents().equals(entry.getKey())) {
                                   count[0] = element;
                               }
                               super.visitElement(element);
                           }
                       });

                collector.collect(count[0], entry.getKey());
            }
        }
    }

    public interface Collector {
        void collect(PsiElement psiElement, String value);
    }

}
