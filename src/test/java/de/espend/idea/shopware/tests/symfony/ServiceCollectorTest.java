package de.espend.idea.shopware.tests.symfony;

import com.intellij.util.containers.ContainerUtil;
import de.espend.idea.shopware.symfony.service.ServiceCollector;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;
import fr.adrienbrault.idea.symfony2plugin.dic.container.ServiceInterface;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceCollectorParameter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.symfony.service.ServiceCollector
 */
public class ServiceCollectorTest extends ShopwareLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/symfony/fixtures";
    }

    public void testCollectionForSubscriberEvents() {
        ServiceCollector serviceCollector = new ServiceCollector();

        Collection<String> names = new ArrayList<>();
        serviceCollector.collectIds(new ServiceCollectorParameter.Id(getProject(), names));
        assertTrue(names.contains("foobar.my.subscriber"));

        Collection<ServiceInterface> services = new ArrayList<>();
        serviceCollector.collectServices(new ServiceCollectorParameter.Service(getProject(), services));

        ServiceInterface service = ContainerUtil.find(services, serviceInterface ->
            serviceInterface.getId().equals("foobar.my.subscriber")
        );

        assertNotNull(service);
        assertEquals("Foo\\MySubscriber", service.getClassName());
    }
}
