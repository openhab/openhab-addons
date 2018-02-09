/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
