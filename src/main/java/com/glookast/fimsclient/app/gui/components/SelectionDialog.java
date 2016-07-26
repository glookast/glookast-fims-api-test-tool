package com.glookast.fimsclient.app.gui.components;

import java.awt.Window;
import java.util.List;
import javax.swing.JDialog;

public abstract class SelectionDialog<T> extends JDialog
{

    public SelectionDialog(Window owner, ModalityType modalityType)
    {
        super(owner, modalityType);
    }


    public abstract List<T> getSelectedObjects();

    public abstract boolean isOK();
}
