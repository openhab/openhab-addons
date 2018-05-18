/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal;

import static org.openhab.binding.fronius.FroniusBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.fronius.handler.FroniusBridgeHandler;
import org.openhab.binding.fronius.handler.FroniusSymoInverterHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FroniusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Rokohl - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.fronius")
public class FroniusHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>() {

        private static final long serialVersionUID = 1L;
        {
            add(THING_TYPE_INVERTER);
            add(THING_TYPE_BRIDGE);
        }
    };

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_INVERTER)) {
            return new FroniusSymoInverterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new FroniusBridgeHandler((Bridge) thing);
        }
        return null;
    }
}
