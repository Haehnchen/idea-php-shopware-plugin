package de.espend.idea.shopware.tests.index;

import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import de.espend.idea.shopware.index.ConfigIndex;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;

import java.util.Set;

public class ConfigIndexTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("sAdmin.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/shopware/tests/index/fixtures";
    }

    public void testPropertyUsageInClass() {
        assertIndexContains(ConfigIndex.KEY, "all");

        for (Set<String> configValues : FileBasedIndex.getInstance().getValues(ConfigIndex.KEY, "all", GlobalSearchScope.allScope(this.getProject()))) {
            for (String value: configValues) {
                System.out.println(value);
            }
        }

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
                value.contains("sBASEFILE")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
                value.contains("globalConfig")
        );

        assertIndexContainsKeyWithValue(ConfigIndex.KEY, "all", value ->
                value.contains("variableConfig")
        );
    }
}
