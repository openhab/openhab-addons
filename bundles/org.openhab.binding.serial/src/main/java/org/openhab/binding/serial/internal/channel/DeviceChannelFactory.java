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
package org.openhab.binding.serial.internal.channel;

import static org.openhab.binding.serial.internal.SerialBindingConstants.DEVICE_DIMMER_CHANNEL;
import static org.openhab.binding.serial.internal.SerialBindingConstants.DEVICE_NUMBER_CHANNEL;
import static org.openhab.binding.serial.internal.SerialBindingConstants.DEVICE_ROLLERSHUTTER_CHANNEL;
import static org.openhab.binding.serial.internal.SerialBindingConstants.DEVICE_STRING_CHANNEL;
import static org.openhab.binding.serial.internal.SerialBindingConstants.DEVICE_SWITCH_CHANNEL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A factory to create {@link DeviceChannel} objects
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class DeviceChannelFactory {

    /**
     * Create a {@link DeviceChannel} for the channel type
     * 
     * @param valueTransformationProvider the transformation provider
     * @param channelConfig the channel configuration
     * @param channelTypeID the channel type id
     * @return the DeviceChannel or null if the channel type is not supported.
     */
    public static @Nullable DeviceChannel createDeviceChannel(final ChannelConfig channelConfig,
            final String channelTypeID) {
        DeviceChannel deviceChannel;

        switch (channelTypeID) {
            case DEVICE_STRING_CHANNEL:
                deviceChannel = new StringChannel(channelConfig);
                break;
            case DEVICE_NUMBER_CHANNEL:
                deviceChannel = new NumberChannel(channelConfig);
                break;
            case DEVICE_DIMMER_CHANNEL:
                deviceChannel = new DimmerChannel(channelConfig);
                break;
            case DEVICE_SWITCH_CHANNEL:
                deviceChannel = new SwitchChannel(channelConfig);
                break;
            case DEVICE_ROLLERSHUTTER_CHANNEL:
                deviceChannel = new RollershutterChannel(channelConfig);
                break;
            default:
                deviceChannel = null;
                break;
        }

        return deviceChannel;
    }
}
