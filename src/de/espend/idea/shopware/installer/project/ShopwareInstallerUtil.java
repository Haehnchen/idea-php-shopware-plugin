package de.espend.idea.shopware.installer.project;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.util.PhpConfigurationUtil;
import de.espend.idea.shopware.installer.project.dict.ShopwareInstallerVersion;
import fr.adrienbrault.idea.symfony2plugin.installer.dict.SymfonyInstallerVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareInstallerUtil {

    public static String PROJECT_SUB_FOLDER = ".checkout";
    public static String INSTALLER_GROUP_DISPLAY_ID = "Shopware";

    @Nullable
    public static VirtualFile downloadPhar(@Nullable Project project, JComponent component, @Nullable String toDir)
    {
        return PhpConfigurationUtil.downloadFile(project, component, toDir, "http://symfony.com/installer", "symfony.phar");
    }

    @Nullable
    public static String extractSuccessMessage(@NotNull String output)
    {
        Matcher matcher = Pattern.compile("Preparing project[.]*(.*)", Pattern.DOTALL).matcher(output);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    @NotNull
    public static String formatConsoleTextIndicatorOutput(@NotNull String text) {

        // 2.03 MB/4.95 MB ======>---   41.2%
        Matcher matcher = Pattern.compile("([^=->]*).*[\\s*]([\\d+.]*%)").matcher(text);
        if (matcher.find()) {
            return String.format("%s - %s", matcher.group(2).trim(), matcher.group(1).trim());
        }

        return text;
    }

    public static boolean isSuccessfullyInstalled(@NotNull String output) {
        // successfully installed
        // [RuntimeException]
        return output.toLowerCase().contains("successfully") && !output.toLowerCase().contains("exception]");
    }

    @Nullable
    public static String formatExceptionMessage(@Nullable String output) {

        if(output == null) {
            return null;
        }

        // [RuntimeException] message
        Matcher matcher = Pattern.compile("Exception](.*)", Pattern.DOTALL).matcher(output);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return output;
    }

    @NotNull
    public static String[] getCreateProjectCommand(@NotNull ShopwareInstallerVersion version, @NotNull String installerPath, @NotNull String newProjectPath, @NotNull String phpPath, @Nullable String commandLineOptions) {

        List<String> commands = new ArrayList<String>();

        commands.add(phpPath);
        commands.add(installerPath);

        // "php symfony demo"
        if("demo".equals(version.getVersion())) {
            commands.add("demo");
            commands.add(newProjectPath + "/" + PROJECT_SUB_FOLDER);
        } else {
            commands.add("new");
            commands.add(newProjectPath + "/" + PROJECT_SUB_FOLDER);
            commands.add(version.getVersion());
        }

        if(commandLineOptions != null) {
            commands.add(commandLineOptions);
        }

        return ArrayUtil.toStringArray(commands);
    }

    @NotNull
    public static List<ShopwareInstallerVersion> getVersions(@NotNull String jsonContent) {

        JsonArray jsonArray = new JsonParser().parse(jsonContent).getAsJsonArray();

        List<ShopwareInstallerVersion> shopwareInstallerVersions = new ArrayList<ShopwareInstallerVersion>();

        for (JsonElement element: jsonArray) {
            JsonObject versionObject = element.getAsJsonObject();
            shopwareInstallerVersions.add(new ShopwareInstallerVersion(versionObject.get("version").getAsString(), versionObject.get("version").getAsString(), versionObject.get("uri").getAsString()));
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
            URL url = new URL("http://update-api.shopware.com/v1/releases/install");
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String content = "";
            String line;
            while ((line = in.readLine()) != null) {
                content += line;
            }

            in.close();

            return content;
        } catch (IOException e) {
            return null;
        }

    }

}
