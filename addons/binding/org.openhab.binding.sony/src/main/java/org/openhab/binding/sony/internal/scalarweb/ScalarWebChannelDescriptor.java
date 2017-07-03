/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebChannelDescriptor.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebChannelDescriptor {

    /** The label. */
    private final String label;

    /** The description. */
    private final String description;

    /** The accepted item type. */
    private final String acceptedItemType;

    /** The channel type. */
    private final String channelType;

    /** The channel. */
    private final ScalarWebChannel channel;

    /**
     * Instantiates a new scalar web channel descriptor.
     *
     * @param channel the channel
     * @param acceptedItemType the accepted item type
     * @param channelType the channel type
     */
    public ScalarWebChannelDescriptor(ScalarWebChannel channel, String acceptedItemType, String channelType) {
        this(channel, acceptedItemType, channelType, null, null);
    }

    /**
     * Instantiates a new scalar web channel descriptor.
     *
     * @param channel the channel
     * @param acceptedItemType the accepted item type
     * @param channelType the channel type
     * @param label the label
     * @param description the description
     */
    public ScalarWebChannelDescriptor(ScalarWebChannel channel, String acceptedItemType, String channelType,
            String label, String description) {
        this.channel = channel;
        this.channelType = channelType;
        this.acceptedItemType = acceptedItemType;
        this.label = label;
        this.description = description;
    }

    /**
     * Creates the channel.
     *
     * @param thingUid the thing uid
     * @return the channel builder
     */
    public ChannelBuilder createChannel(ThingUID thingUid) {

        ChannelBuilder channelBuilder = ChannelBuilder.create(new ChannelUID(thingUid, channel.getChannelId()),
                acceptedItemType);

        final ChannelTypeUID stateTypeUid = new ChannelTypeUID(ScalarWebConstants.THING_TYPE_SCALAR.getBindingId(),
                channelType);
        channelBuilder = channelBuilder.withType(stateTypeUid);

        if (StringUtils.isNotEmpty(label)) {
            channelBuilder = channelBuilder.withLabel(label);
        }

        if (StringUtils.isNotEmpty(description)) {
            channelBuilder = channelBuilder.withDescription(description);
        }

        return channelBuilder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return channel + " accepting " + acceptedItemType + " of type " + channelType;
    }
}
