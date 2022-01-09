/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.silvercrestwifisocket.internal;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.silvercrestwifisocket.internal.exceptions.MacAddressNotValidException;
import org.openhab.binding.silvercrestwifisocket.internal.handler.SilvercrestWifiSocketHandler;
import org.openhab.binding.silvercrestwifisocket.internal.handler.SilvercrestWifiSocketMediator;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SilvercrestWifiSocketHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jaime Vaz - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.silvercrestwifisocket")
public class SilvercrestWifiSocketHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(SilvercrestWifiSocketBindingConstants.THING_TYPE_WIFI_SOCKET);

    private final Logger logger = LoggerFactory.getLogger(SilvercrestWifiSocketHandlerFactory.class);

    private SilvercrestWifiSocketMediator mediator;

    /**
     * Used by OSGI to inject the mediator in the handler factory.
     *
     * @param mediator the mediator
     */
    @Reference
    public void setMediator(final SilvercrestWifiSocketMediator mediator) {
        logger.debug("Mediator has been injected on handler factory service.");
        this.mediator = mediator;
    }

    /**
     * Used by OSGI to unsets the mediator from the handler factory.
     *
     * @param mediator the mediator
     */
    public void unsetMediator(final SilvercrestWifiSocketMediator mitsubishiMediator) {
        logger.debug("Mediator has been unsetted from discovery service.");
        this.mediator = null;
    }

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
                if (this.mediator != null) {
                    this.mediator.registerThingAndWifiSocketHandler(thing, handler);
                } else {
                    logger.error(
                            "The mediator is missing on Handler factory. Without one mediator the handler cannot work!");
                    return null;
                }
                return handler;
            } catch (MacAddressNotValidException e) {
                logger.debug("The mac address passed to WifiSocketHandler by configurations is not valid.");
            }
        }
        return null;
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        if (this.mediator != null) {
            this.mediator.unregisterWifiSocketHandlerByThing(thing);
        }
        super.unregisterHandler(thing);
    }
}
