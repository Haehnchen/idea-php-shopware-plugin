package de.espend.idea.shopware.reference;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.reference.provider.SmartyTemplateProvider;
import de.espend.idea.shopware.util.TemplateUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventSubscriberReferenceContributor extends PsiReferenceContributor {

    public static MethodMatcher.CallToSignature[] EVENT_SIGNATURES = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Shopware_Components_Plugin_Bootstrap", "subscribeEvent"),
        new MethodMatcher.CallToSignature("\\Shopware_Components_Plugin_Bootstrap", "createEvent"),
    };

    public static MethodMatcher.CallToSignature[] REPOSITORY_SIGNATURES = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Doctrine\\Common\\Persistence\\ManagerRegistry", "getRepository"),
        new MethodMatcher.CallToSignature("\\Doctrine\\Common\\Persistence\\ObjectManager", "getRepository"),
        new MethodMatcher.CallToSignature("\\Doctrine\\Common\\Persistence\\ManagerRegistry", "getManagerForClass"),
    };

    public static MethodMatcher.CallToSignature[] TEMPLATE = new MethodMatcher.CallToSignature[] {
        new MethodMatcher.CallToSignature("\\Enlight_Template_Default", "extendsTemplate"),
    };

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar) {

        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

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

                    if (MethodMatcher.getMatchedSignatureWithDepth(psiElement, REPOSITORY_SIGNATURES) == null) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[]{ new ShopwareModelReferenceProvider((StringLiteralExpression) psiElement) };

                }
            }
        );

        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

                    if (MethodMatcher.getMatchedSignatureWithDepth(psiElement, TEMPLATE) == null) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[]{ new SmartyTemplateProvider((StringLiteralExpression) psiElement) };

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
            final List<ResolveResult> results = new ArrayList<ResolveResult>();

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

            final List<LookupElement> lookupElements = new ArrayList<LookupElement>();

            PhpClass phpClass = PsiTreeUtil.getParentOfType(getElement(), PhpClass.class);
            if(phpClass == null) {
                return lookupElements.toArray();
            }

            for(Method method: phpClass.getMethods()) {
                if(method.getModifier().isPublic() && !method.getName().startsWith("_")) {
                    lookupElements.add(LookupElementBuilder.create(method.getName()).withIcon(PhpIcons.METHOD));
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
            final List<ResolveResult> results = new ArrayList<ResolveResult>();

            collectEvents(getElement().getProject(), new Collector() {
                @Override
                public void collect(PsiElement psiElement, String value) {
                    if(value.equals(valueName)) {
                        results.add(new PsiElementResolveResult(psiElement));
                    }
                }
            });

            return results.toArray(new ResolveResult[results.size()]);

        }

        @NotNull
        @Override
        public Object[] getVariants() {

            final List<LookupElement> lookupElements = new ArrayList<LookupElement>();
            collectEvents(getElement().getProject(), new Collector() {
                @Override
                public void collect(PsiElement psiElement, String value) {
                    lookupElements.add(LookupElementBuilder.create(value).withIcon(ShopwarePluginIcons.SHOPWARE));
                }
            });

            return lookupElements.toArray();
        }
    }

    public static class ShopwareModelReferenceProvider extends PsiPolyVariantReferenceBase<PsiElement> {

        final private String valueName;

        public ShopwareModelReferenceProvider(@NotNull StringLiteralExpression element) {
            super(element);
            this.valueName = element.getContents();
        }

        @NotNull
        @Override
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            List<ResolveResult> results = new ArrayList<ResolveResult>();

            for(PhpClass phpClass: PhpIndex.getInstance(getElement().getProject()).getAllSubclasses("\\Shopware\\Components\\Model\\ModelEntity")) {
                if(this.valueName.equals(phpClass.getPresentableFQN())) {
                    results.add(new PsiElementResolveResult(phpClass));
                }
            }

            return results.toArray(new ResolveResult[results.size()]);

        }

        @NotNull
        @Override
        public Object[] getVariants() {

            final List<LookupElement> lookupElements = new ArrayList<LookupElement>();

            for(PhpClass phpClass: PhpIndex.getInstance(getElement().getProject()).getAllSubclasses("\\Shopware\\Components\\Model\\ModelEntity")) {
                if(phpClass.getPresentableFQN() != null) {
                    lookupElements.add(LookupElementBuilder.create(phpClass.getPresentableFQN()).withIcon(Symfony2Icons.DOCTRINE));
                }
            }

            return lookupElements.toArray();
        }
    }

    public static void collectControllerEvents(Project project, Collector collector) {

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        Collection<PhpClass> phpClasses = phpIndex.getAllSubclasses("\\Enlight_Controller_Action");

        Pattern pattern = Pattern.compile(".*_(Frontend|Backend|Core)_(\\w+)");


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
            Method method = PhpElementsUtil.getClassMethod(phpClass, "install");
            if(method != null) {

                PsiElement[] methodReferences = PsiTreeUtil.collectElements(method, new PsiElementFilter() {
                    @Override
                    public boolean isAccepted(PsiElement element) {
                        if(element instanceof MethodReference) {
                            if("subscribeEvent".equals(((MethodReference) element).getName())) {
                                return true;
                            }
                        }
                        return false;
                    }
                });

                for(PsiElement methodReference: methodReferences ) {
                    if(methodReference instanceof MethodReference) {
                        PsiElement parameter = PsiElementUtils.getMethodParameterPsiElementAt((MethodReference) methodReference, 0);
                        String parameterString = PhpElementsUtil.getStringValue(parameter);
                        if(parameterString != null) {
                            collector.collect(parameter, parameterString);
                        }
                    }
                }

            }

        }
    }

    public interface Collector {
        public void collect(PsiElement psiElement, String value);
    }

}
