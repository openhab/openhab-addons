/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthing.internal.link;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webthing.internal.client.ConsumedThing;
import org.openhab.binding.webthing.internal.client.dto.Property;
import org.openhab.core.thing.Channel;

/**
 * Implementation base of a link between a WebThing Property and a channel
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
class AbstractLink {
    protected final ConsumedThing webThing;
    protected final Property property;
    protected final Channel channel;
    protected final TypeConverter typeConverter;

    /**
     * constructor
     *
     * @param webThing the WebThing
     * @param propertyName the property name to be linked
     * @param channel the channel to be linked
     */
    protected AbstractLink(ConsumedThing webThing, String propertyName, Channel channel) {
        this.webThing = webThing;
        this.channel = channel;
        var itemType = Optional.ofNullable(channel.getAcceptedItemType()).orElse("String");
        
        if (!webThing.getThingDescription().properties.containsKey(propertyName)) {
            throw new RuntimeException("property " + propertyName + " does not exist");
        }
        this.property = webThing.getThingDescription().properties.get(propertyName);
        this.typeConverter = TypeConverters.create(itemType, property.type);
    }
}
