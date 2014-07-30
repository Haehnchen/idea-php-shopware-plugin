package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.actions.GotoRelatedFileAction;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.smarty.SmartyFile;
import de.espend.idea.shopware.ShopwarePluginIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

public class RelatedPopupGotoLineMarker {

    public static class NavigationHandler implements GutterIconNavigationHandler<PsiElement> {

        private List<GotoRelatedItem> items;

        public NavigationHandler(List<GotoRelatedItem> items){
            this.items = items;
        }

        public void navigate(MouseEvent e, PsiElement elt) {
            List<GotoRelatedItem>  items = this.items;
            if (items.size() == 1) {
                items.get(0).navigate();
            } else {
                GotoRelatedFileAction.createPopup(items, "Go to Related Files").show(new RelativePoint(e));
            }

        }

    }

    public static class PopupGotoRelatedItem extends GotoRelatedItem {

        private String customName;
        private Icon icon;
        private Icon smallIcon;

        public PopupGotoRelatedItem(@NotNull PsiElement element) {
            super(element);
        }

        public PopupGotoRelatedItem(@NotNull PsiElement element, String customName) {
            super(element);
            this.customName = customName;
        }

        @Nullable
        @Override
        public String getCustomName() {
            return customName;
        }

        @Nullable
        @Override
        public Icon getCustomIcon() {
            if(this.icon != null) {
                return this.icon;
            }

            return super.getCustomIcon();
        }

        public PopupGotoRelatedItem withIcon(Icon icon, Icon smallIcon) {
            this.icon = icon;
            this.smallIcon = smallIcon;
            return this;
        }

        public Icon getSmallIcon() {
            return smallIcon;
        }

    }

    public static RelatedItemLineMarkerInfo<PsiElement> getSingleLineMarker(SmartyFile smartyFile, Collection<LineMarkerInfo> lineMarkerInfos, GotoRelatedItem gotoRelatedItem) {

        // hell: find any possible small icon
        Icon icon = null;
        if(gotoRelatedItem instanceof RelatedPopupGotoLineMarker.PopupGotoRelatedItem) {
            icon = ((RelatedPopupGotoLineMarker.PopupGotoRelatedItem) gotoRelatedItem).getSmallIcon();
        }

        if(icon == null) {
            icon = ShopwarePluginIcons.SHOPWARE_LINEMARKER;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(icon).
            setTargets(gotoRelatedItem.getElement());

        String customName = gotoRelatedItem.getCustomName();
        if(customName != null) {
            builder.setTooltipText(customName);
        }

        return builder.createLineMarkerInfo(smartyFile);
    }


}
