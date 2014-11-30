package de.espend.idea.shopware.lookup;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.icons.AllIcons;
import de.espend.idea.shopware.ShopwarePluginIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class TemplateLookupElement extends LookupElement {

    final private String templateName;

    public TemplateLookupElement(String templateName) {
        this.templateName = templateName;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return templateName;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);
        if(this.templateName.endsWith("tpl")) {
            presentation.setIcon(ShopwarePluginIcons.SHOPWARE_SMARTY);
        }
        if(this.templateName.endsWith("js")) {
            presentation.setIcon(AllIcons.FileTypes.JavaScript);
        }
    }

}
