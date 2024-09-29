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
import org.openhab.binding.alarmdecoder.internal.config.RFZoneConfig;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.RFXMessage;
import org.openhab.core.library.types.OnOffType;
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
 * The {@link RFZoneHandler} is responsible for handling wired zones (i.e. RFX messages).
 *
 * @author Bob Adair - Initial contribution
 * @author Bill Forsyth - Initial contribution
 */
@NonNullByDefault
public class RFZoneHandler extends ADThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RFZoneHandler.class);

    private RFZoneConfig config = new RFZoneConfig();

    public RFZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(RFZoneConfig.class);

        if (config.serial < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid serial setting");
            return;
        }
        logger.debug("RF Zone handler initializing for serial {}", config.serial);

        initDeviceState();

        logger.trace("RF Zone handler finished initializing");
    }

    /**
     * Set contact channel states to "UNDEF" at init time. The real states will be set either when the first message
     * arrives for the zone, or they will be set to "CLOSED" the first time the panel goes into the "READY" state.
     */
    @Override
    public void initChannelState() {
        UnDefType state = UnDefType.UNDEF;
        updateState(CHANNEL_RF_LOWBAT, state);
        updateState(CHANNEL_RF_SUPERVISION, state);
        updateState(CHANNEL_RF_LOOP1, state);
        updateState(CHANNEL_RF_LOOP2, state);
        updateState(CHANNEL_RF_LOOP3, state);
        updateState(CHANNEL_RF_LOOP4, state);
        firstUpdateReceived.set(false);
    }

    @Override
    public void notifyPanelReady() {
        logger.trace("RF Zone handler for {} received panel ready notification.", config.serial);
        if (firstUpdateReceived.compareAndSet(false, true)) {
            updateState(CHANNEL_RF_LOOP1, OpenClosedType.CLOSED);
            updateState(CHANNEL_RF_LOOP2, OpenClosedType.CLOSED);
            updateState(CHANNEL_RF_LOOP3, OpenClosedType.CLOSED);
            updateState(CHANNEL_RF_LOOP4, OpenClosedType.CLOSED);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Does not accept any commands
    }

    @Override
    public void handleUpdate(ADMessage msg) {
        if (!(msg instanceof RFXMessage)) {
            return;
        }
        RFXMessage rfxMsg = (RFXMessage) msg;

        if (config.serial == rfxMsg.serial) {
            logger.trace("RF Zone handler for serial {} received update: {}", config.serial, rfxMsg.data);
            firstUpdateReceived.set(true);

            updateState(CHANNEL_RF_LOWBAT, OnOffType.from((rfxMsg.data & RFXMessage.BIT_LOWBAT) != 0));
            updateState(CHANNEL_RF_SUPERVISION, OnOffType.from((rfxMsg.data & RFXMessage.BIT_SUPER) != 0));

            updateState(CHANNEL_RF_LOOP1,
                    (rfxMsg.data & RFXMessage.BIT_LOOP1) == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateState(CHANNEL_RF_LOOP2,
                    (rfxMsg.data & RFXMessage.BIT_LOOP2) == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateState(CHANNEL_RF_LOOP3,
                    (rfxMsg.data & RFXMessage.BIT_LOOP3) == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            updateState(CHANNEL_RF_LOOP4,
                    (rfxMsg.data & RFXMessage.BIT_LOOP4) == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        }
    }
}
