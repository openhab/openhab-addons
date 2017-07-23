/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_RELOAD_RSSI;

import java.io.IOException;

import org.openhab.binding.homematic.internal.communicator.AbstractHomematicGateway;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual Switch datapoint which loads rssi values for all device from the gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ReloadRssiVirtualDatapointHandler extends AbstractVirtualDatapointHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_RELOAD_RSSI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(HmDevice device) {
        if (device.isGatewayExtras()) {
            addDatapoint(device, 0, getName(), HmValueType.BOOL, Boolean.FALSE, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandleCommand(HmDatapoint dp, Object value) {
        return getName().equals(dp.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        dp.setValue(value);
        if (MiscUtils.isTrueValue(dp.getValue())) {
            try {
                gateway.loadRssiValues();
            } finally {
                gateway.disableDatapoint(dp, AbstractHomematicGateway.DEFAULT_DISABLE_DELAY);
            }
        }
    }

}
