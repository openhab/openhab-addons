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

import static org.openhab.binding.alarmdecoder.internal.AlarmDecoderBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.alarmdecoder.internal.config.LRRConfig;
import org.openhab.binding.alarmdecoder.internal.protocol.LRRMessage;
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

    private @NonNullByDefault({}) LRRConfig config;

    public LRRHandler(Thing thing) {
        super(thing);
    }

    /**
     * Returns true if this handler is responsible for the supplied partition.
     * This is true is this handler's partition is 0 (all), the supplied partition is 0 (all), or if this handler's
     * partition matches the supplied partition.
     */
    public Boolean responsibleFor(final int partition) {
        return (config.partition == partition || config.partition == 0 || partition == 0);
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
    protected void initDeviceState() {
        logger.trace("Initializing device state for RLL partition {}", config.partition);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            initChannelState();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void initChannelState() {
        // TODO: Init channel states if necessary
    }

    @Override
    public void notifyPanelReady() {
        // Do nothing
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // All channels are read-only, so ignore all commands.
    }

    public void handleUpdate(LRRMessage msg) {
        logger.trace("LRR handler for partition {} received update: {}", config.partition, msg);
        updateState(CHANNEL_LRR_PARTITION, new DecimalType(msg.partition));
        updateState(CHANNEL_LRR_EVENTDATA, new DecimalType(msg.eventData));
        updateState(CHANNEL_LRR_CIDMESSAGE, new StringType(msg.cidMessage));
        updateState(CHANNEL_LRR_REPORTCODE, new StringType(msg.reportCode));
    }
}
