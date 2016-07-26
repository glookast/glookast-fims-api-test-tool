package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.NewEditDeleteButtonPanel;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ServicesDialog extends SelectionDialog<Service>
{
    private final AppController myController;

    private final List<Service> myServices;
    private final ServiceControllersTableModel myTableModel;
    private final ScrollTable myTable;

    private final ActionListener myActionListener;

    private final NewEditDeleteButtonPanel myNewEditDeletePanel;
    private final OKCancelButtonPanel myOKCancelPanel;

    private boolean myOK;

    public ServicesDialog(Window owner, AppController controller)
    {
        super(owner, ModalityType.APPLICATION_MODAL);
        setTitle("Services");
        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        myController = controller;
        myController.addListener(new EventListenerImpl());

        myActionListener = new ActionListenerImpl();

        myServices = new ArrayList<>();
        myServices.addAll(myController.getServices(Service.Type.Capture));
        myServices.addAll(myController.getServices(Service.Type.Transfer));
        myServices.addAll(myController.getServices(Service.Type.Transform));

        myTableModel = new ServiceControllersTableModel();
        myTable = new ScrollTable(myTableModel);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());
        myTable.getTable().addMouseListener(new MouseAdapterImpl());

        addWindowListener(new WindowAdapterImpl());

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myNewEditDeletePanel = new NewEditDeleteButtonPanel(myActionListener);
        myOKCancelPanel = new OKCancelButtonPanel(myActionListener, false);

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
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;
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
            }
        });
    }

    @Override
    public List<Service> getSelectedObjects()
    {
        return null;
    }

    @Override
    public boolean isOK()
    {
        return myOK;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "New":
                    newService();
                    break;
                case "Edit":
                    editService(myTable.getSelectedRow());
                    break;
                case "Delete":
                    deleteService();
                    break;
                case "Cancel":
                    dispose();
                    break;
            }
        }
    }

    private void newService()
    {
        ServiceDialog dialog = new ServiceDialog(this, myController, true);
        dialog.setVisible(true);
        if (dialog.isOK()) {
            Service service = dialog.getObject();
            myController.setService(service);
            int index = myServices.indexOf(service);
            if (index >= 0) {
                myTable.addRowSelectionInterval(index, index);
            }
        }
    }

    private void editService(int rowIndex)
    {
        if (rowIndex >= 0) {
            ServiceDialog dialog = new ServiceDialog(this, myController, true);
            Service service = myServices.get(myTable.convertRowIndexToModel(rowIndex));
            dialog.setObject(service);
            dialog.setVisible(true);
            if (dialog.isOK()) {
                myController.delService(service);
                myController.setService(dialog.getObject());
            }
        }
    }

    private void deleteService()
    {
        List<Service> services = new ArrayList<>();

        for (int rowIndex : myTable.getTable().getSelectedRows()) {
            services.add(myServices.get(myTable.convertRowIndexToModel(rowIndex)));
        }

        for (Service service : services) {
            myController.delService(service);
        }
    }

    private class ServiceControllersTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Name", "Type", "Method", "Address", "Callback Address"};
        private final int[] myColumnWidths = {300, 100, 60, 300, 300};

        @Override
        public int getTableHeight()
        {
            return 100;
        }

        @Override
        public int getRowCount()
        {
            return myServices.size();
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
            Service service = myServices.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return service.getName();
                case 1:
                    return service.getType();
                case 2:
                    return service.getMethod();
                case 3:
                    return service.getAddress();
                case 4:
                    return service.getCallbackAddress();
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
                editService(myTable.rowAtPoint(e.getPoint()));
            }
        }
    }

    private class EventListenerImpl implements AppController.EventListener
    {
        @Override
        public void onEvent(Event event)
        {
            if (event == Event.Services) {
                myServices.clear();
                myServices.addAll(myController.getServices(Service.Type.Capture));
                myServices.addAll(myController.getServices(Service.Type.Transfer));
                myServices.addAll(myController.getServices(Service.Type.Transform));
                myTableModel.fireTableDataChanged();
                refreshGUI();
            }
        }
    }

    private class WindowAdapterImpl extends WindowAdapter
    {
        public WindowAdapterImpl()
        {
        }

        @Override
        public void windowClosed(WindowEvent e)
        {
            myController.saveProperties();
        }
    }
}
