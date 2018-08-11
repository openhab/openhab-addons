package org.openhab.binding.nibeuplink.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * extension of Channel class to support SwitchType
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SwitchChannel extends Channel {

    /**
     * constructor for channels with write access enabled
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param writeApiUrl
     */
    SwitchChannel(String id, String name, ChannelGroup channelGroup, @Nullable String writeApiUrl) {
        super(id, name, channelGroup, writeApiUrl, ".*");
    }

    /**
     * constructor for channels without write access
     *
     * @param id
     * @param name
     * @param channelGroup
     */
    SwitchChannel(String id, String name, ChannelGroup channelGroup) {
        this(id, name, channelGroup, null);
    }

    public OnOffType mapValue(double value) {
        if (value == 0.0) {
            return OnOffType.OFF;
        } else {
            return OnOffType.ON;
        }
    }

    public String mapValue(OnOffType value) {
        if (value.equals(OnOffType.OFF)) {
            return "0";
        } else {
            return "1";
        }
    }

}