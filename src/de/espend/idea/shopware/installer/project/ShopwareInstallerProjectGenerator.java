package de.espend.idea.shopware.installer.project;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
import de.espend.idea.shopware.ShopwarePluginIcons;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.installer.SymfonyInstallerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

public class ShopwareInstallerProjectGenerator extends WebProjectTemplate<ShopwareInstallerSettings> {
    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Shopware Installer";
    }

    @Override
    public String getDescription() {
        return "Shopware Installer";
    }

    @Override
    public void generateProject(@NotNull final Project project, final @NotNull VirtualFile baseDir, final @NotNull ShopwareInstallerSettings settings, @NotNull Module module) {

        System.out.println("foo");

        final File baseDirFile = new File(baseDir.getPath());
        final File tempFile = FileUtil.findSequentNonexistentFile(baseDirFile, "symfony", "");

        String composerPath;
        File symfonyInProject = null;
        if (settings.isDownload()) {

            VirtualFile file = SymfonyInstallerUtil.downloadPhar(project, null, tempFile.getPath());
            if (file == null)  {
                showErrorNotification(project, "Cannot download symfony.phar file");
                Symfony2ProjectComponent.getLogger().warn("Cannot download symfony.phar file");
                return;
            }

            composerPath = file.getPath();
            symfonyInProject = tempFile;
        } else {
            composerPath = settings.getExistingPath();
        }

        //String[] commands = SymfonyInstallerUtil.getCreateProjectCommand(settings.getVersion(), composerPath, baseDir.getPath(), settings.getPhpInterpreter(), null);

        /*
        final File finalSymfonyInProject = symfonyInProject;
        SymfonyInstallerCommandExecutor executor = new SymfonyInstallerCommandExecutor(project, baseDir, commands) {
            @Override
            protected void onFinish(@Nullable String message) {
                //IdeHelper.enablePluginAndConfigure(project);

                //if(message != null) {
                    // replace empty lines, provide html output, and remove our temporary path
                 //   showInfoNotification(project, message
                //            .replaceAll("(?m)^\\s*$[\n\r]{1,}", "")
                //            .replaceAll("(\r\n|\n)", "<br />")
                 //           .replace("/" + SymfonyInstallerUtil.PROJECT_SUB_FOLDER, "")
                 //   );
                //}

                // remove temporary symfony installer folder
                if(finalSymfonyInProject != null) {
                    FileUtil.delete(finalSymfonyInProject);
                }

            }

            @Override
            protected void onError(@NotNull String message) {
                showErrorNotification(project, message);
            }

            @Override
            protected String getProgressTitle() {
                return String.format("Installing Shopware %s", settings.getVersion().getPresentableName());
            }
        };
        */
        //executor.execute();
    }

    private static void showErrorNotification(@NotNull Project project, @NotNull String content)
    {
        Notifications.Bus.notify(new Notification(SymfonyInstallerUtil.INSTALLER_GROUP_DISPLAY_ID, "Shopware-Installer", content, NotificationType.ERROR, null), project);
    }

    private static void showInfoNotification(@NotNull Project project, @NotNull String content)
    {
        Notifications.Bus.notify(new Notification(SymfonyInstallerUtil.INSTALLER_GROUP_DISPLAY_ID, "Shopware-Installer", content, NotificationType.INFORMATION, null), project);
    }

    @NotNull
    @Override
    public GeneratorPeer<ShopwareInstallerSettings> createPeer() {
        return new ShopwareInstallerGeneratorPeer();
    }

    public boolean isPrimaryGenerator()
    {
        return PlatformUtils.isPhpStorm();
    }

    @Override
    public Icon getLogo() {
        return ShopwarePluginIcons.SHOPWARE;
    }
}
