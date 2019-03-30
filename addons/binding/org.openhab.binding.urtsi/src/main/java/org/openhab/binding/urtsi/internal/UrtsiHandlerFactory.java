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
package org.openhab.binding.urtsi.internal;

import static org.openhab.binding.urtsi.internal.UrtsiBindingConstants.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.urtsi.internal.handler.RtsDeviceHandler;
import org.openhab.binding.urtsi.internal.handler.UrtsiDeviceHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link UrtsiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oliver Libutzki - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.urtsi")
public class UrtsiHandlerFactory extends BaseThingHandlerFactory {

    private static final List<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Arrays.asList(URTSI_DEVICE_THING_TYPE,
            RTS_DEVICE_THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(URTSI_DEVICE_THING_TYPE) && thing instanceof Bridge) {
            return new UrtsiDeviceHandler((Bridge) thing);
        } else if (thingTypeUID.equals(RTS_DEVICE_THING_TYPE)) {
            return new RtsDeviceHandler(thing);
        }

        return null;
    }
}
