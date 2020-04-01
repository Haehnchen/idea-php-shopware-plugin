package de.espend.idea.shopware.installer.project;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.extensions.PluginId;
import de.espend.idea.shopware.installer.project.dict.ShopwareInstallerVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareInstallerUtil {

    @NotNull
    public static List<ShopwareInstallerVersion> getVersions(@NotNull String jsonContent) {

        JsonArray jsonArray = new JsonParser().parse(jsonContent).getAsJsonArray();

        List<ShopwareInstallerVersion> shopwareInstallerVersions = new ArrayList<>();

        for (JsonElement element: jsonArray) {
            JsonObject versionObject = element.getAsJsonObject();

            shopwareInstallerVersions.add(new ShopwareInstallerVersion(
                versionObject.get("version").getAsString(),
                versionObject.get("version").getAsString(),
                versionObject.get("uri").getAsString()
            ));
        }

        return shopwareInstallerVersions;
    }

    @Nullable
    public static String getDownloadVersions() {

        String userAgent = String.format("%s / %s / Shopware Plugin %s",
            ApplicationInfo.getInstance().getVersionName(),
            ApplicationInfo.getInstance().getBuild(),
            PluginManager.getPlugin(PluginId.getId("de.espend.idea.shopware")).getVersion()
        );

        try {

            // @TODO: PhpStorm9:
            // simple replacement for: com.intellij.util.io.HttpRequests
            URL url = new URL("http://update-api.shopware.com/v1/releases/install?major=6");
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }

            in.close();

            return content.toString();
        } catch (IOException e) {
            return null;
        }

    }

}
