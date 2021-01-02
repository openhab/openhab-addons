/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.types.CommandDescription;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic channel command description provider.
 * Overrides the command description for the controls, which receive its configuration in the runtime.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicCommandDescriptionProvider.class, CommandDescriptionProvider.class })
public class CommandDescriptionProvider implements DynamicCommandDescriptionProvider {

    private final Map<ChannelUID, CommandDescription> descriptions = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(CommandDescriptionProvider.class);

    /**
     * Set a command description for a channel. This description will be used when preparing the channel command by
     * the framework for presentation. A previous description, if existed, will be replaced.
     *
     * @param channelUID
     *            channel UID
     * @param description
     *            state description for the channel
     */
    public void setDescription(ChannelUID channelUID, CommandDescription description) {
        logger.trace("adding command description for channel {}", channelUID);
        descriptions.put(channelUID, description);
    }

    /**
     * remove all descriptions for a given thing
     *
     * @param thingUID the thing's UID
     */
    public void removeDescriptionsForThing(ThingUID thingUID) {
        logger.trace("removing state description for thing {}", thingUID);
        descriptions.entrySet().removeIf(entry -> entry.getKey().getThingUID().equals(thingUID));
    }

    @Override
    public @Nullable CommandDescription getCommandDescription(Channel channel,
            @Nullable CommandDescription originalStateDescription, @Nullable Locale locale) {
        if (descriptions.containsKey(channel.getUID())) {
            logger.trace("returning new stateDescription for {}", channel.getUID());
            return descriptions.get(channel.getUID());
        } else {
            return null;
        }
    }
}
