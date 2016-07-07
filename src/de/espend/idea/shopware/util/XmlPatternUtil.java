package de.espend.idea.shopware.util;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTokenType;
import fr.adrienbrault.idea.symfony2plugin.config.xml.XmlHelper;

public class XmlPatternUtil {

    /**
     * <controller>SwagBackendExample</controller>
     */
    public static PsiElementPattern.Capture<PsiElement> getMenuControllerPattern() {
        return XmlPatterns
                .psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                .withParent(XmlPatterns
                        .xmlText()
                        .withParent(XmlPatterns
                                .xmlTag()
                                .withName("controller")
                        )
                ).inside(XmlHelper.getInsideTagPattern("menu"));
    }
}
