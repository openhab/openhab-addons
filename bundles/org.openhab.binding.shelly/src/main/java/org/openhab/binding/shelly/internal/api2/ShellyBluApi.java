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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.getBluServiceName;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorSleepMode;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorTmp;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorAccel;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorBat;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorHum;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorLux;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorState;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBluHandler;
import org.openhab.binding.shelly.internal.handler.ShellyComponents;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyBluApi} implementsBLU interface
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBluApi extends Shelly2ApiRpc {
    private final Logger logger = LoggerFactory.getLogger(ShellyBluApi.class);
    private boolean connected = false; // true = BLU devices has connected
    private ShellySettingsStatus deviceStatus = new ShellySettingsStatus();
    private int lastPid = -1;
    private static final int PID_CYCLE_TRESHHOLD = 50;

    /**
     * Regular constructor - called by Thing handler
     *
     * @param thingName Symbolic thing name
     * @param thingTable Table of known things (build at runtime)
     * @param thing Thing Handler (ThingHandlerInterface)
     */
    public ShellyBluApi(String thingName, ShellyThingTable thingTable, ShellyThingInterface thing) {
        super(thingName, thingTable, thing);

        ShellyDeviceProfile profile = thing.getProfile();
        ThingTypeUID uid = thing.getThing().getThingTypeUID();
        profile.initializeInputs(uid, SHELLY_BTNT_MOMENTARY);
        deviceStatus = profile.status;
    }

    @Override
    public void initialize() throws ShellyApiException {
        if (!initialized) {
            initialized = true;
            connected = false;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setConfig(String thingName, ShellyThingConfiguration config) {
        this.thingName = thingName;
        this.config = config;
    }

    @Override
    public ShellySettingsDevice getDeviceInfo() throws ShellyApiException {
        ShellySettingsDevice info = new ShellySettingsDevice();
        info.hostname = !config.serviceName.isEmpty() ? config.serviceName : "";
        info.fw = "";
        info.type = "BLU";
        info.mac = config.deviceAddress;
        info.auth = false;
        info.gen = 2;
        return info;
    }

    @Override
    public ShellyDeviceProfile getDeviceProfile(ThingTypeUID thingTypeUID, @Nullable ShellySettingsDevice devInfo)
            throws ShellyApiException {
        ShellyDeviceProfile profile = thing != null ? getProfile() : new ShellyDeviceProfile();

        if (devInfo != null) {
            profile.device = devInfo;
        }
        profile.isBlu = true;
        profile.alwaysOn = false;
        profile.settingsJson = "{}";
        profile.thingName = thingName;
        profile.name = getString(profile.settings.name);
        if (profile.gateway.isEmpty()) {
            profile.gateway = getThing().getProperty(PROPERTY_GW_DEVICE);
        }

        profile.device = getDeviceInfo();
        if (config.serviceName.isEmpty()) {
            config.serviceName = getString(profile.device.hostname);
        }

        // for now we have no API to get this information
        profile.fwDate = profile.fwVersion = profile.status.update.oldVersion = "";
        profile.status.hasUpdate = profile.status.update.hasUpdate = false;

        if (profile.hasBattery) {
            profile.settings.sleepMode = new ShellySensorSleepMode();
            profile.settings.sleepMode.unit = "m";
            profile.settings.sleepMode.period = 720;
        }

        profile.initialized = true;
        return profile;
    }

    @Override
    public ShellySettingsStatus getStatus() throws ShellyApiException {
        if (!connected) {
            throw new ShellyApiException("Thing is not yet initialized -> status not available");
        }
        return deviceStatus;
    }

    @Override
    public ShellyStatusSensor getSensorStatus() throws ShellyApiException {
        if (!connected) {
            throw new ShellyApiException("Thing is not yet initialized -> sensor data not available");
        }

        return sensorData;
    }

    @Override
    public void onNotifyEvent(Shelly2RpcNotifyEvent message) {
        logger.trace("{}: ShellyEvent received: {}", thingName, gson.toJson(message));

        ShellyBluHandler t = (ShellyBluHandler) thing;
        if (t == null) {
            logger.debug("{}: Thing is not initialized -> ignore event", thingName);
            return;
        }

        try {
            ShellyDeviceProfile profile = getProfile();

            t.incProtMessages();

            if (!connected) {
                connected = true;
                t.setThingOnline();
            } else {
                t.restartWatchdog();
            }

            for (Shelly2NotifyEvent e : message.params.events) {
                String event = getString(e.event);
                if (event.startsWith(SHELLY2_EVENT_BLUPREFIX)) {
                    logger.debug("{}: BLU event {} received from address {}, pid={}", thingName, event,
                            getString(e.blu.addr), getInteger(e.blu.pid));
                    if (e.blu.pid != null) {
                        int pid = e.blu.pid;
                        if (lastPid != -1 && pid < (lastPid - PID_CYCLE_TRESHHOLD)) {
                            logger.debug(
                                    "{}: Received pid {} is so low that a new cycle has probably begun since lastPID={}",
                                    thingName, pid, lastPid);
                        } else if (pid <= lastPid) {
                            logger.debug("{}: Duplicate packet for pid {} received, ignore", thingName, pid);
                            break;
                        }
                        lastPid = pid;
                    }
                    getThing().getProfile().gateway = message.src;
                }

                switch (event) {
                    case SHELLY2_EVENT_BLUSCAN:
                        if (e.blu == null || e.blu.addr == null) {
                            logger.debug("{}: Inconsistent BLU scan result ignored: {}", thingName,
                                    gson.toJson(message));
                            break;
                        }
                        if (e.blu.name != null) {
                            profile.settings.name = getBluServiceName(e.blu.name, e.blu.addr);
                            logger.debug("{}: BLU Device {} discovered, mapped to serviceName {}", thingName,
                                    e.blu.name, profile.settings.name);
                        }
                        break;
                    case SHELLY2_EVENT_BLUDATA:
                        if (e.blu == null || e.blu.addr == null || e.blu.pid == null) {
                            logger.debug("{}: Inconsistent BLU packet ignored: {}", thingName, gson.toJson(message));
                            break;
                        }

                        if (e.blu.battery != null) {
                            if (sensorData.bat == null) {
                                sensorData.bat = new ShellySensorBat();
                            }
                            sensorData.bat.value = (double) e.blu.battery;
                        }
                        if (e.blu.rssi != null) {
                            deviceStatus.wifiSta.rssi = e.blu.rssi;
                        }
                        if (e.blu.windowState != null) {
                            if (sensorData.sensor == null) {
                                sensorData.sensor = new ShellySensorState();
                            }
                            sensorData.sensor.isValid = true;
                            sensorData.sensor.state = e.blu.windowState == 1 ? SHELLY_API_DWSTATE_OPEN
                                    : SHELLY_API_DWSTATE_CLOSE;
                        }
                        if (e.blu.illuminance != null) {
                            if (sensorData.lux == null) {
                                sensorData.lux = new ShellySensorLux();
                            }
                            sensorData.lux.isValid = true;
                            sensorData.lux.value = (double) e.blu.illuminance;
                        }
                        if (e.blu.temperatures != null) {
                            if (e.blu.temperatures.length == 1) {
                                if (sensorData.tmp == null) {
                                    sensorData.tmp = new ShellySensorTmp();
                                }
                                sensorData.tmp.units = SHELLY_TEMP_CELSIUS;
                                sensorData.tmp.isValid = true;
                                sensorData.tmp.tC = e.blu.temperatures[0];
                            } else {
                                // BLU TRV reports current temp and target temp
                                // However, we don't support BLU TRV yet, so ignore
                            }
                        }
                        if (e.blu.humidity != null) {
                            if (sensorData.hum == null) {
                                sensorData.hum = new ShellySensorHum();
                            }
                            sensorData.hum.value = e.blu.humidity;
                        }
                        if (e.blu.rotation != null) {
                            if (sensorData.accel == null) {
                                sensorData.accel = new ShellySensorAccel();
                            }
                            sensorData.accel.tilt = e.blu.rotation.intValue();
                        }
                        if (e.blu.motionState != null) {
                            sensorData.motion = e.blu.motionState == 1;
                        }
                        if (e.blu.firmware != null) {
                            int digit4 = (int) (e.blu.firmware & 0x000000FF);
                            int digit3 = (int) (e.blu.firmware & 0x0000FF00) >> 8;
                            int digit2 = (int) (e.blu.firmware & 0x00FF0000) >> 16;
                            int digit1 = (int) (e.blu.firmware & 0xFF000000) >> 24;
                            profile.fwVersion = digit1 > 0 ? //
                                    digit1 + "." + digit2 + "." + digit3 + "." + digit4
                                    : digit2 + "," + digit3 + "." + digit4;
                            logger.debug("{}: Detected firmware version: {}", thingName, profile.fwVersion);
                        }
                        if (e.blu.buttons != null) {
                            logger.trace("{}: Shelly BLU button events received: {}", thingName,
                                    gson.toJson(e.blu.buttons));
                            for (int bttnIdx = 0; bttnIdx < e.blu.buttons.length; bttnIdx++) {
                                if (e.blu.buttons[bttnIdx] != 0) {
                                    ShellyInputState input = deviceStatus.inputs.get(bttnIdx);
                                    input.event = MAP_BLU_INPUT_EVENT_TYPE.getOrDefault(e.blu.buttons[bttnIdx], "");

                                    String group = getProfile().getInputGroup(bttnIdx);
                                    String suffix = profile.getInputSuffix(bttnIdx);
                                    // ignore HOLDING events for counter and trigger
                                    if (!SHELLY_BTNEVENT_HOLDING.equalsIgnoreCase(input.event)) {
                                        logger.debug("{}: update to {}, pid={}", message.src, input.event, e.blu.pid);
                                        t.updateChannel(group, CHANNEL_STATUS_EVENTTYPE + suffix,
                                                getStringType(input.event));
                                        input.eventCount++;
                                        t.updateChannel(group, CHANNEL_STATUS_EVENTCOUNT + suffix,
                                                getDecimal(input.eventCount));
                                        t.triggerButton(profile.getInputGroup(bttnIdx), bttnIdx, input.event);
                                    } else {
                                        logger.debug("{}: ignore H, pid={}", message.src, e.blu.pid);
                                    }
                                    deviceStatus.inputs.set(bttnIdx, input);
                                }
                            }
                        }

                        ShellyComponents.updateDeviceStatus(t, deviceStatus);
                        ShellyComponents.updateSensors(getThing(), deviceStatus);
                        break;
                    default:
                        super.onNotifyEvent(message);
                }
            }
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to process event", thingName, e);
            t.incProtErrors();
        }
    }
}
