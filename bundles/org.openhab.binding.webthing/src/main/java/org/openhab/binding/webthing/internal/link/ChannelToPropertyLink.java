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
package org.openhab.binding.webthing.internal.link;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webthing.internal.ChannelHandler;
import org.openhab.binding.webthing.internal.WebThingHandler;
import org.openhab.binding.webthing.internal.client.ConsumedThing;
import org.openhab.binding.webthing.internal.client.PropertyAccessException;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChannelToPropertyLink} represents an upstream link from a Channel to a WebThing property.
 * This link is used to update a the value of a property
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class ChannelToPropertyLink implements WebThingHandler.ItemChangedListener {
    private final Logger logger = LoggerFactory.getLogger(ChannelToPropertyLink.class);
    private final String propertyName;
    private final String propertyType;
    private final ConsumedThing webThing;
    private final TypeConverter typeConverter;

    /**
     * establish a upstream link from a Channel to a WebThing property
     *
     * @param channelHandler the channel handler that provides registering an ItemChangedListener
     * @param channel the channel to be linked
     * @param webthing the WebThing to be linked
     * @param propertyName the property name
     * @throws UnknownPropertyException if the a WebThing property should be link that does not exist
     */
    public static void establish(ChannelHandler channelHandler, Channel channel, ConsumedThing webthing,
            String propertyName) throws UnknownPropertyException {
        new ChannelToPropertyLink(channelHandler, channel, webthing, propertyName);
    }

    private ChannelToPropertyLink(ChannelHandler channelHandler, Channel channel, ConsumedThing webThing,
            String propertyName) throws UnknownPropertyException {
        this.webThing = webThing;
        var optionalProperty = webThing.getThingDescription().getProperty(propertyName);
        if (optionalProperty.isPresent()) {
            this.propertyType = optionalProperty.get().type;
            var acceptedType = channel.getAcceptedItemType();
            if (acceptedType == null) {
                this.typeConverter = TypeConverters.create("String", propertyType);
            } else {
                this.typeConverter = TypeConverters.create(acceptedType, propertyType);
            }
            this.propertyName = propertyName;
            channelHandler.observeChannel(channel.getUID(), this);
        } else {
            throw new UnknownPropertyException("property " + propertyName + " does not exits");
        }
    }

    @Override
    public void onItemStateChanged(ChannelUID channelUID, State stateCommand) {
        try {
            var propertyValue = typeConverter.toPropertyValue(stateCommand);
            webThing.writeProperty(propertyName, typeConverter.toPropertyValue((State) stateCommand));
            logger.debug("property {} updated with {} ({}) ", propertyName, propertyValue, this.propertyType);
        } catch (PropertyAccessException pae) {
            logger.warn("could not write WebThing property {} with new channel value. {}", propertyName,
                    pae.getMessage());
        }
    }
}
