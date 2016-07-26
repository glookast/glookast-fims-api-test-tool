package com.glookast.fimsclient.services.soap;

import com.glookast.api.fims.Capabilities;
import com.glookast.api.fims.CapabilitiesFilter;
import com.glookast.api.fims.CapabilitiesService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.BindingProvider;
import tv.fims.base.AudioFormatType;
import tv.fims.base.ContainerFormatType;
import tv.fims.base.JobCommandType;
import tv.fims.base.JobInfoSelectionType;
import tv.fims.base.JobType;
import tv.fims.base.ListFilterType;
import tv.fims.base.ManageJobRequestType;
import tv.fims.base.ManageJobResponseType;
import tv.fims.base.ManageQueueRequestType;
import tv.fims.base.ManageQueueResponseType;
import tv.fims.base.PriorityType;
import tv.fims.base.QueryJobRequestByFilterType;
import tv.fims.base.QueryJobRequestByIDType;
import tv.fims.base.QueryJobResponseType;
import tv.fims.base.QueueCommandType;
import tv.fims.base.QueueType;
import tv.fims.base.TransferAtomType;
import tv.fims.base.VideoFormatType;
import tv.fims.capturemedia.CaptureFaultMsg;
import tv.fims.capturemedia.CaptureJobType;
import tv.fims.capturemedia.CaptureMedia;
import tv.fims.capturemedia.CaptureMediaService;
import tv.fims.capturemedia.CaptureRequestType;
import tv.fims.capturemedia.CaptureResponseType;
import tv.fims.capturemedia.FaultMsg;
import tv.fims.capturemedia.MediaServiceStatus;

public class CaptureMediaServiceSOAP
{
    private final CaptureMediaService myCaptureMediaService;
    private final CaptureMedia myCaptureMediaPort;
    private final MediaServiceStatus myCaptureMediaStatusPort;

    private final CapabilitiesService myCapabilitiesService;
    private final Capabilities myCapabilitiesPort;

    public CaptureMediaServiceSOAP(URL wsdlLocation)
    {
        myCaptureMediaService = new CaptureMediaService();
        myCaptureMediaPort = myCaptureMediaService.getCaptureMediaPort();
        myCaptureMediaStatusPort = myCaptureMediaService.getCaptureMediaStatusPort();
        ((BindingProvider) myCaptureMediaPort).getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        ((BindingProvider) myCaptureMediaPort).getRequestContext().put("com.sun.xml.ws.connect.timeout", 10000);
        ((BindingProvider) myCaptureMediaPort).getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 10000);
        ((BindingProvider) myCaptureMediaPort).getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 10000);
        ((BindingProvider) myCaptureMediaPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlLocation.toString() + "/CaptureMediaPort");
        ((BindingProvider) myCaptureMediaStatusPort).getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        ((BindingProvider) myCaptureMediaStatusPort).getRequestContext().put("com.sun.xml.ws.connect.timeout", 10000);
        ((BindingProvider) myCaptureMediaStatusPort).getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 10000);
        ((BindingProvider) myCaptureMediaStatusPort).getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 10000);
        ((BindingProvider) myCaptureMediaStatusPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlLocation.toString() + "/CaptureMediaStatusPort");

        myCapabilitiesService = new CapabilitiesService();
        myCapabilitiesPort = myCapabilitiesService.getCapabilitiesPort();
        ((BindingProvider) myCapabilitiesPort).getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        ((BindingProvider) myCapabilitiesPort).getRequestContext().put("com.sun.xml.ws.connect.timeout", 10000);
        ((BindingProvider) myCapabilitiesPort).getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 10000);
        ((BindingProvider) myCapabilitiesPort).getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 10000);
        ((BindingProvider) myCapabilitiesPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlLocation.toString() + "/CaptureMediaCapabilitiesPort");

    }

    public CaptureJobType sendJob(CaptureJobType job) throws CaptureFaultMsg
    {
        CaptureRequestType request = new CaptureRequestType();
        request.setCaptureJob(job);
        request.setVersion("1_2_0");

        CaptureResponseType response = myCaptureMediaPort.capture(request);

        return response.getCaptureJob();
    }

    public CaptureJobType manageJob(String jobId, JobCommandType jobCommand) throws FaultMsg
    {
        return manageJob(jobId, jobCommand, null);
    }

    public CaptureJobType manageJob(String jobId, JobCommandType jobCommand, PriorityType priority) throws FaultMsg
    {
        ManageJobRequestType request = new ManageJobRequestType();
        request.setVersion("1_2_0");
        request.setJobID(jobId);
        request.setJobCommand(jobCommand);
        if (jobCommand == JobCommandType.MODIFY_PRIORITY) {
            request.setPriority(priority);
        }
        ManageJobResponseType response = myCaptureMediaStatusPort.manageJob(request);
        return (CaptureJobType) response.getJob();
    }

    public QueueType manageQueue(String queueId, QueueCommandType queueCommand) throws FaultMsg
    {
        ManageQueueRequestType request = new ManageQueueRequestType();
        request.setVersion("1_2_0");
        request.setQueueID(queueId);
        request.setQueueCommand(queueCommand);
        ManageQueueResponseType response = myCaptureMediaStatusPort.manageQueue(request);
        return response.getQueue();
    }

    public List<CaptureJobType> queryJob(ListFilterType filter) throws FaultMsg
    {
        List<CaptureJobType> list = new ArrayList<>();

        QueryJobRequestByFilterType request = new QueryJobRequestByFilterType();
        request.setVersion("1_2_0");
        request.setJobInfoSelection(JobInfoSelectionType.ALL);
        request.setListFilter(filter);

        QueryJobResponseType response = myCaptureMediaStatusPort.queryJob(request);

        if (response.getJobs() != null) {
            for (JobType job : response.getJobs().getJob()) {
                if (job instanceof CaptureJobType) {
                    list.add((CaptureJobType) job);
                }
            }
        }
        return list;
    }

    public List<CaptureJobType> queryJob(List<String> jobIds) throws FaultMsg
    {
        List<CaptureJobType> list = new ArrayList<>();

        QueryJobRequestByIDType request = new QueryJobRequestByIDType();
        request.setVersion("1_2_0");
        request.setJobInfoSelection(JobInfoSelectionType.ALL);
        request.getJobID().addAll(jobIds);
        QueryJobResponseType response = myCaptureMediaStatusPort.queryJob(request);

        if (response.getJobs() != null) {
            for (JobType job : response.getJobs().getJob()) {
                if (job instanceof CaptureJobType) {
                    list.add((CaptureJobType) job);
                }
            }
        }
        return list;
    }

    public List<ContainerFormatType> getContainerFormats()
    {
        return myCapabilitiesPort.getContainerFormats(CapabilitiesFilter.ALL, false);
    }

    public List<VideoFormatType> getVideoFormats()
    {
        return myCapabilitiesPort.getVideoFormats(CapabilitiesFilter.ALL, false);
    }

    public List<AudioFormatType> getAudioFormats()
    {
        return myCapabilitiesPort.getAudioFormats(CapabilitiesFilter.ALL, false);
    }

    public List<TransferAtomType> getDestinations()
    {
        return myCapabilitiesPort.getDestinations(CapabilitiesFilter.ALL);
    }
}
