package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.components.ScrollTableModel;
import com.glookast.fimsclient.app.gui.components.SelectionDialog;
import com.glookast.fimsclient.app.gui.components.TablePanel;
import com.glookast.fimsclient.app.gui.components.ViewEditDialog;
import com.glookast.fimsclient.app.utils.StringUtils;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import tv.fims.base.BMObjectType;
import tv.fims.base.BMObjectsType;
import tv.fims.base.ExtensionAttributes;
import tv.fims.base.JobType;
import tv.fims.base.PriorityType;
import tv.fims.base.ProfileType;
import tv.fims.base.StartJobByLatestType;
import tv.fims.base.StartJobByNoWaitType;
import tv.fims.base.StartJobByTimeType;
import tv.fims.base.StartJobType;
import tv.fims.base.StartProcessByNoWaitType;
import tv.fims.base.StartProcessByServiceDefinedTimeType;
import tv.fims.base.StartProcessByTimeMarkType;
import tv.fims.base.StartProcessByTimeType;
import tv.fims.base.StartProcessType;
import tv.fims.base.StopProcessByDurationType;
import tv.fims.base.StopProcessByOpenEndType;
import tv.fims.base.StopProcessByServiceDefinedTimeType;
import tv.fims.base.StopProcessByTimeMarkType;
import tv.fims.base.StopProcessByTimeType;
import tv.fims.base.StopProcessType;
import tv.fims.basetime.DurationType;
import tv.fims.basetime.TimeType;
import tv.fims.capturemedia.CaptureJobType;
import tv.fims.capturemedia.CaptureProfileType;
import tv.fims.transfermedia.TransferJobType;
import tv.fims.transfermedia.TransferProfileType;
import tv.fims.transformmedia.TransformJobType;
import tv.fims.transformmedia.TransformProfileType;

public class JobDialog extends ViewEditDialog<JobType>
{
    private final Service.Type myServiceType;

    private final JComboBox<String> myStartProcessCombo;
    private final JComboBox<String> myStopProcessCombo;
    private final JComboBox<String> myStartJobCombo;
    private final JComboBox<String> myPriorityCombo;

    private final JSpinner myStartProcessSpinner;
    private final JSpinner myStopProcessSpinner;
    private final JSpinner myStartJobSpinner;

    private final JTextField myStartProcessTimecode;
    private final JTextField myStopProcessTimecode;

    private final ActionListener myActionListener;

    private final TablePanel<ProfileType> myProfilesTablePanel;

    private final BMObjectTablePanel myBmObjectsTablePanel;

    private final JTextField myClipNameField;
    private final JTextField myTapeNameField;
    private final JTextField myInterplaySubfolderField;
    private final JComboBox<String> myTimecodeSourceCombo;

    private final List<ProfileType> myProfiles = new ArrayList<>();
    private final List<BMObjectType> myBmObjects = new ArrayList<>();

    public JobDialog(Window owner, AppController controller, Service.Type serviceType, boolean editable)
    {
        super(owner, controller, editable);

        myServiceType = serviceType;

        myActionListener = new ActionListenerImpl();

        JLabel lblClipName = new JLabel("Clip Name:");
        myClipNameField = new JTextField();

        JLabel lblTapeName = new JLabel("Tape Name:");
        myTapeNameField = new JTextField();

        JLabel lblInterplaySubfolder = new JLabel("Interplay Subfolder:");
        myInterplaySubfolderField = new JTextField();

        JLabel lblTimecodeSource = new JLabel("Timecode Source:");
        myTimecodeSourceCombo = new JComboBox<>();
        myTimecodeSourceCombo.addItem("");
        myTimecodeSourceCombo.addItem("vitc");
        myTimecodeSourceCombo.addItem("ltc");
        myTimecodeSourceCombo.addItem("tod");
        myTimecodeSourceCombo.addItem("9pin");

        JLabel lblStartProcess = new JLabel("StartProcess:");
        myStartProcessCombo = new JComboBox<>(new String[]{"No Wait", "Time", "Time Mark", "Service Defined Time"});
        myStartProcessCombo.setActionCommand("Combo");
        myStartProcessCombo.addActionListener(myActionListener);
        myStartProcessSpinner = new JSpinner(new SpinnerDateModel());
        myStartProcessSpinner.setEditor(new JSpinner.DateEditor(myStartProcessSpinner, "yyyy-MM-dd HH:mm:ss"));
        myStartProcessSpinner.setValue(new Date((System.currentTimeMillis() + 1800000) / 900000 * 900000));
        myStartProcessTimecode = new JTextField("00:00:00:00");
        myStartProcessTimecode.setFont(myStartProcessTimecode.getFont().deriveFont(Font.BOLD));
        myStartProcessTimecode.setPreferredSize(new Dimension(70, 20));
        myStartProcessTimecode.setInputVerifier(new TimecodeInputVerifier());
        myStartProcessTimecode.addFocusListener(new TimecodeFocusListener());

        JLabel lblStopProcess = new JLabel("StopProcess:");
        myStopProcessCombo = new JComboBox<>(new String[]{"Open End", "Time", "Time Mark", "Duration", "Service Defined Time"});
        myStopProcessCombo.setActionCommand("Combo");
        myStopProcessCombo.addActionListener(myActionListener);
        myStopProcessSpinner = new JSpinner(new SpinnerDateModel());
        myStopProcessSpinner.setEditor(new JSpinner.DateEditor(myStopProcessSpinner, "yyyy-MM-dd HH:mm:ss"));
        myStopProcessSpinner.setValue(new Date((System.currentTimeMillis() + 5400000) / 900000 * 900000));
        myStopProcessTimecode = new JTextField("00:00:00:00");
        myStopProcessTimecode.setFont(myStopProcessTimecode.getFont().deriveFont(Font.BOLD));
        myStopProcessTimecode.setPreferredSize(new Dimension(70, 20));
        myStopProcessTimecode.setInputVerifier(new TimecodeInputVerifier());
        myStopProcessTimecode.addFocusListener(new TimecodeFocusListener());

        JLabel lblStartJob = new JLabel("StartJob:");
        myStartJobCombo = new JComboBox<>(new String[]{"No Wait", "Time", "Latest"});
        myStartJobCombo.setActionCommand("Combo");
        myStartJobCombo.addActionListener(myActionListener);
        myStartJobSpinner = new JSpinner(new SpinnerDateModel());
        myStartJobSpinner.setEditor(new JSpinner.DateEditor(myStartJobSpinner, "yyyy-MM-dd HH:mm:ss"));
        myStartJobSpinner.setValue(new Date((System.currentTimeMillis() + 900000) / 900000 * 900000));

        JLabel lblPriority = new JLabel("Priority:");
        myPriorityCombo = new JComboBox<>(new String[]{"Low", "Medium", "High", "Urgent", "Immediate"});

        myProfilesTablePanel = new ProfilesTablePanel("Profiles:", myProfiles, new CaptureProfilesTableModel(), myController, myEditable);

        myBmObjectsTablePanel = new BMObjectTablePanel("Business Media Objects:", myBmObjects, new BMObjectsTableModel(), myController, myEditable);

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = 0;
        c1.anchor = GridBagConstraints.WEST;
        c1.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.gridy = 0;
        c2.anchor = GridBagConstraints.WEST;
        c2.weightx = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 2;
        c3.gridy = 0;

        GridBagConstraints c123 = new GridBagConstraints();
        c123.gridx = 0;
        c123.gridwidth = 3;
        c123.insets = new Insets(5, 5, 5, 5);
        c123.fill = GridBagConstraints.HORIZONTAL;
        c123.weightx = 1;

        myPanel.add(lblClipName, c1);
        myPanel.add(myClipNameField, c2);

        c1.gridy++;
        c2.gridy++;

        myPanel.add(lblTapeName, c1);
        myPanel.add(myTapeNameField, c2);

        c1.gridy++;
        c2.gridy++;

        myPanel.add(lblInterplaySubfolder, c1);
        myPanel.add(myInterplaySubfolderField, c2);

        c1.gridy++;
        c2.gridy++;

        myPanel.add(lblTimecodeSource, c1);
        myPanel.add(myTimecodeSourceCombo, c2);

        c1.gridy++;
        c2.gridy++;

        if (myServiceType == Service.Type.Capture) {
            myPanel.add(lblStartProcess, c1);
            myPanel.add(myStartProcessCombo, c2);

            c1.gridy++;
            c2.gridy++;

            myPanel.add(lblStopProcess, c1);
            myPanel.add(myStopProcessCombo, c2);

            c1.gridy++;
            c2.gridy++;
        }

        myPanel.add(lblStartJob, c1);
        myPanel.add(myStartJobCombo, c2);

        c1.gridy++;
        c2.gridy++;

        myPanel.add(lblPriority, c1);
        myPanel.add(myPriorityCombo, c2);

        myPanel.add(myProfilesTablePanel, c123);

        myPanel.add(myBmObjectsTablePanel, c123);

        setTitle();
        pack();
        setLocationRelativeTo(owner);
        refreshGUI();
    }

    @Override
    protected String getObjectName()
    {
        return myServiceType + " Job";
    }

    @Override
    protected final void refreshGUI()
    {
        refreshGUI(null);
    }

    private GridBagLayout getGridBagLayout()
    {
        return (GridBagLayout) myPanel.getLayout();
    }

    private void refreshGUI(final Object source)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myPanel.remove(myStartProcessSpinner);
                myPanel.remove(myStartProcessTimecode);
                myPanel.remove(myStopProcessSpinner);
                myPanel.remove(myStopProcessTimecode);
                myPanel.remove(myStartJobSpinner);

                if (myServiceType == Service.Type.Capture) {
                    GridBagConstraints c = getGridBagLayout().getConstraints(myStartProcessCombo);
                    c.gridx = 2;
                    switch ((String) myStartProcessCombo.getSelectedItem()) {
                        case "Time":
                            myPanel.add(myStartProcessSpinner, c);
                            if (source == myStartProcessCombo) {
                                ((JSpinner.DateEditor) myStartProcessSpinner.getEditor()).getTextField().requestFocus();
                            }
                            break;
                        case "Time Mark":
                            myPanel.add(myStartProcessTimecode, c);
                            if (source == myStartProcessCombo) {
                                myStartProcessTimecode.requestFocus();
                            }
                            break;
                    }

                    c = getGridBagLayout().getConstraints(myStopProcessCombo);
                    c.gridx = 2;
                    switch ((String) myStopProcessCombo.getSelectedItem()) {
                        case "Time":
                            myPanel.add(myStopProcessSpinner, c);
                            if (source == myStopProcessCombo) {
                                ((JSpinner.DateEditor) myStopProcessSpinner.getEditor()).getTextField().requestFocus();
                            }
                            break;
                        case "Time Mark":
                        case "Duration":
                            myPanel.add(myStopProcessTimecode, c);
                            if (source == myStopProcessCombo) {
                                myStopProcessTimecode.requestFocus();
                            }
                            break;
                    }

                    if (myObject == null || myObject.getBmObjects() == null || myObject.getBmObjects().getBmObject().isEmpty()) {
                        myPanel.remove(myBmObjectsTablePanel);
                    }
                }

                if (myStartJobCombo.getSelectedItem().equals("Time")) {
                    GridBagConstraints c = getGridBagLayout().getConstraints(myStartJobCombo);
                    c.gridx = 2;
                    myPanel.add(myStartJobSpinner, c);
                    if (source == myStartJobCombo) {
                        ((JSpinner.DateEditor) myStartJobSpinner.getEditor()).getTextField().requestFocus();
                    }
                }

                pack();
                revalidate();
                repaint();
            }
        });
    }

    @Override
    protected void loadObject()
    {
        myProfiles.clear();

        if (myObject != null) {

            ExtensionAttributes extensionAttributes = myObject.getExtensionAttributes();
            if (extensionAttributes != null) {
                String clipName = extensionAttributes.getOtherAttributes().get(new QName("http://fims.api.glookast.com", "ClipName"));
                if (clipName != null) {
                    myClipNameField.setText(clipName);
                }
                String tapeName = extensionAttributes.getOtherAttributes().get(new QName("http://fims.api.glookast.com", "TapeName"));
                if (tapeName != null) {
                    myTapeNameField.setText(tapeName);
                }
                String interplaySubfolder = extensionAttributes.getOtherAttributes().get(new QName("http://fims.api.glookast.com", "InterplaySubfolder"));
                if (interplaySubfolder != null) {
                    myInterplaySubfolderField.setText(interplaySubfolder);
                }
                String timecodeSource = extensionAttributes.getOtherAttributes().get(new QName("http://fims.api.glookast.com", "TimecodeSource"));
                if (timecodeSource != null) {
                    myTimecodeSourceCombo.setSelectedItem(timecodeSource);
                }
            }

            if (myObject instanceof CaptureJobType) {
                CaptureJobType captureJob = (CaptureJobType) myObject;

                StartProcessType startProcess = captureJob.getStartProcess();
                if (startProcess instanceof StartProcessByNoWaitType) {
                    myStartProcessCombo.setSelectedItem("No Wait");
                } else if (startProcess instanceof StartProcessByTimeType) {
                    myStartProcessCombo.setSelectedItem("Time");
                    myStartProcessSpinner.setValue(((StartProcessByTimeType) startProcess).getTime().getTime());
                } else if (startProcess instanceof StartProcessByTimeMarkType) {
                    myStartProcessCombo.setSelectedItem("Time Mark");
                    myStartProcessTimecode.setText(((StartProcessByTimeMarkType) startProcess).getTimeMark().getTimecode());
                } else if (startProcess instanceof StartProcessByServiceDefinedTimeType) {
                    myStartProcessCombo.setSelectedItem("Service Defined Time");
                }

                StopProcessType stopProcess = captureJob.getStopProcess();
                if (stopProcess instanceof StopProcessByOpenEndType) {
                    myStopProcessCombo.setSelectedItem("Open End");
                } else if (stopProcess instanceof StopProcessByTimeType) {
                    myStopProcessCombo.setSelectedItem("Time");
                    myStopProcessSpinner.setValue(((StopProcessByTimeType) stopProcess).getTime().getTime());
                } else if (stopProcess instanceof StopProcessByTimeMarkType) {
                    myStopProcessCombo.setSelectedItem("Time Mark");
                    myStopProcessTimecode.setText(((StopProcessByTimeMarkType) stopProcess).getTimeMark().getTimecode());
                } else if (stopProcess instanceof StopProcessByDurationType) {
                    myStopProcessCombo.setSelectedItem("Duration");
                    myStopProcessTimecode.setText(((StopProcessByDurationType) stopProcess).getDuration().getTimecode());
                } else if (stopProcess instanceof StopProcessByServiceDefinedTimeType) {
                    myStopProcessCombo.setSelectedItem("Service Defined Time");
                }

                CaptureJobType.Profiles profiles = captureJob.getProfiles();
                if (profiles != null) {
                    myProfiles.addAll(profiles.getCaptureProfile());
                }
            } else if (myObject instanceof TransferJobType) {
                TransferJobType transferJob = (TransferJobType) myObject;
                TransferJobType.Profiles profiles = transferJob.getProfiles();
                if (profiles != null) {
                    myProfiles.addAll(profiles.getTransferProfile());
                }
            } else if (myObject instanceof TransformJobType) {
                TransformJobType transformJob = (TransformJobType) myObject;
                TransformJobType.Profiles profiles = transformJob.getProfiles();
                if (profiles != null) {
                    myProfiles.addAll(profiles.getTransformProfile());
                }
            }

            StartJobType startJob = myObject.getStartJob();
            if (startJob instanceof StartJobByNoWaitType) {
                myStartJobCombo.setSelectedItem("No Wait");
            } else if (startJob instanceof StartJobByTimeType) {
                myStartJobCombo.setSelectedItem("Time");
                myStartJobSpinner.setValue(((StartJobByTimeType) startJob).getTime().getTime());
            } else if (startJob instanceof StartJobByLatestType) {
                myStartJobCombo.setSelectedItem("Latest");
            }

            if (myObject.getPriority() != null)
            {
                switch (myObject.getPriority())
                {
                    case LOW:
                        myPriorityCombo.setSelectedItem("Low");
                        break;
                    case MEDIUM:
                        myPriorityCombo.setSelectedItem("Medium");
                        break;
                    case HIGH:
                        myPriorityCombo.setSelectedItem("High");
                        break;
                    case URGENT:
                        myPriorityCombo.setSelectedItem("Urgent");
                        break;
                    case IMMEDIATE:
                        myPriorityCombo.setSelectedItem("Immediate");
                        break;
                }
            }

            BMObjectsType bmObjects = myObject.getBmObjects();
            if (bmObjects != null) {
                myBmObjects.addAll(bmObjects.getBmObject());
            }
        }
    }

    private boolean verifyFields()
    {
        if (myStartProcessCombo.getSelectedItem().equals("Time Mark") && !myStartProcessTimecode.getInputVerifier().verify(myStartProcessTimecode)) {
            return false;
        }
        if ((myStopProcessCombo.getSelectedItem().equals("Time Mark") || myStopProcessCombo.getSelectedItem().equals("Duration")) && !myStopProcessTimecode.getInputVerifier().verify(myStopProcessTimecode)) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean saveObject()
    {
        if (!verifyFields()) {
            JOptionPane.showMessageDialog(JobDialog.this, "One or more fields have invalid input");
            return false;
        }

        if (myObject == null) {
            switch (myServiceType) {
                case Capture:
                    myObject = new CaptureJobType();
                    break;
                case Transfer:
                    myObject = new TransferJobType();
                    break;
                case Transform:
                    myObject = new TransformJobType();
                    break;
            }
            myObject.setResourceID(UUID.randomUUID().toString());
            myObject.setResourceCreationDate(XmlUtils.createCalendar());
        }

        myObject.setResourceModifiedDate(XmlUtils.createCalendar());

        ExtensionAttributes extensionAttributes = new ExtensionAttributes();
        if (!myClipNameField.getText().isEmpty()) {
            extensionAttributes.getOtherAttributes().put(new QName("http://fims.api.glookast.com", "ClipName"), myClipNameField.getText());
        }
        if (!myTapeNameField.getText().isEmpty()) {
            extensionAttributes.getOtherAttributes().put(new QName("http://fims.api.glookast.com", "TapeName"), myTapeNameField.getText());
        }
        if (!myInterplaySubfolderField.getText().isEmpty()) {
            extensionAttributes.getOtherAttributes().put(new QName("http://fims.api.glookast.com", "InterplaySubfolder"), myInterplaySubfolderField.getText());
        }
        if (!((String) myTimecodeSourceCombo.getSelectedItem()).isEmpty()) {
            extensionAttributes.getOtherAttributes().put(new QName("http://fims.api.glookast.com", "TimecodeSource"), (String) myTimecodeSourceCombo.getSelectedItem());
        }

        myObject.setExtensionAttributes(extensionAttributes);

        switch ((String) myPriorityCombo.getSelectedItem()) {
            case "Low":
                myObject.setPriority(PriorityType.LOW);
                break;
            case "Medium":
                myObject.setPriority(PriorityType.MEDIUM);
                break;
            case "High":
                myObject.setPriority(PriorityType.HIGH);
                break;
            case "Urgent":
                myObject.setPriority(PriorityType.URGENT);
                break;
            case "Immediate":
                myObject.setPriority(PriorityType.IMMEDIATE);
                break;
        }

        switch ((String) myStartJobCombo.getSelectedItem()) {
            case "No Wait":
                myObject.setStartJob(new StartJobByNoWaitType());
                break;
            case "Time":
                StartJobByTimeType startJob = new StartJobByTimeType();
                startJob.setTime(XmlUtils.createCalendar((Date) myStartJobSpinner.getValue()));
                myObject.setStartJob(startJob);
                break;
            case "Latest":
                myObject.setStartJob(new StartJobByLatestType());
                break;
        }

        BMObjectsType bmObjects = new BMObjectsType();
        bmObjects.getBmObject().addAll(myBmObjects);
        myObject.setBmObjects(bmObjects);

        if (myObject instanceof CaptureJobType) {
            CaptureJobType captureJob = (CaptureJobType) myObject;

            switch ((String) myStartProcessCombo.getSelectedItem()) {
                case "No Wait":
                    captureJob.setStartProcess(new StartProcessByNoWaitType());
                    break;
                case "Time":
                    StartProcessByTimeType startProcessTime = new StartProcessByTimeType();
                    startProcessTime.setTime(XmlUtils.createCalendar((Date) myStartProcessSpinner.getValue()));
                    captureJob.setStartProcess(startProcessTime);
                    break;
                case "Time Mark":
                    TimeType time = new TimeType();
                    time.setTimecode(myStartProcessTimecode.getText());
                    StartProcessByTimeMarkType startProcessTimeMark = new StartProcessByTimeMarkType();
                    startProcessTimeMark.setTimeMark(time);
                    captureJob.setStartProcess(startProcessTimeMark);
                    break;
                case "Service Defined Time":
                    captureJob.setStartProcess(new StartProcessByServiceDefinedTimeType());
                    break;
            }

            switch ((String) myStopProcessCombo.getSelectedItem()) {
                case "Open End":
                    captureJob.setStopProcess(new StopProcessByOpenEndType());
                    break;
                case "Time":
                    StopProcessByTimeType stopProcessTime = new StopProcessByTimeType();
                    stopProcessTime.setTime(XmlUtils.createCalendar((Date) myStopProcessSpinner.getValue()));
                    captureJob.setStopProcess(stopProcessTime);
                    break;
                case "Time Mark":
                    TimeType time = new TimeType();
                    time.setTimecode(myStopProcessTimecode.getText());
                    StopProcessByTimeMarkType stopProcessTimeMark = new StopProcessByTimeMarkType();
                    stopProcessTimeMark.setTimeMark(time);
                    captureJob.setStopProcess(stopProcessTimeMark);
                    break;
                case "Duration":
                    DurationType duration = new DurationType();
                    duration.setTimecode(myStopProcessTimecode.getText());
                    StopProcessByDurationType stopProcessDuration = new StopProcessByDurationType();
                    stopProcessDuration.setDuration(duration);
                    captureJob.setStopProcess(stopProcessDuration);
                    break;
                case "Service Defined Time":
                    captureJob.setStopProcess(new StopProcessByServiceDefinedTimeType());
                    break;
            }

            CaptureJobType.Profiles profiles = new CaptureJobType.Profiles();
            for (ProfileType profile : myProfiles) {
                if (profile instanceof CaptureProfileType) {
                    profiles.getCaptureProfile().add((CaptureProfileType) profile);
                }
            }
            captureJob.setProfiles(profiles);
        } else if (myObject instanceof TransferJobType) {
            TransferJobType transferJob = (TransferJobType) myObject;
            TransferJobType.Profiles profiles = new TransferJobType.Profiles();
            for (ProfileType profile : myProfiles) {
                if (profile instanceof TransferProfileType) {
                    profiles.getTransferProfile().add((TransferProfileType) profile);
                }
            }
            transferJob.setProfiles(profiles);
        } else if (myObject instanceof TransformJobType) {
            TransformJobType transformJob = (TransformJobType) myObject;
            TransformJobType.Profiles profiles = new TransformJobType.Profiles();
            for (ProfileType profile : myProfiles) {
                if (profile instanceof TransformProfileType) {
                    profiles.getTransformProfile().add((TransformProfileType) profile);
                }
            }
            transformJob.setProfiles(profiles);
        }

        return true;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "Combo":
                    refreshGUI(e.getSource());
                    break;
            }
        }
    }

    private class TimecodeInputVerifier extends InputVerifier
    {
        private Pattern myPattern;
        private Border myBorder;

        public TimecodeInputVerifier()
        {
            myPattern = Pattern.compile("^(([0-1][0-9])|([2][0-3])):[0-5][0-9]:[0-5][0-9](([.,])|([:;]))[0-2][0-9]$");
        }

        @Override
        public boolean verify(JComponent input)
        {
            if (input instanceof JTextComponent) {
                JTextComponent field = (JTextComponent) input;

                String text = field.getText();
                text = text.replaceAll("[^0-9]", "");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 8 - text.length(); i++) {
                    sb.append("0");
                }
                sb.append(text);
                text = sb.toString();
                text = text.substring(0, 2) + ":" + text.substring(2, 4) + ":" + text.substring(4, 6) + ":" + text.substring(6, 8);
                field.setText(text);
                field.selectAll();

                boolean verified = myPattern.matcher(text).find();

                if (myBorder == null) {
                    myBorder = input.getBorder();
                }

                input.setBorder(verified ? myBorder : BorderFactory.createLineBorder(Color.red));

                return verified;
            }
            return false;
        }
    }

    private class TimecodeFocusListener implements FocusListener
    {
        @Override
        public void focusGained(FocusEvent e)
        {
            ((JTextField) e.getComponent()).selectAll();
            revalidate();
            repaint();
        }

        @Override
        public void focusLost(FocusEvent e)
        {
        }
    }

    private class CaptureProfilesTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Name", "Description"};
        private final int[] myColumnWidths = {200, 200};

        @Override
        public int getTableHeight()
        {
            return 100;
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

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            ProfileType profile = myProfiles.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return profile.getName();
                case 1:
                    return profile.getDescription();
            }

            throw new IndexOutOfBoundsException();
        }
    }

    private class ProfilesTablePanel extends TablePanel<ProfileType>
    {
        public ProfilesTablePanel(String title, List<ProfileType> objects, ScrollTableModel tableModel, AppController controller, boolean editable)
        {
            super(title, objects, tableModel, controller, editable);
        }

        @Override
        protected SelectionDialog<ProfileType> getSelectionDialog(Window owner, AppController controller)
        {
            return new ProfilesDialog(owner, controller, myServiceType);
        }

        @Override
        protected ViewEditDialog<ProfileType> getViewEditDialog(Window owner, AppController controller, boolean editable)
        {
            return new ProfileDialog(owner, controller, myServiceType, editable);
        }

        @Override
        protected void setObject(ProfileType object)
        {
            myController.setProfile(object);
        }

        @Override
        protected String getKey(ProfileType object)
        {
            return object.getResourceID();
        }
    }

    private class BMObjectsTableModel extends ScrollTableModel
    {
        private final String[] myColumnNames = {"Description"};
        private final int[] myColumnWidths = {400};

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

    private class BMObjectTablePanel extends TablePanel<BMObjectType>
    {
        public BMObjectTablePanel(String title, List<BMObjectType> objects, ScrollTableModel tableModel, AppController controller, boolean editable)
        {
            super(title, objects, tableModel, controller, editable);
        }

        @Override
        protected SelectionDialog<BMObjectType> getSelectionDialog(Window owner, AppController controller)
        {
            return new BMObjectsDialog(owner, controller);
        }

        @Override
        protected ViewEditDialog<BMObjectType> getViewEditDialog(Window owner, AppController controller, boolean editable)
        {
            return new BMObjectDialog(owner, controller, editable);
        }

        @Override
        protected void setObject(BMObjectType object)
        {
            myController.setBmObject(object);
        }

        @Override
        protected String getKey(BMObjectType object)
        {
            return object.getResourceID();
        }
    }

}
