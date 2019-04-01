/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.somfytahoma.internal.discovery.SomfyTahomaItemDiscoveryService;
import org.openhab.binding.somfytahoma.internal.handler.*;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
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
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(THING_TYPE_BRIDGE,
            THING_TYPE_GATEWAY, THING_TYPE_ROLLERSHUTTER, THING_TYPE_ROLLERSHUTTER_SILENT, THING_TYPE_SCREEN,
            THING_TYPE_VENETIANBLIND, THING_TYPE_EXTERIORSCREEN, THING_TYPE_EXTERIORVENETIANBLIND,
            THING_TYPE_GARAGEDOOR, THING_TYPE_AWNING, THING_TYPE_ACTIONGROUP, THING_TYPE_ONOFF, THING_TYPE_LIGHT,
            THING_TYPE_LIGHTSENSOR, THING_TYPE_SMOKESENSOR, THING_TYPE_CONTACTSENSOR, THING_TYPE_OCCUPANCYSENSOR,
            THING_TYPE_WINDOW, THING_TYPE_INTERNAL_ALARM, THING_TYPE_EXTERNAL_ALARM, THING_TYPE_POD,
            THING_TYPE_HEATING_SYSTEM, THING_TYPE_ONOFF_HEATING_SYSTEM, THING_TYPE_DOOR_LOCK, THING_TYPE_PERGOLA,
            THING_TYPE_WINDOW_HANDLE, THING_TYPE_TEMPERATURESENSOR, THING_TYPE_GATE));

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    @Nullable
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        logger.debug("Creating handler for {}", thing.getThingTypeUID().getId());

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            SomfyTahomaBridgeHandler handler = new SomfyTahomaBridgeHandler((Bridge) thing);
            registerItemDiscoveryService(handler);
            return handler;
        }
        if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            return new SomfyTahomaGatewayHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER)) {
            return new SomfyTahomaRollerShutterHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_ROLLERSHUTTER_SILENT)) {
            return new SomfyTahomaSilentRollerShutterHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_SCREEN) || thingTypeUID.equals(THING_TYPE_EXTERIORSCREEN)) {
            return new SomfyTahomaRollerShutterHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_VENETIANBLIND) || thingTypeUID.equals(THING_TYPE_EXTERIORVENETIANBLIND)) {
            return new SomfyTahomaVenetianBlindHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_GARAGEDOOR)) {
            return new SomfyTahomaRollerShutterHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_AWNING)) {
            return new SomfyTahomaAwningHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_ACTIONGROUP)) {
            return new SomfyTahomaActionGroupHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_ONOFF)) {
            return new SomfyTahomaOnOffHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_LIGHT)) {
            return new SomfyTahomaOnOffHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_LIGHTSENSOR)) {
            return new SomfyTahomaLightSensorHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_SMOKESENSOR)) {
            return new SomfyTahomaSmokeSensorHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_OCCUPANCYSENSOR)) {
            return new SomfyTahomaOccupancySensorHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_CONTACTSENSOR)) {
            return new SomfyTahomaContactSensorHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_WINDOW)) {
            return new SomfyTahomaWindowHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_INTERNAL_ALARM)) {
            return new SomfyTahomaInternalAlarmHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_EXTERNAL_ALARM)) {
            return new SomfyTahomaExternalAlarmHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_POD)) {
            return new SomfyTahomaPodHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_HEATING_SYSTEM)) {
            return new SomfyTahomaHeatingSystemHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_ONOFF_HEATING_SYSTEM)) {
            return new SomfyTahomaOnOffHeatingSystemHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_DOOR_LOCK)) {
            return new SomfyTahomaDoorLockHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_PERGOLA)) {
            return new SomfyTahomaPergolaHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_WINDOW_HANDLE)) {
            return new SomfyTahomaWindowHandleHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_TEMPERATURESENSOR)) {
            return new SomfyTahomaTemperatureSensorHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_GATE)) {
            return new SomfyTahomaGateHandler(thing);
        }
        return null;
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
