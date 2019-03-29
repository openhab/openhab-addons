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
package org.openhab.binding.bluetooth;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothCharacteristic} class defines the Bluetooth characteristic.
 * <p>
 * Characteristics are defined attribute types that contain a single logical value.
 * <p>
 * https://www.bluetooth.com/specifications/gatt/characteristics
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - Cleaned up code
 */
public class BluetoothCharacteristic {
    public static final int FORMAT_UINT8 = 0x11;
    public static final int FORMAT_UINT16 = 0x12;
    public static final int FORMAT_UINT32 = 0x14;
    public static final int FORMAT_SINT8 = 0x21;
    public static final int FORMAT_SINT16 = 0x22;
    public static final int FORMAT_SINT32 = 0x24;
    public static final int FORMAT_SFLOAT = 0x32;
    public static final int FORMAT_FLOAT = 0x34;

    public static final int PROPERTY_BROADCAST = 0x01;
    public static final int PROPERTY_READ = 0x02;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 0x04;
    public static final int PROPERTY_WRITE = 0x08;
    public static final int PROPERTY_NOTIFY = 0x10;
    public static final int PROPERTY_INDICATE = 0x20;
    public static final int PROPERTY_SIGNED_WRITE = 0x40;
    public static final int PROPERTY_EXTENDED_PROPS = 0x80;

    public static final int PERMISSION_READ = 0x01;
    public static final int PERMISSION_READ_ENCRYPTED = 0x02;
    public static final int PERMISSION_READ_ENCRYPTED_MITM = 0x04;
    public static final int PERMISSION_WRITE = 0x10;
    public static final int PERMISSION_WRITE_ENCRYPTED = 0x20;
    public static final int PERMISSION_WRITE_ENCRYPTED_MITM = 0x40;
    public static final int PERMISSION_WRITE_SIGNED = 0x80;
    public static final int PERMISSION_WRITE_SIGNED_MITM = 0x100;

    public static final int WRITE_TYPE_DEFAULT = 0x02;
    public static final int WRITE_TYPE_NO_RESPONSE = 0x01;
    public static final int WRITE_TYPE_SIGNED = 0x04;

    private final Logger logger = LoggerFactory.getLogger(BluetoothCharacteristic.class);

    /**
     * The {@link UUID} for this characteristic
     */
    protected UUID uuid;

    /**
     * The handle for this characteristic
     */
    protected int handle;

    /**
     * A map of {@link BluetoothDescriptor}s applicable to this characteristic
     */
    protected Map<UUID, BluetoothDescriptor> gattDescriptors = new HashMap<UUID, BluetoothDescriptor>();
    protected int instance;
    protected int properties;
    protected int permissions;
    protected int writeType;

    /**
     * The raw data value for this characteristic
     */
    protected int[] value = new int[0];

    /**
     * The {@link BluetoothService} to which this characteristic belongs
     */
    protected BluetoothService service;

    /**
     * Create a new BluetoothCharacteristic.
     *
     * @param uuid the {@link UUID} of the new characteristic
     * @param handle
     */
    public BluetoothCharacteristic(UUID uuid, int handle) {
        this.uuid = uuid;
        this.handle = handle;
    }

    /**
     * Adds a descriptor to this characteristic.
     *
     * @param descriptor {@link BluetoothDescriptor} to be added to this characteristic.
     * @return true, if the descriptor was added to the characteristic
     */
    public boolean addDescriptor(BluetoothDescriptor descriptor) {
        if (gattDescriptors.get(descriptor.getUuid()) != null) {
            return false;
        }

        gattDescriptors.put(descriptor.getUuid(), descriptor);
        return true;
    }

    /**
     * Returns the {@link UUID} of this characteristic
     *
     * @return UUID of this characteristic
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the instance ID for this characteristic.
     *
     * If a remote device offers multiple characteristics with the same UUID, the instance ID is used to distinguish
     * between characteristics.
     *
     * @return Instance ID of this characteristic
     */
    public int getInstanceId() {
        return instance;
    }

    /**
     * Returns the properties of this characteristic.
     *
     * The properties contain a bit mask of property flags indicating the features of this characteristic.
     *
     */
    public int getProperties() {
        return properties;
    }

    /**
     * Returns the permissions for this characteristic.
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * Gets the write type for this characteristic.
     *
     */
    public int getWriteType() {
        return writeType;
    }

    /**
     * Set the write type for this characteristic
     *
     * @param writeType
     */
    public void setWriteType(int writeType) {
        this.writeType = writeType;
    }

    /**
     * Get the service to which this characteristic belongs
     *
     * @return the {@link BluetoothService}
     */
    public BluetoothService getService() {
        return service;
    }

    /**
     * Returns the handle for this characteristic
     *
     * @return the handle for the characteristic
     */
    public int getHandle() {
        return handle;
    }

    /**
     * Get the service to which this characteristic belongs
     *
     * @return the {@link BluetoothService}
     */
    public void setService(BluetoothService service) {
        this.service = service;
    }

    /**
     * Returns a list of descriptors for this characteristic.
     *
     */
    public List<BluetoothDescriptor> getDescriptors() {
        return new ArrayList<BluetoothDescriptor>(gattDescriptors.values());
    }

    /**
     * Returns a descriptor with a given UUID out of the list of
     * descriptors for this characteristic.
     *
     * @return the {@link BluetoothDescriptor}
     */
    public BluetoothDescriptor getDescriptor(UUID uuid) {
        return gattDescriptors.get(uuid);
    }

    /**
     * Get the stored value for this characteristic.
     *
     */
    public int[] getValue() {
        return value;
    }

    /**
     * Get the stored value for this characteristic.
     *
     */
    public byte[] getByteValue() {
        byte[] byteValue = new byte[value.length];
        for (int cnt = 0; cnt < value.length; cnt++) {
            byteValue[cnt] = (byte) (value[cnt] & 0xFF);
        }
        return byteValue;
    }

    /**
     * Return the stored value of this characteristic.
     *
     */
    public Integer getIntValue(int formatType, int offset) {
        if ((offset + getTypeLen(formatType)) > value.length) {
            return null;
        }

        switch (formatType) {
            case FORMAT_UINT8:
                return unsignedByteToInt(value[offset]);

            case FORMAT_UINT16:
                return unsignedBytesToInt(value[offset], value[offset + 1]);

            case FORMAT_UINT32:
                return unsignedBytesToInt(value[offset], value[offset + 1], value[offset + 2], value[offset + 3]);

            case FORMAT_SINT8:
                return unsignedToSigned(unsignedByteToInt(value[offset]), 8);

            case FORMAT_SINT16:
                return unsignedToSigned(unsignedBytesToInt(value[offset], value[offset + 1]), 16);

            case FORMAT_SINT32:
                return unsignedToSigned(
                        unsignedBytesToInt(value[offset], value[offset + 1], value[offset + 2], value[offset + 3]), 32);
            default:
                logger.error("Unknown format type {} - no int value can be provided for it.", formatType);
        }

        return null;
    }

    /**
     * Return the stored value of this characteristic. This doesn't read the remote data.
     *
     */
    public Float getFloatValue(int formatType, int offset) {
        if ((offset + getTypeLen(formatType)) > value.length) {
            return null;
        }

        switch (formatType) {
            case FORMAT_SFLOAT:
                return bytesToFloat(value[offset], value[offset + 1]);
            case FORMAT_FLOAT:
                return bytesToFloat(value[offset], value[offset + 1], value[offset + 2], value[offset + 3]);
            default:
                logger.error("Unknown format type {} - no float value can be provided for it.", formatType);
        }

        return null;
    }

    /**
     * Return the stored value of this characteristic. This doesn't read the remote data.
     *
     */
    public String getStringValue(int offset) {
        if (value == null || offset > value.length) {
            return null;
        }
        byte[] strBytes = new byte[value.length - offset];
        for (int i = 0; i < (value.length - offset); ++i) {
            strBytes[i] = (byte) value[offset + i];
        }
        return new String(strBytes, StandardCharsets.UTF_8);
    }

    /**
     * Updates the locally stored value of this characteristic.
     *
     * @param value the value to set
     * @return true, if it has been set successfully
     */
    public boolean setValue(int[] value) {
        this.value = value;
        return true;
    }

    /**
     * Set the local value of this characteristic.
     *
     * @param value the value to set
     * @param formatType the format of the value (as one of the FORMAT_* constants in this class)
     * @param offset the offset to use when interpreting the value
     * @return true, if it has been set successfully
     */
    public boolean setValue(int value, int formatType, int offset) {
        int len = offset + getTypeLen(formatType);
        if (this.value == null) {
            this.value = new int[len];
        }
        if (len > this.value.length) {
            return false;
        }
        int val = value;
        switch (formatType) {
            case FORMAT_SINT8:
                val = intToSignedBits(value, 8);
                // Fall-through intended
            case FORMAT_UINT8:
                this.value[offset] = (byte) (val & 0xFF);
                break;

            case FORMAT_SINT16:
                val = intToSignedBits(value, 16);
                // Fall-through intended
            case FORMAT_UINT16:
                this.value[offset] = (byte) (val & 0xFF);
                this.value[offset + 1] = (byte) ((val >> 8) & 0xFF);
                break;

            case FORMAT_SINT32:
                val = intToSignedBits(value, 32);
                // Fall-through intended
            case FORMAT_UINT32:
                this.value[offset] = (byte) (val & 0xFF);
                this.value[offset + 1] = (byte) ((val >> 8) & 0xFF);
                this.value[offset + 2] = (byte) ((val >> 16) & 0xFF);
                this.value[offset + 2] = (byte) ((val >> 24) & 0xFF);
                break;

            default:
                return false;
        }
        return true;
    }

    /**
     * Set the local value of this characteristic.
     *
     * @param mantissa the mantissa of the value
     * @param exponent the exponent of the value
     * @param formatType the format of the value (as one of the FORMAT_* constants in this class)
     * @param offset the offset to use when interpreting the value
     * @return true, if it has been set successfully
     *
     */
    public boolean setValue(int mantissa, int exponent, int formatType, int offset) {
        int len = offset + getTypeLen(formatType);
        if (value == null) {
            value = new int[len];
        }
        if (len > value.length) {
            return false;
        }

        switch (formatType) {
            case FORMAT_SFLOAT:
                int m = intToSignedBits(mantissa, 12);
                int exp = intToSignedBits(exponent, 4);
                value[offset] = (byte) (m & 0xFF);
                value[offset + 1] = (byte) ((m >> 8) & 0x0F);
                value[offset + 1] += (byte) ((exp & 0x0F) << 4);
                break;

            case FORMAT_FLOAT:
                m = intToSignedBits(mantissa, 24);
                exp = intToSignedBits(exponent, 8);
                value[offset] = (byte) (m & 0xFF);
                value[offset + 1] = (byte) ((m >> 8) & 0xFF);
                value[offset + 2] = (byte) ((m >> 16) & 0xFF);
                value[offset + 2] += (byte) (exp & 0xFF);
                break;

            default:
                return false;
        }

        return true;
    }

    /**
     * Set the local value of this characteristic.
     *
     * @param value the value to set
     * @return true, if it has been set successfully
     */
    public boolean setValue(byte[] value) {
        this.value = new int[value.length];
        int cnt = 0;
        for (byte val : value) {
            this.value[cnt++] = val;
        }
        return true;
    }

    /**
     * Set the local value of this characteristic.
     *
     * @param value the value to set
     * @return true, if it has been set successfully
     */
    public boolean setValue(String value) {
        this.value = new int[value.getBytes().length];
        int cnt = 0;
        for (byte val : value.getBytes()) {
            this.value[cnt++] = val;
        }
        return true;
    }

    /**
     * Returns the size of the requested value type.
     */
    private int getTypeLen(int formatType) {
        return formatType & 0xF;
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private int unsignedByteToInt(int value) {
        return value & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private int unsignedBytesToInt(int value1, int value2) {
        return value1 + (value2 << 8);
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private int unsignedBytesToInt(int value1, int value2, int value3, int value4) {
        return value1 + (value2 << 8) + (value3 << 16) + (value4 << 24);
    }

    /**
     * Convert signed bytes to a 16-bit short float value.
     */
    private float bytesToFloat(int value1, int value2) {
        int mantissa = unsignedToSigned(unsignedByteToInt(value1) + ((unsignedByteToInt(value2) & 0x0F) << 8), 12);
        int exponent = unsignedToSigned(unsignedByteToInt(value2) >> 4, 4);
        return (float) (mantissa * Math.pow(10, exponent));
    }

    /**
     * Convert signed bytes to a 32-bit short float value.
     */
    private float bytesToFloat(int value1, int value2, int value3, int value4) {
        int mantissa = unsignedToSigned(
                unsignedByteToInt(value1) + (unsignedByteToInt(value2) << 8) + (unsignedByteToInt(value3) << 16), 24);
        return (float) (mantissa * Math.pow(10, value4));
    }

    /**
     * Convert an unsigned integer to a two's-complement signed value.
     */
    private int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            return -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        } else {
            return unsigned;
        }
    }

    /**
     * Convert an integer into the signed bits of the specified length.
     */
    private int intToSignedBits(int i, int size) {
        if (i < 0) {
            return (1 << size - 1) + (i & ((1 << size - 1) - 1));
        } else {
            return i;
        }
    }

    public GattCharacteristic getGattCharacteristic() {
        return GattCharacteristic.getCharacteristic(uuid);
    }

    public enum GattCharacteristic {
        // Characteristic
        ALERT_CATEGORY_ID(0x2A43),
        ALERT_CATEGORY_ID_BIT_MASK(0x2A42),
        ALERT_LEVEL(0x2A06),
        ALERT_NOTIFICATION_CONTROL_POINT(0x2A44),
        ALERT_STATUS(0x2A3F),
        APPEARANCE(0x2A01),
        BATTERY_LEVEL(0x2A19),
        BLOOD_PRESSURE_FEATURE(0x2A49),
        BLOOD_PRESSURE_MEASUREMENT(0x2A35),
        BODY_SENSOR_LOCATION(0x2A38),
        BOOT_KEYOBARD_INPUT_REPORT(0x2A22),
        BOOT_KEYOBARD_OUTPUT_REPORT(0x2A32),
        BOOT_MOUSE_INPUT_REPORT(0x2A33),
        CSC_FEATURE(0x2A5C),
        CSC_MEASUREMENT(0x2A5B),
        CURRENT_TIME(0x2A2B),
        CYCLING_POWER_CONTROL_POINT(0x2A66),
        CYCLING_POWER_FEATURE(0x2A65),
        CYCLING_POWER_MEASUREMENT(0x2A63),
        CYCLING_POWER_VECTOR(0x2A64),
        DATE_TIME(0x2A08),
        DAY_DATE_TIME(0x2A0A),
        DAY_OF_WEEK(0x2A09),
        DEVICE_NAME(0x2A00),
        DST_OFFSET(0x2A0D),
        EXACT_TIME_256(0x2A0C),
        FIRMWARE_REVISION_STRING(0x2A26),
        GLUCOSE_FEATURE(0x2A51),
        GLUCOSE_MEASUREMENT(0x2A18),
        GLUCOSE_MEASUREMENT_CONTROL(0x2A34),
        HARDWARE_REVISION_STRING(0x2A27),
        HEART_RATE_CONTROL_POINT(0x2A39),
        HEART_RATE_MEASUREMENT(0x2A37),
        HID_CONTROL_POINT(0x2A4C),
        HID_INFORMATION(0x2A4A),
        IEEE11073_20601_REGULATORY_CERTIFICATION_DATA_LIST(0x2A2A),
        INTERMEDIATE_CUFF_PRESSURE(0x2A36),
        INTERMEDIATE_TEMPERATURE(0x2A1E),
        LN_CONTROL_POINT(0x2A6B),
        LN_FEATURE(0x2A6A),
        LOCAL_TIME_INFORMATION(0x2A0F),
        LOCATION_AND_SPEED(0x2A67),
        MANUFACTURER_NAME_STRING(0x2A29),
        MEASUREMENT_INTERVAL(0x2A21),
        MODEL_NUMBER_STRING(0x2A24),
        NAVIGATION(0x2A68),
        NEW_ALERT(0x2A46),
        PERIPERAL_PREFFERED_CONNECTION_PARAMETERS(0x2A04),
        PERIPHERAL_PRIVACY_FLAG(0x2A02),
        PN_PID(0x2A50),
        POSITION_QUALITY(0x2A69),
        PROTOCOL_MODE(0x2A4E),
        RECONNECTION_ADDRESS(0x2A03),
        RECORD_ACCESS_CONTROL_POINT(0x2A52),
        REFERENCE_TIME_INFORMATION(0x2A14),
        REPORT(0x2A4D),
        REPORT_MAP(0x2A4B),
        RINGER_CONTROL_POINT(0x2A40),
        RINGER_SETTING(0x2A41),
        RSC_FEATURE(0x2A54),
        RSC_MEASUREMENT(0x2A53),
        SC_CONTROL_POINT(0x2A55),
        SCAN_INTERVAL_WINDOW(0x2A4F),
        SCAN_REFRESH(0x2A31),
        SENSOR_LOCATION(0x2A5D),
        SERIAL_NUMBER_STRING(0x2A25),
        SERVICE_CHANGED(0x2A05),
        SOFTWARE_REVISION_STRING(0x2A28),
        SUPPORTED_NEW_ALERT_CATEGORY(0x2A47),
        SUPPORTED_UNREAD_ALERT_CATEGORY(0x2A48),
        SYSTEM_ID(0x2A23),
        TEMPERATURE_MEASUREMENT(0x2A1C),
        TEMPERATURE_TYPE(0x2A1D),
        TIME_ACCURACY(0x2A12),
        TIME_SOURCE(0x2A13),
        TIME_UPDATE_CONTROL_POINT(0x2A16),
        TIME_UPDATE_STATE(0x2A17),
        TIME_WITH_DST(0x2A11),
        TIME_ZONE(0x2A0E),
        TX_POWER_LEVEL(0x2A07),
        UNREAD_ALERT_STATUS(0x2A45),
        AGGREGATE_INPUT(0x2A5A),
        ANALOG_INPUT(0x2A58),
        ANALOG_OUTPUT(0x2A59),
        DIGITAL_INPUT(0x2A56),
        DIGITAL_OUTPUT(0x2A57),
        EXACT_TIME_100(0x2A0B),
        NETWORK_AVAILABILITY(0x2A3E),
        SCIENTIFIC_TEMPERATURE_IN_CELSIUS(0x2A3C),
        SECONDARY_TIME_ZONE(0x2A10),
        STRING(0x2A3D),
        TEMPERATURE_IN_CELSIUS(0x2A1F),
        TEMPERATURE_IN_FAHRENHEIT(0x2A20),
        TIME_BROADCAST(0x2A15),
        BATTERY_LEVEL_STATE(0x2A1B),
        BATTERY_POWER_STATE(0x2A1A),
        PULSE_OXIMETRY_CONTINUOUS_MEASUREMENT(0x2A5F),
        PULSE_OXIMETRY_CONTROL_POINT(0x2A62),
        PULSE_OXIMETRY_FEATURES(0x2A61),
        PULSE_OXIMETRY_PULSATILE_EVENT(0x2A60),
        PULSE_OXIMETRY_SPOT_CHECK_MEASUREMENT(0x2A5E),
        RECORD_ACCESS_CONTROL_POINT_TESTVERSION(0x2A52),
        REMOVABLE(0x2A3A),
        SERVICE_REQUIRED(0x2A3B);

        private static Map<UUID, GattCharacteristic> uuidToServiceMapping;

        private UUID uuid;

        private GattCharacteristic(long key) {
            this.uuid = new UUID((key << 32) | 0x1000, BluetoothBindingConstants.BLUETOOTH_BASE_UUID);
        }

        private static void initMapping() {
            uuidToServiceMapping = new HashMap<UUID, GattCharacteristic>();
            for (GattCharacteristic s : values()) {
                uuidToServiceMapping.put(s.uuid, s);
            }
        }

        public static GattCharacteristic getCharacteristic(UUID uuid) {
            if (uuidToServiceMapping == null) {
                initMapping();
            }
            return uuidToServiceMapping.get(uuid);
        }

        /**
         * @return the key
         */
        public UUID getUUID() {
            return uuid;
        }
    }

}
