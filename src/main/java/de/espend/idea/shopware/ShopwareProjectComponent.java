package de.espend.idea.shopware;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.shopware.index.EventConfigGoToIndex;
import de.espend.idea.shopware.util.HookSubscriberUtil;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.dict.PsiParameterStorageRunnable;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareProjectComponent {
    public static class PostStartupActivity implements com.intellij.openapi.startup.StartupActivity {
        @Override
        public void runActivity(@NotNull Project project) {
            if(!Symfony2ProjectComponent.isEnabled(project)) {
                return;
            }

            project.getService(ProjectCloseService.class).start();
        }
    }

    public static class ProjectCloseService implements Disposable {
        private static final int DUMPER_PERIODE = 600 * 1000;

        private final Project project;
        private Timer timer;

        public ProjectCloseService(@NotNull Project project) {
            this.project = project;
        }

        public void start() {
            if(!Symfony2ProjectComponent.isEnabled(project)) {
                return;
            }

            StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
                timer = new Timer();
                timer.schedule(new MyTimerTask(project), 0, DUMPER_PERIODE);
            });
        }

        @Override
        public void dispose() {
            if (this.timer != null) {
                this.timer.cancel();
                this.timer.purge();
                this.timer = null;
            }
        }
    }

    public static boolean isValidForProject(@Nullable PsiElement psiElement) {
        if(ApplicationManager.getApplication().isUnitTestMode()) {
            return true;
        }

        return psiElement != null && isValidForProject(psiElement.getProject());
    }

    public static boolean isValidForProject(@Nullable Project project) {
        if(project == null || Symfony2ProjectComponent.isEnabled(project)) {
            return true;
        }

        if(VfsUtil.findRelativeFile(project.getBaseDir(), "engine", "Shopware", "Kernel.php") != null) {
            return true;
        }

        return PhpElementsUtil.getClassInterface(project, "\\Enlight_Controller_Action") != null;
    }

    private static class MyTimerTask extends TimerTask {
        @NotNull
        private final Project project;

        public MyTimerTask(@NotNull Project project) {
            this.project = project;
        }

        public void run() {
            DumbService.getInstance(project).smartInvokeLater(() -> {
                if (DumbService.getInstance(project).isDumb()) {
                    return;
                }

                if (PhpElementsUtil.getClassInterface(project, "\\Enlight_Controller_Action") == null) {
                    return;
                }

                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Shopware: Event indexer", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        DumbService.getInstance(project).smartInvokeLater(() -> {
                            final Map<String, Collection<String>> events = new HashMap<>();
                            final Set<String> configs = new HashSet<>();

                            final Collection<VirtualFile> containingFiles = new HashSet<>();

                            for (String methodName : EventConfigGoToIndex.METHOD_NAMES) {
                                ApplicationManager.getApplication().runReadAction(() -> {
                                    FileBasedIndex.getInstance().getFilesWithKey(EventConfigGoToIndex.KEY, new HashSet<>(Collections.singletonList(methodName)), virtualFile -> {
                                        containingFiles.add(virtualFile);
                                        return true;
                                    }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), PhpFileType.INSTANCE));
                                });
                            }

                            for (VirtualFile virtualFile : containingFiles) {
                                ApplicationManager.getApplication().runReadAction(new PsiParameterStorageRunnable(project, virtualFile, events, configs));
                            }

                            HookSubscriberUtil.NOTIFY_EVENTS_MAP.clear();
                            HookSubscriberUtil.NOTIFY_EVENTS_MAP.putAll(events);

                            ShopwareUtil.PLUGIN_CONFIGS.clear();
                            ShopwareUtil.PLUGIN_CONFIGS.addAll(configs);
                        });
                    }
                });
            });
        }
    }
}
