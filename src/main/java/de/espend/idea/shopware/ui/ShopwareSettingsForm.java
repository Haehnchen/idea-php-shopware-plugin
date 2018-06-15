package de.espend.idea.shopware.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import de.espend.idea.shopware.ShopwareApplicationSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareSettingsForm implements Configurable {
    @NotNull
    private final Project project;

    private JButton defaultCliToolsPathButton;
    private TextFieldWithBrowseButton cliToolsPharPathTextField;
    private JPanel panel;

    public ShopwareSettingsForm(@NotNull final Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Shopware Settings";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        defaultCliToolsPathButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliToolsPharPathTextField.getTextField().setText(ShopwareApplicationSettings.DEFAULT_CLI_URL);
            }
        });

        cliToolsPharPathTextField.getButton().addMouseListener(createPathButtonMouseListener(
            cliToolsPharPathTextField.getTextField(),
            FileChooserDescriptorFactory.createSingleFileDescriptor("phar"))
        );

        return panel;
    }

    @Override
    public boolean isModified() {
        return !this.cliToolsPharPathTextField.getText().equals(ShopwareApplicationSettings.getInstance().cliToolsPharUrl);
    }

    @Override
    public void apply() {
        ShopwareApplicationSettings.getInstance().cliToolsPharUrl = this.cliToolsPharPathTextField.getText();
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    @Override
    public void disposeUIResources() {
    }

    private void updateUIFromSettings() {
        this.cliToolsPharPathTextField.setText(
            ShopwareApplicationSettings.getInstance().cliToolsPharUrl
        );
    }

    private MouseListener createPathButtonMouseListener(final JTextField textField, final FileChooserDescriptor fileChooserDescriptor) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                VirtualFile projectDirectory = project.getBaseDir();
                VirtualFile selectedFile = FileChooser.chooseFile(
                    fileChooserDescriptor,
                    project,
                    VfsUtil.findRelativeFile(textField.getText(), projectDirectory)
                );

                if (null == selectedFile) {
                    return; // Ignore but keep the previous path
                }

                textField.setText(selectedFile.getPath());
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        };
    }
}
