/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeathomesystem.internal;

import static org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeActuatorHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeBridgeHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeDimmingHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeDoorRingSensor;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeRuleHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeSceneHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeShutterHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeThermostatHandler;
import org.openhab.binding.freeathomesystem.internal.handler.FreeAtHomeWindowSensorHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link FreeAtHomeSystemHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andras Uhrin - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.freeathomesystem", service = ThingHandlerFactory.class)
public class FreeAtHomeSystemHandlerFactory extends BaseThingHandlerFactory {

    private @NonNullByDefault({}) HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeBridgeHandler((Bridge) thing, httpClient);
        } else if (ACTUATOR_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeActuatorHandler(thing);
        } else if (DOOROPENER_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeActuatorHandler(thing);
        } else if (CORRIDORLIGHTSWITCH_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeActuatorHandler(thing);
        } else if (THERMOSTAT_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeThermostatHandler(thing);
        } else if (SHUTTERACTUATOR_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeShutterHandler(thing);
        } else if (WINDOWSENSOR_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeWindowSensorHandler(thing);
        } else if (DIMMINGACTUATOR_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeDimmingHandler(thing);
        } else if (DOORRINGSENSOR_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeDoorRingSensor(thing);
        } else if (SCENE_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeSceneHandler(thing);
        } else if (RULE_TYPE_UID.equals(thingTypeUID)) {
            return new FreeAtHomeRuleHandler(thing);
        }

        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.createHttpClient("FreeAtHome");
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }
}
