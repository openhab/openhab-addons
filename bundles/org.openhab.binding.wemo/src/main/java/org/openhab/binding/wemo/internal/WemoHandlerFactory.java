/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wemo.internal.handler.WemoBridgeHandler;
import org.openhab.binding.wemo.internal.handler.WemoCoffeeHandler;
import org.openhab.binding.wemo.internal.handler.WemoCrockpotHandler;
import org.openhab.binding.wemo.internal.handler.WemoDimmerHandler;
import org.openhab.binding.wemo.internal.handler.WemoHolmesHandler;
import org.openhab.binding.wemo.internal.handler.WemoInsightHandler;
import org.openhab.binding.wemo.internal.handler.WemoLightHandler;
import org.openhab.binding.wemo.internal.handler.WemoMakerHandler;
import org.openhab.binding.wemo.internal.handler.WemoMotionHandler;
import org.openhab.binding.wemo.internal.handler.WemoSwitchHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.transport.upnp.UpnpIOService;
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
 * The {@link WemoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Kai Kreuzer - some refactoring for performance and simplification
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.wemo")
public class WemoHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(WemoHandlerFactory.class);

    private final UpnpIOService upnpIOService;
    private final HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return WemoBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Activate
    public WemoHandlerFactory(final @Reference UpnpIOService upnpIOService,
            final @Reference HttpClientFactory httpClientFactory) {
        this.upnpIOService = upnpIOService;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Trying to create a handler for ThingType '{}", thingTypeUID);

        if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_BRIDGE)) {
            logger.debug("Creating a WemoBridgeHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));
            return new WemoBridgeHandler((Bridge) thing, upnpIOService, httpClient);
        } else if (WemoBindingConstants.THING_TYPE_INSIGHT.equals(thing.getThingTypeUID())) {
            logger.debug("Creating a WemoInsightHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));
            return new WemoInsightHandler(thing, upnpIOService, httpClient);
        } else if (WemoBindingConstants.THING_TYPE_SOCKET.equals(thing.getThingTypeUID())
                || WemoBindingConstants.THING_TYPE_LIGHTSWITCH.equals(thing.getThingTypeUID())) {
            logger.debug("Creating a WemoSwitchHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));
            return new WemoSwitchHandler(thing, upnpIOService, httpClient);
        } else if (WemoBindingConstants.THING_TYPE_MOTION.equals(thing.getThingTypeUID())) {
            logger.debug("Creating a WemoMotionHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));
            return new WemoMotionHandler(thing, upnpIOService, httpClient);
        } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_MAKER)) {
            logger.debug("Creating a WemoMakerHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));
            return new WemoMakerHandler(thing, upnpIOService, httpClient);
        } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_COFFEE)) {
            logger.debug("Creating a WemoCoffeeHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get(UDN));
            return new WemoCoffeeHandler(thing, upnpIOService, httpClient);
        } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_DIMMER)) {
            logger.debug("Creating a WemoDimmerHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get("udn"));
            return new WemoDimmerHandler(thing, upnpIOService, httpClient);
        } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_CROCKPOT)) {
            logger.debug("Creating a WemoCockpotHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get("udn"));
            return new WemoCrockpotHandler(thing, upnpIOService, httpClient);
        } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_PURIFIER)) {
            logger.debug("Creating a WemoHolmesHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get("udn"));
            return new WemoHolmesHandler(thing, upnpIOService, httpClient);
        } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_HUMIDIFIER)) {
            logger.debug("Creating a WemoHolmesHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get("udn"));
            return new WemoHolmesHandler(thing, upnpIOService, httpClient);
        } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_HEATER)) {
            logger.debug("Creating a WemoHolmesHandler for thing '{}' with UDN '{}'", thing.getUID(),
                    thing.getConfiguration().get("udn"));
            return new WemoHolmesHandler(thing, upnpIOService, httpClient);
        } else if (thingTypeUID.equals(WemoBindingConstants.THING_TYPE_MZ100)) {
            return new WemoLightHandler(thing, upnpIOService, httpClient);
        } else {
            logger.warn("ThingHandler not found for {}", thingTypeUID);
            return null;
        }
    }
}
