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
package org.openhab.binding.mqtt.homeassistant.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The types of HomeAssistant channels components.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public enum ComponentChannelType {
    COLOR("ha-color"),
    DIMMER("ha-dimmer"),
    IMAGE("ha-image"),
    LOCATION("ha-location"),
    NUMBER("ha-number"),
    ROLLERSHUTTER("ha-rollershutter"),
    STRING("ha-string"),
    SWITCH("ha-switch"),
    TRIGGER("ha-trigger"),
    HUMIDITY("ha-humidity"),
    GPS_ACCURACY("ha-gps-accuracy"),
    TEMPERATURE("ha-temperature");

    final ChannelTypeUID channelTypeUID;

    ComponentChannelType(String id) {
        channelTypeUID = new ChannelTypeUID(MqttBindingConstants.BINDING_ID, id);
    }

    public ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }
}
