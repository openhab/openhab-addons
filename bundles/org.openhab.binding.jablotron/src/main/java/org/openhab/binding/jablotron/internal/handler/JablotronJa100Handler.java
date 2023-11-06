/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.jablotron.internal.handler;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jablotron.internal.model.JablotronControlResponse;
import org.openhab.binding.jablotron.internal.model.JablotronDataUpdateResponse;
import org.openhab.binding.jablotron.internal.model.JablotronServiceDetailSegment;
import org.openhab.binding.jablotron.internal.model.JablotronServiceDetailSegmentInfo;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JablotronJa100Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronJa100Handler extends JablotronAlarmHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronJa100Handler.class);

    public JablotronJa100Handler(Thing thing, String alarmName) {
        super(thing, alarmName);
        dataCache = new ExpiringCache<>(CACHE_TIMEOUT_MS, this::sendGetStatusRequest);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            logger.debug("refreshing channel: {}", channelUID.getId());
            updateChannel(channelUID.getId());
        } else {
            if (channelUID.getId().startsWith("state_") && command instanceof StringType) {
                scheduler.execute(() -> {
                    controlSTATESection(channelUID.getId().toUpperCase(), command.toString());
                });
            }

            if (channelUID.getId().startsWith("pgm_") && command instanceof OnOffType) {
                scheduler.execute(() -> {
                    controlPGMSection(channelUID.getId().toUpperCase(), command.equals(OnOffType.ON) ? "set" : "unset");
                });
            }
        }
    }

    private void updateChannel(String channel) {
        ExpiringCache<JablotronDataUpdateResponse> localDataCache = dataCache;
        if (localDataCache != null) {
            if (channel.startsWith("state_") || channel.startsWith("pgm_") || channel.startsWith("thermometer_")
                    || channel.startsWith("thermostat_")) {
                updateSegmentStatus(channel, localDataCache.getValue());
            } else if (CHANNEL_LAST_CHECK_TIME.equals(channel)) {
                // not updating
            } else {
                updateEventChannel(channel);
            }
        }
    }

    private void createChannel(JablotronServiceDetailSegment section) {
        if (section.getSegmentId().startsWith("PGM_")) {
            createPGMChannel(section.getSegmentId().toLowerCase(), section.getSegmentName());
        } else {
            createStateChannel(section.getSegmentId().toLowerCase(), section.getSegmentName());
        }
    }

    private void createTempChannel(String name, String label) {
        ChannelTypeUID temperature = new ChannelTypeUID(BINDING_ID, "temperature");
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "Number:Temperature")
                .withLabel(label).withType(temperature).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    private void createThermostatChannel(String name, String label) {
        ChannelTypeUID temperature = new ChannelTypeUID(BINDING_ID, "thermostat");
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "Number:Temperature")
                .withLabel(label).withType(temperature).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    private void createPGMChannel(String name, String label) {
        ChannelTypeUID pgmStatus = new ChannelTypeUID(BINDING_ID, "pgm_state");
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "Switch").withLabel(label)
                .withType(pgmStatus).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    private void createStateChannel(String name, String label) {
        ChannelTypeUID alarmStatus = new ChannelTypeUID(BINDING_ID, "alarm_state");
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "String").withLabel(label)
                .withType(alarmStatus).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    @Override
    protected void updateSegmentStatus(JablotronServiceDetailSegment segment) {
        logger.debug("Segment id: {} and status: {}", segment.getSegmentId(), segment.getSegmentState());
        String segmentId = segment.getSegmentId();

        if (segmentId.startsWith("STATE_") || segmentId.startsWith("PGM_")) {
            processSection(segment);
        } else if (segmentId.startsWith("THERMOMETER_")) {
            processThermometer(segment);
        } else if (segmentId.startsWith("THERMOSTAT_")) {
            processThermostat(segment);
        } else {
            logger.debug("Unknown segment received: {} with state: {}", segment.getSegmentId(),
                    segment.getSegmentState());
        }
    }

    private void processSection(JablotronServiceDetailSegment segment) {
        String segmentId = segment.getSegmentId().toLowerCase();
        Channel channel = getThing().getChannel(segmentId);
        if (channel == null) {
            logger.debug("Creating a new channel: {}", segmentId);
            createChannel(segment);
        }
        channel = getThing().getChannel(segmentId);
        if (channel != null) {
            logger.debug("Updating channel: {} to value: {}", channel.getUID(), segment.getSegmentState());
            State newState;
            if (segmentId.startsWith("pgm_")) {
                newState = "unset".equals(segment.getSegmentState()) ? OnOffType.OFF : OnOffType.ON;
            } else {
                newState = new StringType(segment.getSegmentState());
            }
            updateState(channel.getUID(), newState);
        } else {
            logger.debug("The channel: {} still doesn't exist!", segmentId);
        }
    }

    private void processThermometer(JablotronServiceDetailSegment segment) {
        String segmentId = segment.getSegmentId().toLowerCase();
        Channel channel = getThing().getChannel(segmentId);
        if (channel == null) {
            logger.debug("Creating a new temperature channel: {}", segmentId);
            createTempChannel(segmentId, segment.getSegmentName());
            processThermometer(segment);
            return;
        }
        updateTemperatureChannel(channel, segment);
    }

    private void processThermostat(JablotronServiceDetailSegment segment) {
        String segmentId = segment.getSegmentId().toLowerCase();
        Channel channel = getThing().getChannel(segmentId);
        if (channel == null) {
            logger.debug("Creating a new thermostat channel: {}", segmentId);
            createThermostatChannel(segmentId, segment.getSegmentName());
            processThermostat(segment);
            return;
        }
        updateTemperatureChannel(channel, segment);
    }

    private void updateTemperatureChannel(Channel channel, JablotronServiceDetailSegment segment) {
        List<JablotronServiceDetailSegmentInfo> infos = segment.getSegmentInfos();
        if (!infos.isEmpty()) {
            logger.debug("Found value: {} and type: {}", infos.get(0).getValue(), infos.get(0).getType());
            updateState(channel.getUID(), QuantityType.valueOf(infos.get(0).getValue(), SIUnits.CELSIUS));
        } else {
            logger.debug("No segment information received");
        }
    }

    public synchronized void controlPGMSection(String section, String status) {
        logger.debug("Controlling section: {} with status: {}", section, status);
        JablotronControlResponse response = sendUserCode(section, section.toLowerCase(), status, thingConfig.getCode());

        updateAlarmStatus();
        if (response == null) {
            logger.debug("null response/status received during the control of PGM section: {}", section);
        }
    }

    public synchronized void controlSTATESection(String section, String status) {
        logger.debug("Controlling section: {} with status: {}", section, status);
        JablotronControlResponse response = sendUserCode(section, section.toLowerCase().replace("state", "section"),
                status, thingConfig.getCode());

        updateAlarmStatus();
        if (response == null) {
            logger.debug("null response/status received during the control of STATE section: {}", section);
        }
    }
}
