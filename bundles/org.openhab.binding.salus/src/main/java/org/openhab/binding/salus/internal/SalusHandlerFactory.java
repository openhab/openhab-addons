/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal;

import static org.openhab.binding.salus.internal.SalusBindingConstants.SALUS_DEVICE_TYPE;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SALUS_IT600_DEVICE_TYPE;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SALUS_SERVER_TYPE;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.discovery.CloudDiscovery;
import org.openhab.binding.salus.internal.handler.CloudBridgeHandler;
import org.openhab.binding.salus.internal.handler.DeviceHandler;
import org.openhab.binding.salus.internal.handler.It600Handler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SalusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.salus", service = ThingHandlerFactory.class)
public class SalusHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SalusHandlerFactory.class);
    protected final @NonNullByDefault({}) HttpClientFactory httpClientFactory;

    @Activate
    public SalusHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        var thingTypeUID = thing.getThingTypeUID();

        if (SALUS_DEVICE_TYPE.equals(thingTypeUID)) {
            return newSalusDevice(thing);
        }
        if (SALUS_IT600_DEVICE_TYPE.equals(thingTypeUID)) {
            return newIt600(thing);
        }
        if (SALUS_SERVER_TYPE.equals(thingTypeUID)) {
            return newSalusCloudBridge(thing);
        }

        return null;
    }

    private ThingHandler newSalusDevice(Thing thing) {
        logger.debug("New Salus Device {}", thing.getUID().getId());
        return new DeviceHandler(thing);
    }

    private ThingHandler newIt600(Thing thing) {
        logger.debug("Registering IT600");
        return new It600Handler(thing);
    }

    private ThingHandler newSalusCloudBridge(Thing thing) {
        var handler = new CloudBridgeHandler((Bridge) thing, httpClientFactory);
        var cloudDiscovery = new CloudDiscovery(handler, handler, handler.getThing().getUID());
        registerThingDiscovery(cloudDiscovery);
        return handler;
    }

    private synchronized void registerThingDiscovery(DiscoveryService discoveryService) {
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
    }
}
