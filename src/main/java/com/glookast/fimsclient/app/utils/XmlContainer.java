package com.glookast.fimsclient.app.utils;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import tv.fims.capturemedia.CaptureJobType;
import tv.fims.capturemedia.CaptureProfileType;
import tv.fims.transfermedia.TransferJobType;
import tv.fims.transfermedia.TransferProfileType;
import tv.fims.transformmedia.TransformJobType;
import tv.fims.transformmedia.TransformProfileType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "fims-client")
@XmlType(propOrder = {"captureJob", "captureProfile", "transferJob", "transferProfile", "transformJob", "transformProfile" })
public class XmlContainer
{
    private List<CaptureJobType> captureJob;
    private List<CaptureProfileType> captureProfile;
    private List<TransferJobType> transferJob;
    private List<TransferProfileType> transferProfile;
    private List<TransformJobType> transformJob;
    private List<TransformProfileType> transformProfile;

    public List<CaptureJobType> getCaptureJob()
    {
        if (captureJob == null) {
            captureJob = new ArrayList<>();
        }
        return captureJob;
    }

    public List<CaptureProfileType> getCaptureProfile()
    {
        if (captureProfile == null) {
            captureProfile = new ArrayList<>();
        }
        return captureProfile;
    }

    public List<TransferJobType> getTransferJob()
    {
        if (transferJob == null) {
            transferJob = new ArrayList<>();
        }
        return transferJob;
    }

    public List<TransferProfileType> getTransferProfile()
    {
        if (transferProfile == null) {
            transferProfile = new ArrayList<>();
        }
        return transferProfile;
    }

    public List<TransformJobType> getTransformJob()
    {
        if (transformJob == null) {
            transformJob = new ArrayList<>();
        }
        return transformJob;
    }

    public List<TransformProfileType> getTransformProfile()
    {
        if (transformProfile == null) {
            transformProfile = new ArrayList<>();
        }
        return transformProfile;
    }
}
