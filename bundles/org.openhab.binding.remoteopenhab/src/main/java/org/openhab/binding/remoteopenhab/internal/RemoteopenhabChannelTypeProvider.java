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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.osgi.service.component.annotations.Component;

/**
 * Channel type provider used for all the channel types built by the binding when building dynamically the channels.
 * One different channel type is built for each different item type found on the remote openHAB server.
 *
 * @author Laurent Garnier - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, RemoteopenhabChannelTypeProvider.class })
@NonNullByDefault
public class RemoteopenhabChannelTypeProvider implements ChannelTypeProvider {
    private final List<ChannelType> channelTypes = new CopyOnWriteArrayList<>();
    private final Map<String, List<ChannelType>> channelTypesForItemTypes = new ConcurrentHashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes;
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        for (ChannelType channelType : channelTypes) {
            if (channelType.getUID().equals(channelTypeUID)) {
                return channelType;
            }
        }
        return null;
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
        int nb = channelTypesForItemType == null ? 0 : channelTypesForItemType.size();
        return new ChannelTypeUID(BINDING_ID, String.format("item%s%d", itemType.replace(":", ""), nb + 1));
    }

    public void addChannelType(String itemType, ChannelType channelType) {
        channelTypes.add(channelType);
        List<ChannelType> channelTypesForItemType = channelTypesForItemTypes.computeIfAbsent(itemType,
                type -> new CopyOnWriteArrayList<>());
        if (channelTypesForItemType != null) {
            channelTypesForItemType.add(channelType);
        }
    }

    public void removeChannelType(String itemType, ChannelType channelType) {
        channelTypes.remove(channelType);
        List<ChannelType> channelTypesForItemType = channelTypesForItemTypes.get(itemType);
        if (channelTypesForItemType != null) {
            channelTypesForItemType.remove(channelType);
        }
    }
}
