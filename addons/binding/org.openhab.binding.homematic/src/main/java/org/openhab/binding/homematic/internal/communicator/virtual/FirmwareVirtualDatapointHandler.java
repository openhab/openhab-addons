/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_FIRMWARE;
    }

    @Override
    public void initialize(HmDevice device) {
        if (!device.isGatewayExtras()) {
            addDatapoint(device, 0, getName(), HmValueType.STRING, device.getFirmware(), true);
        }
    }

}
