package de.espend.idea.shopware.util;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.smarty.SmartyFileType;
import com.jetbrains.smarty.lang.psi.SmartyTag;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemplateUtil {

    public static void collectFiles(Project project, SmartyTemplateVisitor smartyTemplateVisitor) {
        collectFiles(project, smartyTemplateVisitor, "tpl");
    }

    public static void collectFiles(Project project, final SmartyTemplateVisitor smartyTemplateVisitor, String... extensions) {

        final List<String> exts = Arrays.asList(extensions);
        final List<VirtualFile> uniqueVirtualFiles = new ArrayList<VirtualFile>();

        collectPluginTemplates(project, smartyTemplateVisitor, exts);

        // search for index files; think of lib and include path
        List<LanguageFileType> languageFileTypes = new ArrayList<LanguageFileType>();

        if(exts.contains("tpl")) {
            languageFileTypes.add(SmartyFileType.INSTANCE);
        }

        if(exts.contains("js")) {
            languageFileTypes.add(JavaScriptSupportLoader.JAVASCRIPT);
        }

        // sw5: provides parent class for themes
        Set<VirtualFile> themes = new HashSet<VirtualFile>();
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
    public static String getTemplateName(Project project, VirtualFile virtualFile) {
        return getTemplateName(project, virtualFile, "frontend", "backend", "widgets");
    }

    @Nullable
    public static String getTemplateName(Project project, VirtualFile virtualFile, String... modules) {

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

}
