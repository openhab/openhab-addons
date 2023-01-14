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
