package com.glookast.fimsclient.app.gui.components;

import com.glookast.fimsclient.app.AppController;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public abstract class TablePanel<T> extends JPanel
{
    protected final AppController myController;

    private final ActionListener myActionListener;

    private final boolean myEditable;

    private final ScrollTableModel myTableModel;
    private final ScrollTable myTable;
    private final ViewAddEditRemoveButtonPanel myViewAddEditRemovePanel;

    private final List<T> myObjects;

    public TablePanel(String title, List<T> objects, ScrollTableModel tableModel, AppController controller, boolean editable)
    {
        super(new GridBagLayout());

        myController = controller;
        myEditable = editable;
        myObjects = objects;

        myActionListener = new ActionListenerImpl();

        JLabel lblObjects = new JLabel(title);
        myTableModel = tableModel;
        myTable = new ScrollTable(myTableModel);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());
        myTable.getTable().addMouseListener(new MouseAdapterImpl());

        myViewAddEditRemovePanel = new ViewAddEditRemoveButtonPanel(myActionListener, myEditable);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        add(lblObjects, c);
        add(myTable, c);
        add(myViewAddEditRemovePanel, c);

        refreshGUI();
    }

    private void refreshGUI()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                int selectedRowCount = myTable.getSelectedRowCount();
                myViewAddEditRemovePanel.getEditButton().setEnabled(selectedRowCount == 1);
                myViewAddEditRemovePanel.getRemoveButton().setEnabled(selectedRowCount > 0);
            }
        });
    }

    protected SelectionDialog<T> getSelectionDialog(Window owner, AppController controller)
    {
        return null;
    }

    protected abstract ViewEditDialog<T> getViewEditDialog(Window owner, AppController controller, boolean editable);

    protected abstract String getKey(T object);

    private void addObject()
    {
        SelectionDialog<T> selectionDialog = getSelectionDialog((Window)SwingUtilities.getRoot(this), myController);
        if (selectionDialog != null) {
            selectionDialog.setVisible(true);
            if (selectionDialog.isOK()) {
                List<T> newObjects = selectionDialog.getSelectedObjects();
                for (T newObject : newObjects) {
                    for (Iterator<T> it = myObjects.iterator(); it.hasNext();) {
                        T object = it.next();
                        if (Objects.equals(getKey(newObject), getKey(object))) {
                            it.remove();
                        }
                    }
                }
                myObjects.addAll(newObjects);
                myTableModel.fireTableDataChanged();
                refreshGUI();
            }
        } else {
            ViewEditDialog<T> viewEditDialog = getViewEditDialog((Window)SwingUtilities.getRoot(this), myController, true);
            viewEditDialog.setVisible(true);
            if (viewEditDialog.isOK()) {
                T newObject = viewEditDialog.getObject();
                for (Iterator<T> it = myObjects.iterator(); it.hasNext();) {
                    T object = it.next();
                    if (Objects.equals(getKey(newObject), getKey(object))) {
                        it.remove();
                    }
                }
                myObjects.add(newObject);
                myTableModel.fireTableDataChanged();
                refreshGUI();
            }
        }
    }

    private void viewEditObject(int rowIndex, boolean editable)
    {
        if (rowIndex >= 0) {
            ViewEditDialog<T> dialog = getViewEditDialog((Window)SwingUtilities.getRoot(this), myController, editable);
            T oldObject = myObjects.get(myTable.convertRowIndexToModel(rowIndex));
            dialog.setObject(oldObject);
            dialog.setVisible(true);
            if (dialog.isOK()) {
                myObjects.remove(oldObject);
                T newObject = dialog.getObject();
                for (Iterator<T> it = myObjects.iterator(); it.hasNext();) {
                    T object = it.next();
                    if (Objects.equals(getKey(newObject), getKey(object))) {
                        it.remove();
                    }
                }
                myObjects.add(newObject);
                myTableModel.fireTableDataChanged();
                setObject(newObject);
            }
        }
    }

    protected void setObject(T object)
    {
    }

    private void removeObject()
    {
        List<T> objects = new ArrayList<>();

        for (int rowIndex : myTable.getSelectedRows()) {
            objects.add(myObjects.get(myTable.convertRowIndexToModel(rowIndex)));
        }

        myObjects.removeAll(objects);
        myTableModel.fireTableDataChanged();
        refreshGUI();
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "Add":
                    addObject();
                    break;
                case "View":
                    viewEditObject(myTable.getSelectedRow(), false);
                    break;
                case "Edit":
                    viewEditObject(myTable.getSelectedRow(), true);
                    break;
                case "Remove":
                    removeObject();
            }
        }
    }

    private class ListSelectionListenerImpl implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            refreshGUI();
        }
    }

    private class MouseAdapterImpl extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getClickCount() == 2) {
                viewEditObject(myTable.rowAtPoint(e.getPoint()), myEditable);
            }
        }
    }
}
