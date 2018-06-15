package de.espend.idea.shopware.installer.project;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
import com.intellij.util.io.ZipUtil;
import com.jetbrains.php.util.PhpConfigurationUtil;
import de.espend.idea.shopware.ShopwarePluginIcons;
import fr.adrienbrault.idea.symfony2plugin.installer.SymfonyInstallerUtil;
import fr.adrienbrault.idea.symfony2plugin.util.IdeHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareInstallerProjectGenerator extends WebProjectTemplate<ShopwareInstallerSettings> {
    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Shopware";
    }

    @Override
    public String getDescription() {
        return "Shopware";
    }

    @Override
    public void generateProject(@NotNull final Project project, final @NotNull VirtualFile baseDir, final @NotNull ShopwareInstallerSettings settings, @NotNull Module module) {

        String downloadPath = settings.getVersion().getUrl();
        String toDir = baseDir.getPath();

        VirtualFile zipFile = PhpConfigurationUtil.downloadFile(project, null, toDir, downloadPath, "shopware.zip");

        if (zipFile == null) {
            showErrorNotification(project, "Cannot download Shopware.zip file");
            return;
        }

        // Convert files
        File zip = VfsUtil.virtualToIoFile(zipFile);
        File base = VfsUtil.virtualToIoFile(baseDir);

        Task.Backgroundable task = new Task.Backgroundable(project, "Extracting", true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {

                try {
                    // unzip file
                    ZipUtil.extract(zip, base, null);

                    // Delete TMP File
                    FileUtil.delete(zip);

                    // Activate Plugin
                    IdeHelper.enablePluginAndConfigure(project);
                } catch (IOException e) {
                    showErrorNotification(project, "There is a error occurred");
                }
            }
        };

        ProgressManager.getInstance().run(task);
    }

    private static void showErrorNotification(@NotNull Project project, @NotNull String content) {
        Notifications.Bus.notify(new Notification(SymfonyInstallerUtil.INSTALLER_GROUP_DISPLAY_ID, "Shopware-Installer", content, NotificationType.ERROR, null), project);
    }

    private static void showInfoNotification(@NotNull Project project, @NotNull String content) {
        Notifications.Bus.notify(new Notification(SymfonyInstallerUtil.INSTALLER_GROUP_DISPLAY_ID, "Shopware-Installer", content, NotificationType.INFORMATION, null), project);
    }

    @NotNull
    @Override
    public GeneratorPeer<ShopwareInstallerSettings> createPeer() {
        return new ShopwareInstallerGeneratorPeer();
    }

    public boolean isPrimaryGenerator() {
        return PlatformUtils.isPhpStorm();
    }

    @Override
    public Icon getLogo() {
        return ShopwarePluginIcons.SHOPWARE;
    }
}
