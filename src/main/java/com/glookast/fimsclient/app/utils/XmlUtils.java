package com.glookast.fimsclient.app.utils;

import com.glookast.fimsclient.app.AppController;
import com.glookast.fimsclient.app.Service;
import com.glookast.fimsclient.app.gui.ImportExportDialog;
import java.awt.Window;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import tv.fims.base.JobType;
import tv.fims.base.ProfileType;
import tv.fims.capturemedia.CaptureJobType;
import tv.fims.capturemedia.CaptureProfileType;
import tv.fims.transfermedia.TransferJobType;
import tv.fims.transfermedia.TransferProfileType;
import tv.fims.transformmedia.TransformJobType;
import tv.fims.transformmedia.TransformProfileType;

public class XmlUtils
{
    private static final JAXBContext theJaxbContext;

    static {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(XmlContainer.class, org.smpte_ra.schemas.st2071._2015.identity.ObjectFactory.class);
        } catch (JAXBException ex) {
            Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        theJaxbContext = jaxbContext;
    }

    public static Calendar createCalendar()
    {
        return Calendar.getInstance();
    }

    public static Calendar createCalendar(Date date)
    {
        Calendar calendar = createCalendar();
        calendar.setTime(date);
        return calendar;
    }

    public static boolean doImport(Window owner, AppController controller, boolean selectCaptureJobs, boolean selectCaptureProfiles, boolean selectTransferJobs, boolean selectTransferProfiles, boolean selectTransformJobs, boolean selectTransformProfiles)
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new XmlFileFilter());

        if (fileChooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                try {
                    XmlContainer container = readXMLContainerFromFile(file);
                    ImportExportDialog dialog = new ImportExportDialog(owner, container, true, selectCaptureJobs, selectCaptureProfiles, selectTransferJobs, selectTransferProfiles, selectTransformJobs, selectTransformProfiles);
                    dialog.setVisible(true);
                    if (dialog.isOK()) {
                        container = dialog.getContainer();

                        int skippedCaptureProfiles = 0;
                        int skippedTransferProfiles = 0;
                        int skippedTransformProfiles = 0;
                        int skippedDestinations = 0;

                        if (!container.getCaptureProfile().isEmpty()) {
                            Map<String, ProfileType> profiles = new LinkedHashMap<>();

                            for (ProfileType profile : controller.getProfiles(Service.Type.Capture)) {
                                profiles.put(profile.getResourceID(), profile);
                            }

                            for (CaptureProfileType captureProfile : container.getCaptureProfile()) {
                                if (profiles.containsKey(captureProfile.getResourceID())) {
                                    skippedCaptureProfiles++;
                                    continue;
                                }
                                controller.setProfile(captureProfile);
                            }
                        }

                        for (CaptureJobType captureJob : container.getCaptureJob()) {
                            captureJob.setResourceID(UUID.randomUUID().toString());
                            controller.setJob(captureJob);
                        }

                        if (!container.getTransferProfile().isEmpty()) {
                            Map<String, ProfileType> profiles = new LinkedHashMap<>();

                            for (ProfileType profile : controller.getProfiles(Service.Type.Transfer)) {
                                profiles.put(profile.getResourceID(), profile);
                            }

                            for (TransferProfileType transferProfile : container.getTransferProfile()) {
                                if (profiles.containsKey(transferProfile.getResourceID())) {
                                    skippedTransferProfiles++;
                                    continue;
                                }
                                controller.setProfile(transferProfile);
                            }
                        }

                        for (TransferJobType transferJob : container.getTransferJob()) {
                            transferJob.setResourceID(UUID.randomUUID().toString());
                            controller.setJob(transferJob);
                        }

                        if (!container.getTransformProfile().isEmpty()) {
                            Map<String, ProfileType> profiles = new LinkedHashMap<>();

                            for (ProfileType profile : controller.getProfiles(Service.Type.Transform)) {
                                profiles.put(profile.getResourceID(), profile);
                            }

                            for (TransformProfileType transformProfile : container.getTransformProfile()) {
                                if (profiles.containsKey(transformProfile.getResourceID())) {
                                    skippedTransformProfiles++;
                                    continue;
                                }
                                controller.setProfile(transformProfile);
                            }
                        }

                        for (TransformJobType transformJob : container.getTransformJob()) {
                            transformJob.setResourceID(UUID.randomUUID().toString());
                            controller.setJob(transformJob);
                        }

                        if (skippedCaptureProfiles > 0 || skippedTransferProfiles > 0 || skippedTransformProfiles > 0 || skippedDestinations > 0) {
                            StringBuilder sb = new StringBuilder();

                            sb.append("The following items were not imported due to duplicates already present:\n");
                            if (skippedCaptureProfiles > 0) {
                                sb.append(skippedCaptureProfiles);
                                sb.append(" Capture Profile");
                                if (skippedCaptureProfiles > 1) {
                                    sb.append("s");
                                }
                                sb.append("\n");
                            }
                            if (skippedTransferProfiles > 0) {
                                sb.append(skippedTransferProfiles);
                                sb.append(" Transfer Profile");
                                if (skippedTransferProfiles > 1) {
                                    sb.append("s");
                                }
                                sb.append("\n");
                            }
                            if (skippedTransformProfiles > 0) {
                                sb.append(skippedTransformProfiles);
                                sb.append(" Transform Profile");
                                if (skippedTransformProfiles > 1) {
                                    sb.append("s");
                                }
                                sb.append("\n");
                            }
                            if (skippedDestinations > 0) {
                                sb.append(skippedDestinations);
                                sb.append(" Destination");
                                if (skippedDestinations > 1) {
                                    sb.append("s");
                                }
                                sb.append("\n");
                            }

                            JOptionPane.showMessageDialog(owner, sb.toString(), "Import Warning", JOptionPane.WARNING_MESSAGE);
                        }

                    }
                    return true;
                } catch (JAXBException ex) {
                    JOptionPane.showMessageDialog(owner, "Failed to load file", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

    public static boolean doExport(Window owner, List<JobType> jobs, List<ProfileType> profiles, boolean selectCaptureJobs, boolean selectCaptureProfiles, boolean selectTransferJobs, boolean selectTransferProfiles, boolean selectTransformJobs, boolean selectTransformProfiles)
    {
        XmlContainer container = new XmlContainer();

        if (jobs != null) {
            for (JobType job : jobs) {
                if (job instanceof CaptureJobType) {
                    container.getCaptureJob().add((CaptureJobType) job);
                } else if (job instanceof TransferJobType) {
                    container.getTransferJob().add((TransferJobType) job);
                } else if (job instanceof TransformJobType) {
                    container.getTransformJob().add((TransformJobType) job);
                }
            }
        }

        if (profiles != null) {
            for (ProfileType profile : profiles) {
                if (profile instanceof CaptureProfileType) {
                    container.getCaptureProfile().add((CaptureProfileType) profile);
                } else if (profile instanceof TransferProfileType) {
                    container.getTransferProfile().add((TransferProfileType) profile);
                } else if (profile instanceof TransformProfileType) {
                    container.getTransformProfile().add((TransformProfileType) profile);
                }
            }
        }

        ImportExportDialog dialog = new ImportExportDialog(owner, container, false, selectCaptureJobs, selectCaptureProfiles, selectTransferJobs, selectTransferProfiles, selectTransformJobs, selectTransformProfiles);
        dialog.setVisible(true);
        if (dialog.isOK()) {
            container = dialog.getContainer();
            if (!container.getCaptureJob().isEmpty() ||
                !container.getCaptureProfile().isEmpty() ||
                !container.getTransferJob().isEmpty() ||
                !container.getTransferProfile().isEmpty() ||
                !container.getTransformJob().isEmpty() ||
                !container.getTransformProfile().isEmpty()) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new XmlFileFilter());

                if (fileChooser.showSaveDialog(owner) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (file != null) {
                        try {
                            if (!file.getName().toLowerCase().endsWith(".xml")) {
                                file = new File(file.getParentFile(), file.getName() + ".xml");
                            }
                            XmlUtils.writeXMLContainerToFile(container, file);
                            JOptionPane.showMessageDialog(owner, "Successfully exported to file", "Success", JOptionPane.INFORMATION_MESSAGE);
                            return true;
                        } catch (JAXBException ex) {
                            JOptionPane.showMessageDialog(owner, "Failed to export to file", "Error", JOptionPane.ERROR_MESSAGE);
                            Logger.getLogger(XmlUtils.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }

        return false;
    }

    public static void writeXMLContainerToFile(XmlContainer container, File file) throws JAXBException
    {
        Marshaller m = theJaxbContext.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(container, file);
    }

    public static XmlContainer readXMLContainerFromFile(File file) throws JAXBException
    {
        Unmarshaller un = theJaxbContext.createUnmarshaller();
        return (XmlContainer) un.unmarshal(file);
    }

    private static class XmlFileFilter extends FileFilter
    {
        @Override
        public boolean accept(File f)
        {
            return f.isFile() && f.getName().toLowerCase().endsWith(".xml");
        }

        @Override
        public String getDescription()
        {
            return "*.xml";
        }
    }
}
