/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.internal.convention.homie300;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.internal.mapping.MQTTvalueTransform;
import org.openhab.binding.mqtt.generic.internal.mapping.MandatoryField;
import org.openhab.binding.mqtt.generic.internal.mapping.TopicPrefix;

/**
 * Homie 3.x Node attributes
 *
 * @author David Graeff - Initial contribution
 */
@TopicPrefix
public class NodeAttributes extends AbstractMqttAttributeClass {
    public @MandatoryField String name;
    public @MandatoryField @MQTTvalueTransform(splitCharacter = ",") String[] properties;
    // Type has no meaning for ESH yet and is currently purely of textual, descriptive nature
    public String type;

    @Override
    public @NonNull Object getFieldsOf() {
        return this;
    }
}
