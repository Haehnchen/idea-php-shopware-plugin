package de.espend.idea.shopware.index;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.*;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServiceDiIndex extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create("de.espend.idea.shopware.service_di_index");
    public static final String ENLIGHT_BOOTSTRAP_INIT_RESOURCE_PREFIX = "Enlight_Bootstrap_InitResource_";
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {

            @NotNull
            @Override
            public Map<String, Void> map(FileContent inputData) {
                final Map<String, String> events = new THashMap<String, String>();

                PsiFile psiFile = inputData.getPsiFile();
                if (!Symfony2ProjectComponent.isEnabled(psiFile.getProject())) {
                    return events;
                }

                final Map<String, String> useImports = new HashMap<String, String>();

                for (PhpUseList phpUseList : PsiTreeUtil.findChildrenOfType(psiFile, PhpUseList.class)) {
                    PhpUse[] declarations = phpUseList.getDeclarations();
                    if (declarations != null) {
                        for (PhpUse phpUse : declarations) {
                            String alias = phpUse.getAliasName();
                            if (alias != null) {
                                useImports.put(alias, phpUse.getOriginal());
                            } else {
                                useImports.put(phpUse.getName(), phpUse.getOriginal());
                            }
                        }
                    }
                }
//                should also work... somehow?!
//                List<PhpUseList> phpUseLists = PhpCodeInsightUtil.collectImports();

                psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if (useImports.containsKey("Enlight\\Event\\SubscriberInterface"))

                        ArrayCreationExpression arrayCreationExpression = PhpElementsUtil.getCompletableArrayCreationElement(element);
                        if (arrayCreationExpression != null) {
                            PsiElement returnStatement = arrayCreationExpression.getParent();
                            if (returnStatement instanceof PhpReturn) {
                                Method method = PsiTreeUtil.getParentOfType(returnStatement, Method.class);
                                if (method != null) {
                                    if ("getSubscribedEvents".equals(method.getName())) {
                                        Map<String, PsiElement> arrayCreationKeyMap = PhpElementsUtil.getArrayCreationKeyMap(arrayCreationExpression);
                                        for (String key : arrayCreationKeyMap.keySet()) {
                                            if (key.startsWith(ENLIGHT_BOOTSTRAP_INIT_RESOURCE_PREFIX)) {
                                                String serviceName = key.substring(ENLIGHT_BOOTSTRAP_INIT_RESOURCE_PREFIX.length());
                                                String className = method.getContainingClass().getPresentableFQN();
                                                String methodName = method.getName();
                                                events.put(serviceName, className + '.' + methodName);
                                            }
                                        }
                                        PhpClass phpClass = method.getContainingClass();
//                                        collectHookLookupElements(originalPosition.getProject(), result, true);
                                    }
                                }
                            }
                        }


//                        if(element instanceof MethodReference && METHOD_NAMES.contains(((MethodReference) element).getName())) {
//                            map.put(((MethodReference) element).getName(), null);
//                        }

                        super.visitElement(element);
                    }

                });

                return events;
            }


        };
    }

    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @Override
    public DataExternalizer<Void> getValueExternalizer() {
        return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
    }

    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(VirtualFile file) {
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
        return 3;
    }
}
