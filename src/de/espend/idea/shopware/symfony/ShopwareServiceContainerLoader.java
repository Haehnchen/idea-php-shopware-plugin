package de.espend.idea.shopware.symfony;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import fr.adrienbrault.idea.symfony2plugin.dic.ContainerFile;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceContainerLoader;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceContainerLoaderParameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareServiceContainerLoader implements ServiceContainerLoader {

    public static final List<String[]> CONTAINER_FILES = Collections.unmodifiableList(Arrays.asList(
        new String[]{"engine", "Shopware", "Components", "DependencyInjection", "services.xml"},
        new String[]{"engine", "Shopware", "Components", "DependencyInjection", "logger.xml"}
    ));

    @Override
    public void attachContainerFile(ServiceContainerLoaderParameter parameter) {

        for(String[] containerPath: CONTAINER_FILES) {
            VirtualFile virtualFile = VfsUtil.findRelativeFile(parameter.getProject().getBaseDir(), containerPath);
            if(virtualFile == null) {
                return;
            }

            String path = VfsUtil.getRelativePath(virtualFile, parameter.getProject().getBaseDir(), '/');

            ContainerFile containerFile = new ContainerFile(path);
            parameter.addContainerFile(containerFile);
        }

    }

}

