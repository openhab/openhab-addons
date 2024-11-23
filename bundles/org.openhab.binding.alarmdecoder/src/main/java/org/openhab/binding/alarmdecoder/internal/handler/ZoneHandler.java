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
import org.openhab.binding.alarmdecoder.internal.config.ZoneConfig;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.EXPMessage;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZoneHandler} is responsible for handling wired zones (i.e. REL &amp; EXP messages).
 *
 * @author Bob Adair - Initial contribution
 * @author Bill Forsyth - Initial contribution
 */
@NonNullByDefault
public class ZoneHandler extends ADThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ZoneHandler.class);

    private ZoneConfig config = new ZoneConfig();

    public ZoneHandler(Thing thing) {
        super(thing);
    }

    /** Construct zone id from address and channel */
    public static String zoneID(int address, int channel) {
        return String.format("%d-%d", address, channel);
    }

    @Override
    public void initialize() {
        config = getConfigAs(ZoneConfig.class);

        if (config.address < 0 || config.channel < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid address/channel setting");
            return;
        }
        logger.debug("Zone handler initializing for address {} channel {}", config.address, config.channel);

        String id = zoneID(config.address, config.channel);
        updateProperty(PROPERTY_ID, id); // set representation property used by discovery

        initDeviceState();
        logger.trace("Zone handler finished initializing");
    }

    /**
     * Set contact channel state to "UNDEF" at init time. The real state will be set either when the first message
     * arrives for the zone, or it should be set to "CLOSED" the first time the panel goes into the "READY" state.
     */
    @Override
    public void initChannelState() {
        UnDefType state = UnDefType.UNDEF;
        updateState(CHANNEL_CONTACT, state);
        firstUpdateReceived.set(false);
    }

    @Override
    public void notifyPanelReady() {
        logger.trace("Zone handler for {},{} received panel ready notification.", config.address, config.channel);
        if (firstUpdateReceived.compareAndSet(false, true)) {
            updateState(CHANNEL_CONTACT, OpenClosedType.CLOSED);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // All channels are read-only, so ignore all commands.
    }

    @Override
    public void handleUpdate(ADMessage msg) {
        if (!(msg instanceof EXPMessage)) {
            return;
        }
        EXPMessage expMsg = (EXPMessage) msg;

        if (config.address == expMsg.address && config.channel == expMsg.channel) {
            logger.trace("Zone handler for {},{} received update: {}", config.address, config.channel, expMsg.data);

            firstUpdateReceived.set(true);
            OpenClosedType state = (expMsg.data == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateState(CHANNEL_CONTACT, state);
        }
    }
}
