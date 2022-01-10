/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.handler;

import org.openhab.binding.evohome.internal.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.api.models.v2.response.GatewayStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystemStatus;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

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
        } else if (!handleActiveFaults(gatewayStatus)) {
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
