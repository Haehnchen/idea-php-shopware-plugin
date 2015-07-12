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
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.inspection.quickfix.CreateMethodQuickFix;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareSubscriperMethodInspection extends LocalInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {

        PsiFile psiFile = holder.getFile();

        psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(element instanceof Method) {
                    visitMethod((Method) element);
                }
                super.visitElement(element);
            }

            private void visitMethod(Method method) {

                if(!"getSubscribedEvents".equals(method.getName())) {
                    return;
                }

                PhpClass phpClass = method.getContainingClass();
                if(phpClass == null) {
                    return;
                }

                Collection<PhpReturn> phpReturns = PsiTreeUtil.collectElementsOfType(method, PhpReturn.class);
                for(PhpReturn phpReturn: phpReturns) {
                    PhpPsiElement arrayCreation = phpReturn.getFirstPsiChild();
                    if(arrayCreation instanceof ArrayCreationExpression) {
                        Collection<ArrayHashElement> arrayHashElements = PsiTreeUtil.collectElementsOfType(arrayCreation, ArrayHashElement.class);
                        for(ArrayHashElement arrayHash: arrayHashElements) {
                            PsiElement arrayValue = arrayHash.getValue();
                            if(arrayValue instanceof StringLiteralExpression) {
                                attachProblems(phpClass, (StringLiteralExpression) arrayValue);
                            }
                        }
                    }

                }


            }

            public void attachProblems(PhpClass phpClass, StringLiteralExpression psiElement) {


                final String contents = psiElement.getContents();
                if(StringUtils.isBlank(contents)) {
                    return;
                }

                Method method = phpClass.findMethodByName(contents);
                if(method != null) {
                    return;
                }

                holder.registerProblem(psiElement, "Create Function", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new CreateMethodQuickFix(reference, phpClass, contents));

            }

        });


        String name = psiFile.getName();
        if(!name.contains("Bootstrap")) {
            return super.buildVisitor(holder, isOnTheFly);
        }

        psiFile.acceptChildren(new MyPsiRecursiveElementWalkingVisitor(holder));
        return super.buildVisitor(holder, isOnTheFly);
    }

    private static class MyPsiRecursiveElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {
        private final ProblemsHolder holder;

        public MyPsiRecursiveElementWalkingVisitor(ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitElement(PsiElement element) {
            if(element instanceof MethodReference && "subscribeEvent".equals(((MethodReference) element).getName())) {
                visitMethodReference((MethodReference) element);
            }
            super.visitElement(element);
        }

        public void visitMethodReference(final MethodReference reference) {

            if(reference.getParameters().length < 2 || !(reference.getParameters()[1] instanceof StringLiteralExpression)) {
                return;
            }

            StringLiteralExpression literalExpression = (StringLiteralExpression) reference.getParameters()[1];
            final PhpClass phpClass = PsiTreeUtil.getParentOfType(reference, PhpClass.class);
            if(phpClass == null) {
                return;
            }

            final String contents = literalExpression.getContents();
            if(StringUtils.isBlank(contents)) {
                return;
            }

            Method method = phpClass.findMethodByName(contents);
            if(method != null) {
                return;
            }

            holder.registerProblem(literalExpression, "Create Function", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new CreateMethodQuickFix(reference, phpClass, contents));

        }

    }
}
