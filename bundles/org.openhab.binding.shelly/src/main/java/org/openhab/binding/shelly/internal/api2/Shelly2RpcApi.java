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

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.handler.ShellyComponents.updateSensors;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpClient;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorSleepMode;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorTmp;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLogin;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRoller;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortStatusRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorBat;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorHum;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigCover;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigInput;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DevConfigSwitch;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceSettings;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2CoverStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusHumidity;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusTempId;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusSys.Shelly2DeviceStatusSysAvlUpdate;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2InputStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RelayStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2WsConfig;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2WsConfigResponse;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Shelly2RpcApi} implements Gen2 RPC interface
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2RpcApi extends ShellyHttpClient implements ShellyApiInterface, Shelly2WebSocketInterface {
    private static final Map<String, String> MAP_INMODE_BTNTYPE = new HashMap<>();
    static {
        MAP_INMODE_BTNTYPE.put(SHELLY2_BTNT_MOMENTARY, SHELLY_BTNT_MOMENTARY);
        MAP_INMODE_BTNTYPE.put(SHELLY2_BTNT_FLIP, SHELLY_BTNT_TOGGLE);
        MAP_INMODE_BTNTYPE.put(SHELLY2_BTNT_FOLLOW, SHELLY_BTNT_EDGE);
        MAP_INMODE_BTNTYPE.put(SHELLY2_BTNT_DETACHED, SHELLY_BTNT_MOMENTARY);
    }
    private static final Map<String, String> MAP_INPUT_EVENT_TYPE = new HashMap<>();
    static {
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_1PUSH, SHELLY_BTNEVENT_1SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_2PUSH, SHELLY_BTNEVENT_2SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_3PUSH, SHELLY_BTNEVENT_3SHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_LPUSH, SHELLY_BTNEVENT_LONGPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_LSPUSH, SHELLY_BTNEVENT_LONGSHORTPUSH);
        MAP_INPUT_EVENT_TYPE.put(SHELLY2_EVENT_SLPUSH, SHELLY_BTNEVENT_SHORTLONGPUSH);
    }

    private static final Map<String, String> MAP_INPUT_EVENT_ID = new HashMap<>();
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

    private static final Map<String, String> MAP_INPUT_MODE = new HashMap<>();
    static {
        MAP_INPUT_MODE.put(SHELLY2_RMODE_SINGLE, SHELLY_INP_MODE_ONEBUTTON);
        MAP_INPUT_MODE.put(SHELLY2_RMODE_DUAL, SHELLY_INP_MODE_OPENCLOSE);
        MAP_INPUT_MODE.put(SHELLY2_RMODE_DETACHED, SHELLY_INP_MODE_ONEBUTTON);
    }

    private static final Map<String, String> MAP_ROLLER_STATE = new HashMap<>();
    static {
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_OPENING, SHELLY2_RSTATE_OPENING); // Gen2-only
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_OPEN, SHELLY_RSTATE_OPEN);
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_CLOSING, SHELLY2_RSTATE_CLOSING); // Gen2-only
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_CLOSED, SHELLY_RSTATE_CLOSE);
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_STOPPED, SHELLY_RSTATE_STOP);
        MAP_ROLLER_STATE.put(SHELLY2_RSTATE_CALIB, SHELLY2_RSTATE_CALIB); // Gen2-only
    }

    private final Logger logger = LoggerFactory.getLogger(Shelly2RpcApi.class);
    private final ShellyStatusRelay relayStatus = new ShellyStatusRelay();
    private final ShellyStatusSensor sdata = new ShellyStatusSensor();
    private final ArrayList<ShellyRollerStatus> rollerStatus = new ArrayList<>();

    private boolean initialized = false;

    private @Nullable ShellyThingInterface thing;
    private @Nullable ShellyWebSocketRpc rpc;

    /**
     * Regular constructore - called by Thing handler
     *
     * @param thingName Symbolic thing name
     * @param thing Thing Handler (ThingHandlerInterface)
     */
    public Shelly2RpcApi(String thingName, ShellyThingInterface thing) {
        super(thingName, thing);
        try {
            this.thing = thing;
            this.rpc = new ShellyWebSocketRpc(config, this);
            getProfile().initFromThingType(thingName);
        } catch (ShellyApiException e) {
            logger.info("{}: Shelly2 API initialization failed!", thingName, e);
        }
    }

    public @Nullable ShellyWebSocketRpc getRpc() {
        return rpc;
    }

    /**
     * Simple initialization - called by discovery handler
     *
     * @param thingName Symbolic thing name
     * @param config Thing Configuration
     * @param httpClient HTTP Client to be passed to ShellyHttpClient
     */
    public Shelly2RpcApi(String thingName, ShellyThingConfiguration config, HttpClient httpClient) {
        super(thingName, config, httpClient);
    }

    @Override
    public void initialize() throws ShellyApiException {
        if (rpc != null) {
            rpc.initialize();
        }
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public ShellyDeviceProfile getDeviceProfile(String thingType) throws ShellyApiException {
        initialize();

        ShellyDeviceProfile profile = thing != null ? getProfile() : new ShellyDeviceProfile();
        Shelly2GetConfigResult dc = callApi("/rpc/Shelly." + SHELLYRPC_METHOD_GETCONFIG, Shelly2GetConfigResult.class);
        profile.thingName = thingName;
        profile.settings.name = profile.status.name = dc.sys.device.name;
        profile.name = getString(profile.settings.name);
        profile.settings.timezone = getString(dc.sys.location.tz);
        if (dc.cloud != null) {
            profile.settings.cloud.enabled = getBool(dc.cloud.enable);
        }
        if (dc.mqtt != null) {
            profile.settings.mqtt.enable = getBool(dc.mqtt.enable);
        }
        if (dc.sys.sntp != null) {
            profile.settings.sntp.server = dc.sys.sntp.server;
        }

        profile.isRoller = dc.cover0 != null; // SHELLY2_PROFILE_ROLLER.equalsIgnoreCase(getString(dc.sys.device.profile));
        profile.settings.relays = fillRelaySettings(profile, dc);
        profile.settings.inputs = fillInputSettings(profile, dc);
        profile.settings.rollers = fillRollerSettings(profile, dc);

        profile.isEMeter = true;
        profile.numInputs = profile.settings.inputs != null ? profile.settings.inputs.size() : 0;
        profile.numRelays = profile.settings.relays != null ? profile.settings.relays.size() : 0;
        profile.numRollers = profile.settings.rollers != null ? profile.settings.rollers.size() : 0;
        profile.hasRelays = profile.numRelays > 0 || profile.numRollers > 0;
        profile.mode = "";
        if (profile.hasRelays) {
            profile.mode = profile.isRoller ? SHELLY_CLASS_ROLLER : SHELLY_CLASS_RELAY;
        }

        ShellySettingsDevice device = getDeviceInfo();
        profile.settings.device = device;
        profile.hostname = device.hostname;
        profile.deviceType = device.type;
        profile.mac = device.mac;
        profile.auth = device.auth;

        profile.fwDate = substringBefore(device.fw, "/");
        profile.fwVersion = substringBefore(ShellyDeviceProfile.extractFwVersion(device.fw.replace("/", "/v")), "-");
        profile.status.hasUpdate = profile.status.update.hasUpdate = false;
        profile.status.update.oldVersion = profile.fwVersion;

        if (profile.hasRelays) {
            profile.status.relays = new ArrayList<>();
            profile.status.meters = new ArrayList<>();
            profile.status.emeters = new ArrayList<>();
            relayStatus.relays = new ArrayList<>();
            relayStatus.meters = new ArrayList<>();
            profile.numMeters = profile.isRoller ? profile.numRollers : profile.numRelays;
            for (int i = 0; i < profile.numMeters; i++) {
                profile.status.relays.add(new ShellySettingsRelay());
                profile.status.meters.add(new ShellySettingsMeter());
                profile.status.emeters.add(new ShellySettingsEMeter());
                relayStatus.relays.add(new ShellyShortStatusRelay());
                relayStatus.meters.add(new ShellySettingsMeter());
            }
        }

        profile.status.inputs = new ArrayList<>();
        for (int i = 0; i < profile.numInputs; i++) {
            ShellyInputState input = new ShellyInputState();
            input.input = 0;
            input.event = "";
            input.eventCount = 0;
            profile.status.inputs.add(input);
        }
        relayStatus.inputs = profile.status.inputs;

        profile.status.rollers = new ArrayList<>();
        for (int i = 0; i < profile.numRollers; i++) {
            ShellyRollerStatus rs = new ShellyRollerStatus();
            rs.calibrating = false;
            profile.status.rollers.add(rs);
            rollerStatus.add(rs);
        }

        profile.status.dimmers = profile.isDimmer ? new ArrayList<>() : null;
        profile.status.lights = profile.isBulb ? new ArrayList<>() : null;
        profile.status.thermostats = profile.isTRV ? new ArrayList<>() : null;

        if (rpc != null) {
            // request status update, this triggers WebSocket updates
            rpc.apiRequest(profile.hostname, SHELLYRPC_METHOD_GETSTATUS, "");
        }

        if (profile.hasBattery) {
            profile.settings.sleepMode = new ShellySensorSleepMode();
            profile.settings.sleepMode.unit = "m";
            profile.settings.sleepMode.period = dc.sys.sleep != null ? dc.sys.sleep.wakeupPeriod / 60 : 720;
            checkSetWsCallback();
        }

        profile.initialized = true;
        return profile;
    }

    private void checkSetWsCallback() throws ShellyApiException {
        Shelly2WsConfig wsConfig = callApi("/rpc/" + SHELLYRPC_METHOD_WSGETCONFIG, Shelly2WsConfig.class);
        ShellyThingConfiguration config = getThing().getThingConfig();
        String url = "ws://" + config.localIp + ":" + config.localPort + "/shelly/wsevent";
        if (!getBool(wsConfig.enable) || !url.equalsIgnoreCase(getString(wsConfig.server))) {
            logger.debug("{}: A battery device was detected without correct callback, fix it", thingName);
            wsConfig.enable = true;
            wsConfig.server = url;
            Shelly2RpcRequest request = new Shelly2RpcRequest();
            request.id = 0;
            request.method = SHELLYRPC_METHOD_WSSETCONFIG;
            request.params.config = wsConfig;
            Shelly2WsConfigResponse response = postApi("/rpc/", gson.toJson(request), Shelly2WsConfigResponse.class);
            if (response.result.restartRequired) {
                logger.info("{}: WebSocket callback was updated, device is restarting", thingName);
                getThing().getApi().deviceReboot();
                getThing().reinitializeThing();
            }
        }
    }

    @Override
    public void onConnect(boolean connected) {
        logger.debug("{}: WebSocket {}", thingName, connected ? "connected successful" : "failed to connect!");
    }

    @Override
    public void onMessage(String message) {
        logger.debug("{}: RPC message received: {}", thingName, message);
    }

    @Override
    public void onNotifyStatus(Shelly2RpcNotifyStatus message) {
        logger.debug("{}: NotifyStatus update received: {}", thingName, gson.toJson(message));
        try {
            getThing().setThingOnline();

            if (message.error != null) {
                logger.debug("{}: Error status received - {} {}", thingName, message.error.code, message.error.message);
            }

            if (message.params != null) {
                boolean updated = false;
                ShellyDeviceProfile profile = getProfile();
                ShellySettingsStatus status = profile.status;
                if (!profile.hasBattery && message.params.sys != null) {
                    status.uptime = getLong(message.params.sys.uptime);
                }

                if (message.params.sys.restartRequired) {
                    logger.warn("{}: Device requires restart to activate changes", thingName);
                }

                status.temperature = -999.0; // mark invalid
                if (message.params.switch0 != null) {
                    updated |= updateRelayStatus(status, message.params.switch0);
                    updated |= updateRelayStatus(status, message.params.switch1);
                    updated |= updateRelayStatus(status, message.params.switch2);
                    updated |= updateRelayStatus(status, message.params.switch3);
                }

                if (message.params.input0 != null) {
                    updated |= updateInputStatus(status, message.params, true);
                }

                if (message.params.cover0 != null) {
                    updated |= fillRollerStatus(status, message.params.cover0, true);
                }

                if (message.params.humidity0 != null) {
                    updateHumidityStatus(sdata, message.params.humidity0);
                }
                if (message.params.temperature0 != null) {
                    updateTemperatureStatus(sdata, message.params.temperature0);
                }
                if (message.params.devicepower0 != null) {
                    updateBatteryStatus(sdata, message.params.devicepower0);
                }
                if (status.temperature == -999.0) {
                    // no device temp available
                    status.temperature = null;
                } else {
                    updated |= updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                            toQuantityType(getDouble(status.tmp.tC), DIGITS_NONE, SIUnits.CELSIUS));
                }
                profile.status = status;

                if (profile.isSensor) {
                    updated |= updateSensors(getThing(), profile.status);
                }
            }
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to process status update", thingName, e);
        }
    }

    @Override
    public void onNotifyEvent(Shelly2RpcNotifyEvent message) {
        try {
            logger.debug("{}: NotifyEvent  received: {}", thingName, gson.toJson(message));
            ShellyDeviceProfile profile = getProfile();

            getThing().restartWatchdog();

            for (Shelly2NotifyEvent e : message.params.events) {
                switch (e.event) {
                    case SHELLY2_EVENT_BTNUP:
                    case SHELLY2_EVENT_BTNDOWN:
                        String bgroup = getProfile().getInputGroup(e.id);
                        updateChannel(bgroup, CHANNEL_INPUT + profile.getInputSuffix(e.id),
                                getOnOff(SHELLY2_EVENT_BTNDOWN.equals(getString(e.event))));
                        getThing().triggerButton(profile.getInputGroup(e.id), e.id,
                                getString(MAP_INPUT_EVENT_ID.get(e.event)));
                        break;

                    case SHELLY2_EVENT_1PUSH:
                    case SHELLY2_EVENT_2PUSH:
                    case SHELLY2_EVENT_3PUSH:
                    case SHELLY2_EVENT_LPUSH:
                    case SHELLY2_EVENT_SLPUSH:
                    case SHELLY2_EVENT_LSPUSH:
                        ShellyInputState input = profile.status.inputs.get(e.id);
                        input.event = getString(MAP_INPUT_EVENT_TYPE.get(e.event));
                        input.eventCount = getInteger(input.eventCount) + 1;
                        profile.status.inputs.set(e.id, input);
                        relayStatus.inputs.set(e.id, input);

                        String group = getProfile().getInputGroup(e.id);
                        updateChannel(group, CHANNEL_STATUS_EVENTTYPE + profile.getInputSuffix(e.id),
                                getStringType(input.event));
                        updateChannel(group, CHANNEL_STATUS_EVENTCOUNT + profile.getInputSuffix(e.id),
                                getDecimal(input.eventCount));
                        getThing().triggerButton(profile.getInputGroup(e.id), e.id,
                                getString(MAP_INPUT_EVENT_ID.get(e.event)));
                        break;
                    case SHELLY2_EVENT_CFGCHANGED:
                        logger.debug("{}: Configuration update detected, re-initialize", thingName);
                        getThing().requestUpdates(1, true); // refresh config
                        break;

                    case SHELLY2_EVENT_OTASTART:
                        getThing().postEvent(e.event, true);
                        getThing().setThingOffline(ThingStatusDetail.FIRMWARE_UPDATING,
                                "offline.status-error-fwupgrade");
                        break;
                    case SHELLY2_EVENT_OTAPROGRESS:
                        getThing().postEvent(e.event, false);
                        break;
                    case SHELLY2_EVENT_OTADONE:
                        getThing().setThingOffline(ThingStatusDetail.CONFIGURATION_PENDING,
                                "offline.status-error-restarted");
                        getThing().requestUpdates(1, true); // refresh config
                        break;
                    case SHELLY2_EVENT_SLEEP:
                        logger.debug("{}: Device went to sleep mode", thingName);
                        break;

                    default:
                        logger.debug("{}: Event {} was not handled", thingName, e.event);
                }
            }
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to process event", thingName, e);
        }
    }

    @Override
    public void onClose() {
        logger.debug("{}: RPC connection closed", thingName);
    }

    @Override
    public void onError(Throwable cause) {
        try {
            logger.debug("{}: WebSocket error, reinit thing", thingName);
            getThing().setThingOffline(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-unexpected-error");
            getThing().reinitializeThing();
        } catch (ShellyApiException e) {
            logger.debug("{}: WebSocket error", thingName, e);
        }
    }

    @Override
    public ShellySettingsDevice getDeviceInfo() throws ShellyApiException {
        Shelly2DeviceSettings device = callApi("/shelly", Shelly2DeviceSettings.class);
        ShellySettingsDevice info = new ShellySettingsDevice();
        info.hostname = getString(device.id);
        info.fw = getString(device.firmware);
        info.type = getString(device.model);
        info.mac = getString(device.mac);
        info.auth = getBool(device.authEnable);
        return info;
    }

    @Override
    public ShellySettingsStatus getStatus() throws ShellyApiException {
        Shelly2DeviceStatusResult ds = callApi("/rpc/Shelly." + SHELLYRPC_METHOD_GETSTATUS,
                Shelly2DeviceStatusResult.class);

        ShellySettingsStatus status = getProfile().status;
        status.time = getString(ds.sys.time);
        status.cloud.connected = getBool(ds.cloud.connected);
        status.mqtt.connected = getBool(ds.mqtt.connected);
        status.wifiSta.ssid = getString(ds.wifi.ssid);
        status.wifiSta.ip = getString(ds.wifi.staIP);
        status.wifiSta.rssi = getInteger(ds.wifi.rssi);
        status.fsFree = ds.sys.fsFree;
        status.fsSize = ds.sys.fsSize;
        status.discoverable = true;
        status.hasUpdate = status.update.hasUpdate = false;
        status.update.oldVersion = getProfile().fwVersion;
        if (ds.sys.availableUpdates != null) {
            status.update.hasUpdate = ds.sys.availableUpdates.stable != null;
            if (ds.sys.availableUpdates.stable != null) {
                status.update.newVersion = "v" + getString(ds.sys.availableUpdates.stable.version);
            }
            if (ds.sys.availableUpdates.beta != null) {
                status.update.betaVersion = "v" + getString(ds.sys.availableUpdates.beta.version);
            }
        }

        fillRelayStatus(status, ds);
        updateInputStatus(status, ds, false);
        fillRollerStatus(status, ds.cover0, false);

        return status;
    }

    @Override
    public void setLedStatus(String ledName, Boolean value) throws ShellyApiException {
    }

    @Override
    public void setSleepTime(int value) throws ShellyApiException {
    }

    @Override
    public ShellyStatusRelay getRelayStatus(int relayIndex) throws ShellyApiException {
        // Update status for a single relay
        getStatus();
        return relayStatus;
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

        return relayStatus;
    }

    @Override
    public void setRelayTurn(int id, String turnMode) throws ShellyApiException {
        callApi("/rpc/Switch.Set?id=" + id + "&on=" + (SHELLY_API_ON.equals(turnMode) ? "true" : "false"),
                String.class);
    }

    @Override
    public ShellyRollerStatus getRollerStatus(int rollerIndex) throws ShellyApiException {
        if (rollerIndex < rollerStatus.size()) {
            return rollerStatus.get(rollerIndex);
        }
        throw new IllegalArgumentException("Invalid rollerIndex on getRollerStatus");
    }

    @Override
    public void setRollerTurn(int relayIndex, String turnMode) throws ShellyApiException {
        String operation = "";
        switch (turnMode) {
            case SHELLY_ALWD_ROLLER_TURN_OPEN:
                operation = "Open";
                break;
            case SHELLY_ALWD_ROLLER_TURN_CLOSE:
                operation = "Close";
                break;
            case SHELLY_ALWD_ROLLER_TURN_STOP:
                operation = "Stop";
                break;
        }
        // callApi("/rpc/Cover." + operation + "?id=" + relayIndex, String.class);
        Shelly2RpcRequest request = new Shelly2RpcRequest().withMethod("Cover." + operation).withId(relayIndex);
        postApi("/rpc/", gson.toJson(request), String.class);
    }

    @Override
    public void setRollerPos(int relayIndex, int position) throws ShellyApiException {
        Shelly2RpcRequest request = new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_COVER_SETPOS).withId(relayIndex)
                .withPos(position);
        postApi("/rpc/", gson.toJson(request), String.class);
    }

    private @Nullable ArrayList<@Nullable ShellySettingsRelay> fillRelaySettings(ShellyDeviceProfile profile,
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
        rsettings.btnType = MAP_INMODE_BTNTYPE.containsKey(mode) ? MAP_INMODE_BTNTYPE.get(mode) : mode;
        relays.add(rsettings);
    }

    private void fillRelayStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult rs) throws ShellyApiException {
        updateRelayStatus(status, 0, rs.switch0);
        updateRelayStatus(status, 1, rs.switch1);
        updateRelayStatus(status, 2, rs.switch2);
        updateRelayStatus(status, 3, rs.switch3);
    }

    private boolean updateRelayStatus(ShellySettingsStatus status, @Nullable Shelly2RelayStatus rs)
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
            if (rs.voltage > status.voltage) {
                // status.voltage = rs.voltage;
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

        group = profile.getMeterGroup(rs.id);
        updated |= updateChannel(rs.apower != null, group, CHANNEL_METER_CURRENTWATTS,
                toQuantityType(rs.apower, DIGITS_WATT, Units.WATT));
        updated |= updateChannel(rs.voltage != null, group, CHANNEL_EMETER_VOLTAGE,
                toQuantityType(rs.voltage, DIGITS_VOLT, Units.VOLT));
        updated |= updateChannel(rs.current != null, group, CHANNEL_EMETER_CURRENT,
                toQuantityType(rs.current, DIGITS_VOLT, Units.AMPERE));
        updated |= updateChannel(rs.pf != null, group, CHANNEL_EMETER_PFACTOR, toQuantityType(rs.pf, Units.PERCENT));
        if (rs.aenergy != null) {
            updated |= updateChannel(rs.aenergy.total != null, group, CHANNEL_METER_TOTALKWH,
                    toQuantityType(getDouble(rs.aenergy.total) / 1000, DIGITS_KWH, Units.KILOWATT_HOUR));
            updated |= updateChannel(rs.aenergy.byMinute != null, group, CHANNEL_METER_LASTMIN1,
                    toQuantityType(rs.aenergy.byMinute[0], DIGITS_WATT, Units.WATT));
            updateChannel(rs.aenergy.minuteTs != null, group, CHANNEL_LAST_UPDATE,
                    getTimestamp(getString(profile.settings.timezone), rs.aenergy.minuteTs));
        } else {
            updateChannel(group, CHANNEL_LAST_UPDATE, getTimestamp());
        }
        return updated;
    }

    private @Nullable ArrayList<@Nullable ShellySettingsRoller> fillRollerSettings(ShellyDeviceProfile profile,
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
        settings.inputMode = MAP_INPUT_MODE.get(getString(coverConfig.inMode));
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

    private boolean fillRollerStatus(ShellySettingsStatus status, @Nullable Shelly2CoverStatus rs,
            boolean updateChannels) throws ShellyApiException {
        if (rs == null) {
            return false;
        }
        status.rollers.set(rs.id, updateRollerStatus(status, rs.id, rs));

        ShellySettingsMeter sm = status.meters.get(rs.id);
        sm.isValid = true;
        sm.power = rs.apower;
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

        return true;
    }

    private ShellyRollerStatus updateRollerStatus(ShellySettingsStatus status, int idx, Shelly2CoverStatus coverStatus)
            throws ShellyApiException {
        ShellyRollerStatus rs = new ShellyRollerStatus();
        rs.isValid = true;
        rs.power = coverStatus.apower;
        rs.state = MAP_ROLLER_STATE.get(getString(coverStatus.state));
        rs.calibrating = SHELLY2_RSTATE_CALIB.equals(rs.state);
        if (rs.calibrating) {
            getThing().postEvent(SHELLY_EVENT_ROLLER_CALIB, false);
        }
        rs.currentPos = coverStatus.current_pos;
        if (coverStatus.moveStartedAt != null) {
            rs.duration = (int) (now()
                    - getTimestamp(getString(getProfile().settings.timezone), coverStatus.moveStartedAt.longValue())
                            .getZonedDateTime().toEpochSecond());
        }
        if (coverStatus.temperature != null && coverStatus.temperature.tC > getDouble(status.temperature)) {
            status.temperature = status.tmp.tC = getDouble(coverStatus.temperature.tC);
        }

        postAlarms(coverStatus.errors);
        rollerStatus.set(idx, rs);
        return rs;
    }

    private void updateHumidityStatus(ShellyStatusSensor sdata, Shelly2DeviceStatusHumidity value) {
        if (sdata.hum == null) {
            sdata.hum = new ShellySensorHum();
        }
        sdata.hum.value = value.rh;
    }

    private void updateTemperatureStatus(ShellyStatusSensor sdata, Shelly2DeviceStatusTempId value) {
        if (sdata.tmp == null) {
            sdata.tmp = new ShellySensorTmp();
        }
        sdata.tmp.isValid = true;
        sdata.tmp.units = SHELLY_TEMP_CELSIUS;
        sdata.tmp.tC = value.tC;
        sdata.tmp.tF = value.tF;
    }

    private void updateBatteryStatus(ShellyStatusSensor sdata, Shelly2DeviceStatusPower value) {
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

    private @Nullable ArrayList<ShellySettingsInput> fillInputSettings(ShellyDeviceProfile profile,
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

    private boolean updateInputStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult ds, boolean updateChannels)
            throws ShellyApiException {
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

        if (updated) {
            updateChannel(group, CHANNEL_LAST_UPDATE, getTimestamp());
        }
        return updated;
    }

    private boolean updateChannel(String group, String channel, State value) throws ShellyApiException {
        return getThing().updateChannel(group, channel, value);
    }

    private boolean updateChannel(boolean condition, String group, String channel, State value)
            throws ShellyApiException {
        if (condition) {
            return updateChannel(group, channel, value);
        }
        return false;
    }

    private ShellyThingInterface getThing() throws ShellyApiException {
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

    @Override
    public ShellyStatusLight getLightStatus() throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public ShellyShortLightStatus getLightStatus(int index) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setLightParm(int lightIndex, String parm, String value) throws ShellyApiException {
    }

    @Override
    public ShellyShortLightStatus setLightTurn(int id, String turnMode) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setBrightness(int id, int brightness, boolean autoOn) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setLightMode(String mode) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setLightParms(int lightIndex, Map<String, String> parameters) throws ShellyApiException {
    }

    @Override
    public ShellyStatusSensor getSensorStatus() throws ShellyApiException {
        return sdata;
    }

    @Override
    public void setTimer(int index, String timerName, int value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValveMode(int id, boolean auto) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValvePosition(int id, double value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValveTemperature(int id, int value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValveBoostTime(int id, int value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void startValveBoost(int id, int value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public String resetStaCache() throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setActionURLs() throws ShellyApiException {
    }

    @Override
    public ShellySettingsLogin setCoIoTPeer(String peer) throws ShellyApiException {
        return new ShellySettingsLogin();
    }

    @Override
    public void sendIRKey(String keyCode) throws ShellyApiException, IllegalArgumentException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValveProfile(int id, int value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public ShellySettingsLogin getLoginSettings() throws ShellyApiException {
        return new ShellySettingsLogin();
    }

    @Override
    public ShellySettingsLogin setLoginCredentials(String user, String password) throws ShellyApiException {
        return new ShellySettingsLogin();
    }

    @Override
    public String setWiFiRecovery(boolean enable) throws ShellyApiException {
        return "failed";
    }

    @Override
    public String setApRoaming(boolean enable) throws ShellyApiException {
        return "false";
    }

    @Override
    public String setCloud(boolean enabled) throws ShellyApiException {
        return "failed";
    }

    @Override
    public String setDebug(boolean enabled) throws ShellyApiException {
        return "failed";
    }

    @Override
    public String getDebugLog(String id) throws ShellyApiException {
        return "";
    }

    @Override
    public String deviceReboot() throws ShellyApiException {
        String s = callApi("/rpc/" + SHELLYRPC_METHOD_REBOOT, String.class);
        return s;
    }

    @Override
    public String factoryReset() throws ShellyApiException {
        String s = callApi("/rpc/" + SHELLYRPC_METHOD_RESET, String.class);
        return s;
    }

    @Override
    public ShellyOtaCheckResult checkForUpdate() throws ShellyApiException {
        Shelly2DeviceStatusSysAvlUpdate status = callApi("/rpc/" + SHELLYRPC_METHOD_CHECKUPD,
                Shelly2DeviceStatusSysAvlUpdate.class);
        ShellyOtaCheckResult result = new ShellyOtaCheckResult();
        result.status = status.stable != null || status.beta != null ? "new" : "ok";
        return result;
    }

    @Override
    public String getCoIoTDescription() {
        return "";
    }
}
