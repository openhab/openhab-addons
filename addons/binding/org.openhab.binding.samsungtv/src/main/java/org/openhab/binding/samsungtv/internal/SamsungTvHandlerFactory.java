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
package org.openhab.binding.samsungtv.internal;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.SAMSUNG_TV_THING_TYPE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.jupnp.UpnpService;
import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;

/**
 * The {@link SamsungTvHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SamsungTvHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(SAMSUNG_TV_THING_TYPE);

    private UpnpIOService upnpIOService;
    private DiscoveryServiceRegistry discoveryServiceRegistry;
    private UpnpService upnpService;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SAMSUNG_TV_THING_TYPE)) {
            return new SamsungTvHandler(thing, upnpIOService, discoveryServiceRegistry, upnpService);
        }

        return null;
    }

    protected void setUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = upnpIOService;
    }

    protected void unsetUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = null;
    }

    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

    protected void setUpnpService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    protected void unsetUpnpService(UpnpService upnpService) {
        this.upnpService = null;
    }
}
