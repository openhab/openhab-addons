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
package org.openhab.binding.veluxactive.internal;

import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.crypto.SigningKeyStore;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveAccountHandler;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveDepartureSwitchHandler;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveGatewayHandler;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveIndoorSensorHandler;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveOpeningHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TimeZoneProvider;
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

/**
 * Creates VELUX ACTIVE things and thing handlers.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.veluxactive", service = ThingHandlerFactory.class)
public class VeluxActiveHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_GATEWAY,
            THING_TYPE_INDOOR_SENSOR, THING_TYPE_DEPARTURE_SWITCH, THING_TYPE_OPENING);

    private final OAuthFactory oAuthFactory;
    private final SigningKeyStore signingKeyStore;
    private final VeluxActiveApi api;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public VeluxActiveHandlerFactory(@Reference OAuthFactory oAuthFactory,
            @Reference HttpClientFactory httpClientFactory, @Reference TimeZoneProvider timeZoneProvider,
            @Reference SigningKeyStore signingKeyStore) {
        this.oAuthFactory = oAuthFactory;
        this.signingKeyStore = signingKeyStore;
        this.api = new VeluxActiveApi(httpClientFactory.getCommonHttpClient());
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_ACCOUNT.equals(thing.getThingTypeUID()) && thing instanceof Bridge bridge) {
            return new VeluxActiveAccountHandler(bridge, oAuthFactory, api, timeZoneProvider);
        }
        if (THING_TYPE_GATEWAY.equals(thing.getThingTypeUID()) && thing instanceof Bridge bridge) {
            return new VeluxActiveGatewayHandler(bridge, signingKeyStore);
        }
        if (THING_TYPE_INDOOR_SENSOR.equals(thing.getThingTypeUID())) {
            return new VeluxActiveIndoorSensorHandler(thing);
        }
        if (THING_TYPE_DEPARTURE_SWITCH.equals(thing.getThingTypeUID())) {
            return new VeluxActiveDepartureSwitchHandler(thing);
        }
        if (THING_TYPE_OPENING.equals(thing.getThingTypeUID())) {
            return new VeluxActiveOpeningHandler(thing);
        }
        return null;
    }
}
