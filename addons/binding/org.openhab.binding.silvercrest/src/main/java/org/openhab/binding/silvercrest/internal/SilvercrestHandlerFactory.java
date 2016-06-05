/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrest.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.silvercrest.SilvercrestBindingConstants;
import org.openhab.binding.silvercrest.discovery.SilvercrestDiscoveryService;
import org.openhab.binding.silvercrest.exceptions.MacAddressNotValidException;
import org.openhab.binding.silvercrest.handler.WifiSocketOutletHandler;
import org.openhab.binding.silvercrest.handler.WifiSocketOutletMediator;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SilvercrestHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jaime Vaz - Initial contribution
 */
public class SilvercrestHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(SilvercrestBindingConstants.THING_TYPE_SOCKET_OUTLET);

    private static final Logger LOG = LoggerFactory.getLogger(SilvercrestHandlerFactory.class);

    private WifiSocketOutletMediator mediator;

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(final Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SilvercrestBindingConstants.THING_TYPE_SOCKET_OUTLET)) {
            WifiSocketOutletHandler handler;
            LOG.debug("Creating a new SilvercrestWifiSocketHandler...");
            try {
                handler = new WifiSocketOutletHandler(thing);
                LOG.debug("SilvercrestWifiSocketMediator will register the handler.");
                this.getMediator().registerThingAndWifiSocketOutletHandler(thing, handler);
                return handler;
            } catch (MacAddressNotValidException e) {
                LOG.debug("The mac address passed to WifiSocketOutletHandler by configurations is not valid.");
            }

        }
        return null;
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        this.getMediator().unregisterWifiSocketOutletHandlerByThing(thing);
        super.unregisterHandler(thing);
    }

    /**
     * Looks up for the the {@link SilvercrestDiscoveryService} and gets the {@link WifiSocketOutletMediator}.
     *
     * @return the {@link WifiSocketOutletMediator}.
     */
    private WifiSocketOutletMediator getMediator() {
        if (this.mediator == null) {
            try {
                // LOOKUP FOR SilvercrestDiscoveryService
                ServiceReference<?>[] references = this.bundleContext
                        .getAllServiceReferences(DiscoveryService.class.getName(), null);
                if (references != null) {
                    for (ServiceReference<?> reference : references) {
                        if (this.bundleContext.getServiceObjects(reference)
                                .getService() instanceof SilvercrestDiscoveryService) {
                            SilvercrestDiscoveryService discoveryService = (SilvercrestDiscoveryService) this.bundleContext
                                    .getServiceObjects(reference).getService();
                            this.mediator = discoveryService.getMediator();
                            break;
                        }
                    }
                }
            } catch (InvalidSyntaxException e) {
                LOG.debug("Error looking up for SilvercrestDiscoveryService to get the WifiSocketOutletMediator: {}",
                        e.getMessage());
            }
        }
        return this.mediator;
    }

}
