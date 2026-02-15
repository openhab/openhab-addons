/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeassistant.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The types of HomeAssistant channels components.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public enum ComponentChannelType {
    COLOR("color"),
    DIMMER("dimmer"),
    IMAGE("image"),
    LOCATION("location"),
    NUMBER("number"),
    ROLLERSHUTTER("rollershutter"),
    STRING("string"),
    SWITCH("switch"),
    TRIGGER("trigger"),
    HUMIDITY("humidity"),
    GPS_ACCURACY("gps-accuracy"),
    TEMPERATURE("temperature");

    final ChannelTypeUID channelTypeUID;

    ComponentChannelType(String id) {
        channelTypeUID = new ChannelTypeUID(HomeAssistantBindingConstants.BINDING_ID, id);
    }

    public ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }
}
