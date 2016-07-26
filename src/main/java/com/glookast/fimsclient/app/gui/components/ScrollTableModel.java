package com.glookast.fimsclient.app.gui.components;

import javax.swing.table.AbstractTableModel;

public abstract class ScrollTableModel extends AbstractTableModel
{
    public abstract int getTableHeight();
    public abstract int getColumnWidth(int column);
}
