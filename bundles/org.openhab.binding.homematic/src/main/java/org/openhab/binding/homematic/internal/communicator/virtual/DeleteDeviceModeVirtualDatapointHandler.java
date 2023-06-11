/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE;

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual enum datapoint which holds the delete flag for deleting a device with the DELETE_DEVICE virtual datapoint.
 * Falls back to LOCKED after 30 seconds to prevent accidental deletion.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DeleteDeviceModeVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    protected static final String MODE_LOCKED = "LOCKED";
    protected static final String MODE_RESET = "RESET";
    protected static final String MODE_FORCE = "FORCE";
    protected static final String MODE_DEFER = "DEFER";
    private static final int DELETE_MODE_DURATION = 30;

    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE;
    }

    @Override
    public void initialize(HmDevice device) {
        if (!device.isGatewayExtras() && !(device.getHmInterface() == HmInterface.CUXD)) {
            HmDatapoint dp = addDatapoint(device, 0, getName(), HmValueType.ENUM, 0, false);
            dp.setOptions(new String[] { MODE_LOCKED, MODE_RESET, MODE_FORCE, MODE_DEFER });
            dp.setMinValue(0);
            dp.setMaxValue(dp.getOptions().length - 1);
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
        if (!MODE_LOCKED.equals(dp.getOptionValue())) {
            gateway.disableDatapoint(dp, DELETE_MODE_DURATION);
        }
    }
}
