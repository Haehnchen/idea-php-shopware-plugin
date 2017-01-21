package de.espend.idea.shopware.util;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.smarty.lang.SmartyTokenTypes;
import com.jetbrains.smarty.lang.psi.SmartyCompositeElementTypes;
import org.jetbrains.annotations.NotNull;

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

    public static PsiElementPattern.Capture<PsiElement> getFileIncludePattern() {
        return PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL).withParent(
            PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE_VALUE).withParent(
                PlatformPatterns.psiElement(SmartyCompositeElementTypes.ATTRIBUTE).withText(PlatformPatterns.string().contains("file=")).withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.INCLUDE_TAG)
                )
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

    public static PsiElementPattern.Capture<PsiElement> getConfigPattern() {
        return PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL)
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(SmartyTokenTypes.EQ),
                    PlatformPatterns.psiElement(SmartyTokenTypes.WHITE_SPACE),
                    PlatformPatterns.psiElement(SmartyTokenTypes.SINGLE_QUOTE),
                    PlatformPatterns.psiElement(SmartyTokenTypes.DOUBLE_QUOTE)
                ),
                PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).withText("name")
            )
            .withParent(
                PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{config"))
            );
    }

    public static ElementPattern<PsiElement> getControllerPattern() {
        return getControllerPattern("url");
    }

    public static ElementPattern<PsiElement> getControllerPattern(String tagName) {
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
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{" + tagName))
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
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{" + tagName))
                )

        );


    }

    public static ElementPattern<PsiElement> getControllerActionPattern() {
        return getControllerActionPattern("url");
    }

    public static ElementPattern<PsiElement> getControllerActionPattern(String tagName) {
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
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{" + tagName))
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
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{" + tagName))
                )

        );


    }

    /**
     * {tag attribute="<caret>"}
     */
    public static ElementPattern<PsiElement> getTagAttributePattern(@NotNull String tag, @NotNull String attribute) {
        return PlatformPatterns.or(
            PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(SmartyTokenTypes.EQ),
                        PlatformPatterns.psiElement(SmartyTokenTypes.WHITE_SPACE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.SINGLE_QUOTE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.DOUBLE_QUOTE)
                    ),
                    PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).withText(attribute)
                )
                .withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{" + tag))
                ),

            PlatformPatterns.psiElement(SmartyTokenTypes.STRING_LITERAL)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(SmartyTokenTypes.EQ),
                        PlatformPatterns.psiElement(SmartyTokenTypes.WHITE_SPACE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.SINGLE_QUOTE),
                        PlatformPatterns.psiElement(SmartyTokenTypes.DOUBLE_QUOTE)
                    ),
                    PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).withText(attribute)
                )
                .withParent(
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{s"))
                )
        );
    }

    public static ElementPattern<PsiElement> getNamespacePattern() {
        return getTagAttributePattern("s", "namespace");
    }

    public static PsiElementPattern.Capture<PsiElement> getVariableReference() {
        return PlatformPatterns.psiElement().afterLeaf(
            PlatformPatterns.psiElement(SmartyTokenTypes.DOLLAR)
        );
    }

    /**
     * foobar="asas"
     */
    public static PsiElementPattern.Capture<PsiElement> getAttributeKeyPattern() {
        return PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).beforeLeafSkipping(
            PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement((SmartyTokenTypes.EQ))
        );
    }
}
