package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.OKCancelButtonPanel;
import com.glookast.fimsclient.app.gui.components.ScrollTable;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tv.fims.base.ContainerFormatType;

public class ContainerFormatsDialog extends JDialog
{
    private final AppController myController;

    private final ActionListener myActionListener;

    private List<ContainerFormatType> myContainerFormats;
    private final ScrollTableModel myTableModel;
    private final ScrollTable myTable;

    private final OKCancelButtonPanel myOKCancelPanel;

    private boolean myOK;

    private ContainerFormatType mySelectedContainerFormat;

    public ContainerFormatsDialog(Window owner, AppController controller, Service.Type serviceType)
    {
        super(owner, "Container Formats", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        myController = controller;

        myActionListener = new ActionListenerImpl();

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myContainerFormats = new ArrayList<>();
        if (myController != null) {
            myContainerFormats.addAll(myController.getContainerFormats(serviceType));
        }
        myTableModel = new ContainerFormatsTableModel();
        myTable = new ScrollTable(myTableModel);
        myTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());

        myOKCancelPanel = new OKCancelButtonPanel(myActionListener, true);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(myTable, c);
        add(myOKCancelPanel, c);

        pack();
        setLocationRelativeTo(owner);
    }

    private void refreshGUI()
    {
    }

    public boolean isOK()
    {
        return myOK;
    }

    public ContainerFormatType getContainerFormat()
    {
        return mySelectedContainerFormat;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "OK":
                    int index = myTable.convertRowIndexToModel(myTable.getSelectedRow());
                    mySelectedContainerFormat = (index >= 0) ? myContainerFormats.get(index) : null;
                    myOK = true;
                    dispose();
                    break;
                case "Cancel":
                    dispose();
                    break;
            }
        }
    }

    private class ContainerFormatsTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Name"};
        private final int[] myColumnWidths = {500 };

        @Override
        public int getTableHeight()
        {
            return 200;
        }

        @Override
        public int getRowCount()
        {
            return myContainerFormats.size();
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
            ContainerFormatType audioFormat = myContainerFormats.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    if (audioFormat.getContainerFormat() == null)
                        return "";
                    return audioFormat.getContainerFormat().getValue();
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
