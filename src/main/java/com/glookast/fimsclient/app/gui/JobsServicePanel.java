package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.JobsTableModel;
import com.glookast.fimsclient.app.gui.components.ListFilterPanel;
import com.glookast.fimsclient.app.gui.components.ManageJobButtonPanel;
import com.glookast.fimsclient.app.gui.components.ScrollTable;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tv.fims.base.JobCommandType;
import tv.fims.base.JobStatusType;
import tv.fims.base.JobType;
import tv.fims.base.PriorityType;
import tv.fims.capturemedia.CaptureFaultType;
import tv.fims.capturemedia.CaptureJobType;

public class JobsServicePanel extends JPanel
{
    private final AppController myController;
    private final Service myService;
    private final Service.Type myServiceType;

    private final ActionListener myActionListener;

    private final ListFilterPanel myListFilterPanel;

    private final ServiceJobsTableModel myServiceJobsTableModel;
    private final ScrollTable myServiceJobsTable;

    private final ServiceJobsUpdateTableModel myServiceJobsUpdateTableModel;
    private final ScrollTable myServiceJobsUpdateTable;

    private final ManageJobButtonPanel myManageJobButtonPanel;

    private final List<JobType> myJobs;
    private final Map<String, List<JobType>> myJobUpdates;
    private final ReentrantReadWriteLock myJobUpdatesLock;
    private volatile String mySelectedJobId;

    public JobsServicePanel(AppController controller, Service service)
    {
        super(new GridBagLayout());

        myController = controller;

        myService = service;
        myService.addListener(new FimsClientServiceControllerEventListenerImpl());
        myServiceType = myService.getType();

        myActionListener = new ActionListenerImpl();

        myListFilterPanel = new ListFilterPanel(myActionListener);

        myJobs = new ArrayList<>();
        myJobUpdates = new HashMap<>();
        myJobUpdatesLock = new ReentrantReadWriteLock();

        String[] columnNames = null;
        switch (myServiceType) {
            case Capture:
                columnNames = new String[]{"ResourceID", "SourceID", "StartProcess", "StopProcess", "StartJob", "Priority", "Status", "Queue Position", "Profiles", "Created", "Modified", "Started", "Completed"};
                break;
            case Transfer:
                columnNames = new String[]{"ResourceID", "SourceID", "Priority", "Status", "Queue Position", "Profiles", "Created", "Modified", "Started", "Completed"};
                break;
            case Transform:
                columnNames = new String[]{"ResourceID", "SourceID", "Priority", "Status", "Queue Position", "Profiles", "Created", "Modified", "Started", "Completed"};
                break;
            default:
                break;
        }

        myServiceJobsTableModel = new ServiceJobsTableModel(columnNames);
        myServiceJobsTable = new ScrollTable(myServiceJobsTableModel);
        myServiceJobsTable.addListSelectionListener(new ListSelectionListenerImpl());
        myServiceJobsTable.getTable().addMouseListener(new MouseAdapterImpl());

        myServiceJobsUpdateTableModel = new ServiceJobsUpdateTableModel(columnNames);
        myServiceJobsUpdateTable = new ScrollTable(myServiceJobsUpdateTableModel);

        myManageJobButtonPanel = new ManageJobButtonPanel(myActionListener);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.weightx = 1;
        c.insets.left = 5;
        c.insets.right = 5;

        add(myListFilterPanel, c);
        add(myServiceJobsTable, c);
        add(myManageJobButtonPanel, c);
        add(new JLabel("Job Updates:"), c);
        add(myServiceJobsUpdateTable, c);

        refreshGUI();
    }

    private void refreshGUI()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                int selectedRowCount = myServiceJobsTable.getSelectedRowCount();
                boolean singleSelection = (selectedRowCount == 1);
                boolean hasSelection = (selectedRowCount > 0);

                boolean canChangePriority = false;
                boolean canCancel = false;
                boolean canStop = false;
                boolean canCleanUp = false;

                synchronized (myJobs) {
                    for (int rowId : myServiceJobsTable.getSelectedRows()) {
                        JobStatusType jobStatus = myJobs.get(myServiceJobsTable.convertRowIndexToModel(rowId)).getStatus();
                        canChangePriority |= canChangePriority(jobStatus);
                        canCancel |= canCancel(jobStatus);
                        canStop |= canStop(jobStatus);
                        canCleanUp |= canCleanUp(jobStatus);
                    }

                    myManageJobButtonPanel.getViewButton().setEnabled(singleSelection);
                    myManageJobButtonPanel.getPriorityButton().setEnabled(hasSelection && canChangePriority);
                    myManageJobButtonPanel.getCancelButton().setEnabled(hasSelection && canCancel);
                    myManageJobButtonPanel.getStopButton().setEnabled(hasSelection && canStop);
                    myManageJobButtonPanel.getCleanupButton().setEnabled(hasSelection && canCleanUp);

                    boolean isConnected = myService.isConnected();
                    for (Component component : myListFilterPanel.getComponents()) {
                        if (!(component instanceof JButton)) {
                            component.setEnabled(isConnected);
                        }
                    }

                    String selectedJobId = (singleSelection) ? myJobs.get(myServiceJobsTable.convertRowIndexToModel(myServiceJobsTable.getSelectedRow())).getResourceID() : null;
                    if (!Objects.equals(mySelectedJobId, selectedJobId)) {
                        mySelectedJobId = selectedJobId;
                        myServiceJobsUpdateTableModel.fireTableDataChanged();
                    }
                }
            }
        });
    }

    private void reload()
    {
        synchronized (myJobs) {
            List<String> selectedResourceIds = new ArrayList<>();
            for (int rowId : myServiceJobsTable.getSelectedRows()) {
                selectedResourceIds.add(myJobs.get(myServiceJobsTable.convertRowIndexToModel(rowId)).getResourceID());
            }

            myJobs.clear();
            myJobs.addAll(myService.getJobs());
            myServiceJobsTableModel.fireTableDataChanged();

            for (String resourceId : selectedResourceIds) {
                for (int i = 0; i < myJobs.size(); i++) {
                    if (resourceId.equals(myJobs.get(i).getResourceID())) {
                        myServiceJobsTable.addSelectionInterval(i, i);
                        break;
                    }
                }
            }
        }
        refreshGUI();
    }

    private class ListSelectionListenerImpl implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting()) {
                refreshGUI();
            }
        }
    }

    private class MouseAdapterImpl extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getClickCount() == 2) {
                viewJob(myServiceJobsTable.rowAtPoint(e.getPoint()));
            }
        }
    }

    private class ServiceJobsTableModel extends JobsTableModel
    {
        public ServiceJobsTableModel(String[] columnNames)
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
            synchronized(myJobs) {
                return new ArrayList<>(myJobs);
            }
        }
    }

    private class ServiceJobsUpdateTableModel extends JobsTableModel
    {
        public ServiceJobsUpdateTableModel(String[] columnNames)
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
            List<JobType> jobs;
            myJobUpdatesLock.readLock().lock();
            try {
                jobs = myJobUpdates.get(mySelectedJobId);
            } finally {
                myJobUpdatesLock.readLock().unlock();
            }

            if (jobs == null) {
                jobs = new ArrayList<>();
            }
            return jobs;
        }
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "Query":
                    myService.queryJobs(myListFilterPanel.getFilter());
                    break;
                case "View":
                    viewJob(myServiceJobsTable.getSelectedRow());
                    break;
                case "Priority":
                    modifyPriorityJobs();
                    break;
                case "Cancel":
                    cancelJobs();
                    break;
                case "Stop":
                    stopJobs();
                    break;
                case "Clean":
                    cleanJobs();
                    break;
            }
        }
    }

    private void viewJob(int rowIndex)
    {
        if (rowIndex >= 0) {
            JobDialog dialog = new JobDialog((Window) SwingUtilities.getRoot(this), myController, myServiceType, false);
            synchronized (myJobs) {
                dialog.setObject(myJobs.get(myServiceJobsTable.convertRowIndexToModel(rowIndex)));
            }
            dialog.setVisible(true);
        }
    }

    private void modifyPriorityJobs()
    {
        List<JobType> jobs = new ArrayList<>();

        PriorityType priority = null;

        synchronized (myJobs) {
            for (int rowIndex : myServiceJobsTable.getSelectedRows()) {
                JobType job = myJobs.get(myServiceJobsTable.convertRowIndexToModel(rowIndex));
                if (canChangePriority(job.getStatus())) {
                    if (priority == null) {
                        priority = job.getPriority();
                    }
                    jobs.add(job);
                }
            }
        }

        if (priority != null) {
            priority = (PriorityType) JOptionPane.showInputDialog(this, "Select Priority", "Modify Priority", JOptionPane.INFORMATION_MESSAGE, null, PriorityType.values(), priority);
            if (priority != null) {
                for (JobType job : jobs) {
                    myService.manageJobPriority(job, priority);
                }
                reload();
            }
        }
    }

    private void cancelJobs()
    {
        synchronized (myJobs) {
            for (int rowIndex : myServiceJobsTable.getSelectedRows()) {
                JobType job = myJobs.get(myServiceJobsTable.convertRowIndexToModel(rowIndex));
                if (canCancel(job.getStatus())) {
                    myService.manageJob(job, JobCommandType.CANCEL);
                }
            }
        }
        reload();
    }

    private void stopJobs()
    {
        synchronized (myJobs) {
            for (int rowIndex : myServiceJobsTable.getSelectedRows()) {
                JobType job = myJobs.get(myServiceJobsTable.convertRowIndexToModel(rowIndex));
                if (canStop(job.getStatus())) {
                    myService.manageJob(job, JobCommandType.STOP);
                }
            }
        }
        reload();
    }

    private void cleanJobs()
    {
        synchronized (myJobs) {
            for (int rowIndex : myServiceJobsTable.getSelectedRows()) {
                JobType job = myJobs.get(myServiceJobsTable.convertRowIndexToModel(rowIndex));
                if (canCleanUp(job.getStatus())) {
                    myService.manageJob(job, JobCommandType.CLEANUP);
                }
            }
        }
        reload();
    }

    private boolean canChangePriority(JobStatusType jobStatus)
    {
        return jobStatus == JobStatusType.NEW ||
               jobStatus == JobStatusType.QUEUED;
    }

    private boolean canCancel(JobStatusType jobStatus)
    {
        return jobStatus == JobStatusType.NEW ||
               jobStatus == JobStatusType.QUEUED ||
               jobStatus == JobStatusType.SCHEDULED ||
               jobStatus == JobStatusType.RUNNING ||
               jobStatus == JobStatusType.PAUSED ||
               jobStatus == JobStatusType.UNKNOWN;
    }

    private boolean canStop(JobStatusType jobStatus)
    {
        return jobStatus == JobStatusType.RUNNING ||
               jobStatus == JobStatusType.PAUSED;
    }

    private boolean canCleanUp(JobStatusType jobStatus)
    {
        return jobStatus == JobStatusType.CANCELED ||
               jobStatus == JobStatusType.COMPLETED ||
               jobStatus == JobStatusType.FAILED ||
               jobStatus == JobStatusType.STOPPED;
    }

    private class FimsClientServiceControllerEventListenerImpl implements Service.EventListener
    {
        @Override
        public void onEvent(Event event)
        {
            if (event == Event.Jobs) {
                reload();
            } else if (event == Event.ConnectionStatus) {
                refreshGUI();
            }
        }

        private void addJob(JobType job)
        {
            String jobId = job.getResourceID();
            myJobUpdatesLock.writeLock().lock();

            List<JobType> jobs;
            try {
                jobs = myJobUpdates.get(jobId);
                if (jobs == null) {
                    jobs = new ArrayList<>();
                    myJobUpdates.put(jobId, jobs);
                }
                jobs.add(job);
            } finally {
                myJobUpdatesLock.writeLock().unlock();
            }
            if (Objects.equals(jobId, mySelectedJobId)) {
                myServiceJobsUpdateTableModel.fireTableRowsInserted(jobs.size() - 1, jobs.size() - 1);
            }
        }

        @Override
        public void onResult(CaptureJobType job)
        {
            addJob(job);
        }

        @Override
        public void onFault(CaptureJobType job, CaptureFaultType fault)
        {
            addJob(job);
        }
    }
}
