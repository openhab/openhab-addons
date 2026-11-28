/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.sinope.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinope.SinopeBindingConstants;
import org.openhab.binding.sinope.handler.SinopeGatewayHandler;
import org.openhab.binding.sinope.handler.SinopeThermostatHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * {@link SinopeHandlerFactory} is a factory for {@link SinopeThermostatHandler}s and {@link SinopeGatewayHandler}s
 *
 * @author Pascal Larin - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
@NonNullByDefault
public class SinopeHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = SinopeBindingConstants.SUPPORTED_THING_TYPES_UIDS;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SinopeBindingConstants.THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new SinopeGatewayHandler((Bridge) thing);
        } else if (SinopeBindingConstants.THING_TYPE_THERMO.equals(thingTypeUID)) {
            return new SinopeThermostatHandler(thing);
        }
        return null;
    }
}
