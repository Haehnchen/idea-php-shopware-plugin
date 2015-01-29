package de.espend.idea.shopware.inspection.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.reference.EventSubscriberReferenceContributor;
import de.espend.idea.shopware.reference.LazySubscriberReferenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class CreateMethodQuickFix implements LocalQuickFix {

    private final MethodReference methodReference;
    private final PhpClass phpClass;
    private final String contents;

    public CreateMethodQuickFix(MethodReference methodReference, PhpClass phpClass, String contents) {
        this.methodReference = methodReference;
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

        Method method = PsiTreeUtil.getParentOfType(methodReference, Method.class);
        if(method == null) {
            return;
        }

        PsiElement[] parameters = methodReference.getParameters();
        String subjectDoc = null;
        Method hookMethod = null;
        String hookName = null;
        if(parameters.length > 1 && parameters[0] instanceof StringLiteralExpression) {
            hookName = ((StringLiteralExpression) parameters[0]).getContents();
            PsiElement subjectTarget = getSubjectTargetOnHook(project, hookName);
            if(subjectTarget instanceof PhpClass) {
                subjectDoc = ((PhpClass) subjectTarget).getPresentableFQN();
            } else if(subjectTarget instanceof Method) {
                hookMethod = (Method) subjectTarget;
                PhpClass containingClass = ((Method) subjectTarget).getContainingClass();
                if(containingClass != null) {
                    subjectDoc = containingClass.getPresentableFQN();
                }
            }
        }

        int insertPos = method.getTextRange().getEndOffset();

        // Enlight_Controller_Action_PostDispatch_Frontend_Blog
        String typeHint = "Enlight_Event_EventArgs";
        if(hookName != null && hookName.contains("::")) {
            // Enlight_Controller_Action::dispatch::replace
            typeHint = "Enlight_Hook_HookArgs";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public function ").append(contents).append("(").append(typeHint).append(" $args) {");
        if(subjectDoc != null) {
            stringBuilder.append("\n");
            stringBuilder.append("/** @var ").append(subjectDoc).append(" $subject */\n");
            stringBuilder.append("$subject = $args->getSubject();\n");

            if(hookName != null && hookName.contains("::")) {
                stringBuilder.append("\n");
                stringBuilder.append("$return = $args->getReturn();\n");

                // add hook parameter
                if(hookMethod != null) {
                    Parameter[] hookMethodParameters = hookMethod.getParameters();
                    if(hookMethodParameters.length > 0 ) {
                        stringBuilder.append("\n");
                        for(Parameter parameter : hookMethodParameters) {
                            String name = parameter.getName();
                            stringBuilder.append("$").append(name).append(" = ").append("$args->get('").append(name).append("');\n");
                        }
                    }
                }

                stringBuilder.append("\n");
                stringBuilder.append("$args->setReturn($return);\n");
            }

            stringBuilder.append("\n");
        }

        if(hookName != null) {

            Pattern pattern = Pattern.compile("Enlight_Controller_Dispatcher_ControllerPath_(Frontend|Backend|Widget|Api)_(\\w+)");
            Matcher matcher = pattern.matcher(hookName);

            if(matcher.find()) {
                stringBuilder.append("\n");
                stringBuilder.append(String.format("return $this->Path() . 'Controllers/%s/%s.php';", matcher.group(1), matcher.group(2)));
                stringBuilder.append("\n");
            }

        }


        stringBuilder.append("}");

        Method methodCreated = PhpCodeUtil.createMethodFromTemplate(phpClass, phpClass.getProject(), stringBuilder.toString());
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
        CodeStyleManager.getInstance(project).reformatText(methodReference.getContainingFile(), insertPos, endPos);
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

        Method insertedMethod = phpClass.findMethodByName(contents);
        if(insertedMethod != null) {
            editor.getCaretModel().moveToOffset(insertedMethod.getTextRange().getStartOffset());
            editor.getScrollingModel().scrollToCaret(ScrollType.CENTER_UP);
        }

    }

    @Nullable
    private PsiElement getSubjectTargetOnHook(Project project, final String contents) {

        for(PsiElement psiElement : LazySubscriberReferenceProvider.getHookTargets(project, contents)) {
            if(psiElement instanceof Method) {
                return psiElement;
            } else if(psiElement instanceof PhpClass) {
                return psiElement;
            }
        }

        return null;
    }

}
