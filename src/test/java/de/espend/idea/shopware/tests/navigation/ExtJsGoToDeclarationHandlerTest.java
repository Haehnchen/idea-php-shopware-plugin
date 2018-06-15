package de.espend.idea.shopware.tests.navigation;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.patterns.PlatformPatterns;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.navigation.ExtJsGoToDeclarationHandler
 */
public class ExtJsGoToDeclarationHandlerTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("widgets.ini", "snippets/foobar/widgets.ini");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/navigation/fixtures";
    }

    public void testSnippetsNavigation() {
        assertNavigationMatch(
            JavaScriptFileType.INSTANCE,
            "var foo = \"{s name='swag-last-registr<caret>ations/customer' namespace='foobar/widgets'}{/s}\"",
            PlatformPatterns.psiFile()
        );
    }

    public void testNamespaceAsCommentNavigation() {
        assertNavigationMatch(
            JavaScriptFileType.INSTANCE,
            "\n//{namespace name=foo<caret>bar/widgets}",
            PlatformPatterns.psiFile()
        );
    }
}
