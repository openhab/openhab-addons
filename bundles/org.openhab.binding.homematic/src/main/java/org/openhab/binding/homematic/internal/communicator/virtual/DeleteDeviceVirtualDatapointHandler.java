/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual boolean datapoint which locks the device so it can not be accidentally removed from the gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DeleteDeviceVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private final Logger logger = LoggerFactory.getLogger(DeleteDeviceVirtualDatapointHandler.class);

    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE;
    }

    @Override
    public void initialize(HmDevice device) {
        if (!device.isGatewayExtras() && device.getHmInterface() != HmInterface.CUXD) {
            addDatapoint(device, 0, getName(), HmValueType.BOOL, Boolean.FALSE, false);
        }
    }

    @Override
    public boolean canHandleCommand(HmDatapoint dp, Object value) {
        return getName().equals(dp.getName());
    }

    @Override
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        dp.setValue(value);
        if (MiscUtils.isTrueValue(dp.getValue())) {
            try {
                HmDatapoint deleteMode = dp.getChannel().getDatapoint(
                        HmDatapointInfo.createValuesInfo(dp.getChannel(), VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE));
                HmDevice device = dp.getChannel().getDevice();
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
                            device.getAddress(), gateway.getId());
                } else {
                    gateway.getRpcClient(device.getHmInterface()).deleteDevice(device, flag);
                }
            } finally {
                gateway.disableDatapoint(dp, AbstractHomematicGateway.DEFAULT_DISABLE_DELAY);
            }
        }
    }
}
