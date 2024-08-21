/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.converter;

import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Holds device specific infos for state invertion.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class StateInvertInfo {
    private String deviceType;
    private int minChannel;
    private int maxChannel;

    /**
     * Creates a StateInvertInfo with the specified deviceType.
     */
    public StateInvertInfo(String deviceType) {
        this(deviceType, -1, -1);
    }

    /**
     * Creates a StateInvertInfo with the specified deviceType and a channel range.
     */
    public StateInvertInfo(String deviceType, int minChannel, int maxChannel) {
        this.deviceType = deviceType;
        this.minChannel = minChannel;
        this.maxChannel = maxChannel;
    }

    /**
     * Validates if the state of a datapoint must be inverted.
     */
    public boolean isToInvert(HmDatapoint dp) {
        String dpDeviceType = dp.getChannel().getDevice().getType();
        if (minChannel != -1) {
            int dpChannelNumber = dp.getChannel().getNumber();
            return dpDeviceType.startsWith(deviceType) && dpChannelNumber >= minChannel
                    && dpChannelNumber <= maxChannel;
        } else {
            return dpDeviceType.startsWith(deviceType);
        }
    }
}
