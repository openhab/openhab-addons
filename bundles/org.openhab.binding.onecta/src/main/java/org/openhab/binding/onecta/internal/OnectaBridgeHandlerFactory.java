/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.handler.*;
import org.openhab.binding.onecta.internal.service.DeviceDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TimeZoneProvider;
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

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_THING_TYPE, DEVICE_THING_TYPE,
            GATEWAY_THING_TYPE, WATERTANK_THING_TYPE, INDOORUNIT_THING_TYPE);
    private HttpClientFactory httpClientFactory;
    private final TimeZoneProvider timeZoneProvider;

    private @Nullable OnectaBridgeHandler bridgeHandler = null;

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Activate
    public OnectaBridgeHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClientFactory = httpClientFactory;
        this.timeZoneProvider = timeZoneProvider;
        OnectaConfiguration.setHttpClientFactory(httpClientFactory);
        OnectaConnectionClient.SetConnectionClient(httpClientFactory);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals((BRIDGE_THING_TYPE))) {
            bridgeHandler = new OnectaBridgeHandler((Bridge) thing);
            OnectaConfiguration.setBridgeThing((Bridge) thing);

            DeviceDiscoveryService deviceDiscoveryService = new DeviceDiscoveryService(bridgeHandler);
            bridgeHandler.setDiscovery(deviceDiscoveryService);

            this.discoveryServiceRegs.put(thing.getUID(), bundleContext.registerService(
                    DiscoveryService.class.getName(), deviceDiscoveryService, new Hashtable<String, Object>()));

            return bridgeHandler;

        } else if (thingTypeUID.equals((DEVICE_THING_TYPE))) {
            return new OnectaDeviceHandler(thing);
        } else if (thingTypeUID.equals((GATEWAY_THING_TYPE))) {
            return new OnectaGatewayHandler(thing);
        } else if (thingTypeUID.equals((WATERTANK_THING_TYPE))) {
            return new OnectaWaterTankHandler(thing);
        } else if (thingTypeUID.equals((INDOORUNIT_THING_TYPE))) {
            return new OnectaIndoorUnitHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler handler) {
        if (handler.getThing().getThingTypeUID().equals(BRIDGE_THING_TYPE)) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(handler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(handler.getThing().getUID());
                bridgeHandler = null;
            }
        }
        super.removeHandler(handler);
    }
}
