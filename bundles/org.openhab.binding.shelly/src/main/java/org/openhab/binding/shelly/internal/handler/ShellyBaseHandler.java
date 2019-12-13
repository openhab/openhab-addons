/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyUtils.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJson.*;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.getThingTypeUID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpApi;
import org.openhab.binding.shelly.internal.coap.ShellyCoapHandler;
import org.openhab.binding.shelly.internal.coap.ShellyCoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBaseHandler extends BaseThingHandler implements ShellyDeviceListener {
    protected final Logger logger = LoggerFactory.getLogger(ShellyBaseHandler.class);

    public String thingName = "";
    protected ShellyBindingConfiguration bindingConfig = new ShellyBindingConfiguration();
    protected ShellyThingConfiguration config = new ShellyThingConfiguration();
    protected @Nullable ShellyHttpApi api;
    private @Nullable ShellyCoapHandler coap;
    protected @Nullable ShellyDeviceProfile profile;
    private final @Nullable ShellyCoapServer coapServer;
    protected boolean lockUpdates = false;

    @SuppressWarnings("unused")
    private long lastUpdateTs = 0;
    private long lastUptime = 0;
    private long lastAlarmTs = 0;

    private @Nullable ScheduledFuture<?> statusJob;
    private int skipUpdate = 0;
    public int scheduledUpdates = 0;
    private int skipCount = UPDATE_SKIP_COUNT;

    // force settings refresh every x seconds
    private int refreshCount = UPDATE_SETTINGS_INTERVAL_SECONDS / UPDATE_STATUS_INTERVAL_SECONDS;
    private boolean refreshSettings = false;

    // delay before enabling channel
    private final int cacheCount = UPDATE_SETTINGS_INTERVAL_SECONDS / UPDATE_STATUS_INTERVAL_SECONDS;
    private boolean channelCache = false;
    private Map<String, Object> channelData = new HashMap<>();

    String localIP = "";
    int httpPort = -1;

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
    public ShellyBaseHandler(Thing thing, ShellyBindingConfiguration bindingConfig,
            @Nullable ShellyCoapServer coapServer, String localIP, int httpPort) {
        super(thing);

        this.bindingConfig = bindingConfig;
        this.coapServer = coapServer;
        this.localIP = localIP;
        this.httpPort = httpPort;
    }

    /**
     * Schedule asynchronous Thing initialization, register thing to event dispatcher
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        // start background initialization:
        scheduler.schedule(() -> {
            boolean start = true;
            try {
                initializeThingConfig();
                Validate.notNull(config);
                logger.debug("{}: Device config: ipAddress={}, http user/password={}/{}, update interval={}",
                        getThing().getLabel(), config.deviceIp, config.userId.isEmpty() ? "<non>" : config.userId,
                        config.password.isEmpty() ? "<none>" : "***", config.updateInterval);
                initializeThing();
            } catch (NullPointerException | IOException e) {
                if (authorizationFailed(e.getMessage())) {
                    start = false;
                }
                logger.debug("{}: Unable to initialize: {} ({}), retrying later\n{}", getThing().getLabel(),
                        e.getMessage(), e.getClass(), e.getStackTrace());
            } finally {
                // even this initialization failed we start the status update
                // the updateJob will then try to auto-initialize the thing
                // in this case the thing stays in status INITIALIZING
                if (start && (getThing().getStatus() != ThingStatus.OFFLINE)) {
                    startUpdateJob();
                }
            }
        }, 2, TimeUnit.SECONDS);
    }

    /**
     * This routine is called every time the Thing configuration has been changed (e.g. PaperUI)
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        logger.debug("{}: Thing config updated.", thingName);
        initializeThingConfig();
        startUpdateJob();
        refreshSettings = true; // force re-initialization
    }

    /**
     * Initialize Thing: Initialize API access, get settings and initialize Device Profile
     * If the device is password protected and the credentials are missing or don't match the API access will throw an
     * Exception. In this case the thing type will be changed to shelly-unknown. The user has the option to edit the
     * thing config and set the correct credentials. The thing type will be changed to the requested one if the
     * credentials are correct and the API access is initialized successful.
     *
     * @throws IOException e.g. http returned non-ok response, check e.getMessage() for details.
     */
    @SuppressWarnings("null")
    private void initializeThing() throws IOException {
        // Get the thing global settings and initialize device capabilities
        channelData = new HashMap<>(); // clear any cached channels
        refreshSettings = false;
        lockUpdates = false;

        Map<String, String> properties = getThing().getProperties();
        thingName = properties.get(PROPERTY_SERVICE_NAME) != null ? properties.get(PROPERTY_SERVICE_NAME).toLowerCase()
                : "";
        logger.debug("{}: Start initializing, ip address {}, CoIoT: {}", getThing().getLabel(), config.deviceIp,
                config.eventsCoIoT);

        // Initialize API access, exceptions will be catched by initialize()
        api = new ShellyHttpApi(config);
        ShellyDeviceProfile tmpPrf = api.getDeviceProfile(this.getThing().getThingTypeUID().getId());
        thingName = (!thingName.isEmpty() ? thingName : tmpPrf.hostname).toLowerCase();
        Validate.isTrue(!thingName.isEmpty(), "initializeThing(): thingName must not be empty!");

        if (this.getThing().getThingTypeUID().equals(THING_TYPE_SHELLYUNKNOWN)) {
            changeThingType(thingName, tmpPrf.mode);
            return; // force re-initialization
        }

        logger.debug("{}: Initializing device {}, type {}, Hardware: Rev: {}, batch {}; Firmware: {} / {} ({})",
                thingName, tmpPrf.hostname, tmpPrf.deviceType, tmpPrf.hwRev, tmpPrf.hwBatchId, tmpPrf.fwVersion,
                tmpPrf.fwDate, tmpPrf.fwId);
        logger.debug("{}: Shelly settings info: {}", thingName, tmpPrf.settingsJson);
        logger.debug(
                "{}: Device has relays: {} (numRelays={}, is roller: {} (numRoller={}), is Plug S: {}, is Dimmer: {}, "
                        + "has LEDs: {}, is Light: {}, has Meter: {} (numMeter={}, EMeter: {}), is Sensor: {}, is Sense: {}, has Battery: {} {}, "
                        + "event urls: btn:{},out:{},push{},roller:{},sensor:{}",
                tmpPrf.hostname, tmpPrf.hasRelays, tmpPrf.numRelays, tmpPrf.isRoller, tmpPrf.numRollers, tmpPrf.isPlugS,
                tmpPrf.isDimmer, tmpPrf.hasLed, tmpPrf.isLight, tmpPrf.hasMeter, tmpPrf.numMeters, tmpPrf.isEMeter,
                tmpPrf.isSensor, tmpPrf.isSense, tmpPrf.hasBattery,
                tmpPrf.hasBattery ? "(low battery threshold=" + config.lowBattery + "%)" : "",
                tmpPrf.supportsButtonUrls, tmpPrf.supportsOutUrls, tmpPrf.supportsPushUrls, tmpPrf.supportsRollerUrls,
                tmpPrf.supportsSensorUrls);

        // update thing properties
        ShellySettingsStatus status = api.getStatus();
        updateProperties(tmpPrf, status);
        if (tmpPrf.fwVersion.compareTo(SHELLY_API_MIN_FWVERSION) < 0) {
            logger.warn(
                    "{}: WARNING: Firmware might be too old (or beta release), installed: {}/{} ({}), required minimal {}. The binding was tested with version 1.50+ only. Older versions might work, but do not support all features or show technical issues. Please consider upgrading to v1.5.0 or newer!",
                    thingName, tmpPrf.fwVersion, tmpPrf.fwDate, tmpPrf.fwId, SHELLY_API_MIN_FWVERSION);
        }
        if (status.update.hasUpdate) {
            logger.info("{} - INFO: New firmware available: current version: {}, new version: {}", thingName,
                    status.update.oldVersion, status.update.newVersion);
        }

        // Set refresh interval for battery-powered devices to
        refreshCount = !tmpPrf.hasBattery
                ? refreshCount = UPDATE_SETTINGS_INTERVAL_SECONDS / UPDATE_STATUS_INTERVAL_SECONDS
                : skipCount;

        // register event urls
        api.setEventURLs();

        profile = tmpPrf; // all initialization done, so keep the profile

        // Validate device mode
        String thingType = getThing().getThingTypeUID().getId();
        String reqMode = thingType.contains("-") ? StringUtils.substringAfter(thingType, "-") : "";
        if (!reqMode.isEmpty() && !tmpPrf.mode.equals(reqMode)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Thing is in mode " + profile.mode + ", required is " + reqMode
                            + " - going offline. Re-run discovery to find the thing for the requested mode.");
            return;
        }

        if (config.eventsCoIoT && (coap == null)) {
            Validate.notNull(coapServer, "coapServer must not be null!");
            coap = new ShellyCoapHandler(config, this, coapServer);
        }
        if (coap != null) {
            coap.start();
        }

        fillDeviceStatus(status, false);

        logger.debug("{}: Thing successfully initialized.", thingName);
        updateStatus(ThingStatus.ONLINE); // if API call was successful the thing must be online
        requestUpdates(3, false); // request 3 updates in a row (during the first 2+3*3 sec)

        postAlarm(ALARM_TYPE_NONE, false);
    }

    /**
     * Handle Channel Command
     */
    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                return;
            }

            if (profile == null) {
                logger.debug("{}: Thing not yet initialized, command {} triggers initialization", thingName,
                        command.toString());
                initializeThing();
            } else {
                profile = getProfile(false);
            }

            boolean update = false;
            lockUpdates = true;

            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_SENSE_KEY: // Shelly Sense: Send Key
                    logger.debug("{}: Send key {}", thingName, command.toString());
                    api.sendIRKey(command.toString());
                    update = true;
                    break;

                default:
                    update = handleDeviceCommand(channelUID, command);
                    break;
            }

            if (update) {
                requestUpdates(1, false);
            }
        } catch (NullPointerException | IOException e) {
            if (authorizationFailed(e.getMessage())) {
                return;
            }
            if (e.getMessage().contains(APIERR_NOT_CALIBRATED)) {
                logger.warn("Device is not calibrated, use Shelly App to perform initial roller calibration.");
            } else {
                logger.warn("{} ERROR: Unable to process command for channel {}: {} ({})\nStack Trace: {}", thingName,
                        channelUID.toString(), e.getMessage(), e.getClass(), e.getStackTrace());
            }
        } finally {
            lockUpdates = false;
        }
    }

    /**
     * Update device status and channels
     */
    @SuppressWarnings("null")
    protected void updateStatus() {
        try {
            boolean updated = false;

            skipUpdate++;
            if (lockUpdates) {
                logger.trace("{}: Update locked, try on next cycle", thingName);
                return;
            }

            if ((skipUpdate % refreshCount == 0) && (profile != null)
                    && (getThing().getStatus() == ThingStatus.ONLINE)) {
                refreshSettings |= !profile.hasBattery;
            }

            if (refreshSettings || (scheduledUpdates > 0) || (skipUpdate % skipCount == 0)) {
                if ((profile == null) || ((getThing().getStatus() == ThingStatus.OFFLINE)
                        && (getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR))) {
                    logger.debug("{}: Status update triggered thing initialization", thingName);
                    initializeThing(); // may fire an exception if initialization failed
                }

                // Get profile, if refreshSettings == true reload settings from device
                profile = getProfile(refreshSettings);

                logger.trace("{}: Updating status", thingName);
                ShellySettingsStatus status = api.getStatus();

                // If status update was successful the thing must be online
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    logger.debug("{}: Thing {} is now online", thingName, getThing().getLabel());
                    updateStatus(ThingStatus.ONLINE); // if API call was successful the thing must be online
                }

                // map status to channels
                updated |= updateDeviceStatus(status);
                updated |= ShellyComponents.updateMeters(this, status);
                updated |= ShellyComponents.updateSensors(this, status);

                if (scheduledUpdates <= 1) {
                    fillDeviceStatus(status, updated);
                }
            }
        } catch (IOException e) {
            // http call failed: go offline except for battery devices, which might be in
            // sleep mode
            // once the next update is successful the device goes back online
            if (e.getMessage().contains("Timeout")) {
                logger.debug("Device {} is not reachable, update canceled ({} skips, {} scheduledUpdates)!", thingName,
                        skipCount, scheduledUpdates);
            } else if (e.getMessage().contains("Not calibrated!")) {
                logger.debug("{}: Roller is not calibrated! Use the Shelly App or Web UI to run calibration.",
                        thingName);
            } else {
                logger.debug("{}: Unable to update status: {} ({})", thingName, e.getMessage(), e.getClass());
            }
            if (e.getMessage().contains(APIERR_HTTP_401_UNAUTHORIZED) || (profile != null && !profile.isSensor)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } catch (NullPointerException e) {
            logger.warn("{}: Unable to update status: {} ({})", thingName, e.getMessage(), e.getClass());
        } finally {
            if (scheduledUpdates > 0) {
                --scheduledUpdates;
                logger.debug("{}: {} more updates requested", thingName, scheduledUpdates);
            } else {

            }
            if ((skipUpdate >= cacheCount) && !channelCache) {
                logger.debug("{}: Enabling channel cache ({} updates / {}s)", thingName, skipUpdate,
                        cacheCount * UPDATE_STATUS_INTERVAL_SECONDS);
                channelCache = true;
            }
        }

    }

    @SuppressWarnings("null")
    private void fillDeviceStatus(ShellySettingsStatus status, boolean updated) {
        String alarm = "";
        boolean force = false;

        // Update uptime and WiFi
        if (updated) {
            lastUpdateTs = now();
        }
        long uptime = getLong(status.uptime);
        Integer rssi = getInteger(status.wifiSta.rssi);
        updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_UPTIME,
                toQuantityType(new DecimalType(uptime), SmartHomeUnits.SECOND));
        updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_RSSI, mapSignalStrength(rssi));

        // Check various device indicators like overheating
        if ((status.uptime < lastUptime) && (profile != null) && !profile.hasBattery) {
            alarm = ALARM_TYPE_RESTARTED;
            force = true;
        }
        lastUptime = uptime;

        if ((rssi < SIGNAL_ALARM_MIN_RSSI) && ((lastAlarmTs == 0))) {
            alarm = ALARM_TYPE_WEAKSIGNAL;
        }
        if (getBool(status.overtemperature)) {
            alarm = ALARM_TYPE_OVERTEMP;
        }
        if (getBool(status.overload)) {
            alarm = ALARM_TYPE_OVERLOAD;
        }
        if (getBool(status.loaderror)) {
            alarm = ALARM_TYPE_LOADERR;
        }

        if (!alarm.isEmpty()) {
            postAlarm(alarm, force);
        }
    }

    /**
     * Save alarm to the lastAlarm channel
     *
     * @param alarm Alarm Message
     */
    public void postAlarm(String alarm, boolean force) {
        String channelId = mkChannelId(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ALARM);
        Object value = getChannelValue(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ALARM);
        String lastAlarm = value != null ? value.toString() : null;

        if (force || ((lastAlarm != null) && !lastAlarm.equals(alarm))
                || (now() > (lastAlarmTs + HEALTH_CHECK_INTERVAL_SEC))) {
            if (alarm.equals(ALARM_TYPE_NONE)) {
                channelData.put(channelId, alarm); // init channel
            } else {
                logger.warn("{}: Alarm condition: {}", thingName, alarm);
                triggerChannel(channelId, alarm);
                channelData.replace(channelId, alarm);
                lastAlarmTs = now();
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
    @SuppressWarnings({ "null" })
    @Override
    public boolean onEvent(String deviceName, String deviceIndex, String type, Map<String, String> parameters) {
        if (profile == null) {
            logger.debug("OnEvent: Thing not yet initialized, skip event");
        }
        if (thingName.equalsIgnoreCase(deviceName) || config.deviceIp.equals(deviceName)) {
            logger.debug("{}: Event received: class={}, index={}, parameters={}", deviceName, type, deviceIndex,
                    parameters.toString());
            boolean hasBattery = profile != null && profile.hasBattery ? true : false;
            if (profile == null) {
                logger.debug("{}: Device is not yet initialized, event triggers initialization", deviceName);
            } else {

                String group = "";
                Integer rindex = !deviceIndex.isEmpty() ? Integer.parseInt(deviceIndex) + 1 : -1;
                if (type.equals(EVENT_TYPE_RELAY)) {
                    group = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                            : CHANNEL_GROUP_RELAY_CONTROL + rindex.toString();
                }
                if (type.equals(EVENT_TYPE_ROLLER)) {
                    group = profile.numRollers <= 1 ? CHANNEL_GROUP_ROL_CONTROL
                            : CHANNEL_GROUP_ROL_CONTROL + rindex.toString();
                }
                if (type.equals(EVENT_TYPE_LIGHT)) {
                    group = profile.numRelays <= 1 ? CHANNEL_GROUP_LIGHT_CONTROL
                            : CHANNEL_GROUP_LIGHT_CONTROL + rindex.toString();
                }
                if (type.equals(EVENT_TYPE_SENSORDATA)) {
                    group = CHANNEL_GROUP_SENSOR;
                }
                if (group.isEmpty()) {
                    logger.debug("Unsupported event class: {}", type);
                    return false;
                }

                // map some of the events to system defined button triggers
                String channel = "";
                String payload = "";
                String event = type.contentEquals(EVENT_TYPE_SENSORDATA) ? SHELLY_EVENT_SENSORDATA
                        : parameters.get("type");
                switch (event) {
                    case SHELLY_EVENT_SHORTPUSH:
                        channel = CHANNEL_BUTTON_TRIGGER;
                        payload = CommonTriggerEvents.SHORT_PRESSED;
                        break;
                    case SHELLY_EVENT_LONGPUSH:
                        channel = CHANNEL_BUTTON_TRIGGER;
                        payload = CommonTriggerEvents.LONG_PRESSED;
                        break;

                    case SHELLY_EVENT_ROLLER_OPEN:
                    case SHELLY_EVENT_ROLLER_CLOSE:
                    case SHELLY_EVENT_ROLLER_STOP:
                        channel = CHANNEL_EVENT_TRIGGER;
                        payload = event;

                    default:
                        // triggered will be provided by input/output channel or sensor channels
                }

                if (!payload.isEmpty()) {
                    // Pass event to trigger channel
                    payload = payload.toUpperCase();
                    logger.debug("{}: Post event {}", thingName, payload);
                    triggerChannel(mkChannelId(group, channel), payload);
                }
            }

            // request update on next interval (2x for non-battery devices)
            requestUpdates(scheduledUpdates >= 2 ? 0 : !hasBattery ? 2 : 1, true);
            return true;
        }
        return false;
    }

    /**
     * Initialize the binding's thing configuration, calc update counts
     */
    protected void initializeThingConfig() {
        config = getConfigAs(ShellyThingConfiguration.class);
        config.localIp = localIP;
        config.httpPort = httpPort;
        if (config.userId.isEmpty() && !bindingConfig.defaultUserId.isEmpty()) {
            config.userId = bindingConfig.defaultUserId;
            config.password = bindingConfig.defaultPassword;
            logger.debug("{}: Using binding default userId", thingName);
        }
        if (config.updateInterval == 0) {
            config.updateInterval = UPDATE_STATUS_INTERVAL_SECONDS * UPDATE_SKIP_COUNT;
        }
        if (config.updateInterval < UPDATE_MIN_DELAY) {
            config.updateInterval = UPDATE_MIN_DELAY;
        }
        skipCount = config.updateInterval / UPDATE_STATUS_INTERVAL_SECONDS;
    }

    /**
     * Checks the http response for authorization error.
     * If the authorization failed the binding can't access the device settings and determine the thing type. In this
     * case the thing type shelly-unknown is set.
     *
     * @param response exception details including the http respone
     * @return true if the authorization failed
     */
    private boolean authorizationFailed(String response) {
        if (response.contains(APIERR_HTTP_401_UNAUTHORIZED)) {
            // If the device is password protected the API doesn't provide settings to the device settings
            logger.warn("{}: Device {} reported 'Access Denied' (user id/password mismatch)", getThing().getLabel(),
                    config.deviceIp);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Access denied, configure user id and password");
            changeThingType(THING_TYPE_SHELLYPROTECTED_STR, "");
            return true;
        }
        return false;
    }

    /**
     * Change type of this thing.
     *
     * @param serviceName mDNS service name from thing discovery - will be used to build the Thing type name
     * @param mode
     */
    private void changeThingType(String thingType, String mode) {
        ThingTypeUID thingTypeUID = getThingTypeUID(thingType, mode);
        if (!thingTypeUID.equals(THING_TYPE_SHELLYUNKNOWN)) {
            logger.debug("{}: Changing thing type to {}", getThing().getLabel(), thingTypeUID.toString());
            Map<String, String> properties = editProperties();
            properties.replace(PROPERTY_DEV_TYPE, thingType);
            updateProperties(properties);
            changeThingType(thingTypeUID, getConfig());
        }
    }

    /**
     * Start the background updates
     */
    @SuppressWarnings("null")
    protected void startUpdateJob() {
        if ((statusJob == null) || statusJob.isCancelled()) {
            statusJob = scheduler.scheduleWithFixedDelay(this::updateStatus, 2, UPDATE_STATUS_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
            Validate.notNull(statusJob, "statusJob must not be null");
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
    public boolean requestUpdates(int requestCount, boolean refreshSettings) {
        this.refreshSettings |= refreshSettings;
        if (refreshSettings) {
            logger.debug("{}: Request settings refresh", thingName);
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
     * Update one channel. Use Channel Cache to avoid unnecessary updates (and avoid
     * messing up the log with those updates)
     *
     * @param channelId Channel id
     * @param value Value (State)
     * @param forceUpdate true: ignore cached data, force update; false check cache of changed data
     * @return true, if successful
     */
    @SuppressWarnings("null")
    public boolean updateChannel(String channelId, State value, Boolean forceUpdate) {
        Validate.notNull(channelData);
        Validate.notNull(channelId);
        Validate.notNull(value, "updateChannel(): value must not be null!");
        try {
            Object current = channelData.get(channelId);
            // logger.trace("{}: Predict channel {}.{} to become {} (type {}).", thingName,
            // group, channel, value, value.getClass());
            if (!channelCache || forceUpdate || (current == null) || !current.equals(value)) {
                updateState(channelId, value);
                if (current == null) {
                    channelData.put(channelId, value);
                } else {
                    channelData.replace(channelId, value);
                }
                logger.trace("{}: Channel {} updated with {} (type {}).", thingName, channelId, value,
                        value.getClass());
                return true;
            }
        } catch (NullPointerException e) {
            logger.debug("Unable to update channel {}.{} with {} (type {}): {} ({})", thingName, channelId, value,
                    value.getClass(), e.getMessage(), e.getClass());
        }
        return false;
    }

    public boolean updateChannel(String group, String channel, State value) {
        return updateChannel(mkChannelId(group, channel), value, false);
    }

    /**
     * Update thing properties with dynamic values
     *
     * @param profile The device profile
     * @param status the /status result
     */
    protected void updateProperties(ShellyDeviceProfile profile, ShellySettingsStatus status) {
        Map<String, Object> properties = fillDeviceProperties(profile);

        // add status properties
        Validate.notNull(status, "updateProperties(): status must not be null!");
        if (status.wifiSta != null) {
            properties.put(PROPERTY_WIFI_NETW, getString(status.wifiSta.ssid));
            properties.put(PROPERTY_WIFI_IP, getString(status.wifiSta.ip));
        }
        if (status.update != null) {
            properties.put(PROPERTY_UPDATE_STATUS, getString(status.update.status));
            properties.put(PROPERTY_UPDATE_AVAILABLE, getBool(status.update.hasUpdate) ? "yes" : "no");
            properties.put(PROPERTY_UPDATE_CURR_VERS, getString(status.update.oldVersion));
            properties.put(PROPERTY_UPDATE_NEW_VERS, getString(status.update.newVersion));
        }

        Map<String, String> thingProperties = new HashMap<String, String>();
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            thingProperties.put(property.getKey(), (String) property.getValue());
        }
        updateProperties(thingProperties);
        logger.trace("{}: Properties updated", thingName);
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

    /**
     * Get one property from the Thing Properties
     *
     * @param key property name
     * @return property value or "" if property is not set
     */
    @SuppressWarnings({ "null" })
    public String getProperty(String key) {
        Map<String, String> thingProperties = getThing().getProperties();
        @Nullable
        String value = thingProperties.get(key);
        return value != null ? value : "";
    }

    /**
     * Fill Thing Properties with device attributes
     *
     * @param profile Property Map to full
     * @return a full property map
     */
    @SuppressWarnings("null")
    public static Map<String, Object> fillDeviceProperties(ShellyDeviceProfile profile) {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(PROPERTY_VENDOR, VENDOR);
        if (profile != null) {
            properties.put(PROPERTY_MODEL_ID, getString(profile.settings.device.type));
            properties.put(PROPERTY_MAC_ADDRESS, profile.mac);
            properties.put(PROPERTY_FIRMWARE_VERSION,
                    profile.fwVersion + "/" + profile.fwDate + "(" + profile.fwId + ")");
            properties.put(PROPERTY_HWREV, profile.hwRev);
            properties.put(PROPERTY_HWBATCH, profile.hwBatchId);
            properties.put(PROPERTY_DEV_MODE, profile.mode);
            properties.put(PROPERTY_NUM_RELAYS, profile.numRelays.toString());
            properties.put(PROPERTY_NUM_ROLLERS, profile.numRollers.toString());
            properties.put(PROPERTY_NUM_METER, profile.numMeters.toString());
        }
        return properties;
    }

    /**
     * Return device profile.
     *
     * @param ForceRefresh true=force refresh before returning, false=return without
     *            refresh
     * @return ShellyDeviceProfile instance
     * @throws IOException
     */
    @SuppressWarnings("null")
    @Nullable
    public ShellyDeviceProfile getProfile(boolean forceRefresh) throws IOException {
        try {
            refreshSettings |= forceRefresh;
            if (refreshSettings) {
                logger.debug("{}: Refresh settings", thingName);
                profile = api.getDeviceProfile(getThing().getThingTypeUID().getId());
            }
        } finally {
            refreshSettings = false;
        }
        return profile;
    }

    @Nullable
    public ShellyDeviceProfile getProfile() {

        return profile;
    }

    @Nullable
    protected ShellyHttpApi getShellyApi() {
        return api;
    }

    @Nullable
    protected ShellyDeviceProfile getDeviceProfile() {
        return profile;
    }

    /**
     * Get a value from the Channel Cache
     *
     * @param group Channel Group
     * @param channel Channel Name
     * @return the data from that channel
     */
    @Nullable
    public Object getChannelValue(String group, String channel) {
        String key = mkChannelId(group, channel);
        return channelData.get(key);
    }

    public void triggerChannel(String group, String channel, String payload) {
        triggerChannel(mkChannelId(group, channel), payload);
    }

    /**
     * Shutdown thing, make sure background jobs are canceled
     */
    @SuppressWarnings("null")
    @Override
    public void dispose() {
        logger.debug("{}: Shutdown thing", thingName);
        try {
            if (coap != null) {
                coap.stop();
                coap = null;
            }
            if (statusJob != null) {
                statusJob.cancel(true);
                statusJob = null;
            }
            logger.debug("{}: Shelly statusJob stopped", thingName);
        } catch (Exception e) {
            logger.debug("Exception on dispose(): {} ({})", e.getMessage(), e.getClass());
        } finally {
            super.dispose();
        }
    }

    /**
     * Device specific command handlers are overriding this method to do additional stuff
     *
     * @throws IOException Communication problem on the API call
     */
    public boolean handleDeviceCommand(ChannelUID channelUID, Command command) throws IOException {
        return false;
    }

    /**
     * Device specific handlers are overriding this method to do additional stuff
     *
     * @throws IOException Communication problem on the API call
     */
    public boolean updateDeviceStatus(ShellySettingsStatus status) throws IOException {
        return false;
    }

    public void createChannels() {

    }

}
