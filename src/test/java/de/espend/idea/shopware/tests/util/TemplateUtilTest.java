package de.espend.idea.shopware.tests.util;

import com.intellij.openapi.vfs.VirtualFile;
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
    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/util/fixtures";
    }

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

    public void testTemplateNameResolving() {
        VirtualFile virtualFile1 = myFixture.copyFileToProject("namespace.tpl", "foo2/Resources/views/frontend/test/foobar/namespace2.tpl");
        myFixture.copyFileToProject("Plugin.php", "foo2/Plugin.php");

        VirtualFile virtualFile2 = myFixture.copyFileToProject("namespace.tpl", "foo2/Views/frontend/test/foobar/namespace3.tpl");
        myFixture.copyFileToProject("Bootstrap.php", "foo2/Bootstrap.php");

        VirtualFile virtualFile3 = myFixture.copyFileToProject("namespace.tpl", "foo4/Views/frontend/test/foobar/namespace3.tpl");
        VirtualFile virtualFile4 = myFixture.copyFileToProject("namespace.tpl", "templates/foo8/Views/frontend/test/foobar2/namespace3.tpl");

        assertEquals(
            "frontend/foobar/namespace.tpl",
            TemplateUtil.getTemplateName(getProject(), myFixture.copyFileToProject("namespace.tpl", "foobar/templates/emotion_blue/frontend/foobar/namespace.tpl"))
        );

        assertEquals(
            "frontend/test/foobar/namespace2.tpl",
            TemplateUtil.getTemplateName(getProject(), virtualFile1)
        );

        assertEquals(
            "frontend/test/foobar/namespace3.tpl",
            TemplateUtil.getTemplateName(getProject(), virtualFile2)
        );

        assertNull(TemplateUtil.getTemplateName(getProject(), virtualFile3));

        assertEquals(
            "frontend/test/foobar2/namespace3.tpl",
            TemplateUtil.getTemplateName(getProject(), virtualFile4)
        );
    }
}
