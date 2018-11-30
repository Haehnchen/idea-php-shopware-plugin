package de.espend.idea.shopware.symfony.service;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.util.ShopwareFQDN;
import de.espend.idea.shopware.util.ShopwareUtil;
import fr.adrienbrault.idea.symfony2plugin.dic.ContainerParameter;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceCollector;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceCollectorParameter;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceParameterCollector;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceParameterCollectorParameter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Soner Sayakci <s.sayakci@gmail.com>
 */
public class DefaultServiceParameterCollector implements ServiceParameterCollector {

    private static Collection<ContainerParameter> DEFAULTS = Arrays.asList(
            new ContainerParameter("shopware.app.rootDir", "", true),
            new ContainerParameter("shopware.app.downloadsDir", "files/downloads", true),
            new ContainerParameter("shopware.app.documentsDir", "files/documents", true),
            new ContainerParameter("shopware.web.webDir", "web", true),
            new ContainerParameter("shopware.web.cacheDir", "web/cache", true),
            new ContainerParameter("shopware.logger.level", "400", true),
            new ContainerParameter("shopware.es.enabled", "", true)
    );

    @Override
    public void collectIds(@NotNull ServiceParameterCollectorParameter.Id parameter) {
        for(PhpClass phpClass: PhpIndex.getInstance(parameter.getProject()).getAllSubclasses(ShopwareFQDN.PLUGIN_BOOTSTRAP)) {
            String formattedPluginName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, phpClass.getName());

            parameter.add(new ContainerParameter(formattedPluginName + ".plugin_dir", getRelativeToProjectPath(phpClass), true));
            parameter.add(new ContainerParameter(formattedPluginName + ".plugin_name", phpClass.getName(), true));
        }

        parameter.addAll(DEFAULTS);
    }

    private String getRelativeToProjectPath(PhpClass phpClass)
    {
        return phpClass.getContainingFile().getContainingDirectory().getVirtualFile().getPath().replace(phpClass.getProject().getBasePath(), "");
    }
}
