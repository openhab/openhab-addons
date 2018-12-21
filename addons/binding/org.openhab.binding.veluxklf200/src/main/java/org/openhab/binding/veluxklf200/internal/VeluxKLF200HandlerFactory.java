/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.veluxklf200.internal.discovery.KLF200DiscoveryService;
import org.openhab.binding.veluxklf200.internal.handler.KLF200BlindHandler;
import org.openhab.binding.veluxklf200.internal.handler.KLF200BridgeHandler;
import org.openhab.binding.veluxklf200.internal.handler.KLF200SceneHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxKLF200HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author mkf - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.veluxklf200", service = ThingHandlerFactory.class)
public class VeluxKLF200HandlerFactory extends BaseThingHandlerFactory {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(VeluxKLF200HandlerFactory.class);

    /** A registry of things we have discovered. */
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory#supportsThingType(org.eclipse.smarthome.core.thing.
     * ThingTypeUID)
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return VeluxKLF200BindingConstants.SUPPORTED_VELUX_KLF200_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory#createHandler(org.eclipse.smarthome.core.thing.
     * Thing)
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(VeluxKLF200BindingConstants.THING_TYPE_VELUX_KLF200)) {
            KLF200BridgeHandler handler = new KLF200BridgeHandler((Bridge) thing);
            registerDiscoveryService(handler);
            logger.debug("Creating the bridge handler.");
            return handler;
        }
        if (thingTypeUID.equals(VeluxKLF200BindingConstants.THING_TYPE_VELUX_BLIND)) {
            logger.debug("Creating the blind handler.");
            return new KLF200BlindHandler(thing);
        }
        if (thingTypeUID.equals(VeluxKLF200BindingConstants.THING_TYPE_VELUX_SCENE)) {
            logger.debug("Creating the scene handler.");
            return new KLF200SceneHandler(thing);
        }
        return null;
    }

    /**
     * Registers a discovery service for the bridge handler.
     *
     * @param bridgeHandler handler to register service for
     */
    private synchronized void registerDiscoveryService(KLF200BridgeHandler bridgeHandler) {
        logger.debug("Registering discovery service for the KLF200 bridgeHandler");
        KLF200DiscoveryService discoveryService = new KLF200DiscoveryService(bridgeHandler);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory#removeHandler(org.eclipse.smarthome.core.thing.
     * binding.ThingHandler)
     */
    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        logger.debug("Removing handler: {}.", thingHandler);
        if (thingHandler instanceof KLF200BridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            serviceReg.unregister();
        }
    }

}
