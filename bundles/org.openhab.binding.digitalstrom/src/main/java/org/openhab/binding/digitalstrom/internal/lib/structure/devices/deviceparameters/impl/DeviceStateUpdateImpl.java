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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceBinarayInputEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;

/**
 * The {@link DeviceStateUpdateImpl} is the implementation of the {@link DeviceStateUpdate}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DeviceStateUpdateImpl implements DeviceStateUpdate {

    private final String updateType;
    private final Object value;

    /**
     * Creates a new {@link DeviceStateUpdateImpl} with the given updateType and a value as {@link Object}.
     *
     * @param updateType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(String updateType, Object value) {
        this.updateType = updateType;
        this.value = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} through the given {@link DeviceBinaryInput} and value as
     * {@link Short}. The updateType as {@link String} will be automatically create.
     *
     * @param updateDeviceBinary must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(DeviceBinarayInputEnum updateDeviceBinary, Object value) {
        this.updateType = DeviceStateUpdate.BINARY_INPUT + updateDeviceBinary.getBinaryInputType();
        this.value = value;
    }

    /**
     * Creates a new {@link DeviceStateUpdateImpl} through the given {@link SensorEnum} and value as
     * {@link Integer}. The updateType as {@link String} will be automatically create.
     *
     * @param updateSensorType must not be null
     * @param value must not be null
     */
    public DeviceStateUpdateImpl(SensorEnum updateSensorType, Object value) {
        this.updateType = DeviceStateUpdate.UPDATE_DEVICE_SENSOR + updateSensorType.getSensorType();
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getType() {
        return updateType;
    }

    @Override
    public Integer getValueAsInteger() {
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            }
            if (value instanceof Float) {
                return ((Float) value).intValue();
            }
            if (value instanceof Short) {
                return ((Short) value).intValue();
            }
            if (value instanceof String string) {
                return Integer.parseInt(string);
            }
        } catch (Exception e) {
            throw new ClassCastException();
        }
        throw new ClassCastException();
    }

    @Override
    public String getValueAsString() {
        if (value instanceof Integer) {
            return ((Integer) value).toString();
        }
        if (value instanceof Float) {
            return ((Float) value).toString();
        }
        if (value instanceof Short) {
            return ((Short) value).toString();
        }
        if (value instanceof String string) {
            return string;
        }
        throw new ClassCastException();
    }

    @Override
    public Short[] getValueAsShortArray() {
        return (Short[]) value;
    }

    @Override
    public Short getValueAsShort() {
        try {
            if (value instanceof Integer) {
                return ((Integer) value).shortValue();
            }
            if (value instanceof Float) {
                return ((Float) value).shortValue();
            }
            if (value instanceof Short) {
                return (Short) value;
            }
            if (value instanceof String string) {
                return Short.parseShort(string);
            }
        } catch (Exception e) {
            throw new ClassCastException();
        }
        throw new ClassCastException();
    }

    @Override
    public Float getValueAsFloat() {
        try {
            if (value instanceof Integer) {
                return ((Integer) value).floatValue();
            }
            if (value instanceof Float) {
                return (Float) value;
            }
            if (value instanceof Short) {
                return ((Short) value).floatValue();
            }
            if (value instanceof String string) {
                return Float.parseFloat(string);
            }
        } catch (Exception e) {
            throw new ClassCastException();
        }
        throw new ClassCastException();
    }

    @Override
    public SensorEnum getTypeAsSensorEnum() {
        return SensorEnum.getSensor(Short.parseShort(updateType.split("-")[1]));
    }

    @Override
    public boolean isSensorUpdateType() {
        return updateType.startsWith(UPDATE_DEVICE_SENSOR);
    }

    @Override
    public DeviceBinarayInputEnum getTypeAsDeviceBinarayInputEnum() {
        return DeviceBinarayInputEnum.getdeviceBinarayInput(Short.parseShort(updateType.split("-")[1]));
    }

    @Override
    public boolean isBinarayInputType() {
        return updateType.startsWith(BINARY_INPUT);
    }

    @Override
    public Short getSceneId() {
        if (isSceneUpdateType()) {
            return ((Short[]) value)[0];
        }
        return -1;
    }

    @Override
    public Short getScenePriority() {
        if (isSceneUpdateType()) {
            return ((Short[]) value)[1];
        }
        return -1;
    }

    @Override
    public boolean isSceneUpdateType() {
        return updateType.equals(UPDATE_SCENE_CONFIG) || updateType.equals(UPDATE_SCENE_OUTPUT);
    }
}
