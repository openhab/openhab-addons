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
package org.openhab.binding.plugwiseha.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.client.HttpClient;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.HttpClientInitializationException;
import org.openhab.binding.plugwiseha.internal.discovery.PlugwiseHADiscoveryService;
import org.openhab.binding.plugwiseha.internal.handler.PlugwiseHAApplianceHandler;
import org.openhab.binding.plugwiseha.internal.handler.PlugwiseHABridgeHandler;
import org.openhab.binding.plugwiseha.internal.handler.PlugwiseHAZoneHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PlugwiseHAHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Bas van Wetten - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.plugwiseha")
public class PlugwiseHAHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(PlugwiseHAHandlerFactory.class);
    private HttpClient httpClient;

    // Constructor

    public PlugwiseHAHandlerFactory() {
        this.httpClient = new HttpClient();
        try {
            this.httpClient.start();
        } catch (Exception e) {
            throw new HttpClientInitializationException("Could not start HttpClient", e);
        }
    }

    // Public methods

    /**
     * Returns whether the handler is able to create a thing or register a thing
     * handler for the given type.
     *
     * @param thingTypeUID the thing type UID
     * @return true, if the handler supports the thing type, false otherwise
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (PlugwiseHABridgeHandler.supportsThingType(thingTypeUID)
                || PlugwiseHAZoneHandler.supportsThingType(thingTypeUID))
                || PlugwiseHAApplianceHandler.supportsThingType(thingTypeUID);
    }

    /**
     * Creates a thing for given arguments.
     *
     * @param thingTypeUID  thing type uid (not null)
     * @param configuration configuration
     * @param thingUID      thing uid, which can be null
     * @param bridgeUID     bridge uid, which can be null
     * @return created thing
     */
    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (PlugwiseHABridgeHandler.supportsThingType(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (PlugwiseHAZoneHandler.supportsThingType(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        } else if (PlugwiseHAApplianceHandler.supportsThingType(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }

        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the plugwiseha binding.");
    }

    // Protected and private methods
    
    /**
     * Creates a {@link ThingHandler} for the given thing.
     *
     * @param thing the thing
     * @return thing the created handler
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (PlugwiseHABridgeHandler.supportsThingType(thingTypeUID)) {
            this.logger.debug("Creating new Plugwise Home Automation Bridge");
            PlugwiseHABridgeHandler bridge = new PlugwiseHABridgeHandler((Bridge) thing, this.httpClient);
            registerPlugwiseHADiscoveryService(bridge);
            return bridge;
        } else if (PlugwiseHAZoneHandler.supportsThingType(thingTypeUID)) {
            logger.debug("Creating new Plugwise Home Automation Zone");
            return new PlugwiseHAZoneHandler(thing);
        } else if (PlugwiseHAApplianceHandler.supportsThingType(thingTypeUID)) {
            logger.debug("Creating new Plugwise Home Automation Appliance");
            return new PlugwiseHAApplianceHandler(thing);
        }
        return null;
    }

    private synchronized void registerPlugwiseHADiscoveryService(PlugwiseHABridgeHandler bridgeHandler) {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        PlugwiseHADiscoveryService discoveryService = new PlugwiseHADiscoveryService(bridgeHandler);

        ServiceRegistration<?> serviceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, properties);

        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), serviceRegistration);

        discoveryService.activate();
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof PlugwiseHABridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                PlugwiseHADiscoveryService service = (PlugwiseHADiscoveryService) bundleContext
                        .getService(serviceReg.getReference());

                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }
}
