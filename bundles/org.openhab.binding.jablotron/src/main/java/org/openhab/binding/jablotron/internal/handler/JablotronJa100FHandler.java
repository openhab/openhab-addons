/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jablotron.internal.model.JablotronHistoryDataEvent;
import org.openhab.binding.jablotron.internal.model.JablotronServiceDetailSegment;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronGetPGResponse;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronGetSectionsResponse;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronGetThermoDevicesResponse;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronSection;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronState;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronThermoDevice;
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
 * The {@link JablotronJa100FHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronJa100FHandler extends JablotronAlarmHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronJa100FHandler.class);

    private ExpiringCache<JablotronGetSectionsResponse> sectionCache;
    private ExpiringCache<JablotronGetPGResponse> pgCache;

    public JablotronJa100FHandler(Thing thing, String alarmName) {
        super(thing, alarmName);
        sectionCache = new ExpiringCache<>(CACHE_TIMEOUT_MS, this::sendGetSections);
        pgCache = new ExpiringCache<>(CACHE_TIMEOUT_MS, this::sendGetProgrammableGates);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            logger.debug("refreshing channel: {}", channelUID.getId());
            updateChannel(channelUID.getId());
        } else {
            if (channelUID.getId().startsWith("sec-") && command instanceof StringType) {
                if ("PARTIAL_ARM".equals(command.toString())) {
                    controlComponent(channelUID.getId(), "CONTROL-SECTION", "DISARM");
                }
                scheduler.execute(() -> controlComponent(channelUID.getId().toUpperCase(), "CONTROL-SECTION",
                        command.toString()));
            }

            if (channelUID.getId().startsWith("pg-") && command instanceof OnOffType) {
                scheduler.execute(
                        () -> controlComponent(channelUID.getId().toUpperCase(), "CONTROL-PG", command.toString()));
            }
        }
    }

    private void updateChannel(String channel) {
        if (channel.startsWith("sec-")) {
            JablotronGetSectionsResponse sections = sectionCache.getValue();
            if (sections != null) {
                updateSectionState(channel, sections.getData().getStates());
            }
        } else if (channel.startsWith("pg-")) {
            JablotronGetPGResponse pgs = pgCache.getValue();
            if (pgs != null) {
                updateSectionState(channel, pgs.getData().getStates());
            }
        } else if (CHANNEL_LAST_CHECK_TIME.equals(channel)) {
            // not updating
        } else {
            updateEventChannel(channel);
        }
    }

    @Override
    protected void updateSegmentStatus(JablotronServiceDetailSegment segment) {
        // nothing
    }

    private void controlComponent(String componentId, String action, String value) {
        logger.debug("Controlling component: {} with action: {} and value: {}", componentId, action, value);

        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            JablotronGetSectionsResponse response;
            try {
                response = handler.controlComponent(getThing(), thingConfig.getCode(), action, value, componentId);
            } catch (SecurityException se) {
                response = handler.controlComponent(getThing(), thingConfig.getCode(), action, value, componentId);
            }
            if (response != null) {
                updateSectionState(response.getData().getStates());
            } else {
                logger.debug("null response/status received during the control of component: {}", componentId);
                updateAlarmStatus();
            }
        }
    }

    private void createPGChannel(String name, String label) {
        ChannelTypeUID pgmStatus = new ChannelTypeUID(BINDING_ID, "pgm_state");
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "Switch").withLabel(label)
                .withType(pgmStatus).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    private void createStateChannel(String name, String label) {
        ChannelTypeUID alarmStatus = new ChannelTypeUID(BINDING_ID, "ja100f_alarm_state");
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "String").withLabel(label)
                .withType(alarmStatus).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    private void createThermoChannel(String name, String label) {
        ChannelTypeUID alarmStatus = new ChannelTypeUID(BINDING_ID, "temperature");
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "Number").withLabel(label)
                .withType(alarmStatus).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    private @Nullable JablotronGetSectionsResponse sendGetSections() {
        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            return handler.sendGetSections(getThing(), alarmName);
        }
        return null;
    }

    protected @Nullable JablotronGetPGResponse sendGetProgrammableGates() {
        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            return handler.sendGetProgrammableGates(getThing(), alarmName);
        }
        return null;
    }

    @Override
    protected synchronized boolean updateAlarmStatus() {
        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            updateState(CHANNEL_LAST_CHECK_TIME, getCheckTime());

            // sections
            JablotronGetSectionsResponse response = handler.sendGetSections(getThing(), alarmName);
            if (response != null) {
                createSectionChannels(response.getData().getSections());
                updateSectionState(response.getData().getStates());
            }

            // PGs
            JablotronGetPGResponse resp = handler.sendGetProgrammableGates(getThing(), alarmName);
            if (resp != null) {
                createPGChannels(resp.getData().getProgrammableGates());
                updateSectionState(resp.getData().getStates());
            }

            // thermo devices
            JablotronGetThermoDevicesResponse respThermo = handler.sendGetThermometers(getThing(), alarmName);
            if (respThermo != null) {
                createThermoDeviceChannels(respThermo.getData().getThermoDevices());
                updateThermoState(respThermo.getData().getStates());
            }

            // update events
            JablotronHistoryDataEvent event = sendGetEventHistory();
            if (event != null) {
                updateLastEvent(event);
            }
            return true;
        } else {
            return false;
        }
    }

    private void createPGChannels(List<JablotronSection> programmableGates) {
        for (JablotronSection gate : programmableGates) {
            String id = gate.getCloudComponentId().toLowerCase();
            logger.trace("component id: {} with name: {}", id, gate.getName());
            Channel channel = getThing().getChannel(id);
            if (channel == null) {
                logger.debug("Creating a new channel: {}", id);
                createPGChannel(id, gate.getName());
            }
        }
    }

    private void createSectionChannels(List<JablotronSection> sections) {
        for (JablotronSection section : sections) {
            String id = section.getCloudComponentId().toLowerCase();
            logger.trace("component id: {} with name: {}", id, section.getName());
            Channel channel = getThing().getChannel(id);
            if (channel == null) {
                logger.debug("Creating a new channel: {}", id);
                createStateChannel(id, section.getName());
            }
        }
    }

    private void createThermoDeviceChannels(List<JablotronThermoDevice> thermoDevices) {
        for (JablotronThermoDevice device : thermoDevices) {
            String id = device.getObjectDeviceId().toLowerCase();
            logger.trace("object device id: {} with name: {}", id, device.getName());
            Channel channel = getThing().getChannel(id);
            if (channel == null) {
                logger.debug("Creating a new channel: {}", id);
                createThermoChannel(id, device.getName());
            }
        }
    }

    private void updateSectionState(String section, List<JablotronState> states) {
        for (JablotronState state : states) {
            String id = state.getCloudComponentId();
            if (id.equals(section.toUpperCase())) {
                updateSection(id, state);
                break;
            }
        }
    }

    private void updateSectionState(List<JablotronState> states) {
        for (JablotronState state : states) {
            String id = state.getCloudComponentId();
            updateSection(id, state);
        }
    }

    private void updateThermoState(List<JablotronState> states) {
        for (JablotronState state : states) {
            logger.debug("updating thermo state: {}", state.getObjectDeviceId());
            String id = state.getObjectDeviceId();
            updateSection(id, state);
        }
    }

    private void updateSection(String id, JablotronState state) {
        logger.debug("component id: {} with state: {}", id, state.getState());
        State newState;

        if (id.startsWith("SEC-")) {
            newState = new StringType(state.getState());
        } else if (id.startsWith("PG-")) {
            newState = OnOffType.from("ON".equals(state.getState()));
        } else if (id.startsWith("THM-")) {
            newState = new QuantityType<>(state.getTemperature(), SIUnits.CELSIUS);
        } else {
            logger.debug("unknown component id: {}", id);
            return;
        }
        Channel channel = getThing().getChannel(id.toLowerCase());
        if (channel != null) {
            updateState(channel.getUID(), newState);
        } else {
            logger.debug("The channel: {} still doesn't exist!", id);
        }
    }
}
