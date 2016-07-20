/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_FIRMWARE;

import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual String datapoint which adds the firmware version to the device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class FirmwareVirtualDatapointHandler extends AbstractVirtualDatapointHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(HmDevice device) {
        if (!device.isGatewayExtras()) {
            addDatapoint(device, 0, VIRTUAL_DATAPOINT_NAME_FIRMWARE, HmValueType.STRING, device.getFirmware(), true);
        }
    }

}
