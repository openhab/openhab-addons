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
package org.openhab.binding.lgwebos.internal;

import static org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.core.io.net.http.WebSocketFactory;
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
 * The {@link LGWebOSHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.lgwebos")
public class LGWebOSHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(LGWebOSHandlerFactory.class);

    private final WebSocketClient webSocketClient;

    private final LGWebOSStateDescriptionOptionProvider stateDescriptionProvider;

    @Activate
    public LGWebOSHandlerFactory(final @Reference WebSocketFactory webSocketFactory,
            final @Reference LGWebOSStateDescriptionOptionProvider stateDescriptionProvider) {
        /*
         * Cannot use openHAB's shared web socket client (webSocketFactory.getCommonWebSocketClient()) as we have to
         * change client settings.
         */
        this.webSocketClient = webSocketFactory.createWebSocketClient(BINDING_ID, new SslContextFactory.Client(true));
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_WEBOSTV)) {
            return new LGWebOSHandler(thing, webSocketClient, stateDescriptionProvider);
        }
        return null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        // reduce timeout from default 15sec
        this.webSocketClient.setConnectTimeout(1000);

        // channel and app listing are json docs up to 4MB
        this.webSocketClient.getPolicy().setMaxTextMessageSize(4 * 1024 * 1024);

        // since this is not using openHAB's shared web socket client we need to start and stop
        try {
            this.webSocketClient.start();
        } catch (Exception e) {
            logger.warn("Unable to to start websocket client.", e);
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        try {
            this.webSocketClient.stop();
        } catch (Exception e) {
            logger.warn("Unable to to stop websocket client.", e);
        }
    }
}
