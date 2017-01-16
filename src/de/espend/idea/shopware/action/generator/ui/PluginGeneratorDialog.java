package de.espend.idea.shopware.action.generator.ui;

import com.intellij.openapi.project.ProjectManager;
import com.jetbrains.php.composer.InterpretersComboWithBrowseButton;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.action.generator.dict.PluginGeneratorSettings;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PluginGeneratorDialog extends JDialog {
    @NotNull
    private final Callback callback;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textPluginName;
    private JCheckBox filterCheckBox;
    private JComboBox namespaceComboBox;
    private JCheckBox addDummyFrontendControllerCheckBox;
    private JCheckBox addDummyBackendControllerCheckBox;
    private JCheckBox addDummyModelsCheckBox;
    private JCheckBox addDummyCommandCheckBox;
    private JCheckBox addDummyWidgetCheckBox;
    private JCheckBox addDummyApiCheckBox;
    private JPanel panelInterpreter;
    private InterpretersComboWithBrowseButton interpretersComboWithBrowseButton;

    public PluginGeneratorDialog(@NotNull Callback callback) {
        this.callback = callback;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e ->
            onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        this.namespaceComboBox.addItem("Frontend");
        this.namespaceComboBox.addItem("Core");
        this.namespaceComboBox.addItem("Backend");

        this.namespaceComboBox.setSelectedIndex(0);
    }

    private void onOK() {
        if (StringUtils.isBlank(textPluginName.getText())) {
            return;
        }

        this.callback.onOk(new PluginGeneratorSettings(
            textPluginName.getText(),
            (String) namespaceComboBox.getSelectedItem(),
            filterCheckBox.isSelected(),
            addDummyFrontendControllerCheckBox.isSelected(),
            addDummyBackendControllerCheckBox.isSelected(),
            addDummyModelsCheckBox.isSelected(),
            addDummyCommandCheckBox.isSelected(),
            addDummyWidgetCheckBox.isSelected(),
            addDummyApiCheckBox.isSelected(),
            getInterpreter()
        ));

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static PluginGeneratorDialog createAndShow(@NotNull Callback callback) {
        PluginGeneratorDialog dialog = new PluginGeneratorDialog(callback);
        dialog.setTitle("Shopware: Plugin Generator");
        dialog.setIconImage(Symfony2Icons.getImage(ShopwarePluginIcons.SHOPWARE));
        dialog.pack();

        dialog.setMinimumSize(new Dimension(350, 250));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        return dialog;
    }

    private String getInterpreter() {
        String text = interpretersComboWithBrowseButton.getText();
        if(StringUtils.isNotBlank(text)) {
            return text;
        }
        return this.interpretersComboWithBrowseButton.getPhpPath();
    }

    private void createUIComponents() {
        panelInterpreter = interpretersComboWithBrowseButton = new InterpretersComboWithBrowseButton(ProjectManager.getInstance().getDefaultProject());
    }

    public interface Callback {
        void onOk(@NotNull PluginGeneratorSettings settings);
    }
}
