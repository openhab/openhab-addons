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
package org.openhab.binding.somfytahoma.internal;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.somfytahoma.internal.discovery.SomfyTahomaItemDiscoveryService;
import org.openhab.binding.somfytahoma.internal.handler.*;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.somfytahoma")
public class SomfyTahomaHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final HttpClient httpClient;

    @Activate
    public SomfyTahomaHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_BRIDGE.equals(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    @Nullable
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Creating handler for {}", thing.getThingTypeUID().getId());

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            SomfyTahomaBridgeHandler handler = new SomfyTahomaBridgeHandler((Bridge) thing, httpClient);
            registerItemDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            return new SomfyTahomaGatewayHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER)) {
            return new SomfyTahomaRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER_SILENT)) {
            return new SomfyTahomaSilentRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER_UNO)) {
            return new SomfyTahomaUnoRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SCREEN) || thingTypeUID.equals(THING_TYPE_EXTERIORSCREEN)) {
            return new SomfyTahomaRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_VENETIANBLIND) || thingTypeUID.equals(THING_TYPE_EXTERIORVENETIANBLIND)) {
            return new SomfyTahomaVenetianBlindHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_GARAGEDOOR)) {
            return new SomfyTahomaRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_AWNING)) {
            return new SomfyTahomaAwningHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ACTIONGROUP)) {
            return new SomfyTahomaActionGroupHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ONOFF)) {
            return new SomfyTahomaOnOffHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_LIGHT)) {
            return new SomfyTahomaOnOffHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_LIGHTSENSOR)) {
            return new SomfyTahomaLightSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SMOKESENSOR)) {
            return new SomfyTahomaSmokeSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OCCUPANCYSENSOR)) {
            return new SomfyTahomaOccupancySensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CONTACTSENSOR)) {
            return new SomfyTahomaContactSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WATERSENSOR)) {
            return new SomfyTahomaWaterSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HUMIDITYSENSOR)) {
            return new SomfyTahomaHumiditySensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WINDOW)) {
            return new SomfyTahomaWindowHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_INTERNAL_ALARM)) {
            return new SomfyTahomaInternalAlarmHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_EXTERNAL_ALARM)) {
            return new SomfyTahomaExternalAlarmHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_POD)) {
            return new SomfyTahomaPodHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HEATING_SYSTEM)) {
            return new SomfyTahomaHeatingSystemHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ONOFF_HEATING_SYSTEM)) {
            return new SomfyTahomaOnOffHeatingSystemHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DOOR_LOCK)) {
            return new SomfyTahomaDoorLockHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_PERGOLA)) {
            return new SomfyTahomaPergolaHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WINDOW_HANDLE)) {
            return new SomfyTahomaWindowHandleHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TEMPERATURESENSOR)) {
            return new SomfyTahomaTemperatureSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_GATE)) {
            return new SomfyTahomaGateHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CURTAIN)) {
            return new SomfyTahomaCurtainHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ELECTRICITYSENSOR)) {
            return new SomfyTahomaElectricitySensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DOCK)) {
            return new SomfyTahomaDockHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SIREN)) {
            return new SomfyTahomaSirenHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ADJUSTABLE_SLATS_ROLLERSHUTTER)) {
            return new SomfyTahomaAdjustableSlatsRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_MYFOX_CAMERA)) {
            return new SomfyTahomaMyfoxCameraHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_MYFOX_ALARM)) {
            return new SomfyTahomaMyfoxAlarmHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_THERMOSTAT)) {
            return new SomfyTahomaThermostatHandler(thing);
        } else {
            return null;
        }
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SomfyTahomaBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            // remove discovery service, if bridge handler is removed
            SomfyTahomaItemDiscoveryService service = (SomfyTahomaItemDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            if (service != null) {
                service.deactivate();
            }
            serviceReg.unregister();
            discoveryServiceRegs.remove(thingHandler.getThing().getUID());
        }
    }

    private synchronized void registerItemDiscoveryService(SomfyTahomaBridgeHandler bridgeHandler) {
        SomfyTahomaItemDiscoveryService discoveryService = new SomfyTahomaItemDiscoveryService(bridgeHandler);
        discoveryService.activate(null);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));

    }
}
