package com.glookast.fimsclient.app.gui.components;

import com.glookast.fimsclient.app.AppController;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public abstract class ViewEditDialog<T> extends JDialog
{
    protected final AppController myController;

    private final ActionListener myActionListener;

    protected final JPanel myPanel;
    private final OKCancelButtonPanel myOKCancelPanel;

    protected final boolean myEditable;

    private boolean myOK;

    protected T myObject;

    public ViewEditDialog(Window owner, AppController controller, boolean editable)
    {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        myController = controller;

        myEditable = editable;

        myActionListener = new ActionListenerImpl();

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myPanel = new JPanel(new GridBagLayout());

        myOKCancelPanel = new OKCancelButtonPanel(myActionListener, myEditable);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        add(myPanel, c);

        c.insets = new Insets(5, 5, 5, 5);
        add(myOKCancelPanel, c);
    }

    protected void refreshGUI()
    {
    }

    protected abstract String getObjectName();

    protected final void setTitle()
    {
        String modeType = (myObject == null) ? "New" : (myEditable) ? "Edit" : "View";
        setTitle(modeType + " " + getObjectName());
    }

    public final boolean isOK()
    {
        return myOK;
    }

    public final T getObject()
    {
        return myObject;
    }

    public final void setObject(T object)
    {
        myObject = object;
        setTitle();
        loadObject();
        refreshGUI();
    }

    protected abstract boolean saveObject();

    protected abstract void loadObject();

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "OK":
                    if (saveObject()) {
                        myOK = true;
                        dispose();
                    }
                    break;
                case "Cancel":
                    dispose();
                    break;
            }
        }
    }
}
