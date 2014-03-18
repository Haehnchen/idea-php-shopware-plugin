package de.espend.idea.shopware;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import de.espend.idea.shopware.util.ShopwareUtil;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import org.jetbrains.annotations.NotNull;

public class ShopwareProjectComponent implements ProjectComponent {


    private static String MAGIC_FILE = "ShopwareIdeMagicStuff.php";

    final Project project;

    public ShopwareProjectComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {

        if(!Symfony2ProjectComponent.isEnabled(project)) {
            return;
        }

        ShopwareUtil.writeShopwareMagicFile(magicTemplate(), getMagicFilePathname());

    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    public static String getMagicFilePathname(Project project) {
        return project.getBasePath() + "/cache/" + MAGIC_FILE;
    }

    public String getMagicFilePathname() {
        return project.getBasePath() + "/cache/" + MAGIC_FILE;
    }


    @NotNull
    @Override
    public String getComponentName() {
        return "Shopware Plugin";
    }

    public static String magicTemplate() {
        return "<?php\n" +
            "\n" +
            "interface ShopwareIdeMagicStuff {\n" +
            "    \n" +
            "    /**\n" +
            "     * @return \\Shopware\\Components\\Logger\n" +
            "     */\n" +
            "    public function Pluginlogger();\n" +
            "}";

    }


}
