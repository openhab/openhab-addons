/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_RELOAD_ALL_FROM_GATEWAY;

import java.io.IOException;

import org.openhab.binding.homematic.internal.communicator.AbstractHomematicGateway;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual Switch datapoint which reloads all device values from the gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ReloadAllFromGatewayVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_RELOAD_ALL_FROM_GATEWAY;
    }

    @Override
    public void initialize(HmDevice device) {
        if (device.isGatewayExtras()) {
            addDatapoint(device, HmChannel.CHANNEL_NUMBER_EXTRAS, getName(), HmValueType.BOOL, Boolean.FALSE, false);
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
                gateway.getGatewayAdapter().reloadAllDeviceValues();
            } finally {
                gateway.disableDatapoint(dp, AbstractHomematicGateway.DEFAULT_DISABLE_DELAY);
            }
        }
    }
}
