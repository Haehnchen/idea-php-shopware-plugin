package de.espend.idea.shopware.util.dict;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.Symfony2InterfacesUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

public class PsiParameterStorageRunnable implements Runnable {

    private final Project project;
    private final VirtualFile virtualFile;
    private final Set<String> events;
    private final Set<String> configs;

    public PsiParameterStorageRunnable(Project project, VirtualFile virtualFile, Set<String> events, Set<String> configs) {
        this.project = project;
        this.virtualFile = virtualFile;
        this.events = events;
        this.configs = configs;
    }

    public void run() {
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile != null) {
            psiFile.acceptChildren(new MyPsiRecursiveElementWalkingVisitor());
        }
    }

    private class MyPsiRecursiveElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {
        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof MethodReference) {
                visitMethodReference((MethodReference) element);
            }
            super.visitElement(element);
        }

        private void visitMethodReference(MethodReference methodReference) {
            String name = methodReference.getName();

            if (name != null && ("notify".equals(name) || "notifyUntil".equals(name))) {
                PsiElement[] parameters = methodReference.getParameters();
                if(parameters.length > 1) {
                    if(parameters[0] instanceof StringLiteralExpression) {

                        PsiElement method = methodReference.resolve();
                        if(method instanceof Method) {
                            PhpClass phpClass = ((Method) method).getContainingClass();
                            if(phpClass != null && new Symfony2InterfacesUtil().isInstanceOf(phpClass, "\\Enlight_Event_EventManager")) {
                                String content = ((StringLiteralExpression) parameters[0]).getContents();
                                if(StringUtils.isNotBlank(content)) {
                                    events.add(content);
                                }
                            }
                        }
                    }
                }
            }

            if (name != null  && ("addElement".equals(name) || "setElement".equals(name))) {
                PsiElement[] parameters = methodReference.getParameters();
                if(parameters.length > 2) {
                    if(parameters[1] instanceof StringLiteralExpression) {

                        PsiElement method = methodReference.resolve();
                        if(method instanceof Method) {
                            PhpClass phpClass = ((Method) method).getContainingClass();
                            if(phpClass != null && new Symfony2InterfacesUtil().isInstanceOf(phpClass, "\\Shopware\\Models\\Config\\Form")) {
                                String content = ((StringLiteralExpression) parameters[1]).getContents();
                                if(StringUtils.isNotBlank(content)) {
                                    configs.add(content);
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
