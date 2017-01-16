package de.espend.idea.shopware.inspection.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import de.espend.idea.shopware.index.utils.SubscriberIndexUtil;
import de.espend.idea.shopware.reference.LazySubscriberReferenceProvider;
import de.espend.idea.shopware.util.HookSubscriberUtil;
import de.espend.idea.shopware.util.ShopwareUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class CreateMethodQuickFix implements LocalQuickFix {

    private final StringLiteralExpression context;
    private final GeneratorContainer generatorContainer;

    public CreateMethodQuickFix(StringLiteralExpression context, GeneratorContainer generatorContainer) {
        this.context = context;
        this.generatorContainer = generatorContainer;
    }

    @NotNull
    @Override
    public String getName() {
        return "Create method";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Method";
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor problemDescriptor) {

        Method method = PsiTreeUtil.getParentOfType(context, Method.class);
        if(method == null) {
            return;
        }

        int insertPos = method.getTextRange().getEndOffset();

        // Enlight_Controller_Action_PostDispatch_Frontend_Blog
        String typeHint = "Enlight_Event_EventArgs";
        if(generatorContainer.getHookName() != null && generatorContainer.getHookName().contains("::")) {
            // Enlight_Controller_Action::dispatch::replace
            typeHint = "Enlight_Hook_HookArgs";
        }

        PsiFile containingFile = method.getContainingFile();
        if(containingFile instanceof PhpFile && PhpCodeInsightUtil.collectNamespaces((PhpFile) containingFile).size() > 0) {
            typeHint = "\\" + typeHint;
        }

        final StringBuilder stringBuilder = new StringBuilder();
        final String contents = context.getContents();
        stringBuilder.append("public function ").append(contents).append("(").append(typeHint).append(" $args) {");

        String subjectDoc = generatorContainer.getSubjectDoc();

        // find subject on controller "Enlight_Controller_Action_PostDispatchSecure_Frontend_Payment"
        if(subjectDoc == null && generatorContainer.getHookName() != null && !generatorContainer.getHookName().contains("::")) {
            PhpClass phpClass = ShopwareUtil.getControllerOnActionSubscriberName(project, generatorContainer.getHookName());
            if(phpClass != null) {
                subjectDoc = phpClass.getFQN();
            }
        }

        if(subjectDoc == null && generatorContainer.getHookName() != null && SubscriberIndexUtil.isContainerServiceEventAndContains(project, generatorContainer.getHookName())) {
            subjectDoc = "Shopware\\Components\\DependencyInjection\\Container";
        }

        if(subjectDoc != null) {
            stringBuilder.append("/** @var \\").append(StringUtils.stripStart(subjectDoc, "\\")).append(" $subject */\n");
            stringBuilder.append("$subject = $args->getSubject();\n");
        }

        if(generatorContainer.getHookName() != null && generatorContainer.getHookName().contains("::")) {
            stringBuilder.append("\n");
            stringBuilder.append("$return = $args->getReturn();\n");

            attachVariablesInScope(project, stringBuilder);

            stringBuilder.append("\n");
            stringBuilder.append("$args->setReturn($return);\n");
        } else {
            attachVariablesInScope(project, stringBuilder);
        }

        if(generatorContainer.getHookName() != null) {

            Pattern pattern = Pattern.compile("Enlight_Controller_Dispatcher_ControllerPath_(Frontend|Backend|Widget|Api)_(\\w+)");
            Matcher matcher = pattern.matcher(generatorContainer.getHookName());

            if(matcher.find()) {
                stringBuilder.append("\n");
                stringBuilder.append(String.format("return $this->Path() . 'Controllers/%s/%s.php';", matcher.group(1), matcher.group(2)));
                stringBuilder.append("\n");
            }

        }


        stringBuilder.append("}");

        PhpClass phpClass = method.getContainingClass();

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
        CodeStyleManager.getInstance(project).reformatText(phpClass.getContainingFile(), insertPos, endPos);
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

        Method insertedMethod = phpClass.findMethodByName(contents);
        if(insertedMethod != null) {
            editor.getCaretModel().moveToOffset(insertedMethod.getTextRange().getStartOffset());
            editor.getScrollingModel().scrollToCaret(ScrollType.CENTER_UP);
        }

    }

    private void attachVariablesInScope(@NotNull Project project, StringBuilder stringBuilder) {

        // Events
        if(HookSubscriberUtil.NOTIFY_EVENTS_MAP.containsKey(generatorContainer.getHookName())) {
            Collection<String> references = HookSubscriberUtil.NOTIFY_EVENTS_MAP.get(generatorContainer.getHookName());
            for (String value : references) {
                String[] split = value.split("\\.");
                Method classMethod = PhpElementsUtil.getClassMethod(project, split[0], split[1]);
                if(classMethod == null) {
                    continue;
                }

                buildEventVariables(project, stringBuilder, classMethod);
            }
        } else if(generatorContainer.getHookMethod() != null) {
            // add hook parameter
            Parameter[] hookMethodParameters = generatorContainer.getHookMethod().getParameters();
            if(hookMethodParameters.length > 0 ) {
                stringBuilder.append("\n");
                for(Parameter parameter : hookMethodParameters) {
                    String name = parameter.getName();
                    stringBuilder.append("$").append(name).append(" = ").append("$args->get('").append(name).append("');\n");
                }
            }
        }
    }

    private void buildEventVariables(@NotNull final Project project, final StringBuilder stringBuilder, Method classMethod) {
        classMethod.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if ((element instanceof StringLiteralExpression) && ((StringLiteralExpression) element).getContents().equals(generatorContainer.getHookName())) {
                    PsiElement parent = element.getParent();
                    if(parent instanceof ParameterList) {
                        PsiElement[] parameterList = ((ParameterList) parent).getParameters();
                        if(parameterList.length > 1) {
                            if(parameterList[1] instanceof ArrayCreationExpression) {
                                Map<String, PsiElement> eventParameters = PhpElementsUtil.getArrayCreationKeyMap((ArrayCreationExpression) parameterList[1]);
                                for(Map.Entry<String, PsiElement> entrySet : eventParameters.entrySet()) {
                                    stringBuilder.append("\n");
                                    PhpPsiElement psiElement = PhpElementsUtil.getArrayValue((ArrayCreationExpression) parameterList[1], entrySet.getKey());
                                    if(psiElement instanceof PhpTypedElement) {

                                        Set<String> classes = new HashSet<>();

                                        PhpType type = ((PhpTypedElement) psiElement).getType();
                                        for (PhpClass aClass : PhpElementsUtil.getClassFromPhpTypeSet(project, type.getTypes())) {
                                            // force absolute namespace
                                            classes.add("\\" + StringUtils.stripStart(aClass.getPresentableFQN(), "\\"));
                                        }

                                        if(classes.size() > 0) {
                                            stringBuilder.append("/** @var ").append(StringUtils.join(classes, "|")).append("$").append(entrySet.getKey()).append(" */\n");
                                        }
                                    }
                                    stringBuilder.append("$").append(entrySet.getKey()).append(" = ").append("$args->get('").append(entrySet.getKey()).append("');\n");
                                }
                            }
                        }
                    }
                }
                super.visitElement(element);
            }
        });
    }

    @Nullable
    public static PsiElement getSubjectTargetOnHook(Project project, final String contents) {

        for(PsiElement psiElement : LazySubscriberReferenceProvider.getHookTargets(project, contents)) {
            if(psiElement instanceof Method) {
                return psiElement;
            } else if(psiElement instanceof PhpClass) {
                return psiElement;
            }
        }

        if(HookSubscriberUtil.NOTIFY_EVENTS_MAP.containsKey(contents)) {
            Collection<String> references = HookSubscriberUtil.NOTIFY_EVENTS_MAP.get(contents);
            for (String value : references) {
                String[] split = value.split("\\.");
                Method classMethod = PhpElementsUtil.getClassMethod(project, split[0], split[1]);
                if(classMethod == null) {
                    continue;
                }

                return classMethod;
            }
        }


        return null;
    }

    public static class GeneratorContainer {

        private String hookName = null;
        private String subjectDoc = null;
        private Method hookMethod = null;

        public GeneratorContainer(String subjectDoc, Method hookMethod, String hookName) {
            this.subjectDoc = subjectDoc;
            this.hookMethod = hookMethod;
            this.hookName = hookName;
        }

        public String getHookName() {
            return hookName;
        }

        public String getSubjectDoc() {
            return subjectDoc;
        }

        public Method getHookMethod() {
            return hookMethod;
        }

    }

}
