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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Valve component, following the https://www.home-assistant.io/integrations/valve.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Valve extends AbstractComponent<Valve.ChannelConfiguration> implements ChannelStateUpdateListener {
    public static final String VALVE_CHANNEL_ID = "valve";
    public static final String STATE_CHANNEL_ID = "state";
    public static final String RAW_STATE_CHANNEL_ID = "state";

    private static final String POSITION_KEY = "position";
    private static final String STATE_KEY = "state";

    private static final String FORMAT_INTEGER = "%.0f";

    private final Logger logger = LoggerFactory.getLogger(Valve.class);

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Valve");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("state_topic")
        protected @Nullable String stateTopic;
        @SerializedName("command_template")
        protected @Nullable String commandTemplate;
        @SerializedName("command_topic")
        protected String commandTopic = "";

        @SerializedName("payload_close")
        protected @Nullable String payloadClose = "CLOSE";
        @SerializedName("payload_open")
        protected @Nullable String payloadOpen = "OPEN";
        @SerializedName("payload_stop")
        protected @Nullable String payloadStop;
        @SerializedName("position_closed")
        protected int positionClosed = 0;
        @SerializedName("position_open")
        protected int positionOpen = 100;
        @SerializedName("reports_position")
        protected boolean reportsPosition = false;
        @SerializedName("state_closed")
        protected @Nullable String stateClosed = "closed";
        @SerializedName("state_closing")
        protected @Nullable String stateClosing = "closing";
        @SerializedName("state_open")
        protected @Nullable String stateOpen = "open";
        @SerializedName("state_opening")
        protected @Nullable String stateOpening = "opening";
    }

    private final OnOffValue onOffValue;
    private final PercentageValue positionValue;
    private final TextValue stateValue;
    private final ChannelStateUpdateListener channelStateUpdateListener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Valve(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);
        this.channelStateUpdateListener = componentConfiguration.getUpdateListener();

        AutoUpdatePolicy autoUpdatePolicy = null;
        if ((channelConfiguration.optimistic != null && channelConfiguration.optimistic == true)
                || channelConfiguration.stateTopic == null) {
            autoUpdatePolicy = AutoUpdatePolicy.RECOMMEND;
        }

        onOffValue = new OnOffValue(channelConfiguration.stateOpen, channelConfiguration.stateClosed,
                channelConfiguration.payloadOpen, channelConfiguration.payloadClose);
        positionValue = new PercentageValue(BigDecimal.valueOf(channelConfiguration.positionClosed),
                BigDecimal.valueOf(channelConfiguration.positionOpen), null, null, null, FORMAT_INTEGER);

        if (channelConfiguration.reportsPosition) {
            buildChannel(VALVE_CHANNEL_ID, ComponentChannelType.DIMMER, positionValue, getName(), this)
                    .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.commandTemplate)
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
        } else {
            buildChannel(VALVE_CHANNEL_ID, ComponentChannelType.SWITCH, onOffValue, getName(), this)
                    .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.commandTemplate)
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
        }

        List<String> commandValues = new ArrayList<>();
        addCommandValue(commandValues, channelConfiguration.payloadOpen);
        addCommandValue(commandValues, channelConfiguration.payloadClose);
        addCommandValue(commandValues, channelConfiguration.payloadStop);

        List<String> stateValues = new ArrayList<>();
        addCommandValue(stateValues, channelConfiguration.stateOpen);
        addCommandValue(stateValues, channelConfiguration.stateOpening);
        addCommandValue(stateValues, channelConfiguration.stateClosed);
        addCommandValue(stateValues, channelConfiguration.stateClosing);
        stateValue = new TextValue(stateValues.toArray(new String[0]), commandValues.toArray(new String[0]));

        final var rawStateChannel = buildChannel(RAW_STATE_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(),
                "State", this).stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos(), channelConfiguration.commandTemplate)
                .build(false);
        hiddenChannels.add(rawStateChannel);

        // If valve doesn't support stop, and can't report in-progress states, we don't need an exposed channel for
        // state
        if (channelConfiguration.payloadStop != null || channelConfiguration.stateOpening != null
                || channelConfiguration.stateClosing != null) {
            buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING, stateValue, "State", this)
                    .withAutoUpdatePolicy(autoUpdatePolicy).isAdvanced(true).commandFilter(command -> {
                        // OPEN and CLOSE need to be sent as 0/100 for positional valves
                        if (channelConfiguration.reportsPosition && command instanceof StringType commandStr) {
                            if (command.equals(channelConfiguration.payloadOpen)) {
                                command = PercentType.HUNDRED;
                            } else if (command.equals(channelConfiguration.payloadClose)) {
                                command = PercentType.ZERO;
                            }
                        }
                        rawStateChannel.getState().publishValue(command);
                        return false;
                    }).build();
        }

        finalizeChannels();
    }

    private void addCommandValue(List<String> commandValues, @Nullable String command) {
        if (command != null) {
            commandValues.add(command);
        }
    }

    // Valve uses a dynamic format for the payload coming through, so we have to apply
    // significant additional logic here.
    // It can be JSON, in which case state and/or position can be supplied
    // Or it can be a raw value - either a literal state, or a numeric position.
    // If it's a numeric position (and wasn't JSON that had both state and position)
    // then the state gets inferred from the position if it's fully open or fully closed.
    @Override
    public void updateChannelState(ChannelUID channel, State state) {
        Set<String> states = stateValue.getStates();

        @Nullable
        String statePayload = state.toString();
        Map<String, String> json = toJson(statePayload);
        // have JSON; we can clearly know what's state and what's position
        if (json != null) {
            statePayload = json.get(STATE_KEY);
            String positionPayload = json.get(POSITION_KEY);
            if (channelConfiguration.reportsPosition && positionPayload == null) {
                logger.warn("Missing required `position` attribute in json payload on topic '{}'",
                        channelConfiguration.stateTopic);
                return;
            }
            if (!channelConfiguration.reportsPosition && statePayload == null) {
                logger.warn("Missing required `state` attribute in json payload on topic '{}'",
                        channelConfiguration.stateTopic);
                return;
            }

            // We have both state and position; no need to guess anything
            if (channelConfiguration.reportsPosition) {
                if (statePayload != null) {
                    if (states != null && !states.contains(statePayload)) {
                        logger.warn("Invalid state '{}' for {}", statePayload, getHaID().toShortTopic());
                    } else {
                        stateValue.update(new StringType(statePayload));
                        channelStateUpdateListener.updateChannelState(buildChannelUID(STATE_CHANNEL_ID),
                                stateValue.getChannelState());
                    }

                    try {
                        positionValue.update((State) positionValue
                                .parseMessage(DecimalType.valueOf(Objects.requireNonNull(positionPayload))));
                        channelStateUpdateListener.updateChannelState(buildChannelUID(VALVE_CHANNEL_ID),
                                positionValue.getChannelState());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Ignoring non numeric payload '{}' received on topic '{}'", positionPayload,
                                channelConfiguration.stateTopic);
                    }

                    return;
                } else {
                    statePayload = positionPayload;
                }
            }
        }

        statePayload = Objects.requireNonNull(statePayload);
        if (states != null && states.contains(statePayload)) {
            if (channelConfiguration.reportsPosition) {
                if (statePayload.equals(channelConfiguration.stateClosed)) {
                    positionValue.update(PercentType.ZERO);
                    channelStateUpdateListener.updateChannelState(buildChannelUID(VALVE_CHANNEL_ID),
                            positionValue.getChannelState());
                } else if (statePayload.equals(channelConfiguration.stateOpen)) {
                    positionValue.update(PercentType.HUNDRED);
                    channelStateUpdateListener.updateChannelState(buildChannelUID(VALVE_CHANNEL_ID),
                            positionValue.getChannelState());
                }
            } else {
                if (statePayload.equals(channelConfiguration.stateClosed)) {
                    onOffValue.update(OnOffType.OFF);
                } else {
                    onOffValue.update(OnOffType.ON);
                }
                channelStateUpdateListener.updateChannelState(buildChannelUID(VALVE_CHANNEL_ID),
                        onOffValue.getChannelState());
            }
            stateValue.update(new StringType(statePayload));
            channelStateUpdateListener.updateChannelState(buildChannelUID(STATE_CHANNEL_ID),
                    stateValue.getChannelState());
            return;
        }

        if (channelConfiguration.reportsPosition) {
            // The state isn't a given state; it must be a number
            try {
                positionValue.update((State) positionValue.parseMessage(DecimalType.valueOf(statePayload)));
            } catch (IllegalArgumentException e) {
                logger.warn("Ignoring non numeric payload '{}' received on topic '{}'", statePayload,
                        channelConfiguration.stateTopic);
                return;
            }
            channelStateUpdateListener.updateChannelState(buildChannelUID(VALVE_CHANNEL_ID),
                    positionValue.getChannelState());
            if (positionValue.getChannelState().equals(PercentType.ZERO)) {
                stateValue.update(new StringType(channelConfiguration.stateClosed));
                channelStateUpdateListener.updateChannelState(buildChannelUID(STATE_CHANNEL_ID),
                        stateValue.getChannelState());
            } else if (positionValue.getChannelState().equals(PercentType.HUNDRED)
                    || stateValue.getChannelState().equals(channelConfiguration.stateClosed)) {
                // Specifically set up to _not_ overwrite "opening" or "closing", but to set to "open"
                // if we're full-open, or if it was previously "closed"
                stateValue.update(new StringType(channelConfiguration.stateOpen));
                channelStateUpdateListener.updateChannelState(buildChannelUID(STATE_CHANNEL_ID),
                        stateValue.getChannelState());
            }

            return;
        }

        logger.warn("Invalid state '{}' for {}", statePayload, getHaID().toShortTopic());
    }

    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        throw new UnsupportedOperationException();
    }

    private @Nullable Map<String, String> toJson(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            if (node.getNodeType() != JsonNodeType.OBJECT) {
                // numbers look like JSON, but we don't want to treat them as such
                return null;
            }

            Map<String, String> result = new HashMap<>();
            Iterator<Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Entry<String, JsonNode> field = it.next();
                result.put(field.getKey(), field.getValue().asText());
            }
            return result;
        } catch (IOException e) {
            // It's not JSON
            return null;
        }
    }
}
