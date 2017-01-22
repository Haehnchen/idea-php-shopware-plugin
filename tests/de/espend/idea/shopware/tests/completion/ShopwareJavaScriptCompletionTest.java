package de.espend.idea.shopware.tests.completion;

import com.intellij.lang.javascript.JavaScriptFileType;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.completion.ShopwareJavaScriptCompletion
 */
public class ShopwareJavaScriptCompletionTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("widgets.ini", "snippets/foobar/widgets.ini");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testCompletionPrefixForSnippetNameWithFileScopeNamespace() {
        String[] dataProvider = {
            "{s name='<caret>'}",
            "{s name=<caret>}",
        };

        for (String s : dataProvider) {
            assertCompletionContains(
                JavaScriptFileType.INSTANCE,
                "//{namespace name=foobar/widgets}\n" +
                    "var foo = {foo: \"" + s + "\"}",
                "swag-last-registrations/customer"
            );
        }
    }

    public void testCompletionPrefixForSnippetNameWithInlineNamespace() {
        String[] dataProvider = {
            "{s name='<caret>' name=foobar/widgets}",
            "{s name=<caret>} name=foobar/widgets}",
        };

        for (String s : dataProvider) {
            assertCompletionContains(
                JavaScriptFileType.INSTANCE,
                "//{namespace name=foobar/widgets}\n" +
                    "var foo = {foo: \"" + s + "\"}",
                "swag-last-registrations/customer"
            );
        }
    }

    public void testCompletionPrefixForSnippetNamespace() {
        String[] dataProvider = {
            "{s namespace='<caret>'}",
            "{s namespace=<caret>}",
        };

        for (String s : dataProvider) {
            assertCompletionContains(
                JavaScriptFileType.INSTANCE,
                "var foo = {foo: \"" + s + "\"}",
                "foobar/widgets"
            );
        }
    }

    public void testCompletionPrefixForCommentNamespace() {
        assertCompletionContains(
            JavaScriptFileType.INSTANCE,
            "//{namespace name=<caret>}",
            "foobar/widgets"
        );
    }
}
