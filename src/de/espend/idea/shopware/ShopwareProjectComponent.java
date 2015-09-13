package de.espend.idea.shopware;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndexImpl;
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
public class ShopwareProjectComponent implements ProjectComponent {

    public static final int DUMPER_PERIODE = 600 * 1000;

    final Project project;
    private Timer timer;

    public ShopwareProjectComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {

        if(!Symfony2ProjectComponent.isEnabled(project)) {
            return;
        }

        DumbService.getInstance(this.project).smartInvokeLater(new Runnable() {
            @Override
            public void run() {

                if(PhpElementsUtil.getClassInterface(project, "\\Enlight_Controller_Action") == null) {
                    return;
                }

                timer = new Timer();

                timer.schedule(new TimerTask() {
                    public void run() {

                        if (DumbService.getInstance(project).isDumb()) {
                            return;
                        }

                        final Map<String, Collection<String>> events = new HashMap<String, Collection<String>>();
                        final Set<String> configs = new HashSet<String>();

                        final Collection<VirtualFile> containingFiles = new HashSet<VirtualFile>();

                        ApplicationManager.getApplication().runReadAction(new Runnable() {
                            @Override
                            public void run() {
                                for(String methodName : EventConfigGoToIndex.METHOD_NAMES) {
                                    FileBasedIndexImpl.getInstance().getFilesWithKey(EventConfigGoToIndex.KEY, new HashSet<String>(Arrays.asList(methodName)), new Processor<VirtualFile>() {
                                        @Override
                                        public boolean process(VirtualFile virtualFile) {
                                            containingFiles.add(virtualFile);
                                            return true;
                                        }
                                    }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), PhpFileType.INSTANCE));
                                }
                            }
                        });

                        for (VirtualFile virtualFile : containingFiles) {
                            ApplicationManager.getApplication().runReadAction(new PsiParameterStorageRunnable(project, virtualFile, events, configs));
                        }

                        HookSubscriberUtil.NOTIFY_EVENTS_MAP.clear();
                        HookSubscriberUtil.NOTIFY_EVENTS_MAP.putAll(events);

                        ShopwareUtil.PLUGIN_CONFIGS.clear();
                        ShopwareUtil.PLUGIN_CONFIGS.addAll(configs);
                    }
                }, 0, DUMPER_PERIODE);
            }
        });

    }

    @Override
    public void projectClosed() {
        if(this.timer != null) {
            this.timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    public static boolean isValidForProject(@Nullable PsiElement psiElement) {
        if(psiElement == null) return false;

        if(!Symfony2ProjectComponent.isEnabled(psiElement)) {
            return false;
        }

        if(VfsUtil.findRelativeFile(psiElement.getProject().getBaseDir(), "engine", "Shopware", "Kernel.php") != null) {
            return true;
        }

        if(PhpElementsUtil.getClassInterface(psiElement.getProject(), "\\Enlight_Controller_Action") != null) {
            return true;
        }

        return false;
    }


    @NotNull
    @Override
    public String getComponentName() {
        return "Shopware Plugin";
    }
}
