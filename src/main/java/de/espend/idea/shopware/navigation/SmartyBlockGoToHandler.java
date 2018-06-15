package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.index.SmartyBlockStubIndex;
import de.espend.idea.shopware.util.SmartyBlockUtil;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.TemplateUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
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

        final List<PsiElement> psiTargets = new ArrayList<>();

        PsiFile containingFile = sourceElement.getContainingFile();
        if(TemplateUtil.isExtendsTemplate(containingFile)) {
            attachExtendsTemplateGoto(sourceElement, containingFile, psiTargets);
        } else {
            attachIncludeTemplateGoto(sourceElement, containingFile, psiTargets);
        }

        return psiTargets.toArray(new PsiElement[0]);
    }

    public void attachIncludeTemplateGoto(final PsiElement sourceElement, final PsiFile psiFile, final List<PsiElement> psiTargets) {

        final String text = sourceElement.getText();

        FileBasedIndexImpl.getInstance().getFilesWithKey(SmartyBlockStubIndex.KEY, new HashSet<>(Collections.singletonList(text)), virtualFile -> {

            if(psiFile.getVirtualFile().equals(virtualFile)) {
                return true;
            }

            PsiFile psiFile1 = PsiManager.getInstance(sourceElement.getProject()).findFile(virtualFile);
            if(psiFile1 != null) {
                psiTargets.addAll(getBlockPsiElement(psiFile1, text));
            }

            return true;
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(psiFile.getProject()), SmartyFileType.INSTANCE));


    }

    public static List<PsiElement> getBlockPsiElement(PsiFile psiFile, final String blockName) {
        final List<PsiElement> psiElements = new ArrayList<>();

        psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {

                if(SmartyPattern.getBlockPattern().accepts(element)) {
                    String text = element.getText();
                    if(blockName.equalsIgnoreCase(text)) {
                        psiElements.add(element);
                    }

                }

                super.visitElement(element);
            }
        });

        return psiElements;
    }

    public void attachExtendsTemplateGoto(PsiElement sourceElement, PsiFile psiFile, List<PsiElement> psiTargets) {

        final Map<VirtualFile, String> map = new HashMap<>();

        TemplateUtil.collectFiles(sourceElement.getProject(), new TemplateUtil.SmartyTemplatePreventSelfVisitor(psiFile.getVirtualFile()) {
            @Override
            public void visitNonSelfFile(VirtualFile virtualFile, String fileName) {
                map.put(virtualFile, fileName);
            }
        });

        List<SmartyBlockUtil.SmartyBlock> blockNameSet = new ArrayList<>();
        SmartyBlockUtil.collectFileBlocks(sourceElement.getContainingFile(), map, blockNameSet, 5);


        for(SmartyBlockUtil.SmartyBlock smartyBlock: blockNameSet) {
            if(smartyBlock.getName().equals(sourceElement.getText())) {
                psiTargets.add(smartyBlock.getElement());
            }
        }

    }

    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

}
