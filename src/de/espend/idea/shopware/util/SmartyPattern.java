package de.espend.idea.shopware.util;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.jetbrains.smarty.lang.SmartyTokenTypes;
import com.jetbrains.smarty.lang.psi.SmartyCompositeElementTypes;


public class SmartyPattern {

    public static String[] TAG_LINK_FILE_EXTENSIONS = {"css", "js", "jpg", "jpeg", "gif", "png", "less", "sass"};

    public static PsiElementPattern.Capture<PsiElement> getFilePattern() {
        // getName dont work
        return PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL).withParent(
            PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE_VALUE).withParent(
                PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE).withText(PlatformPatterns.string().contains("file="))
            )
        );
    }

    public static PsiElementPattern.Capture<PsiElement> getBlockPattern() {
        return PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL).withParent(
            PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE_VALUE).withParent(
                PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE).withText(PlatformPatterns.string().contains("name=")).withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{block"))
                )
            )
        );
    }

    public static PsiElementPattern.Capture<PsiElement> getExtendPattern() {
        return PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL).withParent(
            PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE_VALUE).withParent(
                PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE).withText(PlatformPatterns.string().contains("file=")).withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{extends"))
                )
            )
        );
    }

    public static PsiElementPattern.Capture<PsiElement> getLinkFilePattern() {
        return PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL)
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(SmartyTokenTypes.EQ),
                    PlatformPatterns.psiElement(SmartyTokenTypes.WHITE_SPACE),
                    PlatformPatterns.psiElement(SmartyTokenTypes.SINGLE_QUOTE),
                    PlatformPatterns.psiElement(SmartyTokenTypes.DOUBLE_QUOTE)
                ),
                PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).withText("file")
            )
            .withParent(
                PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{link"))
            );
    }

    public static ElementPattern<PsiElement> getControllerPattern() {
        return PlatformPatterns.or(

            PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(SmartyTokenTypes.EQ),
                        PlatformPatterns.psiElement(SmartyTokenTypes.WHITE_SPACE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.SINGLE_QUOTE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.DOUBLE_QUOTE)
                    ),
                    PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).withText("controller")
                )
                .withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{url"))
                ),

            PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(SmartyTokenTypes.EQ),
                        PlatformPatterns.psiElement(SmartyTokenTypes.WHITE_SPACE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.SINGLE_QUOTE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.DOUBLE_QUOTE)
                    ),
                    PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).withText("controller")
                )
                .withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{url"))
                )

        );


    }


    public static ElementPattern<PsiElement> getControllerActionPattern() {
        return PlatformPatterns.or(

            PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(SmartyTokenTypes.EQ),
                        PlatformPatterns.psiElement(SmartyTokenTypes.WHITE_SPACE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.SINGLE_QUOTE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.DOUBLE_QUOTE)
                    ),
                    PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).withText("action")
                )
                .withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{url"))
                ),

            PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(SmartyTokenTypes.EQ),
                        PlatformPatterns.psiElement(SmartyTokenTypes.WHITE_SPACE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.SINGLE_QUOTE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.DOUBLE_QUOTE)
                    ),
                    PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).withText("action")
                )
                .withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{url"))
                )

        );


    }

}
