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
        protected String payloadOpen = "OPEN";
        @SerializedName("payload_close")
        protected String payloadClose = "CLOSE";
        @SerializedName("payload_stop")
        protected String payloadStop = "STOP";
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
        protected String stateClosed = "closed";
        @SerializedName("state_closing")
        protected String stateClosing = "closing";
        @SerializedName("state_open")
        protected String stateOpen = "open";
        @SerializedName("state_opening")
        protected String stateOpening = "opening";
        @SerializedName("state_stopped")
        protected String stateStopped = "stopped";
    }

    @Nullable
    ComponentChannel stateChannel = null;

    public Cover(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

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
            TextValue value = new TextValue(new String[] { channelConfiguration.stateClosed,
                    channelConfiguration.stateClosing, channelConfiguration.stateOpen,
                    channelConfiguration.stateOpening, channelConfiguration.stateStopped });
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
