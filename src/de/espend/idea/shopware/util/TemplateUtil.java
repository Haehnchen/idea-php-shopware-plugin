package de.espend.idea.shopware.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class TemplateUtil {

    public static void collectFiles(Project project, final SmartyTemplateVisitor smartyTemplateVisitor) {

        final VirtualFile baseDir = project.getBaseDir();
        final VirtualFile templateDir = VfsUtil.findRelativeFile("templates", baseDir);

        if(templateDir == null) {
            return;
        }

        VfsUtil.visitChildrenRecursively(templateDir, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile virtualFile) {

                if(virtualFile.isDirectory() || !virtualFile.getName().endsWith(".tpl")) {
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

    public interface SmartyTemplateVisitor {
        public void visitFile(VirtualFile virtualFile, String fileName);
    }
}
