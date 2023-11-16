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
package org.openhab.binding.remoteopenhab.internal;

import static org.openhab.binding.remoteopenhab.internal.RemoteopenhabBindingConstants.BINDING_ID;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Channel type provider used for all the channel types built by the binding when building dynamically the channels.
 * One different channel type is built for each different item type found on the remote openHAB server.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - Use AbstractStorageBasedTypeProvider
 */
@Component(service = { ChannelTypeProvider.class, RemoteopenhabChannelTypeProvider.class })
@NonNullByDefault
public class RemoteopenhabChannelTypeProvider extends AbstractStorageBasedTypeProvider {

    private static final String PATTERN_CHANNEL_TYPE_ID = "item%s%d";
    private static final Pattern PATTERN_MATCHING_CHANNEL_TYPE_ID = Pattern.compile("^item([a-zA-Z]+)([0-9]+)$");

    private final Logger logger = LoggerFactory.getLogger(RemoteopenhabChannelTypeProvider.class);

    private final Map<String, List<ChannelType>> channelTypesForItemTypes = new ConcurrentHashMap<>();

    @Activate
    public RemoteopenhabChannelTypeProvider(@Reference StorageService storageService) {
        super(storageService);
        getChannelTypes(null).forEach(ct -> {
            Matcher matcher = PATTERN_MATCHING_CHANNEL_TYPE_ID.matcher(ct.getUID().getId());
            if (matcher.find()) {
                String itemType = matcher.group(1);
                // Handle number with a dimension
                if (itemType.startsWith("Number") && !"Number".equals(itemType)) {
                    itemType = itemType.replace("Number", "Number:");
                }
                addChannelTypeForItemType(itemType, ct);
            } else {
                logger.warn("Invalid channel type ID : {}", ct.getUID().getId());
            }
        });
    }

    @Deactivate
    protected void deactivate() {
        channelTypesForItemTypes.values().forEach(l -> l.clear());
        channelTypesForItemTypes.clear();
    }

    public @Nullable ChannelType getChannelType(String itemType, boolean readOnly, String pattern) {
        List<ChannelType> channelTypesForItemType = channelTypesForItemTypes.get(itemType);
        if (channelTypesForItemType != null) {
            for (ChannelType channelType : channelTypesForItemType) {
                boolean channelTypeReadOnly = false;
                String channelTypePattern = null;
                StateDescription stateDescription = channelType.getState();
                if (stateDescription != null) {
                    channelTypeReadOnly = stateDescription.isReadOnly();
                    channelTypePattern = stateDescription.getPattern();
                }
                if (channelTypePattern == null) {
                    channelTypePattern = "";
                }
                if (channelTypeReadOnly == readOnly && channelTypePattern.equals(pattern)) {
                    return channelType;
                }
            }
        }
        return null;
    }

    public ChannelTypeUID buildNewChannelTypeUID(String itemType) {
        List<ChannelType> channelTypesForItemType = channelTypesForItemTypes.get(itemType);
        int max = 0;
        if (channelTypesForItemType != null) {
            for (ChannelType ct : channelTypesForItemType) {
                Matcher matcher = PATTERN_MATCHING_CHANNEL_TYPE_ID.matcher(ct.getUID().getId());
                if (matcher.find()) {
                    int nb = Integer.parseInt(matcher.group(2));
                    if (nb > max) {
                        max = nb;
                    }
                }
            }
        }
        return new ChannelTypeUID(BINDING_ID,
                String.format(PATTERN_CHANNEL_TYPE_ID, itemType.replace(":", ""), max + 1));
    }

    public void addChannelType(String itemType, ChannelType channelType) {
        putChannelType(channelType);
        addChannelTypeForItemType(itemType, channelType);
    }

    public void removeChannelType(String itemType, ChannelType channelType) {
        removeChannelType(channelType.getUID());
        removeChannelTypeForItemType(itemType, channelType);
    }

    private void addChannelTypeForItemType(String itemType, ChannelType channelType) {
        logger.debug("addChannelTypeForItemType {} {}", itemType, channelType.getUID());
        List<ChannelType> channelTypesForItemType = channelTypesForItemTypes.computeIfAbsent(itemType,
                type -> new CopyOnWriteArrayList<>());
        if (channelTypesForItemType != null) {
            channelTypesForItemType.add(channelType);
        }
    }

    private void removeChannelTypeForItemType(String itemType, ChannelType channelType) {
        List<ChannelType> channelTypesForItemType = channelTypesForItemTypes.get(itemType);
        if (channelTypesForItemType != null) {
            channelTypesForItemType.remove(channelType);
        }
    }
}
