/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_RSSI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(HmDevice device) {
        if (isWirelessDevice(device)) {
            addDatapoint(device, 0, getName(), HmValueType.INTEGER, getRssiValue(device.getChannel(0)), true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return isWirelessDevice(dp.getChannel().getDevice())
                && (DATAPOINT_NAME_RSSI_DEVICE.equals(dp.getName()) || DATAPOINT_NAME_RSSI_PEER.equals(dp.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) throws HomematicClientException {
        HmChannel channel = dp.getChannel();
        Object value = getRssiValue(channel);
        HmDatapoint vdpRssi = getVirtualDatapoint(channel);
        vdpRssi.setValue(value);
    }

    /**
     * Returns either the device or the peer rssi value.
     */
    private Object getRssiValue(HmChannel channel) {
        HmDatapoint dpRssiDevice = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_DEVICE);
        HmDatapoint dpRssiPeer = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_PEER);

        if (getDatapointValue(dpRssiDevice) == null && getDatapointValue(dpRssiPeer) != null) {
            return getDatapointValue(dpRssiPeer);
        }
        return getDatapointValue(dpRssiDevice);
    }

    private Integer getDatapointValue(HmDatapoint dp) {
        if (dp == null || dp.getValue() == null || (Integer) dp.getValue() == 0) {
            return null;
        }

        return (Integer) dp.getValue();
    }

    private boolean isWirelessDevice(HmDevice device) {
        return device.getChannel(0).getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_DEVICE) != null;
    }
}
