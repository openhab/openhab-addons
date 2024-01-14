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
package org.openhab.binding.loxone.internal;

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
 * Dynamic channel state description provider.
 * Overrides the state description for the controls, which receive its configuration in the runtime.
 *
 * @author Pawel Pieczul - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, LxDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class LxDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private Map<ChannelUID, StateDescription> descriptions = new ConcurrentHashMap<>();
    private Logger logger = LoggerFactory.getLogger(LxDynamicStateDescriptionProvider.class);

    /**
     * Set a state description for a channel. This description will be used when preparing the channel state by
     * the framework for presentation. A previous description, if existed, will be replaced.
     *
     * @param channelUID channel UID
     * @param description state description for the channel
     */
    void setDescription(ChannelUID channelUID, StateDescription description) {
        logger.debug("Adding state description for channel {}", channelUID);
        descriptions.put(channelUID, description);
    }

    /**
     * Clear all registered state descriptions
     */
    void removeAllDescriptions() {
        logger.debug("Removing all state descriptions");
        descriptions.clear();
    }

    /**
     * Removes a state description for a given channel ID
     *
     * @param channelUID channel ID to remove description for
     */
    void removeDescription(ChannelUID channelUID) {
        logger.debug("Removing state description for channel {}", channelUID);
        descriptions.remove(channelUID);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        StateDescription description = descriptions.get(channel.getUID());
        logger.trace("Providing state description for channel {}", channel.getUID());
        return description;
    }
}
