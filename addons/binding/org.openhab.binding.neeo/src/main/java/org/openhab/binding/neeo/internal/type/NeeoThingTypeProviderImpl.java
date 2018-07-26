/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of {@link NeeoThingTypeProvider} that will store {@link ThingType} by {@link ThingTypeUID}
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { org.eclipse.smarthome.core.thing.binding.ThingTypeProvider.class,
        NeeoThingTypeProvider.class })
public class NeeoThingTypeProviderImpl implements NeeoThingTypeProvider {

    /** The thing types by UID. */
    private Map<ThingTypeUID, ThingType> thingTypesByUID = new ConcurrentHashMap<>();

    @Override
    public Collection<ThingType> getThingTypes(@Nullable Locale locale) {
        return thingTypesByUID.values();
    }

    @Nullable
    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, @Nullable Locale locale) {
        return thingTypesByUID.get(thingTypeUID);
    }

    @Override
    public void addThingType(ThingType thingType) {
        thingTypesByUID.put(thingType.getUID(), thingType);
    }
}
