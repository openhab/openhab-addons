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
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.alarmdecoder.internal.config.ZoneConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZoneHandler} is responsible for handling wired zones (i.e. REL & EXP messages).
 *
 * @author Bob Adair - Initial contribution
 * @author Bill Forsyth - Initial contribution
 */
@NonNullByDefault
public class ZoneHandler extends ADThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ZoneHandler.class);

    private @NonNullByDefault({}) ZoneConfig config;

    /** Construct zone id from address and channel */
    public static final String zoneID(int address, int channel) {
        return String.format("%d-%d", address, channel);
    }

    public ZoneHandler(Thing thing) {
        super(thing);
    }

    /**
     * Returns true if this handler is responsible for the zone with the supplied address and channel.
     */
    public boolean responsibleFor(final int address, final int channel) {
        return (config.address != null && config.channel != null && config.address.equals(address)
                && config.channel.equals(channel));
    }

    @Override
    public void initialize() {
        config = getConfigAs(ZoneConfig.class);

        if (config.address == null || config.channel == null || config.address < 0 || config.channel < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        logger.debug("Zone handler initializing for address {} channel {}", config.address, config.channel);

        String id = zoneID(config.address, config.channel);
        updateProperty(PROPERTY_ID, id); // set representation property used by discovery

        initDeviceState();
        logger.trace("Zone handler finished initializing");
    }

    @Override
    protected void initDeviceState() {
        logger.trace("Initializing device state for Zone {},{}", config.address, config.channel);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            initChannelState();
            firstUpdateReceived.set(false);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Set contact channel state to "UNDEF" at init time. The real state will be set either when the first message
     * arrives for the zone, or it should be set to "CLOSED" the first time the panel goes into the "READY" state.
     */
    @Override
    public void initChannelState() {
        UnDefType state = UnDefType.UNDEF;
        updateState(CHANNEL_CONTACT, state);
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

    public void handleUpdate(int data) {
        logger.trace("Zone handler for {},{} received update: {}", config.address, config.channel, data);
        firstUpdateReceived.set(true);
        OpenClosedType state = (data == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        updateState(CHANNEL_CONTACT, state);
    }
}
