package de.espend.idea.shopware.installer.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.ui.UIUtil;
import de.espend.idea.shopware.installer.project.dict.ShopwareInstallerVersion;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ShopwareInstallerForm {

    private JComboBox comboVersions;
    private JButton buttonRefresh;
    private JPanel mainPanel;
    private JPanel panelInterpreter;
    private JCheckBox checkBoxDemo;

    public ShopwareInstallerForm() {
        buttonRefresh.addActionListener(e -> appendShopwareVersions());

        // @TODO: use com.intellij.util.ui.ReloadableComboBoxPanel in Phpstorm9 api level
        comboVersions.setRenderer(new ListCellRenderer());
        appendShopwareVersions();
    }

    @Nullable
    private List<ShopwareInstallerVersion> getVersions() {

        String content = ShopwareInstallerUtil.getDownloadVersions();
        if(content == null) {
            return null;
        }

        return ShopwareInstallerUtil.getVersions(content);
    }

    private void appendShopwareVersions()
    {

        comboVersions.setModel(new ListComboBoxModel<>(new ArrayList<ShopwareInstallerVersion>()));

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final List<ShopwareInstallerVersion> shopwareInstallerVersions1 = getVersions();
            if (shopwareInstallerVersions1 != null) {
                UIUtil.invokeLaterIfNeeded(() -> comboVersions.setModel(new ListComboBoxModel<>(shopwareInstallerVersions1)));
            }
        });

    }

    public JComponent getContentPane()
    {
        return this.mainPanel;
    }

    private static class ListCellRenderer extends ListCellRendererWrapper<ShopwareInstallerVersion> {

        @Override
        public void customize(JList list, ShopwareInstallerVersion value, int index, boolean selected, boolean hasFocus) {
            if(value != null) {
                setText(value.getPresentableName());
            }
        }
    }

    public ShopwareInstallerVersion getVersion() {

        Object selectedItem = this.comboVersions.getSelectedItem();
        if(selectedItem instanceof ShopwareInstallerVersion) {
            return ((ShopwareInstallerVersion) selectedItem);
        }

        return null;
    }
}
