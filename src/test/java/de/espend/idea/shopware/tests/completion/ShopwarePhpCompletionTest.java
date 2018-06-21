package de.espend.idea.shopware.tests.completion;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.shopware.completion.ShopwarePhpCompletion
 */
public class ShopwarePhpCompletionTest extends ShopwareLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/completion/fixtures";
    }

    public void testThatAttributesUpdateAreCompleted() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $foo \\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService */\n" +
                "$foo->createAttribute('<caret>')",
            "s_article_configurator_template_prices_attributes"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $foo \\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService */\n" +
                "$foo->update('<caret>')",
            "s_article_configurator_template_prices_attributes"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $foo \\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService */\n" +
                "$foo->update('', '', '<caret>')",
            "boolean"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/** @var $foo \\Shopware\\Bundle\\AttributeBundle\\Service\\CrudService */\n" +
                "$foo->update('', '', '', ['<caret>' => ''])",
            "translatable"
        );
    }
}
