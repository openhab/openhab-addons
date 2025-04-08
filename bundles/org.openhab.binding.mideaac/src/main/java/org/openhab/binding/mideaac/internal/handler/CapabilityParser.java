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
package org.openhab.binding.mideaac.internal.handler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CapabilityParser} parses the capability Response.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CapabilityParser {
    private final Logger logger = LoggerFactory.getLogger(CapabilityParser.class);
    private final Map<CapabilityId, Map<String, Boolean>> capabilities = new HashMap<>();
    private final Map<CapabilityId, Map<String, Double>> numericCapabilities = new HashMap<>();
    private final int trailingBytes = 2; // We expect exactly 2 trailing bytes
    private boolean additionalCapabilities = false;

    public void parse(byte[] payload) {
        // Check if the payload is empty or too short to process
        if (payload.length < 2 + trailingBytes) {
            return; // Exit the method without processing
        }

        // The first byte indicates the number of capabilities
        int count = payload[1] & 0xFF; // Unsigned byte
        int offset = 2; // Start after the count

        while (offset < payload.length - trailingBytes && count-- > 0) {
            if (offset + 3 > payload.length - trailingBytes) {
                break; // Exit if there's insufficient data
            }

            // Size of the capability value
            int size = payload[offset + 2] & 0xFF;
            if (size == 0) {
                offset += 3; // Skip empty capabilities
                continue;
            }

            // Read the reversed 16-bit capability ID (little-endian)
            int rawId = ByteBuffer.wrap(payload, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;

            // Map the ID to a CapabilityId enum
            CapabilityId capabilityId;
            try {
                capabilityId = CapabilityId.fromId(rawId);
            } catch (IllegalArgumentException e) {
                logger.debug("Unknown capability ID: {}, Size: {}", rawId, size);
                offset += 3 + size; // Skip unknown capability
                continue;
            }

            // Fetch the first value after the key and size
            int value = payload[offset + 3] & 0xFF;

            // Apply predefined capability readers if available
            if (CapabilityReaders.hasReader(capabilityId)) {
                CapabilityReaders.apply(capabilityId, value, capabilities);
            } else if (capabilityId == CapabilityId.TEMPERATURES) {
                if (size >= 6) {
                    numericCapabilities.put(capabilityId,
                            Map.of("cool_min_temperature", payload[offset + 3] * 0.5, "cool_max_temperature",
                                    payload[offset + 4] * 0.5, "auto_min_temperature", payload[offset + 5] * 0.5,
                                    "auto_max_temperature", payload[offset + 6] * 0.5, "heat_min_temperature",
                                    payload[offset + 7] * 0.5, "heat_max_temperature", payload[offset + 8] * 0.5));
                }
            } else if (capabilityId == CapabilityId._UNKNOWN) {
                logger.debug("Ignored unknown capability ID: {}, Size: {}", rawId, size);
            } else {
                logger.debug("Unsupported capability {}, Size: {}", capabilityId, size);
            }

            offset += 3 + size; // Advance to the next capability
        }

        // Check if additional capability flag exists without interference from CRC
        additionalCapabilities = offset < payload.length - trailingBytes;
        logger.debug("Additional capabilities {}", additionalCapabilities);
    }

    public Map<CapabilityId, Map<String, Boolean>> getCapabilities() {
        return capabilities;
    }

    public Map<CapabilityId, Map<String, Double>> getNumericCapabilities() {
        return numericCapabilities;
    }

    public boolean hasAdditionalCapabilities() {
        return additionalCapabilities;
    }

    /**
     * From original source, kept notes
     */
    public enum CapabilityId {
        SWING_UD_ANGLE(0x0009),
        SWING_LR_ANGLE(0x000A),
        BREEZELESS(0x0018), // AKA "No Wind Sense"
        SMART_EYE(0x0030),
        WIND_ON_ME(0x0032),
        WIND_OFF_ME(0x0033),
        SELF_CLEAN(0x0039), // AKA Active Clean
        _UNKNOWN(0x0040), // Unknown ID from various logs
        BREEZE_AWAY(0x0042), // AKA "Prevent Straight Wind"
        BREEZE_CONTROL(0x0043), // AKA "FA No Wind Sense"
        RATE_SELECT(0x0048),
        FRESH_AIR(0x004B),
        PARENT_CONTROL(0x0051), // ??
        PREVENT_STRAIGHT_WIND_SELECT(0x0058), // ??
        WIND_AROUND(0x0059), // ??
        JET_COOL(0x0067), // ??
        PRESET_IECO(0x00E3),
        ICHECK(0x0091), // ??
        EMERGENT_HEAT_WIND(0x0093), // ??
        HEAT_PTC_WIND(0x0094), // ??
        CVP(0x0098), // ??
        FAN_SPEED_CONTROL(0x0210),
        PRESET_ECO(0x0212),
        PRESET_FREEZE_PROTECTION(0x0213),
        MODES(0x0214),
        SWING_MODES(0x0215),
        ENERGY(0x0216), // AKA electricity
        FILTER_REMIND(0x0217),
        AUX_ELECTRIC_HEAT(0x0219), // AKA PTC
        PRESET_TURBO(0x021A),
        FILTER_CHECK(0x0221),
        ANION(0x021E),
        HUMIDITY(0x021F),
        FAHRENHEIT(0x0222),
        DISPLAY_CONTROL(0x0224),
        TEMPERATURES(0x0225),
        BUZZER(0x022C), // Reference refers to this as "sound". Is this different then beep?
        MAIN_HORIZONTAL_GUIDE_STRIP(0x0230), // ??
        SUP_HORIZONTAL_GUIDE_STRIP(0x0231), // ??
        TWINS_MACHINE(0x0232), // ??
        GUIDE_STRIP_TYPE(0x0233), // ??
        BODY_CHECK(0x0234); // ??

        private final int id;

        CapabilityId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static CapabilityId fromId(int id) {
            for (CapabilityId capability : values()) {
                if (capability.id == id) {
                    return capability;
                }
            }
            throw new IllegalArgumentException("Unknown Capability ID: " + id);
        }
    }
}
