package de.espend.idea.shopware.tests.index;

import de.espend.idea.shopware.index.InitResourceServiceIndex;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class InitResourceServiceIndexTest extends ShopwareLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testThatInlineSubscriber() {
        assertIndexContains(InitResourceServiceIndex.KEY, "foobar");

        assertIndexContainsKeyWithValue(InitResourceServiceIndex.KEY, "foobar", value ->
            value.getServiceName().equals("foobar") && "MySubscriber.foobar".equals(value.getSignature()) && "Enlight_Bootstrap_InitResource_foobar".equals(value.getEvent())
        );
    }
}
