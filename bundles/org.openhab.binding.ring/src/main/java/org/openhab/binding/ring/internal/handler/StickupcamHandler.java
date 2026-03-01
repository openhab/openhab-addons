/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.handler;

import static org.openhab.binding.ring.RingBindingConstants.*;
import static org.openhab.binding.ring.internal.ApiConstants.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.api.RingDeviceTO;
import org.openhab.binding.ring.internal.device.Stickupcam;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The handler for a Ring Video Stickup Cam.
 *
 * @author Chris Milbert - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 *
 */

@NonNullByDefault
public class StickupcamHandler extends RingDeviceHandler {
    private int lastBattery = -1;
    private long lastSnapshotTimestamp = -1;
    private TimeZoneProvider timeZoneProvider;
    private boolean batterySupport = false;
    private boolean lightSupport = false;
    private boolean sirenSupport = false;

    public StickupcamHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Stickupcam handler");
        super.initialize(Stickupcam.class);
        String kind = thing.getProperties().get(THING_PROPERTY_KIND);
        if (kind != null && !kind.isEmpty()) {
            if (BATTERY_KINDS.contains(kind)) {
                batterySupport = true;
            }
            if (LIGHT_KINDS.contains(kind)) {
                lightSupport = true;
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), CHANNEL_STATUS_LIGHT);
                Channel channel = thing.getChannel(channelUID);
                if (channel == null) {
                    logger.debug("Adding channel for light, on device {}", getThing().getUID());
                    ThingBuilder thingBuilder = editThing();
                    channel = ChannelBuilder.create(channelUID, "switch").withLabel("Light Status")
                            .withType(new ChannelTypeUID(BINDING_ID, "light")).build();
                    thingBuilder.withChannel(channel);
                    updateThing(thingBuilder.build());
                }
            }
            if (SIREN_KINDS.contains(kind)) {
                sirenSupport = true;
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), CHANNEL_STATUS_SIREN);
                Channel channel = thing.getChannel(channelUID);
                if (channel == null) {
                    logger.debug("Adding channel for siren, on device {}", getThing().getUID());
                    ThingBuilder thingBuilder = editThing();
                    channel = ChannelBuilder.create(channelUID, "switch").withLabel("Siren Status")
                            .withType(new ChannelTypeUID(BINDING_ID, "siren")).build();
                    thingBuilder.withChannel(channel);
                    updateThing(thingBuilder.build());
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            return;
        }
        if (channelUID.getId().equals(CHANNEL_STATUS_LIGHT)) {
            if (command instanceof OnOffType onOffCommand) {
                lightCommand(onOffCommand == OnOffType.ON);
            }
        }
        if (channelUID.getId().equals(CHANNEL_STATUS_SIREN)) {
            if (command instanceof OnOffType onOffCommand) {
                sirenCommand(onOffCommand == OnOffType.ON);
            }
        }
    }

    @Override
    protected void refreshState() {
        // Do Nothing
    }

    @Override
    protected void minuteTick() {
        logger.debug("StickupcamHandler - minuteTick - device {}", getThing().getUID().getId());
        if (device == null) {
            initialize();
            return;
        }

        RingDeviceTO deviceTO = device.getDeviceStatus();
        if (batterySupport) {
            if (deviceTO.health.batteryPercentage != lastBattery) {
                logger.debug("Battery Level: {}", deviceTO.battery);
                ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_BATTERY);
                updateState(channelUID, new DecimalType(deviceTO.health.batteryPercentage));
                lastBattery = deviceTO.health.batteryPercentage;
            } else {
                logger.debug("Battery Level Unchanged for {} - {} vs {}", getThing().getUID().getId(),
                        deviceTO.health.batteryPercentage, lastBattery);

            }
        }

        if (lightSupport) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_LIGHT);
            updateState(channelUID, OnOffType.from((deviceTO.health.floodlightOn || deviceTO.health.whiteLedOn)));
        }

        if (sirenSupport) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_SIREN);
            updateState(channelUID, OnOffType.from(deviceTO.health.sirenOn));
        }

        long timestamp = getSnapshotTimestamp();
        if (timestamp > lastSnapshotTimestamp) {
            logger.debug("timestamp = {} != lastSnapshotTimestamp {}, update snapshot channel", timestamp,
                    lastSnapshotTimestamp);
            lastSnapshotTimestamp = timestamp;
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_SNAPSHOT);
            updateState(channelUID, new RawType(getSnapshot(), "image/jpeg"));
            channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_SNAPSHOT_TIMESTAMP);
            updateState(channelUID, new DateTimeType(ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp),
                    timeZoneProvider.getTimeZone())));
        }
    }

    protected void lightCommand(boolean b) {
        String command = URL_LIGHT + (b ? "on" : "off");
        sendCommand(URL_DOORBELLS, command);
    }

    protected void sirenCommand(boolean b) {
        String command = URL_SIREN + (b ? "on" : "off");
        sendCommand(URL_DOORBELLS, command);
    }
}
