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
package org.openhab.binding.jablotron.internal.handler;

import static org.openhab.binding.jablotron.JablotronBindingConstants.CHANNEL_LAST_CHECK_TIME;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.jablotron.internal.model.JablotronHistoryDataEvent;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronGetPGResponse;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronGetSectionsResponse;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronSection;
import org.openhab.binding.jablotron.internal.model.ja100f.JablotronState;
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

    public JablotronJa100FHandler(Thing thing, String alarmName) {
        super(thing, alarmName);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (channelUID.getId().startsWith("SEC-") && command instanceof StringType) {
            if ("PARTIAL_ARM".equals(command.toString())) {
                controlComponent(channelUID.getId(), "CONTROL-SECTION", "DISARM");
            }
            scheduler.execute(() -> controlComponent(channelUID.getId(), "CONTROL-SECTION", command.toString()));
        }

        if (channelUID.getId().startsWith("PG-") && command instanceof OnOffType) {
            scheduler.execute(() -> controlComponent(channelUID.getId(), "CONTROL-PG", command.toString()));
        }
    }

    private void controlComponent(String componentId, String action, String value) {
        logger.debug("Controlling component: {} with action: {} and value: {}", componentId, action, value);

        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            JablotronGetSectionsResponse response = handler.controlComponent(getThing(), thingConfig.getCode(), action, value, componentId);
            if (response != null) {
                updateSectionState(response.getData().getStates());
            } else {
                logger.debug("null response/status received");
                updateAlarmStatus();
            }
        }
    }

    private void createPGChannel(String name, String label) {
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "Switch").withLabel(label).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    private void createStateChannel(String name, String label) {
        ChannelTypeUID alarmStatus = new ChannelTypeUID("jablotron", "ja100f_alarm_state");
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), "String").withLabel(label).withType(alarmStatus).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    @Override
    protected synchronized boolean updateAlarmStatus() {
        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            updateState(CHANNEL_LAST_CHECK_TIME, getCheckTime());

            // sections
            JablotronGetSectionsResponse response = handler.sendGetSections(getThing(), alarmName);
            createSectionChannels(response.getData().getSections());
            updateSectionState(response.getData().getStates());

            // PGs
            JablotronGetPGResponse resp = handler.sendGetProgrammableGates(getThing(), alarmName);
            createPGChannels(resp.getData().getProgrammableGates());
            updateSectionState(resp.getData().getStates());

            // update events
            List<JablotronHistoryDataEvent> events = sendGetEventHistory(alarmName);
            if (events != null && events.size() > 0) {
                JablotronHistoryDataEvent event = events.get(0);
                updateLastEvent(event);
            }
            return true;
        } else {
            return false;
        }
    }

    private void createPGChannels(List<JablotronSection> programmableGates) {
        for (JablotronSection gate : programmableGates) {
            String id = gate.getCloudComponentId();
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
            String id = section.getCloudComponentId();
            logger.trace("component id: {} with name: {}", id, section.getName());
            Channel channel = getThing().getChannel(id);
            if (channel == null) {
                logger.debug("Creating a new channel: {}", id);
                createStateChannel(id, section.getName());
            }
        }
    }

    private void updateSectionState(List<JablotronState> states) {
        for (JablotronState state : states) {
            String id = state.getCloudComponentId();
            logger.debug("component id: {} with state: {}", id, state.getState());
            State newState;

            if (id.startsWith("SEC-")) {
                newState = new StringType(state.getState());
            } else if (id.startsWith("PG-")) {
                newState = "ON".equals(state.getState()) ? OnOffType.ON : OnOffType.OFF;
            } else {
                logger.debug("unknown component id: {}", id);
                break;
            }
            Channel channel = getThing().getChannel(id);
            if (channel != null) {
                updateState(channel.getUID(), newState);
            } else {
                logger.debug("The channel: {} still doesn't exist!", id);
            }
        }
    }

}
