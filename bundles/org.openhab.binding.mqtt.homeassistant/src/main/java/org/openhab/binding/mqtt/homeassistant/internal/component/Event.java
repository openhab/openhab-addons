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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantChannelTransformation;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Event, following the https://www.home-assistant.io/integrations/event.mqttspecification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Event extends AbstractComponent<Event.ChannelConfiguration> implements ChannelStateUpdateListener {
    public static final String EVENT_TYPE_CHANNEL_ID = "event-type";
    private static final String EVENT_TYPE_TRANFORMATION = "{{ value_json.event_type }}";

    /**
     * Configuration class for MQTT component
     */
    public static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Event");
        }

        @SerializedName("state_topic")
        protected String stateTopic = "";

        @SerializedName("event_types")
        protected List<String> eventTypes = new ArrayList();
    }

    private final HomeAssistantChannelTransformation transformation;

    public Event(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        transformation = new HomeAssistantChannelTransformation(getJinjava(), this, "");

        buildChannel(EVENT_TYPE_CHANNEL_ID, ComponentChannelType.TRIGGER, new TextValue(), getName(), this)
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate()).trigger(true)
                .build();

        finalizeChannels();
    }

    // Overridden to use create it as a trigger channel
    @Override
    protected void addJsonAttributesChannel() {
        if (channelConfiguration.getJsonAttributesTopic() != null) {
            // It's unclear from the documentation if the JSON attributes value is expected
            // to be the same as the main topic, and thus would always have an event_type
            // attribute (and thus could possibly be shared with multiple components).
            // If that were the case, we would need to intercept events, and check that they
            // had an event_type that is in channelConfiguration.eventTypes. If/when that
            // becomes an issue, change `channelStateUpdateListener` to `this`, and handle
            // the filtering below.
            buildChannel(JSON_ATTRIBUTES_CHANNEL_ID, ComponentChannelType.TRIGGER, new TextValue(), getName(),
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.getJsonAttributesTopic(),
                            channelConfiguration.getJsonAttributesTemplate())
                    .isAdvanced(true).trigger(true).build();
        }
    }

    @Override
    public void triggerChannel(ChannelUID channel, String event) {
        String eventType = transformation.apply(EVENT_TYPE_TRANFORMATION, event).orElse(null);
        if (eventType == null) {
            // Warning logged from inside the transformation
            return;
        }
        // The TextValue allows anything, because it receives the full JSON, and
        // we don't check the actual event_type against valid event_types until here
        if (!channelConfiguration.eventTypes.contains(eventType)) {
            return;
        }

        componentConfiguration.getUpdateListener().triggerChannel(channel, eventType);
    }

    @Override
    public void updateChannelState(ChannelUID channel, State state) {
        // N/A (only trigger channels)
    }

    @Override
    public void postChannelCommand(ChannelUID channel, Command command) {
        // N/A (only trigger channels)
    }
}
