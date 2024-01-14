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
package org.openhab.binding.mqtt.homie.internal.homie300;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.mapping.MQTTvalueTransform;
import org.openhab.binding.mqtt.generic.mapping.MandatoryField;
import org.openhab.binding.mqtt.generic.mapping.TopicPrefix;

/**
 * Homie 3.x Device attributes
 *
 * @author David Graeff - Initial contribution
 */
@TopicPrefix
@NonNullByDefault
public class DeviceAttributes extends AbstractMqttAttributeClass {
    // Lower-case enum value names required. Those are identifiers for the MQTT/homie protocol.
    public enum ReadyState {
        unknown,
        init,
        ready,
        disconnected,
        sleeping,
        lost,
        alert
    }

    public @MandatoryField @Nullable String homie;
    public @MandatoryField @Nullable String name;
    public @MandatoryField ReadyState state = ReadyState.unknown;
    public @MandatoryField @MQTTvalueTransform(splitCharacter = ",") String @Nullable [] nodes;

    @Override
    public Object getFieldsOf() {
        return this;
    }
}
