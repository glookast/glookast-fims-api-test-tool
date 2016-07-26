package com.glookast.fimsclient.app.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public class NewEditDeleteButtonPanel extends JPanel
{
    private final JButton myNewButton;
    private final JButton myEditButton;
    private final JButton myDeleteButton;

    public NewEditDeleteButtonPanel(ActionListener actionListener)
    {
        super(new GridBagLayout());

        myNewButton = new JButton("New");
        myNewButton.setActionCommand("New");
        myNewButton.addActionListener(actionListener);

        myEditButton = new JButton("Edit");
        myEditButton.setActionCommand("Edit");
        myEditButton.addActionListener(actionListener);

        myDeleteButton = new JButton("Delete");
        myDeleteButton.setActionCommand("Delete");
        myDeleteButton.addActionListener(actionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets.right = 5;
        add(myNewButton, c);
        c.insets.left = 5;
        add(myEditButton, c);
        c.insets.right = 0;
        add(myDeleteButton, c);
        c.insets.left = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(Box.createGlue(), c);
    }

    public JButton getNewButton()
    {
        return myNewButton;
    }

    public JButton getEditButton()
    {
        return myEditButton;
    }

    public JButton getDeleteButton()
    {
        return myDeleteButton;
    }
}
