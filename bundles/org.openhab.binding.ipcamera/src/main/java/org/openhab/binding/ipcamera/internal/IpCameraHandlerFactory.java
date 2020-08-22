/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.ipcamera.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.ipcamera.handler.IpCameraGroupHandler;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link IpCameraHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthew Skinner - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.ipcamera")
@NonNullByDefault
public class IpCameraHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (IpCameraHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return true;
        } else if (IpCameraGroupHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return true;
        }
        return false;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (IpCameraHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new IpCameraHandler(thing);
        } else if (IpCameraGroupHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new IpCameraGroupHandler(thing);
        }
        return null;
    }
}
