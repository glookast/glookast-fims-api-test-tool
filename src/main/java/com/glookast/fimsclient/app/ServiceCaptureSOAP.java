package com.glookast.fimsclient.app;

import com.glookast.fimsclient.app.Service.EventListener.Event;
import com.glookast.fimsclient.services.soap.CaptureMediaServiceSOAP;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;
import tv.fims.base.AsyncEndpointType;
import tv.fims.base.AudioFormatType;
import tv.fims.base.ContainerFormatType;
import tv.fims.base.JobCommandType;
import tv.fims.base.JobType;
import tv.fims.base.ListFilterType;
import tv.fims.base.PriorityType;
import tv.fims.base.TransferAtomType;
import tv.fims.base.VideoFormatType;
import tv.fims.capturemedia.CaptureFaultMsg;
import tv.fims.capturemedia.CaptureFaultNotificationType;
import tv.fims.capturemedia.CaptureFaultType;
import tv.fims.capturemedia.CaptureJobType;
import tv.fims.capturemedia.CaptureMediaNotification;
import tv.fims.capturemedia.CaptureNotificationType;
import tv.fims.capturemedia.FaultMsg;

public class ServiceCaptureSOAP implements Service
{
    private final ExecutorService myExecutorService;

    private final ReentrantLock myListenersLock;
    private final List<EventListener> myListeners;

    private final String myName;
    private final URL myAddress;
    private final URL myCallbackAddress;
    private final String myCallbackAddressString;
    private final CaptureMediaServiceSOAP myService;
    private final Endpoint myCallbackEndpoint;

    private volatile boolean myConnected;

    private final ReentrantReadWriteLock myReadWriteLock;
    private final List<ContainerFormatType> myContainerFormats;
    private final List<VideoFormatType> myVideoFormats;
    private final List<AudioFormatType> myAudioFormats;
    private final List<TransferAtomType> myDestinations;
    private final List<JobType> myJobs;

    public ServiceCaptureSOAP(String name, URL address, URL callbackAddress)
    {
        myExecutorService = Executors.newSingleThreadExecutor();

        myListenersLock = new ReentrantLock();
        myListeners = new ArrayList<>();

        myName = name;
        myAddress = address;
        myCallbackAddress = callbackAddress;
        myService = new CaptureMediaServiceSOAP(myAddress);

        Endpoint callbackEndpoint = null;
        String callbackAddressString = null;
        if (myCallbackAddress != null) {
            callbackAddressString = String.valueOf(myCallbackAddress) + "/CaptureMediaNotificationPort";
            try {
                Logger.getLogger(ServiceCaptureSOAP.class.getName()).log(Level.INFO, "Start listening: " + callbackAddressString);
                callbackEndpoint = Endpoint.publish(callbackAddressString, new CaptureMediaNotificationPort());
            } catch (Exception ex) {
                Logger.getLogger(ServiceCaptureSOAP.class.getName()).log(Level.INFO, null, ex);
            }
        }
        myCallbackAddressString = callbackAddressString;
        myCallbackEndpoint = callbackEndpoint;

        myReadWriteLock = new ReentrantReadWriteLock();
        myContainerFormats = new ArrayList<>();
        myVideoFormats = new ArrayList<>();
        myAudioFormats = new ArrayList<>();
        myDestinations = new ArrayList<>();
        myJobs = new ArrayList<>();

        setConnected(true);
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public Service.Type getType()
    {
        return Service.Type.Capture;
    }

    @Override
    public Service.Method getMethod()
    {
        return Service.Method.SOAP;
    }

    @Override
    public URL getAddress()
    {
        return myAddress;
    }

    @Override
    public URL getCallbackAddress()
    {
        return myCallbackAddress;
    }

    @Override
    public boolean isConnected()
    {
        return myConnected;
    }

    @Override
    public List<ContainerFormatType> getContainerFormats()
    {
        myReadWriteLock.readLock().lock();
        try {
            return Collections.unmodifiableList(myContainerFormats);
        } finally {
            myReadWriteLock.readLock().unlock();
        }
    }

    @Override
    public List<VideoFormatType> getVideoFormats()
    {
        myReadWriteLock.readLock().lock();
        try {
            return Collections.unmodifiableList(myVideoFormats);
        } finally {
            myReadWriteLock.readLock().unlock();
        }
    }

    @Override
    public List<AudioFormatType> getAudioFormats()
    {
        myReadWriteLock.readLock().lock();
        try {
            return Collections.unmodifiableList(myAudioFormats);
        } finally {
            myReadWriteLock.readLock().unlock();
        }
    }

    @Override
    public List<TransferAtomType> getDestinations()
    {
        myReadWriteLock.readLock().lock();
        try {
            return Collections.unmodifiableList(myDestinations);
        } finally {
            myReadWriteLock.readLock().unlock();
        }
    }

    private void setConnected(boolean connected)
    {
        if (connected != myConnected) {
            myConnected = connected;
            notifyEventListeners(Event.ConnectionStatus);

            if (myConnected) {
                loadFormats();
            }
        }
    }

    @Override
    public void queryJobs(ListFilterType filter)
    {
        myExecutorService.execute(new WebserviceTask(filter));
    }

    @Override
    public List<JobType> getJobs()
    {
        myReadWriteLock.readLock().lock();
        try {
            return Collections.unmodifiableList(myJobs);
        } finally {
            myReadWriteLock.readLock().unlock();
        }
    }

    @Override
    public Future<Boolean> sendJob(JobType job)
    {
        if (myCallbackAddressString != null) {
            AsyncEndpointType asyncEndpoint = new AsyncEndpointType();
            asyncEndpoint.setReplyTo(myCallbackAddressString);
            asyncEndpoint.setFaultTo(myCallbackAddressString);
            job.setNotifyAt(asyncEndpoint);
        }

        return myExecutorService.submit(new WebserviceSendJobTask(job));
    }

    @Override
    public void manageJob(JobType job, JobCommandType jobCommand)
    {
        myExecutorService.execute(new WebserviceTask(job, jobCommand, null));
    }

    @Override
    public void manageJobPriority(JobType job, PriorityType priority)
    {
        myExecutorService.execute(new WebserviceTask(job, JobCommandType.MODIFY_PRIORITY, priority));
    }

    @Override
    public void stop()
    {
        if (myCallbackEndpoint != null) {
            myCallbackEndpoint.stop();
        }
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

    private void notifyEventListeners(EventListener.Event event)
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

    private void notifyEventListeners(CaptureJobType job, CaptureFaultType fault)
    {
        try {
            myListenersLock.lock();
            for (EventListener listener : myListeners) {
                if (fault == null) {
                    listener.onResult(job);
                } else {
                    listener.onFault(job, fault);
                }
            }
        } finally {
            myListenersLock.unlock();
        }
    }

    private void loadFormats()
    {
        myExecutorService.execute(new WebserviceTask(TaskType.getContainerFormats));
        myExecutorService.execute(new WebserviceTask(TaskType.getVideoFormats));
        myExecutorService.execute(new WebserviceTask(TaskType.getAudioFormats));
        myExecutorService.execute(new WebserviceTask(TaskType.getDestinations));
    }

    private class WebserviceSendJobTask implements Callable<Boolean>
    {
        private final JobType myJob;

        public WebserviceSendJobTask(JobType job)
        {
            myJob = job;
        }

        @Override
        public Boolean call() throws Exception
        {
            if (myJob instanceof CaptureJobType) {
                try {
                    CaptureJobType job = myService.sendJob((CaptureJobType) myJob);
                    myReadWriteLock.writeLock().lock();
                    try {
                        myJobs.add(job);
                    } finally {
                        myReadWriteLock.writeLock().unlock();
                        notifyEventListeners(Event.Jobs);
                    }
                    setConnected(true);
                    return true;
                } catch (WebServiceException ex) {
                    System.err.println(ex.toString());
                    setConnected(false);
                } catch (CaptureFaultMsg ex) {
                    System.out.println(ex.getFaultInfo().getCode() + " " + ex.getFaultInfo().getDescription() + " " + ex.getFaultInfo().getDetail());
                }
            }
            return false;
        }
    }

    private class WebserviceTask implements Runnable
    {
        private final TaskType myType;
        private final ListFilterType myFilter;
        private final JobType myJob;
        private final JobCommandType myJobCommand;
        private final PriorityType myPriority;

        public WebserviceTask(TaskType type)
        {
            this(type, null, null, null, null);
        }

        public WebserviceTask(ListFilterType filter)
        {
            this(TaskType.queryJob, filter, null, null, null);
        }

        public WebserviceTask(JobType job, JobCommandType jobCommand, PriorityType priority)
        {
            this(TaskType.manageJob, null, job, jobCommand, priority);
        }

        private WebserviceTask(TaskType type, ListFilterType filter, JobType job, JobCommandType jobCommand, PriorityType priority)
        {
            myType = type;
            myFilter = filter;
            myJob = job;
            myJobCommand = jobCommand;
            myPriority = priority;
        }

        @Override
        public void run()
        {
            try {
                switch (myType) {
                    case getContainerFormats:
                        List<ContainerFormatType> containerFormats = myService.getContainerFormats();
                        myReadWriteLock.writeLock().lock();
                        try {
                            myContainerFormats.clear();
                            myContainerFormats.addAll(containerFormats);
                        } finally {
                            myReadWriteLock.writeLock().unlock();
                        }
                        break;
                    case getVideoFormats:
                        List<VideoFormatType> videoFormats = myService.getVideoFormats();
                        myReadWriteLock.writeLock().lock();
                        try {
                            myVideoFormats.clear();
                            myVideoFormats.addAll(videoFormats);
                        } finally {
                            myReadWriteLock.writeLock().unlock();
                        }
                        break;
                    case getAudioFormats:
                        List<AudioFormatType> audioFormats = myService.getAudioFormats();
                        myReadWriteLock.writeLock().lock();
                        try {
                            myAudioFormats.clear();
                            myAudioFormats.addAll(audioFormats);
                        } finally {
                            myReadWriteLock.writeLock().unlock();
                        }
                        break;
                    case getDestinations:
                        List<TransferAtomType> destinations = myService.getDestinations();
                        myReadWriteLock.writeLock().lock();
                        try {
                            myDestinations.clear();
                            myDestinations.addAll(destinations);
                        } finally {
                            myReadWriteLock.writeLock().unlock();
                        }
                        break;
                    case queryJob:
                        List<CaptureJobType> jobs = myService.queryJob(myFilter);
                        myReadWriteLock.writeLock().lock();
                        try {
                            myJobs.clear();
                            myJobs.addAll(jobs);
                        } finally {
                            myReadWriteLock.writeLock().unlock();
                            notifyEventListeners(Event.Jobs);
                        }
                        break;
                    case manageJob:
                        CaptureJobType job = myService.manageJob(myJob.getResourceID(), myJobCommand, myPriority);
                        myReadWriteLock.writeLock().lock();
                        try {
                            int pos = -1;
                            for (int i = 0; i < myJobs.size(); i++) {
                                if (job.getResourceID().equals(myJobs.get(i).getResourceID())) {
                                    pos = i;
                                    break;
                                }
                            }
                            if (pos >= 0) {
                                myJobs.remove(pos);
                                myJobs.add(pos, job);
                            } else {
                                myJobs.add(job);
                            }
                        } finally {
                            myReadWriteLock.writeLock().unlock();
                            notifyEventListeners(Event.Jobs);
                        }
                        break;
                }

                setConnected(true);
            } catch (WebServiceException ex) {
                System.err.println(ex.toString());
                setConnected(false);
            } catch (FaultMsg ex) {
                System.out.println(ex.getFaultInfo().getCode() + " " + ex.getFaultInfo().getDescription() + " " + ex.getFaultInfo().getDetail());
            }
        }
    }

    private enum TaskType
    {
        getContainerFormats,
        getVideoFormats,
        getAudioFormats,
        getDestinations,
        queryJob,
        sendJob,
        manageJob
    }

    @WebService(
        serviceName = "CaptureMediaNotificationService",
        portName = "CaptureMediaNotificationPort",
        endpointInterface = "tv.fims.capturemedia.CaptureMediaNotification",
        targetNamespace = "http://capturemedia.fims.tv",
        wsdlLocation = "tv/fims/captureMedia.wsdl"
    )
    public class CaptureMediaNotificationPort implements CaptureMediaNotification
    {
        private void updateServiceJobs(JobType job)
        {
            myReadWriteLock.writeLock().lock();
            try {
                int pos = -1;
                for (int i = 0; i < myJobs.size(); i++) {
                    if (job.getResourceID().equals(myJobs.get(i).getResourceID())) {
                        pos = i;
                        break;
                    }
                }
                if (pos >= 0) {
                    myJobs.remove(pos);
                    myJobs.add(pos, job);
                }
            } finally {
                myReadWriteLock.writeLock().unlock();
                notifyEventListeners(Event.Jobs);
            }
        }

        @Override
        public void notifyCaptureResult(CaptureNotificationType in)
        {
            updateServiceJobs(in.getCaptureJob());
            notifyEventListeners(in.getCaptureJob(), null);
        }

        @Override
        public void notifyFault(CaptureFaultNotificationType fault)
        {
            updateServiceJobs(fault.getCaptureJob());
            notifyEventListeners(fault.getCaptureJob(), fault.getFault());
        }
    }
}
