/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides all ChannelTypes from all Homematic bridges.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Michael Reitler - Added HomematicThingTypeExcluder
 */
@Component(service = { HomematicChannelTypeProvider.class, ChannelTypeProvider.class })
public class HomematicChannelTypeProviderImpl implements HomematicChannelTypeProvider {
    private final Map<ChannelTypeUID, ChannelType> channelTypesByUID = new HashMap<>();
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

    private boolean isChannelTypeExcluded(ChannelTypeUID channelTypeUID) {
        // delegate to excluders
        for (HomematicThingTypeExcluder excluder : homematicThingTypeExcluders) {
            if (excluder.isChannelTypeExcluded(channelTypeUID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        Collection<ChannelType> result = new ArrayList<>();
        for (ChannelTypeUID uid : channelTypesByUID.keySet()) {
            if (!isChannelTypeExcluded(uid)) {
                result.add(channelTypesByUID.get(uid));
            }
        }
        return result;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return isChannelTypeExcluded(channelTypeUID) ? null : channelTypesByUID.get(channelTypeUID);
    }

    @Override
    public ChannelType getInternalChannelType(ChannelTypeUID channelTypeUID) {
        return channelTypesByUID.get(channelTypeUID);
    }

    @Override
    public void addChannelType(ChannelType channelType) {
        channelTypesByUID.put(channelType.getUID(), channelType);
    }
}
