package de.espend.idea.shopware.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.inspection.quickfix.CreateMethodQuickFix;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
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
