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
package org.openhab.binding.amberelectric.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link AmberElectricHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Paul Smedley - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.amberelectric")
@NonNullByDefault
public class AmberElectricHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return AmberElectricBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(AmberElectricBindingConstants.AMBERELECTRIC_THING)) {
            return new AmberElectricHandler(thing);
        }

        return null;
    }
}
