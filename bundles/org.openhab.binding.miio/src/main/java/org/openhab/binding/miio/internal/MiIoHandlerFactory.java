/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.miio.internal.handler.MiIoBasicHandler;
import org.openhab.binding.miio.internal.handler.MiIoGenericHandler;
import org.openhab.binding.miio.internal.handler.MiIoUnsupportedHandler;
import org.openhab.binding.miio.internal.handler.MiIoVacuumHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MiIoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.miio")
public class MiIoHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_MIIO)) {
            return new MiIoGenericHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_BASIC)) {
            return new MiIoBasicHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_VACUUM)) {
            return new MiIoVacuumHandler(thing);
        }
        return new MiIoUnsupportedHandler(thing);
    }
}
