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
import tv.fims.transfermedia.FaultMsg;
import tv.fims.transfermedia.MediaServiceStatus;
import tv.fims.transfermedia.TransferFaultMsg;
import tv.fims.transfermedia.TransferJobType;
import tv.fims.transfermedia.TransferMedia;
import tv.fims.transfermedia.TransferMediaService;
import tv.fims.transfermedia.TransferRequestType;
import tv.fims.transfermedia.TransferResponseType;

public class TransferMediaServiceSOAP
{
    private TransferMediaService myTransferMediaService;
    private TransferMedia myTransferMediaPort;
    private MediaServiceStatus myTransferMediaStatusPort;

    public TransferMediaServiceSOAP(URL wsdlLocation)
    {
        myTransferMediaService = new TransferMediaService();
        myTransferMediaPort = myTransferMediaService.getTransferMediaPort();
        myTransferMediaStatusPort = myTransferMediaService.getTransferMediaStatusPort();
        ((BindingProvider) myTransferMediaPort).getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        ((BindingProvider) myTransferMediaPort).getRequestContext().put("com.sun.xml.ws.connect.timeout", 10000);
        ((BindingProvider) myTransferMediaPort).getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 10000);
        ((BindingProvider) myTransferMediaPort).getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 10000);
        ((BindingProvider) myTransferMediaPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlLocation.toString() + "/TransferMediaPort");
        ((BindingProvider) myTransferMediaStatusPort).getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        ((BindingProvider) myTransferMediaStatusPort).getRequestContext().put("com.sun.xml.ws.connect.timeout", 10000);
        ((BindingProvider) myTransferMediaStatusPort).getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 10000);
        ((BindingProvider) myTransferMediaStatusPort).getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 10000);
        ((BindingProvider) myTransferMediaStatusPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlLocation.toString() + "/TransferMediaStatusPort");
    }

    public TransferJobType sendJob(TransferJobType job) throws TransferFaultMsg
    {
        TransferRequestType request = new TransferRequestType();
        request.setTransferJob(job);
        request.setVersion("1_2_0");

        TransferResponseType response = myTransferMediaPort.transfer(request);

        return response.getTransferJob();
    }

    public TransferJobType manageJob(String jobId, JobCommandType jobCommand) throws FaultMsg
    {
        return manageJob(jobId, jobCommand, null);
    }

    public TransferJobType manageJob(String jobId, JobCommandType jobCommand, PriorityType priority) throws FaultMsg
    {
        ManageJobRequestType request = new ManageJobRequestType();
        request.setJobID(jobId);
        request.setJobCommand(jobCommand);
        if (jobCommand == JobCommandType.MODIFY_PRIORITY) {
            request.setPriority(priority);
        }
        ManageJobResponseType response = myTransferMediaStatusPort.manageJob(request);
        return (TransferJobType) response.getJob();
    }

    public QueueType manageQueue(String queueId, QueueCommandType queueCommand) throws FaultMsg
    {
        ManageQueueRequestType request = new ManageQueueRequestType();
        request.setQueueID(queueId);
        request.setQueueCommand(queueCommand);
        ManageQueueResponseType response = myTransferMediaStatusPort.manageQueue(request);
        return response.getQueue();
    }

    public List<TransferJobType> queryJob(ListFilterType filter) throws FaultMsg
    {
        List<TransferJobType> list = new ArrayList<>();

        QueryJobRequestByFilterType request = new QueryJobRequestByFilterType();
        request.setListFilter(filter);

        QueryJobResponseType response = myTransferMediaStatusPort.queryJob(request);

        for (JobType job : response.getJobs().getJob()) {
            if (job instanceof TransferJobType) {
                list.add((TransferJobType) job);
            }
        }
        return list;
    }

    public List<TransferJobType> queryJob(List<String> jobIds) throws FaultMsg
    {
        List<TransferJobType> list = new ArrayList<>();

        QueryJobRequestByIDType request = new QueryJobRequestByIDType();
        request.getJobID().addAll(jobIds);
        QueryJobResponseType response = myTransferMediaStatusPort.queryJob(request);

        for (JobType job : response.getJobs().getJob()) {
            if (job instanceof TransferJobType) {
                list.add((TransferJobType) job);
            }
        }
        return list;
    }
}
