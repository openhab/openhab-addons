/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.alarmdecoder.internal.AlarmDecoderBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.alarmdecoder.internal.config.VZoneConfig;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
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
        UnDefType state = UnDefType.UNDEF;
        updateState(CHANNEL_STATE, state);
        firstUpdateReceived.set(false);
    }

    @Override
    public void notifyPanelReady() {
        logger.trace("Virtual zone handler for {} received panel ready notification.", config.address);
        if (firstUpdateReceived.compareAndSet(false, true)) {
            updateState(CHANNEL_STATE, OnOffType.ON);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            if (command instanceof StringType stringCommand) {
                String cmd = stringCommand.toString();
                if (CMD_OPEN.equalsIgnoreCase(cmd)) {
                    sendCommand(ADCommand.setZone(config.address, ADCommand.ZONE_OPEN));
                    setChannelState(OnOffType.OFF);
                } else if (CMD_CLOSED.equalsIgnoreCase(cmd)) {
                    sendCommand(ADCommand.setZone(config.address, ADCommand.ZONE_CLOSED));
                    setChannelState(OnOffType.ON);
                } else {
                    logger.debug("Virtual zone handler {} received invalid command: {}", config.address, cmd);
                }
            }
        } else if (channelUID.getId().equals(CHANNEL_STATE)) {
            if (command instanceof OnOffType) {
                if (command == OnOffType.OFF) {
                    sendCommand(ADCommand.setZone(config.address, ADCommand.ZONE_OPEN));
                    setChannelState(OnOffType.OFF);
                } else if (command == OnOffType.ON) {
                    sendCommand(ADCommand.setZone(config.address, ADCommand.ZONE_CLOSED));
                    setChannelState(OnOffType.ON);
                }
            }
        }
    }

    private void setChannelState(OnOffType state) {
        updateState(CHANNEL_STATE, state);
        firstUpdateReceived.set(true);
    }

    @Override
    public void handleUpdate(ADMessage msg) {
        // There can be no update requests
    }
}
