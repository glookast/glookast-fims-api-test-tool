package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import com.glookast.fimsclient.app.gui.components.SelectionDialog;
import com.glookast.fimsclient.app.gui.components.TablePanel;
import com.glookast.fimsclient.app.gui.components.ViewEditDialog;
import com.glookast.fimsclient.app.utils.StringUtils;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import tv.fims.base.AudioFormatType;
import tv.fims.base.ContainerFormatType;
import tv.fims.base.ProfileType;
import tv.fims.base.TransferAtomType;
import tv.fims.base.TransformAtomType;
import tv.fims.base.VideoFormatType;
import tv.fims.capturemedia.CaptureProfileType;
import tv.fims.transfermedia.TransferProfileType;
import tv.fims.transformmedia.TransformProfileType;

public class ProfileDialog extends ViewEditDialog<ProfileType>
{
    private final Service.Type myServiceType;

    private final ActionListener myActionListener;

    private final JTextField myNameField;
    private final JTextField myDescriptionField;

    private VideoFormatType myVideoFormat;
    private final JButton myVideoFormatButton;
    private AudioFormatType myAudioFormat;
    private final JButton myAudioFormatButton;
    private ContainerFormatType myContainerFormat;
    private final JButton myContainerFormatButton;

    private final DestinationsTablePanel myDestinationsTablePanel;

    private final List<TransferAtomType> myDestinations = new ArrayList<>();

    public ProfileDialog(Window owner, AppController controller, Service.Type serviceType, boolean editable)
    {
        super(owner, controller, editable);

        myServiceType = serviceType;

        myActionListener = new ActionListenerImpl();

        JLabel lblName = new JLabel("Name:");
        myNameField = new JTextField();
        myNameField.setText("New Profile");

        JLabel lblDescription = new JLabel("Description:");
        myDescriptionField = new JTextField();

        JLabel lblVideoFormat = new JLabel("Video Format:");
        myVideoFormatButton = new JButton("None");
        myVideoFormatButton.setActionCommand("VideoFormat");
        myVideoFormatButton.addActionListener(myActionListener);

        JLabel lblAudioFormat = new JLabel("Audio Format:");
        myAudioFormatButton = new JButton("None");
        myAudioFormatButton.setActionCommand("AudioFormat");
        myAudioFormatButton.addActionListener(myActionListener);

        JLabel lblContainerFormat = new JLabel("Container Format:");
        myContainerFormatButton = new JButton("None");
        myContainerFormatButton.setActionCommand("ContainerFormat");
        myContainerFormatButton.addActionListener(myActionListener);

        myDestinationsTablePanel = new DestinationsTablePanel("Destinations:", myDestinations, new DestinationsTableModel(), myController, myEditable);

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.anchor = GridBagConstraints.WEST;
        c1.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.anchor = GridBagConstraints.WEST;
        c2.weightx = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c12 = new GridBagConstraints();
        c12.gridx = 0;
        c12.gridwidth = 3;
        c12.insets = new Insets(5, 5, 5, 5);
        c12.fill = GridBagConstraints.HORIZONTAL;
        c12.weightx = 1;

        myPanel.add(lblName, c1);
        myPanel.add(myNameField, c2);

        myPanel.add(lblDescription, c1);
        myPanel.add(myDescriptionField, c2);

        if (myServiceType == Service.Type.Capture || myServiceType == Service.Type.Transform) {
            myPanel.add(lblVideoFormat, c1);
            myPanel.add(myVideoFormatButton, c2);

            myPanel.add(lblAudioFormat, c1);
            myPanel.add(myAudioFormatButton, c2);

            myPanel.add(lblContainerFormat, c1);
            myPanel.add(myContainerFormatButton, c2);
        }

        myPanel.add(myDestinationsTablePanel, c12);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected String getObjectName()
    {
        return myServiceType + " Profile";
    }

    @Override
    protected final void refreshGUI()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myVideoFormatButton.setText(StringUtils.toString(myVideoFormat));
                myAudioFormatButton.setText(StringUtils.toString(myAudioFormat));
                myContainerFormatButton.setText(StringUtils.toString(myContainerFormat));
            }
        });
    }

    public ProfileType getProfile()
    {
        return myObject;
    }

    public void setProfile(ProfileType captureProfile)
    {
        myObject = captureProfile;
        setTitle("Edit Capture Profile");
        loadObject();
        refreshGUI();
    }

    @Override
    protected void loadObject()
    {
        myDestinations.clear();
        myVideoFormat = null;
        myAudioFormat = null;

        if (myObject != null) {
            myNameField.setText(myObject.getName());
            myDescriptionField.setText(myObject.getDescription());

            if (myObject instanceof CaptureProfileType) {
                CaptureProfileType captureProfile = (CaptureProfileType) myObject;

                TransformAtomType transformAtom = captureProfile.getTransformAtom();
                if (transformAtom != null) {
                    myVideoFormat = transformAtom.getVideoFormat();
                    myAudioFormat = transformAtom.getAudioFormat();
                    myContainerFormat = transformAtom.getContainerFormat();
                }

                myDestinations.addAll(captureProfile.getTransferAtom());
            } else if (myObject instanceof TransferProfileType) {
                TransferProfileType transferProfile = (TransferProfileType) myObject;

                myDestinations.addAll(transferProfile.getTransferAtom());
            } else if (myObject instanceof TransformProfileType) {
                TransformProfileType transformProfile = (TransformProfileType) myObject;

                TransformAtomType transformAtom = transformProfile.getTransformAtom();
                if (transformAtom != null) {
                    myVideoFormat = transformAtom.getVideoFormat();
                    myAudioFormat = transformAtom.getAudioFormat();
                    myContainerFormat = transformAtom.getContainerFormat();
                }

                myDestinations.addAll(transformProfile.getTransferAtom());
            }
        }
    }

    @Override
    protected boolean saveObject()
    {
        if (myObject == null) {
            switch (myServiceType) {
                case Capture:
                    myObject = new CaptureProfileType();
                    break;
                case Transfer:
                    myObject = new TransferProfileType();
                    break;
                case Transform:
                    myObject = new TransformProfileType();
                    break;
            }
            myObject.setResourceID(UUID.randomUUID().toString());
            myObject.setResourceCreationDate(XmlUtils.createCalendar());
        }

        myObject.setResourceModifiedDate(XmlUtils.createCalendar());

        myObject.setName(myNameField.getText());
        myObject.setDescription(myDescriptionField.getText());

        TransformAtomType transformAtom = new TransformAtomType();
        transformAtom.setAudioFormat(myAudioFormat);
        transformAtom.setVideoFormat(myVideoFormat);
        transformAtom.setContainerFormat(myContainerFormat);

        if (myObject instanceof CaptureProfileType) {
            CaptureProfileType captureProfile = (CaptureProfileType) myObject;
            captureProfile.setTransformAtom(transformAtom);
            captureProfile.getTransferAtom().clear();
            captureProfile.getTransferAtom().addAll(myDestinations);
        } else if (myObject instanceof TransferProfileType) {
            TransferProfileType transferProfile = (TransferProfileType) myObject;
            transferProfile.getTransferAtom().clear();
            transferProfile.getTransferAtom().addAll(myDestinations);
        } else if (myObject instanceof TransformProfileType) {
            TransformProfileType transformProfile = (TransformProfileType) myObject;
            transformProfile.setTransformAtom(transformAtom);
            transformProfile.getTransferAtom().clear();
            transformProfile.getTransferAtom().addAll(myDestinations);
        }

        return true;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "VideoFormat":
                    selectVideoFormat();
                    break;
                case "AudioFormat":
                    selectAudioFormat();
                    break;
                case "ContainerFormat":
                    selectContainerFormat();
                    break;
            }
        }
    }

    private void selectVideoFormat()
    {
        VideoFormatsDialog dialog = new VideoFormatsDialog(this, myController, myServiceType);
        dialog.setVisible(true);
        if (dialog.isOK()) {
            myVideoFormat = dialog.getVideoFormat();
            refreshGUI();
        }
    }

    private void selectAudioFormat()
    {
        AudioFormatsDialog dialog = new AudioFormatsDialog(this, myController, myServiceType);
        dialog.setVisible(true);
        if (dialog.isOK()) {
            myAudioFormat = dialog.getAudioFormat();
            refreshGUI();
        }
    }

    private void selectContainerFormat()
    {
        ContainerFormatsDialog dialog = new ContainerFormatsDialog(this, myController, myServiceType);
        dialog.setVisible(true);
        if (dialog.isOK()) {
            myContainerFormat = dialog.getContainerFormat();
            refreshGUI();
        }
    }

    private class DestinationsTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Destination"};
        private final int[] myColumnWidths = {400};

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

    private class DestinationsTablePanel extends TablePanel<TransferAtomType>
    {
        public DestinationsTablePanel(String title, List<TransferAtomType> objects, ScrollTableModel tableModel, AppController controller, boolean editable)
        {
            super(title, objects, tableModel, controller, editable);
        }

        @Override
        protected SelectionDialog<TransferAtomType> getSelectionDialog(Window owner, AppController controller)
        {
            return new DestinationsDialog(owner, controller, myServiceType);
        }

        @Override
        protected ViewEditDialog<TransferAtomType> getViewEditDialog(Window owner, AppController controller, boolean editable)
        {
            return new DestinationDialog(owner, controller, false);
        }

        @Override
        protected String getKey(TransferAtomType object)
        {
            return StringUtils.toString(object);
        }
    }
}
