/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.manager.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.climate.TemperatureControlSensorTransmitter;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;
import org.openhab.binding.digitalstrom.internal.lib.event.EventHandler;
import org.openhab.binding.digitalstrom.internal.lib.event.EventListener;
import org.openhab.binding.digitalstrom.internal.lib.event.constants.EventNames;
import org.openhab.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;
import org.openhab.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.openhab.binding.digitalstrom.internal.lib.listener.SystemStateChangeListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ApplicationGroup;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TemperatureControlManager} is responsible for handling the zone temperature control of the digitalSTROM
 * zones. For that it implements an {@link EventHandler} to get informed by control changes, like the target
 * temperature.
 * It also implement the {@link TemperatureControlSensorTransmitter}, so the zone temperature can be set through this
 * class. <br>
 * <br>
 * To check, if the heating-control-app is installed at the digitalSTROM server the static method
 * {@link #isHeatingControllerInstallated(ConnectionManager)} can be used.<br>
 * <br>
 * To get informed by status changes tow listener types can be registered to the {@link TemperatureControlManager}:<br>
 * {@link TemperatureControlStatusListener}, to get informed by configuration and status changes or as discovery.<br>
 * {@link SystemStateChangeListener}, to get informed by heating water system changes. The heating system states are
 * {@link #STATE_HEATING_WATER_SYSTEM_OFF}, {@link #STATE_HEATING_WATER_SYSTEM_COLD_WATER} and
 * {@link #STATE_HEATING_WATER_SYSTEM_COLD_WATER}<br>
 * <br>
 * The {@link TemperatureControlManager} also contains some helpful static constants, like
 * {@link #GET_HEATING_WATER_SYSTEM_STATE_PATH} to get the current heating water system state through
 * {@link DsAPI#propertyTreeGetString(String, String)}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class TemperatureControlManager implements EventHandler, TemperatureControlSensorTransmitter {

    private static final List<String> SUPPORTED_EVENTS = Arrays.asList(EventNames.HEATING_CONTROL_OPERATION_MODE);

    private final Logger logger = LoggerFactory.getLogger(TemperatureControlManager.class);

    private final ConnectionManager connectionMananager;
    private final DsAPI dSapi;
    private final EventListener eventListener;
    private boolean isConfigured = false;

    private HashMap<Integer, TemperatureControlStatusListener> zoneTemperationControlListenerMap;
    private HashMap<Integer, TemperatureControlStatus> temperationControlStatus;
    private TemperatureControlStatusListener discovery;
    private SystemStateChangeListener systemStateChangeListener;

    /**
     * Name of the digitalSTROM heating water system state.
     */
    public static final String STATE_NAME_HEATING_WATER_SYSTEM = "heating_water_system";
    /**
     * digitalSTROM heating water system state as string for off.
     */
    public static final String STATE_HEATING_WATER_SYSTEM_OFF = "off"; // val=0
    /**
     * digitalSTROM heating water system state as string for hot water.
     */
    public static final String STATE_HEATING_WATER_SYSTEM_HOT_WATER = "hot water"; // val=1
    /**
     * digitalSTROM heating water system state as string for cold water.
     */
    public static final String STATE_HEATING_WATER_SYSTEM_COLD_WATER = "cold water"; // val=2

    /**
     * Path to get the current digitalSTROM heating water system state through
     * {@link DsAPI#propertyTreeGetString(String, String)}.
     */
    public static final String GET_HEATING_WATER_SYSTEM_STATE_PATH = "/usr/states/heating_water_system/state";
    /**
     * Path to get the current digitalSTROM heating controller nodes through
     * {@link DsAPI#propertyTreeGetString(String, String)}.
     * Can be used e.g. to check, if the digitalSTROM heating controller app is installed at the digitalSTROM server.
     */
    public static final String GET_HEATING_HEATING_CONTROLLER_CHILDREN_PATH = "/scripts/heating-controller/";

    /**
     * Action for set operation mode at {@link EventNames#HEATING_CONTROL_OPERATION_MODE}.
     */
    public static final String SET_OPERATION_MODE = "setOperationMode";
    /**
     * Action for evaluate real active mode at {@link EventNames#HEATING_CONTROL_OPERATION_MODE}. Will be called after
     * {@link #SET_OPERATION_MODE} or if the configuration of a zone temperature control status has changed.
     */
    public static final String EVALUATE_REAL_ACTIVE_MODE = "evaluateRealActiveMode";

    private String currentHeatingWaterSystemStage;

    private final List<String> echoBox = Collections.synchronizedList(new LinkedList<>());

    /**
     * Creates a new {@link TemperatureControlManager}. The {@link ConnectionManager} is needed. The other fields are
     * only needed, if you want to get automatically informed by status changes through the {@link EventListener} and/or
     * get informed by new configured zones as discovery.
     *
     * @param connectionMananager (must not be null)
     * @param eventListener (can be null)
     * @param discovery (can be null)
     */
    public TemperatureControlManager(ConnectionManager connectionMananager, EventListener eventListener,
            TemperatureControlStatusListener discovery) {
        this(connectionMananager, eventListener, discovery, null);
    }

    /**
     * Same constructor like
     * {@link #TemperatureControlManager(ConnectionManager, EventListener, TemperatureControlStatusListener)}, but it
     * can be set a {@link SystemStateChangeListener}, too.
     *
     * @param connectionMananager (must not be null)
     * @param eventListener (can be null)
     * @param discovery (can be null)
     * @param systemStateChangeListener (can be null)
     * @see #TemperatureControlManager(ConnectionManager, EventListener, TemperatureControlStatusListener)
     */
    public TemperatureControlManager(ConnectionManager connectionMananager, EventListener eventListener,
            TemperatureControlStatusListener discovery, SystemStateChangeListener systemStateChangeListener) {
        this.connectionMananager = connectionMananager;
        this.dSapi = connectionMananager.getDigitalSTROMAPI();
        this.systemStateChangeListener = systemStateChangeListener;
        this.discovery = discovery;
        this.eventListener = eventListener;
        checkZones();
        if (eventListener != null) {
            if (isConfigured) {
                SUPPORTED_EVENTS.add(EventNames.ZONE_SENSOR_VALUE);
                if (systemStateChangeListener != null) {
                    SUPPORTED_EVENTS.add(EventNames.STATE_CHANGED);
                }
            }
            eventListener.addEventHandler(this);
        }
    }

    /**
     * Checks all digitalSTROM zones, if temperature control is configured. If a zone with configured temperature
     * control is found, it will be stored, the flag for {@link #isConfigured()} will be set to true and the discovery
     * will be informed, if a discovery is registered.
     */
    public void checkZones() {
        List<TemperatureControlStatus> temperationControlStatus = dSapi
                .getApartmentTemperatureControlStatus(connectionMananager.getSessionToken());
        if (!temperationControlStatus.isEmpty()) {
            for (TemperatureControlStatus tempConStat : temperationControlStatus) {
                addTemperatureControlStatus(tempConStat);
            }
            if (isConfigured && systemStateChangeListener != null) {
                currentHeatingWaterSystemStage = dSapi.propertyTreeGetString(connectionMananager.getSessionToken(),
                        GET_HEATING_WATER_SYSTEM_STATE_PATH);
            }
        }
    }

    /**
     * Returns true, if the digitalSTROM heating controller app is installed.
     *
     * @param connectionManager (must not be null)
     * @return true, if heating controller app is installed, otherwise false
     */
    public static boolean isHeatingControllerInstallated(ConnectionManager connectionManager) {
        return connectionManager.getDigitalSTROMAPI().propertyTreeGetChildren(connectionManager.getSessionToken(),
                GET_HEATING_HEATING_CONTROLLER_CHILDREN_PATH) != null;
    }

    /**
     * Returns all zone which have temperature controlled configured.
     *
     * @return all temperature controlled zones
     */
    public Collection<TemperatureControlStatus> getTemperatureControlStatusFromAllZones() {
        return temperationControlStatus != null ? this.temperationControlStatus.values() : new LinkedList<>();
    }

    /**
     * Registers a {@link TemperatureControlStatusListener} for a zone, if the temperation control for this zone is
     * configured. It can be also register a {@link TemperatureControlStatusListener} as discovery, if the
     * {@link TemperatureControlStatusListener#getTemperationControlStatusListenrID()} returns
     * {@link TemperatureControlStatusListener#DISCOVERY}.
     *
     * @param temperatureControlStatusListener to register
     */
    public void registerTemperatureControlStatusListener(
            TemperatureControlStatusListener temperatureControlStatusListener) {
        if (temperatureControlStatusListener != null) {
            if (temperatureControlStatusListener.getTemperationControlStatusListenrID()
                    .equals(TemperatureControlStatusListener.DISCOVERY)) {
                logger.debug("discovery is registered");
                this.discovery = temperatureControlStatusListener;
                if (temperationControlStatus != null) {
                    for (TemperatureControlStatus tempConStat : temperationControlStatus.values()) {
                        discovery.configChanged(tempConStat);
                    }
                }
            } else {
                if (zoneTemperationControlListenerMap == null) {
                    zoneTemperationControlListenerMap = new HashMap<>();
                }
                TemperatureControlStatus tempConStat = checkAndGetTemperatureControlStatus(
                        temperatureControlStatusListener.getTemperationControlStatusListenrID());
                if (tempConStat != null) {
                    logger.debug("register listener with id {}",
                            temperatureControlStatusListener.getTemperationControlStatusListenrID());
                    zoneTemperationControlListenerMap.put(
                            temperatureControlStatusListener.getTemperationControlStatusListenrID(),
                            temperatureControlStatusListener);
                    temperatureControlStatusListener.registerTemperatureSensorTransmitter(this);
                }
                temperatureControlStatusListener.configChanged(tempConStat);
            }
        }
    }

    /**
     * Unregisters a {@link TemperatureControlStatusListener}, if it exist.
     *
     * @param temperatureControlStatusListener to unregister
     */
    public void unregisterTemperatureControlStatusListener(
            TemperatureControlStatusListener temperatureControlStatusListener) {
        if (temperatureControlStatusListener != null) {
            if (temperatureControlStatusListener.getTemperationControlStatusListenrID()
                    .equals(TemperatureControlStatusListener.DISCOVERY)) {
                this.discovery = null;
                return;
            }
            if (discovery != null && zoneTemperationControlListenerMap
                    .remove(temperatureControlStatusListener.getTemperationControlStatusListenrID()) != null) {
                discovery.configChanged(temperationControlStatus
                        .get(temperatureControlStatusListener.getTemperationControlStatusListenrID()));
            }
        }
    }

    /**
     * Returns the {@link TemperatureControlStatus} for the given zone, if the temperature control is configured,
     * otherwise it will be returned null.
     *
     * @param zoneID to check
     * @return {@link TemperatureControlStatus} if the temperature control is configured, otherwise null
     */
    public TemperatureControlStatus checkAndGetTemperatureControlStatus(Integer zoneID) {
        TemperatureControlStatus tempConStat = this.temperationControlStatus.get(zoneID);
        if (tempConStat.isNotSetOff()) {
            return tempConStat;
        }
        return null;
    }

    private boolean isEcho(Integer zoneID, SensorEnum sensorType, Float value) {
        return echoBox.remove(zoneID + "-" + sensorType.getSensorType() + "-" + value);
    }

    private void addEcho(Integer zoneID, SensorEnum sensorType, Float value) {
        echoBox.add(zoneID + "-" + sensorType.getSensorType() + "-" + value);
    }

    @Override
    public void handleEvent(EventItem eventItem) {
        try {
            logger.debug("detect event: {}", eventItem.toString());
            if (eventItem.getName().equals(EventNames.ZONE_SENSOR_VALUE)) {
                if (zoneTemperationControlListenerMap != null) {
                    if (SensorEnum.ROOM_TEMPERATURE_SET_POINT.getSensorType().toString()
                            .equals(eventItem.getProperties().get(EventResponseEnum.SENSOR_TYPE))) {
                        Integer zoneID = Integer
                                .parseInt(eventItem.getSource().getOrDefault(EventResponseEnum.ZONEID, ""));
                        if (zoneTemperationControlListenerMap.get(zoneID) != null) {
                            Float newValue = Float.parseFloat(
                                    eventItem.getProperties().getOrDefault(EventResponseEnum.SENSOR_VALUE_FLOAT, ""));
                            if (!isEcho(zoneID, SensorEnum.ROOM_TEMPERATURE_CONTROL_VARIABLE, newValue)) {
                                zoneTemperationControlListenerMap.get(zoneID).onTargetTemperatureChanged(newValue);
                            }
                        }
                    }
                    if (SensorEnum.ROOM_TEMPERATURE_CONTROL_VARIABLE.getSensorType().toString()
                            .equals(eventItem.getProperties().get(EventResponseEnum.SENSOR_TYPE))) {
                        Integer zoneID = Integer
                                .parseInt(eventItem.getSource().getOrDefault(EventResponseEnum.ZONEID, ""));
                        if (zoneTemperationControlListenerMap.get(zoneID) != null) {
                            Float newValue = Float.parseFloat(
                                    eventItem.getProperties().getOrDefault(EventResponseEnum.SENSOR_VALUE_FLOAT, ""));
                            if (!isEcho(zoneID, SensorEnum.ROOM_TEMPERATURE_CONTROL_VARIABLE, newValue)) {
                                zoneTemperationControlListenerMap.get(zoneID)
                                        .onControlValueChanged(newValue.intValue());
                            }
                        }
                    }
                }
            }

            if (eventItem.getName().equals(EventNames.HEATING_CONTROL_OPERATION_MODE)) {
                if (EVALUATE_REAL_ACTIVE_MODE.equals(eventItem.getProperties().get(EventResponseEnum.ACTIONS))) {
                    Integer zoneID = Integer
                            .parseInt(eventItem.getProperties().getOrDefault(EventResponseEnum.ZONEID, ""));
                    TemperatureControlStatus temperationControlStatus = dSapi
                            .getZoneTemperatureControlStatus(connectionMananager.getSessionToken(), zoneID, null);
                    if (temperationControlStatus != null) {
                        addTemperatureControlStatus(temperationControlStatus);
                    }
                }
            }

            if (eventItem.getName().equals(EventNames.STATE_CHANGED)) {
                if (STATE_NAME_HEATING_WATER_SYSTEM
                        .equals(eventItem.getProperties().get(EventResponseEnum.STATE_NAME))) {
                    currentHeatingWaterSystemStage = eventItem.getProperties().get(EventResponseEnum.STATE);
                    logger.debug("heating water system state changed to {}", currentHeatingWaterSystemStage);
                    if (systemStateChangeListener != null) {
                        systemStateChangeListener.onSystemStateChanged(STATE_NAME_HEATING_WATER_SYSTEM,
                                currentHeatingWaterSystemStage);
                    }
                }
            }
        } catch (NumberFormatException e) {
            logger.debug("Unexpected missing or invalid number while handling event", e);
        }
    }

    private void addTemperatureControlStatus(TemperatureControlStatus temperationControlStatus) {
        if (temperationControlStatus.isNotSetOff()) {
            if (this.temperationControlStatus == null) {
                this.temperationControlStatus = new HashMap<>();
            }
            if (this.temperationControlStatus.get(temperationControlStatus.getZoneID()) == null && discovery != null) {
                discovery.configChanged(temperationControlStatus);
                if (!isConfigured) {
                    isConfigured = true;
                }
            }
            this.temperationControlStatus.put(temperationControlStatus.getZoneID(), temperationControlStatus);
            if (zoneTemperationControlListenerMap != null
                    && zoneTemperationControlListenerMap.get(temperationControlStatus.getZoneID()) != null) {
                zoneTemperationControlListenerMap.get(temperationControlStatus.getZoneID())
                        .configChanged(temperationControlStatus);
            }
        }
    }

    @Override
    public List<String> getSupportedEvents() {
        return SUPPORTED_EVENTS;
    }

    @Override
    public boolean supportsEvent(String eventName) {
        return SUPPORTED_EVENTS.contains(eventName);
    }

    @Override
    public String getUID() {
        return getClass().getSimpleName();
    }

    @Override
    public void setEventListener(EventListener eventListener) {
        eventListener.addEventHandler(this);
    }

    @Override
    public void unsetEventListener(EventListener eventListener) {
        eventListener.removeEventHandler(this);
    }

    @Override
    public boolean pushTargetTemperature(Integer zoneID, Float newValue) {
        if (checkAndGetTemperatureControlStatus(zoneID) != null) {
            if (dSapi.pushZoneSensorValue(connectionMananager.getSessionToken(), zoneID, null, (short) 0, null,
                    newValue, SensorEnum.ROOM_TEMPERATURE_SET_POINT)) {
                addEcho(zoneID, SensorEnum.ROOM_TEMPERATURE_SET_POINT, newValue);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pushControlValue(Integer zoneID, Float newValue) {
        if (checkAndGetTemperatureControlStatus(zoneID) != null) {
            if (dSapi.pushZoneSensorValue(connectionMananager.getSessionToken(), zoneID, null,
                    ApplicationGroup.TEMPERATURE_CONTROL.getId(), null, newValue,
                    SensorEnum.ROOM_TEMPERATURE_CONTROL_VARIABLE)) {
                addEcho(zoneID, SensorEnum.ROOM_TEMPERATURE_CONTROL_VARIABLE, newValue);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true, if minimum one zone has temperature control configured.
     *
     * @return true, if minimum one zone has temperature control configured, otherwise false
     */
    public boolean isConfigured() {
        return isConfigured;
    }

    /**
     * Returns the current heating water system state, if a {@link SystemStateChangeListener} is registered, otherwise
     * null.
     *
     * @return the current heating water system state or null, if no {@link SystemStateChangeListener}
     */
    public String getHeatingWaterSystemState() {
        return currentHeatingWaterSystemStage;
    }

    /**
     * Registers the given {@link SystemStateChangeListener}, which will be informed about heating system water state
     * changes.
     *
     * @param systemStateChangeListener to register
     */
    public void registerSystemStateChangeListener(SystemStateChangeListener systemStateChangeListener) {
        if (eventListener != null) {
            SUPPORTED_EVENTS.add(EventNames.STATE_CHANGED);
            eventListener.addSubscribe(EventNames.STATE_CHANGED);
        }
        this.systemStateChangeListener = systemStateChangeListener;
    }

    /**
     * Unregisters a registered {@link SystemStateChangeListener}.
     */
    public void unregisterSystemStateChangeListener() {
        this.systemStateChangeListener = null;
    }
}
