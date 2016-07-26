package com.glookast.fimsclient.app.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public class OKCancelButtonPanel extends JPanel
{
    private final JButton myOKButton;
    private final JButton myCancelButton;

    public OKCancelButtonPanel(ActionListener actionListener, boolean showOKButton)
    {
        super(new GridBagLayout());

        myOKButton = new JButton("OK");
        myOKButton.setActionCommand("OK");
        myOKButton.addActionListener(actionListener);

        myCancelButton = new JButton("Cancel");
        myCancelButton.setActionCommand("Cancel");
        myCancelButton.addActionListener(actionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        if (showOKButton) {
            c.insets.right = 5;
            add(myOKButton, c);
        }
        c.insets.right = 0;
        c.insets.left = 5;
        add(myCancelButton, c);
    }

    public JButton getOKButton()
    {
        return myOKButton;
    }

    public JButton getCancelButton()
    {
        return myCancelButton;
    }
}
