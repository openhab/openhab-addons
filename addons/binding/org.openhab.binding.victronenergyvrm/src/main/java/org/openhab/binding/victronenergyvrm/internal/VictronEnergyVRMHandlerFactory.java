/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergyvrm.internal;

import static org.openhab.binding.victronenergyvrm.VictronEnergyVRMBindingConstants.*;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.victronenergyvrm.handler.VictronEnergyVRMHandler;
import org.openhab.binding.victronenergyvrm.handler.VictronEnergyVrmBmHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link VictronEnergyVRMHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Samuel Lueckoff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.victronenergyvrm")
@NonNullByDefault
public class VictronEnergyVRMHandlerFactory extends BaseThingHandlerFactory {

    private static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Arrays.asList(THING_TYPE_INSTALLATION,
            THING_TYPE_INSTALLATION_BM);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_INSTALLATION)) {
            return new VictronEnergyVRMHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_INSTALLATION_BM)) {
            return new VictronEnergyVrmBmHandler(thing);
        }

        return null;
    }
}
