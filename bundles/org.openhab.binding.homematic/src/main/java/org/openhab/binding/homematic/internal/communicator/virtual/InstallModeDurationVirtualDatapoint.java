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
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_INSTALL_MODE_DURATION;

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual Integer datapoint to hold the duration for the install mode, default one minute.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class InstallModeDurationVirtualDatapoint extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_INSTALL_MODE_DURATION;
    }

    @Override
    public void initialize(HmDevice device) {
        if (device.isGatewayExtras()) {
            HmDatapoint dp = addDatapoint(device, 0, getName(), HmValueType.INTEGER, 60, false);
            dp.setMinValue(10);
            dp.setMaxValue(300);
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
    }
}
