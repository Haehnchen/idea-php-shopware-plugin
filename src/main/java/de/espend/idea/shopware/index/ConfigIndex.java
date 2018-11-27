package de.espend.idea.shopware.index;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.HashSet;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.util.ConfigUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.stubs.indexes.externalizer.StringSetDataExternalizer;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import gnu.trove.THashMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Soner Sayakci <s.sayakci@gmail.com>
 */
public class ConfigIndex extends FileBasedIndexExtension<String, Set<String>> {

    public static final ID<String, Set<String>> KEY = ID.create("de.espend.idea.shopware.config_index");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    private static final Set<String> METHODS = new HashSet<String>() {{
        add("get");
        add("offsetGet");
    }};

    @NotNull
    @Override
    public ID<String, Set<String>> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Set<String>> map = new THashMap<>();
            map.putIfAbsent("all", new java.util.HashSet<>());

            FileType fileType = inputData.getFileType();
            if (fileType == SmartyFileType.INSTANCE && inputData.getPsiFile() instanceof SmartyFile) {
                for (String name : ConfigUtil.getConfigsInFile((SmartyFile) inputData.getPsiFile())) {
                    map.get("all").add(name);
                }

            } else {
                PsiFile psiFile = inputData.getPsiFile();
                if(!Symfony2ProjectComponent.isEnabled(psiFile.getProject())) {
                    return Collections.emptyMap();
                }

                psiFile.accept(new MyPsiRecursiveElementWalkingVisitor(map));
            }


            return map;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<Set<String>> getValueExternalizer() {
        return new StringSetDataExternalizer();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE || file.getFileType() == SmartyFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    private class MyPsiRecursiveElementWalkingVisitor extends PsiRecursiveElementVisitor {

        private final Map<String, Set<String>> map;

        MyPsiRecursiveElementWalkingVisitor(@NotNull Map<String, Set<String>> map) {
            this.map = map;
        }

        @Override
        public void visitElement(PsiElement element) {
            if(!(element instanceof Parameter)) {
                super.visitElement(element);
                return;
            }

            ClassReference classReference = null;

            for(PsiElement loopElement : element.getChildren()) {
                ClassReference classReferenceLoop = ObjectUtils.tryCast(loopElement, ClassReference.class);
                if(classReferenceLoop == null) {
                    return;
                }

                String fqn = StringUtils.stripStart(classReferenceLoop.getFQN(), "\\");
                if(fqn.equals("Shopware_Components_Config")) {
                    classReference = classReferenceLoop;
                    break;
                }
            }

            if (classReference == null) {
                return;
            }

            Parameter parentOfType = PsiTreeUtil.getParentOfType(classReference, Parameter.class);
            if(parentOfType == null) {
                return;
            }

            final String name = parentOfType.getName();

            Method method = PsiTreeUtil.getParentOfType(classReference, Method.class);
            if(method == null) {
                return;
            }

            method.accept(new MyMethodVariableVisitor(name, map));

            super.visitElement(element);
        }
    }

    private class MyMethodVariableVisitor extends PsiRecursiveElementVisitor {

        @NotNull
        private final String name;

        @NotNull
        private final Map<String, Set<String>> result;

        MyMethodVariableVisitor(@NotNull String name, @NotNull Map<String, Set<String>> result) {
            this.name = name;
            this.result = result;
        }

        @Override
        public void visitElement(PsiElement element) {
            if(!(element instanceof Variable) || !name.equals(((Variable) element).getName())) {
                super.visitElement(element);
                return;
            }

            MethodReference methodReference = ObjectUtils.tryCast(element.getParent(), MethodReference.class);
            if(methodReference == null || !METHODS.contains(methodReference.getName())) {
                super.visitElement(element);
                return;
            }

            String value = PhpElementsUtil.getFirstArgumentStringValue(methodReference);
            if(value == null) {
                super.visitElement(element);
                return;
            }

            result.get("all").add(value);

            super.visitElement(element);
        }
    }
}
