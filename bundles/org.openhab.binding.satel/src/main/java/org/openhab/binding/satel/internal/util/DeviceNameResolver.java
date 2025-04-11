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
package org.openhab.binding.satel.internal.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand.DeviceType;
import org.openhab.binding.satel.internal.handler.SatelBridgeHandler;
import org.openhab.binding.satel.internal.handler.SatelEventLogHandler;
import org.openhab.binding.satel.internal.types.IntegraType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for getting device names from the alarm system.
 * Used for friendly descriptions in event log. Names are cached to speed up repeating requests.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @see SatelEventLogHandler
 */
@NonNullByDefault
public class DeviceNameResolver {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, @Nullable String> nameCache = new ConcurrentHashMap<>();
    private final SatelBridgeHandler bridgeHandler;

    public DeviceNameResolver(SatelBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Clears all cached names.
     */
    public void clearCache() {
        nameCache.clear();
    }

    /**
     * Returns name of the device with given type and number.
     *
     * @param deviceType device type
     * @param deviceNumber device number
     * @return device name
     */
    public String resolve(DeviceType deviceType, int deviceNumber) {
        return String.format("%s: %s", deviceType.name().toLowerCase(), readDeviceName(deviceType, deviceNumber));
    }

    /**
     * Returns name of output or expander with given number.
     *
     * @param deviceNumber device number
     * @param upperOutput if {@code true} it is upper half of outputs
     * @return name of output or expander, depending on device number
     */
    public String resolveOutputExpander(int deviceNumber, boolean upperOutput) {
        if (deviceNumber == 0) {
            return "mainboard";
        } else if (deviceNumber <= 128) {
            return resolve(DeviceType.OUTPUT, upperOutput ? 128 + deviceNumber : deviceNumber);
        } else if (deviceNumber <= 192) {
            return resolve(DeviceType.EXPANDER, deviceNumber);
        } else {
            return "invalid output|expander device: " + deviceNumber;
        }
    }

    /**
     * Returns name of zone or expander or keypad with given number.
     *
     * @param deviceNumber device number
     * @param upperZone if {@code true} it is upper half of zones
     * @return name of zone or expander or keypad, depending on device number
     */
    public String resolveZoneExpanderKeypad(int deviceNumber, boolean upperZone) {
        if (deviceNumber == 0) {
            return "mainboard";
        } else if (deviceNumber <= 128) {
            return resolve(DeviceType.ZONE, upperZone ? 128 + deviceNumber : deviceNumber);
        } else if (deviceNumber <= 192) {
            return resolve(DeviceType.EXPANDER, deviceNumber);
        } else {
            return resolve(DeviceType.KEYPAD, deviceNumber);
        }
    }

    /**
     * Returns name of partition keypad with given number.
     *
     * @param deviceNumber device number
     * @return name of partition keypad
     */
    public String resolvePartitionKeypad(int deviceNumber) {
        boolean wrlBoard = bridgeHandler.getIntegraType() == IntegraType.I128_LEON
                || bridgeHandler.getIntegraType() == IntegraType.I128_SIM300;
        // Integra 128-WRL has only one expander bus exposed on the mainboard, it can only have 32 expanders
        // On the second bus it has connected embedded ABAX and GSM modules
        if (wrlBoard && deviceNumber > 32) {
            return "mainboard";
        } else {
            return resolve(DeviceType.EXPANDER, deviceNumber);
        }
    }

    /**
     * Returns name of user with given number.
     *
     * @param deviceNumber device number
     * @return name of user
     */
    public String resolveUser(int deviceNumber) {
        return switch (deviceNumber) {
            case 0 -> "user: unknown";
            case 249 -> "INT-AV";
            case 250 -> "ACCO NET";
            case 251 -> "SMS";
            case 252 -> "timer";
            case 253 -> "function zone";
            case 254 -> "Quick arm";
            case 255 -> "service";
            default -> resolve(DeviceType.USER, deviceNumber);
        };
    }

    /**
     * Returns name of data bus given number.
     *
     * @param deviceNumber device number
     * @return name of data bus
     */
    public String resolveDataBus(int deviceNumber) {
        return "data bus: " + deviceNumber;
    }

    /**
     * Returns name of telephone with given number.
     *
     * @param deviceNumber device number
     * @return name of telephone
     */
    public String resolveTelephone(int deviceNumber) {
        return deviceNumber == 0 ? "telephone: unknown" : resolve(DeviceType.TELEPHONE, deviceNumber);
    }

    /**
     * Returns name of telephone relay with given number.
     *
     * @param deviceNumber device number
     * @return name of telephone relay
     */
    public String resolveTelephoneRelay(int deviceNumber) {
        return "telephone relay: " + deviceNumber;
    }

    private String readDeviceName(DeviceType deviceType, int deviceNumber) {
        String cacheKey = String.format("%s_%d", deviceType, deviceNumber);
        String result = nameCache.computeIfAbsent(cacheKey, k -> {
            ReadDeviceInfoCommand cmd = new ReadDeviceInfoCommand(deviceType, deviceNumber);
            if (!bridgeHandler.sendCommand(cmd, false)) {
                logger.warn("Unable to read device info: {}, {}", deviceType, deviceNumber);
                return null;
            }
            return cmd.getName(bridgeHandler.getEncoding());
        });
        return result == null ? Integer.toString(deviceNumber) : result;
    }
}
