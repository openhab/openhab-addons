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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.*;
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
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiResult;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpApi;
import org.openhab.binding.shelly.internal.coap.ShellyCoapHandler;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO;
import org.openhab.binding.shelly.internal.coap.ShellyCoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.discovery.ShellyThingCreator;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.binding.shelly.internal.util.ShellyChannelCache;
import org.openhab.binding.shelly.internal.util.ShellyVersionDTO;
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
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
public class ShellyBaseHandler extends BaseThingHandler implements ShellyDeviceListener, ShellyManagerInterface {
    protected final Logger logger = LoggerFactory.getLogger(ShellyBaseHandler.class);
    protected final ShellyChannelDefinitions channelDefinitions;

    public String thingName = "";
    public String thingType = "";

    protected final ShellyHttpApi api;
    protected ShellyBindingConfiguration bindingConfig;
    protected ShellyThingConfiguration config = new ShellyThingConfiguration();
    protected ShellyDeviceProfile profile = new ShellyDeviceProfile(); // init empty profile to avoid NPE
    protected ShellyDeviceStats stats = new ShellyDeviceStats();
    private final ShellyCoapHandler coap;
    public boolean autoCoIoT = false;

    public final ShellyTranslationProvider messages;
    protected boolean stopping = false;
    private boolean channelsCreated = false;

    private long watchdog = now();

    private @Nullable ScheduledFuture<?> statusJob;
    public int scheduledUpdates = 0;
    private int skipCount = UPDATE_SKIP_COUNT;
    private int skipUpdate = 0;
    private boolean refreshSettings = false;

    // delay before enabling channel
    private final int cacheCount = UPDATE_SETTINGS_INTERVAL_SECONDS / UPDATE_STATUS_INTERVAL_SECONDS;
    protected final ShellyChannelCache cache;

    private String localIP = "";
    private String localPort = "";

    private String lastWakeupReason = "";
    private int vibrationFilter = 0;

    /**
     * Constructor
     *
     * @param thing The Thing object
     * @param bindingConfig The binding configuration (beside thing
     *            configuration)
     * @param coapServer coap server instance
     * @param localIP local IP address from networkAddressService
     * @param httpPort from httpService
     */
    public ShellyBaseHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, final ShellyCoapServer coapServer, final String localIP,
            int httpPort, final HttpClient httpClient) {
        super(thing);

        this.messages = translationProvider;
        this.cache = new ShellyChannelCache(this);
        this.channelDefinitions = new ShellyChannelDefinitions(messages);
        this.bindingConfig = bindingConfig;

        this.localIP = localIP;
        this.localPort = String.valueOf(httpPort);
        this.api = new ShellyHttpApi(thingName, config, httpClient);

        coap = new ShellyCoapHandler(this, coapServer);
    }

    /**
     * Schedule asynchronous Thing initialization, register thing to event dispatcher
     */
    @Override
    public void initialize() {
        // start background initialization:
        scheduler.schedule(() -> {
            boolean start = true;
            try {
                initializeThingConfig();
                logger.debug("{}: Device config: IP address={}, HTTP user/password={}/{}, update interval={}",
                        thingName, config.deviceIp, config.userId.isEmpty() ? "<non>" : config.userId,
                        config.password.isEmpty() ? "<none>" : "***", config.updateInterval);
                logger.debug(
                        "{}: Configured Events: Button: {}, Switch (on/off): {}, Push: {}, Roller: {}, Sensor: {}, CoIoT: {}, Enable AutoCoIoT: {}",
                        thingName, config.eventsButton, config.eventsSwitch, config.eventsPush, config.eventsRoller,
                        config.eventsSensorReport, config.eventsCoIoT, bindingConfig.autoCoIoT);
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                        messages.get("status.unknown.initializing"));
                start = initializeThing();
            } catch (ShellyApiException e) {
                ShellyApiResult res = e.getApiResult();
                if (isAuthorizationFailed(res)) {
                    start = false;
                }
                logger.debug("{}: Unable to initialize: {}, retrying later", thingName, e.toString());
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

    /**
     * This routine is called every time the Thing configuration has been changed
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        logger.debug("{}: Thing config updated, re-initialize", thingName);
        coap.stop();
        requestUpdates(1, true);// force re-initialization
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
    private boolean initializeThing() throws ShellyApiException {
        // Init from thing type to have a basic profile, gets updated when device info is received from API
        stopping = false;
        refreshSettings = false;
        lastWakeupReason = "";
        profile.initFromThingType(thingType);
        api.setConfig(thingName, config);
        cache.setThingName(thingName);
        cache.clear();

        logger.debug("{}: Start initializing thing {}, type {}, ip address {}, CoIoT: {}", thingName,
                getThing().getLabel(), thingType, config.deviceIp, config.eventsCoIoT);
        if (config.deviceIp.isEmpty()) {
            setThingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "config-status.error.missing-device-ip");
            return false;
        }

        // Setup CoAP listener to we get the CoAP message, which triggers initialization even the thing could not be
        // fully initialized here. In this case the CoAP messages triggers auto-initialization (like the Action URL does
        // when enabled)
        if (config.eventsCoIoT && !profile.alwaysOn) {
            coap.start(thingName, config);
        }

        // Initialize API access, exceptions will be catched by initialize()
        ShellySettingsDevice devInfo = api.getDevInfo();
        if (devInfo.auth && config.userId.isEmpty()) {
            setThingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "offline.conf-error-no-credentials");
            return false;
        }

        ShellyDeviceProfile tmpPrf = api.getDeviceProfile(thingType);
        if (this.getThing().getThingTypeUID().equals(THING_TYPE_SHELLYPROTECTED)) {
            changeThingType(thingName, tmpPrf.mode);
            return false; // force re-initialization
        }
        // Validate device mode
        String reqMode = thingType.contains("-") ? substringAfter(thingType, "-") : "";
        if (!reqMode.isEmpty() && !tmpPrf.mode.equals(reqMode)) {
            setThingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "offline.conf-error-wrong-mode");
            return false;
        }
        if (!getString(devInfo.coiot).isEmpty()) {
            // New Shelly devices might use a different endpoint for the CoAP listener
            tmpPrf.coiotEndpoint = devInfo.coiot;
        }
        tmpPrf.auth = devInfo.auth; // missing in /settings

        logger.debug("{}: Initializing device {}, type {}, Hardware: Rev: {}, batch {}; Firmware: {} / {}", thingName,
                tmpPrf.hostname, tmpPrf.deviceType, tmpPrf.hwRev, tmpPrf.hwBatchId, tmpPrf.fwVersion, tmpPrf.fwDate);
        logger.debug("{}: Shelly settings info for {}: {}", thingName, tmpPrf.hostname, tmpPrf.settingsJson);
        logger.debug("{}: Device "
                + "hasRelays:{} (numRelays={}),isRoller:{} (numRoller={}),isDimmer:{},numMeter={},isEMeter:{})"
                + ",isSensor:{},isDS:{},hasBattery:{}{},isSense:{},isMotion:{},isLight:{},isBulb:{},isDuo:{},isRGBW2:{},inColor:{}"
                + ",alwaysOn:{}, ,updatePeriod:{}sec", thingName, tmpPrf.hasRelays, tmpPrf.numRelays, tmpPrf.isRoller,
                tmpPrf.numRollers, tmpPrf.isDimmer, tmpPrf.numMeters, tmpPrf.isEMeter, tmpPrf.isSensor, tmpPrf.isDW,
                tmpPrf.hasBattery, tmpPrf.hasBattery ? " (low battery threshold=" + config.lowBattery + "%)" : "",
                tmpPrf.isSense, tmpPrf.isMotion, tmpPrf.isLight, profile.isBulb, tmpPrf.isDuo, tmpPrf.isRGBW2,
                tmpPrf.inColor, tmpPrf.alwaysOn, tmpPrf.updatePeriod);

        // update thing properties
        tmpPrf.status = api.getStatus();
        tmpPrf.updateFromStatus(tmpPrf.status);
        updateProperties(tmpPrf, tmpPrf.status);
        checkVersion(tmpPrf, tmpPrf.status);
        if (config.eventsCoIoT && (tmpPrf.settings.coiot != null) && (tmpPrf.settings.coiot.enabled != null)) {
            String devpeer = getString(tmpPrf.settings.coiot.peer);
            String ourpeer = config.localIp + ":" + ShellyCoapJSonDTO.COIOT_PORT;
            if (!tmpPrf.settings.coiot.enabled || (profile.isMotion && devpeer.isEmpty())) {
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

        // All initialization done, so keep the profile and set Thing to ONLINE
        fillDeviceStatus(tmpPrf.status, false);
        postEvent(ALARM_TYPE_NONE, false);
        api.setActionURLs(); // register event urls
        if (config.eventsCoIoT) {
            logger.debug("{}: Starting CoIoT (autoCoIoT={}/{})", thingName, bindingConfig.autoCoIoT, autoCoIoT);
            coap.start(thingName, config);
        }

        logger.debug("{}: Thing successfully initialized.", thingName);
        profile = tmpPrf;
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
                    int value = ((DecimalType) command).intValue();
                    value = value > 0 ? Math.max(SHELLY_MOTION_SLEEPTIME_OFFSET, value - SHELLY_MOTION_SLEEPTIME_OFFSET)
                            : 0;
                    api.setSleepTime(value);
                    break;

                default:
                    update = handleDeviceCommand(channelUID, command);
                    break;
            }

            restartWatchdog();
            if (update && !autoCoIoT) {
                requestUpdates(1, false);
            }
        } catch (ShellyApiException e) {
            ShellyApiResult res = e.getApiResult();
            if (isAuthorizationFailed(res)) {
                return;
            }
            if (res.isNotCalibrtated()) {
                logger.warn("{}: {}", thingName, messages.get("roller.calibrating"));
            } else {
                logger.warn("{}: {} - {}", thingName, messages.get("command.failed", command, channelUID),
                        e.toString());
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
                logger.debug("{}: Vibration events are absorbed for {} more seconds", thingName,
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
                // Get profile, if refreshSettings == true reload settings from device
                logger.trace("{}: Updating status (refreshSettings={})", thingName, refreshSettings);
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

                // If status update was successful the thing must be online
                setThingOnline();

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

                // Restart watchdog when status update was successful (no exception)
                restartWatchdog();
            }
        } catch (ShellyApiException e) {
            // http call failed: go offline except for battery devices, which might be in
            // sleep mode. Once the next update is successful the device goes back online
            String status = "";
            ShellyApiResult res = e.getApiResult();
            if (isWatchdogStarted()) {
                if (!isWatchdogExpired()) {
                    logger.debug("{}: Ignore API Timeout, retry later", thingName);
                } else {
                    if (isThingOnline()) {
                        status = "offline.status-error-watchdog";
                    }
                }
            } else if (res.isHttpAccessUnauthorized()) {
                status = "offline.conf-error-access-denied";
            } else if (e.isJSONException()) {
                status = "offline.status-error-unexpected-api-result";
                logger.debug("{}: Unable to parse API response: {}; json={}", thingName, res.getUrl(), res.response, e);
            } else if (res.isHttpTimeout()) {
                // Watchdog not started, e.g. device in sleep mode
                if (isThingOnline()) { // ignore when already offline
                    status = "offline.status-error-watchdog";
                }
            } else {
                status = "offline.status-error-unexpected-api-result";
                logger.debug("{}: Unexpected API result: {}", thingName, res.response, e);
            }

            if (!status.isEmpty()) {
                setThingOffline(ThingStatusDetail.COMMUNICATION_ERROR, status);
            }
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

    public boolean isThingOnline() {
        return getThing().getStatus() == ThingStatus.ONLINE;
    }

    public boolean isThingOffline() {
        return getThing().getStatus() == ThingStatus.OFFLINE;
    }

    @Override
    public void setThingOnline() {
        if (!isThingOnline()) {
            updateStatus(ThingStatus.ONLINE);

            // request 3 updates in a row (during the first 2+3*3 sec)
            requestUpdates(profile.alwaysOn ? 3 : 1, !channelsCreated);
        }
        restartWatchdog();
    }

    @Override
    public void setThingOffline(ThingStatusDetail detail, String messageKey) {
        if (!isThingOffline()) {
            logger.info("{}: Thing goes OFFLINE: {}", thingName, messages.get(messageKey));
            updateStatus(ThingStatus.OFFLINE, detail, "@text/" + messageKey);
            watchdog = 0;
            channelsCreated = false; // check for new channels after devices gets re-initialized (e.g. new
        }
    }

    public synchronized void restartWatchdog() {
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

    private boolean isWatchdogStarted() {
        return watchdog > 0;
    }

    public void reinitializeThing() {
        logger.debug("{}: Re-Initialize Thing", thingName);
        updateStatus(ThingStatus.UNKNOWN);
        requestUpdates(0, true);
    }

    private void fillDeviceStatus(ShellySettingsStatus status, boolean updated) {
        String alarm = "";

        // Update uptime and WiFi, internal temp
        ShellyComponents.updateDeviceStatus(this, status);
        stats.wifiRssi = status.wifiSta.rssi;

        if (api.isInitialized()) {
            stats.timeoutErrors = api.getTimeoutErrors();
            stats.timeoutsRecorvered = api.getTimeoutsRecovered();
        }
        stats.remainingWatchdog = watchdog > 0 ? now() - watchdog : 0;

        // Check various device indicators like overheating
        if (checkRestarted(status)) {
            // Force re-initialization on next status update
            if (profile.alwaysOn) {
                reinitializeThing();
            }
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
                logger.debug("{}: Max Internal Temp for device changed to {}", thingName, temp);
                stats.maxInternalTemp = temp;
            }
        }

        stats.lastUptime = getLong(status.uptime);
        stats.coiotMessages = coap.getMessageCount();
        stats.coiotErrors = coap.getErrorCount();

        if (!alarm.isEmpty()) {
            postEvent(alarm, false);
        }
    }

    /**
     * Check if device has restarted and needs a new Thing initialization
     *
     * @return true: restart detected
     */

    private boolean checkRestarted(ShellySettingsStatus status) {
        if (profile.isInitialized() && profile.alwaysOn /* exclude battery powered devices */
                && (status.uptime < stats.lastUptime || !profile.status.update.oldVersion.isEmpty()
                        && !status.update.oldVersion.equals(profile.status.update.oldVersion))) {
            updateProperties(profile, status);
            return true;
        }
        return false;
    }

    /**
     * Save alarm to the lastAlarm channel
     *
     * @param alarm Alarm Message
     */
    public void postEvent(String alarm, boolean force) {
        String channelId = mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ALARM);
        State value = cache.getValue(channelId);
        String lastAlarm = value != UnDefType.NULL ? value.toString() : "";

        if (force || !lastAlarm.equals(alarm)
                || (lastAlarm.equals(alarm) && now() > stats.lastAlarmTs + HEALTH_CHECK_INTERVAL_SEC)) {
            if (alarm.isEmpty() || alarm.equals(ALARM_TYPE_NONE)) {
                cache.updateChannel(channelId, getStringType(alarm));
            } else {
                logger.info("{}: {}", thingName, messages.get("event.triggered", alarm));
                triggerChannel(channelId, alarm);
                cache.updateChannel(channelId, getStringType(alarm));
                stats.lastAlarm = alarm;
                stats.lastAlarmTs = now();
                stats.alarms++;
            }
        }
    }

    /**
     * Callback for device events
     *
     * @param deviceName device receiving the event
     * @param parameters parameters from the event URL
     * @param data the HTML input data
     * @return true if event was processed
     */
    @Override
    public boolean onEvent(String ipAddress, String deviceName, String deviceIndex, String type,
            Map<String, String> parameters) {
        if (thingName.equalsIgnoreCase(deviceName) || config.deviceIp.equals(ipAddress)) {
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
                boolean isButton = profile.inButtonMode(idx - 1);
                switch (event) {
                    case SHELLY_EVENT_SHORTPUSH:
                    case SHELLY_EVENT_DOUBLE_SHORTPUSH:
                    case SHELLY_EVENT_TRIPLE_SHORTPUSH:
                    case SHELLY_EVENT_LONGPUSH:
                        if (isButton) {
                            triggerButton(group, idx, mapButtonEvent(event));
                            channel = CHANNEL_BUTTON_TRIGGER + profile.getInputSuffix(idx);
                            payload = ShellyApiJsonDTO.mapButtonEvent(event);
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
                                event.equalsIgnoreCase(SHELLY_EVENT_FLOOD_DETECTED) ? OnOffType.ON : OnOffType.OFF);
                        break;

                    case SHELLY_EVENT_CLOSE: // DW 1.7
                    case SHELLY_EVENT_OPEN: // DW 1.7
                        updateChannel(group, CHANNEL_SENSOR_CONTACT,
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
                    updateChannel(group, onoff, event.toLowerCase().contains("_on") ? OnOffType.ON : OnOffType.OFF);
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
    protected void initializeThingConfig() {
        thingType = getThing().getThingTypeUID().getId();
        final Map<String, String> properties = getThing().getProperties();
        thingName = getString(properties.get(PROPERTY_SERVICE_NAME));
        if (thingName.isEmpty()) {
            thingName = getString(thingType + "-" + getString(getThing().getUID().getId())).toLowerCase();
            logger.debug("{}: Thing name derived from UID {}", thingName, getString(getThing().getUID().toString()));
        }

        config = getConfigAs(ShellyThingConfiguration.class);
        if (config.deviceIp.isEmpty()) {
            logger.info("{}: IP address for the device must not be empty", thingName); // may not set in .things file
            return;
        }
        try {
            InetAddress addr = InetAddress.getByName(config.deviceIp);
            String saddr = addr.getHostAddress();
            if (!config.deviceIp.equals(saddr)) {
                logger.debug("{}: hostname {} resolved to IP address {}", thingName, config.deviceIp, saddr);
                config.deviceIp = saddr;
            }
        } catch (UnknownHostException e) {
            logger.debug("{}: Unable to resolve hostname {}", thingName, config.deviceIp);
        }

        config.localIp = localIP;
        config.localPort = localPort;
        if (config.userId.isEmpty() && !bindingConfig.defaultUserId.isEmpty()) {
            config.userId = bindingConfig.defaultUserId;
            config.password = bindingConfig.defaultPassword;
            logger.debug("{}: Using userId {} from bindingConfig", thingName, config.userId);
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
    }

    private void checkVersion(ShellyDeviceProfile prf, ShellySettingsStatus status) {
        try {
            ShellyVersionDTO version = new ShellyVersionDTO();
            if (version.checkBeta(getString(prf.fwVersion))) {
                logger.info("{}: {}", prf.hostname, messages.get("versioncheck.beta", prf.fwVersion, prf.fwDate));
            } else {
                if ((version.compare(prf.fwVersion, SHELLY_API_MIN_FWVERSION) < 0) && !profile.isMotion) {
                    logger.warn("{}: {}", prf.hostname,
                            messages.get("versioncheck.tooold", prf.fwVersion, prf.fwDate, SHELLY_API_MIN_FWVERSION));
                }
            }
            if (bindingConfig.autoCoIoT && ((version.compare(prf.fwVersion, SHELLY_API_MIN_FWCOIOT)) >= 0)
                    || (prf.fwVersion.equalsIgnoreCase("production_test"))) {
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

    /**
     * Checks the http response for authorization error.
     * If the authorization failed the binding can't access the device settings and determine the thing type. In this
     * case the thing type shelly-unknown is set.
     *
     * @param response exception details including the http respone
     * @return true if the authorization failed
     */
    private boolean isAuthorizationFailed(ShellyApiResult result) {
        if (result.isHttpAccessUnauthorized()) {
            // If the device is password protected the API doesn't provide settings to the device settings
            logger.info("{}: {}", thingName, messages.get("init.protected"));
            setThingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "offline.conf-error-access-denied");
            changeThingType(THING_TYPE_SHELLYPROTECTED_STR, "");
            return true;
        }
        return false;
    }

    /**
     * Change type of this thing.
     *
     * @param thingType thing type acc. to the xml definition
     * @param mode Device mode (e.g. relay, roller)
     */
    private void changeThingType(String thingType, String mode) {
        ThingTypeUID thingTypeUID = ShellyThingCreator.getThingTypeUID(thingType, "", mode);
        if (!thingTypeUID.equals(THING_TYPE_SHELLYUNKNOWN)) {
            logger.debug("{}: Changing thing type to {}", getThing().getLabel(), thingTypeUID);
            Map<String, String> properties = editProperties();
            properties.replace(PROPERTY_DEV_TYPE, thingType);
            properties.replace(PROPERTY_DEV_MODE, mode);
            updateProperties(properties);
            changeThingType(thingTypeUID, getConfig());
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
     * @param groupName Channel Group (relay / relay1...)
     *
     * @param status Shelly device status
     * @return true: one or more inputs were updated
     */
    public boolean updateInputs(ShellySettingsStatus status) {
        boolean updated = false;

        if (status.inputs != null) {
            int idx = 0;
            boolean multiInput = status.inputs.size() >= 2; // device has multiple SW (inputs)
            for (ShellyInputState input : status.inputs) {
                String group = profile.getControlGroup(idx);
                String suffix = multiInput ? profile.getInputSuffix(idx) : "";

                if (!areChannelsCreated()) {
                    updateChannelDefinitions(
                            ShellyChannelDefinitions.createInputChannels(thing, profile, status, group));
                }

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
                        getInteger(status.input) == 0 ? OnOffType.OFF : OnOffType.ON);
            }
        }
        return updated;
    }

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

    public void publishState(String channelId, State value) {
        String id = channelId.contains("$") ? substringBefore(channelId, "$") : channelId;
        if (!stopping && isLinked(id)) {
            updateState(id, value);
        }
    }

    public boolean updateChannel(String group, String channel, State value) {
        return updateChannel(mkChannelId(group, channel), value, false);
    }

    public boolean updateChannel(String channelId, State value, boolean force) {
        return !stopping && cache.updateChannel(channelId, value, force);
    }

    @Override
    public State getChannelValue(String group, String channel) {
        return cache.getValue(group, channel);
    }

    public double getChannelDouble(String group, String channel) {
        State value = getChannelValue(group, channel);
        if (value != UnDefType.NULL) {
            if (value instanceof QuantityType) {
                return ((QuantityType<?>) value).toBigDecimal().doubleValue();
            }
            if (value instanceof DecimalType) {
                return ((DecimalType) value).doubleValue();
            }
        }
        return -1;
    }

    /**
     * Update Thing's channels according to available status information from the API
     *
     * @param thingHandler
     */
    protected void updateChannelDefinitions(Map<String, Channel> dynChannels) {
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

    public boolean areChannelsCreated() {
        return channelsCreated;
    }

    /**
     * Update thing properties with dynamic values
     *
     * @param profile The device profile
     * @param status the /status result
     */
    protected void updateProperties(ShellyDeviceProfile profile, ShellySettingsStatus status) {
        logger.debug("{}: Update properties", thingName);
        Map<String, Object> properties = fillDeviceProperties(profile);
        String serviceName = getString(getThing().getProperties().get(PROPERTY_SERVICE_NAME));
        String hostname = getString(profile.settings.device.hostname).toLowerCase();
        if (serviceName.isEmpty()) {
            properties.put(PROPERTY_SERVICE_NAME, hostname);
            logger.trace("{}: Updated serrviceName to {}", thingName, hostname);
        }
        String deviceName = getString(profile.settings.name);
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
            properties.put(PROPERTY_MODEL_ID, getString(profile.settings.device.type));
            properties.put(PROPERTY_MAC_ADDRESS, profile.mac);
            properties.put(PROPERTY_FIRMWARE_VERSION, profile.fwVersion + "/" + profile.fwDate);
            properties.put(PROPERTY_DEV_MODE, profile.mode);
            properties.put(PROPERTY_NUM_RELAYS, String.valueOf(profile.numRelays));
            properties.put(PROPERTY_NUM_ROLLERS, String.valueOf(profile.numRollers));
            properties.put(PROPERTY_NUM_METER, String.valueOf(profile.numMeters));
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
     * @param ForceRefresh true=force refresh before returning, false=return without
     *            refresh
     * @return ShellyDeviceProfile instance
     * @throws ShellyApiException
     */
    @Override
    public ShellyDeviceProfile getProfile(boolean forceRefresh) throws ShellyApiException {
        try {
            refreshSettings |= forceRefresh;
            if (refreshSettings) {
                profile = api.getDeviceProfile(thingType);
                if (!isThingOnline()) {
                    logger.debug("{}:Device profile re-initialized (thingType={})", thingName, thingType);
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

    protected ShellyHttpApi getShellyApi() {
        return api;
    }

    protected ShellyDeviceProfile getDeviceProfile() {
        return profile;
    }

    public void triggerChannel(String group, String channel, String payload) {
        String triggerCh = mkChannelId(group, channel);
        logger.debug("{}: Send event {} to channel {}", thingName, triggerCh, payload);
        if (EVENT_TYPE_VIBRATION.contentEquals(payload)) {
            if (vibrationFilter == 0) {
                vibrationFilter = VIBRATION_FILTER_SEC / UPDATE_STATUS_INTERVAL_SECONDS + 1;
                logger.debug("{}: Duplicate vibration events will be absorbed for the next {} sec", thingName,
                        vibrationFilter * UPDATE_STATUS_INTERVAL_SECONDS);
            } else {
                logger.debug("{}: Vibration event absorbed, {} sec remaining", thingName,
                        vibrationFilter * UPDATE_STATUS_INTERVAL_SECONDS);
                return;
            }
        }

        triggerChannel(triggerCh, payload);
    }

    public void stop() {
        logger.debug("{}: Shutting down", thingName);
        ScheduledFuture<?> job = this.statusJob;
        if (job != null) {
            job.cancel(true);
            statusJob = null;
            logger.debug("{}: Shelly statusJob stopped", thingName);
        }

        coap.stop();
        profile.initialized = false;
    }

    /**
     * Shutdown thing, make sure background jobs are canceled
     */
    @Override
    public void dispose() {
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
    public ShellyHttpApi getApi() {
        return api;
    }

    public Map<String, String> getStatsProp() {
        return stats.asProperties();
    }

    public String checkForUpdate() {
        try {
            ShellyOtaCheckResult result = api.checkForUpdate();
            return result.status;
        } catch (ShellyApiException e) {
            return "";
        }
    }
}
