/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;

/**
 * Provides all ThingTypes from all Homematic bridges.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicThingTypeProviderImpl implements HomematicThingTypeProvider {
    private Map<ThingTypeUID, ThingType> thingTypesByUID = new HashMap<ThingTypeUID, ThingType>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ThingType> getThingTypes(Locale locale) {
        return thingTypesByUID.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        return thingTypesByUID.get(thingTypeUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addThingType(ThingType thingType) {
        thingTypesByUID.put(thingType.getUID(), thingType);
    }

}
