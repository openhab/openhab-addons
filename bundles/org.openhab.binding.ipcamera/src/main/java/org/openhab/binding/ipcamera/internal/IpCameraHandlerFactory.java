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

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraGroupHandler;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link IpCameraHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthew Skinner - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.ipcamera")
@NonNullByDefault
public class IpCameraHandlerFactory extends BaseThingHandlerFactory {
    private final @Nullable String openhabIpAddress;
    private final GroupTracker groupTracker = new GroupTracker();
    private final IpCameraDynamicStateDescriptionProvider stateDescriptionProvider;

    @Activate
    public IpCameraHandlerFactory(final @Reference NetworkAddressService networkAddressService,
            final @Reference IpCameraDynamicStateDescriptionProvider stateDescriptionProvider) {
        openhabIpAddress = networkAddressService.getPrimaryIpv4HostAddress();
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (SUPPORTED_THING_TYPES.contains(thingTypeUID) || GROUP_SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return true;
        }
        return false;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new IpCameraHandler(thing, openhabIpAddress, groupTracker, stateDescriptionProvider);
        } else if (GROUP_SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new IpCameraGroupHandler(thing, openhabIpAddress, groupTracker);
        }
        return null;
    }
}
