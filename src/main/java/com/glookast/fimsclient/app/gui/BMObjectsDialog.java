package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.AppController.EventListener;
import com.glookast.fimsclient.app.gui.components.ImportExportButtonPanel;
import com.glookast.fimsclient.app.gui.components.NewEditDeleteButtonPanel;
import com.glookast.fimsclient.app.gui.components.OKCancelButtonPanel;
import com.glookast.fimsclient.app.gui.components.ScrollTable;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import com.glookast.fimsclient.app.gui.components.SelectionDialog;
import com.glookast.fimsclient.app.utils.StringUtils;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tv.fims.base.BMObjectType;

public class BMObjectsDialog extends SelectionDialog<BMObjectType>
{
    private AppController myController;

    private List<BMObjectType> myBmObjects;
    private final BMObjectTableModel myTableModel;
    private final ScrollTable myTable;

    private final ActionListener myActionListener;

    private final NewEditDeleteButtonPanel myNewEditDeletePanel;
    private final ImportExportButtonPanel myImportExportPanel;
    private final OKCancelButtonPanel myOKCancelPanel;

    private final List<BMObjectType> mySelectedBmObjects;
    private boolean myOK;

    public BMObjectsDialog(Window owner, AppController controller)
    {
        super(owner, ModalityType.APPLICATION_MODAL);
        setTitle("Business Media Objects");

        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        myController = controller;
        myController.addListener(new FimsClientAppControllerEventListenerImpl());

        myActionListener = new ActionListenerImpl();

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myBmObjects = myController.getBmObjects();
        myTableModel = new BMObjectTableModel();
        myTable = new ScrollTable(myTableModel);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());
        myTable.getTable().addMouseListener(new MouseAdapterImpl());

        mySelectedBmObjects = new ArrayList<>();

        myNewEditDeletePanel = new NewEditDeleteButtonPanel(myActionListener);
        myImportExportPanel = new ImportExportButtonPanel(myActionListener);
        myOKCancelPanel = new OKCancelButtonPanel(myActionListener, true);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        add(myTable, c);
        c.gridy = 1;
        c.gridwidth = 1;
        add(myNewEditDeletePanel, c);
        c.gridx = 1;
        add(myImportExportPanel, c);
        c.gridx = 2;
        add(myOKCancelPanel, c);

        pack();
        setLocationRelativeTo(owner);
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
                boolean singleSelection = (selectedRowCount == 1);
                boolean hasSelection = (selectedRowCount > 0);

                myNewEditDeletePanel.getEditButton().setEnabled(singleSelection);
                myNewEditDeletePanel.getDeleteButton().setEnabled(hasSelection);
                myImportExportPanel.getExportButton().setEnabled(hasSelection);
                myOKCancelPanel.getOKButton().setEnabled(hasSelection);
            }
        });
    }

    @Override
    public boolean isOK()
    {
        return myOK;
    }

    @Override
    public List<BMObjectType> getSelectedObjects()
    {
        return Collections.unmodifiableList(mySelectedBmObjects);
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "New":
                    newBmObject();
                    break;
                case "Edit":
                    editBmObject(myTable.getSelectedRow());
                    break;
                case "Delete":
                    deleteBmObject();
                    break;
                case "Import":
                    importBmObjects();
                    break;
                case "Export":
                    exportBmObjects();
                    break;
                case "OK":
                    setSelectedBmObjects();
                    myOK = true;
                    dispose();
                    break;
                case "Cancel":
                    dispose();
                    break;
            }
        }
    }

    private void newBmObject()
    {
        BMObjectDialog dialog = new BMObjectDialog(this, myController, true);
        dialog.setVisible(true);
        if (dialog.isOK()) {
            BMObjectType bmObject = dialog.getObject();
            myController.setBmObject(bmObject);
            int index = myBmObjects.indexOf(bmObject);
            if (index >= 0) {
                myTable.addRowSelectionInterval(index, index);
            }
        }
    }

    private void editBmObject(int rowIndex)
    {
        if (rowIndex >= 0) {
            BMObjectDialog dialog = new BMObjectDialog(this, myController, true);
            dialog.setObject(myBmObjects.get(myTable.convertRowIndexToModel(rowIndex)));
            dialog.setVisible(true);
            if (dialog.isOK()) {
                myController.setBmObject(dialog.getObject());
            }
        }
    }

    private void deleteBmObject()
    {
        List<BMObjectType> bmObjects = new ArrayList<>();

        for (int rowIndex : myTable.getTable().getSelectedRows()) {
            bmObjects.add(myBmObjects.get(myTable.convertRowIndexToModel(rowIndex)));
        }

        for (BMObjectType bmObject : bmObjects) {
            myController.delBmObject(bmObject);
        }
    }

    private void importBmObjects()
    {
        //XMLUtils.doImport(this, myController, false, false, false, false, false, false, true);
    }

    private void exportBmObjects()
    {
        List<BMObjectType> bmObjects = new ArrayList<>();

        for (int rowIndex : myTable.getSelectedRows()) {
            bmObjects.add(myBmObjects.get(myTable.convertRowIndexToModel(rowIndex)));
        }

        //XMLUtils.doExport(this, null, null, bmObjects, false, false, false, false, false, false, true);
    }

    private void setSelectedBmObjects()
    {
        mySelectedBmObjects.clear();
        for (int rowIndex : myTable.getSelectedRows()) {
            mySelectedBmObjects.add(myBmObjects.get(myTable.convertRowIndexToModel(rowIndex)));
        }
    }

    private class FimsClientAppControllerEventListenerImpl implements AppController.EventListener
    {
        @Override
        public void onEvent(EventListener.Event event)
        {
            if (event == Event.BmObjects) {
                myBmObjects = myController.getBmObjects();
                myTableModel.fireTableDataChanged();
                refreshGUI();
            }
        }
    }

    private class BMObjectTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = { "Description" };
        private final int[] myColumnWidths = { 500 };

        @Override
        public int getTableHeight()
        {
            return 100;
        }

        @Override
        public int getRowCount()
        {
            return myBmObjects.size();
        }

        @Override
        public int getColumnCount()
        {
            return myColumnNames.length;
        }

        @Override
        public String getColumnName(int column)
        {
            return myColumnNames[column];
        }

        @Override
        public int getColumnWidth(int column)
        {
            return myColumnWidths[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            BMObjectType bmObject = myBmObjects.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return StringUtils.toString(bmObject);
            }
            throw new IndexOutOfBoundsException();
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
                editBmObject(myTable.rowAtPoint(e.getPoint()));
            }
        }
    }
}
