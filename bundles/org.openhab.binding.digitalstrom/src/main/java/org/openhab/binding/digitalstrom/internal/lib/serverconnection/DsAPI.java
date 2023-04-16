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
package org.openhab.binding.digitalstrom.internal.lib.serverconnection;

import java.util.List;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.BaseSensorValues;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.AssignedSensors;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.SensorValues;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlConfig;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlInternals;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlValues;
import org.openhab.binding.digitalstrom.internal.lib.structure.Apartment;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.CachedMeteringValue;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceConfig;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceSceneSpec;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceParameterClassEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * digitalSTROM-API based on dSS-Version higher then 1.14.5.
 * digitalSTROM documentation can be found at http://developer.digitalstrom.org/Architecture/v1.1/dss-json.pdf
 *
 * @author Alexander Betker - Initial contribution
 *
 * @author Michael Ochel - add missing java-doc, update digitalSTROM-JSON-API as far as possible to the pdf version from
 *         June 19, 2014 and add checkConnection method and ALL_METERS constant
 * @author Matthias Siegele - add missing java-doc, update digitalSTROM-JSON-API as far as possible to the pdf version
 *         from June 19, 2014 and add checkConnection method and ALL_METERS constant
 */
public interface DsAPI {

    /**
     * Meter field to get all meters by calling {@link #getLatest(String, MeteringTypeEnum, String, MeteringUnitsEnum)}.
     * It has to be set by meterDSIDs.
     */
    static final String ALL_METERS = ".meters(all)";

    /**
     * Calls the scene sceneNumber on all devices of the apartment. If groupID
     * or groupName are specified. Only devices contained in this group will be
     * addressed.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param groupID not required
     * @param groupName not required
     * @param sceneNumber required
     * @param force not required
     * @return true, if successful
     */
    boolean callApartmentScene(String sessionToken, Short groupID, String groupName, Scene sceneNumber, Boolean force);

    /**
     * Returns all zones
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return Apartment
     */
    Apartment getApartmentStructure(String sessionToken);

    /**
     * Returns the list of devices in the apartment. If unassigned is true,
     * only devices that are not assigned to a zone will be returned.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return List of devices
     */
    List<Device> getApartmentDevices(String sessionToken);

    /**
     * Returns an array containing all digitalSTROM-Meters of the apartment.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return List of all {@link Circuit}s
     */
    List<Circuit> getApartmentCircuits(String sessionToken);

    /**
     * Returns a list of dSID's of all meters(dSMs)
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return String-List with dSID's
     */
    List<String> getMeterList(String sessionToken);

    /**
     * Calls the sceneNumber on all devices in the given zone. If groupID or groupName
     * are specified only devices contained in this group will be addressed.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID needs either id or name
     * @param zoneName needs either id or name
     * @param groupID not required
     * @param groupName not required
     * @param sceneNumber required (only a zone/user scene is possible - sceneNumber 0..63 )
     * @param force not required
     * @return true on success
     */
    boolean callZoneScene(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName,
            SceneEnum sceneNumber, Boolean force);

    /**
     * Turns the device on. This will call the scene "max" on the device.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @return true, if successful
     */
    boolean turnDeviceOn(String sessionToken, DSID dSID, String dSUID, String deviceName);

    /**
     * Turns the device off. This will call the scene "min" on the device.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @return true, if successful
     */
    boolean turnDeviceOff(String sessionToken, DSID dSID, String dSUID, String deviceName);

    /**
     * Sets the output value of device.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param value required (0 - 255)
     * @return true, if successful
     */
    boolean setDeviceValue(String sessionToken, DSID dSID, String dSUID, String deviceName, Integer value);

    /**
     * Gets the value of configuration class at offset index.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param clazz required
     * @param index required
     * @return config with values
     */
    DeviceConfig getDeviceConfig(String sessionToken, DSID dSID, String dSUID, String deviceName,
            DeviceParameterClassEnum clazz, Integer index);

    /**
     * Gets the device output value from parameter at the given offset.
     * The available parameters and offsets depend on the features of the
     * hardware components.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param offset required (known offset e.g. 0)
     * @return current device output value or -1, if the request was not successful
     */
    int getDeviceOutputValue(String sessionToken, DSID dSID, String dSUID, String deviceName, Short offset);

    /**
     * Sets the device output value at the given offset. The available
     * parameters and offsets depend on the features of the hardware components.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param offset required
     * @param value required (0 - 65535)
     * @return true, if successful
     */
    boolean setDeviceOutputValue(String sessionToken, DSID dSID, String dSUID, String deviceName, Short offset,
            Integer value);

    /**
     * Gets the device configuration for a specific scene command.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param sceneID required (0 .. 255)
     * @return scene configuration
     */
    DeviceSceneSpec getDeviceSceneMode(String sessionToken, DSID dSID, String dSUID, String deviceName, Short sceneID);

    /**
     * Requests the sensor value for a given index.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param sensorIndex required
     * @return sensor value
     */
    short getDeviceSensorValue(String sessionToken, DSID dSID, String dSUID, String deviceName, Short sensorIndex);

    /**
     * Calls scene sceneNumber on the device.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param sceneNumber required
     * @param force not required
     * @return true, if successful
     */
    boolean callDeviceScene(String sessionToken, DSID dSID, String dSUID, String deviceName, Scene sceneNumber,
            Boolean force);

    /**
     * Subscribes to an event given by the name. The subscriptionID is a unique id
     * that is defined by the subscriber. It is possible to subscribe to several events,
     * using the same subscription id, this allows to retrieve a grouped output of the
     * events (i.e. get output of all subscribed by the given id).
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param eventName required
     * @param subscriptionID required
     * @param connectionTimeout to set
     * @param readTimeout to set
     * @return true on success
     */
    boolean subscribeEvent(String sessionToken, String eventName, Integer subscriptionID, int connectionTimeout,
            int readTimeout);

    /**
     * Unsubscribes from an event given by the name. The subscriptionID is a unique
     * id that was used in the subscribe call.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param eventName required
     * @param subscriptionID required
     * @param connectionTimeout to set
     * @param readTimeout to set
     * @return true on success
     */
    boolean unsubscribeEvent(String sessionToken, String eventName, Integer subscriptionID, int connectionTimeout,
            int readTimeout);

    /**
     * Gets event information and output. The subscriptionID is a unique id
     * that was used in the subscribe call. All events, subscribed with the
     * given id will be handled by this call. A timeout, in case no events
     * are taken place, can be specified (in ms). By default the timeout
     * is disabled: 0 (zero), if no events occur the call will block.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param subscriptionID required
     * @param timeout optional
     * @return Event-String
     */
    String getEvent(String sessionToken, Integer subscriptionID, Integer timeout);

    /**
     * Returns the dSS time in UTC seconds since epoch.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return time
     */
    int getTime(String sessionToken);

    /**
     * <b>Description partially taken form digitalSTROM JSON-API:</b><br>
     * Returns the version of the digitalSTROM-Server software version as {@link Map}.<br>
     * <br>
     * <b>Key, value:</b><br>
     * <ul>
     * <li><b>version</b> the dSS application version</li>
     * <li><b>distroVersion</b> the host platform firmware release (since v1.10)</li>
     * <li><b>Hardware</b> the host platform hardware identifier (since v1.10)</li>
     * <li><b>Revision</b> the host platform hardware revision number (since v1.10)</li>
     * <li><b>Serial</b> the host platform hardware serial number (since v1.10)</li>
     * <li><b>Ethernet</b> the host platform IEEE Mac address (since v1.10)</li>
     * <li><b>MachineID</b> the host system unique id (since v1.10)</li>
     * <li><b>Kernel</b> the host system Linux kernel release string (since v1.10)</li>
     * </ul>
     *
     * @return the digitalSTROM-Server software version
     */
    Map<String, String> getSystemVersion();

    /**
     * Creates a new session using the registered application token
     *
     * @param applicationToken required
     * @return sessionToken
     */
    String loginApplication(String applicationToken);

    /**
     * Creates a new session.
     *
     * @param user required
     * @param password required
     * @return new session token
     */
    String login(String user, String password);

    /**
     * Destroys the session and signs out the user
     *
     * @return true, if logout was successful, otherwise false
     */
    boolean logout();

    /**
     * Returns the dSID of the digitalSTROM Server.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return dSID
     */
    Map<String, String> getDSID(String sessionToken);

    /**
     * Returns a token for passwordless login. The token will need to be approved
     * by a user first, the caller must not be logged in.
     *
     * @param applicationName required
     * @return applicationToken
     */
    String requestAppplicationToken(String applicationName);

    /**
     * Revokes an application token, caller must be logged in.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param applicationToken to login
     * @return true, if successful, otherwise false
     */
    boolean revokeToken(String applicationToken, String sessionToken);

    /**
     * Enables an application token, caller must be logged in.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param applicationToken required
     * @return true, if successful, otherwise false
     */
    boolean enableApplicationToken(String applicationToken, String sessionToken);

    /**
     * Returns all resolutions stored on this dSS
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return List of resolutions
     */
    List<Integer> getResolutions(String sessionToken);

    /**
     * Returns cached energy meter value or cached power consumption
     * value in watt (W). The type parameter defines what should
     * be returned, valid types, 'energyDelta' are 'energy' and
     * 'consumption' you can also see at {@link MeteringTypeEnum}. 'energy' and 'energyDelta' are available in two
     * units: 'Wh' (default) and 'Ws' you can also see at {@link MeteringUnitsEnum}. The meterDSIDs parameter follows
     * the
     * set-syntax, currently it supports: .meters(dsid1,dsid2,...) and .meters(all)
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param type required
     * @param meterDSIDs required
     * @param unit optional
     * @return cached metering values
     */
    List<CachedMeteringValue> getLatest(String sessionToken, MeteringTypeEnum type, String meterDSIDs,
            MeteringUnitsEnum unit);

    /**
     * Returns cached energy meter value or cached power consumption
     * value in watt (W). The type parameter defines what should
     * be returned, valid types, 'energyDelta' are 'energy' and
     * 'consumption' you can also see at {@link MeteringTypeEnum}. 'energy' and 'energyDelta' are available in two
     * units: 'Wh' (default) and 'Ws' you can also see at {@link MeteringUnitsEnum}. <br>
     * The meterDSIDs parameter you can directly pass a {@link List} of the digitalSTROM-Meter dSID's as {@link String}.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param type required
     * @param meterDSIDs required
     * @param unit optional
     * @return cached metering values
     */
    List<CachedMeteringValue> getLatest(String sessionToken, MeteringTypeEnum type, List<String> meterDSIDs,
            MeteringUnitsEnum unit);

    /**
     * Checks the connection and returns the HTTP-Status-Code.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return HTTP-Status-Code
     */
    int checkConnection(String sessionToken);

    /**
     * Returns the configured scene output value for the given sceneId of the digitalSTROM-Device with the given dSID.
     * <br>
     * At array position 0 is the output value and at position 1 the angle value, if the device is a blind.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param sceneId required
     * @return scene value at array position 0 and angle at position 1
     */
    int[] getSceneValue(String sessionToken, DSID dSID, String dSUID, String deviceName, Short sceneId);

    /**
     * Calls the INC scene on the digitalSTROM-Device with the given dSID and returns true if the request was success.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @return success true otherwise false
     */
    boolean increaseValue(String sessionToken, DSID dSID, String dSUID, String deviceName);

    /**
     * Calls the DEC scene on the digitalSTROM-Device with the given dSID and returns true if the request was
     * successful.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @return success true otherwise false
     */
    boolean decreaseValue(String sessionToken, DSID dSID, String dSUID, String deviceName);

    /**
     * Undos the given sceneNumer of the digitalSTROM-Device with the given dSID and returns true if the request was
     * successful.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID, dSUID or name
     * @param dSUID needs either dSID, dSUID or name
     * @param deviceName needs either dSID, dsUID or name
     * @param sceneNumber required
     * @return success true otherwise false
     */
    boolean undoDeviceScene(String sessionToken, DSID dSID, String dSUID, String deviceName, Scene sceneNumber);

    /**
     * Undo the given sceneNumer on the digitalSTROM apartment-group with the given groupID or groupName and returns
     * true
     * if the request was successful.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param groupID needs either groupID or groupName
     * @param groupName needs either groupID or groupName
     * @param sceneNumber required
     * @return success true otherwise false
     */
    boolean undoApartmentScene(String sessionToken, Short groupID, String groupName, Scene sceneNumber);

    /**
     * Undo the given sceneNumer on the digitalSTROM zone-group with the given zoneID or zoneName and groupID or
     * groupName and returns true if the request was successful.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID needs either zoneID or zoneName
     * @param zoneName needs either zoneID or zoneName
     * @param groupID needs either groupID or groupName
     * @param groupName needs either groupID or groupName
     * @param sceneNumber required
     * @return success true otherwise false
     */
    boolean undoZoneScene(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName,
            SceneEnum sceneNumber);

    /**
     * Returns user defined name of the digitalSTROM installation.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return name of the digitalSTROM installation
     */
    String getInstallationName(String sessionToken);

    /**
     * Returns user defined name of the zone from the given zone id.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required
     * @return name of the given zone id
     */
    String getZoneName(String sessionToken, Integer zoneID);

    /**
     * Returns user defined name of the device from the given dSID
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID needs either dSID or dSUID
     * @param dSUID needs either dSID or dSUID
     * @return name of the device with the given dSID or dSUID
     */
    String getDeviceName(String sessionToken, DSID dSID, String dSUID);

    /**
     * Returns user defined name of the circuit from the given dSID.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param dSID required
     * @return name of the given circuit dSID
     */
    String getCircuitName(String sessionToken, DSID dSID);

    /**
     * Returns user defined name of the scene from the given zoneID, groupID and sceneID.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID (0 is broadcast)
     * @param zoneName of the zone
     * @param groupID (0 is broadcast)
     * @param sceneID (between 0 and 127)
     * @return name of the scene otherwise null
     */
    String getSceneName(String sessionToken, Integer zoneID, String zoneName, Short groupID, Short sceneID);

    /**
     * Returns the temperature control status to the given zone.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @return temperature control status to the given zone
     */
    TemperatureControlStatus getZoneTemperatureControlStatus(String sessionToken, Integer zoneID, String zoneName);

    /**
     * Returns the temperature control configuration of the given zone. It's like the temperature control status added
     * by the following control values.
     *
     * CtrlKp = Control proportional factor
     * CtrlTs = Control sampling time
     * CtrlTi = Control integrator time constant
     * CtrlKd = Control differential factor
     * CtrlImin = Control minimum integrator value
     * CtrlImax = Control maximum integrator value
     * CtrlYmin = Control minimum control value
     * CtrlYmax = Control maximum control value
     * CtrlAntiWindUp = Control integrator anti wind up: 0=inactive, 1=active
     * CtrlKeepFloorWarm = Control mode with higher priority on comfort: 0=inactive, 1=active
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @return temperature control status with configuration parameters
     */
    TemperatureControlConfig getZoneTemperatureControlConfig(String sessionToken, Integer zoneID, String zoneName);

    /**
     * Returns the temperature control values to their control modes of the given zone.
     * There are following control modes:
     * <ul>
     * <li>0 Off</li>
     * <li>1 Comfort</li>
     * <li>2 Economy</li>
     * <li>3 Not Used</li>
     * <li>4 Night</li>
     * <li>5 Holiday</li>
     * <li>6 Cooling</li>
     * <li>7 CollingOff</li>
     * </ul>
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @return temperature control values of control modes
     */
    TemperatureControlValues getZoneTemperatureControlValues(String sessionToken, Integer zoneID, String zoneName);

    /**
     * Set the configuration of the zone temperature control.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID (required alternative zoneName)
     * @param zoneName (required alternative zoneID)
     * @param controlDSUID dSUID of the meter or service that runs the control algorithm for this zone (optional)
     * @param controlMode Control mode, can be one of: 0=off; 1=pid-control; 2=zone-follower; 3=fixed-value; 4=manual
     *            (Optional)
     * @param referenceZone Zone number of the reference zone (Optional for ControlMode 2)
     * @param ctrlOffset Control value offset (Optional for ControlMode 2)
     * @param emergencyValue Fixed control value in case of malfunction (Optional for ControlMode 1)
     * @param manualValue Control value for manual mode (Optional for ControlMode 1)
     * @param ctrlKp Control proportional factor (Optional for ControlMode 1)
     * @param ctrlTs Control sampling time (Optional for ControlMode 1)
     * @param ctrlTi Control integrator time constant (Optional for ControlMode 1)
     * @param ctrlKd Control differential factor (Optional for ControlMode 1)
     * @param ctrlImin Control minimum integrator value (Optional for ControlMode 1)
     * @param ctrlImax Control maximum integrator value (Optional for ControlMode 1)
     * @param ctrlYmin Control minimum control value (Optional for ControlMode 1)
     * @param ctrlYmax Control maximum control value (Optional for ControlMode 1)
     * @param ctrlAntiWindUp Control integrator anti wind up (Optional for ControlMode 1)
     * @param ctrlKeepFloorWarm Control mode with higher priority on comfort (Optional for ControlMode 1)
     * @return true, if successful
     */
    boolean setZoneTemperatureControlConfig(String sessionToken, Integer zoneID, String zoneName, String controlDSUID,
            Short controlMode, Integer referenceZone, Float ctrlOffset, Float emergencyValue, Float manualValue,
            Float ctrlKp, Float ctrlTs, Float ctrlTi, Float ctrlKd, Float ctrlImin, Float ctrlImax, Float ctrlYmin,
            Float ctrlYmax, Boolean ctrlAntiWindUp, Boolean ctrlKeepFloorWarm);

    /**
     * Returns the assigned Sensor dSUID of a zone.
     *
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @return assigned Sensor dSUID of the given zone.
     */
    AssignedSensors getZoneAssignedSensors(String sessionToken, Integer zoneID, String zoneName);

    /**
     * Sets the temperature control state of a given zone.<br>
     * Control states: 0=internal; 1=external; 2=exbackup; 3=emergency
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @param controlState required
     * @return success true otherwise false
     */
    boolean setZoneTemperatureControlState(String sessionToken, Integer zoneID, String zoneName, String controlState);

    /**
     * Sets the wished temperature (control mode = {@link ControlModes#PID_CONTROL}) or control valve value for a
     * operation mode, see
     * {@link OperationModes}.<br>
     * To set the values a {@link List} with an object array has to be set as controlVlaues parameter. The 1th field has
     * to be a {@link String} for the {@link OperationModes} name and the 2nd field has to be an {@link Integer} for the
     * new value. If the control mode is {@link ControlModes#PID_CONTROL} it is the nominal temperature, otherwise it is
     * the control valve value.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @param controlValues new control values
     * @return success true otherwise false
     */
    boolean setZoneTemperatureControlValues(String sessionToken, Integer zoneID, String zoneName,
            List<Object[]> controlValues);

    /**
     * Returns the value of a Sensor of the given zone.
     *
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @return value of a Sensor of the given zone
     */
    SensorValues getZoneSensorValues(String sessionToken, Integer zoneID, String zoneName);

    /**
     * Set the source of a sensor in a zone to a given device source address.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @param sensorType to set
     * @param dSID to set
     * @return success true otherwise false
     */
    boolean setZoneSensorSource(String sessionToken, Integer zoneID, String zoneName, SensorEnum sensorType, DSID dSID);

    /**
     * Remove all assignments for a particular sensor type in a zone.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @param sensorType to clear
     * @return success true otherwise false
     *
     */
    boolean clearZoneSensorSource(String sessionToken, Integer zoneID, String zoneName, SensorEnum sensorType);

    /**
     * Returns internal status information of the temperature control of a zone.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID required or zoneName
     * @param zoneName required or zoneID
     * @return internal status information of the temperature control of a zone
     */
    TemperatureControlInternals getZoneTemperatureControlInternals(String sessionToken, Integer zoneID,
            String zoneName);

    /**
     * Returns the temperature control status of all zones.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return temperature control status of all zones
     */
    List<TemperatureControlStatus> getApartmentTemperatureControlStatus(String sessionToken);

    /**
     * Returns the temperature control status of all zones.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return temperature control status of all zones
     */
    Map<Integer, TemperatureControlConfig> getApartmentTemperatureControlConfig(String sessionToken);

    /**
     * Returns the temperature control status of all zones.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return temperature control status of all zones
     */
    Map<Integer, TemperatureControlValues> getApartmentTemperatureControlValues(String sessionToken);

    /**
     * Returns the assigned Sensor dSUID of all zones.
     *
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return assigned Sensor dSUID of all zones.
     */
    Map<Integer, AssignedSensors> getApartmentAssignedSensors(String sessionToken);

    /**
     * Returns the value of a Sensor of all zones.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @return value of a Sensor of all zones
     */
    Map<Integer, BaseSensorValues> getApartmentSensorValues(String sessionToken);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Returns a part of the tree specified by query. All queries start from the root.
     * The properties to be included have to be put in parentheses. A query to get
     * all device from zone4 would look like this: ’/apartment/zones/zone4/*(ZoneID,name)’.
     * More complex combinations (see example below) are also possible.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param query required
     * @return response as {@link JsonObject}
     */
    JsonObject query(String sessionToken, String query);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Differs from query(1) only in the format of the the returned json struct.<br>
     * <br>
     * <i>Folder selects the nodes to descend, Property declares which attributes
     * we are extracting from the current node. If no properties are declared for a
     * folder, nothing is extracted, and the node will not show up in the resulting
     * json structure.</i>
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param query required
     * @return response as {@link JsonObject}
     */
    JsonObject query2(String sessionToken, String query);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Set the output value of a group of devices in a zone to a given value. <br>
     * <br>
     * <b>Notice:</b> Setting output values directly bypasses the group state machine
     * and is not recommended.<br>
     * <br>
     * If the group parameters are omitted the command is sent as broadcast
     * to all devices in the selected zone. <br>
     * <br>
     * <b>Notice:</b> Setting output values without a group identification is strongly
     * unrecommended.<br>
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID or zoneName are required
     * @param zoneName or zoneID are required
     * @param groupID optional
     * @param groupName optional
     * @param value required
     * @return true, if request was successful, otherwise false
     */
    boolean setZoneOutputValue(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName,
            Integer value);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Executes the "blink" function on a group of devices in a zone for identification
     * purposes.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID or zoneName are required
     * @param zoneName or zoneID are required
     * @param groupID optional
     * @param groupName optional
     * @return true, if request was successful, otherwise false
     */
    boolean zoneBlink(String sessionToken, Integer zoneID, String zoneName, Short groupID, String groupName);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Send a sensor value to a group of devices in a zone.<br>
     * If the group parameter is omitted the command is sent as broadcast to
     * all devices in the selected zone. The reference for the sensor type definitions
     * can be found in the ds-basics document.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param zoneID or zoneName are required
     * @param zoneName or zoneID are required
     * @param groupID optional
     * @param sourceDSUID optional
     * @param sensorValue required
     * @param sensorType required
     * @return true, if request was successful, otherwise false
     */
    boolean pushZoneSensorValue(String sessionToken, Integer zoneID, String zoneName, Short groupID, String sourceDSUID,
            Float sensorValue, SensorEnum sensorType);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Returns the string value of the property, this call will fail if the property is
     * not of type ’string’.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path to property required
     * @return {@link String} value of the property
     */
    String propertyTreeGetString(String sessionToken, String path);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Sets the string value of the property, this call will fail if the property is not
     * of type ’string’.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path to property required
     * @return {@link JsonArray} of child nodes
     */
    JsonArray propertyTreeGetChildren(String sessionToken, String path);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Sets the string value of the property, this call will fail if the property is not
     * of type ’string’.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @param value required
     * @return true, if successful
     */
    Boolean propertyTreeSetString(String sessionToken, String path, String value);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Returns the integer value of the property, this call will fail if the property is
     * not of type ’integer’.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @return {@link Integer} value of the property
     */
    Integer propertyTreeGetInteger(String sessionToken, String path);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Sets the integer value of the property, this call will fail if the property is not
     * of type ’integer’.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @param value required
     * @return true, if successful
     */
    Boolean propertyTreeSetInteger(String sessionToken, String path, Integer value);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Returns the boolean value of the property, this call will fail if the property is
     * not of type ’boolean’.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @return {@link Boolean} value of the property
     */
    Boolean propertyTreeGetBoolean(String sessionToken, String path);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Returns the boolean value of the property, this call will fail if the property is
     * not of type ’boolean’.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @param value required
     * @return true, if successful
     */
    Boolean propertyTreeSetBoolean(String sessionToken, String path, Boolean value);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Returns the type of the property as {@link String}, this can be "none", "string", "integer" or
     * "boolean".
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @return the type of the property as {@link String}
     */
    String propertyTreeGetType(String sessionToken, String path);

    /**
     * Returns the flag values of a property as {@link Map}. The key is the flag type and the value the {@link Boolean}
     * value.<br>
     * <br>
     * Flag types are:<br>
     * - READABLE <br>
     * - WRITEABLE <br>
     * - ARCHIVE <br>
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @return the type of the property as {@link String}
     */
    Map<String, Boolean> propertyTreeGetFlages(String sessionToken, String path);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Sets a given flag of a property.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @param flag required
     * @param value required
     * @return true, if successful
     */
    Boolean propertyTreeSetFlag(String sessionToken, String path, String flag, Boolean value);

    /**
     * <b>Description taken form digitalSTROM JSON-API:</b><br>
     * Removes a property node.
     *
     * @param sessionToken can be null, if a
     *            {@link org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager} is
     *            registered at the {@link HttpTransport}
     * @param path required
     * @return true, if successful
     */
    Boolean propertyTreeRemove(String sessionToken, String path);
}
