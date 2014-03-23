package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.SmartyBlockUtil;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.TemplateUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartyBlockGoToHandler implements GotoDeclarationHandler {
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {

        if(!ShopwareProjectComponent.isValidForProject(sourceElement)) {
            return new PsiElement[0];
        }

        if(!SmartyPattern.getBlockPattern().accepts(sourceElement)) {
            return new PsiElement[0];
        }

        final Map<VirtualFile, String> map = new HashMap<VirtualFile, String>();

        TemplateUtil.collectFiles(sourceElement.getProject(), new TemplateUtil.SmartyTemplateVisitor() {
            @Override
            public void visitFile(VirtualFile virtualFile, String fileName) {
                map.put(virtualFile, fileName);
            }
        });

        List<SmartyBlockUtil.SmartyBlock> blockNameSet = new ArrayList<SmartyBlockUtil.SmartyBlock>();
        SmartyBlockUtil.collectFileBlocks(sourceElement.getContainingFile(), map, blockNameSet, 5);


        final List<PsiElement> psiTargets = new ArrayList<PsiElement>();

        for(SmartyBlockUtil.SmartyBlock smartyBlock: blockNameSet) {
            if(smartyBlock.getName().equals(sourceElement.getText())) {
                psiTargets.add(smartyBlock.getElement());
            }
        }

        return psiTargets.toArray(new PsiElement[psiTargets.size()]);
    }

    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

}
