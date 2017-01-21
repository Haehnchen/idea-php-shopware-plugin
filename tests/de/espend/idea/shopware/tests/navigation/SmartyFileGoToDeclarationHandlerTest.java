package de.espend.idea.shopware.tests.navigation;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.navigation.SmartyFileGoToDeclarationHandler
 */
public class SmartyFileGoToDeclarationHandlerTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("widgets.ini", "snippets/foobar/widgets.ini");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testNavigationForSnippetNamespace() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{s namespace='foobar/w<caret>idgets'}{/s}",
            PlatformPatterns.psiFile()
        );
    }

    public void testNavigationForSnippetName() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{s name='swag-last-registr<caret>ations/customer' namespace='foobar/widgets'}{/s}",
            PlatformPatterns.psiFile()
        );
    }
}
