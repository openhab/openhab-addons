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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceBinarayInputEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;

/**
 * Represents a device state update for lights, joker, shades and sensor data.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface DeviceStateUpdate {

    // Update types
    // in certain circumstances it is also better to rename SLAT_ANGLE to e.g. SECONDARY_OUTPUT
    // light
    static final String OUTPUT = "output";
    static final String ON_OFF = "OnOff";
    static final String OUTPUT_INCREASE = "outputIncrese";
    static final String OUTPUT_DECREASE = "outputDecrese";
    static final String OUTPUT_STOP = "outputStop";
    static final String OUTPUT_MOVE = "outputMove";

    // shades
    static final String SLATPOSITION = "slatposition";
    static final String SLAT_ANGLE = "slatAngle";
    static final String SLAT_INCREASE = "slatIncrese";
    static final String SLAT_DECREASE = "slatDecrese";
    static final String SLAT_ANGLE_INCREASE = "slatAngleIncrese";
    static final String SLAT_ANGLE_DECREASE = "slatAngleDecrese";
    static final String OPEN_CLOSE = "openClose";
    static final String OPEN_CLOSE_ANGLE = "openCloseAngle";
    static final String SLAT_MOVE = "slatMove";
    static final String SLAT_STOP = "slatStop";

    // sensor data
    static final String UPDATE_OUTPUT_VALUE = "outputValue";
    static final String UPDATE_DEVICE_SENSOR = "deviceSensor-";

    // metering data
    static final String UPDATE_CIRCUIT_METER = "circuitMeter";

    // binary inputs
    static final String BINARY_INPUT = "binaryInput-";

    // scene
    /** A scene call can have the value between 0 and 127. */
    static final String UPDATE_CALL_SCENE = "callScene";
    static final String UPDATE_UNDO_SCENE = "undoScene";
    static final String UPDATE_SCENE_OUTPUT = "sceneOutput";
    static final String UPDATE_SCENE_CONFIG = "sceneConfig";

    // general
    /** command to refresh the output value of an device. */
    static final String REFRESH_OUTPUT = "refreshOutput";

    // standard values
    static final int ON_VALUE = 1;
    static final int OFF_VALUE = -1;

    /**
     * Returns the state update value.
     * <p>
     * <b>NOTE:</b>
     * </p>
     * <ul>
     * <li>For all OnOff-types the value for off is lower than 0 and for on higher than 0.</li>
     * <li>For all Increase- and Decrease-types the value is the new output value.</li>
     * <li>For SceneCall-type the value is between 0 and 127.</li>
     * <li>For all SceneUndo-types the value is the new output value.</li>
     * <li>For all SensorUpdate-types will read the sensor data directly, if the value is 0, otherwise a
     * {@link SensorJob} will be added to the {@link SensorJobExecutor}.</li>
     * </ul>
     *
     * @return new state value
     */
    Object getValue();

    /**
     * Returns the value as {@link Integer}.
     *
     * @return integer value
     * @see #getValue()
     */
    Integer getValueAsInteger();

    /**
     * Returns the value as {@link String}.
     *
     * @return string value
     * @see #getValue()
     */
    String getValueAsString();

    /**
     * Returns the value as {@link Short}.
     *
     * @return short value
     * @see #getValue()
     */
    Short getValueAsShort();

    /**
     * Returns the value as {@link Float}.
     *
     * @return float value
     * @see #getValue()
     */
    Float getValueAsFloat();

    /**
     * Returns the value as {@link Short}-array.
     *
     * @return short[] value
     * @see #getValue()
     */
    Short[] getValueAsShortArray();

    /**
     * Returns the state update type.
     *
     * @return state update type
     */
    String getType();

    /**
     * Returns the update type as {@link SensorEnum} or null, if the type is not a {@link #UPDATE_DEVICE_SENSOR} type.
     *
     * @return type as {@link SensorEnum} or null
     */
    SensorEnum getTypeAsSensorEnum();

    /**
     * Returns true, if this {@link DeviceStateUpdate} is a {@link #UPDATE_DEVICE_SENSOR} type, otherwise false.
     *
     * @return true, if it is a sensor type
     */
    boolean isSensorUpdateType();

    /**
     * Returns the scene id of this {@link DeviceStateUpdate}, if this {@link DeviceStateUpdate} is a scene update type,
     * otherwise it will be returned -1.
     *
     * @return the scene id or -1
     */
    Short getSceneId();

    /**
     * Returns the scene configuration or output reading priority, if this {@link DeviceStateUpdate} is a
     * {@link #UPDATE_SCENE_CONFIG} or {@link #UPDATE_SCENE_OUTPUT} type.
     *
     * @return scene reading priority
     */
    Short getScenePriority();

    /**
     * Returns true, if this {@link DeviceStateUpdate} is a {@link #UPDATE_SCENE_CONFIG} or {@link #UPDATE_SCENE_OUTPUT}
     * type, otherwise false.
     *
     * @return true, if it is a scene reading type
     */
    boolean isSceneUpdateType();

    /**
     * Returns the update type as {@link DeviceBinarayInputEnum} or null, if the type is not a {@link #BINARY_INPUT}
     * type.
     *
     * @return type as {@link DeviceBinarayInputEnum} or null
     */
    DeviceBinarayInputEnum getTypeAsDeviceBinarayInputEnum();

    /**
     * Returns true, if this {@link DeviceStateUpdate} is a {@link #BINARY_INPUT} type, otherwise false.
     *
     * @return true, if it is a binary input type
     */
    boolean isBinarayInputType();
}
