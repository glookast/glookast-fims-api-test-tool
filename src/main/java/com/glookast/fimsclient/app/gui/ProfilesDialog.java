package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.ImportExportButtonPanel;
import com.glookast.fimsclient.app.gui.components.NewEditDeleteButtonPanel;
import com.glookast.fimsclient.app.gui.components.OKCancelButtonPanel;
import com.glookast.fimsclient.app.gui.components.ScrollTable;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import com.glookast.fimsclient.app.gui.components.SelectionDialog;
import com.glookast.fimsclient.app.utils.StringUtils;
import com.glookast.fimsclient.app.utils.XmlUtils;
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
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tv.fims.base.ProfileType;
import tv.fims.base.TransferAtomType;
import tv.fims.base.TransformAtomType;
import tv.fims.capturemedia.CaptureProfileType;
import tv.fims.transfermedia.TransferProfileType;
import tv.fims.transformmedia.TransformProfileType;

public class ProfilesDialog extends SelectionDialog<ProfileType>
{
    private final AppController myController;
    private final Service.Type myServiceType;

    private List<ProfileType> myProfiles;
    private final ProfileTableModel myTableModel;
    private final ScrollTable myTable;

    private final ActionListener myActionListener;

    private final NewEditDeleteButtonPanel myNewEditDeletePanel;
    private final ImportExportButtonPanel myImportExportPanel;
    private final OKCancelButtonPanel myOKCancelPanel;

    private final List<ProfileType> mySelectedProfiles;
    private boolean myOK;

    public ProfilesDialog(Window owner, AppController controller, Service.Type serviceType)
    {
        super(owner, ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        myController = controller;
        myController.addListener(new FimsClientAppControllerEventListenerImpl());

        myServiceType = serviceType;

        myActionListener = new ActionListenerImpl();

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myProfiles = myController.getProfiles(myServiceType);

        String[] columnNames = null;
        int[] columnWidths = null;
        switch (myServiceType) {
            case Capture:
            case Transform:
                columnNames = new String[]{"Name", "Description", "Video Format", "Audio Format", "Container", "Destinations"};
                columnWidths = new int[]{200, 200, 200, 200, 100, 200};
                break;
            case Transfer:
                columnNames = new String[]{"Name", "Description", "Destinations"};
                columnWidths = new int[]{200, 200, 200};
                break;
        }

        myTableModel = new ProfileTableModel(columnNames, columnWidths);
        myTable = new ScrollTable(myTableModel);
        myTable.addListSelectionListener(new ListSelectionListenerImpl());
        myTable.getTable().addMouseListener(new MouseAdapterImpl());

        mySelectedProfiles = new ArrayList<>();

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
        c.fill = GridBagConstraints.HORIZONTAL;
        add(myOKCancelPanel, c);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    private void setTitle()
    {
        setTitle(myServiceType + " Profiles");
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
    public List<ProfileType> getSelectedObjects()
    {
        return mySelectedProfiles;
    }

    private class ListSelectionListenerImpl implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            refreshGUI();
        }
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "New":
                    newProfile();
                    break;
                case "Edit":
                    editProfile(myTable.getSelectedRow());
                    break;
                case "Delete":
                    deleteProfiles();
                    break;
                case "Import":
                    importProfiles();
                    break;
                case "Export":
                    exportProfiles();
                    break;
                case "OK":
                    setSelectedProfiles();
                    myOK = true;
                    dispose();
                    break;
                case "Cancel":
                    dispose();
                    break;

            }
        }
    }

    private void newProfile()
    {
        ProfileDialog dialog = new ProfileDialog(this, myController, myServiceType, true);
        dialog.setVisible(true);
        if (dialog.isOK()) {
            ProfileType profile = dialog.getProfile();
            myController.setProfile(profile);
            int index = myProfiles.indexOf(profile);
            if (index >= 0) {
                myTable.addRowSelectionInterval(index, index);
            }
        }
    }

    private void editProfile(int rowIndex)
    {
        if (rowIndex >= 0) {
            ProfileDialog dialog = new ProfileDialog(this, myController, myServiceType, true);
            dialog.setProfile(myProfiles.get(myTable.convertRowIndexToModel(rowIndex)));
            dialog.setVisible(true);
            if (dialog.isOK()) {
                myController.setProfile(dialog.getProfile());
            }
        }
    }

    private void deleteProfiles()
    {
        List<ProfileType> profiles = new ArrayList<>();

        for (int rowIndex : myTable.getSelectedRows()) {
            profiles.add(myProfiles.get(myTable.convertRowIndexToModel(rowIndex)));
        }

        for (ProfileType profile : profiles) {
            myController.delProfile(profile);
        }
    }

    private void importProfiles()
    {
        XmlUtils.doImport(this, myController, false, myServiceType == Service.Type.Capture, false, myServiceType == Service.Type.Transfer, false, myServiceType == Service.Type.Transform);
    }

    private void exportProfiles()
    {
        List<ProfileType> profiles = new ArrayList<>();

        for (int rowIndex : myTable.getSelectedRows()) {
            profiles.add(myProfiles.get(myTable.convertRowIndexToModel(rowIndex)));
        }

        XmlUtils.doExport(this, null, profiles, false, myServiceType == Service.Type.Capture, false, myServiceType == Service.Type.Transfer, false, myServiceType == Service.Type.Transform);
    }

    private void setSelectedProfiles()
    {
        mySelectedProfiles.clear();
        for (int rowIndex : myTable.getSelectedRows()) {
            mySelectedProfiles.add(myProfiles.get(myTable.convertRowIndexToModel(rowIndex)));
        }
    }

    private class ProfileTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames;
        private final int[] myColumnWidths;

        public ProfileTableModel(String[] columnNames, int[] columnWidths)
        {
            myColumnNames = columnNames;
            myColumnWidths = columnWidths;
        }

        @Override
        public int getTableHeight()
        {
            return 200;
        }

        @Override
        public int getRowCount()
        {
            return myProfiles.size();
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

        private String getVideoFormat(TransformAtomType transformAtom)
        {
            return StringUtils.toString((transformAtom != null) ? transformAtom.getVideoFormat() : null);
        }

        private String getAudioFormat(TransformAtomType transformAtom)
        {
            return StringUtils.toString((transformAtom != null) ? transformAtom.getAudioFormat() : null);
        }

        private String getContainerFormat(TransformAtomType transformAtom)
        {
            return (transformAtom != null && transformAtom.getContainerFormat() != null && transformAtom.getContainerFormat().getContainerFormat() != null && transformAtom.getContainerFormat().getContainerFormat().getValue() != null) ? transformAtom.getContainerFormat().getContainerFormat().getValue() : "Unknown";
        }

        private String getDestinations(List<TransferAtomType> transferAtoms)
        {
            StringBuilder sb = new StringBuilder();
            for (TransferAtomType transferAtom : transferAtoms) {
                sb.append(transferAtom.getDestination());
                sb.append(", ");
            }
            if (sb.length() >= 2) {
                sb.setLength(sb.length() - 2);
            }
            return sb.toString();
        }

        private Object getValueAt(String columnName, CaptureProfileType captureProfile)
        {
            switch (columnName) {
                case "Video Format":
                    return getVideoFormat(captureProfile.getTransformAtom());
                case "Audio Format":
                    return getAudioFormat(captureProfile.getTransformAtom());
                case "Container":
                    return getContainerFormat(captureProfile.getTransformAtom());
                case "Destinations":
                    return getDestinations(captureProfile.getTransferAtom());
            }
            throw new IndexOutOfBoundsException();
        }

        private Object getValueAt(String columnName, TransferProfileType transferProfile)
        {
            switch (columnName) {
                case "Destinations":
                    return getDestinations(transferProfile.getTransferAtom());
            }
            throw new IndexOutOfBoundsException();
        }

        private Object getValueAt(String columnName, TransformProfileType transformProfile)
        {
            switch (columnName) {
                case "Video Format":
                    return getVideoFormat(transformProfile.getTransformAtom());
                case "Audio Format":
                    return getAudioFormat(transformProfile.getTransformAtom());
                case "Container":
                    return getContainerFormat(transformProfile.getTransformAtom());
                case "Destinations":
                    return getDestinations(transformProfile.getTransferAtom());
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            String columnName = getColumnName(columnIndex);
            ProfileType profile = myProfiles.get(rowIndex);

            switch (columnName) {
                case "Name":
                    return profile.getName();
                case "Description":
                    return profile.getDescription();
            }

            if (profile instanceof CaptureProfileType) {
                return getValueAt(columnName, (CaptureProfileType) profile);
            } else if (profile instanceof TransferProfileType) {
                return getValueAt(columnName, (TransferProfileType) profile);
            } else if (profile instanceof TransformProfileType) {
                return getValueAt(columnName, (TransformProfileType) profile);
            }

            throw new IndexOutOfBoundsException();
        }
    }

    private class FimsClientAppControllerEventListenerImpl implements AppController.EventListener
    {
        @Override
        public void onEvent(Event event)
        {
            if ((myServiceType == Service.Type.Capture && event == Event.CaptureProfiles) ||
                (myServiceType == Service.Type.Transfer && event == Event.TransferProfiles) ||
                (myServiceType == Service.Type.Transform && event == Event.TransformProfiles)) {
                myProfiles = myController.getProfiles(myServiceType);
                myTableModel.fireTableDataChanged();
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
                editProfile(myTable.rowAtPoint(e.getPoint()));
            }
        }
    }
}
