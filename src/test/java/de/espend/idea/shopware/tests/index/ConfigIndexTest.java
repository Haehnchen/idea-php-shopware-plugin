package de.espend.idea.shopware.tests.index;

import de.espend.idea.shopware.index.ConfigIndex;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

/**
 * @author Soner Sayakci <s.sayakci@gmail.com>
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ConfigIndexTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("sAdmin.php");
        myFixture.copyFileToProject("config.tpl");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/index/fixtures";
    }

    public void testPropertyUsageInClass() {
        assertIndexContains(ConfigIndex.KEY, "all");

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("sBASEFILE")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("globalConfig")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("variableConfig")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("variableConfigFo")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("foo_incomplete")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("variableConfigCfg")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("SmartyVoteDisable")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("SmartyVoteDisableQuote")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
            value.contains("captchaMethod")
        );
    }
}
