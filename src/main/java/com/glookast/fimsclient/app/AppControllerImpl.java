package com.glookast.fimsclient.app;

import com.glookast.fimsclient.app.AppController.EventListener.Event;
import com.glookast.fimsclient.app.utils.XmlContainer;
import com.glookast.fimsclient.app.utils.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import tv.fims.base.AudioFormatType;
import tv.fims.base.BMObjectType;
import tv.fims.base.ContainerFormatType;
import tv.fims.base.JobType;
import tv.fims.base.ProfileType;
import tv.fims.base.TransferAtomType;
import tv.fims.base.VideoFormatType;
import tv.fims.capturemedia.CaptureJobType;
import tv.fims.capturemedia.CaptureProfileType;
import tv.fims.transfermedia.TransferJobType;
import tv.fims.transfermedia.TransferProfileType;
import tv.fims.transformmedia.TransformJobType;
import tv.fims.transformmedia.TransformProfileType;

public class AppControllerImpl implements AppController
{
    private final ReentrantLock myListenersLock = new ReentrantLock();
    private final List<EventListener> myListeners = new ArrayList<>();

    private final Map<Service.Type, List<Service>> myServices;
    private final ReentrantReadWriteLock myServicesLock;
    private final Map<Service.Type, Service> myActiveServices;

    private final Map<String, CaptureJobType> myCaptureJobs = new LinkedHashMap<>();
    private final Map<String, CaptureProfileType> myCaptureProfiles = new LinkedHashMap<>();

    private final Map<String, TransferJobType> myTransferJobs = new LinkedHashMap<>();
    private final Map<String, TransferProfileType> myTransferProfiles = new LinkedHashMap<>();

    private final Map<String, TransformJobType> myTransformJobs = new LinkedHashMap<>();
    private final Map<String, TransformProfileType> myTransformProfiles = new LinkedHashMap<>();

    private final Map<String, BMObjectType> myBmObjects = new LinkedHashMap<>();

    private String myPropertiesFilename = "application.properties";

    public AppControllerImpl()
    {
        myServices = new LinkedHashMap<>();
        myServices.put(Service.Type.Capture, new ArrayList<Service>());
        myServices.put(Service.Type.Transfer, new ArrayList<Service>());
        myServices.put(Service.Type.Transform, new ArrayList<Service>());
        myServicesLock = new ReentrantReadWriteLock();
        myActiveServices = new LinkedHashMap<>();
    }

    @Override
    public List<Service> getServices(Service.Type serviceType)
    {
        myServicesLock.readLock().lock();
        try {
            return Collections.unmodifiableList(myServices.get(serviceType));
        } finally {
            myServicesLock.readLock().unlock();
        }
    }

    @Override
    public void setService(Service service)
    {
        myServicesLock.writeLock().lock();
        try {
            myServices.get(service.getType()).add(service);
            myActiveServices.clear();
            notifyEventListeners(Event.Services);
        } finally {
            myServicesLock.writeLock().unlock();
        }
    }

    @Override
    public void delService(Service service)
    {
        myServicesLock.writeLock().lock();
        try {
            service.stop();
            myServices.get(service.getType()).remove(service);
            myActiveServices.clear();
            notifyEventListeners(Event.Services);
        } finally {
            myServicesLock.writeLock().unlock();
        }
    }

    @Override
    public Service getActiveService(Service.Type serviceType)
    {
        myServicesLock.readLock().lock();
        try {
            return myActiveServices.get(serviceType);
        } finally {
            myServicesLock.readLock().unlock();
        }
    }

    @Override
    public void setActiveService(Service service)
    {
        myServicesLock.writeLock().lock();
        try {
            myActiveServices.put(service.getType(), service);
            notifyEventListeners(Event.ActiveServiceChanged);
        } finally {
            myServicesLock.writeLock().unlock();
        }
    }

    @Override
    public List<JobType> getJobs(Service.Type serviceType)
    {
        List<JobType> list = new ArrayList<>();
        switch (serviceType) {
            case Capture:
                list.addAll(myCaptureJobs.values());
                break;
            case Transfer:
                list.addAll(myTransferJobs.values());
                break;
            case Transform:
                list.addAll(myTransformJobs.values());
                break;
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public void setJob(JobType job)
    {
        if (job instanceof CaptureJobType) {
            myCaptureJobs.put(job.getResourceID(), (CaptureJobType) job);
            notifyEventListeners(Event.CaptureJobs);
        } else if (job instanceof TransferJobType) {
            myTransferJobs.put(job.getResourceID(), (TransferJobType) job);
            notifyEventListeners(Event.TransferJobs);
        } else if (job instanceof TransformJobType) {
            myTransformJobs.put(job.getResourceID(), (TransformJobType) job);
            notifyEventListeners(Event.TransformJobs);
        }
        saveApplicationData();
    }

    @Override
    public void delJob(JobType job)
    {
        if (job instanceof CaptureJobType) {
            myCaptureJobs.remove(job.getResourceID());
            notifyEventListeners(Event.CaptureJobs);
        } else if (job instanceof TransferJobType) {
            myTransferJobs.remove(job.getResourceID());
            notifyEventListeners(Event.TransferJobs);
        } else if (job instanceof TransformJobType) {
            myTransformJobs.remove(job.getResourceID());
            notifyEventListeners(Event.TransformJobs);
        }
        saveApplicationData();
    }

    @Override
    public List<ProfileType> getProfiles(Service.Type serviceType)
    {
        List<ProfileType> list = new ArrayList<>();
        switch (serviceType) {
            case Capture:
                list.addAll(myCaptureProfiles.values());
                break;
            case Transfer:
                list.addAll(myTransferProfiles.values());
                break;
            case Transform:
                list.addAll(myTransformProfiles.values());
                break;
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public void setProfile(ProfileType profile)
    {
        if (profile instanceof CaptureProfileType) {
            myCaptureProfiles.put(profile.getResourceID(), (CaptureProfileType) profile);
            notifyEventListeners(Event.CaptureProfiles);
        } else if (profile instanceof TransferProfileType) {
            myTransferProfiles.put(profile.getResourceID(), (TransferProfileType) profile);
            notifyEventListeners(Event.TransferProfiles);
        } else if (profile instanceof TransformProfileType) {
            myTransformProfiles.put(profile.getResourceID(), (TransformProfileType) profile);
            notifyEventListeners(Event.TransformProfiles);
        }
        saveApplicationData();
    }

    @Override
    public void delProfile(ProfileType profile)
    {
        if (profile instanceof CaptureProfileType) {
            myCaptureProfiles.remove(profile.getResourceID());
            notifyEventListeners(Event.CaptureProfiles);
        } else if (profile instanceof TransferProfileType) {
            myTransferProfiles.remove(profile.getResourceID());
            notifyEventListeners(Event.TransferProfiles);
        } else if (profile instanceof TransformProfileType) {
            myTransformProfiles.remove(profile.getResourceID());
            notifyEventListeners(Event.TransformProfiles);
        }
        saveApplicationData();
    }

    @Override
    public List<BMObjectType> getBmObjects()
    {
        List<BMObjectType> list = new ArrayList<>();
        list.addAll(myBmObjects.values());
        return Collections.unmodifiableList(list);
    }

    @Override
    public void setBmObject(BMObjectType bmObject)
    {
        myBmObjects.put(bmObject.getResourceID(), bmObject);
        notifyEventListeners(Event.BmObjects);
    }

    @Override
    public void delBmObject(BMObjectType bmObject)
    {
        myBmObjects.remove(bmObject.getResourceID());
        notifyEventListeners(Event.BmObjects);
    }

    @Override
    public List<VideoFormatType> getVideoFormats(Service.Type serviceType)
    {
        List<Service> services = getServices(serviceType);

        Map<String, VideoFormatType> map = new LinkedHashMap<>();

        for (Service service : services) {
            List<VideoFormatType> formats = service.getVideoFormats();
            for (VideoFormatType format : formats) {
                if (!map.containsKey(format.getResourceID())) {
                    map.put(format.getResourceID(), format);
                }
            }
        }

        List<VideoFormatType> list = new ArrayList<>();
        list.addAll(map.values());
        return list;
    }

    @Override
    public List<AudioFormatType> getAudioFormats(Service.Type serviceType)
    {
        List<Service> services = getServices(serviceType);

        Map<String, AudioFormatType> map = new LinkedHashMap<>();

        for (Service service : services) {
            List<AudioFormatType> formats = service.getAudioFormats();
            for (AudioFormatType format : formats) {
                if (!map.containsKey(format.getResourceID())) {
                    map.put(format.getResourceID(), format);
                }
            }
        }

        List<AudioFormatType> list = new ArrayList<>();
        list.addAll(map.values());
        return list;
    }

    @Override
    public List<ContainerFormatType> getContainerFormats(Service.Type serviceType)
    {
        List<Service> services = getServices(serviceType);

        Map<String, ContainerFormatType> map = new LinkedHashMap<>();

        for (Service service : services) {
            List<ContainerFormatType> formats = service.getContainerFormats();
            for (ContainerFormatType format : formats) {
                if (!map.containsKey(format.getResourceID())) {
                    map.put(format.getResourceID(), format);
                }
            }
        }

        List<ContainerFormatType> list = new ArrayList<>();
        list.addAll(map.values());
        return list;
    }

    @Override
    public List<TransferAtomType> getDestinations(Service.Type serviceType)
    {
        List<Service> services = getServices(serviceType);

        Map<String, TransferAtomType> map = new LinkedHashMap<>();

        for (Service service : services) {
            List<TransferAtomType> transferAtoms = service.getDestinations();
            for (TransferAtomType transferAtom : transferAtoms) {
                if (!map.containsKey(transferAtom.getDestination())) {
                    map.put(transferAtom.getDestination(), transferAtom);
                }
            }
        }

        List<TransferAtomType> list = new ArrayList<>();
        list.addAll(map.values());
        return list;
    }

    @Override
    public void addListener(EventListener listener)
    {
        try {
            myListenersLock.lock();
            myListeners.remove(listener);
            myListeners.add(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    @Override
    public void removeListener(EventListener listener)
    {
        try {
            myListenersLock.lock();
            myListeners.remove(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    private void notifyEventListeners(Event event)
    {
        try {
            myListenersLock.lock();
            for (EventListener listener : myListeners) {
                listener.onEvent(event);
            }
        } finally {
            myListenersLock.unlock();
        }
    }

    @Override
    public String getPropertiesFilename()
    {
        return myPropertiesFilename;
    }

    @Override
    public void setPropertiesFilename(String propertiesFilename)
    {
        myPropertiesFilename = propertiesFilename;
    }

    @Override
    public void loadProperties()
    {
        try (InputStream is = new FileInputStream(myPropertiesFilename)) {
            Properties props = new Properties();
            props.load(is);

            int serviceCount = 0;

            try {
                serviceCount = Integer.valueOf(props.getProperty("service.count"));
            } catch (Exception ex) {
            }

            for (int i = 0; i < serviceCount; i++) {
                try {
                    String name = props.getProperty("service." + i + ".name");
                    Service.Type serviceType = Service.Type.valueOf(props.getProperty("service." + i + ".type"));
                    Service.Method serviceMethod = Service.Method.valueOf(props.getProperty("service." + i + ".method"));
                    URL address = new URL(props.getProperty("service." + i + ".address"));
                    String callbackAddressString = props.getProperty("service." + i + ".callback", "");
                    URL callbackAddress = null;
                    if (!callbackAddressString.isEmpty()) {
                        callbackAddress = new URL(callbackAddressString);
                    }

                    Service service = ServiceFactory.createService(name, serviceType, serviceMethod, address, callbackAddress);
                    if (service != null) {
                        setService(service);
                    }
                } catch (Exception ex) {
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void saveProperties()
    {
        try (OutputStream os = new FileOutputStream(myPropertiesFilename)) {
            Properties props = new Properties();

            myServicesLock.readLock().lock();
            try {
                List<Service> services = new ArrayList<>();
                services.addAll(myServices.get(Service.Type.Capture));
                services.addAll(myServices.get(Service.Type.Transfer));
                services.addAll(myServices.get(Service.Type.Transform));

                int serviceCount = services.size();
                props.setProperty("service.count", String.valueOf(serviceCount));

                for (int i = 0; i < serviceCount; i++) {
                    Service service = services.get(i);
                    props.setProperty("service." + i + ".name", service.getName());
                    props.setProperty("service." + i + ".type", String.valueOf(service.getType()));
                    props.setProperty("service." + i + ".method", String.valueOf(service.getMethod()));
                    props.setProperty("service." + i + ".address", String.valueOf(service.getAddress()));
                    props.setProperty("service." + i + ".callback", (service.getCallbackAddress() != null) ? String.valueOf(service.getCallbackAddress()) : "");
                }
            } finally {
                myServicesLock.readLock().unlock();
            }

            props.store(os, "");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void loadApplicationData()
    {
        File file = new File("application_data.xml");
        if (file.exists()) {
            try {
                XmlContainer container = XmlUtils.readXMLContainerFromFile(file);
                for (CaptureJobType job : container.getCaptureJob()) {
                    myCaptureJobs.put(job.getResourceID(), job);
                }
                for (TransferJobType job : container.getTransferJob()) {
                    myTransferJobs.put(job.getResourceID(), job);
                }
                for (TransformJobType job : container.getTransformJob()) {
                    myTransformJobs.put(job.getResourceID(), job);
                }
                for (CaptureProfileType profile : container.getCaptureProfile()) {
                    myCaptureProfiles.put(profile.getResourceID(), profile);
                }
                for (TransferProfileType profile : container.getTransferProfile()) {
                    myTransferProfiles.put(profile.getResourceID(), profile);
                }
                for (TransformProfileType profile : container.getTransformProfile()) {
                    myTransformProfiles.put(profile.getResourceID(), profile);
                }

            } catch (JAXBException ex) {
                Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void saveApplicationData()
    {
        try {
            XmlContainer container = new XmlContainer();
            container.getCaptureJob().addAll(myCaptureJobs.values());
            container.getTransferJob().addAll(myTransferJobs.values());
            container.getTransformJob().addAll(myTransformJobs.values());
            container.getCaptureProfile().addAll(myCaptureProfiles.values());
            container.getTransferProfile().addAll(myTransferProfiles.values());
            container.getTransformProfile().addAll(myTransformProfiles.values());

            XmlUtils.writeXMLContainerToFile(container, new File("application_data.xml"));
        } catch (JAXBException ex) {
            Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
