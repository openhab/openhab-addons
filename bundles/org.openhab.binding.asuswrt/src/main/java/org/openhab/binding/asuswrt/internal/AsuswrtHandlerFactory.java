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
package org.openhab.binding.asuswrt.internal;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtErrorConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.asuswrt.internal.things.AsuswrtClient;
import org.openhab.binding.asuswrt.internal.things.AsuswrtInterface;
import org.openhab.binding.asuswrt.internal.things.AsuswrtRouter;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AsuswrtHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.asuswrt", service = ThingHandlerFactory.class)
public class AsuswrtHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtHandlerFactory.class);
    private final Set<AsuswrtRouter> routerHandlers = new HashSet<>();
    private final HttpClient httpClient;

    public AsuswrtHandlerFactory() {
        // Set SslContextfactory
        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        if (HTTP_SSL_TRUST_ALL) {
            sslContextFactory.setTrustAll(true);
            sslContextFactory.setEndpointIdentificationAlgorithm(null);
        }
        // Create new httpClient
        httpClient = new HttpClient(sslContextFactory);
        httpClient.setFollowRedirects(false);
        httpClient.setMaxConnectionsPerDestination(HTTP_MAX_CONNECTIONS);
        httpClient.setMaxRequestsQueuedPerDestination(HTTP_MAX_QUEUED_REQUESTS);
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error(ERR_HTTP_CLIENT_FAILED);
        }
    }

    @Deactivate
    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Unable to stop httpClient");
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ROUTER.equals(thingTypeUID)) {
            AsuswrtRouter router = new AsuswrtRouter((Bridge) thing, this.httpClient);
            routerHandlers.add(router);
            return router;
        } else if (THING_TYPE_CLIENT.equals(thingTypeUID)) {
            AsuswrtRouter router = getRouter(thing);
            if (router != null) {
                return new AsuswrtClient(thing, router);
            }
        } else if (THING_TYPE_INTERFACE.equals(thingTypeUID)) {
            AsuswrtRouter router = getRouter(thing);
            if (router != null) {
                return new AsuswrtInterface(thing, router);
            }
        }
        return null;
    }

    /**
     * Gets the {@link AsuswrtRouter} handler (Bridge) from a Thing.
     */
    protected @Nullable AsuswrtRouter getRouter(Thing thing) {
        ThingUID bridgeUID = thing.getBridgeUID();
        if (bridgeUID != null) {
            for (AsuswrtRouter router : routerHandlers) {
                if (bridgeUID.equals(router.getUID())) {
                    return router;
                }
            }
        }
        logger.warn(ERR_BRIDGE_NOT_DECLARED);
        return null;
    }
}
