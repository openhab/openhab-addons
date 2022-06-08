package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpClient;
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
import org.openhab.binding.shelly.internal.handler.ShellyComponents;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shelly2ApiClient extends ShellyHttpClient {
    private final Logger logger = LoggerFactory.getLogger(Shelly2ApiRpc.class);
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
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_OPENING, SHELLY2_RSTATE_OPENING); // Gen2-only
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_OPEN, SHELLY_RSTATE_OPEN);
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_CLOSING, SHELLY2_RSTATE_CLOSING); // Gen2-only
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_CLOSED, SHELLY_RSTATE_CLOSE);
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
        rsettings.autoOn = cs.autoOnDelay;
        rsettings.autoOff = cs.autoOffDelay;
        String mode = getString(cs.mode).toLowerCase();
        rsettings.btnType = mapValue(MAP_INMODE_BTNTYPE, mode);
        relays.add(rsettings);
    }

    protected void fillRelayStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult rs)
            throws ShellyApiException {
        updateRelayStatus(status, 0, rs.switch0);
        updateRelayStatus(status, 1, rs.switch1);
        updateRelayStatus(status, 2, rs.switch2);
        updateRelayStatus(status, 3, rs.switch3);
    }

    private ShellyStatusRelay updateRelayStatus(ShellySettingsStatus status, int relayIndex, Shelly2RelayStatus rs)
            throws ShellyApiException {
        if (relayIndex >= getProfile().numRelays) {
            return new ShellyStatusRelay();
        }

        ShellySettingsRelay rstatus = status.relays.get(relayIndex); // new ShellySettingsRelay();
        rstatus = status.relays.get(relayIndex);
        rstatus.ison = getBool(rs.output);
        rstatus.hasTimer = getInteger(rs.timerDuration) > 0;
        rstatus.autoOn = rstatus.hasTimer && rstatus.ison ? 0 : getInteger(rs.timerDuration) * 1.0;
        rstatus.autoOff = rstatus.hasTimer && rstatus.ison ? getInteger(rs.timerDuration) * 1.0 : 0;
        status.relays.set(relayIndex, rstatus);

        if (rs.temperature != null) {
            status.tmp.isValid = true;
            status.tmp.tC = rs.temperature.tC;
            status.tmp.tF = rs.temperature.tF;
            status.tmp.units = "C";
            status.temperature = status.tmp.tC;
        } else {
            status.tmp.isValid = false;
        }
        // status.extHumidity =
        // status.extTemperature =

        ShellyShortStatusRelay sr = new ShellyShortStatusRelay();
        sr.isValid = true;
        sr.ison = rstatus.ison;
        sr.hasTimer = rstatus.hasTimer;
        sr.name = status.name;
        sr.temperature = status.temperature;
        sr.timerRemaining = getInteger(rs.timerDuration);
        relayStatus.relays.set(relayIndex, sr);

        ShellySettingsMeter sm = new ShellySettingsMeter();
        sm.isValid = true;
        sm.power = rs.apower;
        if (rs.aenergy != null) {
            sm.total = rs.aenergy.total;
            sm.counters = rs.aenergy.byMinute;
            sm.timestamp = rs.aenergy.minuteTs;
        }
        relayStatus.meters.set(relayIndex, sm);
        status.meters.set(relayIndex, sm);

        ShellySettingsEMeter emeter = status.emeters.get(relayIndex);
        emeter.isValid = true;
        emeter.power = rs.apower;
        emeter.voltage = rs.voltage;
        emeter.current = rs.current;
        emeter.pf = rs.pf;
        if (rs.aenergy != null) {
            emeter.total = rs.aenergy.total;
            emeter.total = rs.aenergy.total;
            // emeter.counters = rs.aenergy.byMinute;
        }
        status.emeters.set(relayIndex, emeter);

        ShellyComponents.updateMeters(getThing(), status);
        return relayStatus;
    }

    protected boolean updateRelayStatus(ShellySettingsStatus status, @Nullable Shelly2RelayStatus rs)
            throws ShellyApiException {
        if (rs == null || rs.id >= getProfile().numRelays) {
            return false;
        }

        ShellyDeviceProfile profile = getProfile();
        ShellySettingsRelay rstatus = status.relays.get(rs.id);
        ShellyShortStatusRelay sr = relayStatus.relays.get(rs.id);
        String group = profile.getControlGroup(rs.id);
        boolean updated = false;

        sr.isValid = true;
        if (rs.output != null) {
            rstatus.ison = sr.ison = getBool(rs.output);
            updated |= updateChannel(group, CHANNEL_OUTPUT, getOnOff(rstatus.ison));
        }
        if (getDouble(rs.timerStartetAt) > 0) {
            rstatus.hasTimer = sr.hasTimer = rs.timerDuration > 0;
            sr.timerRemaining = getInteger(rs.timerDuration);
            updated |= updateChannel(group, CHANNEL_TIMER_ACTIVE, getOnOff(sr.hasTimer));
        }
        if (rs.temperature != null && getDouble(rs.temperature.tC) > status.temperature) {
            status.temperature = status.tmp.tC = sr.temperature = getDouble(rs.temperature.tC);
        }
        if (rs.voltage != null) {
            if (status.voltage == null || rs.voltage > status.voltage) {
                status.voltage = rs.voltage;
            }
            updated |= updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_VOLTAGE,
                    toQuantityType(rs.voltage, DIGITS_VOLT, Units.VOLT));
        }
        if (rs.errors != null) {
            for (String error : rs.errors) {
                rstatus.overpower = SHELLY2_ERROR_OVERPOWER.equals(error);
                status.overload = SHELLY2_ERROR_OVERVOLTAGE.equals(error);
                status.overtemperature = SHELLY2_ERROR_OVERTEMP.equals(error);
            }
            sr.overpower = rstatus.overpower;
            sr.overtemperature = status.overtemperature;
        }

        // Update internal structures
        status.relays.set(rs.id, rstatus);
        relayStatus.relays.set(rs.id, sr);
        return updated;
    }

    protected @Nullable ArrayList<@Nullable ShellySettingsRoller> fillRollerSettings(ShellyDeviceProfile profile,
            Shelly2GetConfigResult deviceConfig) {
        if (deviceConfig.cover0 == null) {
            return null;
        }

        ArrayList<@Nullable ShellySettingsRoller> rollers = new ArrayList<>();
        addRollerSettings(rollers, deviceConfig.cover0);
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

    protected boolean fillRollerStatus(ShellySettingsStatus status, @Nullable Shelly2CoverStatus rs)
            throws ShellyApiException {
        if (rs == null) {
            return false;
        }
        status.rollers.set(rs.id, updateRollerStatus(status, rs.id, rs));

        ShellySettingsMeter sm = status.meters.get(rs.id);
        sm.isValid = true;
        sm.power = getDouble(rs.apower);
        if (rs.aenergy != null) {
            sm.total = rs.aenergy.total;
            sm.counters = rs.aenergy.byMinute;
            sm.timestamp = (long) rs.aenergy.minuteTs;
        }

        relayStatus.meters.set(rs.id, sm);
        status.meters.set(rs.id, sm);

        ShellySettingsEMeter emeter = status.emeters.get(rs.id);
        emeter.isValid = true;
        emeter.power = rs.apower;
        emeter.voltage = rs.voltage;
        emeter.current = rs.current;
        emeter.pf = rs.pf;
        if (rs.aenergy != null) {
            emeter.total = rs.aenergy.total;
        }
        status.emeters.set(rs.id, emeter);

        return ShellyComponents.updateMeters(getThing(), status);
    }

    protected ShellyRollerStatus updateRollerStatus(ShellySettingsStatus status, int idx,
            Shelly2CoverStatus coverStatus) throws ShellyApiException {
        ShellyRollerStatus rs = status.rollers.get(coverStatus.id);
        rs.isValid = true;
        rs.power = coverStatus.apower;
        rs.calibrating = SHELLY2_RSTATE_CALIB.equals(rs.state);
        if (coverStatus.state != null) {
            rs.state = mapValue(MAP_ROLLER_STATE, coverStatus.state);
        }
        if (coverStatus.currentPos != null) {
            rs.currentPos = coverStatus.currentPos;
        }
        if (coverStatus.moveStartedAt != null) {
            rs.duration = (int) (now() - coverStatus.moveStartedAt.longValue());
        }
        if (coverStatus.temperature != null && coverStatus.temperature.tC > getDouble(status.temperature)) {
            status.temperature = status.tmp.tC = getDouble(coverStatus.temperature.tC);
        }

        if (rs.calibrating) {
            getThing().postEvent(SHELLY_EVENT_ROLLER_CALIB, false);
        }
        postAlarms(coverStatus.errors);

        rollerStatus.set(idx, rs);
        return rs;
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
        sdata.bat.voltage = value.battery.volt;
        sdata.bat.value = value.battery.percent;
        sdata.charger = value.external.present;
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
        settings.btnType = ic.type.equalsIgnoreCase(SHELLY2_BTNT_DETACHED) ? SHELLY_BTNT_MOMENTARY : SHELLY_BTNT_EDGE;
        inputs.add(settings);
    }

    protected boolean updateInputStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult ds,
            boolean updateChannels) throws ShellyApiException {
        if (ds.input0 == null) {
            return false; // device has no inouts
        }

        boolean updated = false;
        ArrayList<ShellyInputState> inputs = new ArrayList<>();
        updated |= addInputStatus(inputs, ds.input0, updateChannels);
        updated |= addInputStatus(inputs, ds.input1, updateChannels);
        updated |= addInputStatus(inputs, ds.input2, updateChannels);
        updated |= addInputStatus(inputs, ds.input3, updateChannels);
        status.inputs = relayStatus.inputs = inputs;
        return updated;
    }

    private boolean addInputStatus(ArrayList<ShellyInputState> inputs, @Nullable Shelly2InputStatus is,
            boolean updateChannels) throws ShellyApiException {
        if (is == null) {
            return false;
        }

        String group = getProfile().getInputGroup(is.id);
        ShellyInputState input = relayStatus.inputs.get(is.id);
        boolean updated = false;
        input.input = getBool(is.state) ? 1 : 0;
        if (input.event == null && getProfile().inButtonMode(is.id)) {
            input.event = "";
            input.eventCount = 0;
        }
        inputs.add(input);

        if (updateChannels) {
            updated |= updateChannel(group, CHANNEL_INPUT + getProfile().getInputSuffix(is.id),
                    getOnOff(getBool(is.state)));
        }
        return updated;
    }

    protected Shelly2RpcBaseMessage builRequest(String src, String method, Object params) {
        Shelly2RpcBaseMessage request = new Shelly2RpcBaseMessage();
        request.id = random.nextInt();
        // request.src = src;
        request.method = !method.contains(".") ? SHELLYRPC_METHOD_CLASS_SHELLY + "." + method : method;
        request.params = params;
        request.auth = authReq;
        return request;
    }

    protected Shelly2AuthRequest buioldAuthRequest(Shelly2AuthResponse authParm, String user, String realm,
            String password) throws ShellyApiException {
        Shelly2AuthRequest authReq = new Shelly2AuthRequest();
        authReq.username = user;
        authReq.realm = realm;
        authReq.realm = "shellypro4pm-f008d1d8b8b8";
        authReq.nonce = authParm.nonce;
        authReq.nonce = 1625038762l;
        authReq.cnonce = (long) Math.floor(Math.random() * 10e8);
        authReq.algorithm = authParm.algorithm;

        String ha1 = sha256(user + ":" + realm + ":" + password);
        String ha2 = sha256("dummy_method:dummy_uri");
        authReq.response = sha256(
                ha1 + ":" + authReq.nonce + ":" + authParm.nc + ":" + authReq.cnonce + ":" + "auth" + ":" + ha2);
        return authReq;
    }

    protected String mapValue(Map<String, String> map, @Nullable String key) {
        String value;
        boolean known = key != null && !key.isEmpty() && map.containsKey(key);
        value = known ? getString(map.get(key)) : "";
        if (!known) {
            int i = 1;
        }
        logger.debug("{}: API value {} was mapped to {}", thingName, key, known ? value : "UNKNOWN");
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
        throw new ShellyApiException("Thing/profile not initialized!");
    }

}
