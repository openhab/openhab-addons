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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
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
    public final Logger logger = LoggerFactory.getLogger(ShellyBaseHandler.class);

    protected final ShellyHandlerFactory handlerFactory;
    protected ShellyThingConfiguration config = new ShellyThingConfiguration();
    protected @Nullable ShellyHttpApi api;
    private @Nullable ShellyCoapHandler coap;
    protected @Nullable ShellyDeviceProfile profile;
    private final @Nullable ShellyCoapServer coapServer;

    private @Nullable ScheduledFuture<?> statusJob;
    private int skipUpdate = 0;
    public int scheduledUpdates = 0;
    private int skipCount = UPDATE_SKIP_COUNT;
    private int refreshCount = UPDATE_SETTINGS_INTERVAL_SECONDS / UPDATE_STATUS_INTERVAL_SECONDS; // force settings
                                                                                                  // refresh every x
    // seconds
    private final int cacheCount = UPDATE_SETTINGS_INTERVAL_SECONDS / UPDATE_STATUS_INTERVAL_SECONDS; // delay before
                                                                                                      // enabling
                                                                                                      // channel
    // cache
    private boolean refreshSettings = false;
    private boolean channelCache = false;
    protected boolean lockUpdates = false;

    public String thingName = "";
    private Map<String, Object> channelData = new HashMap<>();
    protected ShellyBindingConfiguration bindingConfig = new ShellyBindingConfiguration();

    /**
     *
     * @param thing The Thing object
     * @param handlerFactory Handler Factory instance (will be used for event
     *            handler registration)
     * @param bindingConfig The binding configuration (beside thing
     *            configuration)
     * @param networkAddressService Network service to get local ip
     */
    public ShellyBaseHandler(Thing thing, ShellyHandlerFactory handlerFactory, ShellyBindingConfiguration bindingConfig,
            @Nullable ShellyCoapServer coapServer) {
        super(thing);

        this.handlerFactory = handlerFactory;
        this.bindingConfig = bindingConfig;
        this.coapServer = coapServer;
    }

    /**
     * Schedule asynchronous Thing initialization, register thing to event
     * dispatcher
     */
    @Override
    public void initialize() {
        // start background initialization:
        scheduler.schedule(() -> {
            try {
                initializeThingConfig();
                Validate.notNull(config);
                logger.info("{}: Device config: ipAddress={}, http user/password={}/{}, update interval={}",
                        getThing().getLabel(), config.deviceIp, config.userId.isEmpty() ? "<non>" : config.userId,
                        config.password.isEmpty() ? "<none>" : "***", config.updateInterval);

                handlerFactory.registerDeviceListener(this);
                initializeThing();
            } catch (RuntimeException | IOException e) {
                if (e.getMessage().contains(HTTP_401_UNAUTHORIZED)) {
                    logger.warn("Device {} ({}) reported 'Access defined' (userid/password mismatch)",
                            getThing().getLabel(), config.deviceIp);
                    logger.info(
                            "You could set a default userid and passowrd in the binding config and re-discover devices");
                    logger.info(
                            "or you need to disable device protection (userid/password) in the Shelly App for device discovery.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Access denied, set userid/password for the thing or in the thing config");
                } else {
                    logger.warn("{}: Unable to initialize: {} ({}), retrying later", getThing().getLabel(),
                            e.getMessage(), e.getClass());

                }
            } finally {
                // even this initialization failed we start the status update
                // the updateJob will then try to auto-initialize the thing
                // in this case the thing stays in status INITIALIZING
                if (getThing().getStatus() != ThingStatus.OFFLINE) {
                    startUpdateJob();
                }
            }

        }, 2, TimeUnit.SECONDS);
    }

    /**
     * Initialize the binding's thing configuration, calc update counts
     */
    @SuppressWarnings("null")
    protected void initializeThingConfig() {
        config = getConfigAs(ShellyThingConfiguration.class);
        config.localIp = bindingConfig.localIp;
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

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        logger.debug("Thing config for device {} updated.", thingName);
        initializeThingConfig();
        startUpdateJob();
        refreshSettings = true; // force re-initialization
    }

    @SuppressWarnings("null")
    private void initializeThing() throws IOException {
        // Get the thing global settings and initialize device capabilities
        channelData = new HashMap<>(); // clear any cached channels
        refreshSettings = false;
        lockUpdates = false;

        Map<String, String> properties = getThing().getProperties();
        Validate.notNull(properties, "properties must not be null!");
        thingName = properties.get(PROPERTY_SERVICE_NAME) != null ? properties.get(PROPERTY_SERVICE_NAME).toLowerCase()
                : "";
        logger.debug("{}: Start initializing, ip address {}, CoIoT: {}", getThing().getLabel(), config.deviceIp,
                config.eventsCoIoT);

        if (config.eventsCoIoT && (coap == null)) {
            Validate.notNull(coapServer, "coapServer must not be null!");
            coap = new ShellyCoapHandler(config, this, coapServer);
        }
        if (coap != null) {
            coap.start();
        }

        api = new ShellyHttpApi(config);
        ShellyDeviceProfile tmpPrf = api.getDeviceProfile(this.getThing().getThingTypeUID().getId());
        thingName = (!thingName.isEmpty() ? thingName : tmpPrf.hostname).toLowerCase();
        Validate.isTrue(!thingName.isEmpty(), "initializeThing(): thingName must not be empty!");

        logger.debug("Initializing device {} ({}), type {}, Hardware: Rev: {}, batch {}; Firmware: {} / {} ({})",
                thingName, tmpPrf.hostname, tmpPrf.deviceType, tmpPrf.hwRev, tmpPrf.hwBatchId, tmpPrf.fwVersion,
                tmpPrf.fwDate, tmpPrf.fwId);
        logger.debug("Shelly settings info for {} : {}", thingName, tmpPrf.settingsJson);
        logger.debug(
                "Device {}: has relays: {} (numRelays={}), is roller: {} (numRoller={}), is Plug S: {}, is Dimmer: {}, has LEDs: {}, is Light: {}, has Meter: {} (numMeter={}, EMeter: {}), is Sensor: {}, is Sense: {}, has Battery: {}{}, event urls: btn:{},out:{}.roller:{},sensor:{}",
                tmpPrf.hostname, tmpPrf.hasRelays, tmpPrf.numRelays, tmpPrf.isRoller, tmpPrf.numRollers, tmpPrf.isPlugS,
                tmpPrf.isDimmer, tmpPrf.hasLed, tmpPrf.isLight, tmpPrf.hasMeter, tmpPrf.numMeters, tmpPrf.isEMeter,
                tmpPrf.isSensor, tmpPrf.isSense, tmpPrf.hasBattery,
                tmpPrf.hasBattery ? "(low battery threshold=" + config.lowBattery + "%)" : "",
                tmpPrf.supportsButtonUrls, tmpPrf.supportsOutUrls, tmpPrf.supportsRollerUrls,
                tmpPrf.supportsSensorUrls);

        // update thing properties
        ShellySettingsStatus status = api.getStatus();
        updateProperties(tmpPrf, status);
        if (tmpPrf.fwVersion.compareTo(SHELLY_API_MIN_FWVERSION) < 0) {
            logger.warn(
                    "WARNING: Firmware for device {} might be too old (or beta release), installed: {}/{} ({}), required minimal {}. The binding was tested with Version 1.50+ only. Older versions might work, but doesn't support all features or lead into technical issues. You should consider to upgrade the device to v1.5.0 or newer!",
                    thingName, tmpPrf.fwVersion, tmpPrf.fwDate, tmpPrf.fwId, SHELLY_API_MIN_FWVERSION);
        }
        if (status.update.hasUpdate) {
            logger.info("{} - INFO: New firmware available: current version: {}, new version: {}", thingName,
                    status.update.oldVersion, status.update.newVersion);
        }
        if (tmpPrf.isSense) {
            logger.debug("Sense stored key list loaded, {} entries.", tmpPrf.irCodes.size());
        }

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
        } else {
            requestUpdates(3, false); // request 3 updates in a row (during the furst 2+3*3 sec)
            logger.info("Thing {} successfully initialized.", thingName);
            updateStatus(ThingStatus.ONLINE); // if API call was successful the thing must be online
        }

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
                logger.info("Thing not yet initialized, command {} triggers initialization", command.toString());
                initializeThing();
            } else {
                profile = getProfile(false);
            }

            boolean update = false;
            lockUpdates = true;

            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_SENSE_KEY: // Shelly Sense: Send Key
                    logger.info("{}: Send key {}", thingName, command.toString());
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
        } catch (RuntimeException | IOException e) {
            if (e.getMessage().contains(HTTP_401_UNAUTHORIZED)) {
                logger.warn(
                        "Device {} ({}) reported 'Access defined' (userid/password mismatch). Set userid/password for the thing or in the binding config",
                        thingName, config.deviceIp);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Access denied, set userid/password for the thing or  binding config");
            } else {
                logger.debug("{} ERROR: Unable to process command for channel {}: {} ({})", thingName,
                        channelUID.toString(), e.getMessage(), e.getClass());
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
                    logger.info("{}: Status update triggered thing initialization", thingName);
                    initializeThing(); // may fire an exception if initialization failed
                }

                // Get profile, if refreshSettings == true reload settings from device
                profile = getProfile(refreshSettings);

                logger.trace("{}: Updating status", thingName);
                ShellySettingsStatus status = api.getStatus();

                // If status update was successful the thing must be online
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    logger.info("Thing {} ({}) is now online", getThing().getLabel(), thingName);
                    updateStatus(ThingStatus.ONLINE); // if API call was successful the thing must be online
                }

                // map status to channels
                updated |= updateDeviceStatus(status);
                updated |= ShellyComponents.updateMeters(this, status);
                updated |= ShellyComponents.updateSensors(this, status);

                if (updated && profile.isSensor) {
                    // add last update information
                    LocalDateTime datetime = LocalDateTime.now();
                    String time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(datetime);
                    updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_LASTUPDATE, new StringType(time));
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
                logger.info("{}: Roller is not calibrated! Use the Shelly App or Web UI to run calibration.",
                        thingName);
            } else {
                logger.debug("{}: Unable to update status: {} ({})", thingName, e.getMessage(), e.getClass());
            }
            if (e.getMessage().contains(HTTP_401_UNAUTHORIZED) || (profile != null && !profile.isSensor)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } catch (RuntimeException e) {
            logger.warn("{}: Unable to update status: {} ({})", thingName, e.getMessage(), e.getClass());
        } finally {
            if (scheduledUpdates > 0) {
                --scheduledUpdates;
                logger.debug("{}: {} more updates requested", thingName, scheduledUpdates);
            }
            if (bindingConfig.channelCache && (skipUpdate >= cacheCount) && !channelCache) {
                logger.debug("{}: Enabling channel cache ({} updates / {}s)", thingName, skipUpdate,
                        cacheCount * UPDATE_STATUS_INTERVAL_SECONDS);
                channelCache = true;
            }
        }

    }

    /**
     * Callback for device events
     *
     * @param deviceName device receiving the event
     * @param parameters parameters from the event url
     * @param data the html input data
     */
    @SuppressWarnings({ "null" })
    @Override
    public void onEvent(String deviceName, String deviceIndex, String type, Map<String, String> parameters) {
        if (thingName.equalsIgnoreCase(deviceName) || config.deviceIp.equals(deviceName)) {
            logger.debug("{}: Event received: class={}, index={}, parameters={}", deviceName, type, deviceIndex,
                    parameters.toString());
            boolean hasBattery = profile != null && profile.hasBattery ? true : false;
            if (profile == null) {
                logger.info("{}: Device is not yet initialized, event will trigger initialization", deviceName);
            }

            int i = 0;
            String payload = "{\"device\":\"" + deviceName + "\", \"class\":\"" + type + "\", \"index\":\""
                    + deviceIndex + "\",\"parameters\":[";
            for (String key : parameters.keySet()) {
                if (i++ > 0) {
                    payload = payload + ", ";
                }
                String value = parameters.get(key);
                payload = payload + "{\"" + key + "\":\"" + value + "\"}";
            }
            payload = payload + "]}";

            String group = "";
            Integer rindex = !deviceIndex.isEmpty() ? Integer.parseInt(deviceIndex) + 1 : -1;
            if (type.equals(EVENT_TYPE_RELAY) && profile.hasRelays) {
                group = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                        : CHANNEL_GROUP_RELAY_CONTROL + rindex.toString();
            }
            if (type.equals(EVENT_TYPE_ROLLER) && profile.isRoller) {
                group = profile.numRollers <= 1 ? CHANNEL_GROUP_ROL_CONTROL
                        : CHANNEL_GROUP_ROL_CONTROL + rindex.toString();
            }
            if (type.equals(EVENT_TYPE_LIGHT) && (profile.isLight || profile.isDimmer)) {
                group = profile.numRelays <= 1 ? CHANNEL_GROUP_LIGHT_CONTROL
                        : CHANNEL_GROUP_LIGHT_CONTROL + rindex.toString();
            }
            if (type.equals(EVENT_TYPE_SENSORDATA)) {
                group = CHANNEL_GROUP_SENSOR;
            }
            Validate.isTrue(!group.isEmpty(), "Unsupported event class: " + type);

            String channel = mkChannelId(group, CHANNEL_EVENT_TRIGGER);
            logger.debug("Trigger {} event, channel {}, payload={}", type, channel, payload);
            triggerChannel(channel, payload);

            requestUpdates(scheduledUpdates >= 3 ? 0 : !hasBattery ? 2 : 1, true); // request update on next interval
                                                                                   // (2x for non-battery devices)
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
        }
        if (scheduledUpdates < 10) { // < 30s
            scheduledUpdates += requestCount;
            return true;
        }
        return false;
    }

    /**
     * Update one channel. Use Channel Cache to avoid unessesary updates (and avoid
     * messing up the log with those updates)
     *
     * @param channelId Channel Name
     * @param value Value (State)
     * @param forceUpdate true: ignore cached data, force update; false check cache
     *            of changed data
     * @return
     */
    @SuppressWarnings("null")
    public boolean updateChannel(String channelId, State value, Boolean forceUpdate) {
        Validate.notNull(channelData, "updateChannel(): channelData must not be null!");
        Validate.notNull(channelId, "updateChannel(): channelId must not be null!");
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
        } catch (RuntimeException e) {
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
            properties.put(PROPERTY_WIFI_RSSI, getInteger(status.wifiSta.rssi).toString());
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
            properties.put(PROPERTY_HOSTNAME, profile.hostname);
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

    /**
     * Shutdown thing, make sure background jobs are canceled
     */
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

}
