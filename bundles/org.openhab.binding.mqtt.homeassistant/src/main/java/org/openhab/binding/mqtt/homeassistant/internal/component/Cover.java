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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.values.RollershutterValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.type.AutoUpdatePolicy;

/**
 * A MQTT Cover component, following the https://www.home-assistant.io/integrations/cover.mqtt specification.
 *
 * Supports reporting state and/or position, and commanding OPEN/CLOSE/STOP
 * 
 * Does not yet support tilt or covers that don't go from 0-100.
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Add support for position and discrete state strings
 */
@NonNullByDefault
public class Cover extends AbstractComponent<Cover.Configuration> {
    public static final String COVER_CHANNEL_ID = "cover";
    public static final String STATE_CHANNEL_ID = "state";

    public static final String PAYLOAD_OPEN = "OPEN";
    public static final String PAYLOAD_CLOSE = "CLOSE";
    public static final String PAYLOAD_STOP = "STOP";

    public static final String STATE_CLOSED = "closed";
    public static final String STATE_CLOSING = "closing";
    public static final String STATE_OPEN = "open";
    public static final String STATE_OPENING = "opening";
    public static final String STATE_STOPPED = "stopped";

    private static final Map<String, String> STATE_LABELS = Map.of(STATE_CLOSED, "@text/state.cover.closed",
            STATE_CLOSING, "@text/state.cover.closing", STATE_OPEN, "@text/state.cover.open", STATE_OPENING,
            "@text/state.cover.opening", STATE_STOPPED, "@text/state.cover.stopped");

    public static class Configuration extends EntityConfiguration {
        private final boolean retain, optimistic;

        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Cover");
            retain = getBoolean("retain");
            optimistic = getBoolean("optimistic");
        }

        @Nullable
        String getCommandTopic() {
            return getOptionalString("command_topic");
        }

        @Nullable
        String getPositionTopic() {
            return getOptionalString("position_topic");
        }

        boolean isOptimistic() {
            return optimistic;
        }

        String getPayloadClose() {
            return getString("payload_close");
        }

        String getPayloadOpen() {
            return getString("payload_open");
        }

        String getPayloadStop() {
            return getString("payload_stop");
        }

        int getPositionClosed() {
            return getInt("position_closed");
        }

        int getPositionOpen() {
            return getInt("position_open");
        }

        boolean isRetain() {
            return retain;
        }

        @Nullable
        Value getSetPositionTemplate() {
            return getOptionalValue("set_position_template");
        }

        @Nullable
        String getSetPositionTopic() {
            return getOptionalString("set_position_topic");
        }

        String getStateClosed() {
            return getString("state_closed");
        }

        String getStateClosing() {
            return getString("state_closing");
        }

        String getStateOpen() {
            return getString("state_open");
        }

        String getStateOpening() {
            return getString("state_opening");
        }

        String getStateStopped() {
            return getString("state_stopped");
        }

        @Nullable
        String getStateTopic() {
            return getOptionalString("state_topic");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }

        @Nullable
        Value getPositionTemplate() {
            return getOptionalValue("position_template");
        }
    }

    public Cover(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        boolean optimistic = false;
        String stateTopic = config.getStateTopic();
        String positionTopic = config.getPositionTopic();
        if (stateTopic == null && positionTopic == null) {
            optimistic = true;
        }

        // State can indicate additional information than just
        // the current position, so expose it as a separate channel
        if (stateTopic != null) {
            Map<String, String> states = new LinkedHashMap<>();
            states.put(config.getStateClosed(), STATE_CLOSED);
            states.put(config.getStateClosing(), STATE_CLOSING);
            states.put(config.getStateOpen(), STATE_OPEN);
            states.put(config.getStateOpening(), STATE_OPENING);
            states.put(config.getStateStopped(), STATE_STOPPED);
            TextValue value = new TextValue(states, Map.of(), STATE_LABELS, Map.of());
            buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING, value, "State",
                    componentContext.getUpdateListener()).stateTopic(stateTopic).isAdvanced(true).build();
        }

        String commandTopic = config.getCommandTopic();
        String payloadOpen = config.getPayloadOpen();
        String payloadClose = config.getPayloadClose();
        ComponentChannel stateChannel;
        if (commandTopic != null) {
            hiddenChannels.add(stateChannel = buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(), "State", componentContext.getUpdateListener())
                    .commandTopic(commandTopic, config.isRetain(), config.getQos()).build(false));
        } else {
            stateChannel = null;
            // no command topic. we need to make sure we send
            // integers for open and close
            payloadOpen = String.valueOf(config.getPositionOpen());
            payloadClose = String.valueOf(config.getPositionClosed());
        }

        // We will either have positionTopic or stateTopic.
        // positionTopic is more useful, but if we only have stateTopic,
        // still build a Rollershutter channel so that UP/DOWN/STOP
        // commands can be sent
        String rollershutterStateTopic = config.getPositionTopic();
        Value stateTemplate = config.getPositionTemplate();
        if (rollershutterStateTopic == null) {
            rollershutterStateTopic = stateTopic;
            stateTemplate = config.getValueTemplate();
        }
        String rollershutterCommandTopic = config.getSetPositionTopic();
        if (rollershutterCommandTopic == null) {
            rollershutterCommandTopic = config.getCommandTopic();
        }

        boolean inverted = config.getPositionOpen() > config.getPositionClosed();
        final RollershutterValue value = new RollershutterValue(payloadOpen, payloadClose, config.getPayloadStop(),
                config.getStateOpen(), config.getStateClosed(), inverted, config.getSetPositionTopic() == null);

        buildChannel(COVER_CHANNEL_ID, ComponentChannelType.ROLLERSHUTTER, value, "Cover",
                componentContext.getUpdateListener()).stateTopic(rollershutterStateTopic, stateTemplate)
                .commandTopic(rollershutterCommandTopic, config.isRetain(), config.getQos()).commandFilter(command -> {
                    if (stateChannel == null) {
                        return true;
                    }
                    // If we have a state channel, and this is UP/DOWN/STOP, then
                    // we need to send the command to _that_ channel's topic, not
                    // the position topic.
                    if (command instanceof UpDownType || command instanceof StopMoveType) {
                        command = new StringType(value.getMQTTpublishValue(command, false));
                        stateChannel.getState().publishValue(command);
                        return false;
                    }
                    return true;
                }).withAutoUpdatePolicy(optimistic ? AutoUpdatePolicy.RECOMMEND : null).build();

        finalizeChannels();
    }
}
