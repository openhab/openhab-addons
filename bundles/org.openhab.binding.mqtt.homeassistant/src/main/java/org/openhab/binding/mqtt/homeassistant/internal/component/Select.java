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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.RWConfiguration;

/**
 * A MQTT select, following the https://www.home-assistant.io/components/select.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Select extends AbstractComponent<Select.Configuration> {
    public static final String SELECT_CHANNEL_ID = "select";

    public static class Configuration extends EntityConfiguration implements RWConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Select");
        }

        @Nullable
        Value getCommandTemplate() {
            return getOptionalValue("command_template");
        }

        List<String> getOptions() {
            return getStringList("options");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }
    }

    public Select(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        TextValue value = new TextValue(config.getOptions().toArray(new String[0]));

        buildChannel(SELECT_CHANNEL_ID, ComponentChannelType.STRING, value, "Select",
                componentContext.getUpdateListener()).stateTopic(config.getStateTopic(), config.getValueTemplate())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(), config.getCommandTemplate())
                .inferOptimistic(config.isOptimistic()).build();

        finalizeChannels();
    }
}
