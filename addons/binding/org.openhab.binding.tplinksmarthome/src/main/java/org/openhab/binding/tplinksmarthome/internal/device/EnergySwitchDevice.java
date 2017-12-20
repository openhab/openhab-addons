/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.model.Realtime;

/**
 * TP-Link Smart Home Switch device that also can measure energy usage.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnergySwitchDevice extends SwitchDevice {

    @Override
    public String getUpdateCommand() {
        return Commands.getRealtimeAndSysinfo();
    }

    @Override
    public State updateChannel(String channelId, DeviceState deviceState) {
        final State state;

        if (CHANNELS_ENERGY.contains(channelId)) {
            state = updateEnergyChannel(channelId, deviceState.getRealtime());
        } else {
            state = super.updateChannel(channelId, deviceState);
        }
        return state;
    }

    private State updateEnergyChannel(String channelId, Realtime realtime) {
        final double value;

        switch (channelId) {
            case CHANNEL_ENERGY_CURRENT:
                value = realtime.getCurrent();
                break;
            case CHANNEL_ENERGY_TOTAL:
                value = realtime.getTotal();
                break;
            case CHANNEL_ENERGY_VOLTAGE:
                value = realtime.getVoltage();
                break;
            case CHANNEL_ENERGY_POWER:
                value = realtime.getPower();
                break;
            default:
                value = 0;
                break;
        }
        return new DecimalType(value);
    }
}
