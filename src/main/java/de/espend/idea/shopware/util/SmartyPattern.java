package de.espend.idea.shopware.util;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.smarty.lang.SmartyTokenTypes;
import com.jetbrains.smarty.lang.psi.SmartyCompositeElementTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
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

    public static ElementPattern<PsiElement> getLinkFilePattern() {
        return getTagAttributePattern("link", "file");
    }

    public static ElementPattern<PsiElement> getConfigPattern() {
        return getTagAttributePattern("config", "name");
    }

    public static ElementPattern<PsiElement> getUrlControllerPattern() {
        return getTagAttributePattern("url", "controller");
    }

    public static ElementPattern<PsiElement> getActionControllerPattern() {
        return getTagAttributePattern("action", "controller");
    }

    public static ElementPattern<PsiElement> getControllerActionPattern() {
        return getTagAttributePattern("url", "action");
    }

    public static ElementPattern<PsiElement> getActionActionPattern() {
        return getTagAttributePattern("action", "action");
    }

    /**
     * {tag attribute="<caret>"}
     * {tag attribute=<caret>}
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
                    PlatformPatterns.psiElement(SmartyCompositeElementTypes.TAG).withText(PlatformPatterns.string().startsWith("{" + tag))
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
    static PsiElementPattern.Capture<PsiElement> getAttributeKeyPattern() {
        return PlatformPatterns.psiElement(SmartyTokenTypes.IDENTIFIER).beforeLeafSkipping(
            PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement((SmartyTokenTypes.EQ))
        );
    }
}
