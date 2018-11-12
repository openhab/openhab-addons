/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.handler;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.evohome.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.api.models.v2.response.GatewayStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystemStatus;

/**
 * Handler for a temperature control system. Gets and sets global system mode.
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class EvohomeTemperatureControlSystemHandler extends BaseEvohomeHandler {
    private GatewayStatus gatewayStatus;
    private TemperatureControlSystemStatus tcsStatus;

    public EvohomeTemperatureControlSystemHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public void update(GatewayStatus gatewayStatus, TemperatureControlSystemStatus tcsStatus) {
        this.gatewayStatus = gatewayStatus;
        this.tcsStatus = tcsStatus;

        if (tcsStatus == null || gatewayStatus == null) {
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Status not found, check the display id");
        } else if (handleActiveFaults(gatewayStatus) == false) {
            updateEvohomeThingStatus(ThingStatus.ONLINE);
            updateState(EvohomeBindingConstants.DISPLAY_SYSTEM_MODE_CHANNEL,
                    new StringType(tcsStatus.getMode().getMode()));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            update(gatewayStatus, tcsStatus);
        } else if (channelUID.getId().equals(EvohomeBindingConstants.DISPLAY_SYSTEM_MODE_CHANNEL)) {
            EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
            if (bridge != null) {
                bridge.setTcsMode(getEvohomeThingConfig().id, command.toString());
            }
        }
    }

    private boolean handleActiveFaults(GatewayStatus gatewayStatus) {
        if (gatewayStatus.hasActiveFaults()) {
            updateEvohomeThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    gatewayStatus.getActiveFault(0).getFaultType());
            return true;
        }
        return false;
    }

}
