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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices;

import java.util.List;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.event.constants.EventNames;
import org.openhab.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.openhab.binding.digitalstrom.internal.lib.sensorjobexecutor.sensorjob.impl.DeviceConsumptionSensorJob;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceSceneSpec;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ApplicationGroup;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceBinarayInputEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputModeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DeviceBinaryInput;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DeviceSensorValue;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.InternalScene;

/**
 * The {@link Device} represents a digitalSTROM internal stored device.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - add methods for ESH, new functionalities and JavaDoc
 * @author Mathias Siegele - add methods for ESH, new functionalities and JavaDoc
 */
public interface Device extends GeneralDeviceInformation {

    /**
     * Returns the id of the dS-Meter in which the device is registered.
     *
     * @return meterDSID
     */
    DSID getMeterDSID();

    /**
     * Sets the id of the dS-Meter in which the device is registered.
     *
     * @param meterDSID to set
     */

    void setMeterDSID(String meterDSID);

    /**
     * Returns the hardware info of this device.
     * You can see all available hardware info at
     * http://www.digitalstrom.com/Partner/Support/Techn-Dokumentation/
     *
     * @return hardware info
     */
    String getHWinfo();

    /**
     * Returns the zone id in which this device is in.
     *
     * @return zoneID
     */
    int getZoneId();

    /**
     * Sets the zoneID of this device.
     *
     * @param zoneID to set
     */
    void setZoneId(int zoneID);

    /**
     * Returns true, if this device is on, otherwise false.
     *
     * @return is on (true = on | false = off)
     */
    boolean isOn();

    /**
     * Adds an on command as {@link DeviceStateUpdate}, if the flag is true or off command, if it is false to the list
     * of
     * outstanding commands.
     *
     * @param flag (true = on | false = off)
     */
    void setIsOn(boolean flag);

    /**
     * Returns true, if this shade device is open, otherwise false.
     *
     * @return is on (true = open | false = closed)
     */
    boolean isOpen();

    /**
     * Adds an open command as {@link DeviceStateUpdate}, if the flag is true or closed command, if it is false to the
     * list of outstanding commands.
     *
     * @param flag (true = open | false = closed)
     */
    void setIsOpen(boolean flag);

    /**
     * Returns true, if this device is dimmable, otherwise false.
     *
     * @return is dimmable (true = yes | false = no)
     */
    boolean isDimmable();

    /**
     * Returns true, if this device is a shade device (grey), otherwise false.
     *
     * @return is shade (true = yes | false = no)
     */
    boolean isShade();

    /**
     * Returns true, if the device output mode isn't disabled.
     *
     * @return have output mode (true = yes | false = no)
     */
    boolean isDeviceWithOutput();

    /**
     * Returns the current functional color group of this device.
     * For more informations please have a look at {@link FunctionalColorGroupEnum}.
     *
     * @return current functional color group
     */
    ApplicationGroup getFunctionalColorGroup();

    /**
     * Sets the functional color group of this device.
     *
     * @param fuctionalColorGroup to set
     */
    void setFunctionalColorGroup(ApplicationGroup fuctionalColorGroup);

    /**
     * Returns the current output mode of this device.
     * Some devices are able to have different output modes e.g. the device GE-KM200 is able to
     * be in dimm mode, switch mode or disabled.
     * For more informations please have a look at {@link OutputModeEnum}.
     *
     * @return the current output mode of this device
     */
    OutputModeEnum getOutputMode();

    List<OutputChannelEnum> getOutputChannels();

    /**
     * Adds an increase command as {@link DeviceStateUpdate} to the list of outstanding commands.
     */
    void increase();

    /**
     * Adds a decrease command as {@link DeviceStateUpdate} to the list of outstanding commands.
     */
    void decrease();

    /**
     * Returns the current slat position of this device.
     *
     * @return current slat position
     */
    int getSlatPosition();

    /**
     * Adds a set slat position command as {@link DeviceStateUpdate} with the given slat position to the list of
     * outstanding commands.
     *
     * @param slatPosition to set
     */
    void setSlatPosition(int slatPosition);

    /**
     * Returns the maximal slat position value of this device.
     *
     * @return maximal slat position value
     */
    int getMaxSlatPosition();

    /**
     * Returns the minimal slat position value of this device.
     *
     * @return minimal slat position value
     */
    int getMinSlatPosition();

    /**
     * Returns the current output value of this device.
     * This can be the slat position or the brightness of this device.
     *
     * @return current output value
     */
    short getOutputValue();

    /**
     * Adds a set output value command as {@link DeviceStateUpdate} with the given output value to the list of
     * outstanding commands.
     *
     * @param outputValue to set
     */
    void setOutputValue(short outputValue);

    /**
     * Returns the maximal output value of this device.
     *
     * @return maximal output value
     */
    short getMaxOutputValue();

    /**
     * Returns a list with group ids which the device is part of.
     *
     * @return List of group ids
     */
    List<Short> getGroups();

    /**
     * Adds the given groupID to the group list.
     *
     * @param groupID to add
     */
    void addGroup(Short groupID);

    /**
     * Overrides the existing group list with the given new.
     *
     * @param newGroupList to set
     */
    void setGroups(List<Short> newGroupList);

    /**
     * Returns the scene output value of this device of the given scene id as {@link Integer} array. The first field is
     * the output value and the second is the angle value or -1 if no angle value exists.
     * If the method returns null, this scene id isn't read yet.
     *
     * @param sceneID of the scene
     * @return scene output value and scene angle value or null, if it isn't read out yet
     */
    Integer[] getSceneOutputValue(short sceneID);

    /**
     * Sets the scene output value of this device for the given scene id and scene output value.
     *
     * @param sceneId to set
     * @param sceneOutputValue to set
     */
    void setSceneOutputValue(short sceneId, int sceneOutputValue);

    /**
     * This configuration is very important. The devices can
     * be configured to not react to some commands (scene calls).
     * So you can't imply that a device automatically turns on (by default yes,
     * but if someone configured his own scenes, then maybe not) after a
     * scene call. This method returns true or false, if the configuration
     * for this sceneID already has been read
     *
     * @param sceneId the sceneID
     * @return true if this device has the configuration for this specific scene
     */
    boolean containsSceneConfig(short sceneId);

    /**
     * Add the config for this scene. The config has the configuration
     * for the specific sceneID.
     *
     * @param sceneId scene call id
     * @param sceneSpec config for this sceneID
     */
    void addSceneConfig(short sceneId, DeviceSceneSpec sceneSpec);

    /**
     * Get the config for this scene. The config has the configuration
     * for the specific sceneID.
     *
     * @param sceneId scene call id
     * @return sceneSpec config for this sceneID
     */
    DeviceSceneSpec getSceneConfig(short sceneId);

    /**
     * Should the device react on this scene call or not .
     *
     * @param sceneId scene call id
     * @return true, if this device should react on this sceneID
     */
    boolean doIgnoreScene(short sceneId);

    // follow methods added by Michael Ochel and Matthias Siegele
    /**
     * Returns true, if all sensor data are up to date or false if some have to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    boolean isSensorDataUpToDate();

    /**
     * Sets the priority to refresh the data of the sensors to the given priorities.
     * They can be never, low, medium or high.
     *
     * @param powerConsumptionRefreshPriority to set
     * @param electricMeterRefreshPriority to set
     * @param energyMeterRefreshPriority to set
     */
    void setSensorDataRefreshPriority(String powerConsumptionRefreshPriority, String electricMeterRefreshPriority,
            String energyMeterRefreshPriority);

    /**
     * Returns true, if the device is up to date.
     *
     * @return digitalSTROM-Device is up to date (true = yes | false = no)
     */
    boolean isDeviceUpToDate();

    /**
     * Returns the next {@link DeviceStateUpdate} to update the digitalSTROM-Device on the digitalSTROM-Server.
     *
     * @return DeviceStateUpdate for digitalSTROM-Device
     */
    DeviceStateUpdate getNextDeviceUpdateState();

    /**
     * Update the internal stored device object.
     *
     * @param deviceStateUpdate to update
     */
    void updateInternalDeviceState(DeviceStateUpdate deviceStateUpdate);

    /**
     * Call the given {@link InternalScene} on this {@link Device} and updates it.
     *
     * @param scene to call
     */
    void callInternalScene(InternalScene scene);

    /**
     * Undo the given {@link InternalScene} on this {@link Device} and updates it.
     *
     * @param scene to undo
     */
    void undoInternalScene(InternalScene scene);

    /**
     * Initial a call scene for the given scene number.
     *
     * @param sceneNumber to call
     */
    void callScene(Short sceneNumber);

    /**
     * Returns the current active {@link InternalScene}, otherwise null.
     *
     * @return active {@link InternalScene} or null
     */
    InternalScene getAcitiveScene();

    /**
     * Undo the active scene if a scene is active.
     */
    void undoScene();

    /**
     * Checks the scene configuration for the given scene number and initial a scene configuration reading with the
     * given priority if no scene configuration exists.
     *
     * @param sceneNumber to check
     * @param prio to update
     */
    void checkSceneConfig(Short sceneNumber, short prio);

    /**
     * Sets the given output mode as new output mode of this {@link Device}.
     *
     * @param newOutputMode to set
     */
    void setOutputMode(OutputModeEnum newOutputMode);

    /**
     * Returns a {@link List} of all saved scene-IDs configurations.
     *
     * @return a {@link List} of all saved scene-IDs
     */
    List<Short> getSavedScenes();

    /**
     * Initializes an internal device update as call scene for the given scene number.
     *
     * @param sceneNumber to call
     */
    void internalCallScene(Short sceneNumber);

    /**
     * Initializes an internal device update as undo scene.
     */
    void internalUndoScene();

    /**
     * Returns true, if this {@link Device} is a device with a switch output mode.
     *
     * @return true, if it is a switch otherwise false
     */
    boolean isSwitch();

    /**
     * Sets the given {@link Config} as new {@link Config}.
     *
     * @param config to set
     */
    void setConfig(Config config);

    /**
     * Returns the current angle position of the {@link Device}.
     *
     * @return current angle position
     */
    short getAnglePosition();

    /**
     * Adds a set angle value command as {@link DeviceStateUpdate} with the given angle value to the list of
     * outstanding commands.
     *
     * @param angle to set
     */
    void setAnglePosition(int angle);

    /**
     * Sets the scene output value and scene output angle of this device for the given scene id, scene output value and
     * scene output angle.
     *
     * @param sceneId to set
     * @param value to set
     * @param angle to set
     */
    void setSceneOutputValue(short sceneId, int value, int angle);

    /**
     * Returns the max angle value of the slat.
     *
     * @return max slat angle
     */
    int getMaxSlatAngle();

    /**
     * Returns the min angle value of the slat.
     *
     * @return min slat angle
     */
    int getMinSlatAngle();

    /**
     * Returns true, if it is a blind device.
     *
     * @return is blind (true = yes | false = no
     */
    boolean isBlind();

    /**
     * Saves scene configurations from the given sceneProperties in the {@link Device}. <br>
     * The {@link Map} has to be like the following format:
     * <ul>
     * <li><b>Key:</b> scene[sceneID]</li>
     * <li><b>Value:</b> {Scene: [sceneID], dontcare: [don't care flag], localPrio: [local prio flag], specialMode:
     * [special mode flag]}(0..1), {sceneValue: [scene value], sceneAngle: [scene angle]}(0..1))</li>
     * </ul>
     *
     * @param sceneProperties to save
     */
    void saveConfigSceneSpecificationIntoDevice(Map<String, String> sceneProperties);

    /**
     * Returns the min output value.
     *
     * @return min output value
     */
    short getMinOutputValue();

    /**
     * Adds a slat increase command as {@link DeviceStateUpdate} to the list of outstanding commands.
     */
    void increaseSlatAngle();

    /**
     * Adds a slat decrease command as {@link DeviceStateUpdate} to the list of outstanding commands.
     */
    void decreaseSlatAngle();

    /**
     * Saves scene configurations from the given sceneProperties in the {@link Device}. <br>
     * <br>
     * <b>The {@link String} has to be like the following format:</b><br>
     * {[sceneID] = }(1){Scene: [sceneID], dontcare: [don't care flag], localPrio: [local prio flag], specialMode:
     * [special mode flag]}(0..1), {sceneValue: [sceneValue]{, sceneAngle: [scene angle]}(0..1)}{\n}(0..1)<br>
     * <br>
     * e.g. "10 = Scene: PRESET_4, dontcare: false, localPrio: false, specialMode: false, flashMode: false, sceneValue:
     * 0\n"
     *
     * @param propertries to save
     */
    void saveConfigSceneSpecificationIntoDevice(String propertries);

    /**
     * Returns true, if this {@link Device} is a sensor device. That means, that this {@link Device} has no output
     * channel
     * ({@link OutputModeEnum#DISABLED}), but climate sensors.
     *
     * @return true, if it is a sensor device
     */
    boolean isSensorDevice();

    /**
     * Returns true, if this {@link Device} is a heating device. That means, that the output mode of this {@link Device}
     * is one of the following modes {@link OutputModeEnum#PWM} or {@link OutputModeEnum#SWITCH} and the
     * {@link FuncNameAndColorGroupEnum} is {@link FuncNameAndColorGroupEnum#HEATING}.
     *
     * @return true, if it is a heating device
     */
    boolean isHeatingDevice();

    /**
     * Sets the refresh priority for the given power sensor as {@link SensorEnum}. <br>
     * <b>Note:</b><br>
     * 1. The device must have this sensor type, otherwise the set has no effect.<br>
     * <br>
     * 2. Valid priorities are:<br>
     * - {@link Config#REFRESH_PRIORITY_NEVER}<br>
     * - {@link Config#REFRESH_PRIORITY_LOW}<br>
     * - {@link Config#REFRESH_PRIORITY_MEDIUM}<br>
     * - {@link Config#REFRESH_PRIORITY_HIGH}<br>
     * <br>
     * 3. Valid sensor types are:<br>
     * - {@link SensorEnum#POWER_CONSUMPTION}<br>
     * - {@link SensorEnum#OUTPUT_CURRENT}<br>
     * - {@link SensorEnum#ELECTRIC_METER}<br>
     * - {@link SensorEnum#ACTIVE_POWER}<br>
     *
     * @param powerSensorType the power sensor to set
     * @param refreshPriority the new refresh priority
     */
    void setSensorDataRefreshPriority(SensorEnum powerSensorType, String refreshPriority);

    /**
     * Returns the refresh priority of the given power sensor type as {@link SensorEnum}. If the sensor type is not
     * supported by
     * this {@link Device} or it is not a power sensor it will be returned null.
     *
     * @param powerSensorType of the sensor
     * @return the refresh priority
     */
    String getPowerSensorRefreshPriority(SensorEnum powerSensorType);

    /**
     * Returns a {@link List} with all power sensors, which are supported by this {@link Device}.
     *
     * @return all supported power sensors
     */
    List<SensorEnum> getPowerSensorTypes();

    /**
     * Returns a {@link List} with all climate sensors, which are supported by this {@link Device}.
     *
     * @return all supported climate sensors
     */
    List<SensorEnum> getClimateSensorTypes();

    /**
     * Returns all {@link DeviceSensorValue}'s of this {@link Device}.
     *
     * @return list of all {@link DeviceSensorValue}'s
     */
    List<DeviceSensorValue> getDeviceSensorValues();

    /**
     * Sets the given {@link DeviceSensorValue}. That means the given {@link DeviceSensorValue} will be added, if this
     * type of {@link DeviceSensorValue} does not exist before, otherwise the existing {@link DeviceSensorValue} will be
     * updated,
     * if the given {@link DeviceSensorValue} is newer.
     *
     * @param deviceSensorValue the new device sensor value
     */
    void setDeviceSensorValue(DeviceSensorValue deviceSensorValue);

    /**
     * Returns the {@link DeviceSensorValue} of the given sensor type as {@link SensorEnum} or null, if no
     * {@link DeviceSensorValue} exists for the given sensor type.
     *
     * @param sensorType of the sensor
     * @return the {@link DeviceSensorValue} or null
     */
    DeviceSensorValue getDeviceSensorValue(SensorEnum sensorType);

    /**
     * Returns the {@link DeviceSensorValue} of the given sensor index as {@link Short} or null, if no
     * {@link DeviceSensorValue} exists for the given sensor index.
     *
     * @param sensorIndex of the sensor
     * @return the {@link DeviceSensorValue} or null
     */
    DeviceSensorValue getDeviceSensorValue(Short sensorIndex);

    /**
     * Returns the sensor index for the given sensor type as {@link SensorEnum} of the {@link Device} or null, if the
     * sensor type does not exist. It will be needed to readout the current sensor value of the digitalSTROM device.
     *
     * @param sensorType of the sensor
     * @return sensor index for the sensor type
     */
    Short getSensorIndex(SensorEnum sensorType);

    /**
     * Returns the sensor type as {@link SensorEnum} of the given sensor index or null, if the given sensor type does
     * not exist.
     *
     * @param sensorIndex of the sensor
     * @return the sensor type or null
     */
    SensorEnum getSensorType(Short sensorIndex);

    /**
     * Returns the internal digitalSTROM sensor value for the given sensor type as {@link SensorEnum}, if the sensor
     * type exists and the value is valid. The resolution can be found at {@link SensorEnum}.
     *
     * @param sensorType of the sensor
     * @return the internal digitalSTROM sensor value or null
     */
    Integer getDsSensorValue(SensorEnum sensorType);

    /**
     * Returns the internal digitalSTROM sensor value for the given sensor index as {@link Short}, if the sensor
     * index exists and the value is valid. The resolution can be found at {@link SensorEnum}.
     *
     * @param sensorIndex of the sensor
     * @return the internal digitalSTROM sensor value or null
     */
    Integer getDsSensorValue(Short sensorIndex);

    /**
     * Returns the float sensor value for the given sensor type as {@link SensorEnum}, if the sensor
     * type exists and the value is valid. The resolution can be found at {@link SensorEnum}.
     *
     * @param sensorType of the sensor
     * @return the float sensor value or null
     */
    Float getFloatSensorValue(SensorEnum sensorType);

    /**
     * Returns the float sensor value for the given sensor index as {@link Short}, if the sensor
     * index exists and the value is valid. The resolution can be found at {@link SensorEnum}.
     *
     * @param sensorIndex of the sensor
     * @return the float sensor value or null
     */
    Float getFloatSensorValue(Short sensorIndex);

    /**
     * Sets the float sensor value for a given sensor type as {@link SensorEnum}. If the sensor type does not exist, it
     * will be returned false.
     *
     * @param sensorType of the sensor
     * @param floatSensorValue the new float sensor value
     * @return true, if it was successful, otherwise false
     */
    boolean setFloatSensorValue(SensorEnum sensorType, Float floatSensorValue);

    /**
     * Sets the float sensor value for a given sensor index as {@link Short}. If the sensor type does not exist, it
     * will be returned false.
     *
     * @param sensorIndex of the sensor
     * @param floatSensorValue the new float sensor value
     * @return true, if it was successful, otherwise false
     */
    boolean setFloatSensorValue(Short sensorIndex, Float floatSensorValue);

    /**
     * Sets the internal digitalSTROM sensor value for a given sensor index as {@link Short}. If the sensor index does
     * not exist, it will be returned false.
     *
     * @param sensorIndex of the sensor
     * @param dSSensorValue the new internal digitalSTROM sensor value
     * @return true, if it was successful, otherwise false
     */
    boolean setDsSensorValue(Short sensorIndex, Integer dSSensorValue);

    /**
     * Sets the internal digitalSTROM sensor value for a given sensor type as {@link SensorEnum}. If the sensor type
     * does
     * not exist, it will be returned false.
     *
     * @param sensorType of the sensor
     * @param dSSensorValue the new internal digitalSTROM sensor value
     * @return true, if it was successful, otherwise false
     */
    boolean setDsSensorValue(SensorEnum sensorType, Integer dSSensorValue);

    /**
     * Sets the internal digitalSTROM and float sensor value for a given sensor index as {@link Short}. If the sensor
     * index does not exist, it will be returned false.
     *
     * @param sensorIndex of the sensor
     * @param dSSensorValue the new internal digitalSTROM sensor value
     * @param floatSensorValue the new float sensor value
     * @return true, if it was successful, otherwise false
     */
    boolean setDsSensorValue(Short sensorIndex, Integer dSSensorValue, Float floatSensorValue);

    /**
     * Sets the internal digitalSTROM and float sensor value for a given sensor type as {@link SensorEnum}. If the
     * sensor type does not exist, it will be returned false.
     *
     * @param sensorType of the sensor
     * @param dSSensorValue the new internal digitalSTROM sensor value
     * @param floatSensorValue the new float sensor value
     * @return true, if it was successful, otherwise false
     */
    boolean setDsSensorValue(SensorEnum sensorType, Integer dSSensorValue, Float floatSensorValue);

    /**
     * Returns true, if this {@link Device} has sensors, otherwise false.
     *
     * @return true, if device has sensors
     */
    boolean hasSensors();

    /**
     * Returns true, if this {@link Device} has climate sensors, otherwise false.
     *
     * @return true, if device has climate sensors
     */
    boolean hasClimateSensors();

    /**
     * Returns true, if this {@link Device} has power sensors, otherwise false.
     *
     * @return true, if device has power sensors
     */
    boolean hasPowerSensors();

    /**
     * Only needed for {@link DeviceConsumptionSensorJob}'s. To set the internal digitalSTROM sensor value please use
     * {@link #setDsSensorValue(SensorEnum, Integer)}.
     *
     * @param sensorType of the sensor
     * @param value new value
     */
    void setDeviceSensorDsValueBySensorJob(SensorEnum sensorType, Integer value);

    /**
     * Enables the internal sensor echo box for {@link EventNames#DEVICE_SENSOR_VALUE} events.
     */
    void enableSensorEchoBox();

    /**
     * Disables the internal sensor echo box for {@link EventNames#DEVICE_SENSOR_VALUE} events.
     */
    void disableSensorEchoBox();

    /**
     * Returns true, if the internal sensor echo box is enabled, otherwise false.
     *
     * @return true, if the internal sensor echo box is enabled
     */
    boolean isSensorEchoBoxEnabled();

    /**
     * Sets the {@link DeviceSensorValue} through an {@link EventItem} of the type
     * {@link EventNames#DEVICE_SENSOR_VALUE}.
     *
     * @param event of the sensor update
     */
    void setDeviceSensorByEvent(EventItem event);

    /**
     * Returns true, if the refresh priority of the given power sensor type as {@link SensorEnum} is equals
     * {@link Config#REFRESH_PRIORITY_NEVER}, otherwise false.
     *
     * @param powerSensorType of the sensor
     * @return true, if refresh priority is never
     */
    boolean checkPowerSensorRefreshPriorityNever(SensorEnum powerSensorType);

    /**
     * Returns true, if the given sensor type as {@link SensorEnum} is supported by this {@link Device}, otherwise
     * false.
     *
     * @param sensorType of the sensor
     * @return true, if the sensor type is supported
     */
    boolean supportsSensorType(SensorEnum sensorType);

    /**
     * Returns true, if this {@link Device} is a temperature controlled device, otherwise false. That means, that the
     * output mode is one of the output modes in
     * {@link OutputModeEnum#outputModeIsTemperationControlled(OutputModeEnum)}.
     *
     * @return true, if this {@link Device} is a temperature controlled
     */
    boolean isTemperatureControlledDevice();

    /**
     * Returns true, if this {@link Device} is a binary input device. That means it have no output mode
     * ({@link OutputModeEnum#DISABLED}), but {@link DeviceBinaryInput}'s.
     *
     * @return true, if this {@link Device} is a binary input device
     */
    boolean isBinaryInputDevice();

    /**
     * Returns a {@link List} which contains all currently configured {@link DeviceBinaryInput}'s.
     *
     * @return list with all configured {@link DeviceBinaryInput}'s
     */
    List<DeviceBinaryInput> getBinaryInputs();

    /**
     * Returns the {@link DeviceBinaryInput} of the given binary input type as {@link DeviceBinarayInputEnum}} or null,
     * if the binary input type does not exist.
     *
     * @param binaryInputType of the {@link DeviceBinaryInput}
     * @return the {@link DeviceBinaryInput} or null
     */
    DeviceBinaryInput getBinaryInput(DeviceBinarayInputEnum binaryInputType);

    /**
     * Returns the state of the given binary input type as {@link DeviceBinarayInputEnum}} or null, if the binary input
     * type does not exist.
     *
     * @param binaryInputType of the {@link DeviceBinaryInput}
     * @return state of the given binary input type or null
     */
    Short getBinaryInputState(DeviceBinarayInputEnum binaryInputType);

    /**
     * Sets the state of an existing {@link DeviceBinaryInput}. If the given {@link DeviceBinarayInputEnum} does not
     * exist, it will returned false, otherwise true.
     *
     * @param binaryInputType of the {@link DeviceBinaryInput}
     * @param newState the new state
     * @return true, if it was successful, otherwise false
     */
    boolean setBinaryInputState(DeviceBinarayInputEnum binaryInputType, Short newState);

    /**
     * Sets the given {@link List} of {@link DeviceBinaryInput}'s as supported binary inputs.
     *
     * @param newBinaryInputs to set
     */
    void setBinaryInputs(List<DeviceBinaryInput> newBinaryInputs);

    /**
     * Returns true, if the given power sensor type as {@link SensorEnum} is up to date and does not need a refresh,
     * otherwise it will returned false.
     *
     * @param powerSensorType of the sensor
     * @return true, if the power sensor is up to date
     */
    boolean isPowerSensorUpToDate(SensorEnum powerSensorType);

    /**
     * Returns a {@link List} of all supported sensor types as {@link SensorEnum}.
     *
     * @return all supported sensor types
     */
    List<SensorEnum> getSensorTypes();
}
