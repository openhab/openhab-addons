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

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.homematic.type.HomematicThingTypeExcluder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides all ThingTypes from all Homematic bridges. Getters will exclude
 * ThingTypes which occur in any registered {@link HomematicThingTypeExcluder},
 * which allows external injection of customized thing-types at runtime.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Michael Reitler - Added HomematicThingTypeExcluder
 */
@Component(service = { HomematicThingTypeProvider.class, ThingTypeProvider.class }, immediate = true)
public class HomematicThingTypeProviderImpl implements HomematicThingTypeProvider {
    private Map<ThingTypeUID, ThingType> thingTypesByUID = new HashMap<ThingTypeUID, ThingType>();
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
    
    private Collection<ThingTypeUID> getExcludedThingTypes() {
        Collection<ThingTypeUID> thingTypes = new ArrayList<>();
        for (HomematicThingTypeExcluder excluder : homematicThingTypeExcluders) {
            thingTypes.addAll(excluder.getExcludedThingTypes());
        }
        return thingTypes;
    }
    
    private boolean isThingTypeExcluded(ThingTypeUID thingType) {
        // delegate to excluders
        for (HomematicThingTypeExcluder excluder : homematicThingTypeExcluders) {
            if (excluder.isThingTypeExcluded(thingType)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Collection<ThingType> getThingTypes(Locale locale) {
        Map<ThingTypeUID, ThingType> copy = new HashMap<>(thingTypesByUID);
        copy.keySet().removeAll(getExcludedThingTypes());
        return copy.values();
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        return isThingTypeExcluded(thingTypeUID) ? null : thingTypesByUID.get(thingTypeUID);
    }
    
    @Override
    public ThingType getInternalThingType(ThingTypeUID thingTypeUID) {
        return thingTypesByUID.get(thingTypeUID);
    }

    @Override
    public void addThingType(ThingType thingType) {
        thingTypesByUID.put(thingType.getUID(), thingType);
    }
}
