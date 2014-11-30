package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ExtJsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ExtJsGoToDeclarationHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {

        if(!ShopwareProjectComponent.isValidForProject(sourceElement)) {
            return new PsiElement[0];
        }

        final List<PsiElement> targets = new ArrayList<PsiElement>();

        // {link file='frontend/_resources/styles/framework.css'}
        if(ExtJsUtil.getStringLiteralPattern().accepts(sourceElement)) {
            attachControllerActionNameGoto(sourceElement, targets);
        }

        return targets.toArray(new PsiElement[targets.size()]);
    }

    private void attachControllerActionNameGoto(PsiElement sourceElement, final List<PsiElement> psiElements) {

        String text = PsiElementUtils.trimQuote(sourceElement.getText());
        if(text.startsWith("{") && text.endsWith("}")) {
            psiElements.addAll(ExtJsUtil.getControllerTargets(sourceElement, text));
        }

    }


    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

}
