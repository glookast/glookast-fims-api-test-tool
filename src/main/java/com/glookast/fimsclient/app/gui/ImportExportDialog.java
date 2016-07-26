package com.glookast.fimsclient.app.gui;

import com.glookast.fimsclient.app.gui.components.OKCancelButtonPanel;
import com.glookast.fimsclient.app.utils.XmlContainer;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import tv.fims.capturemedia.CaptureJobType;
import tv.fims.capturemedia.CaptureProfileType;
import tv.fims.transfermedia.TransferJobType;
import tv.fims.transfermedia.TransferProfileType;
import tv.fims.transformmedia.TransformJobType;
import tv.fims.transformmedia.TransformProfileType;

public class ImportExportDialog extends JDialog
{
    private final ActionListener myActionListener;

    private final JCheckBox myCaptureJobsCheckBox;
    private final JCheckBox myCaptureProfilesCheckBox;
    private final JCheckBox myTransferJobsCheckBox;
    private final JCheckBox myTransferProfilesCheckBox;
    private final JCheckBox myTransformJobsCheckBox;
    private final JCheckBox myTransformProfilesCheckBox;

    private final OKCancelButtonPanel myOKCancelPanel;

    private final Map<String, CaptureJobType> myCaptureJobs;
    private final Map<String, CaptureProfileType> myCaptureProfiles;
    private final Map<String, TransferJobType> myTransferJobs;
    private final Map<String, TransferProfileType> myTransferProfiles;
    private final Map<String, TransformJobType> myTransformJobs;
    private final Map<String, TransformProfileType> myTransformProfiles;

    private boolean myOK;
    private XmlContainer myContainer;

    public ImportExportDialog(Window owner, XmlContainer container, boolean isImport, boolean selectCaptureJobs, boolean selectCaptureProfiles, boolean selectTransferJobs, boolean selectTransferProfiles, boolean selectTransformJobs, boolean selectTransformProfiles)
    {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setTitle(isImport ? "Import" : "Export");

        myActionListener = new ActionListenerImpl();

        getRootPane().registerKeyboardAction(myActionListener, "Cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        myCaptureJobs = new LinkedHashMap<>();
        myCaptureProfiles = new LinkedHashMap<>();
        myTransferJobs = new LinkedHashMap<>();
        myTransferProfiles = new LinkedHashMap<>();
        myTransformJobs = new LinkedHashMap<>();
        myTransformProfiles = new LinkedHashMap<>();

        for (CaptureJobType captureJob : container.getCaptureJob()) {
            for (CaptureProfileType captureProfile : captureJob.getProfiles().getCaptureProfile()) {
                myCaptureProfiles.put(captureProfile.getResourceID(), captureProfile);
            }
            myCaptureJobs.put(captureJob.getResourceID(), captureJob);
        }

        for (TransferJobType transferJob : container.getTransferJob()) {
            for (TransferProfileType transferProfile : transferJob.getProfiles().getTransferProfile()) {
                myTransferProfiles.put(transferProfile.getResourceID(), transferProfile);
            }
            myTransferJobs.put(transferJob.getResourceID(), transferJob);
        }

        for (TransformJobType transformJob : container.getTransformJob()) {
            for (TransformProfileType transformProfile : transformJob.getProfiles().getTransformProfile()) {
                myTransformProfiles.put(transformProfile.getResourceID(), transformProfile);
            }
            myTransformJobs.put(transformJob.getResourceID(), transformJob);
        }

        for (CaptureProfileType captureProfile : container.getCaptureProfile()) {
            myCaptureProfiles.put(captureProfile.getResourceID(), captureProfile);
        }

        for (TransferProfileType transferProfile : container.getTransferProfile()) {
            myTransferProfiles.put(transferProfile.getResourceID(), transferProfile);
        }

        for (TransformProfileType transformProfile : container.getTransformProfile()) {
            myTransformProfiles.put(transformProfile.getResourceID(), transformProfile);
        }

        int nrCaptureJobs = myCaptureJobs.size();
        int nrCaptureProfiles = myCaptureProfiles.size();
        int nrTransferJobs = myTransferJobs.size();
        int nrTransferProfiles = myTransferProfiles.size();
        int nrTransformJobs = myTransformJobs.size();
        int nrTransformProfiles = myTransformProfiles.size();

        myCaptureJobsCheckBox = new JCheckBox(nrCaptureJobs + " Capture Job" + ((nrCaptureJobs != 1) ? "s" : ""));
        myCaptureJobsCheckBox.setSelected(selectCaptureJobs && nrCaptureJobs > 0);
        myCaptureJobsCheckBox.setEnabled(nrCaptureJobs > 0);
        myCaptureJobsCheckBox.addActionListener(myActionListener);
        myCaptureJobsCheckBox.setActionCommand("Check");
        myCaptureProfilesCheckBox = new JCheckBox(nrCaptureProfiles + " Capture Profile" + ((nrCaptureProfiles != 1) ? "s" : ""));
        myCaptureProfilesCheckBox.setSelected(selectCaptureProfiles && nrCaptureProfiles > 0);
        myCaptureProfilesCheckBox.setEnabled(nrCaptureProfiles > 0);
        myCaptureProfilesCheckBox.addActionListener(myActionListener);
        myCaptureProfilesCheckBox.setActionCommand("Check");
        myTransferJobsCheckBox = new JCheckBox(nrTransferJobs + " Transfer Job" + ((nrTransferJobs != 1) ? "s" : ""));
        myTransferJobsCheckBox.setSelected(selectTransferJobs && nrTransferJobs > 0);
        myTransferJobsCheckBox.setEnabled(nrTransferJobs > 0);
        myTransferJobsCheckBox.addActionListener(myActionListener);
        myTransferJobsCheckBox.setActionCommand("Check");
        myTransferProfilesCheckBox = new JCheckBox(nrTransferProfiles + " Transfer Profile" + ((nrTransferProfiles != 1) ? "s" : ""));
        myTransferProfilesCheckBox.setSelected(selectTransferProfiles && nrTransferProfiles > 0);
        myTransferProfilesCheckBox.setEnabled(nrTransferProfiles > 0);
        myTransferProfilesCheckBox.addActionListener(myActionListener);
        myTransferProfilesCheckBox.setActionCommand("Check");
        myTransformJobsCheckBox = new JCheckBox(nrTransformJobs + " Transform Job" + ((nrTransformJobs != 1) ? "s" : ""));
        myTransformJobsCheckBox.setSelected(selectTransformJobs && nrTransformJobs > 0);
        myTransformJobsCheckBox.setEnabled(nrTransformJobs > 0);
        myTransformJobsCheckBox.addActionListener(myActionListener);
        myTransformJobsCheckBox.setActionCommand("Check");
        myTransformProfilesCheckBox = new JCheckBox(nrTransformProfiles + " Transform Profile" + ((nrTransformProfiles != 1) ? "s" : ""));
        myTransformProfilesCheckBox.setSelected(selectTransformProfiles && nrTransformProfiles > 0);
        myTransformProfilesCheckBox.setEnabled(nrTransformProfiles > 0);
        myTransformProfilesCheckBox.addActionListener(myActionListener);
        myTransformProfilesCheckBox.setActionCommand("Check");

        myOKCancelPanel = new OKCancelButtonPanel(myActionListener, true);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);

        add(myCaptureJobsCheckBox, c);
        add(myCaptureProfilesCheckBox, c);
        add(myTransferJobsCheckBox, c);
        add(myTransferProfilesCheckBox, c);
        add(myTransformJobsCheckBox, c);
        add(myTransformProfilesCheckBox, c);
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
                boolean enabled = myCaptureProfilesCheckBox.isSelected() ||
                                  myCaptureJobsCheckBox.isSelected() ||
                                  myTransferProfilesCheckBox.isSelected() ||
                                  myTransferJobsCheckBox.isSelected() ||
                                  myTransformProfilesCheckBox.isSelected() ||
                                  myTransformJobsCheckBox.isSelected();

                myOKCancelPanel.getOKButton().setEnabled(enabled);
            }
        });
    }

    public boolean isOK()
    {
        return myOK;
    }

    public XmlContainer getContainer()
    {
        return myContainer;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "Check":
                    refreshGUI();
                    break;
                case "OK":
                    createContainer();
                    myOK = true;
                    dispose();
                    break;
                case "Cancel":
                    dispose();
                    break;
            }
        }
    }

    private void createContainer()
    {
        myContainer = new XmlContainer();

        if (myCaptureProfilesCheckBox.isSelected()) {
            myContainer.getCaptureProfile().addAll(myCaptureProfiles.values());
        }

        if (myCaptureJobsCheckBox.isSelected()) {
            myContainer.getCaptureJob().addAll(myCaptureJobs.values());
        }

        if (myTransferProfilesCheckBox.isSelected()) {
            myContainer.getTransferProfile().addAll(myTransferProfiles.values());
        }

        if (myTransferJobsCheckBox.isSelected()) {
            myContainer.getTransferJob().addAll(myTransferJobs.values());
        }

        if (myTransformProfilesCheckBox.isSelected()) {
            myContainer.getTransformProfile().addAll(myTransformProfiles.values());
        }

        if (!myTransformJobsCheckBox.isSelected()) {
            myContainer.getTransformJob().addAll(myTransformJobs.values());
        }
    }
}
