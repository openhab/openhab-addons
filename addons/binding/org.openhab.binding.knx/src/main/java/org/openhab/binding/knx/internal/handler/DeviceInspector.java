package org.openhab.binding.knx.internal.handler;

import static org.openhab.binding.knx.KNXBindingConstants.*;
import static org.openhab.binding.knx.internal.handler.DeviceConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.DeviceInfoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DeviceDescriptor;
import tuwien.auto.calimero.DeviceDescriptor.DD0;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.mgmt.PropertyAccess.PID;

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
        try {
            if (getClient().isConnected()) {
                logger.debug("Fetching device information for address {}", address);

                Map<String, String> properties = new HashMap<>();
                properties.putAll(readDeviceDescription(address));
                properties.putAll(readDeviceProperties(address));
                Set<GroupAddress> groupAddresses = readDeviceMemory(address);

                return new Result(properties, groupAddresses);
            }
        } catch (Exception e) {
            logger.error("An exception occurred while fetching the device description for a device '{}' : {}", address,
                    e.getMessage(), e);
        }
        return null;
    }

    private Set<GroupAddress> readDeviceMemory(IndividualAddress address) throws InterruptedException {
        Thread.sleep(OPERATION_INTERVAL);
        byte[] tableaddress = getClient().readDeviceProperties(address, ADDRESS_TABLE_OBJECT, PID.TABLE_REFERENCE, 1, 1,
                false, OPERATION_TIMEOUT);
        // According to the KNX specs, devices should expose the PID.IO_LIST property in the DEVICE
        // object, but it seems that a lot, if not all, devices do not do this. In this list we can find out
        // what other kind of objects the device is exposing. Most devices do implement some set of objects,
        // we will just go ahead and try to read them out irrespective of what is in the IO_LIST

        Set<GroupAddress> ret = new HashSet<>();
        if (tableaddress != null) {
            Thread.sleep(OPERATION_INTERVAL);
            byte[] elements = getClient().readDeviceMemory(address, toUnsigned(tableaddress), 1, false,
                    OPERATION_TIMEOUT);
            if (elements != null) {
                int numberOfElements = toUnsigned(elements);
                logger.debug("The KNX Actor with address {} uses {} group addresses", address, numberOfElements - 1);

                byte[] addressData = null;
                while (addressData == null) {
                    Thread.sleep(OPERATION_INTERVAL);
                    addressData = getClient().readDeviceMemory(address, toUnsigned(tableaddress) + 1, 2, false,
                            OPERATION_TIMEOUT);
                    if (addressData != null) {
                        IndividualAddress individualAddress = new IndividualAddress(addressData);
                        logger.debug("The KNX Actor with address {} its real reported individual address is  {}",
                                address, individualAddress);
                    }
                }

                for (int i = 1; i < numberOfElements; i++) {
                    addressData = null;
                    while (addressData == null) {
                        Thread.sleep(OPERATION_INTERVAL);
                        addressData = getClient().readDeviceMemory(address, toUnsigned(tableaddress) + 1 + i * 2, 2,
                                false, OPERATION_TIMEOUT);
                        if (addressData != null) {
                            GroupAddress groupAddress = new GroupAddress(addressData);
                            ret.add(groupAddress);
                        }
                    }
                }

                for (GroupAddress anAddress : ret) {
                    logger.debug("The KNX Actor with address {} uses Group Address {}", address, anAddress);
                }
            }
        } else {
            logger.warn("The KNX Actor with address {} does not expose a Group Address table", address);
        }
        return ret;
    }

    private Map<String, String> readDeviceProperties(IndividualAddress address) throws InterruptedException {
        Map<String, String> ret = new HashMap<>();
        Thread.sleep(OPERATION_INTERVAL);
        // check if there is a Device Object in the KNX Actor
        byte[] elements = getClient().readDeviceProperties(address, DEVICE_OBJECT, PID.OBJECT_TYPE, 0, 1, false,
                OPERATION_TIMEOUT);
        if ((elements == null ? 0 : toUnsigned(elements)) == 1) {

            Thread.sleep(OPERATION_INTERVAL);
            String ManufacturerID = Manufacturer.getName(toUnsigned(getClient().readDeviceProperties(address,
                    DEVICE_OBJECT, PID.MANUFACTURER_ID, 1, 1, false, OPERATION_TIMEOUT)));
            Thread.sleep(OPERATION_INTERVAL);
            String serialNo = DataUnitBuilder.toHex(getClient().readDeviceProperties(address, DEVICE_OBJECT,
                    PID.SERIAL_NUMBER, 1, 1, false, OPERATION_TIMEOUT), "");
            Thread.sleep(OPERATION_INTERVAL);
            String hardwareType = DataUnitBuilder.toHex(getClient().readDeviceProperties(address, DEVICE_OBJECT,
                    HARDWARE_TYPE, 1, 1, false, OPERATION_TIMEOUT), " ");
            Thread.sleep(OPERATION_INTERVAL);
            String firmwareRevision = Integer.toString(toUnsigned(getClient().readDeviceProperties(address,
                    DEVICE_OBJECT, PID.FIRMWARE_REVISION, 1, 1, false, OPERATION_TIMEOUT)));

            ret.put(MANUFACTURER_NAME, ManufacturerID);
            ret.put(MANUFACTURER_SERIAL_NO, serialNo);
            ret.put(MANUFACTURER_HARDWARE_TYPE, hardwareType);
            ret.put(MANUFACTURER_FIRMWARE_REVISION, firmwareRevision);
            logger.info("Identified device {} as a {}, type {}, revision {}, serial number {}", address, ManufacturerID,
                    hardwareType, firmwareRevision, serialNo);
        } else {
            logger.warn("The KNX Actor with address {} does not expose a Device Object", address);
        }
        return ret;
    }

    private Map<String, String> readDeviceDescription(IndividualAddress address) {
        Map<String, String> ret = new HashMap<>();
        byte[] data = getClient().readDeviceDescription(address, 0, false, OPERATION_TIMEOUT);
        if (data != null) {
            final DD0 dd = DeviceDescriptor.DD0.fromType0(data);

            ret.put(FIRMWARE_TYPE, Firmware.getName(dd.getFirmwareType()));
            ret.put(FIRMWARE_VERSION, Firmware.getName(dd.getFirmwareVersion()));
            ret.put(FIRMWARE_SUBVERSION, Firmware.getName(dd.getSubcode()));
            logger.info("The device with address {} is of type {}, version {}, subversion {}", address,
                    Firmware.getName(dd.getFirmwareType()), Firmware.getName(dd.getFirmwareVersion()),
                    Firmware.getName(dd.getSubcode()));
        } else {
            logger.warn("The KNX Actor with address {} does not expose a Device Descriptor", address);
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
