/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homie.internal.homie300;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.mapping.MQTTvalueTransform;
import org.openhab.binding.mqtt.generic.mapping.TopicPrefix;

/**
 * Homie 3.x Property attributes
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@TopicPrefix
public class PropertyAttributes extends AbstractMqttAttributeClass {
    // Lower-case enum value names required. Those are identifiers for the MQTT/homie protocol.
    public enum DataTypeEnum {
        unknown,
        integer_,
        float_,
        boolean_,
        string_,
        enum_,
        color_,
        datetime_
    }

    public String name = "";

    /**
     * stateful + non-settable: The node publishes a property state (temperature sensor)
     * stateful + settable: The node publishes a property state, and can receive commands for the property (by
     * controller or other party) (lamp power)
     * stateless + non-settable: The node publishes momentary events (door bell pressed)
     * stateless + settable: The node publishes momentary events, and can receive commands for the property (by
     * controller or other party) (brew coffee)
     */
    public boolean settable = false;
    public boolean retained = true;
    public String unit = "";
    public @MQTTvalueTransform(suffix = "_") DataTypeEnum datatype = DataTypeEnum.unknown;
    public String format = "";

    @Override
    public Object getFieldsOf() {
        return this;
    }

    /**
     * Return a map with all field values.
     */
    public Map<String, Object> asMap() {
        Map<String, Object> properties = new TreeMap<>();
        properties.put("unit", unit);
        properties.put("name", name);
        properties.put("settable", settable ? "true" : "false");
        properties.put("retained", retained ? "true" : "false");
        properties.put("format", format);
        properties.put("datatype", datatype.name());
        return properties;
    }
}
