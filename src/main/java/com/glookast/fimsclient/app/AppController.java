package com.glookast.fimsclient.app;

import java.util.List;
import tv.fims.base.AudioFormatType;
import tv.fims.base.BMObjectType;
import tv.fims.base.ContainerFormatType;
import tv.fims.base.JobType;
import tv.fims.base.ProfileType;
import tv.fims.base.TransferAtomType;
import tv.fims.base.VideoFormatType;

public interface AppController
{
    List<Service> getServices(Service.Type serviceType);
    void setService(Service service);
    void delService(Service service);

    Service getActiveService(Service.Type serviceType);
    void setActiveService(Service service);

    List<JobType> getJobs(Service.Type serviceType);
    void setJob(JobType job);
    void delJob(JobType job);

    List<ProfileType> getProfiles(Service.Type serviceType);
    void setProfile(ProfileType profile);
    void delProfile(ProfileType profile);

    List<ContainerFormatType> getContainerFormats(Service.Type serviceType);
    List<VideoFormatType> getVideoFormats(Service.Type serviceType);
    List<AudioFormatType> getAudioFormats(Service.Type serviceType);
    List<TransferAtomType> getDestinations(Service.Type serviceType);

    List<BMObjectType> getBmObjects();

    void setBmObject(BMObjectType bmObject);
    void delBmObject(BMObjectType bmObject);

    void addListener(EventListener listener);
    void removeListener(EventListener listener);

    String getPropertiesFilename();
    void setPropertiesFilename(String filename);
    void loadProperties();
    void saveProperties();

    void loadApplicationData();

    public interface EventListener
    {
        public static enum Event
        {
            Services,
            ActiveServiceChanged,
            CaptureJobs,
            CaptureProfiles,
            TransferJobs,
            TransferProfiles,
            TransformJobs,
            TransformProfiles,
            BmObjects,
        }

        void onEvent(Event event);
    }
}
