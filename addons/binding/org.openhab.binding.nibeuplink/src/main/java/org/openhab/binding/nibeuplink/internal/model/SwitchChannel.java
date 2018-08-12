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

    private static final double DEFAULT_OFF = 0;
    private static final double DEFAULT_ON = 1;

    private final double offValue;
    private final double onValue;

    /**
     * constructor for channels with write access enabled. custom on/off mapping
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param offValue
     * @param onValue
     * @param writeApiUrl
     */
    SwitchChannel(String id, String name, ChannelGroup channelGroup, double offValue, double onValue,
            @Nullable String writeApiUrl) {
        super(id, name, channelGroup, writeApiUrl, ".*");
        this.offValue = offValue;
        this.onValue = onValue;
    }

    /**
     * constructor for channels without write access. custom on/off mapping
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param offValue
     * @param onValue
     */
    SwitchChannel(String id, String name, ChannelGroup channelGroup, double offValue, double onValue) {
        this(id, name, channelGroup, offValue, onValue, null);
    }

    /**
     * constructor for channels with write access enabled
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param writeApiUrl
     */
    SwitchChannel(String id, String name, ChannelGroup channelGroup, @Nullable String writeApiUrl) {
        this(id, name, channelGroup, DEFAULT_OFF, DEFAULT_ON, writeApiUrl);
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
        if (value == offValue) {
            return OnOffType.OFF;
        } else {
            return OnOffType.ON;
        }
    }

    public String mapValue(OnOffType value) {
        if (value.equals(OnOffType.OFF)) {
            return String.valueOf(offValue);
        } else {
            return String.valueOf(onValue);
        }
    }

}