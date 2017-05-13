package de.espend.idea.shopware.symfony;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.shopware.ShopwareProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.extension.DoctrineModelProvider;
import fr.adrienbrault.idea.symfony2plugin.extension.DoctrineModelProviderParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareDoctrineModelProvider implements DoctrineModelProvider {
    @Override
    public Collection<DoctrineModelProviderParameter.DoctrineModel> collectModels(DoctrineModelProviderParameter parameter) {
        if(!ShopwareProjectComponent.isValidForProject(parameter.getProject())) {
            return Collections.emptyList();
        }

        List<DoctrineModelProviderParameter.DoctrineModel> doctrineModels = new ArrayList<>();

        for(PhpClass phpClass: PhpIndex.getInstance(parameter.getProject()).getAllSubclasses("\\Shopware\\Components\\Model\\ModelEntity")) {
            doctrineModels.add(new DoctrineModelProviderParameter.DoctrineModel(phpClass));
        }

        return doctrineModels;
    }

}
