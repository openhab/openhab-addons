/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.vwweconnect.internal.handler.VWWeConnectBridgeHandler;
import org.openhab.binding.vwweconnect.internal.handler.VehicleHandler;
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
 * The {@link VWWeConnectHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.vwweconnect")
public class VWWeConnectHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(VWWeConnectHandlerFactory.class);
    private static final boolean DEBUG = true;

    private @NonNullByDefault({}) HttpClient httpClient;

    @Activate
    public VWWeConnectHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        logger.debug("VWWeConnectHandlerFactory this: {}", this);
        this.httpClient = httpClientFactory.getCommonHttpClient();
        if (DEBUG) {
            SslContextFactory sslFactory = new SslContextFactory(true);
            this.httpClient = new HttpClient(sslFactory);
            this.httpClient.setFollowRedirects(false);
            try {
                this.httpClient.start();
            } catch (Exception e) {
                logger.error("Exception: {}", e.getMessage());
            }
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return VWWeConnectBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    public VWWeConnectHandlerFactory() {
        super();
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("createHandler this: {}", thing);
        final ThingHandler thingHandler;
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (VWWeConnectBindingConstants.BRIDGE_THING_TYPE.equals(thing.getThingTypeUID())) {
            logger.debug("Create VWWeConnectBridgeHandler");
            thingHandler = new VWWeConnectBridgeHandler((Bridge) thing, httpClient);
        } else if (VWWeConnectBindingConstants.VEHICLE_THING_TYPE.equals(thing.getThingTypeUID())) {
            logger.debug("Create VehicleHandler {}", thing.getThingTypeUID());
            thingHandler = new VehicleHandler(thing);
        } else {
            logger.debug("Not possible to create thing handler for thing {}", thing);
            thingHandler = null;
        }
        return thingHandler;
    }
}
