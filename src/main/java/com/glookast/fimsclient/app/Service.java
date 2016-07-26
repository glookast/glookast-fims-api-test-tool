package com.glookast.fimsclient.app;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Future;
import tv.fims.base.AudioFormatType;
import tv.fims.base.ContainerFormatType;
import tv.fims.base.JobCommandType;
import tv.fims.base.JobType;
import tv.fims.base.ListFilterType;
import tv.fims.base.PriorityType;
import tv.fims.base.TransferAtomType;
import tv.fims.base.VideoFormatType;
import tv.fims.capturemedia.CaptureFaultType;
import tv.fims.capturemedia.CaptureJobType;

public interface Service
{
    String getName();
    Service.Type getType();
    Service.Method getMethod();
    URL getAddress();
    URL getCallbackAddress();

    boolean isConnected();

    List<ContainerFormatType> getContainerFormats();
    List<VideoFormatType> getVideoFormats();
    List<AudioFormatType> getAudioFormats();
    List<TransferAtomType> getDestinations();

    void queryJobs(ListFilterType filter);
    List<JobType> getJobs();

    Future<Boolean> sendJob(JobType job);
    void manageJob(JobType job, JobCommandType jobCommand);
    void manageJobPriority(JobType job, PriorityType priority);

    void stop();

    void addListener(EventListener listener);
    void removeListener(EventListener listener);

    public interface EventListener
    {
        public static enum Event
        {
            ConnectionStatus,
            ContainerFormats,
            VideoFormats,
            AudioFormats,
            Destinations,
            Jobs
        }

        void onEvent(Event event);

        void onResult(CaptureJobType job);

        void onFault(CaptureJobType job, CaptureFaultType fault);
    }

    public enum Type
    {
        Capture,
        Transfer,
        Transform
    }

    public enum Method
    {
        SOAP,
        REST_XML,
        REST_JSON
    }
}
