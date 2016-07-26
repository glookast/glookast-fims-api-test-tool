package com.glookast.fimsclient.app.gui.components;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ScrollTable extends JScrollPane implements ListSelectionModel
{
    private final JTable myTable;
    private final int myTotalWidth;

    public ScrollTable(ScrollTableModel tableModel)
    {
        myTable = new JTable(tableModel);
        setViewportView(myTable);
        myTable.setRowSelectionAllowed(true);
        myTable.setColumnSelectionAllowed(false);

        int total = 0;
        int count = tableModel.getColumnCount();
        TableColumnModel model = myTable.getColumnModel();
        for (int i = 0; i < count; i++) {
            TableColumn column = model.getColumn(i);
            int width = tableModel.getColumnWidth(i);
            column.setPreferredWidth(width);
            total += width + 1;
        }

        myTotalWidth = total;

        setMinimumSize(new Dimension(myTotalWidth, tableModel.getTableHeight()));
        setPreferredSize(new Dimension(myTotalWidth, tableModel.getTableHeight()));
        myTable.setFillsViewportHeight(true);
    }

    public JTable getTable()
    {
        return myTable;
    }

    public int getRowCount()
    {
        return myTable.getRowCount();
    }

    public int getColumnCount()
    {
        return myTable.getColumnCount();
    }

    public int getSelectedRowCount()
    {
        return myTable.getSelectedRowCount();
    }

    public int getSelectedRow()
    {
        return myTable.getSelectedRow();
    }

    public int[] getSelectedRows()
    {
        return myTable.getSelectedRows();
    }

    public void addRowSelectionInterval(int index0, int index1)
    {
        myTable.addRowSelectionInterval(index0, index1);
    }

    public int convertRowIndexToModel(int viewRowIndex)
    {
        return myTable.convertRowIndexToModel(viewRowIndex);
    }

    public int rowAtPoint(Point point)
    {
        return myTable.rowAtPoint(point);
    }

    @Override
    public void setSelectionInterval(int index0, int index1)
    {
        myTable.getSelectionModel().setSelectionInterval(index0, index1);
    }

    @Override
    public void addSelectionInterval(int index0, int index1)
    {
        myTable.getSelectionModel().addSelectionInterval(index0, index1);
    }

    @Override
    public void removeSelectionInterval(int index0, int index1)
    {
        myTable.getSelectionModel().removeSelectionInterval(index0, index1);
    }

    @Override
    public int getMinSelectionIndex()
    {
        return myTable.getSelectionModel().getMinSelectionIndex();
    }

    @Override
    public int getMaxSelectionIndex()
    {
        return myTable.getSelectionModel().getMaxSelectionIndex();
    }

    @Override
    public boolean isSelectedIndex(int index)
    {
        return myTable.getSelectionModel().isSelectedIndex(index);
    }

    @Override
    public int getAnchorSelectionIndex()
    {
        return myTable.getSelectionModel().getAnchorSelectionIndex();
    }

    @Override
    public void setAnchorSelectionIndex(int index)
    {
        myTable.getSelectionModel().setAnchorSelectionIndex(index);
    }

    @Override
    public int getLeadSelectionIndex()
    {
        return myTable.getSelectionModel().getLeadSelectionIndex();
    }

    @Override
    public void setLeadSelectionIndex(int index)
    {
        myTable.getSelectionModel().setLeadSelectionIndex(index);
    }

    @Override
    public void clearSelection()
    {
        myTable.getSelectionModel().clearSelection();
    }

    @Override
    public boolean isSelectionEmpty()
    {
        return myTable.getSelectionModel().isSelectionEmpty();
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before)
    {
        myTable.getSelectionModel().insertIndexInterval(index, length, before);
    }

    @Override
    public void removeIndexInterval(int index0, int index1)
    {
        myTable.getSelectionModel().removeIndexInterval(index0, index1);
    }

    @Override
    public void setValueIsAdjusting(boolean valueIsAdjusting)
    {
        myTable.getSelectionModel().setValueIsAdjusting(valueIsAdjusting);
    }

    @Override
    public boolean getValueIsAdjusting()
    {
        return myTable.getSelectionModel().getValueIsAdjusting();
    }

    @Override
    public void setSelectionMode(int selectionMode)
    {
        myTable.getSelectionModel().setSelectionMode(selectionMode);
    }

    @Override
    public int getSelectionMode()
    {
        return myTable.getSelectionModel().getSelectionMode();
    }

    @Override
    public void addListSelectionListener(ListSelectionListener x)
    {
        myTable.getSelectionModel().addListSelectionListener(x);
    }

    @Override
    public void removeListSelectionListener(ListSelectionListener x)
    {
        myTable.getSelectionModel().removeListSelectionListener(x);
    }
}
