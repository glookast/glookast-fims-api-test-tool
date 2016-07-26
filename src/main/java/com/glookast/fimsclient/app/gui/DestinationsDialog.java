package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.OKCancelButtonPanel;
import com.glookast.fimsclient.app.gui.components.ScrollTable;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import com.glookast.fimsclient.app.gui.components.SelectionDialog;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tv.fims.base.TransferAtomType;

public class DestinationsDialog extends SelectionDialog<TransferAtomType>
{
    private final AppController myController;

    private List<TransferAtomType> myDestinations;
    private final DestinationsTableModel myTableModel;
    private final ScrollTable myTable;

    private final ActionListener myActionListener;

    private final OKCancelButtonPanel myOKCancelPanel;

    private final List<TransferAtomType> mySelectedDestinations;
    private boolean myOK;

    public DestinationsDialog(Window owner, AppController controller, Service.Type serviceType)
    {
        super(owner, ModalityType.APPLICATION_MODAL);
        setTitle("Destinations");
        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        myController = controller;

        myActionListener = new ActionListenerImpl();

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myDestinations = myController.getDestinations(serviceType);
        myTableModel = new DestinationsTableModel();
        myTable = new ScrollTable(myTableModel);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());

        mySelectedDestinations = new ArrayList<>();

        myOKCancelPanel = new OKCancelButtonPanel(myActionListener, true);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 5, 5);
        add(myTable, c);
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;
        c.gridx = 1;
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
                boolean hasSelection = (selectedRowCount > 0);

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
    public List<TransferAtomType> getSelectedObjects()
    {
        return Collections.unmodifiableList(mySelectedDestinations);
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "OK":
                    setSelectedDestinations();
                    myOK = true;
                    dispose();
                    break;
                case "Cancel":
                    dispose();
                    break;
            }
        }
    }

    private void setSelectedDestinations()
    {
        mySelectedDestinations.clear();
        for (int rowIndex : myTable.getSelectedRows()) {
            mySelectedDestinations.add(myDestinations.get(myTable.convertRowIndexToModel(rowIndex)));
        }
    }

    private class DestinationsTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = { "Destination" };
        private final int[] myColumnWidths = { 500 };

        @Override
        public int getTableHeight()
        {
            return 100;
        }

        @Override
        public int getRowCount()
        {
            return myDestinations.size();
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
            TransferAtomType transferAtom = myDestinations.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return transferAtom.getDestination();
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
}
