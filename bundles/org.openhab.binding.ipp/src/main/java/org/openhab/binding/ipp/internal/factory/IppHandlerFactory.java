/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ipp.internal.factory;

import static org.openhab.binding.ipp.internal.IppBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipp.internal.handler.IppPrinterHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryServiceRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IppHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tobias Braeutigam - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.ipp")
@NonNullByDefault
public class IppHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(IppHandlerFactory.class);

    private final DiscoveryServiceRegistry discoveryServiceRegistry;

    @Activate
    public IppHandlerFactory(final @Reference DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        logger.trace("createThing({},{},{},{})", thingTypeUID, configuration, thingUID, bridgeUID);
        if (PRINTER_THING_TYPE.equals(thingTypeUID)) {
            ThingUID deviceUID = getIppPrinterUID(thingTypeUID, thingUID, configuration);
            logger.debug("creating thing {} from deviceUID: {}", thingTypeUID, deviceUID);
            return super.createThing(thingTypeUID, configuration, deviceUID, null);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }

    private ThingUID getIppPrinterUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String name = (String) configuration.get(PRINTER_PARAMETER_NAME);
            return new ThingUID(thingTypeUID, name);
        }
        return thingUID;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (PRINTER_THING_TYPE.equals(thingTypeUID)) {
            return new IppPrinterHandler(thing, discoveryServiceRegistry);
        }
        return null;
    }
}
