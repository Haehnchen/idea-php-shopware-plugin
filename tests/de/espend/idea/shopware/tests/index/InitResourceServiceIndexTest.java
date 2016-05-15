package de.espend.idea.shopware.tests.index;

import com.intellij.util.containers.ContainerUtil;
import de.espend.idea.shopware.index.InitResourceServiceIndex;
import de.espend.idea.shopware.index.dict.BootstrapResource;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.index.utils.SubscriberIndexUtil;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.index.InitResourceServiceIndex
 */
public class InitResourceServiceIndexTest extends ShopwareLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testThatInlineIsIndexedSubscriber() {
        ServiceResource resource = ContainerUtil.find(SubscriberIndexUtil.getIndexedBootstrapResources(getProject(), BootstrapResource.INIT_RESOURCE), value ->
            value.getServiceName().equals("foobar")
        );

        assertNotNull(resource);

        assertEquals(BootstrapResource.INIT_RESOURCE, resource.getSubscriber());
        assertEquals("MySubscriber.foobar", resource.getSignature());
        assertEquals("Enlight_Bootstrap_InitResource_foobar", resource.getEvent());
    }

    public void testThatArraySubscriberIsIndexed() {
        assertNotNull(ContainerUtil.find(SubscriberIndexUtil.getIndexedBootstrapResources(getProject(), BootstrapResource.INIT_RESOURCE), value ->
            value.getServiceName().equals("foobar_array") && value.getSubscriber() == BootstrapResource.INIT_RESOURCE &&
            "MySubscriber.foobar_array".equals(value.getSignature()) && "Enlight_Bootstrap_InitResource_foobar_array".equals(value.getEvent())
        ));
    }

    public void testAfterAndRegisterResourceSubscriberIsIndexed() {
        assertNotNull(ContainerUtil.find(SubscriberIndexUtil.getIndexedBootstrapResources(getProject(), BootstrapResource.AFTER_INIT_RESOURCE), value ->
            value.getServiceName().equals("foobar_after_init")
        ));

        assertNotNull(ContainerUtil.find(SubscriberIndexUtil.getIndexedBootstrapResources(getProject(), BootstrapResource.AFTER_REGISTER_RESOURCE), value ->
            value.getServiceName().equals("foobar_register_resource")
        ));
    }
}
