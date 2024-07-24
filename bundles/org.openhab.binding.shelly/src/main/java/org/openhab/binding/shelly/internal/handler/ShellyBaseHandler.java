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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.*;
import static org.openhab.binding.shelly.internal.handler.ShellyComponents.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;
import static org.openhab.core.thing.Thing.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyApiResult;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyFavPos;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyThermnostat;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapHandler;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2APClientList.Shelly2APClient;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.api2.ShellyBluApi;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.discovery.ShellyBasicDiscoveryService;
import org.openhab.binding.shelly.internal.discovery.ShellyThingCreator;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.binding.shelly.internal.util.ShellyChannelCache;
import org.openhab.binding.shelly.internal.util.ShellyVersionDTO;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public abstract class ShellyBaseHandler extends BaseThingHandler
        implements ShellyThingInterface, ShellyDeviceListener, ShellyManagerInterface {

    protected final Logger logger = LoggerFactory.getLogger(ShellyBaseHandler.class);
    protected final ShellyChannelDefinitions channelDefinitions;

    public String thingName = "";
    public String thingType = "";

    protected final ShellyApiInterface api;
    private final HttpClient httpClient;
    private final ShellyThingTable thingTable;

    private ShellyBindingConfiguration bindingConfig;
    protected ShellyThingConfiguration config = new ShellyThingConfiguration();
    protected ShellyDeviceProfile profile = new ShellyDeviceProfile(); // init empty profile to avoid NPE
    private ShellyDeviceStats stats = new ShellyDeviceStats();
    private @Nullable Shelly1CoapHandler coap;

    private final ShellyTranslationProvider messages;
    private final ShellyChannelCache cache;
    private final int cacheCount = UPDATE_SETTINGS_INTERVAL_SECONDS / UPDATE_STATUS_INTERVAL_SECONDS;

    private boolean gen2 = false;
    private final boolean blu;
    protected boolean autoCoIoT = false;

    // Thing status
    private boolean channelsCreated = false;
    private boolean stopping = false;
    private int vibrationFilter = 0;
    private String lastWakeupReason = "";

    // Scheduler
    private long watchdog = now();
    protected int scheduledUpdates = 0;
    private int skipCount = UPDATE_SKIP_COUNT;
    private int skipUpdate = 0;
    private boolean refreshSettings = false;
    private @Nullable ScheduledFuture<?> statusJob;
    private @Nullable ScheduledFuture<?> initJob;

    /**
     * Constructor
     *
     * @param thing The Thing object
     * @param translationProvider
     * @param bindingConfig The binding configuration (beside thing
     *            configuration)
     * @param thingTable
     * @param coapServer coap server instance
     * @param httpClient from httpService
     */
    public ShellyBaseHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient) {
        super(thing);

        this.thingTable = thingTable;
        this.thingName = getString(thing.getLabel());
        this.messages = translationProvider;
        this.cache = new ShellyChannelCache(this);
        this.channelDefinitions = new ShellyChannelDefinitions(messages);
        this.bindingConfig = bindingConfig;
        this.config = getConfigAs(ShellyThingConfiguration.class);
        this.httpClient = httpClient;

        // Create thing handler depending on device generation
        String thingType = getThingType();
        blu = ShellyDeviceProfile.isBluSeries(thingType);
        gen2 = ShellyDeviceProfile.isGeneration2(thingType);
        if (blu) {
            this.api = new ShellyBluApi(thingName, thingTable, this);
        } else if (gen2) {
            this.api = new Shelly2ApiRpc(thingName, thingTable, this);
        } else {
            this.api = new Shelly1HttpApi(thingName, this);
        }
        if (gen2) {
            config.eventsCoIoT = false;
        }
        if (config.eventsCoIoT) {
            this.coap = new Shelly1CoapHandler(this, coapServer);
        }
    }

    @Override
    public boolean checkRepresentation(String key) {
        return key.equalsIgnoreCase(getUID()) || key.equalsIgnoreCase(config.deviceAddress)
                || key.equalsIgnoreCase(config.serviceName) || key.equalsIgnoreCase(getThingName());
    }

    /**
     * Schedule asynchronous Thing initialization, register thing to event dispatcher
     */
    @Override
    public void initialize() {
        // start background initialization:
        initJob = scheduler.schedule(() -> {
            boolean start = true;
            try {
                if (initializeThingConfig()) {
                    logger.debug("{}: Config: {}", thingName, config);
                    start = initializeThing();
                }
            } catch (ShellyApiException e) {
                start = handleApiException(e);
            } catch (IllegalArgumentException e) {
                logger.debug("{}: Unable to initialize, retrying later", thingName, e);
            } finally {
                // even this initialization failed we start the status update
                // the updateJob will then try to auto-initialize the thing
                // in this case the thing stays in status INITIALIZING
                if (start) {
                    startUpdateJob();
                }
            }
        }, 2, TimeUnit.SECONDS);
    }

    private boolean handleApiException(ShellyApiException e) {
        ShellyApiResult res = e.getApiResult();
        ThingStatusDetail errorCode = ThingStatusDetail.COMMUNICATION_ERROR;
        String status = "";
        boolean retry = true;
        if (e.isJsonError()) { // invalid JSON format
            logger.debug("{}: Unable to parse API response: {}; json={}", thingName, res.getUrl(), res.response, e);
            status = "offline.status-error-unexpected-error";
            errorCode = ThingStatusDetail.CONFIGURATION_ERROR;
            retry = false;
        } else if (res.isHttpAccessUnauthorized()) {
            status = "offline.conf-error-access-denied";
            errorCode = ThingStatusDetail.CONFIGURATION_ERROR;
            retry = false;
        } else if (isWatchdogExpired()) {
            status = "offline.status-error-watchdog";
        } else if (res.httpCode >= 400) {
            logger.debug("{}: Unexpected API result: {}/{}", thingName, res.httpCode, res.httpReason, e);
            status = "offline.status-error-unexpected-api-result";
            retry = false;
        } else if (profile.alwaysOn && (e.isConnectionError() || res.isHttpTimeout())) {
            status = "offline.status-error-connect";
        }

        if (!status.isEmpty()) {
            setThingOffline(errorCode, status, e.toString());
        } else {
            logger.debug("{}: Unable to initialize: {}, retrying later", thingName, e.toString());
        }

        if (!retry) {
            api.close();
        }

        return retry;
    }

    @Override
    public ShellyThingConfiguration getThingConfig() {
        return config;
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public void startScan() {
        if (api.isInitialized()) {
            api.startScan();
        }

        checkRangeExtender(profile);
    }

    /**
     * This routine is called every time the Thing configuration has been changed
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        logger.debug("{}: Thing config updated, re-initialize", thingName);
        if (coap != null) {
            coap.stop();
        }
        stopping = false;
        reinitializeThing();// force re-initialization
    }

    /**
     * Initialize Thing: Initialize API access, get settings and initialize Device Profile
     * If the device is password protected and the credentials are missing or don't match the API access will throw an
     * Exception. In this case the thing type will be changed to shelly-unknown. The user has the option to edit the
     * thing config and set the correct credentials. The thing type will be changed to the requested one if the
     * credentials are correct and the API access is initialized successful.
     *
     * @throws ShellyApiException e.g. http returned non-ok response, check e.getMessage() for details.
     */
    public boolean initializeThing() throws ShellyApiException {
        // Init from thing type to have a basic profile, gets updated when device info is received from API
        refreshSettings = false;
        lastWakeupReason = "";
        cache.setThingName(thingName);
        cache.clear();
        resetStats();

        profile.initFromThingType(thingType);
        logger.debug(
                "{}: Start initializing for thing {}, type {}, Device address {}, Gen2: {}, isBlu: {}, alwaysOn: {}, hasBattery: {}, CoIoT: {}",
                thingName, getThing().getLabel(), thingType, config.deviceAddress.toUpperCase(), gen2, profile.isBlu,
                profile.alwaysOn, profile.hasBattery, config.eventsCoIoT);
        if (config.deviceAddress.isEmpty()) {
            setThingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "config-status.error.missing-device-address");
            return false;
        }

        if (profile.alwaysOn || !profile.isInitialized()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                    messages.get("status.unknown.initializing"));
        }

        // Gen 1 only: Setup CoAP listener to we get the CoAP message, which triggers initialization even the thing
        // could not be fully initialized here. In this case the CoAP messages triggers auto-initialization (like the
        // Action URL does when enabled)
        if (coap != null && config.eventsCoIoT && !profile.alwaysOn) {
            coap.start(thingName, config);
        }

        // Initialize API access, exceptions will be catched by initialize()
        api.initialize();
        ShellySettingsDevice device = profile.device = api.getDeviceInfo();
        if (getBool(device.auth) && config.password.isEmpty()) {
            setThingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "offline.conf-error-no-credentials");
            return false;
        }
        if (config.serviceName.isEmpty()) {
            config.serviceName = getString(device.hostname).toLowerCase();
        }

        api.setConfig(thingName, config);
        ShellyDeviceProfile tmpPrf = api.getDeviceProfile(thingType, profile.device);
        String mode = getString(tmpPrf.device.mode);
        if (this.getThing().getThingTypeUID().equals(THING_TYPE_SHELLYPROTECTED)) {
            changeThingType(thingName, mode);
            return false; // force re-initialization
        }
        // Validate device mode
        String reqMode = thingType.contains("-") ? substringAfter(thingType, "-") : "";
        if (!reqMode.isEmpty() && !mode.equals(reqMode)) {
            setThingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "offline.conf-error-wrong-mode", mode, reqMode);
            return false;
        }
        if (!getString(tmpPrf.device.coiot).isEmpty()) {
            // New Shelly devices might use a different endpoint for the CoAP listener
            tmpPrf.coiotEndpoint = tmpPrf.device.coiot;
        }
        if (tmpPrf.settings.sleepMode != null && !tmpPrf.isTRV) {
            // Sensor, usually 12h, H&T in USB mode 10min
            tmpPrf.updatePeriod = "m".equalsIgnoreCase(getString(tmpPrf.settings.sleepMode.unit))
                    ? tmpPrf.settings.sleepMode.period * 60 // minutes
                    : tmpPrf.settings.sleepMode.period * 3600; // hours
            tmpPrf.updatePeriod += 60; // give 1min extra
        } else if (tmpPrf.settings.coiot != null && tmpPrf.settings.coiot.updatePeriod != null) {
            // Derive from CoAP update interval, usually 2*15+10s=40sec -> 70sec
            tmpPrf.updatePeriod = Math.max(UPDATE_SETTINGS_INTERVAL_SECONDS,
                    2 * getInteger(tmpPrf.settings.coiot.updatePeriod)) + 10;
        } else {
            tmpPrf.updatePeriod = UPDATE_SETTINGS_INTERVAL_SECONDS + 10;
        }

        tmpPrf.status = api.getStatus(); // update thing properties
        tmpPrf.updateFromStatus(tmpPrf.status);
        addStateOptions(tmpPrf);

        // update thing properties
        updateProperties(tmpPrf, tmpPrf.status);
        checkVersion(tmpPrf, tmpPrf.status);

        // Check for Range Extender mode, add secondary device to Inbox
        checkRangeExtender(tmpPrf);

        startCoap(config, tmpPrf);
        if (!gen2 && !blu) {
            api.setActionURLs(); // register event urls
        }

        // All initialization done, so keep the profile and set Thing to ONLINE
        fillDeviceStatus(tmpPrf.status, false);
        postEvent(ALARM_TYPE_NONE, false);

        profile = tmpPrf;
        showThingConfig(profile);

        logger.debug("{}: Thing successfully initialized.", thingName);
        updateProperties(profile, profile.status);
        setThingOnline(); // if API call was successful the thing must be online
        return true; // success
    }

    /**
     * Handle Channel Commands
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                String channelId = channelUID.getId();
                State value = cache.getValue(channelId);
                if (value != UnDefType.NULL) {
                    updateState(channelId, value);
                }
                return;
            }

            if (!profile.isInitialized()) {
                logger.debug("{}: {}", thingName, messages.get("command.init", command));
                initializeThing();
            } else {
                profile = getProfile(false);
            }

            boolean update = false;
            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_SENSE_KEY: // Shelly Sense: Send Key
                    logger.debug("{}: Send key {}", thingName, command);
                    api.sendIRKey(command.toString());
                    update = true;
                    break;

                case CHANNEL_LED_STATUS_DISABLE:
                    logger.debug("{}: Set STATUS LED disabled to {}", thingName, command);
                    api.setLedStatus(SHELLY_LED_STATUS_DISABLE, command == OnOffType.ON);
                    break;
                case CHANNEL_LED_POWER_DISABLE:
                    logger.debug("{}: Set POWER LED disabled to {}", thingName, command);
                    api.setLedStatus(SHELLY_LED_POWER_DISABLE, command == OnOffType.ON);
                    break;

                case CHANNEL_SENSOR_SLEEPTIME:
                    logger.debug("{}: Set sensor sleep time to {}", thingName, command);
                    int value = getNumber(command).intValue();
                    value = value > 0 ? Math.max(SHELLY_MOTION_SLEEPTIME_OFFSET, value - SHELLY_MOTION_SLEEPTIME_OFFSET)
                            : 0;
                    api.setSleepTime(value);
                    break;
                case CHANNEL_CONTROL_SCHEDULE:
                    if (profile.isTRV) {
                        logger.debug("{}: {} Valve schedule/profile", thingName,
                                command == OnOffType.ON ? "Enable" : "Disable");
                        api.setValveProfile(0,
                                command == OnOffType.OFF ? 0 : profile.status.thermostats.get(0).profile);
                    }
                    break;
                case CHANNEL_CONTROL_PROFILE:
                    logger.debug("{}: Select profile {}", thingName, command);
                    int id = -1;
                    if (command instanceof Number) {
                        id = getNumber(command).intValue();
                    } else {
                        String cmd = command.toString();
                        if (isDigit(cmd.charAt(0))) {
                            id = Integer.parseInt(cmd);
                        } else if (profile.settings.thermostats != null) {
                            ShellyThermnostat t = profile.settings.thermostats.get(0);
                            for (int i = 0; i < t.profileNames.length; i++) {
                                if (t.profileNames[i].equalsIgnoreCase(cmd)) {
                                    id = i + 1;
                                }
                            }
                        }
                    }
                    if (id < 0 || id > 5) {
                        logger.warn("{}: Invalid profile Id {} requested", thingName, profile);
                        break;
                    }
                    api.setValveProfile(0, id);
                    break;
                case CHANNEL_CONTROL_MODE:
                    logger.debug("{}: Set mode to {}", thingName, command);
                    api.setValveMode(0, CHANNEL_CONTROL_MODE.equalsIgnoreCase(command.toString()));
                    break;
                case CHANNEL_CONTROL_SETTEMP:
                    logger.debug("{}: Set temperature to {}", thingName, command);
                    api.setValveTemperature(0, getNumber(command).doubleValue());
                    break;
                case CHANNEL_CONTROL_POSITION:
                    logger.debug("{}: Set position to {}", thingName, command);
                    api.setValvePosition(0, getNumber(command));
                    break;
                case CHANNEL_CONTROL_BCONTROL:
                    logger.debug("{}: Set boost mode to {}", thingName, command);
                    api.startValveBoost(0, command == OnOffType.ON ? -1 : 0);
                    break;
                case CHANNEL_CONTROL_BTIMER:
                    logger.debug("{}: Set boost timer to {}", thingName, command);
                    api.setValveBoostTime(0, getNumber(command).intValue());
                    break;
                case CHANNEL_SENSOR_MUTE:
                    if (profile.isSmoke && ((OnOffType) command) == OnOffType.ON) {
                        logger.debug("{}: Mute Smoke Alarm", thingName);
                        api.muteSmokeAlarm(0);
                        updateChannel(getString(channelUID.getGroupId()), CHANNEL_SENSOR_MUTE, OnOffType.OFF);
                    }
                    break;
                default:
                    update = handleDeviceCommand(channelUID, command);
                    break;
            }

            restartWatchdog();
            if (update && !autoCoIoT && !isUpdateScheduled()) {
                requestUpdates(1, false);
            }
        } catch (ShellyApiException e) {
            if (!handleApiException(e)) {
                return;
            }

            ShellyApiResult res = e.getApiResult();
            if (res.isNotCalibrtated()) {
                logger.warn("{}: {}", thingName, messages.get("roller.calibrating"));
            } else {
                logger.warn("{}: {} - {}", thingName, messages.get("command.failed", command, channelUID),
                        e.toString());
            }

            String group = getString(channelUID.getGroupId());
            String channel = getString(channelUID.getIdWithoutGroup());
            State oldValue = getChannelValue(group, channel);
            if (oldValue != UnDefType.NULL) {
                logger.info("{}: Restore channel value to {}", thingName, oldValue);
                updateChannel(group, channel, oldValue);
            }

        } catch (IllegalArgumentException e) {
            logger.debug("{}: {}", thingName, messages.get("command.failed", command, channelUID));
        }
    }

    /**
     * Update device status and channels
     */
    protected void refreshStatus() {
        try {
            boolean updated = false;

            if (vibrationFilter > 0) {
                vibrationFilter--;
                logger.debug("{}: Vibration events are absorbed for {} more seconds", thingName,
                        vibrationFilter * UPDATE_STATUS_INTERVAL_SECONDS);
            }

            skipUpdate++;
            ThingStatus thingStatus = getThing().getStatus();
            if (refreshSettings || (scheduledUpdates > 0) || (skipUpdate % skipCount == 0)) {
                if (!profile.isInitialized() || ((thingStatus == ThingStatus.OFFLINE))
                        || (thingStatus == ThingStatus.UNKNOWN)) {
                    logger.debug("{}: Status update triggered thing initialization", thingName);
                    initializeThing(); // may fire an exception if initialization failed
                }
                ShellySettingsStatus status = api.getStatus();
                boolean restarted = checkRestarted(status);
                profile = getProfile(refreshSettings || restarted);
                profile.status = status;
                profile.updateFromStatus(status);
                if (restarted) {
                    logger.debug("{}: Device restart #{} detected", thingName, stats.restarts);
                    stats.restarts++;
                    postEvent(ALARM_TYPE_RESTARTED, true);
                }

                // If status update was successful the thing must be online,
                // but not while firmware update is in progress
                if (getThingStatusDetail() != ThingStatusDetail.FIRMWARE_UPDATING) {
                    setThingOnline();
                }

                // map status to channels
                updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_NAME, getStringType(profile.settings.name));
                updated |= this.updateDeviceStatus(status);
                updated |= ShellyComponents.updateDeviceStatus(this, status);
                fillDeviceStatus(status, updated);
                updated |= updateInputs(status);
                updated |= updateMeters(this, status);
                updated |= updateSensors(this, status);

                // All channels must be created after the first cycle
                channelsCreated = true;
            }
        } catch (ShellyApiException e) {
            // http call failed: go offline except for battery devices, which might be in
            // sleep mode. Once the next update is successful the device goes back online
            handleApiException(e);
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.debug("{}: Unable to refresh status: {}", thingName, messages.get("statusupdate.failed"), e);
        } finally {
            if (scheduledUpdates > 0) {
                --scheduledUpdates;
                logger.trace("{}: {} more updates requested", thingName, scheduledUpdates);
            } else if ((skipUpdate >= cacheCount) && !cache.isEnabled()) {
                logger.debug("{}: Enabling channel cache ({} updates / {}s)", thingName, skipUpdate,
                        cacheCount * UPDATE_STATUS_INTERVAL_SECONDS);
                cache.enable();
            }
        }
    }

    private void checkRangeExtender(ShellyDeviceProfile prf) {
        if (getBool(prf.settings.rangeExtender) && config.enableRangeExtender && prf.status.rangeExtender != null
                && prf.status.rangeExtender.apClients != null) {
            for (Shelly2APClient client : profile.status.rangeExtender.apClients) {
                String secondaryIp = config.deviceIp + ":" + client.mport.toString();
                String name = "shellyplusrange-" + client.mac.replaceAll(":", "");
                DiscoveryResult result = ShellyBasicDiscoveryService.createResult(true, name, secondaryIp,
                        bindingConfig, httpClient, messages);
                if (result != null) {
                    thingTable.discoveredResult(result);
                }
            }
        }
    }

    private void showThingConfig(ShellyDeviceProfile profile) {
        logger.debug("{}: Initializing device {}, type {}, Hardware: Rev: {}, batch {}; Firmware: {} / {}", thingName,
                profile.device.hostname, profile.device.type, profile.hwRev, profile.hwBatchId, profile.fwVersion,
                profile.fwDate);
        logger.debug("{}: Shelly settings info for {}: {}", thingName, profile.device.hostname, profile.settingsJson);
        logger.debug("{}: Device "
                + "hasRelays:{} (numRelays={}),isRoller:{} (numRoller={}),isDimmer:{},numMeter={},isEMeter:{}), ext. Switch Add-On: {}"
                + ",isSensor:{},isDS:{},hasBattery:{}{},isSense:{},isMotion:{},isLight:{},isBulb:{},isDuo:{},isRGBW2:{},inColor:{}, BLU Gateway support: {}"
                + ",alwaysOn:{}, updatePeriod:{}sec", thingName, profile.hasRelays, profile.numRelays, profile.isRoller,
                profile.numRollers, profile.isDimmer, profile.numMeters, profile.isEMeter,
                profile.settings.extSwitch != null ? "installed" : "n/a", profile.isSensor, profile.isDW,
                profile.hasBattery, profile.hasBattery ? " (low battery threshold=" + config.lowBattery + "%)" : "",
                profile.isSense, profile.isMotion, profile.isLight, profile.isBulb, profile.isDuo, profile.isRGBW2,
                profile.inColor, profile.alwaysOn, profile.updatePeriod, config.enableBluGateway);
        if (profile.status.extTemperature != null || profile.status.extHumidity != null
                || profile.status.extVoltage != null || profile.status.extAnalogInput != null) {
            logger.debug("{}: Shelly Add-On detected with at least 1 external sensor", thingName);
        }
    }

    private void addStateOptions(ShellyDeviceProfile prf) {
        if (prf.isTRV) {
            String[] profileNames = prf.getValveProfileList(0);
            String channelId = mkChannelId(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE);
            logger.debug("{}: Adding TRV profile names to channel description: {}", thingName, profileNames);
            channelDefinitions.clearStateOptions(channelId);
            int fid = 1;
            for (String name : profileNames) {
                channelDefinitions.addStateOption(channelId, "" + fid, fid + ": " + name);
                fid++;
            }
        }
        if (prf.isRoller && prf.settings.favorites != null) {
            String channelId = mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_FAV);
            logger.debug("{}: Adding {} roler favorite(s) to channel description", thingName,
                    prf.settings.favorites.size());
            channelDefinitions.clearStateOptions(channelId);
            int fid = 1;
            for (ShellyFavPos fav : prf.settings.favorites) {
                channelDefinitions.addStateOption(channelId, "" + fid, fid + ": " + fav.name);
                fid++;
            }
        }
    }

    @Override
    public String getThingType() {
        return thing.getThingTypeUID().getId();
    }

    @Override
    public ThingStatus getThingStatus() {
        return thing.getStatus();
    }

    @Override
    public ThingStatusDetail getThingStatusDetail() {
        return thing.getStatusInfo().getStatusDetail();
    }

    @Override
    public boolean isThingOnline() {
        return getThingStatus() == ThingStatus.ONLINE;
    }

    public boolean isThingOffline() {
        return getThingStatus() == ThingStatus.OFFLINE;
    }

    @Override
    public void setThingOnline() {
        if (!isThingOnline()) {
            updateStatus(ThingStatus.ONLINE);

            // request 3 updates in a row (during the first 2+3*3 sec)
            requestUpdates(profile.alwaysOn ? 3 : 1, !channelsCreated);
        }

        // Restart watchdog when status update was successful (no exception)
        restartWatchdog();
    }

    @Override
    public void setThingOffline(ThingStatusDetail detail, String messageKey, Object... arguments) {
        if (!isThingOffline()) {
            updateStatus(ThingStatus.OFFLINE, detail, messages.get(messageKey, arguments));
            api.close(); // Gen2: disconnect WS/close http sessions
            watchdog = 0;
            channelsCreated = false; // check for new channels after devices gets re-initialized (e.g. new
        }
    }

    @Override
    public void restartWatchdog() {
        watchdog = now();
        updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_HEARTBEAT, getTimestamp());
        logger.trace("{}: Watchdog restarted (expires in {} sec)", thingName, profile.updatePeriod);
    }

    private boolean isWatchdogExpired() {
        long delta = now() - watchdog;
        if ((watchdog > 0) && (delta > profile.updatePeriod)) {
            stats.remainingWatchdog = delta;
            return true;
        }
        return false;
    }

    @Override
    public void reinitializeThing() {
        logger.debug("{}: Re-Initialize Thing", thingName);
        if (isStopping()) {
            logger.debug("{}: Handler is shutting down, ignore", thingName);
            return;
        }
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                messages.get("offline.status-error-restarted"));
        requestUpdates(0, true);
    }

    @Override
    public boolean isStopping() {
        return stopping;
    }

    @Override
    public void fillDeviceStatus(ShellySettingsStatus status, boolean updated) {
        String alarm = "";

        // Update uptime and WiFi, internal temp
        ShellyComponents.updateDeviceStatus(this, status);
        stats.wifiRssi = getInteger(status.wifiSta.rssi);

        if (api.isInitialized()) {
            stats.timeoutErrors = api.getTimeoutErrors();
            stats.timeoutsRecorvered = api.getTimeoutsRecovered();
        }
        stats.remainingWatchdog = watchdog > 0 ? now() - watchdog : 0;

        // Check various device indicators like overheating
        if (checkRestarted(status)) {
            // Force re-initialization on next status update
            reinitializeThing();
        } else if (getBool(status.overtemperature)) {
            alarm = ALARM_TYPE_OVERTEMP;
        } else if (getBool(status.overload)) {
            alarm = ALARM_TYPE_OVERLOAD;
        } else if (getBool(status.loaderror)) {
            alarm = ALARM_TYPE_LOADERR;
        }
        State internalTemp = getChannelValue(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP);
        if (internalTemp != UnDefType.NULL) {
            int temp = ((Number) internalTemp).intValue();
            if (temp > stats.maxInternalTemp) {
                stats.maxInternalTemp = temp;
            }
        }

        if (status.uptime != null) {
            stats.lastUptime = getLong(status.uptime);
        }

        if (!alarm.isEmpty()) {
            postEvent(alarm, false);
        }
    }

    @Override
    public void incProtMessages() {
        stats.protocolMessages++;
    }

    @Override
    public void incProtErrors() {
        stats.protocolErrors++;
    }

    /**
     * Check if device has restarted and needs a new Thing initialization
     *
     * @return true: restart detected
     */

    private boolean checkRestarted(ShellySettingsStatus status) {
        if (profile.isInitialized() && profile.alwaysOn /* exclude battery powered devices */
                && (status.uptime != null && status.uptime < stats.lastUptime
                        || (profile.status.update != null && !getString(profile.status.update.oldVersion).isEmpty()
                                && !status.update.oldVersion.equals(profile.status.update.oldVersion)))) {
            logger.debug("{}: Device has been restarted, uptime={}/{}, firmware={}/{}", thingName, stats.lastUptime,
                    getLong(status.uptime), profile.status.update.oldVersion, status.update.oldVersion);
            updateProperties(profile, status);
            return true;
        }
        return false;
    }

    /**
     * Save alarm to the lastAlarm channel
     *
     * @param event Alarm Message
     * @param force
     */
    @Override
    public void postEvent(String event, boolean force) {
        String channelId = mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ALARM);
        State value = cache.getValue(channelId);
        String lastAlarm = value != UnDefType.NULL ? value.toString() : "";

        if (force || !lastAlarm.equals(event)
                || (lastAlarm.equals(event) && now() > stats.lastAlarmTs + HEALTH_CHECK_INTERVAL_SEC)) {
            switch (event.toUpperCase()) {
                case "":
                case "0": // DW2 1.8
                case SHELLY_WAKEUPT_SENSOR:
                case SHELLY_WAKEUPT_PERIODIC:
                case SHELLY_WAKEUPT_BUTTON:
                case SHELLY_WAKEUPT_POWERON:
                case SHELLY_WAKEUPT_EXT_POWER:
                case SHELLY_WAKEUPT_UNKNOWN:
                case Shelly2ApiJsonDTO.SHELLY2_EVENT_OTASTART:
                case Shelly2ApiJsonDTO.SHELLY2_EVENT_OTAPROGRESS:
                case Shelly2ApiJsonDTO.SHELLY2_EVENT_OTADONE:
                case SHELLY_EVENT_ROLLER_CALIB:
                    logger.debug("{}: {}", thingName, messages.get("event.filtered", event));
                case ALARM_TYPE_NONE:
                    break;
                default:
                    logger.debug("{}: {}", thingName, messages.get("event.triggered", event));
                    triggerChannel(channelId, event);
                    cache.updateChannel(channelId, getStringType(event.toUpperCase()));
                    stats.lastAlarm = event;
                    stats.lastAlarmTs = now();
                    stats.alarms++;
            }
        }
    }

    public boolean isUpdateScheduled() {
        return scheduledUpdates > 0;
    }

    /**
     * Callback for device events
     *
     * @param address
     * @param deviceName device receiving the event
     * @param deviceIndex
     * @param type the HTML input data
     * @param parameters parameters from the event URL
     * @return true if event was processed
     */
    @Override
    public boolean onEvent(String address, String deviceName, String deviceIndex, String type,
            Map<String, String> parameters) {
        if (thingName.equalsIgnoreCase(deviceName) || config.deviceAddress.equals(address)
                || config.serviceName.equals(deviceName)) {
            logger.debug("{}: Event received: class={}, index={}, parameters={}", deviceName, type, deviceIndex,
                    parameters);
            int idx = !deviceIndex.isEmpty() ? Integer.parseInt(deviceIndex) : 1;
            if (!profile.isInitialized()) {
                logger.debug("{}: Device is not yet initialized, event triggers initialization", deviceName);
                requestUpdates(1, true);
            } else {
                String group = profile.getControlGroup(idx);
                if (group.isEmpty()) {
                    logger.debug("{}: Unsupported event class: {}", thingName, type);
                    return false;
                }

                // map some of the events to system defined button triggers
                String channel = "";
                String onoff = "";
                String payload = "";
                String parmType = getString(parameters.get("type"));
                String event = !parmType.isEmpty() ? parmType : type;
                boolean isButton = profile.inButtonMode(idx - 1) || "button".equals(type);
                switch (event) {
                    case SHELLY_EVENT_SHORTPUSH:
                    case SHELLY_EVENT_DOUBLE_SHORTPUSH:
                    case SHELLY_EVENT_TRIPLE_SHORTPUSH:
                    case SHELLY_EVENT_LONGPUSH:
                        if (isButton) {
                            triggerButton(group, idx, mapButtonEvent(event));
                            channel = CHANNEL_BUTTON_TRIGGER + profile.getInputSuffix(idx);
                            payload = Shelly1ApiJsonDTO.mapButtonEvent(event);
                        } else {
                            logger.debug("{}: Relay button is not in memontary or detached mode, ignore SHORT/LONGPUSH",
                                    thingName);
                        }
                        break;
                    case SHELLY_EVENT_BTN_ON:
                    case SHELLY_EVENT_BTN_OFF:
                        if (profile.isRGBW2) {
                            // RGBW2 has only one input, so not per channel
                            group = CHANNEL_GROUP_LIGHT_CONTROL;
                        }
                        onoff = CHANNEL_INPUT;
                        break;
                    case SHELLY_EVENT_BTN1_ON:
                    case SHELLY_EVENT_BTN1_OFF:
                        onoff = CHANNEL_INPUT1;
                        break;
                    case SHELLY_EVENT_BTN2_ON:
                    case SHELLY_EVENT_BTN2_OFF:
                        onoff = CHANNEL_INPUT2;
                        break;
                    case SHELLY_EVENT_OUT_ON:
                    case SHELLY_EVENT_OUT_OFF:
                        onoff = CHANNEL_OUTPUT;
                        break;
                    case SHELLY_EVENT_ROLLER_OPEN:
                    case SHELLY_EVENT_ROLLER_CLOSE:
                    case SHELLY_EVENT_ROLLER_STOP:
                        channel = CHANNEL_EVENT_TRIGGER;
                        payload = event;
                        break;
                    case SHELLY_EVENT_SENSORREPORT:
                        // process sensor with next refresh
                        break;
                    case SHELLY_EVENT_TEMP_OVER: // DW2
                    case SHELLY_EVENT_TEMP_UNDER:
                        channel = CHANNEL_EVENT_TRIGGER;
                        payload = event;
                        break;
                    case SHELLY_EVENT_FLOOD_DETECTED:
                    case SHELLY_EVENT_FLOOD_GONE:
                        updateChannel(group, CHANNEL_SENSOR_FLOOD,
                                OnOffType.from(event.equalsIgnoreCase(SHELLY_EVENT_FLOOD_DETECTED)));
                        break;

                    case SHELLY_EVENT_CLOSE: // DW 1.7
                    case SHELLY_EVENT_OPEN: // DW 1.7
                        updateChannel(group, CHANNEL_SENSOR_STATE,
                                event.equalsIgnoreCase(SHELLY_API_DWSTATE_OPEN) ? OpenClosedType.OPEN
                                        : OpenClosedType.CLOSED);
                        break;

                    case SHELLY_EVENT_DARK: // DW 1.7
                    case SHELLY_EVENT_TWILIGHT: // DW 1.7
                    case SHELLY_EVENT_BRIGHT: // DW 1.7
                        updateChannel(group, CHANNEL_SENSOR_ILLUM, getStringType(event));
                        break;

                    case SHELLY_EVENT_ALARM_MILD: // Shelly Gas
                    case SHELLY_EVENT_ALARM_HEAVY:
                    case SHELLY_EVENT_ALARM_OFF:
                    case SHELLY_EVENT_VIBRATION: // DW2
                        channel = CHANNEL_SENSOR_ALARM_STATE;
                        payload = event.toUpperCase();
                        break;

                    default:
                        // trigger will be provided by input/output channel or sensor channels
                }

                if (!onoff.isEmpty()) {
                    updateChannel(group, onoff, OnOffType.from(event.toLowerCase().contains("_on")));
                }
                if (!payload.isEmpty()) {
                    // Pass event to trigger channel
                    payload = payload.toUpperCase();
                    logger.debug("{}: Post event {}", thingName, payload);
                    triggerChannel(mkChannelId(group, channel), payload);
                }
            }

            // request update on next interval (2x for non-battery devices)
            restartWatchdog();
            requestUpdates(scheduledUpdates >= 2 ? 0 : !profile.hasBattery ? 2 : 1, true);
            return true;
        }
        return false;
    }

    /**
     * Initialize the binding's thing configuration, calc update counts
     */
    protected boolean initializeThingConfig() {
        thingType = getThing().getThingTypeUID().getId();
        final Map<String, String> properties = getThing().getProperties();
        thingName = getString(properties.get(PROPERTY_SERVICE_NAME));
        if (thingName.isEmpty()) {
            thingName = getString(thingType + "-" + getString(getThing().getUID().getId())).toLowerCase();
        }

        config = getConfigAs(ShellyThingConfiguration.class);
        if (config.deviceAddress.isEmpty()) {
            config.deviceAddress = config.deviceIp;
        }
        if (config.deviceAddress.isEmpty()) {
            logger.debug("{}: IP/MAC address for the device must not be empty", thingName); // may not set in .things
                                                                                            // file
            return false;
        }

        config.deviceAddress = config.deviceAddress.toLowerCase().replace(":", ""); // remove : from MAC address and
                                                                                    // convert to lower case
        if (!config.deviceIp.isEmpty()) {
            try {
                String ip = config.deviceIp.contains(":") ? substringBefore(config.deviceIp, ":") : config.deviceIp;
                String port = config.deviceIp.contains(":") ? substringAfter(config.deviceIp, ":") : "";
                InetAddress addr = InetAddress.getByName(ip);
                String saddr = addr.getHostAddress();
                if (!ip.equals(saddr)) {
                    logger.debug("{}: hostname {} resolved to IP address {}", thingName, config.deviceIp, saddr);
                    config.deviceIp = saddr + (port.isEmpty() ? "" : ":" + port);
                }
            } catch (UnknownHostException e) {
                logger.debug("{}: Unable to resolve hostname {}", thingName, config.deviceIp);
            }
        }

        config.serviceName = getString(properties.get(PROPERTY_SERVICE_NAME));
        config.localIp = bindingConfig.localIP;
        config.localPort = String.valueOf(bindingConfig.httpPort);
        if (config.localIp.startsWith("169.254")) {
            setThingOffline(ThingStatusDetail.COMMUNICATION_ERROR, "config-status.error.network-config",
                    config.localIp);
            return false;
        }

        if (!profile.isGen2 && config.userId.isEmpty() && !bindingConfig.defaultUserId.isEmpty()) {
            // Gen2 has hard coded user "admin"
            config.userId = bindingConfig.defaultUserId;
            logger.debug("{}: Using default userId {} from binding config", thingName, config.userId);
        }
        if (config.password.isEmpty() && !bindingConfig.defaultPassword.isEmpty()) {
            config.password = bindingConfig.defaultPassword;
            logger.debug("{}: Using default password from bindingConfig (userId={})", thingName, config.userId);
        }

        if (config.updateInterval == 0) {
            config.updateInterval = UPDATE_STATUS_INTERVAL_SECONDS * UPDATE_SKIP_COUNT;
        }
        if (config.updateInterval < UPDATE_MIN_DELAY) {
            config.updateInterval = UPDATE_MIN_DELAY;
        }

        // Try to get updatePeriod from properties
        // For battery devinities the REST call to get the settings will most likely fail, because the device is in
        // sleep mode. Therefore we use the last saved property value as default. Will be overwritten, when device is
        // initialized successfully by the REST call.
        String lastPeriod = getString(properties.get(PROPERTY_UPDATE_PERIOD));
        if (!lastPeriod.isEmpty()) {
            int period = Integer.parseInt(lastPeriod);
            if (period > 0) {
                profile.updatePeriod = period;
            }
        }

        skipCount = config.updateInterval / UPDATE_STATUS_INTERVAL_SECONDS;
        logger.trace("{}: updateInterval = {}s -> skipCount = {}", thingName, config.updateInterval, skipCount);
        return true;
    }

    private void checkVersion(ShellyDeviceProfile prf, ShellySettingsStatus status) {
        try {
            if (prf.fwVersion.isEmpty()) {
                // no fw version available (e.g. BLU device)
                return;
            }
            ShellyVersionDTO version = new ShellyVersionDTO();
            if (version.checkBeta(getString(prf.fwVersion))) {
                logger.info("{}: {}", prf.device.hostname,
                        messages.get("versioncheck.beta", prf.fwVersion, prf.fwDate));
            } else {
                String minVersion = !gen2 ? SHELLY_API_MIN_FWVERSION : SHELLY2_API_MIN_FWVERSION;
                if (version.compare(prf.fwVersion, minVersion) < 0) {
                    logger.warn("{}: {}", prf.device.hostname,
                            messages.get("versioncheck.tooold", prf.fwVersion, prf.fwDate, minVersion));
                }
            }
            if (!gen2 && bindingConfig.autoCoIoT && ((version.compare(prf.fwVersion, SHELLY_API_MIN_FWCOIOT)) >= 0)
                    || ("production_test".equalsIgnoreCase(prf.fwVersion))) {
                if (!config.eventsCoIoT) {
                    logger.info("{}: {}", thingName, messages.get("versioncheck.autocoiot"));
                }
                autoCoIoT = true;
            }
            if (status.update.hasUpdate && !version.checkBeta(getString(prf.fwVersion))) {
                logger.info("{}: {}", thingName,
                        messages.get("versioncheck.update", status.update.oldVersion, status.update.newVersion));
            }
        } catch (NullPointerException e) { // could be inconsistant format of beta version
            logger.debug("{}: {}", thingName, messages.get("versioncheck.failed", prf.fwVersion));
        }
    }

    public String checkForUpdate() {
        try {
            ShellyOtaCheckResult result = api.checkForUpdate();
            return result.status;
        } catch (ShellyApiException e) {
            return "";
        }
    }

    public void startCoap(ShellyThingConfiguration config, ShellyDeviceProfile profile) throws ShellyApiException {
        if (coap == null || !config.eventsCoIoT) {
            return;
        }
        if (profile.settings.coiot != null && profile.settings.coiot.enabled != null) {
            String devpeer = getString(profile.settings.coiot.peer);
            String ourpeer = config.localIp + ":" + Shelly1CoapJSonDTO.COIOT_PORT;
            if (!profile.settings.coiot.enabled || (profile.isMotion && devpeer.isEmpty())) {
                try {
                    api.setCoIoTPeer(ourpeer);
                    logger.info("{}: CoIoT peer updated to {}", thingName, ourpeer);
                } catch (ShellyApiException e) {
                    logger.debug("{}: Unable to set CoIoT peer: {}", thingName, e.toString());
                }
            } else if (!devpeer.isEmpty() && !devpeer.equals(ourpeer)) {
                logger.warn("{}: CoIoT peer in device settings does not point this to this host", thingName);
            }
        }
        if (autoCoIoT) {
            logger.debug("{}: Auto-CoIoT is enabled, disabling action urls", thingName);
            config.eventsCoIoT = true;
            config.eventsSwitch = false;
            config.eventsButton = false;
            config.eventsPush = false;
            config.eventsRoller = false;
            config.eventsSensorReport = false;
            api.setConfig(thingName, config);
        }

        logger.debug("{}: Starting CoIoT (autoCoIoT={}/{})", thingName, bindingConfig.autoCoIoT, autoCoIoT);
        if (coap != null) {
            coap.start(thingName, config);
        }
    }

    /**
     * Change type of this thing.
     *
     * @param thingType thing type acc. to the xml definition
     * @param mode Device mode (e.g. relay, roller)
     */
    protected void changeThingType(String thingType, String mode) {
        String deviceType = substringBefore(thingType, "-");
        ThingTypeUID thingTypeUID = ShellyThingCreator.getThingTypeUID(thingType, deviceType, mode);
        if (!thingTypeUID.equals(THING_TYPE_SHELLYUNKNOWN)) {
            logger.debug("{}: Changing thing type to {}", getThing().getLabel(), thingTypeUID);
            Map<String, String> properties = editProperties();
            properties.replace(PROPERTY_DEV_TYPE, deviceType);
            properties.replace(PROPERTY_DEV_MODE, mode);
            updateProperties(properties);
            changeThingType(thingTypeUID, getConfig());
        } else {
            logger.debug("{}:  to {}", thingName, thingType);
            setThingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Unable to change thing type to " + thingType);
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("{}: Channel definitions updated.", thingName);
        super.thingUpdated(thing);
    }

    /**
     * Start the background updates
     */
    protected void startUpdateJob() {
        ScheduledFuture<?> statusJob = this.statusJob;
        if ((statusJob == null) || statusJob.isCancelled()) {
            this.statusJob = scheduler.scheduleWithFixedDelay(this::refreshStatus, 2, UPDATE_STATUS_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
            logger.debug("{}: Update status job started, interval={}*{}={}sec.", thingName, skipCount,
                    UPDATE_STATUS_INTERVAL_SECONDS, skipCount * UPDATE_STATUS_INTERVAL_SECONDS);
        }
    }

    /**
     * Flag the status job to do an exceptional update (something happened) rather
     * than waiting until the next regular poll
     *
     * @param requestCount number of polls to execute
     * @param refreshSettings true=force a /settings query
     * @return true=Update schedule, false=skipped (too many updates already
     *         scheduled)
     */
    @Override
    public boolean requestUpdates(int requestCount, boolean refreshSettings) {
        this.refreshSettings |= refreshSettings;
        if (refreshSettings) {
            if (requestCount == 0) {
                logger.debug("{}: Request settings refresh", thingName);
            }
            scheduledUpdates = 1;
            return true;
        }
        if (scheduledUpdates < 10) { // < 30s
            scheduledUpdates += requestCount;
            return true;
        }
        return false;
    }

    /**
     * Map input states to channels
     *
     * @param status Shelly device status
     * @return true: one or more inputs were updated
     */
    @Override
    public boolean updateInputs(ShellySettingsStatus status) {
        boolean updated = false;

        if (status.inputs != null) {
            if (!areChannelsCreated()) {
                updateChannelDefinitions(ShellyChannelDefinitions.createInputChannels(thing, profile, status));
            }

            int idx = 0;
            boolean multiInput = !profile.isIX && status.inputs.size() >= 2; // device has multiple SW (inputs)
            for (ShellyInputState input : status.inputs) {
                String group = profile.getInputGroup(idx);
                String suffix = multiInput ? profile.getInputSuffix(idx) : "";
                updated |= updateChannel(group, CHANNEL_INPUT + suffix, getOnOff(input.input));
                if (input.event != null) {
                    updated |= updateChannel(group, CHANNEL_STATUS_EVENTTYPE + suffix, getStringType(input.event));
                    updated |= updateChannel(group, CHANNEL_STATUS_EVENTCOUNT + suffix, getDecimal(input.eventCount));
                }
                idx++;
            }
        } else {
            if (status.input != null) {
                // RGBW2: a single int rather than an array
                return updateChannel(profile.getControlGroup(0), CHANNEL_INPUT,
                        OnOffType.from(getInteger(status.input) != 0));
            }
        }
        return updated;
    }

    @Override
    public boolean updateWakeupReason(@Nullable List<Object> valueArray) {
        boolean changed = false;
        if (valueArray != null && !valueArray.isEmpty()) {
            String reason = getString((String) valueArray.get(0));
            String newVal = valueArray.toString();
            changed = updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_WAKEUP, getStringType(reason));
            changed |= !lastWakeupReason.isEmpty() && !lastWakeupReason.equals(newVal);
            if (changed) {
                postEvent(reason.toUpperCase(), true);
            }
            lastWakeupReason = newVal;
        }
        return changed;
    }

    @Override
    public void triggerButton(String group, int idx, String value) {
        String trigger = mapButtonEvent(value);
        if (trigger.isEmpty()) {
            return;
        }

        logger.debug("{}: Update button state with {}/{}", thingName, value, trigger);
        triggerChannel(group,
                profile.isRoller ? CHANNEL_EVENT_TRIGGER : CHANNEL_BUTTON_TRIGGER + profile.getInputSuffix(idx),
                trigger);
        updateChannel(group, CHANNEL_LAST_UPDATE, getTimestamp());
        if (profile.alwaysOn) {
            // refresh status of the input channel
            requestUpdates(1, false);
        }
    }

    @Override
    public void publishState(String channelId, State value) {
        String id = channelId.contains("$") ? substringBefore(channelId, "$") : channelId;
        if (!stopping && isLinked(id)) {
            updateState(id, value);
            logger.debug("{}: Channel {} updated with {} (type {}).", thingName, channelId, value, value.getClass());
        }
    }

    @Override
    public boolean updateChannel(String group, String channel, State value) {
        return updateChannel(mkChannelId(group, channel), value, false);
    }

    @Override
    public boolean updateChannel(String channelId, State value, boolean force) {
        return !stopping && cache.updateChannel(channelId, value, force);
    }

    @Override
    public State getChannelValue(String group, String channel) {
        return cache.getValue(group, channel);
    }

    @Override
    public double getChannelDouble(String group, String channel) {
        State value = getChannelValue(group, channel);
        if (value != UnDefType.NULL) {
            if (value instanceof QuantityType<?> quantityCommand) {
                return quantityCommand.toBigDecimal().doubleValue();
            }
            if (value instanceof DecimalType decimalCommand) {
                return decimalCommand.doubleValue();
            }
        }
        return -1;
    }

    /**
     * Update Thing's channels according to available status information from the API
     *
     * @param dynChannels
     */
    @Override
    public void updateChannelDefinitions(Map<String, Channel> dynChannels) {
        if (channelsCreated) {
            return; // already done
        }

        try {
            // Get subset of those channels that currently do not exist
            List<Channel> existingChannels = getThing().getChannels();
            for (Channel channel : existingChannels) {
                String id = channel.getUID().getId();
                if (dynChannels.containsKey(id)) {
                    dynChannels.remove(id);
                }
            }

            if (!dynChannels.isEmpty()) {
                logger.debug("{}: Updating channel definitions, {} channels", thingName, dynChannels.size());
                ThingBuilder thingBuilder = editThing();
                for (Map.Entry<String, Channel> channel : dynChannels.entrySet()) {
                    Channel c = channel.getValue();
                    logger.debug("{}: Adding channel {}", thingName, c.getUID().getId());
                    thingBuilder.withChannel(c);
                }
                updateThing(thingBuilder.build());
                logger.debug("{}: Channel definitions updated", thingName);
            }
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Unable to update channel definitions", thingName, e);
        }
    }

    @Override
    public boolean areChannelsCreated() {
        return channelsCreated;
    }

    /**
     * Update thing properties with dynamic values
     *
     * @param profile The device profile
     * @param status the /status result
     */
    public void updateProperties(ShellyDeviceProfile profile, ShellySettingsStatus status) {
        Map<String, Object> properties = fillDeviceProperties(profile);
        String deviceName = getString(profile.settings.name);
        properties.put(PROPERTY_SERVICE_NAME, config.serviceName);
        properties.put(PROPERTY_DEV_AUTH, getBool(profile.device.auth) ? "yes" : "no");
        if (!deviceName.isEmpty()) {
            properties.put(PROPERTY_DEV_NAME, deviceName);
        }

        // add status properties
        if (status.wifiSta != null) {
            properties.put(PROPERTY_WIFI_NETW, getString(status.wifiSta.ssid));
        }
        if (status.update != null) {
            properties.put(PROPERTY_UPDATE_STATUS, getString(status.update.status));
            properties.put(PROPERTY_UPDATE_AVAILABLE, getBool(status.update.hasUpdate) ? "yes" : "no");
            properties.put(PROPERTY_UPDATE_CURR_VERS, getString(status.update.oldVersion));
            properties.put(PROPERTY_UPDATE_NEW_VERS, getString(status.update.newVersion));
        }
        properties.put(PROPERTY_COIOTAUTO, String.valueOf(autoCoIoT));

        Map<String, String> thingProperties = new TreeMap<>();
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            thingProperties.put(property.getKey(), (String) property.getValue());
        }
        flushProperties(thingProperties);
    }

    /**
     * Add one property to the Thing Properties
     *
     * @param key Name of the property
     * @param value Value of the property
     */
    @Override
    public void updateProperties(String key, String value) {
        Map<String, String> thingProperties = editProperties();
        if (thingProperties.containsKey(key)) {
            thingProperties.replace(key, value);
        } else {
            thingProperties.put(key, value);
        }
        updateProperties(thingProperties);
        logger.trace("{}: Properties updated", thingName);
    }

    public void flushProperties(Map<String, String> propertyUpdates) {
        Map<String, String> thingProperties = editProperties();
        for (Map.Entry<String, String> property : propertyUpdates.entrySet()) {
            if (thingProperties.containsKey(property.getKey())) {
                thingProperties.replace(property.getKey(), property.getValue());
            } else {
                thingProperties.put(property.getKey(), property.getValue());
            }
        }
        updateProperties(thingProperties);
    }

    /**
     * Get one property from the Thing Properties
     *
     * @param key property name
     * @return property value or "" if property is not set
     */
    @Override
    public String getProperty(String key) {
        Map<String, String> thingProperties = getThing().getProperties();
        return getString(thingProperties.get(key));
    }

    /**
     * Fill Thing Properties with device attributes
     *
     * @param profile Property Map to full
     * @return a full property map
     */
    public static Map<String, Object> fillDeviceProperties(ShellyDeviceProfile profile) {
        Map<String, Object> properties = new TreeMap<>();
        properties.put(PROPERTY_VENDOR, VENDOR);
        if (profile.isInitialized()) {
            properties.put(PROPERTY_MODEL_ID, getString(profile.device.type));
            properties.put(PROPERTY_MAC_ADDRESS, profile.device.mac);
            properties.put(PROPERTY_FIRMWARE_VERSION, profile.fwVersion + "/" + profile.fwDate);
            properties.put(PROPERTY_DEV_MODE, profile.device.mode);
            if (profile.hasRelays) {
                properties.put(PROPERTY_NUM_RELAYS, String.valueOf(profile.numRelays));
                properties.put(PROPERTY_NUM_ROLLERS, String.valueOf(profile.numRollers));
                properties.put(PROPERTY_NUM_METER, String.valueOf(profile.numMeters));
            }
            properties.put(PROPERTY_UPDATE_PERIOD, String.valueOf(profile.updatePeriod));
            if (!profile.hwRev.isEmpty()) {
                properties.put(PROPERTY_HWREV, profile.hwRev);
                properties.put(PROPERTY_HWBATCH, profile.hwBatchId);
            }
        }
        return properties;
    }

    /**
     * Return device profile.
     *
     * @param forceRefresh true=force refresh before returning, false=return without
     *            refresh
     * @return ShellyDeviceProfile instance
     * @throws ShellyApiException
     */
    @Override
    public ShellyDeviceProfile getProfile(boolean forceRefresh) throws ShellyApiException {
        try {
            refreshSettings |= forceRefresh;
            if (refreshSettings) {
                profile = api.getDeviceProfile(thingType, null);
                if (!isThingOnline()) {
                    logger.debug("{}: Device profile re-initialized (thingType={})", thingName, thingType);
                }
            }
        } finally {
            refreshSettings = false;
        }
        return profile;
    }

    @Override
    public ShellyDeviceProfile getProfile() {
        return profile;
    }

    @Override
    public @Nullable List<StateOption> getStateOptions(ChannelTypeUID uid) {
        List<StateOption> options = channelDefinitions.getStateOptions(uid);
        if (!options.isEmpty()) {
            logger.debug("{}: Return {} state options for channel uid {}", thingName, options.size(), uid.getId());
            return options;
        }
        return null;
    }

    protected ShellyDeviceProfile getDeviceProfile() {
        return profile;
    }

    @Override
    public void triggerChannel(String group, String channel, String payload) {
        String triggerCh = mkChannelId(group, channel);
        logger.debug("{}: Send event {} to channel {}", thingName, triggerCh, payload);
        if (EVENT_TYPE_VIBRATION.contentEquals(payload)) {
            if (vibrationFilter == 0) {
                vibrationFilter = VIBRATION_FILTER_SEC / UPDATE_STATUS_INTERVAL_SECONDS + 1;
                logger.debug("{}: Duplicate vibration events will be absorbed for the next {} sec", thingName,
                        vibrationFilter * UPDATE_STATUS_INTERVAL_SECONDS);
            } else {
                logger.debug("{}: Vibration event absorbed, {} sec remaining", thingName,
                        vibrationFilter * UPDATE_STATUS_INTERVAL_SECONDS);
                return;
            }
        }

        triggerChannel(triggerCh, payload);
    }

    public void stop() {
        logger.debug("{}: Shutting down", thingName);
        ScheduledFuture<?> job = this.initJob;
        if (job != null) {
            job.cancel(true);
            initJob = null;
        }
        job = this.statusJob;
        if (job != null) {
            job.cancel(true);
            statusJob = null;
            logger.debug("{}: Shelly statusJob stopped", thingName);
        }
        api.close();
        profile.initialized = false;
    }

    /**
     * Shutdown thing, make sure background jobs are canceled
     */
    @Override
    public void dispose() {
        logger.debug("{}: Stopping Thing", thingName);
        stopping = true;
        stop();
        super.dispose();
    }

    /**
     * Device specific command handlers are overriding this method to do additional stuff
     */
    public boolean handleDeviceCommand(ChannelUID channelUID, Command command) throws ShellyApiException {
        return false;
    }

    public String getUID() {
        return getThing().getUID().getAsString();
    }

    /**
     * Device specific handlers are overriding this method to do additional stuff
     */
    public boolean updateDeviceStatus(ShellySettingsStatus status) throws ShellyApiException {
        return false;
    }

    @Override
    public String getThingName() {
        return thingName;
    }

    @Override
    public void resetStats() {
        // reset statistics
        stats = new ShellyDeviceStats();
    }

    @Override
    public ShellyDeviceStats getStats() {
        return stats;
    }

    @Override
    public ShellyApiInterface getApi() {
        return api;
    }

    @Override
    public long getScheduledUpdates() {
        return scheduledUpdates;
    }

    public Map<String, String> getStatsProp() {
        return stats.asProperties();
    }

    @Override
    public void triggerUpdateFromCoap() {
        if ((!autoCoIoT && (getScheduledUpdates() < 1)) || (autoCoIoT && !profile.isLight && !profile.hasBattery)) {
            requestUpdates(1, false);
        }
    }
}
