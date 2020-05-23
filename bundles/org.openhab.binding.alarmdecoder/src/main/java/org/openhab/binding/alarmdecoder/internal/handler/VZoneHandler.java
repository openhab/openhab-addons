/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.alarmdecoder.internal.handler;

import static org.openhab.binding.alarmdecoder.internal.AlarmDecoderBindingConstants.CHANNEL_COMMAND;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.alarmdecoder.internal.config.VZoneConfig;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VZoneHandler} is responsible for sending state commands to virtual zones.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class VZoneHandler extends ADThingHandler {

    public static final String CMD_OPEN = "OPEN";
    public static final String CMD_CLOSED = "CLOSED";

    private final Logger logger = LoggerFactory.getLogger(VZoneHandler.class);

    private VZoneConfig config = new VZoneConfig();

    public VZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(VZoneConfig.class);

        if (config.address < 0 || config.address > 99) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid address setting");
            return;
        }
        logger.debug("Virtual zone handler initializing for address {}", config.address);
        initDeviceState();
    }

    @Override
    public void initChannelState() {
        // Do nothing
    }

    @Override
    public void notifyPanelReady() {
        // Do nothing
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            if (command instanceof StringType) {
                String cmd = ((StringType) command).toString();
                if (CMD_OPEN.equalsIgnoreCase(cmd)) {
                    sendCommand(ADCommand.setZone(config.address, ADCommand.ZONE_OPEN));
                } else if (CMD_CLOSED.equalsIgnoreCase(cmd)) {
                    sendCommand(ADCommand.setZone(config.address, ADCommand.ZONE_CLOSED));
                } else {
                    logger.debug("Virtual zone handler {} received invalid command: {}", config.address, cmd);
                }
            }
        }
    }

    @Override
    public void handleUpdate(ADMessage msg) {
        // Ignore update requests
    }
}
