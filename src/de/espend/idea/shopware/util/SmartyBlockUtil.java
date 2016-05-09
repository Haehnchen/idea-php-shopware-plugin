package de.espend.idea.shopware.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmartyBlockUtil {

    public static List<SmartyBlock> collectFileBlocks(PsiFile psiFile, final Map<VirtualFile, String> map, List<SmartyBlock> blockNameSet, int depth) {
        return collectFileBlocks(psiFile, map, blockNameSet, new ArrayList<>(), depth);
    }

    public static List<SmartyBlock> collectFileBlocks(PsiFile psiFile, final Map<VirtualFile, String> map, List<SmartyBlock> blockNameSet, List<VirtualFile> virtualFilesCatch, int depth) {

        final List<VirtualFile> virtualFiles = new ArrayList<>();

        PsiTreeUtil.processElements(psiFile, element -> {

            if (SmartyPattern.getExtendPattern().accepts(element)) {
                String extendsName = element.getText();
                if(extendsName.startsWith("parent:")) {
                    extendsName = extendsName.substring(7);
                }
                for (Map.Entry<VirtualFile, String> entry : map.entrySet()) {
                    if (entry.getValue().equals(extendsName)) {
                        virtualFiles.add(entry.getKey());
                    }

                }
            }

            return true;
        });

        if(virtualFiles.size() == 0) {
            return blockNameSet;
        }

        // for recursive calls
        List<PsiFile> parentFiles = new ArrayList<>();

        for(VirtualFile virtualFile: virtualFiles) {
            if(!virtualFilesCatch.contains(virtualFile)) {
                virtualFilesCatch.add(virtualFile);
                PsiFile parentPsiFile = PsiManager.getInstance(psiFile.getProject()).findFile(virtualFile);
                if(parentPsiFile != null) {
                    parentFiles.add(parentPsiFile);
                    blockNameSet.addAll(getFileBlocks(parentPsiFile));
                }
            }
        }

        if(depth-- < 0) {
            return blockNameSet;
        }

        for(PsiFile parentFile: parentFiles) {
            collectFileBlocks(parentFile, map, blockNameSet,virtualFilesCatch, depth);
        }

        return blockNameSet;
    }

    public static Set<SmartyBlock> getFileBlocks(PsiFile psiFile) {

        final Set<SmartyBlock> blockNameSet = new HashSet<>();

        PsiTreeUtil.processElements(psiFile, element -> {

            if (SmartyPattern.getBlockPattern().accepts(element)) {
                blockNameSet.add(new SmartyBlock(element, element.getText()));
            }

            return true;
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
