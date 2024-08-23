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
package org.openhab.binding.tesla.internal;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler;
import org.openhab.binding.tesla.internal.handler.TeslaVehicleHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeMigrationService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link TeslaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 * @author Nicolai Grødum - Adding token based auth
 * @author Kai Kreuzer - Introduced account handler
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tesla")
@NonNullByDefault
public class TeslaHandlerFactory extends BaseThingHandlerFactory {

    private static final int EVENT_STREAM_CONNECT_TIMEOUT = 3;
    private static final int EVENT_STREAM_READ_TIMEOUT = 200;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_VEHICLE,
            THING_TYPE_MODELS, THING_TYPE_MODEL3, THING_TYPE_MODELX, THING_TYPE_MODELY);

    private final ClientBuilder clientBuilder;
    private final HttpClientFactory httpClientFactory;
    private final WebSocketFactory webSocketFactory;
    private final ThingTypeMigrationService thingTypeMigrationService;

    @Activate
    public TeslaHandlerFactory(@Reference ClientBuilder clientBuilder, @Reference HttpClientFactory httpClientFactory,
            final @Reference WebSocketFactory webSocketFactory,
            final @Reference ThingTypeMigrationService thingTypeMigrationService) {
        this.clientBuilder = clientBuilder //
                .connectTimeout(EVENT_STREAM_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(EVENT_STREAM_READ_TIMEOUT, TimeUnit.SECONDS);
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;
        this.thingTypeMigrationService = thingTypeMigrationService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            return new TeslaAccountHandler((Bridge) thing, clientBuilder.build(), httpClientFactory,
                    thingTypeMigrationService);
        } else {
            return new TeslaVehicleHandler(thing, webSocketFactory);
        }
    }
}
