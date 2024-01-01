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
import org.openhab.binding.alarmdecoder.internal.config.LRRConfig;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.LRRMessage;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LRRHandler} is responsible for handling long range radio (LRR) messages.
 *
 * @author Bob Adair - Initial contribution
 * @author Bill Forsyth - Initial contribution
 */
@NonNullByDefault
public class LRRHandler extends ADThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LRRHandler.class);

    private LRRConfig config = new LRRConfig();

    public LRRHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(LRRConfig.class);

        if (config.partition < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        logger.debug("LRR handler initializing for partition {}", config.partition);

        initDeviceState();

        logger.trace("LRR handler finished initializing");
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
        // All channels are read-only, so ignore all commands.
    }

    @Override
    public void handleUpdate(ADMessage msg) {
        if (!(msg instanceof LRRMessage)) {
            return;
        }
        LRRMessage lrrMsg = (LRRMessage) msg;

        if (config.partition == lrrMsg.partition || config.partition == 0 || lrrMsg.partition == 0) {
            logger.trace("LRR handler for partition {} received update: {}", config.partition, msg);
            updateState(CHANNEL_LRR_PARTITION, new DecimalType(lrrMsg.partition));
            updateState(CHANNEL_LRR_EVENTDATA, new DecimalType(lrrMsg.eventData));
            updateState(CHANNEL_LRR_CIDMESSAGE, new StringType(lrrMsg.cidMessage));
            updateState(CHANNEL_LRR_REPORTCODE, new StringType(lrrMsg.reportCode));
        }
    }
}
