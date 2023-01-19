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

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_SIGNAL_STRENGTH;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual datapoint that represents signal strength of a device as a number with values 0, 1, 2, 3 or 4, 0 being
 * worst strength and 4 being best strength.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class SignalStrengthVirtualDatapointHandler extends RssiVirtualDatapointHandler {
    private static final int RSSI_START = 40;
    private static final int RSSI_STEP = 25;
    private static final int RSSI_UNITS = 4;

    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_SIGNAL_STRENGTH;
    }

    @Override
    public void initialize(HmDevice device) {
        if (isWirelessDevice(device)) {
            addDatapoint(device, 0, getName(), HmValueType.INTEGER, getRssiValue(device.getChannel(0)), true);
        }
    }

    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) {
        HmChannel channel = dp.getChannel();
        Integer value = getRssiValue(channel);

        if (value != null) {
            Integer strength = Math.max(Math.abs(value), RSSI_START);
            strength = strength > RSSI_START + RSSI_STEP * RSSI_UNITS ? 0
                    : RSSI_UNITS - ((strength - RSSI_START) / RSSI_STEP);

            HmDatapoint vdpRssi = getVirtualDatapoint(channel);
            vdpRssi.setValue(strength);
        }
    }
}
