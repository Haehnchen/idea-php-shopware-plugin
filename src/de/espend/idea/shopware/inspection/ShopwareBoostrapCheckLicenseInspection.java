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
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ShopwareBoostrapCheckLicenseInspection extends LocalInspectionTool {

    final private static String CHECK_LICENSE_METHOD_NAME = "checkLicense";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {

        final PsiFile psiFile = holder.getFile();

        /**
         * Only run inspection if the file is a bootstrap.php, then iterate over each element in class.
         */
        if ("Bootstrap.php".equals(psiFile.getName())) {
            psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    /**
                     * We only want elements that are methods, nothing else...
                     */
                    if (element instanceof Method) {

                        if (CHECK_LICENSE_METHOD_NAME.equals(((Method) element).getName())) {
                            /**
                             * Check if method is not empty.
                             */
                            String methodName = ((Method) element).getName();
                            if (PsiTreeUtil.collectElementsOfType(element, PhpReturn.class).size() == 0) {
                                PsiElement psiElement = PsiElementUtils.getChildrenOfType(element, PlatformPatterns.psiElement(PhpTokenTypes.IDENTIFIER).withText(methodName));
                                if (psiElement != null) {
                                    holder.registerProblem(psiElement, "This method must not be empty!", ProblemHighlightType.GENERIC_ERROR);
                                }
                            }

                            /**
                             * Check if checkLicense-method is actually called.
                             */
                            final int[] counterOfFoundCallsOfCheckLicense = {0};
                            final ArrayList<String> checkLicenseCalledFrom = new ArrayList<String>();

                            /**
                             * Count occurrences of calls and where it is called
                             */
                            psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                                public void visitElement(PsiElement element) {

                                    if (element instanceof MethodReference && CHECK_LICENSE_METHOD_NAME.equals(((MethodReference) element).getName())) {
                                        counterOfFoundCallsOfCheckLicense[0]++;
                                        Method method = PsiTreeUtil.getParentOfType(element, Method.class);
                                        if (method != null) {
                                            checkLicenseCalledFrom.add(method.getName());
                                        }
                                    }

                                    /**
                                     * Super must be called at the end.
                                     */
                                    super.visitElement(element);
                                }
                            });

                            /**
                             * Handle the situation when no call was found, or not from install()
                             */
                            if (0 == counterOfFoundCallsOfCheckLicense[0] || !checkLicenseCalledFrom.contains("install")) {
                                // todo: add english wiki page!!
                                String descriptionTemplate = "This method should be called at least once from install()-function.";
                                holder.registerProblem(element, descriptionTemplate, ProblemHighlightType.GENERIC_ERROR);
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
