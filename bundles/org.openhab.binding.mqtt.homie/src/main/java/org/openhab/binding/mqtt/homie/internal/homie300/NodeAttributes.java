/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Homie 3.x Node attributes
 *
 * @author David Graeff - Initial contribution
 */
@TopicPrefix
@NonNullByDefault
public class NodeAttributes extends AbstractMqttAttributeClass {
    public @MandatoryField String name = "";
    public @MandatoryField @MQTTvalueTransform(splitCharacter = ",") String @Nullable [] properties;
    // Type has no meaning yet and is currently purely of textual, descriptive nature
    public @Nullable String type;

    @Override
    public Object getFieldsOf() {
        return this;
    }
}
