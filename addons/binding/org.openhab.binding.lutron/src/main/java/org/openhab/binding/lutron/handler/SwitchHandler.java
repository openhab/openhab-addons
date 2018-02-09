/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_SWITCH;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;

/**
 * Handler responsible for communicating with a switch.
 *
 * @author Allan Tong - Initial contribution
 */
public class SwitchHandler extends LutronHandler {
    private static final Integer ACTION_ZONELEVEL = 1;

    private int integrationId;

    public SwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");

        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");

            return;
        }

        this.integrationId = id.intValue();

        updateStatus(ThingStatus.ONLINE);
        queryOutput(ACTION_ZONELEVEL);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            if (command.equals(OnOffType.ON)) {
                output(ACTION_ZONELEVEL, 100);
            } else if (command.equals(OnOffType.OFF)) {
                output(ACTION_ZONELEVEL, 0);
            }
        }
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length > 1
                && ACTION_ZONELEVEL.toString().equals(parameters[0])) {
            BigDecimal level = new BigDecimal(parameters[1]);

            postCommand(CHANNEL_SWITCH, level.compareTo(BigDecimal.ZERO) == 0 ? OnOffType.OFF : OnOffType.ON);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            // Refresh state when new item is linked.
            queryOutput(ACTION_ZONELEVEL);
        }
    }
}
