package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.ImportExportButtonPanel;
import com.glookast.fimsclient.app.gui.components.JobsTableModel;
import com.glookast.fimsclient.app.gui.components.NewEditDeleteButtonPanel;
import com.glookast.fimsclient.app.gui.components.ScrollTable;
import com.glookast.fimsclient.app.gui.components.SendSendAllButtonPanel;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tv.fims.base.JobType;

public class JobsPanel extends JPanel
{
    private final AppController myController;
    private final Service.Type myServiceType;

    private final ActionListener myActionListener;

    private final NewEditDeleteButtonPanel myNewEditDeletePanel;
    private final ImportExportButtonPanel myImportExportPanel;
    private final SendSendAllButtonPanel mySendPanel;

    private List<JobType> myJobs;
    private final AvailableJobsTableModel myTableModel;
    private final ScrollTable myTable;

    public JobsPanel(AppController controller, Service.Type serviceType)
    {
        super(new GridBagLayout());

        myController = controller;
        myController.addListener(new FimsClientAppControllerEventListenerImpl());
        myServiceType = serviceType;

        myActionListener = new ActionListenerImpl();

        myJobs = myController.getJobs(myServiceType);

        String[] columnNames = null;
        switch (myServiceType) {
            case Capture:
                columnNames = new String[]{"ResourceID", "SourceID", "StartProcess", "StopProcess", "StartJob", "Priority", "Profiles", "Created", "Modified"};
                break;
            case Transfer:
                columnNames = new String[]{"ResourceID", "StartJob", "Priority", "Profiles", "Created", "Modified"};
                break;
            case Transform:
                columnNames = new String[]{"ResourceID", "StartJob", "Priority", "Profiles", "Created", "Modified"};
                break;
        }

        myTableModel = new AvailableJobsTableModel(columnNames);
        myTable = new ScrollTable(myTableModel);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());
        myTable.getTable().addMouseListener(new MouseAdapterImpl());

        myNewEditDeletePanel = new NewEditDeleteButtonPanel(myActionListener);
        myImportExportPanel = new ImportExportButtonPanel(myActionListener);
        mySendPanel = new SendSendAllButtonPanel(myActionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.weightx = 1;
        c.gridwidth = 8;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);

        switch (myServiceType) {
            case Capture:
                add(new JLabel("Capture Jobs"), c);
                break;
            case Transfer:
                add(new JLabel("Transfer Jobs"), c);
                break;
            case Transform:
                add(new JLabel("Transform Jobs"), c);
                break;
        }

        add(myTable, c);

        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;

        add(myNewEditDeletePanel, c);
        add(myImportExportPanel, c);
        add(mySendPanel, c);

        c.insets = new Insets(0, 0, 0, 0);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(Box.createGlue(), c);

        refreshGUI();
    }

    private void refreshGUI()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                int rowCount = myTable.getRowCount();
                boolean hasRows = rowCount > 0;

                int selectedRowCount = myTable.getSelectedRowCount();
                boolean singleSelection = (selectedRowCount == 1);
                boolean hasSelection = (selectedRowCount > 0);

                Service service = myController.getActiveService(myServiceType);
                boolean isServiceConnected = (service != null && service.isConnected());

                myNewEditDeletePanel.getEditButton().setEnabled(singleSelection);
                myNewEditDeletePanel.getDeleteButton().setEnabled(hasSelection);
                myImportExportPanel.getExportButton().setEnabled(hasSelection);
                mySendPanel.getSendButton().setEnabled(hasSelection && isServiceConnected);
                mySendPanel.getSendAllButton().setEnabled(hasRows && isServiceConnected);
            }
        });
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "New":
                    newJob();
                    break;
                case "Edit":
                    editJob(myTable.getSelectedRow());
                    break;
                case "Delete":
                    deleteJob();
                    break;
                case "Import":
                    importJobs();
                    break;
                case "Export":
                    exportJobs();
                    break;
                case "Send":
                    sendJobs(false);
                    break;
                case "Send All":
                    sendJobs(true);
                    break;
            }
        }
    }

    private void newJob()
    {
        JobDialog dialog = new JobDialog((Window) SwingUtilities.getRoot(this), myController, myServiceType, true);
        dialog.setVisible(true);
        if (dialog.isOK()) {
            myController.setJob(dialog.getObject());
        }
    }

    private void editJob(int rowIndex)
    {
        if (rowIndex >= 0) {
            JobDialog dialog = new JobDialog((Window) SwingUtilities.getRoot(this), myController, myServiceType, true);
            dialog.setObject(myJobs.get(myTable.convertRowIndexToModel(rowIndex)));
            dialog.setVisible(true);
            if (dialog.isOK()) {
                myController.setJob(dialog.getObject());
            }
        }
    }

    private void deleteJob()
    {
        List<JobType> jobs = new ArrayList<>();

        for (int rowIndex : myTable.getSelectedRows()) {
            jobs.add(myJobs.get(myTable.convertRowIndexToModel(rowIndex)));
        }

        for (JobType job : jobs) {
            myController.delJob(job);
        }
    }

    private void importJobs()
    {
        XmlUtils.doImport((Window) SwingUtilities.getRoot(this), myController, myServiceType == Service.Type.Capture, false, myServiceType == Service.Type.Transfer, false, myServiceType == Service.Type.Transform, false);
    }

    private void exportJobs()
    {
        List<JobType> jobs = new ArrayList<>();

        for (int rowIndex : myTable.getSelectedRows()) {
            jobs.add(myJobs.get(myTable.convertRowIndexToModel(rowIndex)));
        }

        XmlUtils.doExport((Window) SwingUtilities.getRoot(this), jobs, null, myServiceType == Service.Type.Capture, false, myServiceType == Service.Type.Transfer, false, myServiceType == Service.Type.Transform, false);
    }

    private void sendJobs(boolean all)
    {
        List<JobType> jobs = new ArrayList<>();

        if (all) {
            jobs.addAll(myJobs);
        } else {
            for (int rowIndex : myTable.getSelectedRows()) {
                jobs.add(myJobs.get(myTable.convertRowIndexToModel(rowIndex)));
            }
        }

        for (final JobType job : jobs) {
            Service service = myController.getActiveService(myServiceType);
            if (service != null) {
                final Future<Boolean> result = service.sendJob(job);

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
                {
                    @Override
                    protected Void doInBackground() throws Exception
                    {
                        if (result.get()) {
                            myController.delJob(job);
                        }
                        return null;
                    }
                };
                worker.execute();
            }
        }
    }

    private class AvailableJobsTableModel extends JobsTableModel
    {
        public AvailableJobsTableModel(String[] columnNames)
        {
            super(columnNames);
        }

        @Override
        public int getTableHeight()
        {
            return 200;
        }

        @Override
        protected List<JobType> getJobs()
        {
            return myJobs;
        }
    }

    private class FimsClientAppControllerEventListenerImpl implements AppController.EventListener
    {
        @Override
        public void onEvent(Event event)
        {
            if ((myServiceType == Service.Type.Capture && event == Event.CaptureJobs) ||
                (myServiceType == Service.Type.Transfer && event == Event.TransferJobs) ||
                (myServiceType == Service.Type.Transform && event == Event.TransformJobs)) {
                myJobs = myController.getJobs(myServiceType);
                myTableModel.fireTableDataChanged();
                refreshGUI();
            } else if (event == Event.ActiveServiceChanged) {
                refreshGUI();
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
                editJob(myTable.rowAtPoint(e.getPoint()));
            }
        }
    }
}
