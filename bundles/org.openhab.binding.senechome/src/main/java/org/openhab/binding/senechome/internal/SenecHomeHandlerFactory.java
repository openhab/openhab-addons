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
package org.openhab.binding.senechome.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SenecHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Steven Schwarznau - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.senechome", service = ThingHandlerFactory.class)
public class SenecHomeHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(SenecHomeHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(SenecHomeBindingConstants.THING_TYPE_SENEC_HOME_BATTERY);

    private final HttpClient httpClient;

    @Activate
    public SenecHomeHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client(true); // Accept all certificates
        this.httpClient = httpClientFactory.createHttpClient(SenecHomeBindingConstants.BINDING_ID, sslContextFactory);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SenecHomeBindingConstants.THING_TYPE_SENEC_HOME_BATTERY.equals(thingTypeUID)) {
            return new SenecHomeHandler(thing, httpClient);
        }

        return null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.warn("cannot start Jetty-Http-Client", e);
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);

        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("cannot stop Jetty-Http-Client", e);
        }
    }
}
