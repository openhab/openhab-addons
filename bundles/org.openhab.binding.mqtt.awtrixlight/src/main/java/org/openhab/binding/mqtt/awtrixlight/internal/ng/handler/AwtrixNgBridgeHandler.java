/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.awtrixlight.internal.ng.handler;

import static org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgBindingConstants.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgHelper;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgStateDescriptionProvider;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.BridgeConfigOptions;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.PayloadChannel;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.ScreenImage;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.SettingsChannels;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.action.AwtrixNgActions;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.discovery.AwtrixNgBridgeDiscoveryService;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.Capabilities;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.DeviceState;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.Indicator;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.IndicatorCommand;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.NotificationPayload;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.ScreenFrame;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.Settings;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.SoundSpec;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwtrixNgBridgeHandler} represents an AWTRIX NG clock. It follows the retained state topics of the clock,
 * maps them to channels and publishes the commands the user sends to them.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class AwtrixNgBridgeHandler extends BaseBridgeHandler implements MqttMessageSubscriber {

    private static final int MQTT_QOS = 1;
    private static final List<String> APP_CAPABILITY_CHANNELS = List.of(CHANNEL_EFFECT, CHANNEL_OVERLAY, CHANNEL_FONT);
    private static final String DEFAULT_INDICATOR_COLOR = "#00FF00";
    private static final String NO_INDICATOR_PAYLOAD = "{}";
    private static final String MOODLIGHT_ON_COLOR = "#FFFFFF";

    /** What to set the clock back to when a lock has ended, with the indicator the lock replaced. */
    private record LockRestore(Settings settings, @Nullable Indicator indicator) {
    }

    private final Logger logger = LoggerFactory.getLogger(AwtrixNgBridgeHandler.class);

    private volatile @Nullable MqttBrokerConnection connection;
    private volatile String basetopic = "";
    private volatile int appLockTimeout = 10;
    private volatile boolean discoverDefaultApps;
    // True between initialize() and dispose(), so late callbacks of the broker do not touch a disposed handler
    private volatile boolean active;
    private volatile boolean deviceOnline;
    private final AtomicLong connectionRequests = new AtomicLong();

    private final AwtrixNgStateDescriptionProvider stateDescriptionProvider;
    private final Map<String, AwtrixNgAppHandler> appHandlers = new ConcurrentHashMap<>();
    /** Apps that were deleted while the broker was not connected, and are deleted when it is */
    private final Set<String> pendingAppDeletes = ConcurrentHashMap.newKeySet();
    private final Map<String, State> lastStates = new ConcurrentHashMap<>();
    private final Set<String> pressedButtons = ConcurrentHashMap.newKeySet();
    private volatile @Nullable Capabilities capabilities;
    private volatile @Nullable AwtrixNgBridgeDiscoveryService discoveryCallback;
    private volatile String currentApp = "";

    // The clock navigates by itself while no app is locked, so the settings it reports then are the ones to go back to
    private volatile @Nullable Boolean reportedAutoTransition;
    private volatile @Nullable Boolean reportedBlockNavigation;

    // Guards the app lock; it is never held while calling out
    private final Object appLockMonitor = new Object();
    private boolean appLock;
    private boolean autoTransitionToRestore = true;
    private boolean blockNavigationToRestore;
    // The lock indicator belongs to the app lock, so what it showed before is put back when the lock ends
    private volatile @Nullable Indicator reportedLockIndicator;
    private @Nullable Indicator lockIndicatorToRestore;
    // Set while the clock may still report the settings and the indicator of a lock that has ended
    private boolean lockEndedRecently;
    // A lock that ended while the clock was offline, the clock is set back when it is online again
    private @Nullable LockRestore pendingLockRestore;
    // What this handler stored for the lock last, to tell the delivery of its own message from a lock of an earlier run
    private @Nullable String ownStoredLock;
    private @Nullable ScheduledFuture<?> appLockTimeoutJob;
    private @Nullable ScheduledFuture<?> appLockWarningJob;

    public AwtrixNgBridgeHandler(Bridge bridge, AwtrixNgStateDescriptionProvider stateDescriptionProvider) {
        super(bridge);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initialize() {
        BridgeConfigOptions config = getConfigAs(BridgeConfigOptions.class);
        String base = config.basetopic.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.isEmpty()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "The base topic is not set");
            return;
        }
        this.basetopic = base;
        this.appLockTimeout = config.appLockTimeout;
        this.discoverDefaultApps = config.discoverDefaultApps;
        this.active = true;
        bridgeStatusChanged(getBridgeStatus());
    }

    @Override
    public void dispose() {
        leaveAppControlMode();
        stateDescriptionProvider.removeStateOptions(new ChannelUID(thing.getUID(), CHANNEL_TRANSITION_EFFECT));
        stateDescriptionProvider.removeStateOptions(new ChannelUID(thing.getUID(), CHANNEL_OVERLAY));
        this.active = false;
        this.deviceOnline = false;
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            unsubscribeAll(localConnection);
        }
        this.connection = null;
        resetClockState();
    }

    /**
     * The handler is initialized again after a change of the configuration, possibly for another clock, which must not
     * get what was learned from the one before. A lock that is still stored on the broker is ended from there.
     */
    private void resetClockState() {
        synchronized (appLockMonitor) {
            pendingLockRestore = null;
            ownStoredLock = null;
            lockEndedRecently = false;
        }
        reportedAutoTransition = null;
        reportedBlockNavigation = null;
        reportedLockIndicator = null;
        capabilities = null;
        currentApp = "";
        lastStates.clear();
        pressedButtons.clear();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (!active) {
            return;
        }
        // A connection that is still being requested for an earlier status is of no use any more when it arrives
        long request = connectionRequests.incrementAndGet();
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            // The retained availability message is delivered again once the broker is back
            this.deviceOnline = false;
            pressedButtons.clear();
            MqttBrokerConnection lost = connection;
            if (lost != null) {
                unsubscribeAll(lost);
            }
            this.connection = null;
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        if (bridgeStatusInfo.getStatus() == ThingStatus.UNKNOWN) {
            // The broker is unknown until it is connected, which is normal after a start
            updateStatus(ThingStatus.UNKNOWN, "Waiting for the MQTT broker");
            return;
        }
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        Bridge broker = getBridge();
        ThingHandler handler = broker == null ? null : broker.getHandler();
        if (handler instanceof AbstractBrokerHandler brokerHandler) {
            brokerHandler.getConnectionAsync().thenAccept(newConnection -> {
                if (connectionRequests.get() == request) {
                    connectionAvailable(newConnection);
                }
            }).exceptionally(e -> {
                if (active && connectionRequests.get() == request) {
                    updateStatus(ThingStatusDetail.BRIDGE_UNINITIALIZED, "The MQTT broker has no valid connection");
                }
                return null;
            });
        } else {
            updateStatus(ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "The MQTT broker is missing, set up a working broker first");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            if (CHANNEL_SCREEN.equals(channelId)) {
                publish(TOPIC_CMD_SCREEN_GET, "");
            } else {
                // The clock reports on a change only, so a channel that is linked later gets what was reported last
                State lastState = lastStates.get(channelId);
                if (lastState != null) {
                    updateState(channelUID, lastState);
                }
            }
            return;
        }
        switch (channelId) {
            case CHANNEL_AUTO_BRIGHTNESS -> {
                if (command instanceof OnOffType onOff) {
                    Settings settings = new Settings();
                    settings.autoBrightness = onOff == OnOffType.ON;
                    publish(TOPIC_CMD_SETTINGS, AwtrixNgHelper.toJson(settings));
                }
            }
            case CHANNEL_BRIGHTNESS -> handleBrightness(command);
            case CHANNEL_DISPLAY -> {
                if (command instanceof OnOffType onOff) {
                    publish(TOPIC_CMD_DISPLAY, AwtrixNgHelper.toJson(Map.of("power", onOff == OnOffType.ON)));
                }
            }
            case CHANNEL_APP -> {
                if (command instanceof StringType) {
                    publish(TOPIC_CMD_APPS_SWITCH, command.toString());
                }
            }
            case CHANNEL_SOUND -> {
                if (command instanceof StringType) {
                    SoundSpec sound = new SoundSpec();
                    sound.file = command.toString();
                    play(sound);
                }
            }
            case CHANNEL_RTTTL -> {
                if (command instanceof StringType) {
                    SoundSpec sound = new SoundSpec();
                    sound.rtttl = command.toString();
                    play(sound);
                }
            }
            case CHANNEL_MOODLIGHT -> handleMoodlight(command);
            case CHANNEL_OVERLAY -> {
                if (command instanceof StringType) {
                    String overlay = command.toString().trim();
                    publish(TOPIC_CMD_DISPLAY, overlay.isEmpty() ? "{\"overlay\":null}"
                            : AwtrixNgHelper.toJson(Map.of("overlay", overlay)));
                }
            }
            default -> {
                PayloadChannel<Settings> setting = SettingsChannels.get(channelId);
                int indicator = indicatorNumber(channelId);
                if (setting != null) {
                    handleSetting(setting, command);
                } else if (indicator > 0) {
                    handleIndicator(indicator, command);
                }
            }
        }
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        String base = basetopic;
        if (!active || !topic.startsWith(base)) {
            return;
        }
        String message = new String(payload, StandardCharsets.UTF_8);
        switch (topic.substring(base.length())) {
            case TOPIC_AVAILABILITY -> handleAvailability(message);
            case TOPIC_STATE_DEVICE -> handleDeviceState(message);
            case TOPIC_STATE_SETTINGS -> handleSettings(message);
            case TOPIC_STATE_CAPABILITIES -> handleCapabilities(message);
            case TOPIC_STATE_ACTIVE_APP -> handleActiveApp(message);
            case TOPIC_STATE_SCREEN -> handleScreen(message);
            case TOPIC_STATE_BUTTON_LEFT -> handleButton(CHANNEL_BUTTON_LEFT, message);
            case TOPIC_STATE_BUTTON_SELECT -> handleButton(CHANNEL_BUTTON_SELECT, message);
            case TOPIC_STATE_BUTTON_RIGHT -> handleButton(CHANNEL_BUTTON_RIGHT, message);
            case TOPIC_EVENT_ERROR -> handleError(message);
            case TOPIC_STORE_APP_LOCK -> handleStoredAppLock(message);
            default -> logger.trace("Ignoring message on topic {}", topic);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof AwtrixNgAppHandler app) {
            registerApp(app, childThing.getUID());
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof AwtrixNgAppHandler app) {
            unregisterApp(app.getAppName(), app, childThing.getUID());
        }
    }

    /**
     * Also called by an app that is renamed, as the framework does not tell the bridge about that. An app thing that
     * does not get its name is told, as things that initialize at the same time cannot see each other beforehand.
     *
     * @return true if the app owns its name now
     */
    boolean registerApp(AwtrixNgAppHandler app, ThingUID appUid) {
        String name = app.getAppName();
        if (!APP_NAME_PATTERN.matcher(name).matches()) {
            return false;
        }
        AwtrixNgAppHandler owner = appHandlers.putIfAbsent(name, app);
        if (owner != null) {
            if (owner.equals(app)) {
                return true;
            }
            logger.warn("The app {} of {} is already used by {}, it is not registered", name, appUid,
                    owner.getThing().getUID());
            app.rejectAppName(name);
            return false;
        }
        Capabilities known = capabilities;
        if (known != null) {
            applyAppCapabilities(appUid, known);
        }
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            localConnection.subscribe(basetopic + TOPIC_STORE_APPS + name, app);
        }
        return true;
    }

    /** @return true if the app was registered under the name, and so owned it */
    boolean unregisterApp(String name, AwtrixNgAppHandler app, ThingUID appUid) {
        if (appHandlers.remove(name, app)) {
            for (String channelId : APP_CAPABILITY_CHANNELS) {
                stateDescriptionProvider.removeStateOptions(new ChannelUID(appUid, channelId));
            }
            MqttBrokerConnection localConnection = connection;
            if (localConnection != null) {
                localConnection.unsubscribe(basetopic + TOPIC_STORE_APPS + name, app);
            }
            return true;
        }
        return false;
    }

    /** @return true if an app thing other than {@code app} is registered under the name */
    boolean isAppNameTaken(String name, AwtrixNgAppHandler app) {
        AwtrixNgAppHandler owner = appHandlers.get(name);
        return owner != null && !owner.equals(app);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(AwtrixNgBridgeDiscoveryService.class, AwtrixNgActions.class);
    }

    public void setAppDiscoveryCallback(AwtrixNgBridgeDiscoveryService discovery) {
        this.discoveryCallback = discovery;
    }

    public void removeAppDiscoveryCallback() {
        this.discoveryCallback = null;
    }

    // The operations below are those of the rule actions. They check what they are given, as a rule can pass anything,
    // and answer to the log: NG rejects a command as a whole and only tells its error topic.

    /** Switches an indicator on, with its color blinking or fading. */
    public void setIndicator(int indicator, String color, @Nullable Integer blinkMs, @Nullable Integer fadeMs) {
        String hex = AwtrixNgHelper.normalizeColor(color);
        if (!isIndicator(indicator) || hex == null || !isIndicatorTime(blinkMs) || !isIndicatorTime(fadeMs)) {
            logger.warn("Ignoring indicator {} with color {}, blink {} and fade {}: the indicator is 1 to 3, the color"
                    + " #RRGGBB and the times 0 to {} ms", indicator, color, blinkMs, fadeMs, INDICATOR_MAX_MS);
            return;
        }
        IndicatorCommand command = new IndicatorCommand(hex);
        command.blinkMs = blinkMs;
        command.fadeMs = fadeMs;
        publish(TOPIC_CMD_INDICATOR + indicator, AwtrixNgHelper.toJson(command));
    }

    public void resetIndicator(int indicator) {
        if (isIndicator(indicator)) {
            publish(TOPIC_CMD_INDICATOR + indicator, NO_INDICATOR_PAYLOAD);
        } else {
            logger.warn("Ignoring indicator {}, it is 1 to 3", indicator);
        }
    }

    public void setMoodlight(String color, @Nullable Integer brightness) {
        String hex = AwtrixNgHelper.normalizeColor(color);
        if (hex == null || (brightness != null && (brightness < 0 || brightness > 255))) {
            logger.warn("Ignoring mood light with color {} and brightness {}: the color is #RRGGBB, the brightness"
                    + " 0 to 255", color, brightness);
            return;
        }
        publish(TOPIC_CMD_MOODLIGHT, AwtrixNgHelper.jsonObject("color", hex, "brightness", brightness));
    }

    /** Switches the mood light off, which an empty payload does. */
    public void clearMoodlight() {
        publish(TOPIC_CMD_MOODLIGHT, "");
    }

    public void reboot() {
        publish(TOPIC_CMD_DEVICE_REBOOT, "");
    }

    public void sleep(int durationMs) {
        if (durationMs <= 0) {
            logger.warn("Ignoring sleep of {} ms, it must be more than 0", durationMs);
            return;
        }
        publish(TOPIC_CMD_DEVICE_SLEEP, AwtrixNgHelper.jsonObject("durationMs", durationMs));
    }

    public void play(SoundSpec sound) {
        Integer track = sound.track;
        boolean trackValid = track != null && track >= MIN_TRACK && track <= MAX_TRACK;
        boolean fileValid = sound.file != null && !sound.file.isBlank();
        boolean rtttlValid = sound.rtttl != null && !sound.rtttl.isBlank();
        if ((track != null && !trackValid) || !(trackValid || fileValid || rtttlValid)) {
            logger.warn("Ignoring sound {}: it needs a file, a melody or a track from {} to {}",
                    AwtrixNgHelper.toJson(sound), MIN_TRACK, MAX_TRACK);
            return;
        }
        publish(TOPIC_CMD_AUDIO_PLAY, AwtrixNgHelper.toJson(sound));
    }

    /** @param group alert, app or radio, or null to stop all sounds */
    public void stopSound(@Nullable String group) {
        if (group == null || group.isBlank()) {
            publish(TOPIC_CMD_AUDIO_STOP, NO_INDICATOR_PAYLOAD);
        } else if (SOUND_GROUPS.contains(group)) {
            publish(TOPIC_CMD_AUDIO_STOP, AwtrixNgHelper.jsonObject("group", group));
        } else {
            logger.warn("Ignoring sound group {}, it is one of {}", group, SOUND_GROUPS);
        }
    }

    public void showNotification(NotificationPayload notification) {
        publish(TOPIC_CMD_NOTIFY, AwtrixNgHelper.toJson(notification));
    }

    /** @param name the name the notification was shown with, or null for the notification on the display */
    public void dismissNotification(@Nullable String name) {
        if (name == null || name.isBlank()) {
            publish(TOPIC_CMD_NOTIFY_DISMISS, "");
        } else if (APP_NAME_PATTERN.matcher(name).matches()) {
            publish(TOPIC_CMD_NOTIFY_DISMISS + "/" + name, "");
        } else {
            logger.warn("Ignoring notification name {}, it has 1 to 32 letters, digits, - or _", name);
        }
    }

    public void nextApp() {
        publish(TOPIC_CMD_APPS_NEXT, "");
    }

    public void previousApp() {
        publish(TOPIC_CMD_APPS_PREVIOUS, "");
    }

    public void switchApp(@Nullable String appName, boolean fast) {
        if (appName == null || appName.isBlank()) {
            logger.warn("Ignoring switch to an app without a name");
            return;
        }
        publish(TOPIC_CMD_APPS_SWITCH, AwtrixNgHelper.jsonObject("name", appName, "fast", fast));
    }

    /**
     * @param order the apps in the order they rotate, null to keep the order
     * @param disabled all apps that are switched off, every other app is switched on
     */
    public void setAppOrder(@Nullable List<String> order, @Nullable List<String> disabled) {
        // A rule can pass null, which would leave out the apps that are switched off instead of switching none off
        if (disabled == null || !allAppNames(order) || !allAppNames(disabled)) {
            logger.warn("Ignoring app order with {} and {}: app names have 1 to 32 letters, digits, - or _", order,
                    disabled);
            return;
        }
        publish(TOPIC_CMD_APPS_ORDER, AwtrixNgHelper.jsonObject("order",
                order == null || order.isEmpty() ? null : order, "disabled", disabled));
    }

    public void setAppEnabled(@Nullable String appName, boolean enabled) {
        if (appName == null || !APP_NAME_PATTERN.matcher(appName).matches()) {
            logger.warn("Ignoring app name {}, it has 1 to 32 letters, digits, - or _", appName);
            return;
        }
        publish(TOPIC_CMD_APPS + appName + "/enabled", Boolean.toString(enabled));
    }

    private static boolean isIndicator(int indicator) {
        return indicator >= 1 && indicator <= INDICATOR_COUNT;
    }

    private static boolean isIndicatorTime(@Nullable Integer millis) {
        return millis == null || (millis >= 0 && millis <= INDICATOR_MAX_MS);
    }

    private static boolean allAppNames(@Nullable List<String> names) {
        return names == null || names.stream().allMatch(name -> APP_NAME_PATTERN.matcher(name).matches());
    }

    /** Creates or updates the pushed app on the clock. */
    boolean pushApp(String appName, String payload) {
        return publish(TOPIC_CMD_APPS_PUSHED + appName, payload);
    }

    /**
     * Keeps the state of the app, to restore it after openHAB restarted.
     *
     * @return {@code false} if the broker is not connected, so the state was not kept
     */
    boolean storeApp(String appName, String payload) {
        return publish(TOPIC_STORE_APPS + appName, payload, true);
    }

    /**
     * Removes the app from the clock and from the store. Without a broker connection this is done as soon as there is
     * one, as the app would come back from the store when a thing with its name starts.
     */
    void deleteApp(String appName) {
        boolean cleared = publish(TOPIC_CMD_APPS_PUSHED + appName, "");
        cleared &= publish(TOPIC_STORE_APPS + appName, "", true);
        if (!cleared) {
            pendingAppDeletes.add(appName);
        }
    }

    private ThingStatusInfo getBridgeStatus() {
        Bridge broker = getBridge();
        return broker != null ? broker.getStatusInfo()
                : new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
    }

    private void connectionAvailable(MqttBrokerConnection newConnection) {
        if (!active) {
            return;
        }
        if (!deviceOnline) {
            // Before subscribing, as the retained availability message may arrive immediately
            updateStatus(ThingStatus.UNKNOWN, "Waiting for the clock to report availability");
        }
        this.connection = newConnection;
        // Before subscribing to the stores, so that a retained app that was deleted is gone when it is delivered
        new ArrayList<>(pendingAppDeletes).forEach(name -> {
            pendingAppDeletes.remove(name);
            deleteApp(name);
        });
        String base = basetopic;
        newConnection.subscribe(base + TOPIC_AVAILABILITY, this);
        newConnection.subscribe(base + TOPIC_STATE + "/#", this);
        newConnection.subscribe(base + TOPIC_EVENT_ERROR, this);
        newConnection.subscribe(base + TOPIC_STORE_APP_LOCK, this);
        appHandlers.forEach((name, app) -> newConnection.subscribe(base + TOPIC_STORE_APPS + name, app));
    }

    private void unsubscribeAll(MqttBrokerConnection oldConnection) {
        String base = basetopic;
        oldConnection.unsubscribe(base + TOPIC_AVAILABILITY, this);
        oldConnection.unsubscribe(base + TOPIC_STATE + "/#", this);
        oldConnection.unsubscribe(base + TOPIC_EVENT_ERROR, this);
        oldConnection.unsubscribe(base + TOPIC_STORE_APP_LOCK, this);
        appHandlers.forEach((name, app) -> oldConnection.unsubscribe(base + TOPIC_STORE_APPS + name, app));
    }

    private boolean publish(String topicSuffix, String payload) {
        return publish(topicSuffix, payload, false);
    }

    private boolean publish(String topicSuffix, String payload, boolean retain) {
        MqttBrokerConnection localConnection = connection;
        if (localConnection == null) {
            logger.debug("Not connected to the broker, dropping the message for {}", topicSuffix);
            return false;
        }
        String topic = basetopic + topicSuffix;
        logger.debug("Publishing {} to {}", payload, topic);
        localConnection.publish(topic, payload.getBytes(StandardCharsets.UTF_8), MQTT_QOS, retain);
        return true;
    }

    private void handleBrightness(Command command) {
        Double percent = AwtrixNgHelper.toPercent(command);
        if (percent == null || percent < 0 || percent > 100) {
            logger.debug("Ignoring brightness command {}", command);
            return;
        }
        Settings settings = new Settings();
        settings.autoBrightness = false;
        settings.brightness = (int) Math.round(255 * percent / 100);
        publish(TOPIC_CMD_SETTINGS, AwtrixNgHelper.toJson(settings));
        updateChannel(CHANNEL_AUTO_BRIGHTNESS, OnOffType.OFF);
    }

    private static int indicatorNumber(String channelId) {
        if (channelId.startsWith(CHANNEL_INDICATOR_PREFIX)) {
            try {
                int number = Integer.parseInt(channelId.substring(CHANNEL_INDICATOR_PREFIX.length()));
                return number >= 1 && number <= INDICATOR_COUNT ? number : 0;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private void handleSetting(PayloadChannel<Settings> setting, Command command) {
        Settings update = setting.toPayload(Settings::new, command);
        if (update == null) {
            logger.warn("Ignoring command {}: its value is not valid for the setting", command);
            return;
        }
        publish(TOPIC_CMD_SETTINGS, AwtrixNgHelper.toJson(update));
    }

    /** Switching the mood light off takes an empty payload, a color switches it on. */
    private void handleMoodlight(Command command) {
        handleColorCommand("mood light", command, MOODLIGHT_ON_COLOR, hex -> setMoodlight(hex, null),
                this::clearMoodlight);
    }

    private void handleIndicator(int indicator, Command command) {
        handleColorCommand("indicator", command, DEFAULT_INDICATOR_COLOR,
                hex -> setIndicator(indicator, hex, null, null), () -> resetIndicator(indicator));
    }

    /** Black is the same as OFF, as a light of that color would be dark. */
    private void handleColorCommand(String light, Command command, String onColor, Consumer<String> switchOn,
            Runnable switchOff) {
        if (command instanceof HSBType color) {
            String hex = AwtrixNgHelper.toHex(color);
            if ("#000000".equals(hex)) {
                switchOff.run();
            } else {
                switchOn.accept(hex);
            }
        } else if (command == OnOffType.ON) {
            switchOn.accept(onColor);
        } else if (command == OnOffType.OFF) {
            switchOff.run();
        } else {
            logger.debug("Ignoring {} command {}", light, command);
        }
    }

    private void handleAvailability(String message) {
        boolean online = AVAILABILITY_ONLINE.equals(message.trim());
        this.deviceOnline = online;
        if (online) {
            updateStatus(ThingStatus.ONLINE);
            finishPendingLockRestore();
        } else {
            pressedButtons.clear();
            synchronized (appLockMonitor) {
                lockEndedRecently = false;
            }
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "The clock reported offline");
        }
    }

    private void handleDeviceState(String message) {
        DeviceState state = AwtrixNgHelper.fromJson(message, DeviceState.class);
        if (state == null) {
            logger.debug("Ignoring invalid device state: {}", message);
            return;
        }
        updateProperties(state);

        if (state.matrixPower != null) {
            updateChannel(CHANNEL_DISPLAY, OnOffType.from(state.matrixPower));
        }
        if (state.brightness != null) {
            updateChannel(CHANNEL_BRIGHTNESS,
                    new QuantityType<>(Math.round(state.brightness * 100 / 255.0), Units.PERCENT));
        }
        updateQuantity(CHANNEL_LIGHT_LEVEL, state.lightLevel, Units.PERCENT);
        updateQuantity(CHANNEL_TEMPERATURE, state.temperature, SIUnits.CELSIUS);
        updateQuantity(CHANNEL_HUMIDITY, state.humidity, Units.PERCENT);
        updateQuantity(CHANNEL_PRESSURE, state.pressureHpa, MetricPrefix.HECTO(SIUnits.PASCAL));
        updateQuantity(CHANNEL_BATTERY_LEVEL, state.batteryPercent, Units.PERCENT);
        updateQuantity(CHANNEL_BATTERY_VOLTAGE, state.batteryVoltage, Units.VOLT);
        updateQuantity(CHANNEL_RSSI, state.wifiRssi, Units.DECIBEL_MILLIWATTS);
        updateQuantity(CHANNEL_UPTIME, state.uptimeSeconds, Units.SECOND);
        updateQuantity(CHANNEL_FREE_HEAP, state.freeHeapBytes, Units.BYTE);
        if (state.fps != null) {
            updateChannel(CHANNEL_FPS, new DecimalType(state.fps));
        }
        if (state.ipAddress != null) {
            updateChannel(CHANNEL_IP_ADDRESS, new StringType(state.ipAddress));
        }
        if (state.lowBattery != null) {
            updateChannel(CHANNEL_LOW_BATTERY, OnOffType.from(state.lowBattery));
        }
        updateIndicators(state.indicators);
    }

    private void updateProperties(DeviceState state) {
        Map<String, String> reported = new HashMap<>();
        putIfPresent(reported, PROP_FIRMWARE, state.version);
        putIfPresent(reported, PROP_UNIQUEID, state.uid);
        putIfPresent(reported, PROP_BOARD_TYPE, state.boardType);
        putIfPresent(reported, PROP_MAC_ADDRESS, state.macAddress);
        putIfPresent(reported, PROP_HOSTNAME, state.hostname);
        reported.entrySet().removeIf(e -> e.getValue().equals(thing.getProperties().get(e.getKey())));
        if (!reported.isEmpty()) {
            updateProperties(reported);
        }
        if (reported.containsKey(PROP_UNIQUEID)) {
            // An app that went online before the clock told its unique ID could not set its ID yet
            appHandlers.values().forEach(AwtrixNgAppHandler::updateAppId);
        }
    }

    private static void putIfPresent(Map<String, String> properties, String name, @Nullable String value) {
        if (value != null) {
            properties.put(name, value);
        }
    }

    private void updateIndicators(@Nullable List<@Nullable Indicator> indicators) {
        if (indicators == null) {
            return;
        }
        for (int i = 0; i < Math.min(indicators.size(), INDICATOR_COUNT); i++) {
            Indicator indicator = indicators.get(i);
            if (indicator == null) {
                continue;
            }
            if (i + 1 == APP_LOCK_INDICATOR && !isStaleLockIndicator(indicator)) {
                reportedLockIndicator = indicator;
            }
            HSBType color = indicator.on ? AwtrixNgHelper.fromHex(indicator.color) : null;
            updateChannel(CHANNEL_INDICATOR_PREFIX + (i + 1), color != null ? color : HSBType.BLACK);
        }
    }

    private void handleSettings(String message) {
        Settings settings = AwtrixNgHelper.fromJson(message, Settings.class);
        if (settings == null) {
            logger.debug("Ignoring invalid settings: {}", message);
            return;
        }
        if (settings.autoBrightness != null) {
            updateChannel(CHANNEL_AUTO_BRIGHTNESS, OnOffType.from(settings.autoBrightness));
        }
        synchronized (appLockMonitor) {
            if (!appLock && !isStaleLockSettings(settings)) {
                reportedAutoTransition = settings.autoTransition;
                reportedBlockNavigation = settings.blockNavigation;
            }
        }
        for (Map.Entry<String, PayloadChannel<Settings>> channel : SettingsChannels.all()) {
            State state = channel.getValue().toState(settings);
            if (state != null) {
                updateChannel(channel.getKey(), state);
            }
        }
    }

    /**
     * After the lock the clock may still deliver the settings of the lock, which must not be taken for the state to go
     * back to. Any other settings end that. Must be called with the app lock monitor held.
     */
    private boolean isStaleLockSettings(Settings settings) {
        if (!lockMayStillBeReported()) {
            return false;
        }
        if (Boolean.FALSE.equals(settings.autoTransition) && Boolean.TRUE.equals(settings.blockNavigation)) {
            return true;
        }
        lockEndedRecently = false;
        return false;
    }

    /** Must be called with the app lock monitor held. */
    private boolean lockMayStillBeReported() {
        return lockEndedRecently || pendingLockRestore != null;
    }

    /** The third indicator is the lock's own while the lock lasts, and may still be after it. */
    private boolean isStaleLockIndicator(Indicator indicator) {
        synchronized (appLockMonitor) {
            return appLock || lockMayStillBeReported() && indicator.on
                    && APP_LOCK_INDICATOR_COLOR.equalsIgnoreCase(indicator.color);
        }
    }

    private void handleActiveApp(String message) {
        currentApp = message;
        updateChannel(CHANNEL_APP, new StringType(message));
        appHandlers.forEach((name, app) -> app.setActive(name.equals(message)));
        AwtrixNgBridgeDiscoveryService discovery = discoveryCallback;
        if (discovery != null && !appHandlers.containsKey(message) && isDiscoverable(message)) {
            discovery.appDiscovered(message);
        }
        if (isLinked(new ChannelUID(thing.getUID(), CHANNEL_SCREEN))) {
            publish(TOPIC_CMD_SCREEN_GET, "");
        }
    }

    private void handleScreen(String message) {
        // Another client of the clock can ask for frames, which are not worth rendering when nothing shows them
        if (!isLinked(new ChannelUID(thing.getUID(), CHANNEL_SCREEN))) {
            return;
        }
        ScreenFrame frame = AwtrixNgHelper.fromJson(message, ScreenFrame.class);
        if (frame == null) {
            logger.debug("Ignoring invalid screen frame");
            return;
        }
        try {
            byte[] png = ScreenImage.toPng(frame);
            if (png.length > 0) {
                updateChannel(CHANNEL_SCREEN, new RawType(png, "image/png"));
            } else {
                logger.debug("Ignoring screen frame of {}x{} with {} pixels", frame.width, frame.height,
                        frame.pixels == null ? 0 : frame.pixels.length);
            }
        } catch (IOException e) {
            logger.warn("Failed to render the screen image", e);
        }
    }

    /**
     * The clock tells the names of its effects, transitions, overlays and fonts, which are the choices of the channels.
     */
    private void handleCapabilities(String message) {
        Capabilities reported = AwtrixNgHelper.fromJson(message, Capabilities.class);
        if (reported == null) {
            logger.debug("Ignoring invalid capabilities: {}", message);
        } else {
            capabilities = reported;
            setOptions(new ChannelUID(thing.getUID(), CHANNEL_TRANSITION_EFFECT), reported.transitions);
            setOptions(new ChannelUID(thing.getUID(), CHANNEL_OVERLAY), reported.overlays);
            appHandlers.values().forEach(app -> applyAppCapabilities(app.getThing().getUID(), reported));
        }
        // The clock publishes its capabilities, retained, every time it connects, which also tells that a clock has
        // restarted and lost its pushed apps, even when the broker did not publish its last will. So the availability
        // need not send the apps as well, and a message that cannot be read must not keep the apps from being sent
        appHandlers.values().forEach(AwtrixNgAppHandler::deviceOnline);
    }

    private void applyAppCapabilities(ThingUID appUid, Capabilities reported) {
        setOptions(new ChannelUID(appUid, CHANNEL_EFFECT), reported.effects);
        setOptions(new ChannelUID(appUid, CHANNEL_OVERLAY), reported.overlays);
        setOptions(new ChannelUID(appUid, CHANNEL_FONT), reported.fonts == null ? null
                : reported.fonts.stream().map(font -> font == null ? null : font.name).toList());
    }

    /** A list that the clock did not report keeps the names of the thing type definition. */
    private void setOptions(ChannelUID channelUID, @Nullable List<? extends @Nullable String> names) {
        if (names == null) {
            return;
        }
        List<StateOption> options = new ArrayList<>();
        for (String name : names) {
            // A list that came over the broker can have holes
            if (name != null && !name.isEmpty()) {
                options.add(new StateOption(name, name));
            }
        }
        if (!options.isEmpty()) {
            stateDescriptionProvider.setStateOptions(channelUID, options);
        }
    }

    private boolean isDiscoverable(String appName) {
        return APP_NAME_PATTERN.matcher(appName).matches() && !NOTIFICATION_APP.equals(appName)
                && (discoverDefaultApps || !BUILT_IN_APPS.contains(appName));
    }

    /**
     * The select button of an app that is controlled by the buttons locks the clock on that app, so that its own
     * buttons events can be used until the lock times out. While locked every button press also goes to the app.
     */
    private void handleButton(String channelId, String message) {
        boolean pressed = BUTTON_PRESSED_PAYLOAD.equals(message.trim());
        if (pressed) {
            pressedButtons.add(channelId);
        } else if (!pressedButtons.remove(channelId)) {
            // A clock reports the state of its buttons, a 0, when it connects, and nobody released a button then
            logger.trace("Ignoring a release of {} that was not pressed", channelId);
            return;
        }
        String event = pressed ? EVENT_PRESSED : EVENT_RELEASED;
        triggerChannel(new ChannelUID(thing.getUID(), channelId), event);

        AwtrixNgAppHandler app = appHandlers.get(currentApp);
        if (app == null) {
            return;
        }
        if (isAppLocked()) {
            scheduleAppLockTimeout();
            app.handleButton(channelId, event);
        } else if (CHANNEL_BUTTON_SELECT.equals(channelId) && EVENT_RELEASED.equals(event)
                && app.isButtonControlled()) {
            enterAppControlMode();
        }
    }

    private boolean isAppLocked() {
        synchronized (appLockMonitor) {
            return appLock;
        }
    }

    private void enterAppControlMode() {
        Settings toRestore = new Settings();
        synchronized (appLockMonitor) {
            if (appLock) {
                return;
            }
            appLock = true;
            pendingLockRestore = null;
            lockEndedRecently = false;
            lockIndicatorToRestore = reportedLockIndicator;
            // What the clock reported before the lock is what it goes back to, unknown means the defaults of NG
            Boolean autoTransition = reportedAutoTransition;
            Boolean blockNavigation = reportedBlockNavigation;
            autoTransitionToRestore = autoTransition == null || autoTransition;
            blockNavigationToRestore = blockNavigation != null && blockNavigation;
            toRestore.autoTransition = autoTransitionToRestore;
            toRestore.blockNavigation = blockNavigationToRestore;
            ownStoredLock = AwtrixNgHelper.toJson(toRestore);
        }
        // NG keeps the lock over a restart, so what to go back to is stored before the lock is made
        publish(TOPIC_STORE_APP_LOCK, AwtrixNgHelper.toJson(toRestore), true);
        Settings lock = new Settings();
        lock.autoTransition = false;
        lock.blockNavigation = true;
        publish(TOPIC_CMD_SETTINGS, AwtrixNgHelper.toJson(lock));
        scheduleAppLockTimeout();
    }

    /** Every press of a button extends the lock; it is also what shows the lock on the indicator. */
    private void scheduleAppLockTimeout() {
        synchronized (appLockMonitor) {
            if (!appLock) {
                return;
            }
            cancelAppLockJobs();
            appLockTimeoutJob = scheduler.schedule(this::leaveAppControlMode, appLockTimeout, TimeUnit.SECONDS);
            if (appLockTimeout > APP_LOCK_WARNING_SECONDS) {
                appLockWarningJob = scheduler.schedule(this::signalAppLockEnds,
                        appLockTimeout - APP_LOCK_WARNING_SECONDS, TimeUnit.SECONDS);
            }
        }
        showLockIndicator(null);
    }

    private void signalAppLockEnds() {
        showLockIndicator(APP_LOCK_WARNING_FADE_MS);
    }

    private void showLockIndicator(@Nullable Integer fadeMs) {
        setIndicator(APP_LOCK_INDICATOR, APP_LOCK_INDICATOR_COLOR, null, fadeMs);
        // The lock can end while the indicator is being set, then the end of the lock must have the last word
        boolean locked;
        Indicator toRestore;
        synchronized (appLockMonitor) {
            locked = appLock;
            toRestore = lockIndicatorToRestore;
        }
        if (!locked) {
            restoreLockIndicator(toRestore);
        }
    }

    /** Package private so that tests need not wait for the timeout. */
    void leaveAppControlMode() {
        Settings restore = new Settings();
        Indicator indicator;
        synchronized (appLockMonitor) {
            if (!appLock) {
                return;
            }
            appLock = false;
            // Set together with appLock, or the settings of the lock arriving in between count as the clock's own
            lockEndedRecently = true;
            cancelAppLockJobs();
            restore.autoTransition = autoTransitionToRestore;
            restore.blockNavigation = blockNavigationToRestore;
            indicator = lockIndicatorToRestore;
        }
        endLock(restore, indicator);
    }

    /**
     * Sets the clock back to what it was before the lock. A clock that is offline does not receive the commands, so
     * this waits until it is online and leaves the stored lock where it is until then.
     */
    private void endLock(Settings restore, @Nullable Indicator indicator) {
        if (!deviceOnline) {
            logger.debug("The clock is offline, the app lock is ended when it is online");
            synchronized (appLockMonitor) {
                // One that is waiting already came from the lock of this run, and knows the indicator too
                if (pendingLockRestore == null) {
                    pendingLockRestore = new LockRestore(restore, indicator);
                }
            }
            return;
        }
        synchronized (appLockMonitor) {
            lockEndedRecently = true;
        }
        publish(TOPIC_CMD_SETTINGS, AwtrixNgHelper.toJson(restore));
        publish(TOPIC_STORE_APP_LOCK, "", true);
        restoreLockIndicator(indicator);
    }

    private void finishPendingLockRestore() {
        @Nullable
        LockRestore restore;
        synchronized (appLockMonitor) {
            restore = pendingLockRestore;
            pendingLockRestore = null;
        }
        if (restore != null) {
            endLock(restore.settings(), restore.indicator());
        }
    }

    private void restoreLockIndicator(@Nullable Indicator indicator) {
        String color = indicator == null || !indicator.on ? null : AwtrixNgHelper.normalizeColor(indicator.color);
        if (indicator == null || color == null) {
            resetIndicator(APP_LOCK_INDICATOR);
            return;
        }
        setIndicator(APP_LOCK_INDICATOR, color, indicator.blinkMs > 0 ? indicator.blinkMs : null,
                indicator.fadeMs > 0 ? indicator.fadeMs : null);
    }

    /**
     * An app lock that is stored while this handler holds none is left from a run that could not end it, for example
     * because openHAB stopped or the broker was gone when the timeout passed. It is ended now.
     */
    private void handleStoredAppLock(String message) {
        if (message.isBlank()) {
            return;
        }
        synchronized (appLockMonitor) {
            // The message is our own, which can also be delivered after the lock has ended again
            if (appLock || message.equals(ownStoredLock)) {
                return;
            }
        }
        Settings stored = AwtrixNgHelper.fromJson(message, Settings.class);
        if (stored == null || stored.autoTransition == null || stored.blockNavigation == null) {
            logger.debug("Ignoring the stored app lock {}", message);
            return;
        }
        logger.debug("Ending the app lock that an earlier run left on the clock");
        Settings restore = new Settings();
        restore.autoTransition = stored.autoTransition;
        restore.blockNavigation = stored.blockNavigation;
        endLock(restore, null);
    }

    /** Must be called with the app lock monitor held. */
    private void cancelAppLockJobs() {
        ScheduledFuture<?> timeout = appLockTimeoutJob;
        if (timeout != null) {
            timeout.cancel(false);
            appLockTimeoutJob = null;
        }
        ScheduledFuture<?> warning = appLockWarningJob;
        if (warning != null) {
            warning.cancel(false);
            appLockWarningJob = null;
        }
    }

    private void handleError(String message) {
        logger.debug("The clock reported an error: {}", message);
        triggerChannel(new ChannelUID(thing.getUID(), CHANNEL_ERROR), message);
    }

    private <Q extends Quantity<Q>> void updateQuantity(String channelId, @Nullable Number value, Unit<Q> unit) {
        if (value != null) {
            updateChannel(channelId, new QuantityType<>(value, unit));
        }
    }

    private void updateChannel(String channelId, State state) {
        if (!CHANNEL_SCREEN.equals(channelId)) {
            lastStates.put(channelId, state);
        }
        updateState(new ChannelUID(thing.getUID(), channelId), state);
    }
}
