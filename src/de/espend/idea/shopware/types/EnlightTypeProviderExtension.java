package de.espend.idea.shopware.types;

import fr.adrienbrault.idea.symfony2plugin.assistant.signature.MethodSignatureSetting;
import fr.adrienbrault.idea.symfony2plugin.extension.MethodSignatureTypeProviderExtension;
import fr.adrienbrault.idea.symfony2plugin.extension.MethodSignatureTypeProviderParameter;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class EnlightTypeProviderExtension implements MethodSignatureTypeProviderExtension {

    @Override
    public Collection<MethodSignatureSetting> getSignatures(MethodSignatureTypeProviderParameter parameter) {
        return Collections.singletonList(new MethodSignatureSetting("\\Enlight_Controller_Action", "get", 0, "Service"));
    }

}
