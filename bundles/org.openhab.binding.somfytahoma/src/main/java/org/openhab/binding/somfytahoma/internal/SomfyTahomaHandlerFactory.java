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
package org.openhab.binding.somfytahoma.internal;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaActionGroupHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaAdjustableSlatsRollerShutterHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaAwningHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaBioclimaticPergolaHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaBridgeHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaCarbonDioxideSensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaContactSensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaCurtainHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaDimmerLightHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaDockHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaDoorLockHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaElectricitySensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaExteriorHeatingSystemHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaExternalAlarmHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaGateHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaGatewayHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaHitachiATWHZHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaHitachiATWMCHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaHitachiDHWHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaHumiditySensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaInternalAlarmHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaLightSensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaMyfoxAlarmHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaMyfoxCameraHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaNoiseSensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaOccupancySensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaOnOffHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaOnOffHeatingSystemHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaPergolaHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaPodHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaRainSensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaRollerShutterHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaShutterHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaSilentRollerShutterHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaSirenHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaSmokeSensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaTemperatureSensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaThermostatHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaUnoRollerShutterHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaValveHeatingSystemHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaVenetianBlindHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaWaterHeatingSystemHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaWaterSensorHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaWindowHandleHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaWindowHandler;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaZwaveHeatingSystemHandler;
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

    private final HttpClientFactory httpClientFactory;
    private final SomfyTahomaStateDescriptionOptionProvider stateDescriptionProvider;

    @Activate
    public SomfyTahomaHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            final @Reference SomfyTahomaStateDescriptionOptionProvider stateDescriptionProvider) {
        this.httpClientFactory = httpClientFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
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
            return new SomfyTahomaBridgeHandler((Bridge) thing, httpClientFactory);
        } else if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            return new SomfyTahomaGatewayHandler(thing, stateDescriptionProvider);
        } else if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER)) {
            return new SomfyTahomaRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER_SILENT)) {
            return new SomfyTahomaSilentRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER_UNO)) {
            return new SomfyTahomaUnoRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SCREEN) || thingTypeUID.equals(THING_TYPE_EXTERIORSCREEN)) {
            return new SomfyTahomaRollerShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SHUTTER)) {
            return new SomfyTahomaShutterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_VENETIANBLIND)
                || thingTypeUID.equals(THING_TYPE_EXTERIORVENETIANBLIND)) {
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
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER_LIGHT)) {
            return new SomfyTahomaDimmerLightHandler(thing);
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
        } else if (thingTypeUID.equals(THING_TYPE_VALVE_HEATING_SYSTEM)) {
            return new SomfyTahomaValveHeatingSystemHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ZWAVE_HEATING_SYSTEM)) {
            return new SomfyTahomaZwaveHeatingSystemHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ONOFF_HEATING_SYSTEM)) {
            return new SomfyTahomaOnOffHeatingSystemHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_EXTERIOR_HEATING_SYSTEM)) {
            return new SomfyTahomaExteriorHeatingSystemHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DOOR_LOCK)) {
            return new SomfyTahomaDoorLockHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_PERGOLA)) {
            return new SomfyTahomaPergolaHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BIOCLIMATIC_PERGOLA)) {
            return new SomfyTahomaBioclimaticPergolaHandler(thing);
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
        } else if (thingTypeUID.equals(THING_TYPE_WATERHEATINGSYSTEM)) {
            return new SomfyTahomaWaterHeatingSystemHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HITACHI_ATWHZ)) {
            return new SomfyTahomaHitachiATWHZHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HITACHI_DHW)) {
            return new SomfyTahomaHitachiDHWHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HITACHI_ATWMC)) {
            return new SomfyTahomaHitachiATWMCHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_RAINSENSOR)) {
            return new SomfyTahomaRainSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CARBON_DIOXIDE_SENSOR)) {
            return new SomfyTahomaCarbonDioxideSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_NOISE_SENSOR)) {
            return new SomfyTahomaNoiseSensorHandler(thing);
        } else {
            return null;
        }
    }
}
