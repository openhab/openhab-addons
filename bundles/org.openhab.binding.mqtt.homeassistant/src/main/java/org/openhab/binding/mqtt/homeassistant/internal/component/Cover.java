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
import org.openhab.binding.mqtt.generic.values.RollershutterValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.type.AutoUpdatePolicy;

import com.google.gson.annotations.SerializedName;

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
public class Cover extends AbstractComponent<Cover.ChannelConfiguration> {
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

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Cover");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("state_topic")
        protected @Nullable String stateTopic;
        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("payload_open")
        protected String payloadOpen = PAYLOAD_OPEN;
        @SerializedName("payload_close")
        protected String payloadClose = PAYLOAD_CLOSE;
        @SerializedName("payload_stop")
        protected String payloadStop = PAYLOAD_STOP;
        @SerializedName("position_closed")
        protected int positionClosed = 0;
        @SerializedName("position_open")
        protected int positionOpen = 100;
        @SerializedName("position_template")
        protected @Nullable String positionTemplate;
        @SerializedName("position_topic")
        protected @Nullable String positionTopic;
        @SerializedName("set_position_template")
        protected @Nullable String setPositionTemplate;
        @SerializedName("set_position_topic")
        protected @Nullable String setPositionTopic;
        @SerializedName("state_closed")
        protected String stateClosed = STATE_CLOSED;
        @SerializedName("state_closing")
        protected String stateClosing = STATE_CLOSING;
        @SerializedName("state_open")
        protected String stateOpen = STATE_OPEN;
        @SerializedName("state_opening")
        protected String stateOpening = STATE_OPENING;
        @SerializedName("state_stopped")
        protected String stateStopped = STATE_STOPPED;
    }

    @Nullable
    ComponentChannel stateChannel = null;

    public Cover(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        boolean optimistic = false;
        Boolean localOptimistic = channelConfiguration.optimistic;
        if (localOptimistic != null && localOptimistic == true
                || channelConfiguration.stateTopic == null && channelConfiguration.positionTopic == null) {
            optimistic = true;
        }
        String stateTopic = channelConfiguration.stateTopic;

        // State can indicate additional information than just
        // the current position, so expose it as a separate channel
        if (stateTopic != null) {
            Map<String, String> states = new LinkedHashMap<>();
            states.put(channelConfiguration.stateClosed, STATE_CLOSED);
            states.put(channelConfiguration.stateClosing, STATE_CLOSING);
            states.put(channelConfiguration.stateOpen, STATE_OPEN);
            states.put(channelConfiguration.stateOpening, STATE_OPENING);
            states.put(channelConfiguration.stateStopped, STATE_STOPPED);
            TextValue value = new TextValue(states, Map.of(), STATE_LABELS, Map.of());
            buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING, value, "State",
                    componentConfiguration.getUpdateListener()).stateTopic(stateTopic).isAdvanced(true).build();
        }

        if (channelConfiguration.commandTopic != null) {
            hiddenChannels.add(stateChannel = buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(), "State", componentConfiguration.getUpdateListener())
                    .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .build(false));
        } else {
            // no command topic. we need to make sure we send
            // integers for open and close
            channelConfiguration.payloadOpen = String.valueOf(channelConfiguration.positionOpen);
            channelConfiguration.payloadClose = String.valueOf(channelConfiguration.positionClosed);
        }

        // We will either have positionTopic or stateTopic.
        // positionTopic is more useful, but if we only have stateTopic,
        // still build a Rollershutter channel so that UP/DOWN/STOP
        // commands can be sent
        String rollershutterStateTopic = channelConfiguration.positionTopic;
        String stateTemplate = channelConfiguration.positionTemplate;
        if (rollershutterStateTopic == null) {
            rollershutterStateTopic = stateTopic;
            stateTemplate = channelConfiguration.getValueTemplate();
        }
        String rollershutterCommandTopic = channelConfiguration.setPositionTopic;
        if (rollershutterCommandTopic == null) {
            rollershutterCommandTopic = channelConfiguration.commandTopic;
        }

        boolean inverted = channelConfiguration.positionOpen > channelConfiguration.positionClosed;
        final RollershutterValue value = new RollershutterValue(channelConfiguration.payloadOpen,
                channelConfiguration.payloadClose, channelConfiguration.payloadStop, channelConfiguration.stateOpen,
                channelConfiguration.stateClosed, inverted, channelConfiguration.setPositionTopic == null);

        buildChannel(COVER_CHANNEL_ID, ComponentChannelType.ROLLERSHUTTER, value, "Cover",
                componentConfiguration.getUpdateListener()).stateTopic(rollershutterStateTopic, stateTemplate)
                .commandTopic(rollershutterCommandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos())
                .commandFilter(command -> {
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
