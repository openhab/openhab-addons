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
package org.openhab.binding.onecta.internal;

import static org.openhab.binding.onecta.internal.constants.OnectaBridgeConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.handler.*;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthTokenRefresher;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link OnectaBridgeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.onecta", service = ThingHandlerFactory.class)
public class OnectaBridgeHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE,
            THING_TYPE_CLIMATECONTROL, THING_TYPE_GATEWAY, THING_TYPE_WATERTANK);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final OnectaTranslationProvider translation;

    @Activate
    public OnectaBridgeHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference OAuthTokenRefresher openHabOAuthTokenRefresher,
            @Reference OnectaTranslationProvider translation) {
        this.translation = translation;
        OnectaConfiguration.setTranslation(translation);
        OnectaConfiguration.setHttpClientFactory(httpClientFactory);
        OnectaConfiguration.setOAuthTokenRefresher(openHabOAuthTokenRefresher);
        OnectaConfiguration.getOnectaConnectionClient().openConnecttion();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals((THING_TYPE_BRIDGE))) {
            OnectaBridgeHandler bridgeHandler = new OnectaBridgeHandler((Bridge) thing);
            OnectaConfiguration.setBridgeThing((Bridge) thing);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_CLIMATECONTROL)) {
            return new OnectaDeviceHandler(thing);
        } else if (thingTypeUID.equals((THING_TYPE_GATEWAY))) {
            return new OnectaGatewayHandler(thing);
        } else if (thingTypeUID.equals((THING_TYPE_WATERTANK))) {
            return new OnectaWaterTankHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler handler) {
        if (handler.getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE)) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(handler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(handler.getThing().getUID());
            }
        }
        super.removeHandler(handler);
    }
}
