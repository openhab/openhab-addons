/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.eyeonwater.internal;

import static org.openhab.binding.eyeonwater.internal.EyeOnWaterBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.eyeonwater.internal.handler.EyeOnWaterBridgeHandler;
import org.openhab.binding.eyeonwater.internal.handler.EyeOnWaterMeterHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EyeOnWaterHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.eyeonwater", service = ThingHandlerFactory.class)
public class EyeOnWaterHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_METER);

    private final Logger logger = LoggerFactory.getLogger(EyeOnWaterHandlerFactory.class);

    private @Nullable BundleContext bundleContext;

    @Reference
    private @Nullable TimeZoneProvider timeZoneProvider;

    @Reference
    private @Nullable HttpClientFactory httpClientFactory;

    private @Nullable HttpClient httpClient;

    @Activate
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        this.bundleContext = componentContext.getBundleContext();
        HttpClientFactory factory = httpClientFactory;
        if (factory != null) {
            HttpClient client = factory.createHttpClient(BINDING_ID);
            try {
                client.start();
                this.httpClient = client;
            } catch (Exception e) {
                logger.error("Failed to start EyeOnWater HTTP client: {}", e.getMessage(), e);
            }
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        this.bundleContext = null;
        HttpClient client = httpClient;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Failed to stop EyeOnWater HTTP client: {}", e.getMessage());
            }
            this.httpClient = null;
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            BundleContext bc = bundleContext;
            if (bc != null) {
                return new EyeOnWaterBridgeHandler((Bridge) thing, bc, httpClient);
            }
        } else if (THING_TYPE_METER.equals(thingTypeUID)) {
            TimeZoneProvider tzp = timeZoneProvider;
            if (tzp != null) {
                return new EyeOnWaterMeterHandler(thing, tzp);
            }
        }

        return null;
    }
}
