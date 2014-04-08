package de.espend.idea.shopware.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TemplateUtil {

    public static void collectFiles(Project project, SmartyTemplateVisitor smartyTemplateVisitor) {
        collectFiles(project, smartyTemplateVisitor, "tpl");
    }

    public static void collectFiles(Project project, final SmartyTemplateVisitor smartyTemplateVisitor, String... extensions) {

        final List<String> exts = Arrays.asList(extensions);
        final List<VirtualFile> uniqueVirtualFiles = new ArrayList<VirtualFile>();

        collectPluginTemplates(project, smartyTemplateVisitor, exts);

        final VirtualFile baseDir = project.getBaseDir();
        final VirtualFile templateDir = VfsUtil.findRelativeFile("templates", baseDir);

        if(templateDir == null) {
            return;
        }

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

                String[] pathSplits = StringUtils.split(frontendName, "/");
                if(pathSplits.length < 2 || !"frontend".equals(pathSplits[1])) {
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

                return true;
            }
        });
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
        public void visitFile(VirtualFile virtualFile, String fileName);
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

}
