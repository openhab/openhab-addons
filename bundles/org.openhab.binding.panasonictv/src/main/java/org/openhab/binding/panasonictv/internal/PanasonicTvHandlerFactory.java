/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv.internal;

import static org.openhab.binding.panasonictv.PanasonicTvBindingConstants.PANASONIC_TV_THING_TYPE;

import java.util.Collection;
import java.util.List;

import org.jupnp.UpnpService;
import org.openhab.binding.panasonictv.handler.PanasonicTvHandler;
import org.openhab.core.config.discovery.DiscoveryServiceRegistry;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@link PanasonicTvHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
public class PanasonicTvHandlerFactory extends BaseThingHandlerFactory {

    private static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = List.of(PANASONIC_TV_THING_TYPE);

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

        if (thingTypeUID.equals(PANASONIC_TV_THING_TYPE)) {
            return new PanasonicTvHandler(thing, upnpIOService, discoveryServiceRegistry, upnpService);
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
