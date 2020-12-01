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
package org.openhab.binding.sony.internal.scalarweb;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * This class represents channel descriptor. Channel descriptors will describe the definition of a channel that will be
 * created in {@link ScalarWebHandler}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebChannelDescriptor {

    /** The label for the channel */
    private final @Nullable String label;

    /** The description of the channel */
    private final @Nullable String description;

    /** The accepted item type */
    private final String acceptedItemType;

    /** The channel type */
    private final String channelType;

    /** The underlying channel */
    private final ScalarWebChannel channel;

    /**
     * Instantiates a new scalar web channel descriptor.
     *
     * @param channel the non-null underlying channel
     * @param acceptedItemType the non-null, non-empty accepted item type
     * @param channelType the non-null, non-empty channel type
     * @param label the potentially null, potentially empty label
     * @param description the potentially null, potentially empty description
     */
    public ScalarWebChannelDescriptor(final ScalarWebChannel channel, final String acceptedItemType,
            final String channelType, final @Nullable String label, final @Nullable String description) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Validate.notEmpty(acceptedItemType, "acceptedItemType cannot be empty");
        Validate.notEmpty(channelType, "channelType cannot be empty");
        this.channel = channel;
        this.channelType = channelType;
        this.acceptedItemType = acceptedItemType;
        this.label = label;
        this.description = description;
    }

    /**
     * Returns the {@link ScalarWebChannel} from the descriptor
     * 
     * @return a non-null ScalarWebChannel
     */
    public ScalarWebChannel getChannel() {
        return channel;
    }

    /**
     * Creates the channel builder from the descriptor
     *
     * @param thingUid the non-null thing uid to use
     * @return the channel builder
     */
    public ChannelBuilder createChannel(final ThingUID thingUid) {
        Objects.requireNonNull(thingUid, "thingUid canot be null");

        ChannelBuilder channelBuilder = ChannelBuilder.create(new ChannelUID(thingUid, channel.getChannelId()),
                acceptedItemType);

        final ChannelTypeUID stateTypeUid = new ChannelTypeUID(ScalarWebConstants.THING_TYPE_SCALAR.getBindingId(),
                channelType);
        channelBuilder = channelBuilder.withType(stateTypeUid);

        final String localLabel = label;
        if (localLabel != null && StringUtils.isNotEmpty(localLabel)) {
            channelBuilder = channelBuilder.withLabel(localLabel);
        }

        final String localDesc = description;
        if (localDesc != null && StringUtils.isNotEmpty(localDesc)) {
            channelBuilder = channelBuilder.withDescription(localDesc);
        }

        channelBuilder.withProperties(channel.getProperties());

        return channelBuilder;
    }

    @Override
    public String toString() {
        return channel + " accepting " + acceptedItemType + " of type " + channelType;
    }
}
