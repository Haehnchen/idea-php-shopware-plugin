package de.espend.idea.shopware.tests.completion;

import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SmartyFileCompletionProviderTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("widgets.ini", "snippets/foobar/widgets.ini");
        myFixture.copyFileToProject("config.tpl");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/completion/fixtures";
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

    public void testCompletionForConfig() {
        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{config name=\"<caret>\"}",
            "SmartyVoteDisableQuote"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{config name=\'<caret>\'}",
            "SmartyVoteDisableQuote"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{config name=<caret>}",
            "SmartyVoteDisableQuote"
        );

        /*
        Not working:

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{$captchaName = {config name=<caret>}}",
            "SmartyVoteDisableQuote"
        );

        */
    }
}
