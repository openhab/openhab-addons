/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all virtual datapoints with common methods.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class AbstractVirtualDatapointHandler implements VirtualDatapointHandler {
    private final Logger logger = LoggerFactory.getLogger(AbstractVirtualDatapointHandler.class);

    @Override
    public boolean canHandleCommand(HmDatapoint dp, Object value) {
        return false;
    }

    @Override
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
    }

    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return false;
    }

    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) {
    }

    @Override
    public HmDatapoint getVirtualDatapoint(HmChannel channel) {
        return channel.getDatapoint(HmParamsetType.VALUES, getName());
    }

    /**
     * Creates a new datapoint with the given parameters and adds it to the channel.
     */
    protected HmDatapoint addDatapoint(HmDevice device, Integer channelNumber, String datapointName,
            HmValueType valueType, Object value, boolean readOnly) {
        HmChannel channel = device.getChannel(channelNumber);
        HmDatapoint dp = new HmDatapoint(datapointName, datapointName, valueType, value, readOnly,
                HmParamsetType.VALUES);
        return addDatapoint(channel, dp);
    }

    /**
     * Adds a new datapoint to the channel.
     */
    protected HmDatapoint addDatapoint(HmChannel channel, HmDatapoint dp) {
        logger.trace("Adding virtual datapoint '{}' to device '{}' ({}) and channel {}", dp.getName(),
                channel.getDevice().getAddress(), channel.getDevice().getType(), channel.getNumber());
        dp.setVirtual(true);
        dp.setReadable(true);
        channel.addDatapoint(dp);
        return dp;
    }

}
