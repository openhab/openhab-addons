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
package org.openhab.binding.satel.internal;

import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.config.SatelThingConfig;
import org.openhab.binding.satel.internal.discovery.SatelDeviceDiscoveryService;
import org.openhab.binding.satel.internal.handler.Atd100Handler;
import org.openhab.binding.satel.internal.handler.Ethm1BridgeHandler;
import org.openhab.binding.satel.internal.handler.IntRSBridgeHandler;
import org.openhab.binding.satel.internal.handler.SatelBridgeHandler;
import org.openhab.binding.satel.internal.handler.SatelEventLogHandler;
import org.openhab.binding.satel.internal.handler.SatelOutputHandler;
import org.openhab.binding.satel.internal.handler.SatelPartitionHandler;
import org.openhab.binding.satel.internal.handler.SatelShutterHandler;
import org.openhab.binding.satel.internal.handler.SatelSystemHandler;
import org.openhab.binding.satel.internal.handler.SatelZoneHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ThingType;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SatelHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.satel")
@NonNullByDefault
public class SatelHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS, DEVICE_THING_TYPES_UIDS, VIRTUAL_THING_TYPES_UIDS)
            .flatMap(uids -> uids.stream()).collect(Collectors.toSet());

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new ConcurrentHashMap<>();

    private @Nullable SerialPortManager serialPortManager;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        ThingUID effectiveUID = thingUID;
        if (effectiveUID == null) {
            if (DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
                effectiveUID = getDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            }
        }
        return super.createThing(thingTypeUID, configuration, effectiveUID, bridgeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Ethm1BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            SatelBridgeHandler bridgeHandler = new Ethm1BridgeHandler((Bridge) thing);
            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (IntRSBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            final SerialPortManager serialPortManager = this.serialPortManager;
            if (serialPortManager != null) {
                SatelBridgeHandler bridgeHandler = new IntRSBridgeHandler((Bridge) thing, serialPortManager);
                registerDiscoveryService(bridgeHandler);
                return bridgeHandler;
            } else {
                throw new IllegalStateException(
                        "Unable to create INT-RS bridge thing. The serial port manager is missing.");
            }
        } else if (SatelZoneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelZoneHandler(thing);
        } else if (SatelOutputHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelOutputHandler(thing);
        } else if (SatelPartitionHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelPartitionHandler(thing);
        } else if (SatelShutterHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelShutterHandler(thing);
        } else if (SatelSystemHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelSystemHandler(thing);
        } else if (SatelEventLogHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelEventLogHandler(thing);
        } else if (Atd100Handler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new Atd100Handler(thing);
        }

        return null;
    }

    @SuppressWarnings("null")
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        super.removeHandler(thingHandler);

        ServiceRegistration<?> discoveryServiceRegistration = discoveryServiceRegistrations
                .remove(thingHandler.getThing().getUID());
        if (discoveryServiceRegistration != null) {
            discoveryServiceRegistration.unregister();
        }
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    private void registerDiscoveryService(SatelBridgeHandler bridgeHandler) {
        SatelDeviceDiscoveryService discoveryService = new SatelDeviceDiscoveryService(bridgeHandler,
                this::resolveThingType);
        ServiceRegistration<?> discoveryServiceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), discoveryServiceRegistration);
    }

    private ThingUID getDeviceUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration,
            @Nullable ThingUID bridgeUID) {
        String deviceId;
        if (THING_TYPE_SHUTTER.equals(thingTypeUID)) {
            deviceId = String.format("%s-%s", configuration.get(SatelThingConfig.UP_ID),
                    configuration.get(SatelThingConfig.DOWN_ID));
        } else {
            deviceId = String.valueOf(configuration.get(SatelThingConfig.ID));
        }
        return bridgeUID != null ? new ThingUID(thingTypeUID, deviceId, bridgeUID.getId())
                : new ThingUID(thingTypeUID, deviceId);
    }

    private ThingType resolveThingType(ThingTypeUID thingTypeUID) {
        final ThingType result = super.getThingTypeByUID(thingTypeUID);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("Invalid thing type provided: " + thingTypeUID);
    }
}
