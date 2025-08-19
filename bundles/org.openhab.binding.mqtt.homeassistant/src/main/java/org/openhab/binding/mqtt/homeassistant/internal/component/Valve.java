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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
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

/**
 * A MQTT Valve component, following the https://www.home-assistant.io/integrations/valve.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Valve extends AbstractComponent<Valve.Configuration> implements ChannelStateUpdateListener {
    public static final String VALVE_CHANNEL_ID = "valve";
    public static final String STATE_CHANNEL_ID = "state";
    public static final String RAW_STATE_CHANNEL_ID = "state";

    private static final String POSITION_KEY = "position";
    private static final String STATE_KEY = "state";

    public static final String PAYLOAD_OPEN = "OPEN";
    public static final String PAYLOAD_CLOSE = "CLOSE";
    public static final String PAYLOAD_STOP = "STOP";

    private static final Map<String, String> COMMAND_LABELS = Map.of(PAYLOAD_OPEN, "@text/command.valve.open",
            PAYLOAD_CLOSE, "@text/command.valve.close", PAYLOAD_STOP, "@text/command.valve.stop");

    public static final String STATE_OPEN = "open";
    public static final String STATE_OPENING = "opening";
    public static final String STATE_CLOSED = "closed";
    public static final String STATE_CLOSING = "closing";

    private static final Map<String, String> STATE_LABELS = Map.of(STATE_OPEN, "@text/state.valve.open", STATE_OPENING,
            "@text/state.valve.opening", STATE_CLOSED, "@text/state.valve.closed", STATE_CLOSING,
            "@text/state.valve.closing");

    private final Logger logger = LoggerFactory.getLogger(Valve.class);

    public static class Configuration extends EntityConfiguration {
        private final boolean reportsPosition;
        private final String stateClosed, stateOpen;

        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Valve");
            reportsPosition = getBoolean("reports_position");
            stateClosed = getString("state_closed");
            stateOpen = getString("state_open");
        }

        @Nullable
        String getCommandTopic() {
            return getOptionalString("command_topic");
        }

        @Nullable
        Value getCommandTemplate() {
            return getOptionalValue("command_template");
        }

        boolean isOptimistic() {
            return getBoolean("optimistic");
        }

        @Nullable
        String getPayloadClose() {
            return getOptionalString("payload_close");
        }

        @Nullable
        String getPayloadOpen() {
            return getOptionalString("payload_open");
        }

        @Nullable
        String getPayloadStop() {
            return getOptionalString("payload_stop");
        }

        int getPositionClosed() {
            return getInt("position_closed");
        }

        int getPositionOpen() {
            return getInt("position_open");
        }

        boolean reportsPosition() {
            return reportsPosition;
        }

        boolean isRetain() {
            return getBoolean("retain");
        }

        String getStateClosed() {
            return stateClosed;
        }

        String getStateClosing() {
            return getString("state_closing");
        }

        String getStateOpen() {
            return stateOpen;
        }

        String getStateOpening() {
            return getString("state_opening");
        }

        @Nullable
        String getStateTopic() {
            return getOptionalString("state_topic");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }
    }

    private final OnOffValue onOffValue;
    private final PercentageValue positionValue;
    private final TextValue stateValue;
    private final ChannelStateUpdateListener channelStateUpdateListener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Valve(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);
        this.channelStateUpdateListener = componentContext.getUpdateListener();

        AutoUpdatePolicy autoUpdatePolicy = null;
        String stateTopic = config.getStateTopic();
        if (config.isOptimistic() || stateTopic == null) {
            autoUpdatePolicy = AutoUpdatePolicy.RECOMMEND;
        }

        String payloadOpen = config.getPayloadOpen();
        String payloadClose = config.getPayloadClose();
        String payloadStop = config.getPayloadStop();
        onOffValue = new OnOffValue(config.getStateOpen(), config.getStateClosed(), payloadOpen, payloadClose);
        positionValue = new PercentageValue(BigDecimal.valueOf(config.getPositionClosed()),
                BigDecimal.valueOf(config.getPositionOpen()), null, null, null, FORMAT_INTEGER);

        boolean reportsPosition = config.reportsPosition();
        if (reportsPosition) {
            buildChannel(VALVE_CHANNEL_ID, ComponentChannelType.DIMMER, positionValue, "Valve", this)
                    .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(),
                            config.getCommandTemplate())
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
        } else {
            buildChannel(VALVE_CHANNEL_ID, ComponentChannelType.SWITCH, onOffValue, "Valve", this)
                    .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(),
                            config.getCommandTemplate())
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
        }

        Map<String, String> commandValues = new HashMap<>();
        addCommandValue(commandValues, PAYLOAD_OPEN, payloadOpen);
        addCommandValue(commandValues, PAYLOAD_CLOSE, payloadClose);
        addCommandValue(commandValues, PAYLOAD_STOP, payloadStop);

        Map<String, String> stateValues = new HashMap<>();
        addCommandValue(stateValues, config.getStateOpen(), STATE_OPEN);
        addCommandValue(stateValues, config.getStateOpening(), STATE_OPENING);
        addCommandValue(stateValues, config.getStateClosed(), STATE_CLOSED);
        addCommandValue(stateValues, config.getStateClosing(), STATE_CLOSING);
        stateValue = new TextValue(stateValues, commandValues, STATE_LABELS, COMMAND_LABELS);

        final var rawStateChannel = buildChannel(RAW_STATE_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(),
                "State", this).stateTopic(config.getStateTopic(), config.getValueTemplate())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(), config.getCommandTemplate())
                .build(false);
        hiddenChannels.add(rawStateChannel);

        buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING, stateValue, "State", this)
                .withAutoUpdatePolicy(autoUpdatePolicy).isAdvanced(true).commandFilter(command -> {
                    // OPEN and CLOSE need to be sent as 0/100 for positional valves
                    if (reportsPosition && command instanceof StringType commandStr) {
                        if (commandStr.toString().equals(payloadOpen)) {
                            command = PercentType.HUNDRED;
                        } else if (commandStr.toString().equals(payloadClose)) {
                            command = PercentType.ZERO;
                        }
                    }
                    rawStateChannel.getState().publishValue(command);
                    return false;
                }).build();

        finalizeChannels();
    }

    private void addCommandValue(Map<String, String> commandValues, @Nullable String key, @Nullable String value) {
        if (key != null && value != null) {
            commandValues.put(key, value);
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
        Map<String, String> states = stateValue.getStates();

        @Nullable
        String statePayload = state.toString();
        Map<String, String> json = toJson(statePayload);
        // have JSON; we can clearly know what's state and what's position
        if (json != null) {
            statePayload = json.get(STATE_KEY);
            String positionPayload = json.get(POSITION_KEY);
            if (config.reportsPosition() && positionPayload == null) {
                logger.warn("Missing required `position` attribute in json payload on topic '{}'",
                        config.getStateTopic());
                return;
            }
            if (!config.reportsPosition() && statePayload == null) {
                logger.warn("Missing required `state` attribute in json payload on topic '{}'", config.getStateTopic());
                return;
            }

            // We have both state and position; no need to guess anything
            if (config.reportsPosition()) {
                if (statePayload != null) {
                    if (states != null && !states.containsKey(statePayload)) {
                        logger.warn("Invalid state '{}' for {}", statePayload, getHaID().toShortTopic());
                    } else {
                        if (states != null) {
                            statePayload = Objects.requireNonNull(states.get(statePayload));
                        }
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
                                config.getStateTopic());
                    }

                    return;
                } else {
                    statePayload = positionPayload;
                }
            }
        }

        statePayload = Objects.requireNonNull(statePayload);
        if (states != null && states.containsKey(statePayload)) {
            if (config.reportsPosition()) {
                if (statePayload.equals(config.getStateClosed())) {
                    positionValue.update(PercentType.ZERO);
                    channelStateUpdateListener.updateChannelState(buildChannelUID(VALVE_CHANNEL_ID),
                            positionValue.getChannelState());
                } else if (statePayload.equals(config.getStateOpen())) {
                    positionValue.update(PercentType.HUNDRED);
                    channelStateUpdateListener.updateChannelState(buildChannelUID(VALVE_CHANNEL_ID),
                            positionValue.getChannelState());
                }
            } else {
                if (statePayload.equals(config.getStateClosed())) {
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

        if (config.reportsPosition()) {
            // The state isn't a given state; it must be a number
            try {
                positionValue.update((State) positionValue.parseMessage(DecimalType.valueOf(statePayload)));
            } catch (IllegalArgumentException e) {
                logger.warn("Ignoring non numeric payload '{}' received on topic '{}'", statePayload,
                        config.getStateTopic());
                return;
            }
            channelStateUpdateListener.updateChannelState(buildChannelUID(VALVE_CHANNEL_ID),
                    positionValue.getChannelState());
            if (positionValue.getChannelState().equals(PercentType.ZERO)) {
                stateValue.update(new StringType(config.getStateClosed()));
                channelStateUpdateListener.updateChannelState(buildChannelUID(STATE_CHANNEL_ID),
                        stateValue.getChannelState());
            } else if (positionValue.getChannelState().equals(PercentType.HUNDRED)
                    || stateValue.getChannelState().toString().equals(config.getStateClosed())) {
                // Specifically set up to _not_ overwrite "opening" or "closing", but to set to "open"
                // if we're full-open, or if it was previously "closed"
                stateValue.update(new StringType(config.getStateOpen()));
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
