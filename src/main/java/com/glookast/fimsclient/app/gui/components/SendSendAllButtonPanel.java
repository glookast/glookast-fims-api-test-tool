package com.glookast.fimsclient.app.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public class SendSendAllButtonPanel extends JPanel
{
    private final JButton mySendButton;
    private final JButton mySendAllButton;

    public SendSendAllButtonPanel(ActionListener actionListener)
    {
        super(new GridBagLayout());

        mySendButton = new JButton("Send");
        mySendButton.setActionCommand("Send");
        mySendButton.addActionListener(actionListener);

        mySendAllButton = new JButton("Send All");
        mySendAllButton.setActionCommand("Send All");
        mySendAllButton.addActionListener(actionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.insets.right = 5;
        add(mySendButton, c);
        c.insets.right = 0;
        c.insets.left = 5;
        add(mySendAllButton, c);
    }

    public JButton getSendButton()
    {
        return mySendButton;
    }

    public JButton getSendAllButton()
    {
        return mySendAllButton;
    }
}
