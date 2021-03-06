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
        myFixture.copyFileToProject("classes.php");
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
            "{s namespace=<caret>}{/s}",
            "foobar/widgets"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{s namespace=\"<caret>\"}{/s}",
            "foobar/widgets"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{s namespace='<caret>'}{/s}",
            "foobar/widgets"
        );
    }

    public void testCompletionForSnippetNamespaceInFile() {
        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{namespace name=<caret>}",
            "foobar/widgets"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{namespace name='<caret>'}",
            "foobar/widgets"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{namespace name=\"<caret>\"}",
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

    public void testCompletionForActionController() {
        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=<caret> action=shopMenu}",
            "Listing"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=\"<caret>\" action=shopMenu}",
            "Listing"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller='<caret>' action=shopMenu}",
            "Listing"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=frontend controller='<caret>' action=shopMenu}",
            "FrontendListing"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=\"frontend\" controller='<caret>' action=shopMenu}",
            "FrontendListing"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module='frontend' controller='<caret>' action=shopMenu}",
            "FrontendListing"
        );
    }

    public void testCompletionForActionControllerActionName() {
        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=Listing action=<caret>}",
            "topSeller"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=Listing action='<caret>'}",
            "topSeller"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=Listing action=\"<caret>\"}",
            "topSeller"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{action module=\"frontend\" controller='FrontendListing' action=\"<caret>\"}",
            "topSellerFrontend"
        );
    }

    public void testCompletionForUrlController() {
        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{url controller=<caret>}",
            "FrontendListing"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{url controller='<caret>'}",
            "FrontendListing"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{url controller=\"<caret>\"}",
            "FrontendListing"
        );
    }

    public void testCompletionForUrlControllerAction() {
        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{url controller=FrontendListing action='<caret>'}",
            "topSellerFrontend"
        );

        assertCompletionContains(
            SmartyFileType.INSTANCE,
            "{url controller=FrontendListing action=<caret>}",
            "topSellerFrontend"
        );
    }
}
