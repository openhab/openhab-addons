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
package org.openhab.binding.networkupstools.internal;

import static org.openhab.binding.networkupstools.internal.NUTBindingConstants.BINDING_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to enrich dynamic created channels with additional configurations.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
class NUTDynamicChannelFactory {

    private static final String QUANTITY_ITEM_TYPE_PREFIX = CoreItemFactory.NUMBER + ':';

    private final Logger logger = LoggerFactory.getLogger(NUTDynamicChannelFactory.class);

    private final NUTChannelTypeProvider channelTypeProvider;

    NUTDynamicChannelFactory(final NUTChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    /**
     * Enriches the channel from the given channel and returns the newly created channel.
     *
     * @param channel Channel to enrich
     * @param channelConfig channel configuration
     * @return new created channel or null if it was not possible to create a new channel due to missing information or
     *         otherwise
     */
    public @Nullable Channel createChannel(final Channel channel, final NUTDynamicChannelConfiguration channelConfig) {
        final String acceptedItemType = channel.getAcceptedItemType();

        if (acceptedItemType == null) {
            return null;
        }
        final ChannelTypeUID channelTypeUID;

        if (acceptedItemType.startsWith(QUANTITY_ITEM_TYPE_PREFIX)) {
            channelTypeUID = createQuantityTypeChannel(channel, acceptedItemType, channelConfig);
        } else {
            channelTypeUID = getChannelTypeUID(acceptedItemType, channel.getUID());
        }
        return channelTypeUID == null ? null : ChannelBuilder.create(channel).withType(channelTypeUID).build();
    }

    /**
     * Returns the {@link ChannelTypeUID} for channels types that are supported and have a channel-type definition in
     * the binding thing xml.
     *
     * @param itemType item type to get the channel type for
     * @param channelUID ChannelUID for which the channel type is determined
     * @return channel type or null if not supported
     */
    private @Nullable ChannelTypeUID getChannelTypeUID(final String itemType, final ChannelUID channelUID) {
        switch (itemType) {
            case CoreItemFactory.NUMBER:
                return NUTBindingConstants.CHANNEL_TYPE_DYNAMIC_NUMBER;
            case CoreItemFactory.STRING:
                return NUTBindingConstants.CHANNEL_TYPE_DYNAMIC_STRING;
            case CoreItemFactory.SWITCH:
                return NUTBindingConstants.CHANNEL_TYPE_DYNAMIC_SWITCH;
            default:
                logger.info("Dynamic channel '{}' is ignored because the type '{}' is not supported.", channelUID,
                        itemType);
                return null;
        }
    }

    /**
     * Creates a new {@link ChannelTypeUID} for dynamically created {@link QuantityType} channels.
     * It registers the new channel type with the channel type provider.
     *
     * @param channel Channel to enrich
     * @param itemType item type to get the channel type for
     * @param channelConfig channel configuration
     * @return channel type or null if not supported
     */
    private @Nullable ChannelTypeUID createQuantityTypeChannel(final Channel channel, final String itemType,
            final NUTDynamicChannelConfiguration channelConfig) {
        if (channelConfig.unit == null || channelConfig.unit.isEmpty()) {
            logger.info("Dynamic Channel '{}' is ignored because it's a QuantityType without a 'unit' property.",
                    channel.getUID());
            return null;
        }
        final StateDescriptionFragmentBuilder sdb = StateDescriptionFragmentBuilder.create();
        final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channel.getUID().getId() + "Type");
        final String label = channel.getLabel();
        final ChannelType channelType = ChannelTypeBuilder.state(channelTypeUID, label == null ? "" : label, itemType)
                .withStateDescriptionFragment(sdb.withReadOnly(Boolean.TRUE).build())
                .withConfigDescriptionURI(NUTBindingConstants.DYNAMIC_CHANNEL_CONFIG_QUANTITY_TYPE).build();
        channelTypeProvider.addChannelType(channelType);
        return channelTypeUID;
    }
}
