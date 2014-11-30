package de.espend.idea.shopware.inspection.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class CreateMethodQuickFix implements LocalQuickFix {

    private final MethodReference reference;
    private final PhpClass phpClass;
    private final String contents;

    public CreateMethodQuickFix(MethodReference reference, PhpClass phpClass, String contents) {
        this.reference = reference;
        this.phpClass = phpClass;
        this.contents = contents;
    }

    @NotNull
    @Override
    public String getName() {
        return "Create Method";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Method";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {

        Method method = PsiTreeUtil.getParentOfType(reference, Method.class);
        if(method == null) {
            return;
        }

        int insertPos = method.getTextRange().getEndOffset();
        Method methodCreated = PhpCodeUtil.createMethodFromTemplate(phpClass, phpClass.getProject(), "public function " + contents + "(Enlight_Event_EventArgs $args) {}");
        if(methodCreated == null) {
            return;
        }

        StringBuffer textBuf = new StringBuffer();
        textBuf.append("\n");
        textBuf.append(methodCreated.getText());

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if(editor == null) {
            return;
        }

        editor.getDocument().insertString(insertPos, textBuf);
        int endPos = insertPos + textBuf.length();
        CodeStyleManager.getInstance(project).reformatText(reference.getContainingFile(), insertPos, endPos);
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

    }

}
