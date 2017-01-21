package de.espend.idea.shopware.tests.completion;

import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SmartyFileCompletionProviderTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("widgets.ini", "snippets/foobar/widgets.ini");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testCompletionForSnippetNames() {
        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{s name='<caret>' namespace='foobar/widgets'}{/s}",
            "swag-last-registrations/customer"
        );
    }

    public void testCompletionForSnippetNamespace() {
        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{s namespace='<caret>'}{/s}",
            "foobar/widgets"
        );
    }
}
