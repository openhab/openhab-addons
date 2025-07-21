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

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.RWConfiguration;
import org.openhab.core.types.util.UnitUtils;

/**
 * A MQTT Number, following the https://www.home-assistant.io/components/number.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Number extends AbstractComponent<Number.Configuration> {
    public static final String NUMBER_CHANNEL_ID = "number"; // Randomly chosen channel "ID"

    public static class Configuration extends EntityConfiguration implements RWConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Number");
        }

        @Nullable
        Value getCommandTemplate() {
            return getOptionalValue("command_template");
        }

        double getMin() {
            return getDouble("min");
        }

        double getMax() {
            return getDouble("max");
        }

        String getPayloadReset() {
            return getString("payload_reset");
        }

        double getStep() {
            return getDouble("step");
        }

        @Nullable
        String getUnitOfMeasurement() {
            return getOptionalString("unit_of_measurement");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }
    }

    public Number(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        NumberValue value = new NumberValue(BigDecimal.valueOf(config.getMin()), BigDecimal.valueOf(config.getMax()),
                BigDecimal.valueOf(config.getStep()), UnitUtils.parseUnit(config.getUnitOfMeasurement()));

        buildChannel(NUMBER_CHANNEL_ID, ComponentChannelType.NUMBER, value, "Number",
                componentContext.getUpdateListener()).stateTopic(config.getStateTopic(), config.getValueTemplate())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(), config.getCommandTemplate())
                .inferOptimistic(config.isOptimistic()).build();

        finalizeChannels();
    }
}
