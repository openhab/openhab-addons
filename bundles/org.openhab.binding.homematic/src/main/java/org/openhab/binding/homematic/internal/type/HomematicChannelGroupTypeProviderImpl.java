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
package org.openhab.binding.homematic.internal.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openhab.binding.homematic.type.HomematicThingTypeExcluder;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides all ChannelGroupTypes from all Homematic bridges.
 *
 * @author Michael Reitler - Initial contribution
 */
@Component(service = { HomematicChannelGroupTypeProvider.class, ChannelGroupTypeProvider.class })
public class HomematicChannelGroupTypeProviderImpl implements HomematicChannelGroupTypeProvider {
    private final Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypesByUID = new HashMap<>();
    protected List<HomematicThingTypeExcluder> homematicThingTypeExcluders = new CopyOnWriteArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addHomematicThingTypeExcluder(HomematicThingTypeExcluder homematicThingTypeExcluder) {
        if (homematicThingTypeExcluders != null) {
            homematicThingTypeExcluders.add(homematicThingTypeExcluder);
        }
    }

    protected void removeHomematicThingTypeExcluder(HomematicThingTypeExcluder homematicThingTypeExcluder) {
        if (homematicThingTypeExcluders != null) {
            homematicThingTypeExcluders.remove(homematicThingTypeExcluder);
        }
    }

    private boolean isChannelGroupTypeExcluded(ChannelGroupTypeUID channelGroupTypeUID) {
        // delegate to excluders
        for (HomematicThingTypeExcluder excluder : homematicThingTypeExcluders) {
            if (excluder.isChannelGroupTypeExcluded(channelGroupTypeUID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return isChannelGroupTypeExcluded(channelGroupTypeUID) ? null : channelGroupTypesByUID.get(channelGroupTypeUID);
    }

    @Override
    public ChannelGroupType getInternalChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID) {
        return channelGroupTypesByUID.get(channelGroupTypeUID);
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        Collection<ChannelGroupType> result = new ArrayList<>();
        for (ChannelGroupTypeUID uid : channelGroupTypesByUID.keySet()) {
            if (!isChannelGroupTypeExcluded(uid)) {
                result.add(channelGroupTypesByUID.get(uid));
            }
        }
        return result;
    }

    @Override
    public void addChannelGroupType(ChannelGroupType channelGroupType) {
        channelGroupTypesByUID.put(channelGroupType.getUID(), channelGroupType);
    }
}
