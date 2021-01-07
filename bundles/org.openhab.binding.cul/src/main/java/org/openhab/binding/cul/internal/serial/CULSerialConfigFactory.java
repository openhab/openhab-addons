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
package org.openhab.binding.cul.internal.serial;

import java.util.*;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.internal.CULConfig;
import org.openhab.binding.cul.internal.CULConfigFactory;
import org.openhab.binding.cul.internal.CULMode;
import org.openhab.core.io.transport.serial.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration factory for serial device handler implementation.
 *
 * @author Patrick Ruckstuhl - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.9.0
 */
@NonNullByDefault
public class CULSerialConfigFactory implements CULConfigFactory {

    public static final String DEVICE_TYPE = "serial";
    private final Logger logger = LoggerFactory.getLogger(CULSerialConfigFactory.class);
    private static final Map<String, Integer> VALID_PARITIES_MAP;
    private static final List<Integer> VALID_BAUDRATE_MAP;

    static {
        Map<String, Integer> parities = new HashMap<String, Integer>();
        parities.put("EVEN", SerialPort.PARITY_EVEN);
        parities.put("ODD", SerialPort.PARITY_ODD);
        parities.put("MARK", SerialPort.PARITY_MARK);
        parities.put("NONE", SerialPort.PARITY_NONE);
        parities.put("SPACE", SerialPort.PARITY_SPACE);
        VALID_PARITIES_MAP = Collections.unmodifiableMap(parities);

        Integer baudrates[] = { 75, 110, 300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200 };
        VALID_BAUDRATE_MAP = Collections.unmodifiableList(Arrays.asList(baudrates));
    }

    private static final String KEY_BAUDRATE = "baudrate";
    private static final String KEY_PARITY = "parity";

    public CULConfig create(String deviceType, String deviceAddress, CULMode mode, Integer configuredBaudRate,
            String configuredParity) {
        Hashtable<String, String> config = new Hashtable<>();
        config.put(KEY_BAUDRATE, Integer.toString(configuredBaudRate));
        config.put(KEY_PARITY, configuredParity);
        return create(deviceType, deviceAddress, mode, config);
    }

    public CULConfig create(String deviceType, String deviceAddress, CULMode mode, Dictionary<String, ?> config) {
        int baudRate = 9600;
        final String configuredBaudRate = (String) config.get(KEY_BAUDRATE);
        Integer tmpBaudRate = baudrateFromConfig(configuredBaudRate);
        if (tmpBaudRate != null) {
            baudRate = tmpBaudRate;
            logger.info("Update config, {} = {}", KEY_BAUDRATE, baudRate);
        }

        int parityMode = SerialPort.PARITY_EVEN;
        final String configuredParity = (String) config.get(KEY_PARITY);
        Integer parsedParityNumber = parityFromConfig(configuredParity);
        if (parsedParityNumber != null) {
            parityMode = parsedParityNumber;
            logger.info("Update config, {} = {} ({})", KEY_PARITY, convertParityModeToString(parityMode), parityMode);
        }

        return new CULSerialConfig(deviceType, deviceAddress, mode, baudRate, parityMode);
    }

    private @Nullable Integer parityFromConfig(@Nullable final String configuredParity) {
        if (configuredParity != null && !configuredParity.isBlank()) {
            try {
                if (isValidParity(configuredParity)) {
                    return VALID_PARITIES_MAP.get(configuredParity.toUpperCase());
                } else { // allow literal parity assignment?
                    int parsedParityNumber = Integer.parseInt(configuredParity);
                    if (isValidParity(parsedParityNumber)) {
                        return parsedParityNumber;
                    } else {
                        logger.error("The configured '{}' value is invalid. The value '{}' has to be one of {}.",
                                KEY_PARITY, parsedParityNumber, VALID_PARITIES_MAP.keySet());
                    }
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing config key '{}'. Use one of {}.", KEY_PARITY, VALID_PARITIES_MAP.keySet());
            }
        }
        return null;
    }

    /**
     * calculate baudrate from config String
     *
     * @param configuredBaudRate
     * @return baud Rate or null if failed
     */
    private @Nullable Integer baudrateFromConfig(@Nullable final String configuredBaudRate) {
        if (configuredBaudRate != null && !configuredBaudRate.isBlank()) {
            try {
                int tmpBaudRate = Integer.parseInt(configuredBaudRate);
                if (VALID_BAUDRATE_MAP.contains(tmpBaudRate)) {
                    return tmpBaudRate;
                } else {
                    logger.error(
                            "Error parsing config parameter '{}'. Value = {} is not a valid baudrate. Value must be in [75, 110, 300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200]",
                            KEY_BAUDRATE, tmpBaudRate);
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing config parameter '{}' to integer. Value = {}", KEY_BAUDRATE,
                        configuredBaudRate);
            }

        }
        return null;
    }

    /**
     * Checks if mode is a valid input for 'SerialPort' - class
     *
     * @param mode
     * @return true if valid
     */
    private boolean isValidParity(int mode) {
        return VALID_PARITIES_MAP.containsValue(mode);
    }

    /**
     * Checks if mode is a valid input for 'SerialPort' - class
     *
     * @param mode
     * @return true if valid
     */
    private boolean isValidParity(String mode) {
        return VALID_PARITIES_MAP.containsKey(mode.toUpperCase());
    }

    /**
     * converts modes integer representation into a readable sting
     *
     * @param mode
     * @return text if mode was valid, otherwise "invalid mode"
     */
    private String convertParityModeToString(int mode) {
        if (VALID_PARITIES_MAP.containsValue(mode)) {
            for (Entry<String, Integer> parity : VALID_PARITIES_MAP.entrySet()) {
                if (parity.getValue().equals(mode)) {
                    return parity.getKey();
                }
            }
        }
        return "invalid mode";
    }
}
