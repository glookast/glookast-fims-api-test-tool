package com.glookast.fimsclient.app.gui.components;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tv.fims.base.JobType;
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
import tv.fims.capturemedia.CaptureJobType;
import tv.fims.capturemedia.CaptureProfileType;
import tv.fims.transfermedia.TransferJobType;
import tv.fims.transfermedia.TransferProfileType;
import tv.fims.transformmedia.TransformJobType;
import tv.fims.transformmedia.TransformProfileType;

public abstract class JobsTableModel extends ScrollTableModel
{
    protected final String[] myColumnNames;
    protected final Map<String, Integer> myColumnWidths;

    protected abstract List<JobType> getJobs();

    public JobsTableModel(String[] columnNames)
    {
        myColumnNames = columnNames;

        myColumnWidths = new HashMap<>();
        myColumnWidths.put("ResourceID", 250);
        myColumnWidths.put("SourceID", 150);
        myColumnWidths.put("StartProcess", 135);
        myColumnWidths.put("StopProcess", 135);
        myColumnWidths.put("StartJob", 120);
        myColumnWidths.put("Priority", 70);
        myColumnWidths.put("Profiles", 200);
        myColumnWidths.put("Created", 120);
        myColumnWidths.put("Modified", 120);
        myColumnWidths.put("Started", 120);
        myColumnWidths.put("Completed", 120);
        myColumnWidths.put("Status", 80);
        myColumnWidths.put("Queue Position", 100);
    }

    @Override
    public int getColumnWidth(int column)
    {
        String columnName = getColumnName(column);
        if (myColumnWidths.containsKey(columnName)) {
            return myColumnWidths.get(columnName);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public String getColumnName(int column)
    {
        return myColumnNames[column];
    }

    @Override
    public int getColumnCount()
    {
        return myColumnNames.length;
    }

    @Override
    public int getRowCount()
    {
        return getJobs().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        JobType job = getJobs().get(rowIndex);

        switch (getColumnName(columnIndex)) {
            case "ResourceID":
                return job.getResourceID();
            case "SourceID":
                return ((CaptureJobType) job).getSourceID();
            case "StartProcess":
                StartProcessType startProcess = ((CaptureJobType) job).getStartProcess();
                if (startProcess instanceof StartProcessByNoWaitType) {
                    return "No Wait";
                } else if (startProcess instanceof StartProcessByTimeType) {
                    return calendarToDateTimeString(((StartProcessByTimeType) startProcess).getTime());
                } else if (startProcess instanceof StartProcessByTimeMarkType) {
                    return "Time Mark: " + ((StartProcessByTimeMarkType) startProcess).getTimeMark().getTimecode();
                } else if (startProcess instanceof StartProcessByServiceDefinedTimeType) {
                    return "Service Defined Time";
                }
            case "StopProcess":
                StopProcessType stopProcess = ((CaptureJobType) job).getStopProcess();
                if (stopProcess instanceof StopProcessByOpenEndType) {
                    return "Open End";
                } else if (stopProcess instanceof StopProcessByTimeType) {
                    return calendarToDateTimeString(((StopProcessByTimeType) stopProcess).getTime());
                } else if (stopProcess instanceof StopProcessByTimeMarkType) {
                    return "Time Mark: " + ((StopProcessByTimeMarkType) stopProcess).getTimeMark().getTimecode();
                } else if (stopProcess instanceof StopProcessByDurationType) {
                    return "Duration: " + ((StopProcessByDurationType) stopProcess).getDuration().getTimecode();
                } else if (stopProcess instanceof StopProcessByServiceDefinedTimeType) {
                    return "Service Defined Time";
                }
            case "StartJob":
                StartJobType startJob = job.getStartJob();
                if (startJob instanceof StartJobByNoWaitType) {
                    return "No Wait";
                } else if (startJob instanceof StartJobByTimeType) {
                    return calendarToDateTimeString(((StartJobByTimeType) startJob).getTime());
                } else if (startJob instanceof StartJobByLatestType) {
                    return "Latest";
                }
            case "Priority":
                return job.getPriority();
            case "Profiles":
                StringBuilder sb = new StringBuilder();
                if (job instanceof CaptureJobType) {
                    CaptureJobType.Profiles profiles = ((CaptureJobType) job).getProfiles();
                    if (profiles != null) {
                        for (CaptureProfileType profile : profiles.getCaptureProfile()) {
                            sb.append(profile.getName());
                            sb.append(", ");
                        }
                    }
                } else if (job instanceof TransferJobType) {
                    TransferJobType.Profiles profiles = ((TransferJobType) job).getProfiles();
                    if (profiles != null) {
                        for (TransferProfileType profile : profiles.getTransferProfile()) {
                            sb.append(profile.getName());
                            sb.append(", ");
                        }
                    }
                } else if (job instanceof TransformJobType) {
                    TransformJobType.Profiles profiles = ((TransformJobType) job).getProfiles();
                    if (profiles != null) {
                        for (TransformProfileType profile : profiles.getTransformProfile()) {
                            sb.append(profile.getName());
                            sb.append(", ");
                        }
                    }
                }
                if (sb.length() >= 2) {
                    sb.setLength(sb.length() - 2);
                }
                return sb.toString();
            case "Created":
                return calendarToDateTimeString(job.getResourceCreationDate());
            case "Modified":
                return calendarToDateTimeString(job.getResourceModifiedDate());
            case "Status":
                return job.getStatus();
            case "Queue Position":
                if (job.getCurrentQueuePosition() != null) {
                    return job.getCurrentQueuePosition();
                }
                return "";
            case "Started":
                return calendarToDateTimeString(job.getJobStartedTime());
            case "Completed":
                return calendarToDateTimeString(job.getJobCompletedTime());
        }
        throw new IndexOutOfBoundsException();
    }

    private String calendarToDateTimeString(Calendar calendar)
    {
        return (calendar == null) ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

}
