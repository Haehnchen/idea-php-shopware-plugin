package de.espend.idea.shopware.util;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.jetbrains.smarty.lang.SmartyTokenTypes;
import com.jetbrains.smarty.lang.psi.SmartyCompositeElementTypes;


public class SmartyPattern {

    public static PsiElementPattern.Capture<PsiElement> getFilePattern() {
        // getName dont work
        return PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL).withParent(
            PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE_VALUE).withParent(
                PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE).withText(PlatformPatterns.string().contains("file="))
            )
        );
    }

}
