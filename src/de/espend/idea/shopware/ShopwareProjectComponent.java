package de.espend.idea.shopware;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiElement;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareProjectComponent implements ProjectComponent {


    private static String MAGIC_FILE = "ShopwareIdeMagicStuff.php";

    final Project project;

    public ShopwareProjectComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {

        /*

        if(!Symfony2ProjectComponent.isEnabled(project)) {
            return;
        }

        DumbService.getInstance(this.project).smartInvokeLater(new Runnable() {
            @Override
            public void run() {

                if(PhpElementsUtil.getClassInterface(project, "\\Enlight_Controller_Action") == null) {
                    return;
                }

                ShopwareUtil.writeShopwareMagicFile(magicTemplate(), getMagicFilePathname());

                new Task.Backgroundable(project, "Shopware Parameter Parser", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {

                        final Set<String> events = new HashSet<String>();
                        final Set<String> configs = new HashSet<String>();

                        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PhpFileType.INSTANCE, GlobalSearchScope.allScope(project));
                        float stepSize = (float) (1.0 / containingFiles.size());
                        for (VirtualFile virtualFile : containingFiles) {
                            ApplicationManager.getApplication().runReadAction(new PsiParameterStorageRunnable(getProject(), virtualFile, events, configs));
                            indicator.setFraction(indicator.getFraction() + stepSize);
                        }

                        HookSubscriberUtil.NOTIFY_EVENTS.clear();
                        HookSubscriberUtil.NOTIFY_EVENTS.addAll(events);

                        ShopwareUtil.PLUGIN_CONFIGS.clear();
                        ShopwareUtil.PLUGIN_CONFIGS.addAll(configs);

                    }
                }.queue();
            }
        });
        */

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
