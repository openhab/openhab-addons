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
package org.openhab.binding.wiz.internal;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a dynamic state description for color temp to define the min/max as provided by the
 * actual bulb.
 * This service is started on-demand only, as soon as {@link WizThingHandlerFactory} requires it.
 *
 * @author Cody Cutrer - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, WizStateDescriptionProvider.class })
@NonNullByDefault
public class WizStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private final Map<ChannelUID, StateDescription> stateDescriptions = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(WizStateDescriptionProvider.class);

    /**
     * Set a state description for a channel. This description will be used when preparing the channel state by
     * the framework for presentation. A previous description, if existed, will be replaced.
     *
     * @param channelUID channel UID
     * @param description state description for the channel
     */
    public void setDescription(ChannelUID channelUID, StateDescription description) {
        logger.debug("Adding state description for channel {}: {}", channelUID, description);
        stateDescriptions.put(channelUID, description);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        StateDescription description = stateDescriptions.get(channel.getUID());
        if (description != null) {
            logger.trace("Providing state description for channel {}", channel.getUID());
        }
        return description;
    }

    /**
     * Removes the given channel description.
     *
     * @param channel The channel
     */
    public void remove(ChannelUID channel) {
        stateDescriptions.remove(channel);
    }
}
