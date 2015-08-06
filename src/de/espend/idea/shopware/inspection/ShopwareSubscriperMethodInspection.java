package de.espend.idea.shopware.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.inspection.quickfix.CreateMethodQuickFix;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareSubscriperMethodInspection extends LocalInspectionTool {

    final private static Set<String> INSTALL_METHODS = new HashSet<String>(Arrays.asList("install", "uninstall", "update"));

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {

        PsiFile psiFile = holder.getFile();

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

            holder.registerProblem(literalExpression, "Create function", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new CreateMethodQuickFix(reference, phpClass, contents));

        }

    }
}
