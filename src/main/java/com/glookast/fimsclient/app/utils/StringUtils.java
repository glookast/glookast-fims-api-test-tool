package com.glookast.fimsclient.app.utils;

import tv.fims.base.AudioFormatType;
import tv.fims.base.BMContentFormatType;
import tv.fims.base.BMContentType;
import tv.fims.base.BMContentsType;
import tv.fims.base.BMEssenceLocatorType;
import tv.fims.base.BMEssenceLocatorsType;
import tv.fims.base.BMObjectType;
import tv.fims.base.ContainerFormatType;
import tv.fims.base.DescriptionType;
import tv.fims.base.DescriptionsType;
import tv.fims.base.ListFileLocatorType;
import tv.fims.base.ScanningFormatType;
import tv.fims.base.SimpleFileLocatorType;
import tv.fims.base.TransferAtomType;
import tv.fims.base.VideoFormatType;
import tv.fims.description.BmContentDescriptionType;

public class StringUtils
{
    public static String toString(VideoFormatType videoFormat)
    {
        if (videoFormat == null) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();
        if (videoFormat.getVideoEncoding() != null) {
            sb.append(videoFormat.getVideoEncoding().getName());
            sb.append(" ");
        }
        if (videoFormat.getBitRate() != null) {
            sb.append(videoFormat.getBitRate().longValue() / 1000000);
            sb.append(" ");
        }
        if (videoFormat.getLines() != null) {
            sb.append(videoFormat.getLines());
        }
        if (videoFormat.getScanningFormat() != null) {
            sb.append(videoFormat.getScanningFormat() == ScanningFormatType.INTERLACED ? "i" : "p");
        }
        if (videoFormat.getFrameRate() != null) {
            sb.append(String.format("%.2f", videoFormat.getFrameRate().getNumerator().doubleValue() / videoFormat.getFrameRate().getDenominator().doubleValue()));
        }

        return sb.toString();
    }

    public static String toString(AudioFormatType audioFormat)
    {
        if (audioFormat == null) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();
        if (audioFormat.getAudioEncoding() != null) {
            sb.append(audioFormat.getAudioEncoding().getName());
            sb.append(" ");
        }
        if (audioFormat.getChannels() != null) {
            sb.append(audioFormat.getChannels());
            sb.append(" channels ");
        }
        if (audioFormat.getSampleSize() != null) {
            sb.append(audioFormat.getSampleSize());
            sb.append("bit ");
        }
        if (audioFormat.getSamplingRate() != null) {
            sb.append(audioFormat.getSamplingRate());
            sb.append("Hz");
        }

        return sb.toString();
    }

    public static String toString(ContainerFormatType containerFormat)
    {
        if (containerFormat == null || containerFormat.getContainerFormat() == null || containerFormat.getContainerFormat().getValue() == null) {
            return "None";
        }
        return containerFormat.getContainerFormat().getValue();
    }

    public static String toString(TransferAtomType transferAtom)
    {
        if (transferAtom == null) {
            return "Undefined";
        }

        return transferAtom.getDestination();
    }

    public static String toString(BMContentType bmContent)
    {
        if (bmContent == null) {
            return "Undefined";
        }

        DescriptionsType descriptions = bmContent.getDescriptions();

        if (descriptions != null && !descriptions.getDescription().isEmpty()) {
            for (DescriptionType description : descriptions.getDescription()) {
                BmContentDescriptionType contentDescription = description.getBmContentDescription();
                if (contentDescription != null && !contentDescription.getTitle().isEmpty()) {
                    return contentDescription.getTitle().get(0).getValue();
                }
            }
        }

        return "Content without title";
    }

    public static String toString(BMObjectType bmObject)
    {
        if (bmObject == null) {
            return "Undefined";
        }

        BMContentsType mbContents = bmObject.getBmContents();
        if (mbContents == null || mbContents.getBmContent().isEmpty()) {
            return "No Business Media Contents";
        }

        StringBuilder sb = new StringBuilder();
        for (BMContentType bmContent : mbContents.getBmContent()) {
            sb.append(StringUtils.toString(bmContent));
            sb.append(", ");
        }
        if (sb.length() >= 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    public static String toString(BMContentFormatType bmContentFormat)
    {
        if (bmContentFormat == null) {
            return "Undefined";
        }

        BMEssenceLocatorsType bmEssenceLocators = bmContentFormat.getBmEssenceLocators();

        if (bmEssenceLocators != null) {
            for (BMEssenceLocatorType bmEssenceLocator : bmEssenceLocators.getBmEssenceLocator()) {
                if (bmEssenceLocator instanceof SimpleFileLocatorType) {
                    return ((SimpleFileLocatorType) bmEssenceLocator).getFile();
                }
                if (bmEssenceLocator instanceof ListFileLocatorType) {
                    StringBuilder sb = new StringBuilder();
                    for (String file : ((ListFileLocatorType)bmEssenceLocator).getFile()) {
                        sb.append(file).append("; ");
                    }
                    return sb.toString();
                }
            }
        }

        return "";
    }

}
