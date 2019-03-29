package de.espend.idea.shopware.symfony.service;

import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ShopwareFQDN;
import de.espend.idea.shopware.util.ShopwareUtil;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceCollector;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceCollectorParameter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DefaultServiceCollector implements ServiceCollector {

    private static Map<String, String> DEFAULTS = new HashMap<String, String>() {{
        put("bootstrap", "Shopware_Bootstrap");
        put("application", "Shopware");
        put("shop", "Shopware\\Models\\Shop\\Shop");
        put("db_connection", "\\PDO");
    }};

    @Override
    public void collectServices(@NotNull ServiceCollectorParameter.Service parameter) {
        if(!ShopwareProjectComponent.isValidForProject(parameter.getProject())) {
            return;
        }

        DEFAULTS.forEach(parameter::add);

        for (String pluginName : ShopwareUtil.getPluginsWithFilesystem(parameter.getProject())) {
            String camelCaseName = ShopwareUtil.toCamelCase(pluginName, true);
            parameter.add(camelCaseName + ".filesystem.private", ShopwareFQDN.PREFIX_FILESYSTEM);
            parameter.add(camelCaseName + ".filesystem.public",  ShopwareFQDN.PREFIX_FILESYSTEM);
        }

        for (String pluginName : ShopwareUtil.getPluginsWithLogger(parameter.getProject())) {
            String camelCaseName = ShopwareUtil.toCamelCase(pluginName, true);
            parameter.add(camelCaseName + ".logger", ShopwareFQDN.SHOPWARE_LOGGER);
        }
    }

    @Override
    public void collectIds(@NotNull ServiceCollectorParameter.Id parameter) {
        parameter.addAll(DEFAULTS.keySet());

        for (String pluginName : ShopwareUtil.getPluginsWithFilesystem(parameter.getProject())) {
            String camelCaseName = ShopwareUtil.toCamelCase(pluginName, true);
            parameter.add(camelCaseName + ".filesystem.private");
            parameter.add(camelCaseName + ".filesystem.public");
        }

        for (String pluginName : ShopwareUtil.getPluginsWithLogger(parameter.getProject())) {
            String camelCaseName = ShopwareUtil.toCamelCase(pluginName, true);
            parameter.add(camelCaseName + ".logger");
        }
    }
}
