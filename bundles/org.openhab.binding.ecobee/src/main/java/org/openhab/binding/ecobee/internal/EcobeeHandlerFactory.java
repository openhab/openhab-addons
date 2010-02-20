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
package org.openhab.binding.ecobee.internal;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.ecobee.internal.handler.EcobeeAccountBridgeHandler;
import org.openhab.binding.ecobee.internal.handler.EcobeeSensorThingHandler;
import org.openhab.binding.ecobee.internal.handler.EcobeeThermostatBridgeHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EcobeeHandlerFactory} is responsible for creating thing handlers
 * for the account bridge, thermostat bridge, and sensor thing.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ecobee", service = ThingHandlerFactory.class)
public class EcobeeHandlerFactory extends BaseThingHandlerFactory {

    private final TimeZoneProvider timeZoneProvider;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    @Activate
    public EcobeeHandlerFactory(@Reference TimeZoneProvider timeZoneProvider,
            @Reference ChannelTypeRegistry channelTypeRegistry, @Reference OAuthFactory oAuthFactory,
            @Reference HttpClientFactory httpClientFactory) {
        this.timeZoneProvider = timeZoneProvider;
        this.channelTypeRegistry = channelTypeRegistry;
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_ACCOUNT_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new EcobeeAccountBridgeHandler((Bridge) thing, oAuthFactory, httpClient);
        }
        if (SUPPORTED_THERMOSTAT_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new EcobeeThermostatBridgeHandler((Bridge) thing, timeZoneProvider, channelTypeRegistry);
        }
        if (SUPPORTED_SENSOR_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new EcobeeSensorThingHandler(thing);
        }
        return null;
    }
}
