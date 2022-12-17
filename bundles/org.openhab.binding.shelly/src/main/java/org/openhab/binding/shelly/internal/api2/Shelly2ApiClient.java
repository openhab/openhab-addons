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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_INPUT;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpClient;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyFavPos;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorTmp;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRoller;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortStatusRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtAnalogInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtDigitalInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtHumidity;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtTemperature;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtTemperature.ShellyShortTemp;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtVoltage;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorBat;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorHum;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2AuthRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2AuthResponse;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigCover;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigInput;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigSwitch;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2CoverStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusHumidity;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusTempId;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2InputStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RelayStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.openhab.binding.shelly.internal.handler.ShellyComponents;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Shelly2ApiClient} Low level part of the RPC API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2ApiClient extends ShellyHttpClient {
    private final Logger logger = LoggerFactory.getLogger(Shelly2ApiClient.class);
    protected final Random random = new Random();
    protected final ShellyStatusRelay relayStatus = new ShellyStatusRelay();
    protected final ShellyStatusSensor sensorData = new ShellyStatusSensor();
    protected final ArrayList<ShellyRollerStatus> rollerStatus = new ArrayList<>();
    protected @Nullable ShellyThingInterface thing;
    protected @Nullable Shelly2AuthRequest authReq;

    public Shelly2ApiClient(String thingName, ShellyThingInterface thing) {
        super(thingName, thing);
        this.thing = thing;
    }

    public Shelly2ApiClient(String thingName, ShellyThingConfiguration config, HttpClient httpClient) {
        super(thingName, config, httpClient);
    }

    protected static final Map<String, String> MAP_INMODE_BTNTYPE = new HashMap<>();
    static {
        MAP_INMODE_BTNTYPE.put(SHELLY2_BTNT_MOMENTARY, SHELLY_BTNT_MOMENTARY);
        MAP_INMODE_BTNTYPE.put(SHELLY2_BTNT_FLIP, SHELLY_BTNT_TOGGLE);
        MAP_INMODE_BTNTYPE.put(SHELLY2_BTNT_FOLLOW, SHELLY_BTNT_EDGE);
        MAP_INMODE_BTNTYPE.put(SHELLY2_BTNT_DETACHED, SHELLY_BTNT_MOMENTARY);
    }

    protected static final Map<String, String> MAP_INPUT_EVENT_TYPE = new HashMap<>();
    static {
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_1PUSH, SHELLY_BTNEVENT_1SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_2PUSH, SHELLY_BTNEVENT_2SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_3PUSH, SHELLY_BTNEVENT_3SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_LPUSH, SHELLY_BTNEVENT_LONGPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_LSPUSH, SHELLY_BTNEVENT_LONGSHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_SLPUSH, SHELLY_BTNEVENT_SHORTLONGPUSH);
    }

    protected static final Map<String, String> MAP_INPUT_EVENT_ID = new HashMap<>();
    static {
        MAP_INPUT_EVENT_ID.put(SHELLY2_EVENT_BTNUP, SHELLY_EVENT_BTN_OFF);
        MAP_INPUT_EVENT_ID.put(SHELLY2_EVENT_BTNDOWN, SHELLY_EVENT_BTN_ON);
        MAP_INPUT_EVENT_ID.put(SHELLY2_EVENT_1PUSH, SHELLY_EVENT_SHORTPUSH);
        MAP_INPUT_EVENT_ID.put(SHELLY2_EVENT_2PUSH, SHELLY_EVENT_DOUBLE_SHORTPUSH);
        MAP_INPUT_EVENT_ID.put(SHELLY2_EVENT_3PUSH, SHELLY_EVENT_TRIPLE_SHORTPUSH);
        MAP_INPUT_EVENT_ID.put(SHELLY2_EVENT_LPUSH, SHELLY_EVENT_LONGPUSH);
        MAP_INPUT_EVENT_ID.put(SHELLY2_EVENT_LSPUSH, SHELLY_EVENT_LONG_SHORTPUSH);
        MAP_INPUT_EVENT_ID.put(SHELLY2_EVENT_SLPUSH, SHELLY_EVENT_SHORT_LONGTPUSH);
    }

    protected static final Map<String, String> MAP_INPUT_MODE = new HashMap<>();
    static {
        MAP_INPUT_MODE.put(SHELLY2_RMODE_SINGLE, SHELLY_INP_MODE_ONEBUTTON);
        MAP_INPUT_MODE.put(SHELLY2_RMODE_DUAL, SHELLY_INP_MODE_OPENCLOSE);
        MAP_INPUT_MODE.put(SHELLY2_RMODE_DETACHED, SHELLY_INP_MODE_ONEBUTTON);
    }

    protected static final Map<String, String> MAP_ROLLER_STATE = new HashMap<>();
    static {
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_OPEN, SHELLY_RSTATE_OPEN);
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_CLOSED, SHELLY_RSTATE_CLOSE);
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_OPENING, SHELLY2_RSTATE_OPENING); // Gen2-only
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_CLOSING, SHELLY2_RSTATE_CLOSING); // Gen2-only
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_STOPPED, SHELLY_RSTATE_STOP);
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_CALIB, SHELLY2_RSTATE_CALIB); // Gen2-only
    }

    protected @Nullable ArrayList<@Nullable ShellySettingsRelay> fillRelaySettings(ShellyDeviceProfile profile,
            Shelly2GetConfigResult dc) {
        if (dc.switch0 == null) {
            return null;
        }
        ArrayList<@Nullable ShellySettingsRelay> relays = new ArrayList<>();
        addRelaySettings(relays, dc.switch0);
        addRelaySettings(relays, dc.switch1);
        addRelaySettings(relays, dc.switch2);
        addRelaySettings(relays, dc.switch3);
        return relays;
    }

    private void addRelaySettings(ArrayList<@Nullable ShellySettingsRelay> relays,
            @Nullable Shelly2DevConfigSwitch cs) {
        if (cs == null) {
            return;
        }

        ShellySettingsRelay rsettings = new ShellySettingsRelay();
        rsettings.name = cs.name;
        rsettings.ison = false;
        rsettings.autoOn = getBool(cs.autoOn) ? cs.autoOnDelay : 0;
        rsettings.autoOff = getBool(cs.autoOff) ? cs.autoOffDelay : 0;
        rsettings.hasTimer = false;
        rsettings.btnType = mapValue(MAP_INMODE_BTNTYPE, getString(cs.mode).toLowerCase());
        relays.add(rsettings);
    }

    protected boolean fillDeviceStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult result,
            boolean channelUpdate) throws ShellyApiException {
        boolean updated = false;

        updated |= updateInputStatus(status, result, channelUpdate);
        updated |= updateRelayStatus(status, result.switch0, channelUpdate);
        updated |= updateRelayStatus(status, result.switch1, channelUpdate);
        updated |= updateRelayStatus(status, result.switch2, channelUpdate);
        updated |= updateRelayStatus(status, result.switch3, channelUpdate);
        updated |= updateRollerStatus(status, result.cover0, channelUpdate);
        if (channelUpdate) {
            updated |= ShellyComponents.updateMeters(getThing(), status);
        }

        updateHumidityStatus(sensorData, result.humidity0);
        updateTemperatureStatus(sensorData, result.temperature0);
        updateBatteryStatus(sensorData, result.devicepower0);
        updateAddonStatus(status, result);
        updated |= ShellyComponents.updateSensors(getThing(), status);
        return updated;
    }

    private boolean updateRelayStatus(ShellySettingsStatus status, @Nullable Shelly2RelayStatus rs,
            boolean channelUpdate) throws ShellyApiException {
        if (rs == null) {
            return false;
        }
        ShellyDeviceProfile profile = getProfile();
        if (rs.id >= profile.numRelays) {
            throw new IllegalArgumentException("Update for invalid relay index");
        }

        ShellySettingsRelay rstatus = status.relays.get(rs.id);
        ShellyShortStatusRelay sr = relayStatus.relays.get(rs.id);
        sr.isValid = rstatus.isValid = true;
        sr.name = rstatus.name = status.name;
        if (rs.output != null) {
            sr.ison = rstatus.ison = getBool(rs.output);
        }
        if (getDouble(rs.timerStartetAt) > 0) {
            int duration = (int) (now() - rs.timerStartetAt);
            sr.timerRemaining = duration;
        }
        if (rs.temperature != null) {
            status.tmp.isValid = true;
            status.tmp.tC = rs.temperature.tC;
            status.tmp.tF = rs.temperature.tF;
            status.tmp.units = "C";
            sr.temperature = getDouble(rs.temperature.tC);
            if (status.temperature == null || getDouble(rs.temperature.tC) > status.temperature) {
                status.temperature = sr.temperature;
            }
        } else {
            status.tmp.isValid = false;
        }
        if (rs.voltage != null) {
            if (status.voltage == null || rs.voltage > status.voltage) {
                status.voltage = rs.voltage;
            }
        }
        if (rs.errors != null) {
            for (String error : rs.errors) {
                sr.overpower = rstatus.overpower = SHELLY2_ERROR_OVERPOWER.equals(error);
                status.overload = SHELLY2_ERROR_OVERVOLTAGE.equals(error);
                status.overtemperature = SHELLY2_ERROR_OVERTEMP.equals(error);
            }
            sr.overtemperature = status.overtemperature;
        }

        ShellySettingsMeter sm = new ShellySettingsMeter();
        ShellySettingsEMeter emeter = status.emeters.get(rs.id);
        sm.isValid = emeter.isValid = true;
        if (rs.apower != null) {
            sm.power = emeter.power = rs.apower;
        }
        if (rs.aenergy != null) {
            sm.total = emeter.total = rs.aenergy.total;
            sm.counters = rs.aenergy.byMinute;
            sm.timestamp = rs.aenergy.minuteTs;
        }
        if (rs.voltage != null) {
            emeter.voltage = rs.voltage;
        }
        if (rs.current != null) {
            emeter.current = rs.current;
        }
        if (rs.pf != null) {
            emeter.pf = rs.pf;
        }

        // Update internal structures
        status.relays.set(rs.id, rstatus);
        status.meters.set(rs.id, sm);
        status.emeters.set(rs.id, emeter);
        relayStatus.relays.set(rs.id, sr);
        relayStatus.meters.set(rs.id, sm);

        return channelUpdate ? ShellyComponents.updateRelay((ShellyBaseHandler) getThing(), status, rs.id) : false;
    }

    protected @Nullable ArrayList<@Nullable ShellySettingsRoller> fillRollerSettings(ShellyDeviceProfile profile,
            Shelly2GetConfigResult dc) {
        if (dc.cover0 == null) {
            return null;
        }

        ArrayList<@Nullable ShellySettingsRoller> rollers = new ArrayList<>();

        addRollerSettings(rollers, dc.cover0);
        fillRollerFavorites(profile, dc);
        return rollers;
    }

    private void addRollerSettings(ArrayList<@Nullable ShellySettingsRoller> rollers,
            @Nullable Shelly2DevConfigCover coverConfig) {
        if (coverConfig == null) {
            return;
        }

        ShellySettingsRoller settings = new ShellySettingsRoller();
        settings.isValid = true;
        settings.defaultState = coverConfig.initialState;
        settings.inputMode = mapValue(MAP_INPUT_MODE, coverConfig.inMode);
        settings.btnReverse = getBool(coverConfig.invertDirections) ? 1 : 0;
        settings.swapInputs = coverConfig.swapInputs;
        settings.maxtime = 0.0; // n/a
        settings.maxtimeOpen = coverConfig.maxtimeOpen;
        settings.maxtimeClose = coverConfig.maxtimeClose;
        if (coverConfig.safetySwitch != null) {
            settings.safetySwitch = coverConfig.safetySwitch.enable;
            settings.safetyAction = coverConfig.safetySwitch.action;
        }
        if (coverConfig.obstructionDetection != null) {
            settings.obstacleAction = coverConfig.obstructionDetection.action;
            settings.obstacleDelay = coverConfig.obstructionDetection.holdoff.intValue();
            settings.obstaclePower = coverConfig.obstructionDetection.powerThr;
        }
        rollers.add(settings);
    }

    private void fillRollerFavorites(ShellyDeviceProfile profile, Shelly2GetConfigResult dc) {
        if (dc.sys.uiData.cover != null) {
            String[] favorites = dc.sys.uiData.cover.split(",");
            profile.settings.favorites = new ArrayList<>();
            for (int i = 0; i < favorites.length; i++) {
                ShellyFavPos fav = new ShellyFavPos();
                fav.pos = Integer.parseInt(favorites[i]);
                fav.name = fav.pos + "%";
                profile.settings.favorites.add(fav);
            }
            profile.settings.favoritesEnabled = profile.settings.favorites.size() > 0;
            logger.debug("{}: Roller Favorites loaded: {}", thingName,
                    profile.settings.favoritesEnabled ? profile.settings.favorites.size() : "none");
        }
    }

    private boolean updateRollerStatus(ShellySettingsStatus status, @Nullable Shelly2CoverStatus cs,
            boolean updateChannels) throws ShellyApiException {
        if (cs == null) {
            return false;
        }

        ShellyRollerStatus rs = status.rollers.get(cs.id);
        ShellySettingsMeter sm = status.meters.get(cs.id);
        ShellySettingsEMeter emeter = status.emeters.get(cs.id);
        rs.isValid = sm.isValid = emeter.isValid = true;
        if (cs.state != null) {
            if (!getString(rs.state).equals(cs.state)) {
                logger.debug("{}: Roller status changed from {} to {}, updateChannels={}", thingName, rs.state,
                        mapValue(MAP_ROLLER_STATE, cs.state), updateChannels);
            }
            rs.state = mapValue(MAP_ROLLER_STATE, cs.state);
            rs.calibrating = SHELLY2_RSTATE_CALIB.equals(cs.state);
        }
        if (cs.currentPos != null) {
            rs.currentPos = cs.currentPos;
        }
        if (cs.moveStartedAt != null) {
            rs.duration = (int) (now() - cs.moveStartedAt.longValue());
        }
        if (cs.temperature != null && cs.temperature.tC > getDouble(status.temperature)) {
            status.temperature = status.tmp.tC = getDouble(cs.temperature.tC);
        }
        if (cs.apower != null) {
            rs.power = sm.power = emeter.power = cs.apower;
        }
        if (cs.aenergy != null) {
            sm.total = emeter.total = cs.aenergy.total;
            sm.counters = cs.aenergy.byMinute;
            sm.timestamp = (long) cs.aenergy.minuteTs;
        }
        if (cs.voltage != null) {
            emeter.voltage = cs.voltage;
        }
        if (cs.current != null) {
            emeter.current = cs.current;
        }
        if (cs.pf != null) {
            emeter.pf = cs.pf;
        }

        rollerStatus.set(cs.id, rs);
        status.rollers.set(cs.id, rs);
        relayStatus.meters.set(cs.id, sm);
        status.meters.set(cs.id, sm);
        status.emeters.set(cs.id, emeter);

        postAlarms(cs.errors);
        if (rs.calibrating != null && rs.calibrating) {
            getThing().postEvent(SHELLY_EVENT_ROLLER_CALIB, false);
        }

        return updateChannels ? ShellyComponents.updateRoller((ShellyBaseHandler) getThing(), rs, cs.id) : false;
    }

    // Addon
    private void updateAddonStatus(ShellySettingsStatus status, @Nullable Shelly2DeviceStatusResult ds)
            throws ShellyApiException {
        if (ds == null) {
            return;
        }

        if (ds.temperature100 != null) {
            if (status.extTemperature == null) {
                status.extTemperature = new ShellyExtTemperature();
            }
            status.extTemperature.sensor1 = updateExtTempSensor(ds.temperature100);
            status.extTemperature.sensor2 = updateExtTempSensor(ds.temperature101);
            status.extTemperature.sensor3 = updateExtTempSensor(ds.temperature102);
            status.extTemperature.sensor4 = updateExtTempSensor(ds.temperature103);
            status.extTemperature.sensor5 = updateExtTempSensor(ds.temperature104);
        }
        if (ds.humidity100 != null) {
            status.extHumidity = new ShellyExtHumidity(ds.humidity100.rh);
        }
        if (ds.voltmeter100 != null) {
            status.extVoltage = new ShellyExtVoltage(ds.voltmeter100.voltage);
        }
        if (ds.input100 != null) {
            status.extDigitalInput = new ShellyExtDigitalInput(getBool(ds.input100.state));
        }
    }

    private @Nullable ShellyShortTemp updateExtTempSensor(@Nullable Shelly2DeviceStatusTempId value) {
        if (value != null) {
            ShellyShortTemp temp = new ShellyShortTemp();
            temp.hwID = value.id.toString();
            temp.tC = value.tC;
            temp.tF = value.tF;
            return temp;
        }
        return null;
    }

    protected void updateHumidityStatus(ShellyStatusSensor sdata, @Nullable Shelly2DeviceStatusHumidity value) {
        if (value == null) {
            return;
        }
        if (sdata.hum == null) {
            sdata.hum = new ShellySensorHum();
        }
        sdata.hum.value = getDouble(value.rh);
    }

    protected void updateTemperatureStatus(ShellyStatusSensor sdata, @Nullable Shelly2DeviceStatusTempId value) {
        if (value == null) {
            return;
        }
        if (sdata.tmp == null) {
            sdata.tmp = new ShellySensorTmp();
        }
        sdata.tmp.isValid = true;
        sdata.tmp.units = SHELLY_TEMP_CELSIUS;
        sdata.tmp.tC = value.tC;
        sdata.tmp.tF = value.tF;
    }

    protected void updateBatteryStatus(ShellyStatusSensor sdata, @Nullable Shelly2DeviceStatusPower value) {
        if (value == null) {
            return;
        }
        if (sdata.bat == null) {
            sdata.bat = new ShellySensorBat();
        }

        if (value.battery != null) {
            sdata.bat.voltage = value.battery.volt;
            sdata.bat.value = value.battery.percent;
        }
        if (value.external != null) {
            sdata.charger = value.external.present;
        }
    }

    private void postAlarms(@Nullable ArrayList<@Nullable String> errors) throws ShellyApiException {
        if (errors != null) {
            for (String e : errors) {
                if (e != null) {
                    getThing().postEvent(e, false);
                }
            }
        }
    }

    protected @Nullable ArrayList<ShellySettingsInput> fillInputSettings(ShellyDeviceProfile profile,
            Shelly2GetConfigResult dc) {
        if (dc.input0 == null) {
            return null; // device has no input
        }

        ArrayList<ShellySettingsInput> inputs = new ArrayList<>();
        addInputSettings(inputs, dc.input0);
        addInputSettings(inputs, dc.input1);
        addInputSettings(inputs, dc.input2);
        addInputSettings(inputs, dc.input3);
        return inputs;
    }

    private void addInputSettings(ArrayList<ShellySettingsInput> inputs, @Nullable Shelly2DevConfigInput ic) {
        if (ic == null) {
            return;
        }

        ShellySettingsInput settings = new ShellySettingsInput();
        settings.btnType = getString(ic.type).equalsIgnoreCase(SHELLY2_INPUTT_BUTTON) ? SHELLY_BTNT_MOMENTARY
                : SHELLY_BTNT_EDGE;
        inputs.add(settings);
    }

    protected boolean updateInputStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult ds,
            boolean updateChannels) throws ShellyApiException {
        boolean updated = false;
        updated |= addInputStatus(status, ds.input0, updateChannels);
        updated |= addInputStatus(status, ds.input1, updateChannels);
        updated |= addInputStatus(status, ds.input2, updateChannels);
        updated |= addInputStatus(status, ds.input3, updateChannels);
        status.inputs = relayStatus.inputs;
        return updated;
    }

    private boolean addInputStatus(ShellySettingsStatus status, @Nullable Shelly2InputStatus is, boolean updateChannels)
            throws ShellyApiException {
        if (is == null) {
            return false;
        }
        ShellyDeviceProfile profile = getProfile();

        if (is.id == null || is.id > profile.numInputs) {
            logger.debug("{}: Invalid input id: {}", thingName, is.id);
            return false;
        }

        String group = profile.getInputGroup(is.id);
        ShellyInputState input = relayStatus.inputs.size() > is.id ? relayStatus.inputs.get(is.id)
                : new ShellyInputState();
        boolean updated = false;
        input.input = getBool(is.state) ? 1 : 0; // old format Integer, new one Boolean
        if (input.event == null && profile.inButtonMode(is.id)) {
            input.event = "";
            input.eventCount = 0;
        }
        if (is.percent != null) { // analogous input
            status.extAnalogInput = new ShellyExtAnalogInput(getDouble(is.percent));
        }
        relayStatus.inputs.set(is.id, input);
        if (updateChannels) {
            updated |= updateChannel(group, CHANNEL_INPUT + profile.getInputSuffix(is.id), getOnOff(getBool(is.state)));
        }
        return updated;
    }

    protected Shelly2RpcBaseMessage buildRequest(String method, @Nullable Object params) throws ShellyApiException {
        Shelly2RpcBaseMessage request = new Shelly2RpcBaseMessage();
        request.id = Math.abs(random.nextInt());
        request.src = thingName;
        request.method = !method.contains(".") ? SHELLYRPC_METHOD_CLASS_SHELLY + "." + method : method;
        request.params = params;
        request.auth = authReq;
        return request;
    }

    protected Shelly2AuthRequest buildAuthRequest(Shelly2AuthResponse authParm, String user, String realm,
            String password) throws ShellyApiException {
        Shelly2AuthRequest authReq = new Shelly2AuthRequest();
        authReq.username = "admin";
        authReq.realm = realm;
        authReq.nonce = authParm.nonce;
        authReq.cnonce = (long) Math.floor(Math.random() * 10e8);
        authReq.nc = authParm.nc != null ? authParm.nc : 1;
        authReq.authType = SHELLY2_AUTHTTYPE_DIGEST;
        authReq.algorithm = SHELLY2_AUTHALG_SHA256;
        String ha1 = sha256(authReq.username + ":" + authReq.realm + ":" + password);
        String ha2 = SHELLY2_AUTH_NOISE;
        authReq.response = sha256(
                ha1 + ":" + authReq.nonce + ":" + authReq.nc + ":" + authReq.cnonce + ":" + "auth" + ":" + ha2);
        return authReq;
    }

    protected String mapValue(Map<String, String> map, @Nullable String key) {
        String value;
        boolean known = key != null && !key.isEmpty() && map.containsKey(key);
        value = known ? getString(map.get(key)) : "";
        logger.trace("{}: API value {} was mapped to {}", thingName, key, known ? value : "UNKNOWN");
        return value;
    }

    protected boolean updateChannel(String group, String channel, State value) throws ShellyApiException {
        return getThing().updateChannel(group, channel, value);
    }

    protected ShellyThingInterface getThing() throws ShellyApiException {
        ShellyThingInterface t = thing;
        if (t != null) {
            return t;
        }
        throw new ShellyApiException("Thing/profile not initialized!");
    }

    ShellyDeviceProfile getProfile() throws ShellyApiException {
        if (thing != null) {
            return thing.getProfile();
        }
        throw new ShellyApiException("Unable to get profile, thing not initialized!");
    }
}
