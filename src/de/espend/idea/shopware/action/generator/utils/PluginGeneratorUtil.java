package de.espend.idea.shopware.action.generator.utils;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.util.PhpConfigurationUtil;
import de.espend.idea.shopware.action.generator.dict.PluginGeneratorSettings;
import fr.adrienbrault.idea.symfony2plugin.installer.SymfonyInstallerCommandExecutor;
import fr.adrienbrault.idea.symfony2plugin.installer.SymfonyInstallerUtil;
import fr.adrienbrault.idea.symfony2plugin.util.IdeHelper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginGeneratorUtil {

    private static final long CHECKING_TIMEOUT_IN_MILLISECONDS = 1000L;

    public static void installPlugin(@NotNull Project project, @NotNull PluginGeneratorSettings settings) {

        Task task = new Task.Modal(project, "Generate", true) {
            public void run(@NotNull final ProgressIndicator indicator) {
                indicator.setIndeterminate(true);

                List<String> commands = new ArrayList<String>();

                commands.add("php");
                VirtualFile cliFile = VfsUtil.findFileByIoFile(new File(project.getBasePath() + "/sw-cli-tools.phar"), true);
                if(cliFile == null) {
                    //PhpConfigurationUtil.downloadFile(project, null, project.getBasePath(), "http://shopwarelabs.github.io/sw-cli-tools/sw.phar", "sw-cli-tools.phar");
                }

                commands.add("sw-cli-tools.phar");
                commands.add("plugin:create");

                commands.add(settings.getPluginName());


                String[] myCommand = ArrayUtil.toStringArray(commands);

                StringBuilder sb = new StringBuilder();
                sb.append("Running: ");
                for (String aCommandToRun : Arrays.copyOfRange(myCommand, 1, myCommand.length)) {
                    if (aCommandToRun.length() > 35) {
                        aCommandToRun = "..." + aCommandToRun.substring(aCommandToRun.length() - 35);
                    }
                    sb.append(" ").append(aCommandToRun);
                }
                indicator.setText(sb.toString());

                boolean cancelledByUser = false;
                final StringBuilder outputBuilder = new StringBuilder();
                try {
                    OSProcessHandler processHandler = ScriptRunnerUtil.execute(myCommand[0], project.getBaseDir().getPath(), null, Arrays.copyOfRange(myCommand, 1, myCommand.length));

                    processHandler.addProcessListener(new ProcessAdapter() {
                        @Override
                        public void onTextAvailable(ProcessEvent event, com.intellij.openapi.util.Key outputType) {
                            String text = event.getText();
                            outputBuilder.append(text);

                            text = SymfonyInstallerUtil.formatConsoleTextIndicatorOutput(text);
                            if(StringUtils.isNotBlank(text)) {
                                indicator.setText2(text);
                            }

                        }
                    });

                    processHandler.startNotify();
                    for (;;){
                        boolean finished = processHandler.waitFor(CHECKING_TIMEOUT_IN_MILLISECONDS);
                        if (finished) {
                            break;
                        }
                        if (indicator.isCanceled()) {
                            cancelledByUser = true;
                            OSProcessManager.getInstance().killProcessTree(processHandler.getProcess());
                            break;
                        }
                    }

                }

                catch (ExecutionException e) {
                    showErrorNotification(project, e.getMessage());
                    return;
                }

                if(cancelledByUser) {
                    showErrorNotification(project, "Checkout canceled");
                    return;
                }

                String output = outputBuilder.toString();
                if (output.toLowerCase().contains("exception")) {

                    String message = SymfonyInstallerUtil.formatExceptionMessage(output);
                    if(message == null) {
                        message = "The unexpected happens...";
                    }

                    showErrorNotification(project, message);
                    return;
                }

                showInfoNotification(project, "OK?");

                VirtualFile fileByIoFile = VfsUtil.findFileByIoFile(new File(project.getBasePath() + "/" + settings.getPluginName() + "/Bootstrap.php"), true);
                if(fileByIoFile == null) {
                    return;
                }

                final PsiFile[] file = {null};
                ApplicationManager.getApplication().runReadAction(() -> {
                    //file[0] = PsiManager.getInstance(getProject()).findFile(fileByIoFile);
                });

              }
        };

        ProgressManager.getInstance().run(task);

    }

    private static void showErrorNotification(@NotNull Project project, @NotNull String content)
    {
        Notifications.Bus.notify(new Notification(SymfonyInstallerUtil.INSTALLER_GROUP_DISPLAY_ID, "Shopware-Plugin", content, NotificationType.ERROR, null), project);
    }

    private static void showInfoNotification(@NotNull Project project, @NotNull String content)
    {
        Notifications.Bus.notify(new Notification(SymfonyInstallerUtil.INSTALLER_GROUP_DISPLAY_ID, "Shopware-Plugin", content, NotificationType.INFORMATION, null), project);
    }
}
