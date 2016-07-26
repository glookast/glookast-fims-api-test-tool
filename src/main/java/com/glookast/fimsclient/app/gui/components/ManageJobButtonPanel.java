package com.glookast.fimsclient.app.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class ManageJobButtonPanel extends JPanel
{
    private final JButton myViewButton;
    private final JButton myPriorityButton;
    private final JButton myCancelButton;
    private final JButton myStopButton;
    private final JButton myCleanupButton;

    private final JCheckBox myAllowIllegalActionsCheckBox;

    public ManageJobButtonPanel(ActionListener actionListener)
    {
        super(new GridBagLayout());

        myViewButton = new JButton("View");
        myViewButton.setActionCommand("View");
        myViewButton.addActionListener(actionListener);

        myPriorityButton = new JButton("Modify Priority");
        myPriorityButton.setActionCommand("Priority");
        myPriorityButton.addActionListener(actionListener);

        myCancelButton = new JButton("Cancel");
        myCancelButton.setActionCommand("Cancel");
        myCancelButton.addActionListener(actionListener);

        myStopButton = new JButton("Stop");
        myStopButton.setActionCommand("Stop");
        myStopButton.addActionListener(actionListener);

        myCleanupButton = new JButton("Clean Up");
        myCleanupButton.setActionCommand("Clean");
        myCleanupButton.addActionListener(actionListener);

        myAllowIllegalActionsCheckBox = new JCheckBox("Allow illegal actions");
        myAllowIllegalActionsCheckBox.setActionCommand("AllowIllegalActions");
        myAllowIllegalActionsCheckBox.addActionListener(actionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.insets.right = 5;
        add(myViewButton, c);
        c.insets.left = 5;
        add(myPriorityButton, c);
        add(myCancelButton, c);
        add(myStopButton, c);
        add(myCleanupButton, c);
        c.insets.right = 0;
        //add(myAllowIllegalActionsCheckBox, c);
        c.insets.left = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(Box.createGlue(), c);
    }

    public JButton getViewButton()
    {
        return myViewButton;
    }

    public JButton getPriorityButton()
    {
        return myPriorityButton;
    }

    public JButton getCancelButton()
    {
        return myCancelButton;
    }

    public JButton getStopButton()
    {
        return myStopButton;
    }

    public JButton getCleanupButton()
    {
        return myCleanupButton;
    }

    public JCheckBox getAllowIllegalActionsCheckBox()
    {
        return myAllowIllegalActionsCheckBox;
    }


}
