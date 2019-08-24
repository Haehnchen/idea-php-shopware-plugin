package de.espend.idea.shopware.tests.navigation;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.navigation.SmartyFileGoToDeclarationHandler
 */
public class SmartyFileGoToDeclarationHandlerTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("widgets.ini", "snippets/foobar/widgets.ini");
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/navigation/fixtures";
    }

    public void testNavigationForSnippetNamespace() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{s namespace='foobar/w<caret>idgets'}{/s}",
            PlatformPatterns.psiFile()
        );
    }

    public void testNavigationForSnippetName() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{s name='swag-last-registr<caret>ations/customer' namespace='foobar/widgets'}{/s}",
            PlatformPatterns.psiFile()
        );
    }

    public void testNavigationForActionController() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=Lis<caret>ting action=shopMenu}",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=\"Lis<caret>ting\" action=shopMenu}",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller='Lis<caret>ting' action=shopMenu}",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=frontend controller=Fronten<caret>dListing action=shopMenu}",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=\"frontend\" controller=Fronten<caret>dListing action=shopMenu}",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module='frontend' controller=Fronten<caret>dListing action=shopMenu}",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }
}
