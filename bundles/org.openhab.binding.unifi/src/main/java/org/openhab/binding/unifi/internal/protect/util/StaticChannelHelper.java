/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.protect.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.slf4j.Logger;

/**
 * Adds any statically-defined channels that a stored thing is missing.
 * <p>
 * openHAB only stamps a thing-type's static channels onto a thing when the thing is first created.
 * Things that were created before their thing-type XML was available — for example the Protect things
 * migrated during the unify merge — can end up persisted with an empty channel list even though the
 * thing-type now resolves correctly. This helper reconciles such things on handler initialization by
 * adding the missing channels, mirroring how the camera handler already rebuilds its dynamic channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class StaticChannelHelper {

    private StaticChannelHelper() {
    }

    /**
     * Ensures every channel defined by the thing's resolved thing-type exists on the thing, adding any
     * that are missing. Existing channels (including dynamically-added ones) are left untouched.
     *
     * @param callback the handler callback used to build channels from their channel-type
     * @param thingTypeRegistry registry used to resolve the thing-type definition
     * @param thing the thing being initialized
     * @param logger the handler's logger, for diagnostics
     * @return the channels added, or an empty list if nothing changed or the type could not be resolved
     */
    public static List<Channel> addMissingChannels(@Nullable ThingHandlerCallback callback,
            ThingTypeRegistry thingTypeRegistry, Thing thing, Logger logger) {
        if (callback == null) {
            return List.of();
        }
        ThingType thingType = thingTypeRegistry.getThingType(thing.getThingTypeUID());
        if (thingType == null) {
            logger.debug("Cannot reconcile channels, thing-type {} not found", thing.getThingTypeUID());
            return List.of();
        }
        List<Channel> added = new ArrayList<>();
        for (ChannelDefinition definition : thingType.getChannelDefinitions()) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), definition.getId());
            if (thing.getChannel(channelUID) != null) {
                continue;
            }
            ChannelTypeUID channelTypeUID = definition.getChannelTypeUID();
            ChannelBuilder builder = callback.createChannelBuilder(channelUID, channelTypeUID);
            String label = definition.getLabel();
            if (label != null) {
                builder.withLabel(label);
            }
            String description = definition.getDescription();
            if (description != null) {
                builder.withDescription(description);
            }
            added.add(builder.build());
        }
        if (!added.isEmpty()) {
            logger.debug("Adding {} missing static channel(s) to {}", added.size(), thing.getUID());
        }
        return added;
    }
}
