/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
     * @param id identifier of the channel
     * @param name human readable name
     * @param channelGroup group of the channel
     * @param offValue value which represents OFF state
     * @param onValue value which represents ON state
     * @param writeApiUrl API URL for channel updates
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
     * @param id identifier of the channel
     * @param name human readable name
     * @param channelGroup group of the channel
     * @param offValue value which represents OFF state
     * @param onValue value which represents ON state
     */
    SwitchChannel(String id, String name, ChannelGroup channelGroup, double offValue, double onValue) {
        this(id, name, channelGroup, offValue, onValue, null);
    }

    /**
     * constructor for channels with write access enabled
     *
     * @param id identifier of the channel
     * @param name human readable name
     * @param channelGroup group of the channel
     * @param writeApiUrl API URL for channel updates
     */
    SwitchChannel(String id, String name, ChannelGroup channelGroup, @Nullable String writeApiUrl) {
        this(id, name, channelGroup, DEFAULT_OFF, DEFAULT_ON, writeApiUrl);
    }

    /**
     * constructor for channels without write access
     *
     * @param id identifier of the channel
     * @param name human readable name
     * @param channelGroup group of the channel
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
