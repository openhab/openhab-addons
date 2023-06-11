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
package org.openhab.binding.homematic.internal.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openhab.binding.homematic.type.HomematicThingTypeExcluder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.type.ThingType;
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
@Component(service = { HomematicThingTypeProvider.class, ThingTypeProvider.class })
public class HomematicThingTypeProviderImpl implements HomematicThingTypeProvider {
    private Map<ThingTypeUID, ThingType> thingTypesByUID = new HashMap<>();
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
