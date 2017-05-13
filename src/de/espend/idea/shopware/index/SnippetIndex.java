package de.espend.idea.shopware.index;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.util.SnippetUtil;
import de.espend.idea.shopware.util.dict.ShopwareSnippet;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.stubs.indexes.externalizer.StringSetDataExternalizer;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SnippetIndex extends FileBasedIndexExtension<String, Set<String>> {

    public static final ID<String, Set<String>> KEY = ID.create("de.espend.idea.shopware.snippets");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Set<String>> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return inputData -> {
            if(!Symfony2ProjectComponent.isEnabled(inputData.getProject())) {
                return Collections.emptyMap();
            }

            final Map<String, Set<String>> snippets = new THashMap<>();

            PsiFile psiFile = inputData.getPsiFile();
            if (psiFile instanceof SmartyFile) {
                // template files

                for (ShopwareSnippet snippet : SnippetUtil.getSnippetsInFile((SmartyFile) psiFile)) {
                    snippets.putIfAbsent(snippet.getNamespace(), new HashSet<>());
                    snippets.get(snippet.getNamespace()).add(snippet.getName());
                }
            } else if ("ini".equalsIgnoreCase((inputData.getFile().getExtension()))) {
                // ini files

                String presentableUrl = inputData.getFile().getUrl();
                int i = presentableUrl.lastIndexOf("/snippets/");
                if(i > 0) {
                    Set<String> iniKeys = SnippetUtil.getIniKeys(inputData.getContentAsText().toString());
                    if(iniKeys.size() > 0) {
                        String namespace = presentableUrl.substring(i + "/snippets/".length(), presentableUrl.length() - 4);
                        snippets.putIfAbsent(namespace, new HashSet<>());
                        snippets.get(namespace).addAll(iniKeys);
                    }
                }
            } else if (psiFile instanceof JSFile) {
                for (ShopwareSnippet snippet : SnippetUtil.getSnippetsInFile((JSFile) psiFile)) {
                    snippets.putIfAbsent(snippet.getNamespace(), new HashSet<>());
                    snippets.get(snippet.getNamespace()).add(snippet.getName());
                }
            }

            return snippets;
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
        return file -> {
            FileType fileType = file.getFileType();
            return fileType == SmartyFileType.INSTANCE || fileType == JavaScriptFileType.INSTANCE || "ini".equalsIgnoreCase(file.getExtension());
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
