package de.espend.idea.shopware.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ShopwareBoostrapInspection extends LocalInspectionTool {

    final private static Set<String> INSTALL_METHODS = new HashSet<String>(Arrays.asList("install", "uninstall", "update"));

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {

        PsiFile psiFile = holder.getFile();

        if("Bootstrap.php".equals(psiFile.getName())) {
            psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if(element instanceof Method) {

                        String methodName = ((Method) element).getName();
                        if(INSTALL_METHODS.contains(((Method) element).getName())) {
                            if(PsiTreeUtil.collectElementsOfType(element, PhpReturn.class).size() == 0) {
                                PsiElement psiElement = PsiElementUtils.getChildrenOfType(element, PlatformPatterns.psiElement(PhpTokenTypes.IDENTIFIER).withText(methodName));
                                if(psiElement != null) {
                                    holder.registerProblem(psiElement, "Shopware need return statement", ProblemHighlightType.GENERIC_ERROR);
                                }
                            }
                        }

                    }
                    super.visitElement(element);
                }
            });

        }

        return super.buildVisitor(holder, isOnTheFly);
    }

}
