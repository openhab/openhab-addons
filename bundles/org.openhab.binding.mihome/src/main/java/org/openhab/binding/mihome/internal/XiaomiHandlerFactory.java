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
package org.openhab.binding.mihome.internal;

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.mihome.internal.discovery.XiaomiItemDiscoveryService;
import org.openhab.binding.mihome.internal.handler.XiaomiActorCurtainHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiActorGatewayHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiActorPlugHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiAqaraActorSwitch1Handler;
import org.openhab.binding.mihome.internal.handler.XiaomiAqaraActorSwitch2Handler;
import org.openhab.binding.mihome.internal.handler.XiaomiAqaraSensorSwitch1Handler;
import org.openhab.binding.mihome.internal.handler.XiaomiAqaraSensorSwitch2Handler;
import org.openhab.binding.mihome.internal.handler.XiaomiBridgeHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiDeviceBaseHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorCubeHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorGasHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorHtHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorLockHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorMagnetHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorMotionHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorSmokeHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorSwitchHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorVibrationHandler;
import org.openhab.binding.mihome.internal.handler.XiaomiSensorWaterHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link XiaomiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt - Refactor, add devices
 * @author Daniel Walters - Added Aqara Door/Window sensor and Aqara temperature, humidity and pressure sensor
 * @author Kuba Wolanin - Added Water Leak sensor and Aqara motion sensor
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.mihome")
public class XiaomiHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.concat(XiaomiBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    XiaomiDeviceBaseHandler.SUPPORTED_THING_TYPES.stream()).collect(Collectors.toSet()));

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (XiaomiBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID newBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, newBridgeUID, null);
        } else if (XiaomiDeviceBaseHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID newThingUID = getThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, newThingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the mihome binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(SERIAL_NUMBER);
            return new ThingUID(thingTypeUID, serialNumber);
        }
        return thingUID;
    }

    private ThingUID getThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (thingUID == null) {
            String itemId = (String) configuration.get(ITEM_ID);
            return new ThingUID(thingTypeUID, itemId, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            XiaomiBridgeHandler handler = new XiaomiBridgeHandler((Bridge) thing);
            registerItemDiscoveryService(handler);
            return handler;
        } else if (THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new XiaomiActorGatewayHandler(thing);
        } else if (THING_TYPE_SENSOR_HT.equals(thingTypeUID)) {
            return new XiaomiSensorHtHandler(thing);
        } else if (THING_TYPE_SENSOR_MOTION.equals(thingTypeUID)) {
            return new XiaomiSensorMotionHandler(thing);
        } else if (THING_TYPE_SENSOR_SWITCH.equals(thingTypeUID)) {
            return new XiaomiSensorSwitchHandler(thing);
        } else if (THING_TYPE_SENSOR_AQARA_SWITCH.equals(thingTypeUID)) {
            return new XiaomiSensorSwitchHandler(thing);
        } else if (THING_TYPE_SENSOR_MAGNET.equals(thingTypeUID)) {
            return new XiaomiSensorMagnetHandler(thing);
        } else if (THING_TYPE_SENSOR_CUBE.equals(thingTypeUID)) {
            return new XiaomiSensorCubeHandler(thing);
        } else if (THING_TYPE_SENSOR_SMOKE.equals(thingTypeUID)) {
            return new XiaomiSensorSmokeHandler(thing);
        } else if (THING_TYPE_SENSOR_GAS.equals(thingTypeUID)) {
            return new XiaomiSensorGasHandler(thing);
        } else if (THING_TYPE_SENSOR_WATER.equals(thingTypeUID)) {
            return new XiaomiSensorWaterHandler(thing);
        } else if (THING_TYPE_SENSOR_AQARA1.equals(thingTypeUID)) {
            return new XiaomiAqaraSensorSwitch1Handler(thing);
        } else if (THING_TYPE_SENSOR_AQARA2.equals(thingTypeUID)) {
            return new XiaomiAqaraSensorSwitch2Handler(thing);
        } else if (THING_TYPE_ACTOR_AQARA1.equals(thingTypeUID)) {
            return new XiaomiAqaraActorSwitch1Handler(thing);
        } else if (THING_TYPE_ACTOR_AQARA2.equals(thingTypeUID)) {
            return new XiaomiAqaraActorSwitch2Handler(thing);
        } else if (THING_TYPE_ACTOR_PLUG.equals(thingTypeUID)) {
            return new XiaomiActorPlugHandler(thing);
        } else if (THING_TYPE_ACTOR_AQARA_ZERO1.equals(thingTypeUID)) {
            return new XiaomiAqaraActorSwitch1Handler(thing);
        } else if (THING_TYPE_ACTOR_AQARA_ZERO2.equals(thingTypeUID)) {
            return new XiaomiAqaraActorSwitch2Handler(thing);
        } else if (THING_TYPE_ACTOR_CURTAIN.equals(thingTypeUID)) {
            return new XiaomiActorCurtainHandler(thing);
        } else if (THING_TYPE_SENSOR_AQARA_WEATHER_V1.equals(thingTypeUID)) {
            return new XiaomiSensorHtHandler(thing);
        } else if (THING_TYPE_SENSOR_AQARA_MAGNET.equals(thingTypeUID)) {
            return new XiaomiSensorMagnetHandler(thing);
        } else if (THING_TYPE_SENSOR_AQARA_MOTION.equals(thingTypeUID)) {
            return new XiaomiSensorMotionHandler(thing);
        } else if (THING_TYPE_SENSOR_AQARA_VIBRATION.equals(thingTypeUID)) {
            return new XiaomiSensorVibrationHandler(thing);
        } else if (THING_TYPE_SENSOR_AQARA_LOCK.equals(thingTypeUID)) {
            return new XiaomiSensorLockHandler(thing);
        } else if (THING_TYPE_BASIC.equals(thingTypeUID)) {
            return new XiaomiDeviceBaseHandler(thing);
        } else {
            return null;
        }
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof XiaomiBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                XiaomiItemDiscoveryService service = (XiaomiItemDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.onHandlerRemoved();
                    service.deactivate();
                }
            }
        }
    }

    private synchronized void registerItemDiscoveryService(XiaomiBridgeHandler bridgeHandler) {
        XiaomiItemDiscoveryService discoveryService = new XiaomiItemDiscoveryService(bridgeHandler);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
