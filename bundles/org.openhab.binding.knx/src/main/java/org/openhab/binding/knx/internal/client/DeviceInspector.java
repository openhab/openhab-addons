/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.Firmware;
import org.openhab.binding.knx.internal.handler.Manufacturer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DeviceDescriptor;
import tuwien.auto.calimero.DeviceDescriptor.DD0;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.mgmt.PropertyAccess.PID;

/**
 * Client dedicated to read device specific information using the {@link DeviceInfoClient}.
 *
 * @author Simon Kaufmann - initial contribution and API.
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
                Thread.sleep(OPERATION_INTERVAL);
                String firmwareRevision = Integer.toString(toUnsigned(getClient().readDeviceProperties(address,
                        DEVICE_OBJECT, PID.FIRMWARE_REVISION, 1, 1, false, OPERATION_TIMEOUT)));

                ret.put(MANUFACTURER_NAME, manufacturerID);
                if (serialNo != null) {
                    ret.put(MANUFACTURER_SERIAL_NO, serialNo);
                }
                if (hardwareType != null) {
                    ret.put(MANUFACTURER_HARDWARE_TYPE, hardwareType);
                }
                ret.put(MANUFACTURER_FIRMWARE_REVISION, firmwareRevision);
                logger.debug("Identified device {} as a {}, type {}, revision {}, serial number {}", address,
                        manufacturerID, hardwareType, firmwareRevision, serialNo);
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
            final DD0 dd = DeviceDescriptor.DD0.from(data);

            ret.put(FIRMWARE_TYPE, Firmware.getName(dd.firmwareType()));
            ret.put(FIRMWARE_VERSION, Firmware.getName(dd.firmwareVersion()));
            ret.put(FIRMWARE_SUBVERSION, Firmware.getName(dd.firmwareSubcode()));
            logger.debug("The device with address {} is of type {}, version {}, subversion {}", address,
                    Firmware.getName(dd.firmwareType()), Firmware.getName(dd.firmwareVersion()),
                    Firmware.getName(dd.firmwareSubcode()));
        } else {
            logger.debug("The KNX device with address {} does not expose a Device Descriptor", address);
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
}
