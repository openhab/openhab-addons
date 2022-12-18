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
package org.openhab.binding.knx.internal.client;

import static org.openhab.binding.knx.internal.KNXBindingConstants.*;
import static org.openhab.binding.knx.internal.handler.DeviceConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.Manufacturer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DeviceDescriptor;
import tuwien.auto.calimero.DeviceDescriptor.DD0;
import tuwien.auto.calimero.DeviceDescriptor.DD2;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXIllegalArgumentException;
import tuwien.auto.calimero.mgmt.PropertyAccess.PID;

/**
 * Client dedicated to read device specific information using the {@link DeviceInfoClient}.
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author Holger Friedrich - support additional device properties
 *
 */
@NonNullByDefault
public class DeviceInspector {

    private static final long OPERATION_TIMEOUT = 5000;
    private static final long OPERATION_INTERVAL = 2000;

    private final Logger logger = LoggerFactory.getLogger(DeviceInspector.class);
    private final DeviceInfoClient client;
    private final IndividualAddress address;

    public static class Result {
        private final Map<String, String> properties;
        private final Set<GroupAddress> groupAddresses;

        public Result(Map<String, String> properties, Set<GroupAddress> groupAddresses) {
            super();
            this.properties = properties;
            this.groupAddresses = groupAddresses;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public Set<GroupAddress> getGroupAddresses() {
            return groupAddresses;
        }
    }

    public DeviceInspector(DeviceInfoClient client, IndividualAddress address) {
        this.client = client;
        this.address = address;
    }

    private DeviceInfoClient getClient() {
        return client;
    }

    @Nullable
    public Result readDeviceInfo() {
        if (!getClient().isConnected()) {
            return null;
        }

        logger.debug("Fetching device information for address {}", address);
        Map<String, String> properties = new HashMap<>();
        properties.putAll(readDeviceDescription(address));
        properties.putAll(readDeviceProperties(address));
        return new Result(properties, Collections.emptySet());
    }

    private Map<String, String> readDeviceProperties(IndividualAddress address) {
        Map<String, String> ret = new HashMap<>();
        try {
            Thread.sleep(OPERATION_INTERVAL);
            // check if there is a Device Object in the KNX device
            byte[] elements = getClient().readDeviceProperties(address, DEVICE_OBJECT, PID.OBJECT_TYPE, 0, 1, false,
                    OPERATION_TIMEOUT);
            if ((elements == null ? 0 : toUnsigned(elements)) == 1) {
                Thread.sleep(OPERATION_INTERVAL);
                String manufacturerID = Manufacturer.getName(toUnsigned(getClient().readDeviceProperties(address,
                        DEVICE_OBJECT, PID.MANUFACTURER_ID, 1, 1, false, OPERATION_TIMEOUT)));

                Thread.sleep(OPERATION_INTERVAL);
                String serialNo = toHex(getClient().readDeviceProperties(address, DEVICE_OBJECT, PID.SERIAL_NUMBER, 1,
                        1, false, OPERATION_TIMEOUT), "");

                Thread.sleep(OPERATION_INTERVAL);
                String hardwareType = toHex(getClient().readDeviceProperties(address, DEVICE_OBJECT, HARDWARE_TYPE, 1,
                        1, false, OPERATION_TIMEOUT), " ");

                // PID_FIRMWARE_REVISION, optional, fallback PID_VERSION according to spec
                Thread.sleep(OPERATION_INTERVAL);
                String firmwareRevision = null;
                try {
                    byte[] result = getClient().readDeviceProperties(address, DEVICE_OBJECT, PID.FIRMWARE_REVISION, 1,
                            1, false, OPERATION_TIMEOUT);
                    if (result != null) {
                        firmwareRevision = Integer.toString(toUnsigned(result));
                    } else {
                        // try fallback to PID_VERSION
                        result = getClient().readDeviceProperties(address, DEVICE_OBJECT, PID.VERSION, 1, 1, false,
                                OPERATION_TIMEOUT);
                        if (result != null) {
                            // data format is DPT217.001
                            int i = toUnsigned(result);
                            firmwareRevision = Integer.toString((i & 0xF800) >> 11) + "."
                                    + Integer.toString((i & 0x07C0) >> 6) + "." + Integer.toString((i & 0x003F));
                        }
                    }
                } catch (Exception e) {
                    // allowed to fail, optional
                }

                // MAX_APDU_LENGTH, for *routing*, optional, fallback to MAX_APDU_LENGTH of device
                Thread.sleep(OPERATION_INTERVAL);
                String maxApdu = "";
                try {
                    byte[] result = getClient().readDeviceProperties(address, DEVICE_OBJECT, PID.MAX_APDULENGTH, 1, 1,
                            false, OPERATION_TIMEOUT);
                    if (result != null) {
                        maxApdu = Integer.toString(toUnsigned(result));
                    }
                } catch (Exception e) {
                    // allowed to fail, optional
                }
                if ((maxApdu != null) && (!maxApdu.isEmpty())) {
                    logger.trace("Max APDU for device {} is {} bytes (routing)", address, maxApdu);
                } else {
                    // fallback: MAX_APDU_LENGTH; if availble set the default is 14 according to spec
                    Thread.sleep(OPERATION_INTERVAL);
                    try {
                        byte[] result = getClient().readDeviceProperties(address, ADDRESS_TABLE_OBJECT,
                                MAX_ROUTED_APDU_LENGTH, 1, 1, false, OPERATION_TIMEOUT);
                        if (result != null) {
                            maxApdu = Integer.toString(toUnsigned(result));
                        }
                    } catch (Exception e) {
                        // allowed to fail, optional
                    }
                    if ((maxApdu != null) && (!maxApdu.isEmpty())) {
                        logger.trace("Max APDU for device {} is {} bytes", address, maxApdu);
                    } else {
                        logger.trace("Max APDU for device {} not set, fallback to 14 bytes", address);
                        maxApdu = "14"; // see spec
                    }
                }

                Thread.sleep(OPERATION_INTERVAL);
                byte[] orderInfo = getClient().readDeviceProperties(address, DEVICE_OBJECT, PID.ORDER_INFO, 1, 1, false,
                        OPERATION_TIMEOUT);
                if (orderInfo != null) {
                    final String hexString = toHex(orderInfo, "");
                    if ((!"ffffffffffffffffffff".equals(hexString)) && (!"00000000000000000000".equals(hexString))) {
                        String result = new String(orderInfo);
                        // result = result.trim();
                        result = StringUtils.strip(result, "\0");
                        if ((result == null) || result.isEmpty()) {
                            result = "0x" + hexString;
                        } else if (!StringUtils.isAsciiPrintable(result)) {
                            result = result.replaceAll("[^\\x20-\\x7E]", ".") + " (0x" + toHex(orderInfo, "") + ")";
                        }
                        logger.trace("Order code for device {} is \"{}\"", address, result);
                        ret.put(MANUFACTURER_ORDER_INFO, result);
                    }
                }

                // read FRIENDLY_NAME, optional
                Thread.sleep(OPERATION_INTERVAL);
                try {
                    byte[] count = getClient().readDeviceProperties(address, ROUTER_OBJECT, PID.FRIENDLY_NAME, 0, 1,
                            false, OPERATION_TIMEOUT);
                    if ((count != null) && (toUnsigned(count) == 30)) {
                        StringBuffer buf = new StringBuffer(30);
                        for (int i = 1; i <= 30; i++) {
                            Thread.sleep(OPERATION_INTERVAL);
                            // for some reason, reading more than one character per message fails
                            // reading only one character is inefficient, but works
                            byte[] data = getClient().readDeviceProperties(address, ROUTER_OBJECT, PID.FRIENDLY_NAME, i,
                                    1, false, OPERATION_TIMEOUT);
                            if (toUnsigned(data) != 0) {
                                if (data != null) {
                                    buf.append(new String(data));
                                }
                            } else {
                                break;
                            }
                        }
                        final String result = buf.toString();
                        if ((result != null) && (StringUtils.isAsciiPrintable(result))) {
                            logger.debug("Identified device {} as \"{}\"", address, result);
                            ret.put(FRIENDLY_NAME, result);
                        } else {
                            // this is due to devices which have a buggy implememtation (and show a broken string also
                            // in ETS tool)
                            logger.debug("Ignoring FRIENDLY_NAME as it contains non-printable characters");
                        }
                    }
                } catch (Exception e) {
                    // allowed to fail, optional
                }

                ret.put(MANUFACTURER_NAME, manufacturerID);
                if (serialNo != null) {
                    ret.put(MANUFACTURER_SERIAL_NO, serialNo);
                }
                if (hardwareType != null) {
                    ret.put(MANUFACTURER_HARDWARE_TYPE, hardwareType);
                }
                if (firmwareRevision != null) {
                    ret.put(MANUFACTURER_FIRMWARE_REVISION, firmwareRevision);
                }
                ret.put(MAX_APDU_LENGTH, maxApdu);
                logger.debug("Identified device {} as {}, type {}, revision {}, serial number {}, max APDU {}", address,
                        manufacturerID, hardwareType, firmwareRevision, serialNo, maxApdu);
            } else {
                logger.debug("The KNX device with address {} does not expose a Device Object", address);
            }
        } catch (InterruptedException e) {
            logger.debug("Interrupted while fetching the device description for a device '{}' : {}", address,
                    e.getMessage());
        }
        return ret;
    }

    private @Nullable String toHex(byte @Nullable [] input, String separator) {
        return input == null ? null : DataUnitBuilder.toHex(input, separator);
    }

    private Map<String, String> readDeviceDescription(IndividualAddress address) {
        Map<String, String> ret = new HashMap<>();
        byte[] data = getClient().readDeviceDescription(address, 0, false, OPERATION_TIMEOUT);
        if (data != null) {
            try {
                final DD0 dd = DeviceDescriptor.DD0.from(data);

                ret.put(DEVICE_MASK_VERSION, String.format("%04X", dd.maskVersion()));
                ret.put(DEVICE_PROFILE, dd.deviceProfile());
                ret.put(DEVICE_MEDIUM_TYPE, getMediumType(dd.mediumType()));
                logger.debug("The device with address {} has mask {} ({}, medium {})", address,
                        ret.get(DEVICE_MASK_VERSION), ret.get(DEVICE_PROFILE), ret.get(DEVICE_MEDIUM_TYPE));
            } catch (KNXIllegalArgumentException e) {
                logger.info("Can not parse Device Descriptor 0 for device with address {}: {}", address,
                        e.getMessage());
            }
        } else {
            logger.debug("The device with address {} does not expose a Device Descriptor type 0", address);
        }
        data = getClient().readDeviceDescription(address, 2, false, OPERATION_TIMEOUT);
        if (data != null) {
            try {
                final DD2 dd = DeviceDescriptor.DD2.from(data);
                logger.debug("The device with address {} is has DD2 {}", address, dd.toString());
            } catch (KNXIllegalArgumentException e) {
                logger.info("Can not parse device descriptor 2 for device with address {}: {}", address,
                        e.getMessage());
            }
        }
        return ret;
    }

    private int toUnsigned(final byte @Nullable [] data) {
        if (data == null) {
            return 0;
        }
        int value = data[0] & 0xff;
        if (data.length == 1) {
            return value;
        }
        value = value << 8 | data[1] & 0xff;
        if (data.length == 2) {
            return value;
        }
        value = value << 16 | data[2] & 0xff << 8 | data[3] & 0xff;
        return value;
    }

    private static String getMediumType(int type) {
        switch (type) {
            case 0:
                return "TP";
            case 1:
                return "PL";
            case 2:
                return "RF";
            case 3:
                return "TP0 (deprecated)";
            case 4:
                return "PL123 (deprecated)";
            case 5:
                return "IP";
            default:
                return "unknown (" + type + ")";
        }
    }
}
