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

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_INPUT;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRoller;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
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
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorLux;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2AuthRsp;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2CBStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigCover;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigInput;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigSwitch;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.ShellyDeviceConfigCB;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2CoverStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusEm;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusEmData;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusHumidity;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusIlluminance;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusSmoke;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusTempId;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusVoltage;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2RGBWStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2InputStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RelayStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2StatusEm1;
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
    protected @Nullable Shelly2AuthRsp authReq;

    public Shelly2ApiClient(String thingName, ShellyThingInterface thing) {
        super(thingName, thing);
        this.thing = thing;
    }

    public Shelly2ApiClient(String thingName, ShellyThingConfiguration config, HttpClient httpClient) {
        super(thingName, config, httpClient);
    }

    protected static final Map<String, String> MAP_INMODE_BTNTYPE = Map.of(//
            SHELLY2_BTNT_MOMENTARY, SHELLY_BTNT_MOMENTARY, //
            SHELLY2_BTNT_FLIP, SHELLY_BTNT_TOGGLE, //
            SHELLY2_BTNT_FOLLOW, SHELLY_BTNT_EDGE, //
            SHELLY2_BTNT_DETACHED, SHELLY_BTNT_MOMENTARY);

    protected static final Map<String, String> MAP_INPUT_EVENT_TYPE = Map.ofEntries(//
            Map.entry(SHELLY2_EVENT_1PUSH, SHELLY_BTNEVENT_1SHORTPUSH),
            Map.entry(SHELLY2_EVENT_2PUSH, SHELLY_BTNEVENT_2SHORTPUSH),
            Map.entry(SHELLY2_EVENT_3PUSH, SHELLY_BTNEVENT_3SHORTPUSH),
            Map.entry(SHELLY2_EVENT_LPUSH, SHELLY_BTNEVENT_LONGPUSH),
            Map.entry(SHELLY2_EVENT_LSPUSH, SHELLY_BTNEVENT_LONGSHORTPUSH),
            Map.entry(SHELLY2_EVENT_SLPUSH, SHELLY_BTNEVENT_SHORTLONGPUSH));

    public static final Map<Integer, String> MAP_BLU_INPUT_EVENT_TYPE = Map.ofEntries(//
            // BTHome
            Map.entry(1, SHELLY_BTNEVENT_1SHORTPUSH), // press
            Map.entry(2, SHELLY_BTNEVENT_2SHORTPUSH), // double_press
            Map.entry(3, SHELLY_BTNEVENT_3SHORTPUSH), // triple_press
            Map.entry(4, SHELLY_BTNEVENT_LONGPUSH), // long_press
            Map.entry(5, SHELLY_BTNEVENT_LONGSHORTPUSH), // we have no long_double_press
            Map.entry(6, SHELLY_BTNEVENT_SHORTLONGPUSH), // we have no long_triple_press
            Map.entry(128, SHELLY_BTNEVENT_HOLDING), // hold_press
            Map.entry(254, SHELLY_BTNEVENT_HOLDING)); // hold_press old firmware

    protected static final Map<String, String> MAP_INPUT_EVENT_ID = Map.of(//
            SHELLY2_EVENT_BTNUP, SHELLY_EVENT_BTN_OFF, //
            SHELLY2_EVENT_BTNDOWN, SHELLY_EVENT_BTN_ON, //
            SHELLY2_EVENT_1PUSH, SHELLY_EVENT_SHORTPUSH, //
            SHELLY2_EVENT_2PUSH, SHELLY_EVENT_DOUBLE_SHORTPUSH, //
            SHELLY2_EVENT_3PUSH, SHELLY_EVENT_TRIPLE_SHORTPUSH, //
            SHELLY2_EVENT_LPUSH, SHELLY_EVENT_LONGPUSH, //
            SHELLY2_EVENT_LSPUSH, SHELLY_EVENT_LONG_SHORTPUSH, //
            SHELLY2_EVENT_SLPUSH, SHELLY_EVENT_SHORT_LONGTPUSH, //
            SHELLY2_EVENT_HOLDING, SHELLY_EVENT_HOLDING);

    protected static final Map<String, String> MAP_INPUT_MODE = Map.of(//
            SHELLY2_RMODE_SINGLE, SHELLY_INP_MODE_ONEBUTTON, //
            SHELLY2_RMODE_DUAL, SHELLY_INP_MODE_OPENCLOSE, //
            SHELLY2_RMODE_DETACHED, SHELLY_INP_MODE_ONEBUTTON);

    protected static final Map<String, String> MAP_ROLLER_STATE = Map.of(//
            SHELLY2_RSTATE_OPEN, SHELLY_RSTATE_OPEN, //
            SHELLY2_RSTATE_CLOSED, SHELLY_RSTATE_CLOSE, //
            SHELLY2_RSTATE_OPENING, SHELLY2_RSTATE_OPENING, // Gen2-only
            SHELLY2_RSTATE_CLOSING, SHELLY2_RSTATE_CLOSING, // Gen2-only
            SHELLY2_RSTATE_STOPPED, SHELLY_RSTATE_STOP, //
            SHELLY2_RSTATE_CALIB, SHELLY2_RSTATE_CALIB); // Gen2-only

    protected static final Map<String, String> MAP_PROFILE = Map.of(//
            SHELLY2_PROFILE_RELAY, SHELLY_CLASS_RELAY, //
            SHELLY2_PROFILE_COVER, SHELLY_CLASS_ROLLER, //
            SHELLY2_PROFILE_LIGHT, SHELLY_MODE_WHITE, //
            SHELLY2_PROFILE_RGB, SHELLY_MODE_COLOR, //
            SHELLY2_PROFILE_RGBW, SHELLY_MODE_COLOR);

    protected @Nullable ArrayList<@Nullable ShellySettingsRelay> fillRelaySettings(ShellyDeviceProfile profile,
            Shelly2GetConfigResult dc) {
        ArrayList<@Nullable ShellySettingsRelay> relays = new ArrayList<>();
        addRelaySettings(relays, dc.switch0);
        addRelaySettings(relays, dc.switch1);
        addRelaySettings(relays, dc.switch2);
        addRelaySettings(relays, dc.switch3);
        addRelaySettings(relays, dc.switch100);
        return !relays.isEmpty() ? relays : null;
    }

    private void addRelaySettings(ArrayList<@Nullable ShellySettingsRelay> relays,
            @Nullable Shelly2DevConfigSwitch cs) {
        if (cs == null) {
            return;
        }

        ShellySettingsRelay rsettings = new ShellySettingsRelay();
        rsettings.id = cs.id;
        rsettings.isValid = cs.id != null;
        rsettings.name = cs.name;
        rsettings.ison = false;
        rsettings.autoOn = getBool(cs.autoOn) ? cs.autoOnDelay : 0;
        rsettings.autoOff = getBool(cs.autoOff) ? cs.autoOffDelay : 0;
        rsettings.hasTimer = false;
        rsettings.btnType = mapValue(MAP_INMODE_BTNTYPE, getString(cs.mode).toLowerCase());
        relays.add(rsettings);
    }

    protected @Nullable ArrayList<@Nullable ShellySettingsRelay> fillBreakerSettings(ShellyDeviceProfile profile,
            Shelly2GetConfigResult dc) {
        ArrayList<@Nullable ShellySettingsRelay> relays = new ArrayList<>();
        addBreakerSettings(relays, dc.cb0);
        addBreakerSettings(relays, dc.cb1);
        addBreakerSettings(relays, dc.cb2);
        addBreakerSettings(relays, dc.cb3);
        return !relays.isEmpty() ? relays : null;
    }

    private void addBreakerSettings(ArrayList<@Nullable ShellySettingsRelay> relays,
            @Nullable ShellyDeviceConfigCB cs) {
        if (cs == null) {
            return;
        }

        ShellySettingsRelay rsettings = new ShellySettingsRelay();
        rsettings.id = cs.id;
        rsettings.isValid = cs.id != null;
        rsettings.name = cs.name;
        rsettings.ison = false;
        relays.add(rsettings);
    }

    protected boolean fillDeviceStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult result,
            boolean channelUpdate) throws ShellyApiException {
        boolean updated = false;

        if (result.temperature0 != null && result.temperature0.tC != null && !getProfile().isSensor) {
            if (status.tmp == null) {
                status.tmp = new ShellySensorTmp();
            }
            status.temperature = status.tmp.tC = result.temperature0.tC;
        }

        updated |= updateInputStatus(status, result, channelUpdate);
        updated |= updateRelayStatus(0, status, result.switch0, channelUpdate);
        updated |= updateRelayStatus(1, status, result.switch1, channelUpdate);
        updated |= updateRelayStatus(2, status, result.switch2, channelUpdate);
        updated |= updateRelayStatus(3, status, result.switch3, channelUpdate);
        updated |= updateRelayStatus(100, status, result.switch100, channelUpdate);
        updated |= updateRelayStatus(10, status, result.pm10, channelUpdate);
        updated |= updateBreakerStatus(0, status, result.cb0, result.voltmeter0, channelUpdate);
        updated |= updateBreakerStatus(1, status, result.cb1, result.voltmeter1, channelUpdate);
        updated |= updateBreakerStatus(2, status, result.cb2, result.voltmeter2, channelUpdate);
        updated |= updateBreakerStatus(3, status, result.cb3, result.voltmeter3, channelUpdate);
        updated |= updateEmStatus(0, status, result.em0, result.emdata0, channelUpdate);
        updated |= updateEmStatus(10, status, result.em10, channelUpdate);
        updated |= updateEmStatus(11, status, result.em11, channelUpdate);
        updated |= updateRollerStatus(0, status, result.cover0, channelUpdate);
        updated |= updateDimmerStatus(0, status, result.light0, channelUpdate);
        updated |= updateRGBWStatus(0, status, result.rgbw0, channelUpdate);
        if (channelUpdate) {
            updated |= ShellyComponents.updateMeters(getThing(), status);
        }

        updateHumidityStatus(sensorData, result.humidity0);
        updateTemperatureStatus(sensorData, result.temperature0);
        updateIlluminanceStatus(sensorData, result.illuminance0);
        updateSmokeStatus(sensorData, result.smoke0);
        updateBatteryStatus(sensorData, result.devicepower0);
        updateAddonStatus(status, result);
        updated |= ShellyComponents.updateSensors(getThing(), status);
        return updated;
    }

    private boolean updateRelayStatus(int id, ShellySettingsStatus status, @Nullable Shelly2RelayStatus rs,
            boolean channelUpdate) throws ShellyApiException {
        if (rs == null) {
            return false;
        }
        ShellyDeviceProfile profile = getProfile();

        ShellySettingsRelay rstatus;
        ShellyShortStatusRelay sr;
        if (rs.id == null) { // firmware 1.6.1 returns id = null!
            rs.id = id;
        }
        int rIdx = getRelayIdx(profile, rs.id);
        if (profile.hasRelays) {
            if (rIdx == -1) {
                throw new IllegalArgumentException("Update for invalid relay index");
            }
            rstatus = status.relays.get(rIdx);
            sr = relayStatus.relays.get(rIdx);
        } else {
            rstatus = new ShellySettingsRelay();
            sr = new ShellyShortStatusRelay();
            rIdx = rs.id;
        }

        sr.isValid = rstatus.isValid = true;
        sr.name = rstatus.name = status.name;
        if (rs.output != null) {
            sr.ison = rstatus.ison = getBool(rs.output);
        }
        if (rs.timerStartetAt != null && rs.timerStartetAt > 0) {
            sr.timerRemaining = (int) (now() - rs.timerStartetAt);
        }
        if (rs.temperature != null && rs.temperature.tC != null) {
            if (status.tmp == null) {
                status.tmp = new ShellySensorTmp();
            }
            status.tmp.isValid = true;
            status.tmp.tC = rs.temperature.tC;
            status.tmp.tF = rs.temperature.tF;
            status.tmp.units = "C";
            sr.temperature = rs.temperature.tC;
            if (status.temperature == null || rs.temperature.tC > status.temperature) {
                status.temperature = sr.temperature;
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
        ShellySettingsEMeter emeter = status.emeters != null ? status.emeters.get(rIdx) : new ShellySettingsEMeter();
        if (rs.apower != null) {
            sm.power = emeter.power = rs.apower;
        }
        if (rs.aenergy != null) {
            // Gen2 reports Watt, needs to be converted to W/h
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
        if (rs.frequency != null) {
            emeter.frequency = rs.frequency;
        }
        if (rs.pf != null) {
            emeter.pf = rs.pf;
        }

        if (profile.hasRelays) {
            // Update internal structures
            status.relays.set(rIdx, rstatus);
            relayStatus.relays.set(rIdx, sr);
        }

        updateMeter(status, rIdx, sm, emeter, channelUpdate);
        return channelUpdate && profile.hasRelays
                ? ShellyComponents.updateRelay((ShellyBaseHandler) getThing(), status, rIdx)
                : false;
    }

    private boolean updateBreakerStatus(int id, ShellySettingsStatus status, @Nullable Shelly2CBStatus bs,
            @Nullable Shelly2DeviceStatusVoltage vm, boolean channelUpdate) throws ShellyApiException {
        if (bs == null) {
            return false;
        }
        ShellyDeviceProfile profile = getProfile();

        ShellySettingsRelay rstatus;
        ShellyShortStatusRelay sr;
        if (bs.id == null) { // invalid for fw 1.6.1
            bs.id = id;
        }
        int rIdx = getRelayIdx(profile, bs.id);
        if (profile.hasRelays) {
            if (rIdx == -1) {
                throw new IllegalArgumentException("Update for invalid relay index");
            }
            rstatus = status.relays.get(rIdx);
            sr = relayStatus.relays.get(rIdx);
        } else {
            rstatus = new ShellySettingsRelay();
            sr = new ShellyShortStatusRelay();
            rIdx = bs.id;
        }

        sr.isValid = rstatus.isValid = true;
        sr.name = rstatus.name = status.name;
        if (bs.output != null) {
            sr.ison = rstatus.ison = getBool(bs.output);
        }
        if (bs.temperature != null && bs.temperature.tC != null) {
            if (status.tmp == null) {
                status.tmp = new ShellySensorTmp();
            }
            status.tmp.isValid = true;
            status.tmp.tC = bs.temperature.tC;
            status.tmp.tF = bs.temperature.tF;
            status.tmp.units = "C";
            sr.temperature = getDouble(bs.temperature.tC);
            if (status.temperature == null || getDouble(bs.temperature.tC) > status.temperature) {
                status.temperature = sr.temperature;
            }
        }

        ShellySettingsMeter sm = new ShellySettingsMeter();
        ShellySettingsEMeter emeter = status.emeters != null ? status.emeters.get(rIdx) : new ShellySettingsEMeter();
        if (vm != null && vm.voltage != null) {
            emeter.voltage = vm.voltage;
        }

        if (profile.hasRelays) {
            // Update internal structures
            status.relays.set(rIdx, rstatus);
            relayStatus.relays.set(rIdx, sr);
        }

        updateMeter(status, rIdx, sm, emeter, channelUpdate);
        return channelUpdate && profile.hasRelays
                ? ShellyComponents.updateRelay((ShellyBaseHandler) getThing(), status, rIdx)
                : false;
    }

    private int getRelayIdx(ShellyDeviceProfile profile, @Nullable Integer id) {
        List<ShellySettingsRelay> relays = profile.settings.relays;
        if (id != null && relays != null) {
            int idx = 0;
            for (ShellySettingsRelay relay : relays) {
                if (relay.isValid && relay.id != null && relay.id.intValue() == id.intValue()) {
                    return idx;
                }
                idx++;
            }
        }
        return -1;
    }

    private void updateMeter(ShellySettingsStatus status, int id, ShellySettingsMeter sm, ShellySettingsEMeter emeter,
            boolean channelUpdate) throws ShellyApiException {
        if (getProfile().numMeters == 0) {
            return;
        }
        sm.isValid = sm.power != null || sm.total != null;
        emeter.isValid = emeter.current != null || emeter.voltage != null || emeter.power != null
                || emeter.total != null;
        status.meters.set(id, sm);
        status.emeters.set(id, emeter);
        relayStatus.meters.set(id, sm);
    }

    private boolean updateEmStatus(int id, ShellySettingsStatus status, @Nullable Shelly2StatusEm1 em,
            boolean channelUpdate) throws ShellyApiException {
        if (em == null) {
            return false;
        }
        if (em.id == null) { // invalid in fw 1.6.1
            em.id = id >= 10 ? id - 10 : id; // ids start at 10
        }

        ShellySettingsMeter sm = new ShellySettingsMeter();
        ShellySettingsEMeter emeter = status.emeters.get(em.id);
        if (em.actPower != null) {
            sm.power = emeter.power = em.actPower;
        }
        if (em.aptrPower != null) {
            emeter.totalReturned = em.aptrPower;
        }
        if (em.voltage != null) {
            emeter.voltage = em.voltage;
        }
        if (em.current != null) {
            emeter.current = em.current;
        }
        if (em.pf != null) {
            emeter.pf = em.pf;
        }
        // Update internal structures
        updateMeter(status, em.id, sm, emeter, channelUpdate);

        postAlarms(em.errors);
        return channelUpdate ? ShellyComponents.updateMeters(getThing(), status) : false;
    }

    private boolean updateEmStatus(int id, ShellySettingsStatus status, @Nullable Shelly2DeviceStatusEm em,
            @Nullable Shelly2DeviceStatusEmData emData, boolean channelUpdate) throws ShellyApiException {
        if (em == null || emData == null) {
            return false;
        }
        if (em.id == null) { // 1.6.1 bug
            em.id = id;
        }

        if (em.totalCurrent != null) {
            status.totalCurrent = em.totalCurrent;
        }
        if (em.totalActPower != null) {
            status.totalPower = em.totalActPower;
        }
        if (em.totalAprtPower != null) {
            status.totalReturned = em.totalAprtPower;
        }

        if (emData.totalKWH != null) {
            status.totalKWH = emData.totalKWH;
        }

        ShellySettingsMeter sm = new ShellySettingsMeter();
        ShellySettingsEMeter emeter = status.emeters.get(0);
        if (em.aActPower != null) {
            sm.power = emeter.power = em.aActPower;
        }
        if (emData.aTotal != null) {
            emeter.total = emData.aTotal;
        }
        if (em.aAprtPower != null) {
            emeter.totalReturned = em.aAprtPower;
        }
        if (em.aVoltage != null) {
            emeter.voltage = em.aVoltage;
        }
        if (em.aCurrent != null) {
            emeter.current = em.aCurrent;
        }
        if (em.aPF != null) {
            emeter.pf = em.aPF;
        }
        // Update internal structures
        updateMeter(status, 0, sm, emeter, channelUpdate);

        if (status.emeters.size() > 1) {
            sm = new ShellySettingsMeter();
            emeter = status.emeters.get(1);
            sm.isValid = emeter.isValid = true;
            if (em.bActPower != null) {
                sm.power = emeter.power = em.bActPower;
            }
            if (emData.bTotal != null) {
                emeter.total = emData.bTotal;
            }
            if (em.bAprtPower != null) {
                emeter.totalReturned = em.bAprtPower;
            }
            if (em.bVoltage != null) {
                emeter.voltage = em.bVoltage;
            }
            if (em.bCurrent != null) {
                emeter.current = em.bCurrent;
            }
            if (em.bPF != null) {
                emeter.pf = em.bPF;
            }
            // Update internal structures
            updateMeter(status, 1, sm, emeter, channelUpdate);
        }

        if (status.emeters.size() > 2) {
            sm = new ShellySettingsMeter();
            emeter = status.emeters.get(2);
            sm.isValid = emeter.isValid = true;
            if (em.cActPower != null) {
                sm.power = emeter.power = em.cActPower;
            }
            if (emData.cTotal != null) {
                emeter.total = emData.cTotal;
            }
            if (em.cAprtPower != null) {
                emeter.totalReturned = em.cAprtPower;
            }
            if (em.cVoltage != null) {
                emeter.voltage = em.cVoltage;
            }
            if (em.cCurrent != null) {
                emeter.current = em.cCurrent;
            }
            if (em.cPF != null) {
                emeter.pf = em.cPF;
            }
            // Update internal structures
            updateMeter(status, 2, sm, emeter, channelUpdate);
        }

        return channelUpdate ? ShellyComponents.updateMeters(getThing(), status) : false;
    }

    protected @Nullable ArrayList<@Nullable ShellySettingsRoller> fillRollerSettings(ShellyDeviceProfile profile,
            Shelly2GetConfigResult dc) {
        if (dc.cover0 == null) {
            return null;
        }

        ArrayList<@Nullable ShellySettingsRoller> rollers = new ArrayList<>();

        addRollerSettings(rollers, 0, dc.cover0);
        fillRollerFavorites(profile, dc);
        return rollers;
    }

    private void addRollerSettings(ArrayList<@Nullable ShellySettingsRoller> rollers, int id,
            @Nullable Shelly2DevConfigCover coverConfig) {
        if (coverConfig == null) {
            return;
        }

        ShellySettingsRoller settings = new ShellySettingsRoller();
        settings.id = id;
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
        if (dc.sys.uiData.cover != null && !dc.sys.uiData.cover.isEmpty()) {
            String[] favorites = dc.sys.uiData.cover.split(",");
            profile.settings.favorites = new ArrayList<>();
            for (int i = 0; i < favorites.length; i++) {
                ShellyFavPos fav = new ShellyFavPos();
                fav.pos = Integer.parseInt(favorites[i]);
                fav.name = fav.pos + "%";
                profile.settings.favorites.add(fav);
            }
            profile.settings.favoritesEnabled = !profile.settings.favorites.isEmpty();
            logger.debug("{}: Roller Favorites loaded: {}", thingName,
                    profile.settings.favoritesEnabled ? profile.settings.favorites.size() : "none");
        }
    }

    private boolean updateRollerStatus(int id, ShellySettingsStatus status, @Nullable Shelly2CoverStatus cs,
            boolean updateChannels) throws ShellyApiException {
        if (cs == null) {
            return false;
        }

        if (cs.id == null) {
            cs.id = id;
        }
        int rIdx = getRollerIdx(getProfile(), cs.id);
        ShellyRollerStatus rs = status.rollers.get(rIdx);
        ShellySettingsMeter sm;
        ShellySettingsEMeter emeter;
        if (status.emeters != null) {
            emeter = status.emeters.get(rIdx);
            sm = status.meters.get(rIdx);
            rs.isValid = sm.isValid = emeter.isValid = true;
        } else {
            emeter = new ShellySettingsEMeter();
            sm = new ShellySettingsMeter();
            rs.isValid = sm.isValid = emeter.isValid = false;
        }
        if (cs.state != null) {
            if (!getString(rs.state).equals(cs.state)) {
                logger.debug("{}: Roller status changed from {} to {}, updateChannels={}", thingName, rs.state,
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
        if (cs.temperature != null && getDouble(cs.temperature.tC) > getDouble(status.temperature)) {
            if (status.tmp == null) {
                status.tmp = new ShellySensorTmp();
            }
            status.temperature = status.tmp.tC = getDouble(cs.temperature.tC);
        }
        if (cs.apower != null) {
            rs.power = sm.power = emeter.power = cs.apower;
        }
        if (cs.aenergy != null) {
            sm.total = emeter.total = getDouble(cs.aenergy.total);
            if (cs.aenergy.byMinute != null) {
                sm.counters = cs.aenergy.byMinute;
            }
            if (cs.aenergy.minuteTs != null) {
                sm.timestamp = (long) cs.aenergy.minuteTs;
            }
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
        if (emeter.isValid) { // Shelly Shutter has no meters
            relayStatus.meters.set(cs.id, sm);
            status.meters.set(cs.id, sm);
            status.emeters.set(cs.id, emeter);
        }

        postAlarms(cs.errors);
        if (rs.calibrating != null && rs.calibrating) {
            getThing().postEvent(SHELLY_EVENT_ROLLER_CALIB, false);
        }

        return updateChannels ? ShellyComponents.updateRoller((ShellyBaseHandler) getThing(), rs, rIdx) : false;
    }

    private int getRollerIdx(ShellyDeviceProfile profile, Integer id) {
        List<ShellySettingsRoller> rollers = profile.settings.rollers;
        if (rollers != null) {
            int idx = 0;
            for (ShellySettingsRoller roller : rollers) {
                if (roller.isValid && roller.id != null && roller.id.intValue() == id.intValue()) {
                    return idx;
                }
                idx++;
            }
        }
        throw new IllegalArgumentException("Update for invalid roller index");
    }

    protected void fillDimmerSettings(ShellyDeviceProfile profile, Shelly2GetConfigResult dc) {
        if (!profile.isDimmer || dc.light0 == null) {
            return;
        }

        List<ShellySettingsDimmer> dimmers = profile.settings.dimmers;
        if (dimmers != null) {
            ShellySettingsDimmer ds = dimmers.get(0);
            ds.autoOn = dc.light0.autoOnDelay;
            ds.autoOff = dc.light0.autoOffDelay;
            ds.name = dc.light0.name;
            dimmers.set(0, ds);
        }
    }

    protected void fillRgbwSettings(ShellyDeviceProfile profile, Shelly2GetConfigResult dc) {
        if (!profile.isRGBW2 || dc.rgbw0 == null) {
            return;
        }

        List<ShellySettingsRgbwLight> lights = profile.settings.lights;
        if (lights != null) {
            ShellySettingsRgbwLight ls = lights.get(0);
            ls.autoOn = dc.rgbw0.autoOnDelay;
            ls.autoOff = dc.rgbw0.autoOffDelay;
            ls.name = dc.rgbw0.name;
            lights.set(0, ls);
        }
    }

    private boolean updateDimmerStatus(int id, ShellySettingsStatus status, @Nullable Shelly2DeviceStatusLight value,
            boolean channelUpdate) throws ShellyApiException {
        ShellyDeviceProfile profile = getProfile();
        if (!profile.isDimmer || value == null) {
            return false;
        }
        if (value.id == null) { // fw 1.6.1
            value.id = id;
        }

        ShellyShortLightStatus ds = status.dimmers.get(value.id);
        if (value.brightness != null) {
            ds.brightness = value.brightness.intValue();
        }
        ds.ison = value.output;
        ds.hasTimer = value.timerStartedAt != null;
        ds.timerDuration = getDuration(value.timerStartedAt, value.timerDuration);
        status.dimmers.set(value.id, ds);
        return channelUpdate ? ShellyComponents.updateDimmers(getThing(), status) : false;
    }

    private boolean updateRGBWStatus(int id, ShellySettingsStatus status, @Nullable Shelly2RGBWStatus value,
            boolean channelUpdate) throws ShellyApiException {
        ShellyDeviceProfile profile = getProfile();
        if (!profile.isRGBW2 || value == null) {
            return false;
        }
        if (value.id == null) {
            value.id = id;
        }

        ShellySettingsLight ds = status.lights.get(value.id);
        ds.brightness = Objects.requireNonNullElse(value.brightness, ds.brightness).intValue();
        if (value.rgb != null) {
            ds.red = value.rgb[0];
            ds.green = value.rgb[1];
            ds.blue = value.rgb[2];
        }
        ds.white = Objects.requireNonNullElse(value.white, ds.white);
        ds.ison = value.output;

        status.lights.set(value.id, ds);
        return channelUpdate ? ShellyComponents.updateRGBW(getThing(), status) : false;
    }

    protected @Nullable Integer getDuration(@Nullable Double timerStartedAt, @Nullable Double timerDuration) {
        if (timerStartedAt == null || timerDuration == null) {
            return null;
        }
        double duration = now() - timerStartedAt;
        return duration <= timerDuration ? (int) (timerDuration - duration) : 0;
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
            if (ds.input100.state != null) {
                status.extDigitalInput = new ShellyExtDigitalInput(getBool(ds.input100.state));
            } else if (ds.input100.percent != null) {
                status.extAnalogInput = new ShellyExtAnalogInput(getDouble(ds.input100.percent));
            }
        }
    }

    private @Nullable ShellyShortTemp updateExtTempSensor(@Nullable Shelly2DeviceStatusTempId value) {
        if (value != null) {
            ShellyShortTemp temp = new ShellyShortTemp();
            temp.hwID = value.id != null ? value.id.toString() : "999";
            temp.tC = getDouble(value.tC);
            temp.tF = getDouble(value.tF);
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
        sdata.tmp.tC = getDouble(value.tC);
        sdata.tmp.tF = getDouble(value.tF);
    }

    protected void updateIlluminanceStatus(ShellyStatusSensor sdata, @Nullable Shelly2DeviceStatusIlluminance value) {
        if (value == null) {
            return;
        }
        if (sdata.lux == null) {
            sdata.lux = new ShellySensorLux();
        }
        sdata.lux.isValid = value.lux != null;
        sdata.lux.value = getDouble(value.lux);
        sdata.lux.illumination = getString(value.illumination);
    }

    protected void updateSmokeStatus(ShellyStatusSensor sdata, @Nullable Shelly2DeviceStatusSmoke value) {
        if (value == null) {
            return;
        }
        sdata.smoke = getBool(value.alarm);
        sdata.mute = getBool(value.mute);
    }

    protected void updateBatteryStatus(ShellyStatusSensor sdata, @Nullable Shelly2DeviceStatusPower value) {
        if (value == null) {
            return;
        }
        if (sdata.bat == null) {
            sdata.bat = new ShellySensorBat();
        }

        if (value.battery != null) {
            sdata.bat.voltage = getDouble(value.battery.volt);
            sdata.bat.value = getDouble(value.battery.percent);
        }
        if (value.external != null && value.external.present != null) {
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

    protected @Nullable ArrayList<@Nullable ShellySettingsInput> fillInputSettings(ShellyDeviceProfile profile,
            Shelly2GetConfigResult dc) {
        if (dc.input0 == null) {
            return null; // device has no input
        }

        ArrayList<@Nullable ShellySettingsInput> inputs = new ArrayList<>();
        addInputSettings(inputs, dc.input0);
        addInputSettings(inputs, dc.input1);
        addInputSettings(inputs, dc.input2);
        addInputSettings(inputs, dc.input3);

        return inputs;
    }

    private void addInputSettings(List<@Nullable ShellySettingsInput> inputs, @Nullable Shelly2DevConfigInput ic) {
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
        updated |= addInputStatus(0, status, ds.input0, updateChannels);
        updated |= addInputStatus(1, status, ds.input1, updateChannels);
        updated |= addInputStatus(2, status, ds.input2, updateChannels);
        updated |= addInputStatus(3, status, ds.input3, updateChannels);
        status.inputs = relayStatus.inputs;
        return updated;
    }

    private boolean addInputStatus(int id, ShellySettingsStatus status, @Nullable Shelly2InputStatus is,
            boolean updateChannels) throws ShellyApiException {
        if (is == null) {
            return false;
        }
        if (is.id == null) {
            is.id = id;
        }
        ShellyDeviceProfile profile = getProfile();

        if (is.id == null || is.id > profile.numInputs) {
            logger.debug("{}: Invalid input id: {}", thingName, is.id);
            return false;
        }

        String group = profile.getInputGroup(is.id);
        ShellyInputState input = relayStatus.inputs.size() > is.id ? relayStatus.inputs.get(is.id)
                : new ShellyInputState(is.id);
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
        request.src = "openhab-" + config.localIp; // use a unique identifier;
        request.method = !method.contains(".") ? SHELLYRPC_METHOD_CLASS_SHELLY + "." + method : method;
        request.params = params;
        request.auth = authReq;
        return request;
    }

    protected String mapValue(Map<String, String> map, @Nullable String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        if (!map.containsKey(key)) {
            logger.warn("{}: Unknown API value '{}' (map data={}), please create an issue on GitHub", thingName, key,
                    map);
            return "";
        }
        String value = getString(map.get(key));
        logger.trace("{}: API value '{}' was mapped to '{}'", thingName, key, value);
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

    protected ShellyDeviceProfile getProfile() throws ShellyApiException {
        ShellyThingInterface thing = this.thing;
        if (thing != null) {
            return thing.getProfile();
        }
        throw new ShellyApiException("Unable to get profile, thing not initialized!");
    }
}
