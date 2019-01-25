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
package org.openhab.binding.wemo.internal;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.UDN;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.wemo.internal.discovery.WemoLinkDiscoveryService;
import org.openhab.binding.wemo.internal.handler.WemoBridgeHandler;
import org.openhab.binding.wemo.internal.handler.WemoCoffeeHandler;
import org.openhab.binding.wemo.internal.handler.WemoHandler;
import org.openhab.binding.wemo.internal.handler.WemoLightHandler;
import org.openhab.binding.wemo.internal.handler.WemoMakerHandler;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Kai Kreuzer - some refactoring for performance and simplification
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.wemo")
public class WemoHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(WemoHandlerFactory.class);

    private UpnpIOService upnpIOService;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = WemoBindingConstants.SUPPORTED_THING_TYPES;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID != null) {
            logger.debug("Trying to create a handler for ThingType '{}", thingTypeUID);

            WemoHttpCall wemoHttpcaller = new WemoHttpCall();

            if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_BRIDGE)) {
                logger.debug("Creating a WemoBridgeHandler for thing '{}' with UDN '{}'", thing.getUID(),
                        thing.getConfiguration().get(UDN));
                WemoBridgeHandler handler = new WemoBridgeHandler((Bridge) thing);
                registerDeviceDiscoveryService(handler, wemoHttpcaller);
                return handler;
            } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_MAKER)) {
                logger.debug("Creating a WemoMakerHandler for thing '{}' with UDN '{}'", thing.getUID(),
                        thing.getConfiguration().get(UDN));
                return new WemoMakerHandler(thing, upnpIOService, wemoHttpcaller);
            } else if (WemoBindingConstants.SUPPORTED_DEVICE_THING_TYPES.contains(thing.getThingTypeUID())) {
                logger.debug("Creating a WemoHandler for thing '{}' with UDN '{}'", thing.getUID(),
                        thing.getConfiguration().get(UDN));
                return new WemoHandler(thing, upnpIOService, wemoHttpcaller);
            } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_COFFEE)) {
                logger.debug("Creating a WemoCoffeeHandler for thing '{}' with UDN '{}'", thing.getUID(),
                        thing.getConfiguration().get(UDN));
                return new WemoCoffeeHandler(thing, upnpIOService, wemoHttpcaller);
            } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_MZ100)) {
                return new WemoLightHandler(thing, upnpIOService, wemoHttpcaller);
            } else {
                logger.warn("ThingHandler not found for {}", thingTypeUID);
                return null;
            }
        }
        return null;
    }

    @Reference
    protected void setUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = upnpIOService;
    }

    protected void unsetUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof WemoBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }

    private synchronized void registerDeviceDiscoveryService(WemoBridgeHandler wemoBridgeHandler,
            WemoHttpCall wemoHttpCaller) {
        WemoLinkDiscoveryService discoveryService = new WemoLinkDiscoveryService(wemoBridgeHandler, upnpIOService,
                wemoHttpCaller);
        this.discoveryServiceRegs.put(wemoBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

}
