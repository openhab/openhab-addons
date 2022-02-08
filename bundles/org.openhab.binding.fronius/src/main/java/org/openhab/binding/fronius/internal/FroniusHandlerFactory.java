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
package org.openhab.binding.fronius.internal;

import static org.openhab.binding.fronius.internal.FroniusBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.fronius.internal.handler.FroniusBridgeHandler;
import org.openhab.binding.fronius.internal.handler.FroniusMeterHandler;
import org.openhab.binding.fronius.internal.handler.FroniusOhmpilotHandler;
import org.openhab.binding.fronius.internal.handler.FroniusSymoInverterHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FroniusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Hannes Spenger - Added ohmpilot
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.fronius")
public class FroniusHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>() {

        private static final long serialVersionUID = 1L;
        {
            add(THING_TYPE_INVERTER);
            add(THING_TYPE_BRIDGE);
            add(THING_TYPE_METER);
            add(THING_TYPE_OHMPILOT);
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
        } else if (thingTypeUID.equals(THING_TYPE_METER)) {
            return new FroniusMeterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OHMPILOT)) {
            return new FroniusOhmpilotHandler(thing);
        }
        return null;
    }
}
