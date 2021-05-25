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
package org.openhab.binding.carnet.internal.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Extends the ChannelTypeProvider for user defined channel and channel group types.
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
@Component(service = { ChannelTypeProvider.class, CarNetChannelTypeProvider.class })
public class CarNetChannelTypeProvider implements ChannelTypeProvider, ChannelGroupTypeProvider {
    private final CarNetIChanneldMapper channelIdMapper;
    private List<ChannelType> channelTypes = new CopyOnWriteArrayList<ChannelType>();
    private List<ChannelGroupType> channelGroupTypes = new CopyOnWriteArrayList<ChannelGroupType>();

    @Activate
    public CarNetChannelTypeProvider(@Reference CarNetIChanneldMapper channelIdMapper) {
        this.channelIdMapper = channelIdMapper;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes;
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().startsWith(channelTypeUID.getAsString())) {
                return c;
            }
        }

        String channelId = channelTypeUID.getId();
        ChannelIdMapEntry channelDef = channelIdMapper.find(channelId);
        if ((channelDef == null) || channelDef.groupName.isEmpty() || channelDef.channelName.isEmpty()
                || channelDef.getLabel().isEmpty() || channelDef.itemType.isEmpty()) {
            return null;
        }

        StateDescriptionFragmentBuilder desc = CarNetStateDescriptionProvider.buildStateDescriptor(channelIdMapper,
                channelId);
        ChannelType ct = null;
        if (desc != null) {
            ct = ChannelTypeBuilder.state(channelTypeUID, channelDef.getLabel(), channelDef.itemType)
                    .withDescription(channelDef.getDescription()).isAdvanced(channelDef.advanced)
                    .withStateDescriptionFragment(desc.build()).build();
            channelTypes.add(ct);
        }
        return ct;
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        for (ChannelGroupType channelGroupType : channelGroupTypes) {
            if (channelGroupType.getUID().equals(channelGroupTypeUID)) {
                return channelGroupType;
            }
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return channelGroupTypes;
    }

    public void addChannelType(ChannelType type) {
        channelTypes.add(type);
    }

    public void removeChannelType(ChannelType type) {
        channelTypes.remove(type);
    }

    public void removeChannelTypesForThing(ThingUID uid) {
        List<ChannelType> removes = new ArrayList<ChannelType>();
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().startsWith(uid.getAsString())) {
                removes.add(c);
            }
        }
        channelTypes.removeAll(removes);
    }

    public boolean channelTypeExists(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().startsWith(channelTypeUID.getAsString())) {
                return true;
            }
        }
        return false;
    }
}
