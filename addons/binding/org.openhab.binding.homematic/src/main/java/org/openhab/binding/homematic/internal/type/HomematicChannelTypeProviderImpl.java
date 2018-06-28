/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.homematic.type.HomematicThingTypeExcluder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides all ChannelTypes and ChannelGroupTypes from all Homematic bridges.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Michael Reitler - Added HomematicThingTypeExcluder
 */
@Component(service = { HomematicChannelTypeProvider.class, ChannelTypeProvider.class }, immediate = true)
public class HomematicChannelTypeProviderImpl implements HomematicChannelTypeProvider {
    private Map<ChannelTypeUID, ChannelType> channelTypesByUID = new HashMap<ChannelTypeUID, ChannelType>();
    private Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypesByUID = new HashMap<ChannelGroupTypeUID, ChannelGroupType>();
    protected List<HomematicThingTypeExcluder> homematicThingTypeExcluders = new CopyOnWriteArrayList<>();
    
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addHomematicThingTypeExcluder(HomematicThingTypeExcluder homematicThingTypeExcluder){
        if(homematicThingTypeExcluders != null){
            homematicThingTypeExcluders.add(homematicThingTypeExcluder);
        }
    }
     
    protected void removeHomematicThingTypeExcluder(HomematicThingTypeExcluder homematicThingTypeExcluder){
        if(homematicThingTypeExcluders != null){
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
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return isChannelGroupTypeExcluded(channelGroupTypeUID) ? null : channelGroupTypesByUID.get(channelGroupTypeUID);
    }
    
    @Override
    public ChannelType getInternalChannelType(ChannelTypeUID channelTypeUID) {
        return channelTypesByUID.get(channelTypeUID);
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
    public void addChannelType(ChannelType channelType) {
        channelTypesByUID.put(channelType.getUID(), channelType);
    }

    @Override
    public void addChannelGroupType(ChannelGroupType channelGroupType) {
        channelGroupTypesByUID.put(channelGroupType.getUID(), channelGroupType);
    }

}
