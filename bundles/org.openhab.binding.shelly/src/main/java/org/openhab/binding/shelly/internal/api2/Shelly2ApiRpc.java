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
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_CAP_NUM_METERS;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyApiResult;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorSleepMode;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLogin;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRoller;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsUpdate;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsWiFiNetwork;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortStatusRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLightChannel;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2APClientList;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2AuthChallenge;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2ConfigParms;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DevConfigBle.Shelly2DevConfigBleObserver;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2DeviceConfigSta;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfigAp;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfigAp.Shelly2DeviceConfigApRE;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceSettings;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2RGBWStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusSys.Shelly2DeviceStatusSysAvlUpdate;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyStatus.Shelly2NotifyStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest.Shelly2RpcRequestParams;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2WsConfigResponse;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2WsConfigResponse.Shelly2WsConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.ShellyScriptListResponse;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.ShellyScriptListResponse.ShellyScriptListEntry;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.ShellyScriptPutCodeParams;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.ShellyScriptResponse;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.binding.shelly.internal.util.ShellyVersionDTO;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Shelly2ApiRpc} implements Gen2 RPC interface
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2ApiRpc extends Shelly2ApiClient implements ShellyApiInterface, Shelly2RpctInterface {
    private final Logger logger = LoggerFactory.getLogger(Shelly2ApiRpc.class);
    private final ShellyThingTable thingTable;

    protected boolean initialized = false;
    private boolean discovery = false;
    private Shelly2RpcSocket rpcSocket = new Shelly2RpcSocket();
    private @Nullable Shelly2AuthChallenge authInfo;

    // Plus devices support up to 3 scripts, Pro devices up to 10
    // We need to find a free script id when uploading our script
    // We want to limit script ids being checked, so define a max id
    private static final int MAX_SCRIPT_ID = 15;

    /**
     * Regular constructor - called by Thing handler
     *
     * @param thingName Symbolic thing name
     * @param thing Thing Handler (ThingHandlerInterface)
     */
    public Shelly2ApiRpc(String thingName, ShellyThingTable thingTable, ShellyThingInterface thing) {
        super(thingName, thing);
        this.thingName = thingName;
        this.thing = thing;
        this.thingTable = thingTable;
    }

    /**
     * Simple initialization - called by discovery handler
     *
     * @param thingName Symbolic thing name
     * @param config Thing Configuration
     * @param httpClient HTTP Client to be passed to ShellyHttpClient
     */
    public Shelly2ApiRpc(String thingName, ShellyThingConfiguration config, HttpClient httpClient) {
        super(thingName, config, httpClient);
        this.thingName = thingName;
        this.thingTable = new ShellyThingTable(); // create empty table;
        this.discovery = true;
    }

    @Override
    public void initialize() throws ShellyApiException {
        if (initialized) {
            logger.debug("{}: Disconnect Rpc Socket on initialize", thingName);
            disconnect();
        }
        rpcSocket = new Shelly2RpcSocket(thingName, thingTable, config.deviceIp);
        rpcSocket.addMessageHandler(this);
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void startScan() {
        try {
            if (getProfile().isBlu) {
                installScript(SHELLY2_BLU_GWSCRIPT, config.enableBluGateway);
            }
        } catch (ShellyApiException e) {
        }
    }

    @Override
    public ShellyDeviceProfile getDeviceProfile(ThingTypeUID thingTypeUID, @Nullable ShellySettingsDevice devInfo)
            throws ShellyApiException {
        ShellyDeviceProfile profile = thing != null ? getProfile() : new ShellyDeviceProfile();

        if (devInfo != null) {
            profile.device = devInfo;
        }
        if (profile.device.type == null) {
            profile.device = getDeviceInfo();
        }

        Shelly2GetConfigResult dc = apiRequest(SHELLYRPC_METHOD_GETCONFIG, null, Shelly2GetConfigResult.class);
        profile.settingsJson = gson.toJson(dc);
        profile.thingName = thingName;
        profile.settings.name = profile.status.name = dc.sys.device.name;
        profile.name = getString(profile.settings.name);
        profile.settings.timezone = getString(dc.sys.location.tz);
        profile.settings.discoverable = getBool(dc.sys.device.discoverable);
        if (dc.wifi != null && dc.wifi.ap != null && dc.wifi.ap.rangeExtender != null) {
            profile.settings.wifiAp.rangeExtender = getBool(dc.wifi.ap.rangeExtender.enable);
        }
        if (dc.cloud != null) {
            profile.settings.cloud.enabled = getBool(dc.cloud.enable);
        }
        if (dc.mqtt != null) {
            profile.settings.mqtt.enable = getBool(dc.mqtt.enable);
        }
        if (dc.sys.sntp != null) {
            profile.settings.sntp.server = dc.sys.sntp.server;
        }

        profile.isRoller = dc.cover0 != null;
        profile.settings.relays = !profile.isCB ? fillRelaySettings(profile, dc) : fillBreakerSettings(profile, dc);
        profile.settings.inputs = fillInputSettings(profile, dc);
        profile.settings.rollers = fillRollerSettings(profile, dc);
        profile.isCB = dc.cb0 != null || dc.cb1 != null || dc.cb2 != null || dc.cb3 != null;

        profile.isEMeter = true;
        List<ShellySettingsInput> inputs = profile.settings.inputs;
        profile.numInputs = inputs != null ? inputs.size() : 0;

        List<ShellySettingsRelay> relays = profile.settings.relays;
        profile.numRelays = relays != null ? relays.size() : 0;

        List<ShellySettingsRoller> rollers = profile.settings.rollers;
        profile.numRollers = rollers != null ? rollers.size() : 0;
        profile.hasRelays = profile.numRelays > 0 || profile.numRollers > 0;

        ShellySettingsDevice device = profile.device;
        if (config.serviceName.isBlank()) {
            config.serviceName = getString(profile.device.hostname);
            logger.trace("{}: {} is used as serviceName", thingName, config.serviceName);
        }
        profile.settings.fw = getString(device.fw);
        profile.fwDate = substringBefore(substringBefore(device.fw, "/"), "-");
        profile.fwVersion = profile.status.update.oldVersion = ShellyDeviceProfile
                .extractFwVersion(profile.settings.fw);
        profile.status.hasUpdate = profile.status.update.hasUpdate = false;

        if (dc.eth != null) {
            profile.settings.ethernet = getBool(dc.eth.enable);
        }
        if (dc.ble != null) {
            profile.settings.bluetooth = getBool(dc.ble.enable);
        }

        profile.settings.wifiSta = new ShellySettingsWiFiNetwork();
        profile.settings.wifiSta1 = new ShellySettingsWiFiNetwork();
        fillWiFiSta(dc.wifi.sta, profile.settings.wifiSta);
        fillWiFiSta(dc.wifi.sta1, profile.settings.wifiSta1);
        if (dc.wifi.ap != null && dc.wifi.ap.rangeExtender != null) {
            profile.settings.rangeExtender = getBool(dc.wifi.ap.rangeExtender.enable);
        }

        profile.numMeters = 0;
        if (profile.hasRelays) {
            profile.status.relays = new ArrayList<>();
            relayStatus.relays = new ArrayList<>();
            profile.numMeters = profile.isRoller ? profile.numRollers : profile.numRelays;
            for (int i = 0; i < profile.numRelays; i++) {
                profile.status.relays.add(new ShellySettingsRelay());
                relayStatus.relays.add(new ShellyShortStatusRelay());
            }
        }

        if (profile.numInputs > 0) {
            profile.status.inputs = new ArrayList<>();
            relayStatus.inputs = new ArrayList<>();
            for (int i = 0; i < profile.numInputs; i++) {
                ShellyInputState input = new ShellyInputState(i);
                profile.status.inputs.add(input);
                relayStatus.inputs.add(input);
            }
        }

        // handle special cases, because there is no indicator for a meter in GetConfig
        // Pro 3EM has 3 meters
        // Pro 2 has 2 relays, but no meters
        // Mini PM has 1 meter, but no relay
        Integer numMeters = THING_TYPE_CAP_NUM_METERS.get(thingTypeUID);
        if (numMeters != null) {
            profile.numMeters = numMeters;
        } else if (dc.pm10 != null) {
            profile.numMeters = 1;
        } else if (dc.em0 != null) {
            profile.numMeters = 3;
        } else if (dc.em10 != null) {
            profile.numMeters = 2;
        }

        if (profile.numMeters > 0) {
            profile.status.meters = new ArrayList<>();
            profile.status.emeters = new ArrayList<>();
            relayStatus.meters = new ArrayList<>();

            for (int i = 0; i < profile.numMeters; i++) {
                profile.status.meters.add(new ShellySettingsMeter());
                profile.status.emeters.add(new ShellySettingsEMeter());
                relayStatus.meters.add(new ShellySettingsMeter());
            }
        }

        if (profile.settings.inputs != null) {
            relayStatus.inputs = new ArrayList<>();
            for (int i = 0; i < profile.numInputs; i++) {
                relayStatus.inputs.add(new ShellyInputState(0));
            }
        }

        if (profile.isRoller) {
            profile.status.rollers = new ArrayList<>();
            for (int i = 0; i < profile.numRollers; i++) {
                ShellyRollerStatus rs = new ShellyRollerStatus();
                profile.status.rollers.add(rs);
                rollerStatus.add(rs);
            }
        }

        if (profile.isDimmer) {
            ArrayList<@Nullable ShellySettingsDimmer> dimmers = new ArrayList<>();
            dimmers.add(new ShellySettingsDimmer());
            profile.settings.dimmers = dimmers;
            profile.status.dimmers = new ArrayList<>();
            profile.status.dimmers.add(new ShellyShortLightStatus());
            fillDimmerSettings(profile, dc);
        }
        profile.status.lights = profile.isBulb ? new ArrayList<>() : null;
        if (profile.isRGBW2) {
            ArrayList<ShellySettingsRgbwLight> rgbwLights = new ArrayList<>();
            rgbwLights.add(new ShellySettingsRgbwLight());
            profile.settings.lights = rgbwLights;
            profile.status.lights = new ArrayList<>();
            profile.status.lights.add(new ShellySettingsLight());
            fillRgbwSettings(profile, dc);
        }
        profile.status.thermostats = profile.isTRV ? new ArrayList<>() : null;

        if (profile.hasBattery) {
            profile.settings.sleepMode = new ShellySensorSleepMode();
            profile.settings.sleepMode.unit = "m";
            profile.settings.sleepMode.period = dc.sys.sleep != null ? dc.sys.sleep.wakeupPeriod / 60 : 720;
            checkSetWsCallback();
        }

        if (dc.led != null) {
            profile.settings.ledStatusDisable = !getBool(dc.led.sysLedEnable);
            profile.settings.ledPowerDisable = "off".equals(getString(dc.led.powerLed));
        }

        profile.initialized = true;
        if (!discovery) {
            getStatus(); // make sure profile.status is initialized (e.g,. relay/meter status)
            asyncApiRequest(SHELLYRPC_METHOD_GETSTATUS); // request periodic status updates from device

            try {
                if (profile.alwaysOn && dc.ble != null) {
                    logger.debug("{}: BLU Gateway support is {} for this device", thingName,
                            config.enableBluGateway ? "enabled" : "disabled");
                    if (config.enableBluGateway) {
                        boolean bluetooth = getBool(dc.ble.enable);
                        boolean observer = dc.ble.observer != null && getBool(dc.ble.observer.enable);
                        if (!bluetooth) {
                            logger.warn("{}: Bluetooth will be enabled to activate BLU Gateway mode", thingName);
                        }
                        if (observer) {
                            logger.warn("{}: Shelly Cloud Bluetooth Gateway conflicts with openHAB, disabling it",
                                    thingName);
                        }
                        boolean restart = false;
                        if (!bluetooth || observer) {
                            logger.info("{}: Setup openHAB BLU Gateway", thingName);
                            restart = setBluetooth(true);
                        }

                        installScript(SHELLY2_BLU_GWSCRIPT, config.enableBluGateway && bluetooth);

                        if (restart) {
                            logger.info("{}: Restart device to activate BLU Gateway", thingName);
                            deviceReboot();
                            getThing().reinitializeThing();
                        }
                    }
                }
            } catch (ShellyApiException e) {
                logger.debug("{}: Device config failed", thingName, e);
            }
        }

        return profile;
    }

    private void fillWiFiSta(@Nullable Shelly2DeviceConfigSta from, ShellySettingsWiFiNetwork to) {
        to.enabled = from != null && !getString(from.ssid).isEmpty();
        if (from != null) {
            to.ssid = from.ssid;
            to.ip = from.ip;
            to.mask = from.netmask;
            to.dns = from.nameserver;
        }
    }

    private void checkSetWsCallback() throws ShellyApiException {
        Shelly2ConfigParms wsConfig = apiRequest(SHELLYRPC_METHOD_WSGETCONFIG, null, Shelly2ConfigParms.class);
        String url = "ws://" + config.localIp + ":" + config.localPort + "/shelly/wsevent";
        if (!config.localIp.isEmpty() && !getBool(wsConfig.enable)
                || !url.equalsIgnoreCase(getString(wsConfig.server))) {
            logger.debug("{}: A battery device was detected without correct callback, fix it", thingName);
            wsConfig.enable = true;
            wsConfig.server = url;
            Shelly2RpcRequest request = new Shelly2RpcRequest();
            request.id = 0;
            request.method = SHELLYRPC_METHOD_WSSETCONFIG;
            request.params.config = wsConfig;
            Shelly2WsConfigResponse response = apiRequest(SHELLYRPC_METHOD_WSSETCONFIG, request.params,
                    Shelly2WsConfigResponse.class);
            if (response.result != null && response.result.restartRequired) {
                logger.info("{}: WebSocket callback was updated, device is restarting", thingName);
                getThing().getApi().deviceReboot();
                getThing().reinitializeThing();
            }
        }
    }

    protected void installScript(String script, boolean install) throws ShellyApiException {
        try {
            ShellyScriptListResponse scriptList = apiRequest(
                    new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_SCRIPT_LIST), ShellyScriptListResponse.class);
            Integer ourId = -1;
            String code = "";

            if (install) {
                logger.debug("{}: Install or restart script {} on Shelly Device", thingName, script);
            }
            boolean running = false, upload = false;
            for (ShellyScriptListEntry s : scriptList.scripts) {
                if (s.name.startsWith(script)) {
                    ourId = s.id;
                    running = s.running;
                    logger.debug("{}: Script {} is already installed, id={}", thingName, script, ourId);
                    break;
                }
            }

            if (!install) {
                if (ourId != -1) {
                    startScript(ourId, false);
                    enableScript(script, ourId, false);
                    deleteScript(ourId);
                    logger.debug("{}: Script {} was disabled, id={}", thingName, script, ourId);
                }
                return;
            }

            // get script code from bundle resources
            String file = BUNDLE_RESOURCE_SCRIPTS + "/" + script;
            ClassLoader cl = Shelly2ApiRpc.class.getClassLoader();
            if (cl != null) {
                try (InputStream inputStream = cl.getResourceAsStream(file)) {
                    if (inputStream != null) {
                        code = new BufferedReader(new InputStreamReader(inputStream)).lines()
                                .collect(Collectors.joining("\n"));
                    }
                } catch (IOException | UncheckedIOException e) {
                    logger.debug("{}: Installation of script {} failed: Unable to read {} from bundle resources!",
                            thingName, script, file, e);
                }
            }

            boolean restart = false;
            if (ourId == -1) {
                // script not installed -> install it
                upload = true;
            } else {
                try {
                    // verify that the same code version is active (avoid unnesesary flash updates)
                    ShellyScriptResponse rsp = apiRequest(
                            new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_SCRIPT_GETCODE).withId(ourId),
                            ShellyScriptResponse.class);
                    if (!rsp.data.trim().equals(code.trim())) {
                        logger.debug("{}: A script version was found, update to newest one", thingName);
                        upload = true;
                    } else {
                        logger.debug("{}: Same script version was found, restart", thingName);
                        restart = true;
                    }
                } catch (ShellyApiException e) {
                    logger.debug("{}: Unable to read current script code -> force update (deviced returned: {})",
                            thingName, e.getMessage());
                    upload = true;
                }
            }

            if (restart || (running && upload)) {
                // first stop running script
                startScript(ourId, false);
                running = false;
            }
            if (upload && ourId != -1) {
                // Delete existing script
                deleteScript(ourId);
            }

            if (upload) {
                logger.debug("{}: Script will be installed...", thingName);

                if (ourId == -1) {
                    // find free script id
                    ourId = 0;
                    for (ourId = 1; ourId <= MAX_SCRIPT_ID && testScriptId(scriptList, ourId); ourId++) {
                    }
                }
                if (ourId <= MAX_SCRIPT_ID) {
                    // Create new script, get id
                    ShellyScriptResponse rsp = apiRequest(new Shelly2RpcRequest()
                            .withMethod(SHELLYRPC_METHOD_SCRIPT_CREATE).withId(ourId).withName(script),
                            ShellyScriptResponse.class);
                    ourId = rsp.id;
                    logger.debug("{}: Script has been created, id={}", thingName, ourId);
                    upload = true;
                } else {
                    logger.debug("{}: Too many scripts installed on this device", thingName);
                    upload = false;
                }
            }

            if (upload) {
                // Put script code for generated id
                ShellyScriptPutCodeParams parms = new ShellyScriptPutCodeParams();
                parms.id = ourId;
                parms.append = false;
                int length = code.length(), processed = 0, chunk = 1;
                do {
                    int nextlen = Math.min(1024, length - processed);
                    parms.code = code.substring(processed, processed + nextlen);
                    logger.debug("{}: Uploading chunk {} of script (total {} chars, {} processed)", thingName, chunk,
                            length, processed);
                    apiRequest(SHELLYRPC_METHOD_SCRIPT_PUTCODE, parms, String.class);
                    processed += nextlen;
                    chunk++;
                    parms.append = true;
                } while (processed < length);
                running = false;
            }
            if (enableScript(script, ourId, true) && upload) {
                logger.info("{}: Script {} was {} installed successful", thingName, thingName, script);
            }

            if (!running) {
                running = startScript(ourId, true);
                logger.debug("{}: Script {} {}", thingName, script,
                        running ? "was successfully started" : "failed to start");
            }
        } catch (ShellyApiException e) {
            ShellyApiResult res = e.getApiResult();
            if (res.httpCode == HttpStatus.NOT_FOUND_404) { // Shely 4Pro
                logger.debug("{}: Script {} was not installed, device doesn't support scripts", thingName, script);
            } else {
                logger.debug("{}: Unable to install script {}: {}", thingName, script, res.toString());
            }
        }
    }

    private boolean testScriptId(ShellyScriptListResponse scriptList, int id) {
        for (ShellyScriptListEntry s : scriptList.scripts) {
            if (s.id == id) {
                return true;
            }
        }
        return false;
    }

    private boolean startScript(int ourId, boolean start) {
        if (ourId != -1) {
            try {
                apiRequest(new Shelly2RpcRequest()
                        .withMethod(start ? SHELLYRPC_METHOD_SCRIPT_START : SHELLYRPC_METHOD_SCRIPT_STOP)
                        .withId(ourId));
                return true;
            } catch (ShellyApiException e) {
            }
        }
        return false;
    }

    private boolean enableScript(String script, int scriptId, boolean enable) {
        try {
            Shelly2RpcRequestParams params = new Shelly2RpcRequestParams().withConfig();
            params.id = scriptId;
            params.config.name = script;
            params.config.enable = enable;
            apiRequest(SHELLYRPC_METHOD_SCRIPT_SETCONFIG, params, String.class);
            return true;
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to enable script {}", thingName, script, e);
            return false;
        }
    }

    private boolean deleteScript(int id) {
        if (id == -1) {
            throw new IllegalArgumentException("Invalid Script Id");
        }
        try {
            logger.debug("{}: Delete existing script with id{}", thingName, id);
            apiRequest(new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_SCRIPT_DELETE).withId(id));
            return true;
        } catch (ShellyApiException e) {
            logger.debug("{}: Unable to delete script with id {}", thingName, id);
        }
        return false;
    }

    @Override
    public void onConnect(String deviceIp, boolean connected) {
        ShellyThingTable thingTable = this.thingTable;
        thing = thingTable.getThing(deviceIp);
        logger.debug("{}: Get thing from thingTable", thingName);
    }

    @Override
    public void onNotifyStatus(Shelly2RpcNotifyStatus message) throws ShellyApiException {
        logger.debug("{}: NotifyStatus update received: {}", thingName, gson.toJson(message));
        ShellyThingInterface t = thing;
        if (t == null) {
            logger.debug("{}: No matching thing on NotifyStatus for {}, ignore (src={}, dst={}, discovery={})",
                    thingName, thingName, message.src, message.dst, discovery);
            return;
        }
        if (t.isStopping()) {
            logger.debug("{}: Thing is shutting down, ignore WebSocket message", thingName);
            return;
        }
        if (!t.isThingOnline() && t.getThingStatusDetail() != ThingStatusDetail.CONFIGURATION_PENDING) {
            logger.debug("{}: Thing is not in online state/connectable, ignore NotifyStatus", thingName);
            return;
        }

        getThing().incProtMessages();
        if (message.error != null) {
            if (message.error.code == HttpStatus.UNAUTHORIZED_401 && !getString(message.error.message).isEmpty()) {
                // Save nonce for notification
                Shelly2AuthChallenge auth = gson.fromJson(message.error.message, Shelly2AuthChallenge.class);
                if (auth != null && auth.realm == null) {
                    logger.debug("{}: Authentication data received: {}", thingName, message.error.message);
                    authInfo = auth;
                }
            } else {
                logger.debug("{}: Error status received - {} {}", thingName, message.error.code, message.error.message);
                incProtErrors();
            }
        }

        Shelly2NotifyStatus params = message.params;
        if (params != null) {
            if (getThing().getThingStatusDetail() != ThingStatusDetail.FIRMWARE_UPDATING) {
                getThing().setThingOnline();
            }

            boolean updated = false;
            ShellyDeviceProfile profile = getProfile();
            ShellySettingsStatus status = profile.status;
            if (params.sys != null) {
                if (getBool(params.sys.restartRequired)) {
                    logger.warn("{}: Device requires restart to activate changes", thingName);
                }
                status.uptime = params.sys.uptime;
            }
            status.temperature = SHELLY_API_INVTEMP; // mark invalid
            updated |= fillDeviceStatus(status, message.params, true);
            if (getDouble(status.temperature) == SHELLY_API_INVTEMP) {
                // no device temp available
                status.temperature = null;
            } else {
                if (status.tmp != null) {
                    updated |= updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                            toQuantityType(getDouble(status.tmp.tC), DIGITS_NONE, SIUnits.CELSIUS));
                }
            }

            profile.status = status;
            if (updated) {
                getThing().restartWatchdog();
            }
        }
    }

    @Override
    public void onNotifyEvent(Shelly2RpcNotifyEvent message) throws ShellyApiException {
        logger.debug("{}: NotifyEvent  received: {}", thingName, gson.toJson(message));
        ShellyDeviceProfile profile = getProfile();

        getThing().incProtMessages();
        getThing().restartWatchdog();

        for (Shelly2NotifyEvent e : message.params.events) {
            switch (e.event) {
                case SHELLY2_EVENT_BTNUP:
                case SHELLY2_EVENT_BTNDOWN:
                    String bgroup = getProfile().getInputGroup(e.id);
                    updateChannel(bgroup, CHANNEL_INPUT + profile.getInputSuffix(e.id),
                            getOnOff(SHELLY2_EVENT_BTNDOWN.equals(getString(e.event))));
                    getThing().triggerButton(profile.getInputGroup(e.id), e.id, mapValue(MAP_INPUT_EVENT_ID, e.event));
                    break;

                case SHELLY2_EVENT_1PUSH:
                case SHELLY2_EVENT_2PUSH:
                case SHELLY2_EVENT_3PUSH:
                case SHELLY2_EVENT_LPUSH:
                case SHELLY2_EVENT_SLPUSH:
                case SHELLY2_EVENT_LSPUSH:
                    if (e.id < profile.numInputs) {
                        ShellyInputState input = relayStatus.inputs.get(e.id);
                        input.event = getString(MAP_INPUT_EVENT_TYPE.get(e.event));
                        input.eventCount = getInteger(input.eventCount) + 1;
                        relayStatus.inputs.set(e.id, input);
                        profile.status.inputs.set(e.id, input);

                        String group = getProfile().getInputGroup(e.id);
                        updateChannel(group, CHANNEL_STATUS_EVENTTYPE + profile.getInputSuffix(e.id),
                                getStringType(input.event));
                        updateChannel(group, CHANNEL_STATUS_EVENTCOUNT + profile.getInputSuffix(e.id),
                                getDecimal(input.eventCount));
                        getThing().triggerButton(profile.getInputGroup(e.id), e.id,
                                mapValue(MAP_INPUT_EVENT_ID, e.event));
                    }
                    break;
                case SHELLY2_EVENT_CFGCHANGED:
                    logger.debug("{}: Configuration update detected, re-initialize", thingName);
                    getThing().requestUpdates(1, true); // refresh config
                    break;

                case SHELLY2_EVENT_OTASTART:
                    logger.debug("{}: Firmware update started: {}", thingName, getString(e.msg));
                    getThing().setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.FIRMWARE_UPDATING,
                            "offline.status-error-fwupgrade");
                    break;
                case SHELLY2_EVENT_OTAPROGRESS:
                    logger.debug("{}: Firmware update in progress: {}", thingName, getString(e.msg));
                    break;
                case SHELLY2_EVENT_OTADONE:
                    logger.debug("{}: Firmware update completed with status {}", thingName, getString(e.msg));
                    getThing().setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE,
                            "message.offline.status-error-fwcompleted");
                    break;
                case SHELLY2_EVENT_RESTART:
                    logger.debug("{}: Device was restarted: {}", thingName, getString(e.msg));
                    getThing().setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE,
                            "offline.status-error-restarted");
                    getThing().postEvent(ALARM_TYPE_RESTARTED, true);
                    break;
                case SHELLY2_EVENT_SLEEP:
                    logger.debug("{}: Connection terminated, e.g. device in sleep mode", thingName);
                    break;
                case SHELLY2_EVENT_WIFICONNFAILED:
                    logger.debug("{}: WiFi connect failed, check setup, reason {}", thingName, getInteger(e.reason));
                    getThing().postEvent(e.event, false);
                    break;
                case SHELLY2_EVENT_WIFIDISCONNECTED:
                    logger.debug("{}: WiFi disconnected, reason {}", thingName, getInteger(e.reason));
                    getThing().postEvent(e.event, false);
                    break;
                default:
                    logger.debug("{}: Event {} was not handled", thingName, e.event);
            }
        }
    }

    @Override
    public void onMessage(String message) {
        logger.debug("{}: Unexpected RPC message received: {}", thingName, message);
        incProtErrors();
    }

    @Override
    public void onClose(int statusCode, String description) {
        try {
            String reason = getString(description);
            logger.debug("{}: WebSocket connection closed, status = {}/{}", thingName, statusCode, reason);
            if ("Bye".equalsIgnoreCase(reason)) {
                logger.debug("{}: Device went to sleep mode or was restarted", thingName);
            } else if (statusCode == StatusCode.ABNORMAL && !discovery && getProfile().alwaysOn) {
                // e.g. device rebooted
                if (getThing().getThingStatusDetail() != ThingStatusDetail.DUTY_CYCLE) {
                    thingOffline("WebSocket connection closed abnormally");
                }
            }
        } catch (ShellyApiException e) {
            logger.debug("{}: Exception on onClose()", thingName, e);
            incProtErrors();
        }
    }

    @Override
    public void onError(Throwable cause) {
        logger.debug("{}: WebSocket error: {}", thingName, cause.getMessage());
        ShellyThingInterface thing = this.thing;
        if (thing != null && thing.getProfile().alwaysOn) {
            thingOffline("WebSocket error");
        }
    }

    private void thingOffline(String reason) {
        ShellyThingInterface thing = this.thing;
        if (thing != null) { // do not reinit of battery powered devices with sleep mode
            thing.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR,
                    "offline.status-error-unexpected-error", reason);
        }
    }

    @Override
    public ShellySettingsDevice getDeviceInfo() throws ShellyApiException {
        Shelly2DeviceSettings device = callApi("/shelly", Shelly2DeviceSettings.class);
        ShellySettingsDevice info = new ShellySettingsDevice();
        info.hostname = getString(device.id);
        info.name = getString(device.name);
        info.fw = getString(device.fw);
        info.type = getString(device.model);
        info.mac = getString(device.mac);
        info.auth = getBool(device.auth);
        info.gen = getInteger(device.gen);
        info.mode = mapValue(MAP_PROFILE, getString(device.profile));
        return info;
    }

    @Override
    public ShellySettingsStatus getStatus() throws ShellyApiException {
        ShellyDeviceProfile profile = getProfile();
        ShellySettingsStatus status = profile.status;
        Shelly2DeviceStatusResult ds = apiRequest(SHELLYRPC_METHOD_GETSTATUS, null, Shelly2DeviceStatusResult.class);
        status.time = ds.sys.time;
        status.uptime = ds.sys.uptime;
        status.cloud.connected = getBool(ds.cloud.connected);
        status.mqtt.connected = getBool(ds.mqtt.connected);
        status.wifiSta.ssid = getString(ds.wifi.ssid);
        status.wifiSta.enabled = !status.wifiSta.ssid.isEmpty();
        status.wifiSta.ip = getString(ds.wifi.staIP);
        status.wifiSta.rssi = getInteger(ds.wifi.rssi);
        status.fsFree = ds.sys.fsFree;
        status.fsSize = ds.sys.fsSize;
        status.discoverable = getBool(profile.settings.discoverable);

        if (ds.sys.wakeupPeriod != null) {
            profile.settings.sleepMode.period = ds.sys.wakeupPeriod / 60;
        }

        if (ds.sys.availableUpdates != null) {
            status.update.hasUpdate = ds.sys.availableUpdates.stable != null;
            if (ds.sys.availableUpdates.stable != null) {
                status.update.newVersion = ShellyDeviceProfile
                        .extractFwVersion(getString(ds.sys.availableUpdates.stable.version));
                status.hasUpdate = new ShellyVersionDTO().compare(profile.fwVersion, status.update.newVersion) < 0;
            }
            if (ds.sys.availableUpdates.beta != null) {
                status.update.betaVersion = ShellyDeviceProfile
                        .extractFwVersion(getString(ds.sys.availableUpdates.beta.version));
                status.hasUpdate = new ShellyVersionDTO().compare(profile.fwVersion, status.update.betaVersion) < 0;
            }
        }

        if (ds.sys.wakeUpReason != null && ds.sys.wakeUpReason.boot != null)

        {
            List<Object> values = new ArrayList<>();
            String boot = getString(ds.sys.wakeUpReason.boot);
            String cause = getString(ds.sys.wakeUpReason.cause);

            // Index 0 is aggregated status, 1 boot, 2 cause
            String reason = boot.equals(SHELLY2_WAKEUPO_BOOT_RESTART) ? ALARM_TYPE_RESTARTED : cause;
            values.add(reason);
            values.add(ds.sys.wakeUpReason.boot);
            values.add(ds.sys.wakeUpReason.cause);
            getThing().updateWakeupReason(values);
        }

        fillDeviceStatus(status, ds, false);
        if (getBool(profile.settings.rangeExtender)) {
            try {
                // Get List of AP clients
                profile.status.rangeExtender = apiRequest(SHELLYRPC_METHOD_WIFILISTAPCLIENTS, null,
                        Shelly2APClientList.class);
                logger.debug("{}: Range extender is enabled, {} clients connected", thingName,
                        profile.status.rangeExtender.apClients.size());
            } catch (ShellyApiException e) {
                logger.debug("{}: Range extender is enabled, but unable to read AP client list", thingName, e);
                profile.settings.rangeExtender = false;
            }
        }

        return status;
    }

    @Override
    public void setSleepTime(int value) throws ShellyApiException {
    }

    @Override
    public ShellyStatusRelay getRelayStatus(int relayIndex) throws ShellyApiException {
        if (getProfile().status.wifiSta.ssid == null) {
            // Update status when not yet initialized
            getStatus();
        }
        return relayStatus;
    }

    @Override
    public void setRelayTurn(int id, String turnMode) throws ShellyApiException {
        ShellyDeviceProfile profile = getProfile();
        int rIdx = id;
        List<ShellySettingsRelay> relays = profile.settings.relays;
        if (relays != null) {
            Integer rid = relays.get(id).id;
            if (rid != null) {
                rIdx = rid;
            }
        }
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams();
        String method = "";
        params.id = rIdx;
        if (!profile.isCB) {
            method = SHELLYRPC_METHOD_SWITCH_SET;
            params.on = SHELLY_API_ON.equals(turnMode);
        } else {
            method = SHELLYRPC_METHOD_CB_SET;
            params.output = SHELLY_API_ON.equals(turnMode);
        }
        apiRequest(method, params, String.class);
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
                operation = SHELLY2_COVER_CMD_OPEN;
                break;
            case SHELLY_ALWD_ROLLER_TURN_CLOSE:
                operation = SHELLY2_COVER_CMD_CLOSE;
                break;
            case SHELLY_ALWD_ROLLER_TURN_STOP:
                operation = SHELLY2_COVER_CMD_STOP;
                break;
        }

        apiRequest(new Shelly2RpcRequest().withMethod("Cover." + operation).withId(relayIndex));
    }

    @Override
    public void setRollerPos(int relayIndex, int position) throws ShellyApiException {
        apiRequest(
                new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_COVER_SETPOS).withId(relayIndex).withPos(position));
    }

    @Override
    public ShellyStatusLight getLightStatus() throws ShellyApiException {
        ShellyDeviceProfile profile = getProfile();
        if (profile.isRGBW2) {
            Shelly2RGBWStatus ls = apiRequest(
                    new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_RGBW_STATUS).withId(0),
                    Shelly2RGBWStatus.class);
            ShellyStatusLightChannel lightChannel = new ShellyStatusLightChannel();
            lightChannel.red = ls.rgb[0];
            lightChannel.green = ls.rgb[1];
            lightChannel.blue = ls.rgb[2];
            lightChannel.white = ls.white;
            lightChannel.brightness = ls.brightness.intValue();

            ShellyStatusLight status = new ShellyStatusLight();
            status.lights = new ArrayList<>();
            status.lights.add(lightChannel);
            status.ison = ls.output;

            return status;
        }

        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public ShellyShortLightStatus getLightStatus(int index) throws ShellyApiException {
        ShellyShortLightStatus status = new ShellyShortLightStatus();
        Shelly2DeviceStatusLight ls = apiRequest(
                new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_LIGHT_STATUS).withId(index),
                Shelly2DeviceStatusLight.class);
        status.ison = ls.output;
        status.hasTimer = ls.timerStartedAt != null;
        status.timerDuration = getDuration(ls.timerStartedAt, ls.timerDuration);
        if (ls.brightness != null) {
            status.brightness = ls.brightness.intValue();
        }
        return status;
    }

    @Override
    public void setBrightness(int id, int brightness, boolean autoOn) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams();
        params.id = id;
        params.brightness = brightness;
        params.on = brightness > 0;
        apiRequest(SHELLYRPC_METHOD_LIGHT_SET, params, String.class);
    }

    @Override
    public ShellyShortLightStatus setLightTurn(int id, String turnMode) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams();
        params.id = id;
        params.on = turnMode.equals(SHELLY_API_ON);
        apiRequest(SHELLYRPC_METHOD_LIGHT_SET, params, String.class);
        return getLightStatus(id);
    }

    @Override
    public ShellyStatusSensor getSensorStatus() throws ShellyApiException {
        return sensorData;
    }

    @Override
    public void setAutoTimer(int index, String timerName, double value) throws ShellyApiException {
        ShellyDeviceProfile profile = getProfile();
        boolean isLight = profile.isLight || profile.isDimmer;
        String method = isLight ? SHELLYRPC_METHOD_LIGHT_SETCONFIG : SHELLYRPC_METHOD_SWITCH_SETCONFIG;
        String component = isLight ? "Light" : "Switch";
        Shelly2RpcRequest req = new Shelly2RpcRequest().withMethod(method).withId(index);
        req.params.withConfig();
        req.params.config.name = component + index;
        if (timerName.equals(SHELLY_TIMER_AUTOON)) {
            req.params.config.autoOn = value > 0;
            req.params.config.autoOnDelay = value;
        } else {
            req.params.config.autoOff = value > 0;
            req.params.config.autoOffDelay = value;
        }
        apiRequest(req);
    }

    @Override
    public void setLedStatus(String ledName, boolean value) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams().withConfig();
        params.id = 0;
        if (ledName.equals(SHELLY_LED_STATUS_DISABLE)) {
            params.config.sysLedEnable = value;
        } else if (ledName.equals(SHELLY_LED_POWER_DISABLE)) {
            params.config.powerLed = value ? SHELLY2_POWERLED_OFF : SHELLY2_POWERLED_MATCH;
        } else {
            throw new ShellyApiException("API call not implemented for this LED type");
        }
        apiRequest(SHELLYRPC_METHOD_LED_SETCONFIG, params, Shelly2WsConfigResult.class);
    }

    @Override
    public void resetMeterTotal(int id) throws ShellyApiException {
        apiRequest(new Shelly2RpcRequest()
                .withMethod(getProfile().is3EM ? SHELLYRPC_METHOD_EMDATARESET : SHELLYRPC_METHOD_EM1DATARESET)
                .withId(id));
    }

    @Override
    public void muteSmokeAlarm(int index) throws ShellyApiException {
        apiRequest(new Shelly2RpcRequest().withMethod(SHELLYRPC_METHOD_SMOKE_MUTE).withId(index));
    }

    @Override
    public ShellySettingsLogin getLoginSettings() throws ShellyApiException {
        return new ShellySettingsLogin();
    }

    @Override
    public ShellySettingsLogin setLoginCredentials(String user, String password) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams();
        params.user = "admin";
        params.realm = config.serviceName;
        params.ha1 = sha256(params.user + ":" + params.realm + ":" + password);
        apiRequest(SHELLYRPC_METHOD_AUTHSET, params, String.class);

        ShellySettingsLogin res = new ShellySettingsLogin();
        res.enabled = true;
        res.username = params.user;
        res.password = password;
        return new ShellySettingsLogin();
    }

    @Override
    public boolean setWiFiRangeExtender(boolean enable) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams().withConfig();
        params.config.ap = new Shelly2DeviceConfigAp();
        params.config.ap.rangeExtender = new Shelly2DeviceConfigApRE();
        params.config.ap.rangeExtender.enable = enable;
        Shelly2WsConfigResult res = apiRequest(SHELLYRPC_METHOD_WIFISETCONG, params, Shelly2WsConfigResult.class);
        return res.restartRequired;
    }

    @Override
    public boolean setEthernet(boolean enable) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams().withConfig();
        params.config.enable = enable;
        Shelly2WsConfigResult res = apiRequest(SHELLYRPC_METHOD_ETHSETCONG, params, Shelly2WsConfigResult.class);
        return res.restartRequired;
    }

    @Override
    public boolean setBluetooth(boolean enable) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams().withConfig();
        params.config.enable = enable;
        if (enable) {
            params.config.observer = new Shelly2DevConfigBleObserver();
            params.config.observer.enable = false;
        }
        Shelly2WsConfigResult res = apiRequest(SHELLYRPC_METHOD_BLESETCONG, params, Shelly2WsConfigResult.class);
        return res.restartRequired;
    }

    @Override
    public void deviceReboot() throws ShellyApiException {
        apiRequest(SHELLYRPC_METHOD_REBOOT, null, String.class);
    }

    @Override
    public String factoryReset() throws ShellyApiException {
        return apiRequest(SHELLYRPC_METHOD_RESET, null, String.class);
    }

    @Override
    public ShellyOtaCheckResult checkForUpdate() throws ShellyApiException {
        Shelly2DeviceStatusSysAvlUpdate status = apiRequest(SHELLYRPC_METHOD_CHECKUPD, null,
                Shelly2DeviceStatusSysAvlUpdate.class);
        ShellyOtaCheckResult result = new ShellyOtaCheckResult();
        result.status = status.stable != null || status.beta != null ? "new" : "ok";
        return result;
    }

    @Override
    public ShellySettingsUpdate firmwareUpdate(String fwurl) throws ShellyApiException {
        ShellySettingsUpdate res = new ShellySettingsUpdate();
        boolean prod = fwurl.contains("update");
        boolean beta = fwurl.contains("beta");

        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams();
        if (prod || beta) {
            params.stage = prod ? "stable" : "beta";
        } else {
            params.url = fwurl;
        }
        apiRequest(SHELLYRPC_METHOD_UPDATE, params, String.class);
        res.status = "Update initiated";
        return res;
    }

    @Override
    public String setCloud(boolean enable) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams().withConfig();
        params.config.enable = enable;
        Shelly2WsConfigResult res = apiRequest(SHELLYRPC_METHOD_CLOUDSET, params, Shelly2WsConfigResult.class);
        return res.restartRequired ? "restart required" : "ok";
    }

    @Override
    public String setDebug(boolean enabled) throws ShellyApiException {
        return "failed";
    }

    @Override
    public String getDebugLog(String id) throws ShellyApiException {
        return ""; // Gen2 uses WS to publish debug log
    }

    /*
     * The following API calls are not yet relevant, because currently there a no Plus/Pro (Gen2) devices of those
     * categories (e.g. bulbs)
     */

    @Override
    public void setLightParm(int lightIndex, String parm, String value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setLightParms(int lightIndex, Map<String, String> parameters) throws ShellyApiException {
        Shelly2RpcRequestParams params = new Shelly2RpcRequestParams();
        if (getProfile().isRGBW2) {
            String brightness = parameters.get(SHELLY_COLOR_BRIGHTNESS);
            if (brightness != null) {
                params.brightness = Integer.parseInt(brightness);
            }
            String red = parameters.get(SHELLY_COLOR_RED);
            String green = parameters.get(SHELLY_COLOR_GREEN);
            String blue = parameters.get(SHELLY_COLOR_BLUE);
            if (red != null && green != null && blue != null) {
                params.rgb = new Integer[] { Integer.parseInt(red), Integer.parseInt(green), Integer.parseInt(blue) };
            }
            String white = parameters.get(SHELLY_COLOR_WHITE);
            if (white != null) {
                params.white = Integer.parseInt(white);
            }
            if (parameters.containsKey(SHELLY_LIGHT_TURN)) {
                params.on = SHELLY_API_ON.equals(parameters.get(SHELLY_LIGHT_TURN));
            }
            params.id = lightIndex;

            apiRequest(SHELLYRPC_METHOD_RGBW_SET, params, String.class);
        }
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setLightMode(String mode) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValveMode(int valveId, boolean auto) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValvePosition(int valveId, double value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValveTemperature(int valveId, double value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValveProfile(int valveId, int value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setValveBoostTime(int valveId, int value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void startValveBoost(int valveId, int value) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public String resetStaCache() throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public void setActionURLs() throws ShellyApiException {
        // not relevant for Gen2
    }

    @Override
    public ShellySettingsLogin setCoIoTPeer(String peer) throws ShellyApiException {
        // not relevant for Gen2
        return new ShellySettingsLogin();
    }

    @Override
    public String getCoIoTDescription() {
        return ""; // not relevant to Gen2
    }

    @Override
    public void sendIRKey(String keyCode) throws ShellyApiException {
        throw new ShellyApiException("API call not implemented");
    }

    @Override
    public String setWiFiRecovery(boolean enable) throws ShellyApiException {
        return "failed"; // not supported by Gen2
    }

    @Override
    public String setApRoaming(boolean enable) throws ShellyApiException {
        return "false";// not supported by Gen2
    }

    private void asyncApiRequest(String method) throws ShellyApiException {
        Shelly2RpcBaseMessage request = buildRequest(method, null);
        reconnect();
        rpcSocket.sendMessage(gson.toJson(request)); // submit, result wull be async
    }

    public <T> T apiRequest(String method, @Nullable Object params, Class<T> classOfT) throws ShellyApiException {
        String json = "";
        Shelly2RpcBaseMessage req = buildRequest(method, params);
        try {
            reconnect(); // make sure WS is connected
            json = rpcPost(gson.toJson(req));
        } catch (ShellyApiException e) {
            ShellyApiResult res = e.getApiResult();
            String auth = getString(res.authChallenge);
            if (res.isHttpAccessUnauthorized() && !auth.isEmpty()) {
                String[] options = auth.split(",");
                Shelly2AuthChallenge authInfo = this.authInfo = new Shelly2AuthChallenge();
                for (String o : options) {
                    String key = substringBefore(o, "=").stripLeading().trim();
                    String value = substringAfter(o, "=").replace("\"", "").trim();
                    switch (key) {
                        case "Digest qop":
                            authInfo.authType = SHELLY2_AUTHTTYPE_DIGEST;
                            break;
                        case "realm":
                            authInfo.realm = value;
                            break;
                        case "nonce":
                            // authInfo.nonce = Long.parseLong(value, 16);
                            authInfo.nonce = value;
                            break;
                        case "algorithm":
                            authInfo.algorithm = value;
                            break;
                    }
                }
                json = rpcPost(gson.toJson(req));
            } else {
                throw e;
            }
        }
        Shelly2RpcBaseMessage response = gson.fromJson(json, Shelly2RpcBaseMessage.class);
        if (response == null) {
            throw new ShellyApiException("Unable to convert API result to object");
        }
        if (response.result != null) {
            // return sub element result as requested class type
            json = gson.toJson(response.result);
            boolean isString = response.result instanceof String;
            return fromJson(gson, isString && "null".equalsIgnoreCase(((String) response.result)) ? "{}" : json,
                    classOfT);
        } else {
            // return direct format
            @Nullable
            T result = gson.fromJson(json, classOfT == String.class ? Shelly2RpcBaseMessage.class : classOfT);
            if (result == null) {
                throw new ShellyApiException("Unable to convert API result to object");
            }
            return result;
        }
    }

    public <T> T apiRequest(Shelly2RpcRequest request, Class<T> classOfT) throws ShellyApiException {
        return apiRequest(request.method, request.params, classOfT);
    }

    public void apiRequest(Shelly2RpcRequest request) throws ShellyApiException {
        apiRequest(request.method, request.params, Shelly2RpcBaseMessage.class);
    }

    private String rpcPost(String postData) throws ShellyApiException {
        return httpPost(authInfo, postData);
    }

    private void reconnect() throws ShellyApiException {
        if (!rpcSocket.isConnected()) {
            logger.debug("{}: Connect Rpc Socket (discovery = {})", thingName, discovery);
            rpcSocket.connect();
        }
    }

    private void disconnect() {
        if (rpcSocket.isConnected()) {
            logger.trace("{}: Disconnect Rpc Socket", thingName);
        }
        rpcSocket.disconnect();
    }

    public Shelly2RpctInterface getRpcHandler() {
        return this;
    }

    @Override
    public void close() {
        if (initialized || rpcSocket.isConnected()) {
            logger.debug("{}: Closing Rpc API (socket is {}, discovery={})", thingName,
                    rpcSocket.isConnected() ? "connected" : "disconnected", discovery);
        }
        disconnect();
        initialized = false;
    }

    private void incProtErrors() {
        ShellyThingInterface thing = this.thing;
        if (thing != null) {
            thing.incProtErrors();
        }
    }
}
