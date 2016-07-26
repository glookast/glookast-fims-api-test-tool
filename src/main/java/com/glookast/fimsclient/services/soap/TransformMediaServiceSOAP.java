package com.glookast.fimsclient.services.soap;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.BindingProvider;
import tv.fims.base.JobCommandType;
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
import tv.fims.transformmedia.FaultMsg;
import tv.fims.transformmedia.MediaServiceStatus;
import tv.fims.transformmedia.TransformFaultMsg;
import tv.fims.transformmedia.TransformJobType;
import tv.fims.transformmedia.TransformMedia;
import tv.fims.transformmedia.TransformMediaService;
import tv.fims.transformmedia.TransformRequestType;
import tv.fims.transformmedia.TransformResponseType;

public class TransformMediaServiceSOAP
{
    private TransformMediaService myTransformMediaService;
    private TransformMedia myTransformMediaPort;
    private MediaServiceStatus myTransformMediaStatusPort;

    public TransformMediaServiceSOAP(URL wsdlLocation)
    {
        myTransformMediaService = new TransformMediaService();
        myTransformMediaPort = myTransformMediaService.getTransformMediaPort();
        myTransformMediaStatusPort = myTransformMediaService.getTransformMediaStatusPort();
        ((BindingProvider) myTransformMediaPort).getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        ((BindingProvider) myTransformMediaPort).getRequestContext().put("com.sun.xml.ws.connect.timeout", 10000);
        ((BindingProvider) myTransformMediaPort).getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 10000);
        ((BindingProvider) myTransformMediaPort).getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 10000);
        ((BindingProvider) myTransformMediaPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlLocation.toString() + "/TransformMediaPort");
        ((BindingProvider) myTransformMediaStatusPort).getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        ((BindingProvider) myTransformMediaStatusPort).getRequestContext().put("com.sun.xml.ws.connect.timeout", 10000);
        ((BindingProvider) myTransformMediaStatusPort).getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 10000);
        ((BindingProvider) myTransformMediaStatusPort).getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 10000);
        ((BindingProvider) myTransformMediaStatusPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlLocation.toString() + "/TransformMediaStatusPort");
    }

    public TransformJobType sendJob(TransformJobType job) throws TransformFaultMsg
    {
        TransformRequestType request = new TransformRequestType();
        request.setTransformJob(job);
        request.setVersion("1_2_0");

        TransformResponseType response = myTransformMediaPort.transform(request);

        return response.getTransformJob();
    }

    public TransformJobType manageJob(String jobId, JobCommandType jobCommand) throws FaultMsg
    {
        return manageJob(jobId, jobCommand, null);
    }

    public TransformJobType manageJob(String jobId, JobCommandType jobCommand, PriorityType priority) throws FaultMsg
    {
        ManageJobRequestType request = new ManageJobRequestType();
        request.setJobID(jobId);
        request.setJobCommand(jobCommand);
        if (jobCommand == JobCommandType.MODIFY_PRIORITY) {
            request.setPriority(priority);
        }
        ManageJobResponseType response = myTransformMediaStatusPort.manageJob(request);
        return (TransformJobType) response.getJob();
    }

    public QueueType manageQueue(String queueId, QueueCommandType queueCommand) throws FaultMsg
    {
        ManageQueueRequestType request = new ManageQueueRequestType();
        request.setQueueID(queueId);
        request.setQueueCommand(queueCommand);
        ManageQueueResponseType response = myTransformMediaStatusPort.manageQueue(request);
        return response.getQueue();
    }

    public List<TransformJobType> queryJob(ListFilterType filter) throws FaultMsg
    {
        List<TransformJobType> list = new ArrayList<>();

        QueryJobRequestByFilterType request = new QueryJobRequestByFilterType();
        request.setListFilter(filter);

        QueryJobResponseType response = myTransformMediaStatusPort.queryJob(request);

        for (JobType job : response.getJobs().getJob()) {
            if (job instanceof TransformJobType) {
                list.add((TransformJobType) job);
            }
        }
        return list;
    }

    public List<TransformJobType> queryJob(List<String> jobIds) throws FaultMsg
    {
        List<TransformJobType> list = new ArrayList<>();

        QueryJobRequestByIDType request = new QueryJobRequestByIDType();
        request.getJobID().addAll(jobIds);
        QueryJobResponseType response = myTransformMediaStatusPort.queryJob(request);

        for (JobType job : response.getJobs().getJob()) {
            if (job instanceof TransformJobType) {
                list.add((TransformJobType) job);
            }
        }
        return list;
    }
}
