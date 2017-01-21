package de.espend.idea.shopware.tests.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.smarty.SmartyFileType;
import com.jetbrains.smarty.lang.psi.SmartyTag;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;
import de.espend.idea.shopware.util.TemplateUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.util.TemplateUtil
 */
public class TemplateUtilTest extends ShopwareLightCodeInsightFixtureTestCase {
    /**
     * @see TemplateUtil#getTagAttributeValueByName
     */
    public void testGetTagAttributeValueByName() {
        myFixture.configureByText(SmartyFileType.INSTANCE, "{s name='foo<caret>bar'}");
        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertEquals("foobar", TemplateUtil.getTagAttributeValueByName((SmartyTag) psiElement.getParent(), "name"));

        myFixture.configureByText(SmartyFileType.INSTANCE, "{s name = 'foo<caret>bar'}");
        psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertEquals("foobar", TemplateUtil.getTagAttributeValueByName((SmartyTag) psiElement.getParent(), "name"));

    }
}
