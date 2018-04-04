/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

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
public class RssiVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_RSSI;
    }

    @Override
    public void initialize(HmDevice device) {
        if (isWirelessDevice(device)) {
            HmDatapoint dp = addDatapoint(device, 0, getName(), HmValueType.INTEGER, getRssiValue(device.getChannel(0)),
                    true);
            dp.setUnit("dBm");
            dp.setMinValue(Integer.MIN_VALUE);
            dp.setMaxValue(Integer.MAX_VALUE);
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
        vdpRssi.setValue(value);
    }

    /**
     * Returns either the device or the peer rssi value.
     */
    protected Integer getRssiValue(HmChannel channel) {
        HmDatapoint dpRssiDevice = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_DEVICE);
        HmDatapoint dpRssiPeer = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_PEER);

        Integer deviceValue = getDatapointValue(dpRssiDevice);
        Integer peerValue = getDatapointValue(dpRssiPeer);

        if ((deviceValue == null || deviceValue == 0) && peerValue != null) {
            return peerValue;
        }
        return deviceValue;
    }

    private Integer getDatapointValue(HmDatapoint dp) {
        if (dp == null || dp.getValue() == null || (Integer) dp.getValue() == 0) {
            return null;
        }

        return (Integer) dp.getValue();
    }

    protected boolean isWirelessDevice(HmDevice device) {
        return device.getChannel(0).getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_DEVICE) != null;
    }
}
