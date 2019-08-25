package de.espend.idea.shopware.util;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.SmartyFileType;
import com.jetbrains.smarty.lang.SmartyTokenTypes;
import com.jetbrains.smarty.lang.psi.SmartyTag;
import fr.adrienbrault.idea.symfony2plugin.util.yaml.YamlHelper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class TemplateUtil {

    public static void collectFiles(Project project, SmartyTemplateVisitor smartyTemplateVisitor) {
        collectFiles(project, smartyTemplateVisitor, "tpl");
    }

    public static void collectFiles(Project project, final SmartyTemplateVisitor smartyTemplateVisitor, String... extensions) {

        final List<String> exts = Arrays.asList(extensions);
        final List<VirtualFile> uniqueVirtualFiles = new ArrayList<>();

        collectPluginTemplates(project, smartyTemplateVisitor, exts);

        // search for index files; think of lib and include path
        List<LanguageFileType> languageFileTypes = new ArrayList<>();

        if(exts.contains("tpl")) {
            languageFileTypes.add(SmartyFileType.INSTANCE);
        }

        if(exts.contains("js")) {
            languageFileTypes.add(JavaScriptSupportLoader.JAVASCRIPT);
        }

        // sw5: provides parent class for themes
        Set<VirtualFile> themes = new HashSet<>();
        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Theme")) {
            PsiDirectory parent = phpClass.getContainingFile().getParent();
            if(parent != null) {
                themes.add(parent.getVirtualFile());
            }
        }

        for(LanguageFileType fileType: languageFileTypes) {
            for(VirtualFile virtualFile : FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, fileType, GlobalSearchScope.allScope(project))) {
                if(!uniqueVirtualFiles.contains(virtualFile)) {
                    uniqueVirtualFiles.add(virtualFile);

                    // try to get /templates/frontend/...
                    String path = virtualFile.toString();
                    int i = path.lastIndexOf("/templates/");
                    if(i > 0) {
                        String frontendName = path.substring(i + "/templates/".length());
                        attachTemplates(virtualFile, frontendName, smartyTemplateVisitor);
                    } else if (themes.size() > 0) {

                        // sw5: check if file is somewhere inside a theme folder
                        for(VirtualFile themeDir: themes) {
                            if(VfsUtil.isAncestor(themeDir, virtualFile, false)) {
                                String relativePath = VfsUtil.getRelativePath(virtualFile, themeDir, '/');
                                if(relativePath != null) {
                                    // we are too lazy prepend path name to simulate old behavior:
                                    // "Bare/frontend/campaign"
                                    attachTemplates(virtualFile, themeDir.getName() + "/" + relativePath, smartyTemplateVisitor);
                                }

                            }
                        }

                    }
                }
            }
        }

        // wooh... not a full project, so skip it
        // shopware is in lib only condition
        final VirtualFile templateDir = VfsUtil.findRelativeFile("templates", project.getBaseDir());
        if(templateDir == null) {
            return;
        }

        // collect on project template dir
        VfsUtil.visitChildrenRecursively(templateDir, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile virtualFile) {

                if(uniqueVirtualFiles.contains(virtualFile)) {
                    return true;
                }

                uniqueVirtualFiles.add(virtualFile);

                if(!isValidTemplateFile(virtualFile, exts)) {
                    return true;
                }

                String frontendName = VfsUtil.getRelativePath(virtualFile, templateDir, '/');
                if(frontendName == null) {
                    return true;
                }

                attachTemplates(virtualFile, frontendName, smartyTemplateVisitor);

                return true;
            }
        });
    }

    private static boolean attachTemplates(VirtualFile virtualFile, String frontendName, SmartyTemplateVisitor smartyTemplateVisitor) {

        String[] pathSplits = StringUtils.split(frontendName, "/");
        if(pathSplits.length < 2 || (!"frontend".equals(pathSplits[1]) && !"backend".equals(pathSplits[1])) && !"widgets".equals(pathSplits[1])) {
            return true;
        }

        int i = frontendName.indexOf("/");
        if(i == -1) {
            return true;
        }

        int n = pathSplits.length-1;
        String[] newArray = new String[n];
        System.arraycopy(pathSplits, 1, newArray, 0, n);

        String fileName = StringUtils.join(newArray, "/");
        smartyTemplateVisitor.visitFile(virtualFile, fileName);

        return false;
    }

    private static void collectPluginTemplates(Project project, final SmartyTemplateVisitor smartyTemplateVisitor, final List<String> exts) {
        Collection<PhpClass> phpClasses = PhpIndex.getInstance(project).getAllSubclasses("\\Shopware_Components_Plugin_Bootstrap");
        for(PhpClass phpClass: phpClasses) {

            PsiDirectory psiDirectory = phpClass.getContainingFile().getContainingDirectory();
            final VirtualFile virtualViewDir = VfsUtil.findRelativeFile("Views", psiDirectory.getVirtualFile());
            if(virtualViewDir != null) {
                VfsUtil.visitChildrenRecursively(virtualViewDir, new VirtualFileVisitor() {
                    @Override
                    public boolean visitFile(@NotNull VirtualFile file) {

                        if(!isValidTemplateFile(file, exts)) {
                            return true;
                        }

                        String frontendName = VfsUtil.getRelativePath(file, virtualViewDir, '/');
                        if(frontendName == null) {
                            return true;
                        }

                        smartyTemplateVisitor.visitFile(file, frontendName);

                        return true;
                    }
                });
            }
        }

        Collection<PhpClass> newPluginPhpClasses = PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Plugin");
        for(PhpClass phpClass: newPluginPhpClasses) {

            PsiDirectory psiDirectory = phpClass.getContainingFile().getContainingDirectory();
            final VirtualFile virtualViewDir = VfsUtil.findRelativeFile("Resources/views", psiDirectory.getVirtualFile());
            if(virtualViewDir != null) {
                VfsUtil.visitChildrenRecursively(virtualViewDir, new VirtualFileVisitor() {
                    @Override
                    public boolean visitFile(@NotNull VirtualFile file) {

                        if(!isValidTemplateFile(file, exts)) {
                            return true;
                        }

                        String frontendName = VfsUtil.getRelativePath(file, virtualViewDir, '/');
                        if(frontendName == null) {
                            return true;
                        }

                        smartyTemplateVisitor.visitFile(file, frontendName);

                        return true;
                    }
                });
            }
        }
    }

    private static boolean isValidTemplateFile(VirtualFile virtualFile, List<String> extensions) {

        if(virtualFile.isDirectory()) {
            return false;
        }
        String filename = virtualFile.getName();
        for(String ext: extensions) {
            if(filename.toLowerCase().endsWith(ext.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public interface SmartyTemplateVisitor {
        void visitFile(VirtualFile virtualFile, String fileName);
    }

    public static abstract class SmartyTemplatePreventSelfVisitor implements SmartyTemplateVisitor {

        final VirtualFile virtualFile;

        public SmartyTemplatePreventSelfVisitor(VirtualFile virtualFile) {
            this.virtualFile = virtualFile;
        }

        public void visitFile(VirtualFile virtualFile, String fileName) {
            if(!this.virtualFile.equals(virtualFile)) {
                visitNonSelfFile(virtualFile, fileName);
            }
        }

        abstract public void visitNonSelfFile(VirtualFile virtualFile, String fileName);
    }

    public static boolean isExtendsTemplate(PsiFile psiFile) {

        for(SmartyTag smartyTag: PsiTreeUtil.getChildrenOfTypeAsList(psiFile, SmartyTag.class)) {
            if("extends".equals(smartyTag.getName())) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public static String getTemplateName(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return getTemplateName(project, virtualFile, "frontend", "backend", "widgets");
    }

    /**
     * Find on Theme.php scope: "foo/Theme.php" => "frontend/plugins/payment/sepa"
     * Find on Plugin.php scope: "foo/Plugin.php" => "Resources/views/frontend/plugins/payment/sepa"
     */
    @Nullable
    public static String getTemplateNameViaPath(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String fileNamespaceViaPath = SnippetUtil.getFileNamespaceViaPath(project, virtualFile);

        return fileNamespaceViaPath != null
            ? fileNamespaceViaPath + "." + virtualFile.getExtension()
            : null;
    }

    @Nullable
    public static String getTemplateName(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull String... modules) {
        String templateNameViaPath = getTemplateNameViaPath(project, virtualFile);
        if (templateNameViaPath != null) {
            return templateNameViaPath;
        }

        // Shopware <= 5.1 "/templates/[emotion_black]/frontend"
        String frontendName = VfsUtil.getRelativePath(virtualFile, project.getBaseDir(), '/');
        if(frontendName == null) {
            // search for possible indexed files
            String path = virtualFile.toString();
            int i = path.lastIndexOf("/templates/");
            if(i == -1) {
                return null;
            }

            frontendName = path.substring(i + "/templates/".length());
        }

        // find "frontend" or any other given module inside the path
        for(String module: modules) {
            int i = frontendName.indexOf(module);
            if(i > 0) {
                return frontendName.substring(i);
            }
        }

        return null;
    }

    public static String cleanTemplateName(String templateName) {

        if(templateName.startsWith("parent:")) {
            templateName = templateName.substring("parent:".length());
        }

        if(templateName.startsWith("./")) {
            templateName = templateName.substring("./".length());
        }

        return templateName;
    }

    /**
     * {tag attribute="foobar"}{/s}
     */
    @Nullable
    public static PsiElement getTagAttributeByName(@NotNull SmartyTag tag, @NotNull String attribute) {
        return ContainerUtil.find(YamlHelper.getChildrenFix(tag), psiElement ->
            SmartyPattern.getAttributeKeyPattern().accepts(psiElement) && attribute.equals(psiElement.getText())
        );
    }

    /**
     * {tag attribute="foobar"}{/s}
     */
    @Nullable
    public static String getTagAttributeValueByName(@NotNull SmartyTag tag, @NotNull String attribute) {
        PsiElement psiAttribute = getTagAttributeByName(tag, attribute);
        if(psiAttribute == null) {
            return null;
        }

        PsiElement nextSibling = PhpPsiUtil.getNextSibling(psiAttribute, (Condition<PsiElement>) psiElement -> {
            IElementType elementType = psiElement.getNode().getElementType();

            return psiElement instanceof PsiWhiteSpace ||
                elementType == SmartyTokenTypes.EQ ||
                elementType == SmartyTokenTypes.DOUBLE_QUOTE ||
                elementType == SmartyTokenTypes.SINGLE_QUOTE;
        });

        if(nextSibling == null) {
            return null;
        }

        String text = nextSibling.getText();
        if(StringUtils.isNotBlank(text)) {
            return text;
        }

        return null;
    }

    /**
     * Find snippet namespace by tag scope and with file scope fallback
     *
     * {s namespace="foobar"}
     * {namespace="foobar"}
     */
    @Nullable
    public static String getSnippetNamespaceByScope(@NotNull SmartyTag smartyTag) {
        String namespace = TemplateUtil.getTagAttributeValueByName(smartyTag, "namespace");
        if(namespace != null) {
            return namespace;
        }

        PsiFile containingFile = smartyTag.getContainingFile();
        if(containingFile instanceof SmartyFile) {
            return SnippetUtil.getFileNamespace((SmartyFile) containingFile);
        }

        return null;
    }

    public static String findControllerModuleFromTagContext(@NotNull PsiElement psiElement) {
        String module = null;
        PsiElement smartyTag = psiElement.getParent();
        if (smartyTag instanceof SmartyTag) {
            String moduleValue = TemplateUtil.getTagAttributeValueByName((SmartyTag) smartyTag, "module");
            if (StringUtils.isNotBlank(moduleValue)) {
                module = moduleValue;
            }
        }

        return module != null ? module : "Widgets";
    }
}
