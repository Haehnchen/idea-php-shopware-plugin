package de.espend.idea.shopware.tests.navigation;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.navigation.PhpLineMarkerProvider
 */
public class PhpLineMarkerProviderTest extends ShopwareLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testNavigationToInlineSubscriber() {
        assertLineMarker(myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "class Bar implements \\Enlight\\Event\\SubscriberInterface {" +
            "   function getSubscribedEvents() {\n" +
            "       return [" +
            "           'foo.foobar' => 'foobar'," +
            "       ];" +
            "   }" +
            "   function foobar() {}\n" +
            "}\n"
        ), new LineMarker.ToolTipEqualsAssert("Related Targets"));
    }
}
