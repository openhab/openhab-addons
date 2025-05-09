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

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractComponentConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;

/**
 * A MQTT Device Trigger, following the https://www.home-assistant.io/integrations/device_trigger.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class DeviceTrigger extends AbstractComponent<DeviceTrigger.Configuration> {
    public static class Configuration extends AbstractComponentConfiguration {
        private final String subtype, topic, type;
        private final @Nullable String payload;
        private final @Nullable Value valueTemplate;

        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "Device Trigger"); // Technically device triggers don't have a name
            subtype = getString("subtype");
            topic = getString("topic");
            type = getString("type");
            payload = getOptionalString("payload");
            valueTemplate = getOptionalValue("value_template");
        }

        String getAutomationType() {
            return getString("automation_type");
        }

        @Nullable
        String getPayload() {
            return payload;
        }

        String getSubtype() {
            return subtype;
        }

        String getTopic() {
            return topic;
        }

        String getType() {
            return type;
        }

        @Nullable
        Value getValueTemplate() {
            return valueTemplate;
        }
    }

    public DeviceTrigger(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        if (!"trigger".equals(config.getAutomationType())) {
            throw new ConfigurationException("Component:DeviceTrigger must have automation_type 'trigger'");
        }

        // Name the channel after the subtype, not the component ID
        // So that we only end up with a single channel for all possible events
        // for a single button (subtype is the button, type is type of press)
        componentId = config.getSubtype();
        groupId = null;

        TextValue value;
        String payload = config.getPayload();
        if (payload != null) {
            value = new TextValue(new String[] { payload });
        } else {
            value = new TextValue();
        }

        buildChannel(config.getType(), ComponentChannelType.TRIGGER, value, "Trigger",
                componentContext.getUpdateListener()).stateTopic(config.getTopic(), config.getValueTemplate())
                .trigger(true).build();
    }

    @Override
    public boolean mergeable(AbstractComponent<?> other) {
        if (other instanceof DeviceTrigger newTrigger && newTrigger.getConfig().getSubtype().equals(config.getSubtype())
                && newTrigger.getConfig().getTopic().equals(config.getTopic())
                && getHaID().nodeID.equals(newTrigger.getHaID().nodeID)) {
            Value newTriggerValueTemplate = newTrigger.getConfig().getValueTemplate();
            Value oldTriggerValueTemplate = config.getValueTemplate();
            if ((newTriggerValueTemplate == null && oldTriggerValueTemplate == null) || (newTriggerValueTemplate != null
                    && oldTriggerValueTemplate != null && newTriggerValueTemplate.equals(oldTriggerValueTemplate))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Take another DeviceTrigger (presumably whose subtype, topic, and value template match),
     * and adjust this component's channel to accept the payload that trigger allows.
     * 
     * @return if the component was stopped, and thus needs restarted
     */
    @Override
    public boolean merge(AbstractComponent<?> other) {
        DeviceTrigger newTrigger = (DeviceTrigger) other;
        ComponentChannel channel = Objects.requireNonNull(channels.get(componentId));
        org.openhab.core.config.core.Configuration newConfiguration = mergeChannelConfiguration(channel, newTrigger);

        TextValue value = (TextValue) channel.getState().getCache();
        Map<String, String> payloads = value.getStates();

        // Append payload to allowed values
        String otherPayload = newTrigger.getConfig().getPayload();
        if (payloads == null || otherPayload == null) {
            // Need to accept anything
            value = new TextValue();
        } else {
            String[] newValues = Stream.concat(payloads.keySet().stream(), Stream.of(otherPayload)).distinct()
                    .toArray(String[]::new);
            value = new TextValue(newValues);
        }

        // Recreate the channel
        stop();
        buildChannel(config.getType(), ComponentChannelType.TRIGGER, value, componentId,
                componentContext.getUpdateListener()).withConfiguration(newConfiguration)
                .stateTopic(config.getTopic(), config.getValueTemplate()).trigger(true).build();
        return true;
    }
}
