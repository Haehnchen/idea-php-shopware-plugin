package de.espend.idea.shopware.util;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.lang.psi.SmartyTag;
import de.espend.idea.shopware.index.SnippetIndex;
import de.espend.idea.shopware.util.dict.ShopwareSnippet;
import fr.adrienbrault.idea.symfony2plugin.stubs.SymfonyProcessors;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SnippetUtil {

    /**
     * {s name="foobar" namespace ="foobar/foobar"}{/s}
     */
    private static void visitSnippets(@NotNull SmartyFile file, @NotNull Consumer<ShopwareSnippet> consumer) {
        LazySmartyFileNamespace lazyFileNamespace = new LazySmartyFileNamespace(file);

        file.acceptChildren(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(!SmartyPattern.getTagAttributePattern("s", "name").accepts(element)) {
                    super.visitElement(element);
                    return;
                }

                String text = element.getText();
                if(StringUtils.isBlank(text)) {
                    super.visitElement(element);
                    return;
                }

                PsiElement parent = element.getParent();
                String namespace = TemplateUtil.getTagAttributeValueByName((SmartyTag) parent, "namespace");
                if(namespace == null) {
                    namespace = lazyFileNamespace.getNamespace();
                }

                if(namespace != null) {
                    consumer.accept(new ShopwareSnippet(element, namespace, text));
                }

                super.visitElement(element);
            }
        });
    }

    /**
     * ExtJs files
     */
    private static void visitSnippets(@NotNull JSFile file, @NotNull Consumer<ShopwareSnippet> consumer) {
        LazyJavascriptFileNamespace lazyFileNamespace = new LazyJavascriptFileNamespace(file);

        file.acceptChildren(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(!(element instanceof JSLiteralExpression)) {
                    super.visitElement(element);
                    return;
                }

                Object value = ((JSLiteralExpression) element).getValue();
                if(!(value instanceof String)) {
                    super.visitElement(element);
                    return;
                }

                String text = (String) value;
                if(text.startsWith("{s")) {
                    visitSnippetText(element, text);
                } else if(text.contains("{s") && text.contains("}")) {
                    Matcher matcher = Pattern.compile("(\\{s\\s+[^{]*})").matcher(text);
                    while(matcher.find()){
                        visitSnippetText(element, matcher.group(1));
                    }
                }

                super.visitElement(element);
            }

            private void visitSnippetText(PsiElement element, String text) {
                String name = ExtJsUtil.getAttributeTagValueFromSmartyString("s", "name", text);
                if(name != null) {
                    String namespace = ExtJsUtil.getAttributeTagValueFromSmartyString("s", "namespace", text);

                    if(namespace == null) {
                        namespace = lazyFileNamespace.getNamespace();
                    }

                    if(namespace != null) {
                        consumer.accept(new ShopwareSnippet(element, namespace, name));
                    }
                }
            }
        });
    }

    /**
     * This call should only used inside index process
     */
    @NotNull
    public static Collection<ShopwareSnippet> getSnippetsInFile(@NotNull SmartyFile file) {
        Collection<ShopwareSnippet> snippets = new ArrayList<>();
        visitSnippets(file, snippets::add);
        return snippets;
    }

    /**
     * This call should only used inside index process
     */
    @NotNull
    public static Collection<ShopwareSnippet> getSnippetsInFile(@NotNull JSFile file) {
        Collection<ShopwareSnippet> snippets = new ArrayList<>();
        visitSnippets(file, snippets::add);
        return snippets;
    }

    @NotNull
    public static Set<String> getSnippetKeysByNamespace(@NotNull Project project, @NotNull String namespace) {
        Set<String> keys = new HashSet<>();

        for (Set<String> snippetValues : FileBasedIndex.getInstance().getValues(SnippetIndex.KEY, namespace, GlobalSearchScope.allScope(project))) {
            keys.addAll(snippetValues);
        }

        return keys;
    }

    @NotNull
    public static Set<String> getSnippetNamespaces(@NotNull Project project) {
        return SymfonyProcessors.createResult(project, SnippetIndex.KEY);
    }

    @NotNull
    public static Collection<PsiElement> getSnippetNameTargets(@NotNull Project project, @NotNull String namespace, @NotNull String name) {
        Set<VirtualFile> files = new HashSet<>();

        // filter ini files for targets
        Set<VirtualFile> collect = FileBasedIndex.getInstance().getContainingFiles(SnippetIndex.KEY, namespace, GlobalSearchScope.allScope(project))
            .stream()
            .filter(virtualFile -> "ini".equalsIgnoreCase(virtualFile.getExtension()))
            .collect(Collectors.toSet());

        // after collect search index again
        for (VirtualFile virtualFile : collect) {
            FileBasedIndex.getInstance().processValues(SnippetIndex.KEY, namespace, virtualFile, (virtualFile1, value) -> {
                if(value.contains(name)) {
                    files.add(virtualFile);
                }

                return true;
            }, GlobalSearchScope.allScope(project));
        }

        PsiManager instance = PsiManager.getInstance(project);

        return files.stream()
            .map(instance::findFile)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }


    /**
     * Snippet target, only use ini files
     */
    @NotNull
    public static Set<PsiElement> getSnippetNamespaceTargets(@NotNull Project project, @NotNull String namespace) {
        Set<VirtualFile> files = new HashSet<>();

        FileBasedIndexImpl.getInstance().getFilesWithKey(SnippetIndex.KEY, new HashSet<>(Collections.singletonList(namespace)), virtualFile -> {
            if("ini".equalsIgnoreCase(virtualFile.getExtension())) {
                files.add(virtualFile);
            }

            return true;
        }, GlobalSearchScope.allScope(project));

        // we are not allows to resolve inside index process
        PsiManager instance = PsiManager.getInstance(project);

        return files.stream()
            .map(instance::findFile)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * The the snippets namespace based on the file scope
     *
     * - {namespace name='frontend/plugins/payment/sepa'}
     * - Find on Theme.php scope: "foo/Theme.php" => "frontend/plugins/payment/sepa"
     */
    @Nullable
    static String getFileNamespace(@NotNull SmartyFile file) {
        String namespace = getFileNamespaceViaInlineNamespace(file);
        if (namespace != null) {
            return namespace;
        }

        return getFileNamespaceViaPath(file);
    }

    /**
     * {namespace name='frontend/plugins/payment/sepa'}
     */
    @Nullable
    public static String getFileNamespaceViaInlineNamespace(@NotNull SmartyFile file) {
        for (PsiElement psiElement : file.getChildren()) {
            if(psiElement instanceof SmartyTag && "namespace".equals(((SmartyTag) psiElement).getTagName())) {
                String name = TemplateUtil.getTagAttributeValueByName((SmartyTag) psiElement, "name");
                if(StringUtils.isBlank(name)) {
                    return null;
                }

                return name;
            }
        }

        return null;
    }

    /**
     * Find on Theme.php scope: "foo/Theme.php" => "frontend/plugins/payment/sepa"
     * Find on Plugin.php scope: "foo/Plugin.php" => "Resources/views/frontend/plugins/payment/sepa"
     */
    @Nullable
    public static String getFileNamespaceViaPath(@NotNull SmartyFile file) {
        VirtualFile parent = file.getVirtualFile().getParent();

        // "foo/Theme.php" => "frontend/plugins/payment/sepa"
        for(PhpClass phpClass: PhpIndex.getInstance(file.getProject()).getAllSubclasses("\\Shopware\\Components\\Theme")) {
            VirtualFile virtualFile = phpClass.getContainingFile().getVirtualFile().getParent();
            String relativePath = VfsUtil.findRelativePath(virtualFile, parent, '/');

            if(relativePath != null) {
                return relativePath + "/" + file.getVirtualFile().getNameWithoutExtension();
            }
        }

        // "foo/Plugin.php" => "foo/Resources/views/frontend/plugins/payment/sepa"
        for(PhpClass phpClass: PhpIndex.getInstance(file.getProject()).getAllSubclasses("\\Shopware\\Components\\Plugin")) {
            VirtualFile virtualFile = phpClass.getContainingFile().getVirtualFile().getParent();
            if(virtualFile == null) {
                continue;
            }

            VirtualFile views = VfsUtil.findRelativeFile(virtualFile, "Resources", "views");
            if(views == null) {
                continue;
            }

            String relativePath = VfsUtil.findRelativePath(views, parent, '/');

            if(relativePath != null) {
                return relativePath + "/" + file.getVirtualFile().getNameWithoutExtension();
            }
        }

        return null;
    }

    private static class LazySmartyFileNamespace {
        @NotNull
        private final SmartyFile smartyFile;

        @Nullable
        private String namespace = null;

        private boolean loaded = false;

        LazySmartyFileNamespace(@NotNull SmartyFile smartyFile) {
            this.smartyFile = smartyFile;
        }

        @Nullable
        public String getNamespace() {
            if(loaded) {
                return this.namespace;
            }

            // for nullable loaded
            loaded = true;

            return this.namespace = getFileNamespaceViaInlineNamespace(this.smartyFile);
        }
    }

    private static class LazyJavascriptFileNamespace {
        @NotNull
        private final JSFile jsFile;

        @Nullable
        private String namespace = null;

        private boolean loaded = false;

        LazyJavascriptFileNamespace(@NotNull JSFile jsFile) {
            this.jsFile = jsFile;
        }

        @Nullable
        public String getNamespace() {
            if(loaded) {
                return this.namespace;
            }

            // for nullable loaded
            loaded = true;

            return this.namespace = ExtJsUtil.getSnippetNamespaceFromFile(this.jsFile);
        }
    }

    /**
     * Ini file key loader for snippets
     * "foo = foobar"
     */
    @NotNull
    public static Set<String> getIniKeys(@NotNull String contents) {
        Set<String> entries = new HashSet<>();

        Pattern pattern = Pattern.compile("\\s*([^=]*)=(.*)");

        try(BufferedReader br = new BufferedReader(new StringReader(contents))) {
            String line;
            while((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if(matcher.matches()) {
                    entries.add(matcher.group(1).trim());
                }
            }
        } catch (IOException ignored) {
        }

        return entries;
    }
}
