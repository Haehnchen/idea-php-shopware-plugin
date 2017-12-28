package de.espend.idea.shopware.util;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTokenType;
import fr.adrienbrault.idea.symfony2plugin.config.xml.XmlHelper;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
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

    /**
     * <parent identifiedBy="controller">SwagBackendExample</parent>
     */
    public static PsiElementPattern.Capture<PsiElement> getMenuControllerByParentPattern() {
        return XmlPatterns
            .psiElement(XmlTokenType.XML_DATA_CHARACTERS)
            .withParent(XmlPatterns
                .xmlText()
                .withParent(XmlPatterns
                    .xmlTag()
                    .withName("parent").withAttributeValue("identifiedBy", "controller")
                )
            ).inside(XmlHelper.getInsideTagPattern("menu"));
    }

    /**
     * <action>SwagBackendExample</action>
     */
    public static PsiElementPattern.Capture<PsiElement> getMenuControllerActionPattern() {
        return XmlPatterns
            .psiElement(XmlTokenType.XML_DATA_CHARACTERS)
            .withParent(XmlPatterns
                .xmlText()
                .withParent(XmlPatterns
                    .xmlTag()
                    .withName("action")
                )
            ).inside(XmlHelper.getInsideTagPattern("menu"));
    }
}
