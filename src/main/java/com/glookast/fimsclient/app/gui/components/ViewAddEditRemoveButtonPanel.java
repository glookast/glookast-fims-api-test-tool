package com.glookast.fimsclient.app.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ViewAddEditRemoveButtonPanel extends JPanel
{
    private final JButton myViewButton;
    private final JButton myAddButton;
    private final JButton myEditButton;
    private final JButton myRemoveButton;

    public ViewAddEditRemoveButtonPanel(ActionListener actionListener, boolean editable)
    {
        super(new GridBagLayout());

        myViewButton = new JButton("View");
        myViewButton.setActionCommand("View");
        myViewButton.addActionListener(actionListener);

        myAddButton = new JButton("Add");
        myAddButton.setActionCommand("Add");
        myAddButton.addActionListener(actionListener);

        myEditButton = new JButton("Edit");
        myEditButton.setActionCommand("Edit");
        myEditButton.addActionListener(actionListener);

        myRemoveButton = new JButton("Remove");
        myRemoveButton.setActionCommand("Remove");
        myRemoveButton.addActionListener(actionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        if (editable) {
            c.insets.right = 5;
            add(myAddButton, c);
            c.insets.left = 5;
            add(myEditButton, c);
            c.insets.right = 0;
            add(myRemoveButton, c);
            c.insets.left = 0;
        } else {
            add(myViewButton, c);
        }
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(Box.createGlue(), c);
    }

    public JButton getViewButton()
    {
        return myViewButton;
    }

    public JButton getAddButton()
    {
        return myAddButton;
    }

    public JButton getEditButton()
    {
        return myEditButton;
    }

    public JButton getRemoveButton()
    {
        return myRemoveButton;
    }
}
