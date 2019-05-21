package de.espend.idea.shopware.index;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
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
import de.espend.idea.shopware.util.ShopwareFQDN;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.stubs.indexes.externalizer.StringSetDataExternalizer;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import gnu.trove.THashMap;
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
            if(!(element instanceof MethodReference)) {
                super.visitElement(element);
                return;
            }

            MethodReference methodReference = (MethodReference) element;
            if (!METHODS.contains(methodReference.getName())) {
                super.visitElement(element);
                return;
            }

            if (!(shouldIndex(element.getFirstChild()))) {
                super.visitElement(element);
                return;
            }

            String firstArgument = PhpElementsUtil.getFirstArgumentStringValue(methodReference);


            if (firstArgument.isEmpty()) {
                super.visitElement(element);
                return;
            }

            map.get("all").add(firstArgument);
            super.visitElement(element);
        }
    }

    private Boolean shouldIndex(PsiElement element)
    {
        if (element instanceof FieldReference) {
            FieldReference fieldReference = (FieldReference) element;

            for(String reference : fieldReference.getType().getTypes()) {
                if (reference.equals(ShopwareFQDN.SHOPWARE_CONFIG)) {
                    return true;
                }
            }
        }

        if (element instanceof MethodReference) {
            for(String reference : ((MethodReference) element).getType().getTypes()) {
                if (reference.equals(ShopwareFQDN.SHOPWARE_CONFIG)) {
                    return true;
                }
            }
        }

        if (element instanceof Variable) {
            for(String reference : ((Variable) element).getType().getTypes()) {
                if (reference.equals(ShopwareFQDN.SHOPWARE_CONFIG)) {
                    return true;
                }
            }
        }

        return false;
    }
}
