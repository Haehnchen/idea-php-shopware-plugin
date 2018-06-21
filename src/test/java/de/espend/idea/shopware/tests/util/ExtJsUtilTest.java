package de.espend.idea.shopware.tests.util;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;
import de.espend.idea.shopware.util.ExtJsUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.util.ExtJsUtil
 */
public class ExtJsUtilTest extends ShopwareLightCodeInsightFixtureTestCase {
    /**
     * @see ExtJsUtil#getSnippetNamespaceFromFile
     */
    public void testGetSnippetNamespaceFromFile() {
        String[] foo = {
            "//{namespace name=backend/index/view/widgets}",
            "//{namespace name = backend/index/view/widgets}",
            "//{namespace foobar='aaaa' name='backend/index/view/widgets' }",
            "//{namespace name=\"backend/index/view/widgets\" }",
        };

        for (String s : foo) {
            PsiFile fileFromText = PsiFileFactory.getInstance(getProject())
                .createFileFromText("foo.js", JavaScriptFileType.INSTANCE, s);

            assertEquals("backend/index/view/widgets", ExtJsUtil.getSnippetNamespaceFromFile(fileFromText));
        }
    }

    /**
     * @see ExtJsUtil#getAttributeTagValueFromSmartyString
     */
    public void testGetAttributeTagValueFromSmartyString() {
        assertEquals(
            "backend/index/view/widgets",
            ExtJsUtil.getAttributeTagValueFromSmartyString("s", "name", "{s name=backend/index/view/widgets}")
        );

        assertEquals(
            "backend/index/view/widgets",
            ExtJsUtil.getAttributeTagValueFromSmartyString("s", "name", "{s foobar=\"\" name=\"backend/index/view/widgets\" foo}")
        );
    }

    /**
     * @see ExtJsUtil#getNamespaceFromStringLiteral
     */
    public void testGetNamespaceFromStringLiteral() {
        myFixture.configureByText(
            JavaScriptFileType.INSTANCE,
            "var foo = { foo: \"{s name=backend/inde<caret>x/view/widgets namespace='foobar'}\"}"
        );

        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();

        assertEquals("foobar", ExtJsUtil.getNamespaceFromStringLiteral((JSLiteralExpression) psiElement));
    }
}
