package de.espend.idea.shopware.index;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.util.SmartyPattern;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import gnu.trove.THashMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SmartyBlockStubIndex extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create("de.espend.idea.shopware.block_names");
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
                final Map<String, Void> map = new THashMap<String, Void>();

                PsiFile psiFile = inputData.getPsiFile();
                if(!Symfony2ProjectComponent.isEnabled(psiFile.getProject())) {
                    return map;
                }

                psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {

                        if(SmartyPattern.getBlockPattern().accepts(element)) {
                            visitBlock(element);
                        }

                        super.visitElement(element);
                    }

                    private void visitBlock(PsiElement element) {
                        String content = element.getText();
                        if(StringUtils.isNotBlank(content)) {
                            map.put(content, null);
                        }
                    }

                });

                return map;
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
                return file.getFileType() == SmartyFileType.INSTANCE;
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
