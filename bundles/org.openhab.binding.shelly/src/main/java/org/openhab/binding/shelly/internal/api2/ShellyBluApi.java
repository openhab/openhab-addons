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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorSleepMode;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorTmp;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorAccel;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorBat;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorLux;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorState;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBluSensorHandler;
import org.openhab.binding.shelly.internal.handler.ShellyComponents;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyBluApi} implementsBLU interface
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyBluApi extends Shelly2ApiRpc {
    private static final Logger logger = LoggerFactory.getLogger(ShellyBluApi.class);
    private boolean connected = false; // true = BLU devices has connected
    private ShellySettingsStatus deviceStatus = new ShellySettingsStatus();
    private int lastPid = -1;

    private static final Map<String, String> MAP_INPUT_EVENT_TYPE = new HashMap<>();
    static {
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_1PUSH, SHELLY_BTNEVENT_1SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_2PUSH, SHELLY_BTNEVENT_2SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_3PUSH, SHELLY_BTNEVENT_3SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_LPUSH, SHELLY_BTNEVENT_LONGPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_LSPUSH, SHELLY_BTNEVENT_LONGSHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_SLPUSH, SHELLY_BTNEVENT_SHORTLONGPUSH);
        MAP_INPUT_EVENT_TYPE.put("1", SHELLY_BTNEVENT_1SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put("2", SHELLY_BTNEVENT_2SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put("3", SHELLY_BTNEVENT_3SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put("4", SHELLY_BTNEVENT_LONGPUSH);
    }

    /**
     * Regular constructor - called by Thing handler
     *
     * @param thingName Symbolic thing name
     * @param thing Thing Handler (ThingHandlerInterface)
     */
    public ShellyBluApi(String thingName, ShellyThingTable thingTable, ShellyThingInterface thing) {
        super(thingName, thingTable, thing);

        ShellyInputState input = new ShellyInputState();
        deviceStatus.inputs = new ArrayList<>();
        input.input = 0;
        input.event = "";
        input.eventCount = 0;
        deviceStatus.inputs.add(input);
    }

    @Override
    public void initialize() throws ShellyApiException {
        if (!initialized) {
            initialized = true;
            connected = false;
        } else {
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
    public ShellyDeviceProfile getDeviceProfile(String thingType, @Nullable ShellySettingsDevice devInfo)
            throws ShellyApiException {
        ShellyDeviceProfile profile = thing != null ? getProfile() : new ShellyDeviceProfile();

        if (devInfo != null) {
            profile.device = devInfo;
        }
        profile.isBlu = true;
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

        if (profile.isButton) {
            ShellySettingsInput settings = new ShellySettingsInput();
            profile.numInputs = 1;
            settings.btnType = SHELLY_BTNT_MOMENTARY;

            if (profile.settings.inputs != null) {
                profile.settings.inputs.set(0, settings);
            } else {
                profile.settings.inputs = new ArrayList<>();
                profile.settings.inputs.add(settings);
            }
            profile.status = deviceStatus;
        }

        if (!connected) {
            throw new ShellyApiException("BLU Device not yet connected");
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

        boolean updated = false;
        ShellyBluSensorHandler t = (ShellyBluSensorHandler) thing;
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
                logger.debug("{}: BluEvent received: {}", thingName, gson.toJson(message));
                String event = getString(e.event);
                if (event.startsWith(SHELLY2_EVENT_BLUPREFIX)) {
                    logger.debug("{}: BLU event {} received from address {}, pid={}", thingName, event,
                            getString(e.data.addr), getInteger(e.data.pid));
                    if (e.data.pid != null) {
                        int pid = e.data.pid;
                        if (pid == lastPid) {
                            logger.debug("{}: Duplicate packet for PID={} received, ignore", thingName, pid);
                            break;
                        }
                        lastPid = pid;
                    }
                    getThing().getProfile().gateway = message.src;
                }

                switch (event) {
                    case SHELLY2_EVENT_BLUSCAN:
                        if (e.data == null || e.data.addr == null) {
                            logger.debug("{}: Inconsistent BLU scan result ignored: {}", thingName,
                                    gson.toJson(message));
                            break;
                        }
                        logger.debug("{}: BLU Device discovered", thingName);
                        if (e.data.name != null) {
                            profile.settings.name = ShellyDeviceProfile.buildBluServiceName(e.data.name, e.data.addr);
                        }
                        break;
                    case SHELLY2_EVENT_BLUDATA:
                        if (e.data == null || e.data.addr == null || e.data.pid == null) {
                            logger.debug("{}: Inconsistent BLU packet ignored: {}", thingName, gson.toJson(message));
                            break;
                        }

                        if (e.data.battery != null) {
                            if (sensorData.bat == null) {
                                sensorData.bat = new ShellySensorBat();
                            }
                            sensorData.bat.value = (double) e.data.battery;
                        }
                        if (e.data.rssi != null) {
                            deviceStatus.wifiSta.rssi = e.data.rssi;
                        }
                        if (e.data.windowState != null) {
                            if (sensorData.sensor == null) {
                                sensorData.sensor = new ShellySensorState();
                            }
                            sensorData.sensor.isValid = true;
                            sensorData.sensor.state = e.data.windowState == 1 ? SHELLY_API_DWSTATE_OPEN
                                    : SHELLY_API_DWSTATE_CLOSE;
                        }
                        if (e.data.illuminance != null) {
                            if (sensorData.lux == null) {
                                sensorData.lux = new ShellySensorLux();
                            }
                            sensorData.lux.isValid = true;
                            sensorData.lux.value = (double) e.data.illuminance;
                        }
                        if (e.data.temperature != null) {
                            if (sensorData.tmp == null) {
                                sensorData.tmp = new ShellySensorTmp();
                            }
                            sensorData.tmp.tC = e.data.temperature;
                            sensorData.tmp.isValid = true;
                        }
                        if (e.data.rotation != null) {
                            if (sensorData.accel == null) {
                                sensorData.accel = new ShellySensorAccel();
                            }
                            sensorData.accel.tilt = e.data.rotation.intValue();
                        }
                        if (e.data.motionState != null) {
                            sensorData.motion = e.data.motionState == 1;
                        }

                        if (e.data.buttonEvent != null) {
                            ShellyInputState input = deviceStatus.inputs != null ? deviceStatus.inputs.get(0)
                                    : new ShellyInputState();
                            input.event = mapValue(MAP_INPUT_EVENT_TYPE, e.data.buttonEvent + "");
                            input.eventCount++;
                            deviceStatus.inputs.set(0, input);
                            // sensorData.inputs.set(0, input);

                            String group = getProfile().getInputGroup(0);
                            String suffix = profile.getInputSuffix(0);
                            t.updateChannel(group, CHANNEL_STATUS_EVENTTYPE + suffix, getStringType(input.event));
                            t.updateChannel(group, CHANNEL_STATUS_EVENTCOUNT + suffix, getDecimal(input.eventCount));
                            t.triggerButton(profile.getInputGroup(0), 0, input.event);
                        }
                        updated |= ShellyComponents.updateDeviceStatus(t, deviceStatus);
                        updated |= ShellyComponents.updateSensors(getThing(), deviceStatus);
                        break;
                    default:
                        super.onNotifyEvent(message);
                }
            }
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to process event", thingName, e);
            t.incProtErrors();
        }
        if (updated) {
        }
    }
}
