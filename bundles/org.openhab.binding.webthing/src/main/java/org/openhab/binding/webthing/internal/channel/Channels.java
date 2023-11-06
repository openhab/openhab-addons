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
package org.openhab.binding.webthing.internal.channel;

import static org.openhab.binding.webthing.internal.WebThingBindingConstants.BINDING_ID;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webthing.internal.client.dto.Property;
import org.openhab.binding.webthing.internal.link.TypeMapping;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link Channels} class is an utility class to create Channel based on the property characteristics as
 * well as ChannelUID identifier
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class Channels {

    /**
     * create a ChannelUIFD identifier for a given property name
     *
     * @param thingUID the thing uid of the associated WebThing
     * @param propertyName the property name
     * @return the ChannelUID identifier
     */
    public static ChannelUID createChannelUID(ThingUID thingUID, String propertyName) {
        return new ChannelUID(thingUID.toString() + ":" + propertyName);
    }

    /**
     * create a Channel base on a given WebThing property
     *
     * @param thingUID the thing uid of the associated WebThing
     * @param propertyName the property name
     * @param property the WebThing property
     * @return the Channel according to the properties characteristics
     */
    public static Channel createChannel(ThingUID thingUID, String propertyName, Property property) {
        var itemType = TypeMapping.toItemType(property);
        var channelUID = createChannelUID(thingUID, propertyName);
        var channelBuilder = ChannelBuilder.create(channelUID, itemType.getType());

        // Currently, few predefined, generic channel types such as number, string or color are defined
        // inside the thing-types.xml file. A better solution would be to create the channel types
        // dynamically based on the WebThing description to make most of the meta data of a WebThing.
        // The goal of the WebThing meta data is to enable semantic interoperability for connected things
        channelBuilder.withType(new ChannelTypeUID(BINDING_ID, itemType.getType().toLowerCase()));
        channelBuilder.withDescription(property.description);
        channelBuilder.withLabel(property.title);
        Set<String> defaultTags = itemType.getTags();
        if (!defaultTags.isEmpty()) {
            channelBuilder.withDefaultTags(defaultTags);
        }
        return channelBuilder.build();
    }
}
