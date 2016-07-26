package com.glookast.fimsclient.app.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ImportExportButtonPanel extends JPanel
{
    private final JButton myImportButton;
    private final JButton myExportButton;

    public ImportExportButtonPanel(ActionListener actionListener)
    {
        super(new GridBagLayout());

        myImportButton = new JButton("Import");
        myImportButton.setActionCommand("Import");
        myImportButton.addActionListener(actionListener);

        myExportButton = new JButton("Export");
        myExportButton.setActionCommand("Export");
        myExportButton.addActionListener(actionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets.right = 5;
        add(myImportButton, c);
        c.insets.left = 5;
        c.insets.right = 0;
        add(myExportButton, c);
        c.insets.left = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(Box.createGlue(), c);
    }

    public JButton getImportButton()
    {
        return myImportButton;
    }

    public JButton getExportButton()
    {
        return myExportButton;
    }
}
