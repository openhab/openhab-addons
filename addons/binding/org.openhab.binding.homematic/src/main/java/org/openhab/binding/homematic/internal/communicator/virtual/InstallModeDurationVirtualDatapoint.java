/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(HmDevice device) {
        if (device.isGatewayExtras()) {
            HmDatapoint dp = addDatapoint(device, 0, VIRTUAL_DATAPOINT_NAME_INSTALL_MODE_DURATION, HmValueType.INTEGER,
                    60, false);
            dp.setMinValue(10);
            dp.setMaxValue(300);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(HmDatapoint dp, Object value) {
        return VIRTUAL_DATAPOINT_NAME_INSTALL_MODE_DURATION.equals(dp.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        dp.setValue(value);
    }
}
