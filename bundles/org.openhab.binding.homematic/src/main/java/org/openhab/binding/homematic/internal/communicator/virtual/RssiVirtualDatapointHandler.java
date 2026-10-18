/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual datapoint that unifies the device and peer rssi datapoints.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class RssiVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_RSSI;
    }

    @Override
    public void initialize(HmDevice device) {
        if (isWirelessDevice(device)) {
            HmChannel channel = device.getChannel(0);
            if (channel != null) {
                HmDatapoint dp = addDatapoint(channel, getName(), HmValueType.INTEGER, getRssiValue(channel), true);
                dp.setUnit("dBm");
                dp.setMinValue(Integer.MIN_VALUE);
                dp.setMaxValue(Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return isWirelessDevice(dp.getChannel().getDevice())
                && (DATAPOINT_NAME_RSSI_DEVICE.equals(dp.getName()) || DATAPOINT_NAME_RSSI_PEER.equals(dp.getName()));
    }

    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) {
        HmChannel channel = dp.getChannel();
        Object value = getRssiValue(channel);
        HmDatapoint vdpRssi = getVirtualDatapoint(channel);
        if (vdpRssi != null) {
            vdpRssi.setValue(value);
        }
    }

    /**
     * Returns either the device or the peer rssi value.
     */
    protected @Nullable Integer getRssiValue(HmChannel channel) {
        HmDatapoint dpRssiDevice = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_DEVICE);
        if (dpRssiDevice != null) {
            Integer deviceValue = getDatapointValue(dpRssiDevice);
            if (deviceValue != null) {
                return deviceValue;
            }
        }

        HmDatapoint dpRssiPeer = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_PEER);
        if (dpRssiPeer != null) {
            Integer peerValue = getDatapointValue(dpRssiPeer);
            if (peerValue != null) {
                return peerValue;
            }
        }
        return null;
    }

    private @Nullable Integer getDatapointValue(HmDatapoint dp) {
        if (dp.getValue() instanceof Integer value && value != 0) {
            return value;
        }

        return null;
    }

    protected boolean isWirelessDevice(HmDevice device) {
        HmChannel channel = device.getChannel(0);
        return channel != null && channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_DEVICE) != null;
    }
}
