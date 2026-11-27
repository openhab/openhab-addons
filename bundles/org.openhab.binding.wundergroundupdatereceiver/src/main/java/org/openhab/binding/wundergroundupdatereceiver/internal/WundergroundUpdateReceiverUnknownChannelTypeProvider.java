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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Demus - Initial contribution
 * @author Leo Siepel - Persist and restore generated channel types
 */
@Component(service = { ChannelTypeProvider.class, WundergroundUpdateReceiverUnknownChannelTypeProvider.class })
@NonNullByDefault
public class WundergroundUpdateReceiverUnknownChannelTypeProvider extends AbstractStorageBasedTypeProvider {

    private static final List<String> BOOLEAN_STRINGS = List.of("1", "0", "true", "false", "yes", "no", "on", "off");
    private final Logger logger = LoggerFactory.getLogger(WundergroundUpdateReceiverUnknownChannelTypeProvider.class);

    @Activate
    public WundergroundUpdateReceiverUnknownChannelTypeProvider(@Reference StorageService storageService,
            @Reference ManagedThingProvider managedThingProvider) {
        super(storageService);
        // Older versions persisted the channels, but kept their generated types only in memory.
        // Recover those types before Thing initialization checks the channel type registry.
        managedThingProvider.getAll().stream()
                .filter(thing -> THING_TYPE_UPDATE_RECEIVER.equals(thing.getThingTypeUID()))
                .flatMap(thing -> thing.getChannels().stream()).forEach(this::restoreChannelType);
    }

    private void restoreChannelType(Channel channel) {
        ChannelTypeUID typeUid = channel.getChannelTypeUID();
        String itemType = channel.getAcceptedItemType();
        if (channel.getKind() == ChannelKind.STATE && typeUid != null && itemType != null
                && BINDING_ID.equals(typeUid.getBindingId()) && UNCATEGORIZED.equals(channel.getUID().getGroupId())
                && typeUid.getId().equals(channel.getUID().getIdWithoutGroup())
                && getChannelType(typeUid, null) == null) {
            addChannelType(typeUid, itemType);
        }
    }

    public ChannelType getOrCreateChannelType(String parameterName, String value) {
        ChannelTypeUID typeUid = new ChannelTypeUID(THING_TYPE_UPDATE_RECEIVER.getBindingId(), parameterName);
        @Nullable
        ChannelType type = getChannelType(typeUid, null);
        return type != null ? type : addChannelType(typeUid, guessItemType(value));
    }

    private static String guessItemType(String value) {
        if (BOOLEAN_STRINGS.contains(value.toLowerCase())) {
            return CoreItemFactory.SWITCH;
        }
        try {
            Float.valueOf(value);
            return CoreItemFactory.NUMBER;
        } catch (NumberFormatException ignored) {
        }
        return CoreItemFactory.STRING;
    }

    private ChannelType addChannelType(ChannelTypeUID channelTypeUID, String itemType) {
        ChannelType type = ChannelTypeBuilder.state(channelTypeUID, channelTypeUID.getId() + " channel type", itemType)
                .isAdvanced(true).withCategory(UNCATEGORIZED).withAutoUpdatePolicy(AutoUpdatePolicy.DEFAULT).build();
        putChannelType(type);
        logger.debug("Adding synthetic channel type {} for an unrecognized parameter", channelTypeUID);
        return type;
    }
}
