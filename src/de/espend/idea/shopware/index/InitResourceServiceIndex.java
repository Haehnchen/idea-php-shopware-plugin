package de.espend.idea.shopware.index;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import gnu.trove.THashMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class InitResourceServiceIndex extends FileBasedIndexExtension<String, String> {

    public static final ID<String, String> KEY = ID.create("de.espend.idea.shopware.init_resource_service_index");
    public static final String ENLIGHT_BOOTSTRAP_INIT_RESOURCE_PREFIX = "Enlight_Bootstrap_InitResource_";
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, String> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return new DataIndexer<String, String, FileContent>() {

            @NotNull
            @Override
            public Map<String, String> map(@NotNull FileContent inputData) {
                final Map<String, String> events = new THashMap<String, String>();

                PsiFile psiFile = inputData.getPsiFile();
                if (!(psiFile instanceof PhpFile) || !Symfony2ProjectComponent.isEnabled(psiFile.getProject())) {
                    return events;
                }

                final Collection<Method> methodReferences = new ArrayList<Method>();

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
                for(final Method method : methodReferences) {
                    method.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                        @Override
                        public void visitElement(PsiElement element) {

                            if(element instanceof StringLiteralExpression) {
                                ArrayCreationExpression arrayCreationExpression = PhpElementsUtil.getCompletableArrayCreationElement(element);
                                if (arrayCreationExpression != null) {
                                    PsiElement returnStatement = arrayCreationExpression.getParent();
                                    if (returnStatement instanceof PhpReturn) {
                                        Map<String, String> arrayCreationKeyMap = PhpElementsUtil.getArrayKeyValueMap(arrayCreationExpression);
                                        for (String key : arrayCreationKeyMap.keySet()) {
                                            if (key.startsWith(ENLIGHT_BOOTSTRAP_INIT_RESOURCE_PREFIX)) {
                                                String serviceName = key.substring(ENLIGHT_BOOTSTRAP_INIT_RESOURCE_PREFIX.length());
                                                String methodName = arrayCreationKeyMap.get(key);
                                                if(StringUtils.isNotBlank(serviceName) && StringUtils.isNotBlank(methodName)) {
                                                    PhpClass phpClass = method.getContainingClass();
                                                    if (phpClass != null) {
                                                        String presentableFQN = phpClass.getPresentableFQN();
                                                        if (StringUtils.isNotBlank(presentableFQN)) {
                                                            events.put(serviceName, presentableFQN + '.' + methodName);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            super.visitElement(element);
                        }
                    });
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
    public DataExternalizer<String> getValueExternalizer() {
        return StringDataExternalizer.STRING_DATA_EXTERNALIZER;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return file.getFileType() == PhpFileType.INSTANCE;
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 7;
    }

    private static class StringDataExternalizer implements DataExternalizer<String> {

        public static final StringDataExternalizer STRING_DATA_EXTERNALIZER = new StringDataExternalizer();
        private final EnumeratorStringDescriptor myStringEnumerator = new EnumeratorStringDescriptor();

        @Override
        public void save(@NotNull DataOutput out, String value) throws IOException {

            if(value == null) {
                value = "";
            }

            this.myStringEnumerator.save(out, value);
        }

        @Override
        public String read(@NotNull DataInput in) throws IOException {

            String value = this.myStringEnumerator.read(in);

            // EnumeratorStringDescriptor writes out "null" as string, so workaround here
            if("null".equals(value)) {
                value = "";
            }

            // it looks like this is our "null keys not supported" #238, #277
            // so dont force null values here

            return value;
        }
    }
}