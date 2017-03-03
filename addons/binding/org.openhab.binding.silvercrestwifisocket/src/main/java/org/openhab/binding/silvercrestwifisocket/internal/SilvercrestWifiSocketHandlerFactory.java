/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrestwifisocket.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.silvercrestwifisocket.SilvercrestWifiSocketBindingConstants;
import org.openhab.binding.silvercrestwifisocket.discovery.SilvercrestWifiSocketDiscoveryService;
import org.openhab.binding.silvercrestwifisocket.handler.SilvercrestWifiSocketHandler;
import org.openhab.binding.silvercrestwifisocket.handler.SilvercrestWifiSocketMediator;
import org.openhab.binding.silvercrestwifisocket.internal.exceptions.MacAddressNotValidException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SilvercrestWifiSocketHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jaime Vaz - Initial contribution
 */
public class SilvercrestWifiSocketHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(SilvercrestWifiSocketBindingConstants.THING_TYPE_WIFI_SOCKET);

    private final Logger logger = LoggerFactory.getLogger(SilvercrestWifiSocketHandlerFactory.class);

    private SilvercrestWifiSocketMediator mediator;

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(final Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SilvercrestWifiSocketBindingConstants.THING_TYPE_WIFI_SOCKET)) {
            SilvercrestWifiSocketHandler handler;
            logger.debug("Creating a new SilvercrestWifiSocketHandler...");
            try {
                handler = new SilvercrestWifiSocketHandler(thing);
                logger.debug("SilvercrestWifiSocketMediator will register the handler.");
                this.getMediator().registerThingAndWifiSocketHandler(thing, handler);
                return handler;
            } catch (MacAddressNotValidException e) {
                logger.debug("The mac address passed to WifiSocketHandler by configurations is not valid.");
            }

        }
        return null;
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        this.getMediator().unregisterWifiSocketHandlerByThing(thing);
        super.unregisterHandler(thing);
    }

    /**
     * Looks up for the the {@link SilvercrestWifiSocketDiscoveryService} and gets the
     * {@link SilvercrestWifiSocketMediator}.
     *
     * @return the {@link SilvercrestWifiSocketMediator}.
     */
    private SilvercrestWifiSocketMediator getMediator() {
        if (this.mediator == null) {
            try {
                // LOOKUP FOR SilvercrestDiscoveryService
                ServiceReference<?>[] references = this.bundleContext
                        .getAllServiceReferences(DiscoveryService.class.getName(), null);
                if (references != null) {
                    for (ServiceReference<?> reference : references) {
                        if (this.bundleContext.getServiceObjects(reference)
                                .getService() instanceof SilvercrestWifiSocketDiscoveryService) {
                            SilvercrestWifiSocketDiscoveryService discoveryService = (SilvercrestWifiSocketDiscoveryService) this.bundleContext
                                    .getServiceObjects(reference).getService();
                            this.mediator = discoveryService.getMediator();
                            break;
                        }
                    }
                }
            } catch (InvalidSyntaxException e) {
                logger.debug("Error looking up for SilvercrestDiscoveryService to get the WifiSocketMediator: {}",
                        e.getMessage());
            }
        }
        return this.mediator;
    }

}
