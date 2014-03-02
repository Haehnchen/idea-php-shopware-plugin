package de.espend.idea.shopware.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SmartyBlockUtil {

    public static List<SmartyBlock> collectFileBlocks(PsiFile psiFile, final Map<VirtualFile, String> map, List<SmartyBlock> blockNameSet, int depth) {

        final List<VirtualFile> virtualFiles = new ArrayList<VirtualFile>();

        PsiTreeUtil.processElements(psiFile, new PsiElementProcessor() {
            @Override
            public boolean execute(@NotNull PsiElement element) {

                if (SmartyPattern.getExtendPattern().accepts(element)) {
                    String extendsName = element.getText();
                    for (Map.Entry<VirtualFile, String> entry : map.entrySet()) {
                        if (entry.getValue().equals(extendsName)) {
                            virtualFiles.add(entry.getKey());
                        }

                    }
                }

                return true;
            }
        });

        if(virtualFiles.size() == 0) {
            return blockNameSet;
        }

        // for recursive calls
        List<PsiFile> parentFiles = new ArrayList<PsiFile>();

        for(VirtualFile virtualFile: virtualFiles) {
            PsiFile parentPsiFile = PsiManager.getInstance(psiFile.getProject()).findFile(virtualFile);
            if(parentPsiFile != null) {
                parentFiles.add(parentPsiFile);
                blockNameSet.addAll(getFileBlocks(parentPsiFile));
            }
        }

        if(depth-- < 0) {
            return blockNameSet;
        }

        for(PsiFile parentFile: parentFiles) {
            collectFileBlocks(parentFile, map, blockNameSet, depth);
        }

        return blockNameSet;
    }

    public static Set<SmartyBlock> getFileBlocks(PsiFile psiFile) {

        final Set<SmartyBlock> blockNameSet = new HashSet<SmartyBlock>();

        PsiTreeUtil.processElements(psiFile, new PsiElementProcessor() {
            @Override
            public boolean execute(@NotNull PsiElement element) {

                if (SmartyPattern.getBlockPattern().accepts(element)) {
                    blockNameSet.add(new SmartyBlock(element, element.getText()));
                }

                return true;
            }
        });

        return blockNameSet;
    }


    public static class SmartyBlock {

        final private PsiElement element;
        final private String name;

        public SmartyBlock(PsiElement element, String name) {
            this.element = element;
            this.name = name;
        }

        public PsiElement getElement() {
            return element;
        }

        public String getName() {
            return name;
        }

    }
}
