/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.client.HttpClient;
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
import org.openhab.binding.km200.handler.KM200GatewayHandler;
import org.openhab.binding.km200.handler.KM200ThingHandler;
import org.openhab.binding.km200.internal.discovery.KM200GatewayDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link KM200HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Eckhardt - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.km200", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class KM200HandlerFactory extends BaseThingHandlerFactory {

    public final Set<ThingTypeUID> SUPPORTED_ALL_THING_TYPES_UIDS = Sets
            .union(KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS, KM200ThingHandler.SUPPORTED_THING_TYPES_UIDS);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(KM200HandlerFactory.class);

    private KM200ChannelTypeProvider channelTypeProvider;

    /**
     * shared instance of HTTP client for asynchronous calls
     */
    private HttpClient httpClient;

    @Reference
    protected void setChannelTypeProvider(KM200ChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    protected void unsetChannelTypeProvider(KM200ChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_ALL_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID) {
        logger.debug("Create thing UID: {}", thingUID);
        return createThing(thingTypeUID, configuration, thingUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Create thing handler for: {}", thingTypeUID.getAsString());
        if (KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            logger.debug("It's a gataway: {}", thingTypeUID.getAsString());
            KM200GatewayHandler gatewayHandler = new KM200GatewayHandler((Bridge) thing, httpClient);
            registerKM200GatewayDiscoveryService(gatewayHandler);
            return gatewayHandler;
        } else if (KM200ThingHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            logger.debug("It's a thing: {}", thingTypeUID.getAsString());
            return new KM200ThingHandler(thing, channelTypeProvider);
        } else {
            return null;
        }
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof KM200GatewayHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    /**
     * Adds KM200GatewayHandler to the discovery service to find the KMXXX device
     *
     * @param gatewayHandler
     */
    private synchronized void registerKM200GatewayDiscoveryService(KM200GatewayHandler gatewayHandler) {
        KM200GatewayDiscoveryService discoveryService = new KM200GatewayDiscoveryService(gatewayHandler);
        this.discoveryServiceRegs.put(gatewayHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}
