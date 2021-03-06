package de.espend.idea.shopware.tests.navigation;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.psi.elements.Method;
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

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{s name=\"swag-last-registr<caret>ations/customer\" namespace='foobar/widgets'}{/s}",
            PlatformPatterns.psiFile()
        );
    }

    public void testNavigationForSnippetNamespaceInFile() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{namespace name=\"foobar/<caret>widgets\"}",
            PlatformPatterns.psiFile()
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{namespace name='foobar/<caret>widgets'}",
            PlatformPatterns.psiFile()
        );
    }

    public void testNavigationForSnippetNamespaceInFileNamespaceClick() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{name<caret>space name=\"foobar/widgets\"}",
            PlatformPatterns.psiFile()
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{name<caret>space name='foobar/widgets'}",
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

    public void testNavigationFForActionControllerActionName() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=Listing action=topS<caret>eller}",
            PlatformPatterns.psiElement(Method.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=widgets controller=Listing action='topS<caret>eller'}",
            PlatformPatterns.psiElement(Method.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=\"widgets\" controller='Listing' action=\"topS<caret>eller\"}",
            PlatformPatterns.psiElement(Method.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=frontend controller=FrontendListing action=topSellerFr<caret>ontend}",
            PlatformPatterns.psiElement(Method.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{action module=frontend controller=FrontendListing action=top_selle<caret>r_frontend}",
            PlatformPatterns.psiElement(Method.class)
        );
    }

    public void testNavigationForUrlController() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{url controller=Fronten<caret>dListing}",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{url controller='Fronten<caret>dListing'}",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{url controller='fronten<caret>d_listing'}",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }

    public void testNavigationForUrlControllerAction() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{url controller=FrontendListing action=topSelle<caret>rFrontend}",
            PlatformPatterns.psiElement(Method.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{url controller=FrontendListing action=top_seller<caret>_frontend}",
            PlatformPatterns.psiElement(Method.class)
        );
    }

    public void testNavigationForUrlTag() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{ur<caret>l controller=FrontendListing action=top_seller_frontend}",
            PlatformPatterns.psiElement(Method.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{ur<caret>l controller=FrontendListing action=top_seller_frontend}",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }

    public void testNavigationForActionTag() {
        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{act<caret>ion module=frontend controller=FrontendListing action=top_seller_frontend}",
            PlatformPatterns.psiElement(Method.class)
        );

        assertNavigationMatch(
            SmartyFileType.INSTANCE,
            "{act<caret>ion module=frontend controller=FrontendListing action=top_seller_frontend}",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }
}
