package de.espend.idea.shopware.index;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.*;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.index.dict.ServiceResources;
import de.espend.idea.shopware.index.dict.SubscriberInfo;
import de.espend.idea.shopware.index.utils.SubscriberIndexUtil;
import de.espend.idea.shopware.util.HookSubscriberUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.stubs.indexes.externalizer.ArrayDataExternalizer;
import fr.adrienbrault.idea.symfony2plugin.stubs.indexes.externalizer.ObjectStreamDataExternalizer;
import gnu.trove.THashMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InitResourceServiceIndex extends FileBasedIndexExtension<String, String[]> {

    public static final ID<String, String[]> KEY = ID.create("de.espend.idea.shopware.init_resource2");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
    private final static ObjectStreamDataExternalizer<ServiceResources> EXTERNALIZER = new ObjectStreamDataExternalizer<>();

    public final static char TRIM_KEY = '\u0200';

    @NotNull
    @Override
    public ID<String, String[]> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, String[], FileContent> getIndexer() {
        return new DataIndexer<String, String[], FileContent>() {

            @NotNull
            @Override
            public Map<String, String[]> map(@NotNull FileContent inputData) {
                final Map<String, String[]> events = new THashMap<>();

                PsiFile psiFile = inputData.getPsiFile();
                if (!(psiFile instanceof PhpFile) || !Symfony2ProjectComponent.isEnabled(psiFile.getProject())) {
                    return events;
                }

                final Collection<Method> methodReferences = new ArrayList<>();

                psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if(element instanceof Method && "getSubscribedEvents".equals(((Method) element).getName())) {
                            methodReferences.add((Method) element);
                        }
                        super.visitElement(element);
                    }
                });

                if(methodReferences.size() == 0) {
                    return events;
                }

                //public static function getSubscribedEvents() {
                //  return [
                //  'Enlight_Bootstrap_InitResource_swagcoupons.basket_helper' => 'onInitBasketHelper',
                //  'Enlight_Bootstrap_InitResource_swagcoupons.settings' => 'onInitCouponSettings'
                // ];
                //}
                Map<String, Collection<ServiceResource>> serviceMap = new HashMap<>();
                for(final Method method : methodReferences) {
                    method.acceptChildren(new MyEventSubscriberVisitor(method, serviceMap));
                }

                // serialize to container object
                for (Map.Entry<String, Collection<ServiceResource>> entry : serviceMap.entrySet()) {
                    List<String> map = ContainerUtil.map(entry.getValue(), serviceResource ->
                        serviceResource.getEvent() + TRIM_KEY + (serviceResource.getSubscriber() != null ? serviceResource.getSubscriber().getText() : "") + TRIM_KEY + serviceResource.getServiceName() + TRIM_KEY + serviceResource.getSignature()
                    );

                    events.put(entry.getKey(), map.toArray(new String[map.size()]));
                }

                return events;
            }
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }


    @NotNull
    @Override
    public DataExternalizer<String[]> getValueExternalizer() {
        return new ArrayDataExternalizer();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 4;
    }

    private static class MyEventSubscriberVisitor extends PsiRecursiveElementWalkingVisitor {

        @NotNull
        private final Method method;

        @NotNull
        private final Map<String, Collection<ServiceResource>> serviceMap;

        MyEventSubscriberVisitor(@NotNull Method method, @NotNull Map<String, Collection<ServiceResource>> serviceMap) {
            this.method = method;
            this.serviceMap = serviceMap;
        }

        @Override
        public void visitElement(PsiElement element) {
            if(element instanceof PhpReturn) {
                visitPhpReturn((PhpReturn) element);
            }

            super.visitElement(element);
        }

        private void visitPhpReturn(@NotNull PhpReturn phpReturn) {
            PhpClass phpClass = method.getContainingClass();
            if (phpClass == null) {
                return;
            }

            HookSubscriberUtil.visitSubscriberEvents(phpReturn, (event, methodName, key) -> {
                SubscriberInfo subscriberInfo = SubscriberIndexUtil.getSubscriberInfo(event);
                if(subscriberInfo == null) {
                    return;
                }

                ServiceResource serviceResource = new ServiceResource(event, subscriberInfo.getEvent().getText(), subscriberInfo.getService())
                    .setSignature(StringUtils.strip(phpClass.getFQN(), "\\") + '.' + methodName);

                String resourceKey = subscriberInfo.getEvent().getText();
                if(!serviceMap.containsKey(resourceKey)) {
                    serviceMap.put(resourceKey, new ArrayList<>());
                }

                serviceMap.get(resourceKey).add(serviceResource);
            });
        }
    }
}
