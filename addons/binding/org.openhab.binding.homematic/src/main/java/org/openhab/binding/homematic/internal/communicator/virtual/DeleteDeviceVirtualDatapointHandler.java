/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.communicator.virtual.DeleteDeviceModeVirtualDatapointHandler.*;
import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;

import org.openhab.binding.homematic.internal.communicator.AbstractHomematicGateway;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual boolean datapoint which locks the device so it can not be accidentally removed from the gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DeleteDeviceVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeleteDeviceVirtualDatapointHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(HmDevice device) {
        if (!device.isGatewayExtras()) {
            addDatapoint(device, 0, VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE, HmValueType.BOOL, Boolean.FALSE, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(HmDatapoint dp, Object value) {
        return VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE.equals(dp.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        dp.setValue(value);
        if (MiscUtils.isTrueValue(dp.getValue())) {
            try {
                HmDatapoint deleteMode = dp.getChannel().getDatapoint(
                        HmDatapointInfo.createValuesInfo(dp.getChannel(), VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE));

                int flag = -1;
                switch (deleteMode.getOptionValue()) {
                    case MODE_RESET:
                        flag = 1;
                        break;
                    case MODE_FORCE:
                        flag = 2;
                        break;
                    case MODE_DEFER:
                        flag = 4;
                }
                if (flag == -1) {
                    logger.info("Can't delete device '{}' from gateway '{}', DELETE_MODE is LOCKED",
                            dp.getChannel().getDevice().getAddress(), gateway.getId());
                } else {
                    gateway.getRpcClient().deleteDevice(dp.getChannel().getDevice(), flag);
                }
            } finally {
                gateway.disableDatapoint(dp, AbstractHomematicGateway.DEFAULT_DISABLE_DELAY);
            }
        }
    }
}
