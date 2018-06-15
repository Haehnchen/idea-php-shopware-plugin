package de.espend.idea.shopware.tests.references;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.reference.LazySubscriberReferenceProvider
 */
public class LazySubscriberReferenceProviderTest extends ShopwareLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("services.xml");
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/references/fixtures";
    }

    public void testCompletionContains() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
            "class MySubscriber implements \\Enlight\\Event\\SubscriberInterface\n" +
            "{\n" +
            "   public static function getSubscribedEvents()\n" +
            "   {\n" +
            "       return [\n" +
            "           '<caret>' => 'foobar',\n" +
            "       ];\n" +
            "   }\n" +
            "}",
            "Enlight_Bootstrap_AfterInitResource_foo.datetime", "Enlight_Bootstrap_AfterRegisterResource_foo.datetime", "Enlight_Bootstrap_InitResource_foo.datetime"
        );
    }
}
