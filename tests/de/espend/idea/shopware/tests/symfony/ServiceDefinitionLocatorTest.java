package de.espend.idea.shopware.tests.symfony;

import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import de.espend.idea.shopware.symfony.service.ServiceCollector;
import de.espend.idea.shopware.symfony.service.ServiceDefinitionLocator;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceDefinitionLocatorParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see ServiceDefinitionLocator
 */
public class ServiceDefinitionLocatorTest extends ShopwareLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testTargetSubscriberEvents() {
        ServiceDefinitionLocator locator = new ServiceDefinitionLocator();

        Collection<PsiElement> psiElements = new ArrayList<>();
        locator.locate("foobar.my.subscriber", new ServiceDefinitionLocatorParameter(getProject(), psiElements));

        assertNotNull(
            ContainerUtil.find(psiElements, psiElement -> psiElement instanceof Method && ((Method) psiElement).getName().equals("foobar"))
        );
    }
}
