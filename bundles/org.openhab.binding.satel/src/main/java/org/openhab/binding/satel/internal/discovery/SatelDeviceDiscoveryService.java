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
package org.openhab.binding.satel.internal.discovery;

import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand.DeviceType;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.config.SatelThingConfig;
import org.openhab.binding.satel.internal.handler.SatelBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ThingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelDeviceDiscoveryService} searches for available Satel
 * devices and modules connected to the API console
 *
 * @author Krzysztof Goworek - Initial contribution
 *
 */
@NonNullByDefault
public class SatelDeviceDiscoveryService extends AbstractDiscoveryService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(DEVICE_THING_TYPES_UIDS, VIRTUAL_THING_TYPES_UIDS).flatMap(Collection::stream)
            .collect(Collectors.toSet());
    private static final int OUTPUT_FUNCTION_SHUTTER = 105;

    private final Logger logger = LoggerFactory.getLogger(SatelDeviceDiscoveryService.class);

    private final SatelBridgeHandler bridgeHandler;
    private final Function<ThingTypeUID, ThingType> thingTypeProvider;
    private volatile boolean scanStopped;

    public SatelDeviceDiscoveryService(SatelBridgeHandler bridgeHandler,
            Function<ThingTypeUID, ThingType> thingTypeProvider) {
        super(SUPPORTED_THING_TYPES, 60, false);
        this.bridgeHandler = bridgeHandler;
        this.thingTypeProvider = thingTypeProvider;
    }

    @Override
    protected void startScan() {
        scanStopped = false;
        if (bridgeHandler.isInitialized()) {
            // add virtual things by default
            for (ThingTypeUID thingTypeUID : VIRTUAL_THING_TYPES_UIDS) {
                ThingType thingType = thingTypeProvider.apply(thingTypeUID);
                addThing(thingTypeUID, toCamelCase(thingTypeUID.getId()), thingType.getLabel(), Collections.emptyMap());
            }
        }
        if (!scanStopped) {
            scanForDevices(DeviceType.PARTITION, bridgeHandler.getIntegraType().getPartitions());
        }
        if (!scanStopped) {
            scanForDevices(DeviceType.ZONE, bridgeHandler.getIntegraType().getZones());
        }
        if (!scanStopped) {
            scanForDevices(DeviceType.OUTPUT, bridgeHandler.getIntegraType().getZones());
        }
    }

    @Override
    protected synchronized void stopScan() {
        scanStopped = true;
        super.stopScan();
    }

    private void scanForDevices(DeviceType deviceType, int maxNumber) {
        logger.debug("Scanning for {} started", deviceType.name());
        final Charset encoding = bridgeHandler.getEncoding();
        for (int i = 1; i <= maxNumber && !scanStopped; ++i) {
            ReadDeviceInfoCommand cmd = new ReadDeviceInfoCommand(deviceType, i);
            cmd.ignoreResponseError();
            if (bridgeHandler.sendCommand(cmd, false)) {
                String name = cmd.getName(encoding);
                int deviceKind = cmd.getDeviceKind();
                logger.debug("Found device: type={}, id={}, name={}, kind/function={}, info={}", deviceType.name(), i,
                        name, deviceKind, cmd.getAdditionalInfo());
                if (isDeviceAvailable(deviceType, deviceKind)) {
                    addDevice(deviceType, deviceKind, i, name);
                }
            } else if (cmd.getState() != SatelCommand.State.FAILED) {
                // serious failure, disconnection or so
                scanStopped = true;
                logger.error("Unexpected failure during scan for {} using {}", deviceType.name(),
                        bridgeHandler.getThing().getUID());
            }
        }
        if (scanStopped) {
            logger.debug("Scanning stopped");
        } else {
            logger.debug("Scanning for {} finished", deviceType.name());
        }
    }

    private void addDevice(DeviceType deviceType, int deviceKind, int deviceId, String deviceName) {
        ThingTypeUID thingTypeUID = getThingTypeUID(deviceType, deviceKind);

        if (thingTypeUID == null) {
            logger.warn("Unknown device found: type={}, kind={}, name={}", deviceType.name(), deviceKind, deviceName);
        } else if (!getSupportedThingTypes().contains(thingTypeUID)) {
            logger.warn("Unsupported device: {}", thingTypeUID);
        } else {
            Map<String, Object> properties = new HashMap<>();

            if (THING_TYPE_SHUTTER.equals(thingTypeUID)) {
                properties.put(SatelThingConfig.UP_ID, deviceId);
                properties.put(SatelThingConfig.DOWN_ID, deviceId + 1);
            } else {
                properties.put(SatelThingConfig.ID, deviceId);
            }

            addThing(thingTypeUID, String.valueOf(deviceId), deviceName, properties);
        }
    }

    private void addThing(ThingTypeUID thingTypeUID, String deviceId, String label, Map<String, Object> properties) {
        final ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        final ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceId);
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withBridge(bridgeUID).withLabel(label).withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

    private static @Nullable ThingTypeUID getThingTypeUID(DeviceType deviceType, int deviceKind) {
        return switch (deviceType) {
            case OUTPUT -> (deviceKind == OUTPUT_FUNCTION_SHUTTER) ? THING_TYPE_SHUTTER : THING_TYPE_OUTPUT;
            case PARTITION -> THING_TYPE_PARTITION;
            case ZONE -> THING_TYPE_ZONE;
            default -> null;
        };
    }

    private static boolean isDeviceAvailable(DeviceType deviceType, int deviceKind) {
        return switch (deviceType) {
            case OUTPUT -> deviceKind != 0 && deviceKind != (OUTPUT_FUNCTION_SHUTTER + 1);
            case PARTITION, ZONE -> true;
            default -> false;
        };
    }

    private static String toCamelCase(String s) {
        StringBuilder result = new StringBuilder();
        boolean makeUpper = true;
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '-') {
                makeUpper = true;
            } else {
                result.append(makeUpper ? Character.toUpperCase(c) : c);
                makeUpper = false;
            }
        }
        return result.toString();
    }
}
