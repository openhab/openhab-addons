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
package org.openhab.binding.webthing.internal.link;

import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webthing.internal.ChannelHandler;
import org.openhab.binding.webthing.internal.client.ConsumedThing;
import org.openhab.core.thing.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PropertyToChannelLink} represents a downstream link from a WebThing property to a Channel.
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class PropertyToChannelLink implements BiConsumer<String, Object> {
    private final Logger logger = LoggerFactory.getLogger(PropertyToChannelLink.class);
    private final ChannelHandler channelHandler;
    private final Channel channel;
    private final TypeConverter typeConverter;

    /**
     * establish downstream link from a WebTHing property to a Channel
     *
     * @param webThing the WebThing to be linked
     * @param propertyName the property name
     * @param channelHandler the channel handler that provides updating the Item state of a channel
     * @param channel the channel to be linked
     * @throws UnknownPropertyException if the a WebThing property should be link that does not exist
     */
    public static void establish(ConsumedThing webThing, String propertyName, ChannelHandler channelHandler,
            Channel channel) throws UnknownPropertyException {
        new PropertyToChannelLink(webThing, propertyName, channelHandler, channel);
    }

    private PropertyToChannelLink(ConsumedThing webThing, String propertyName, ChannelHandler channelHandler,
            Channel channel) throws UnknownPropertyException {
        this.channel = channel;
        var optionalProperty = webThing.getThingDescription().getProperty(propertyName);
        if (optionalProperty.isPresent()) {
            var propertyType = optionalProperty.get().type;
            var acceptedType = channel.getAcceptedItemType();
            if (acceptedType == null) {
                this.typeConverter = TypeConverters.create("String", propertyType);
            } else {
                this.typeConverter = TypeConverters.create(acceptedType, propertyType);
            }
            this.channelHandler = channelHandler;
            webThing.observeProperty(propertyName, this);
        } else {
            throw new UnknownPropertyException("property " + propertyName + " does not exits");
        }
    }

    @Override
    public void accept(String propertyName, Object value) {
        var stateCommand = typeConverter.toStateCommand(value);
        channelHandler.updateItemState(channel.getUID(), stateCommand);
        logger.debug("channel {} updated with {} ({})", channel.getUID().getAsString(), value,
                channel.getAcceptedItemType());
    }
}
