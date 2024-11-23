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
package org.openhab.binding.modbus.kermi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.kermi.internal.handler.KermiXcenterThingHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link KermiHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Kai Neuhaus - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.kermi", service = ThingHandlerFactory.class)
public class KermiHandlerFactory extends BaseThingHandlerFactory {
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.equals(KermiBindingConstants.THING_TYPE_KERMI_XCENTER);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (KermiBindingConstants.THING_TYPE_KERMI_XCENTER.equals(thingTypeUID)) {
            return new KermiXcenterThingHandler((Bridge) thing);
        } // else here
        return null;
    }
}
