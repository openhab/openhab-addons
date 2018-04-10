/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ipp.internal;

import static org.openhab.binding.ipp.IppBindingConstants.PRINTER_THING_TYPE;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.ipp.IppBindingConstants;
import org.openhab.binding.ipp.handler.IppPrinterHandler;
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
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.ipp")
public class IppHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(IppHandlerFactory.class);

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    @Reference
    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return IppBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        logger.trace("createThing({},{},{},{})", thingTypeUID, configuration, thingUID, bridgeUID);
        if (IppBindingConstants.PRINTER_THING_TYPE.equals(thingTypeUID)) {
            ThingUID deviceUID = getIppPrinterUID(thingTypeUID, thingUID, configuration);
            logger.debug("creating thing {} from deviceUID: {}", thingTypeUID, deviceUID);
            return super.createThing(thingTypeUID, configuration, deviceUID, null);
        }
        throw new IllegalArgumentException("The thing type {} " + thingTypeUID + " is not supported by the binding.");
    }

    private ThingUID getIppPrinterUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String name = (String) configuration.get(IppBindingConstants.PRINTER_PARAMETER_NAME);
            thingUID = new ThingUID(thingTypeUID, name);
        }
        return thingUID;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(PRINTER_THING_TYPE)) {
            return new IppPrinterHandler(thing, discoveryServiceRegistry);
        }
        return null;
    }
}
