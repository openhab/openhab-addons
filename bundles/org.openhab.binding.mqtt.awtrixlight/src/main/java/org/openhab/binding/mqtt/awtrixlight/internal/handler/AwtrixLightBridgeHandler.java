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

package org.openhab.binding.mqtt.awtrixlight.internal.handler;

import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.BridgeConfigOptions;
import org.openhab.binding.mqtt.awtrixlight.internal.Helper;
import org.openhab.binding.mqtt.awtrixlight.internal.action.AwtrixActions;
import org.openhab.binding.mqtt.awtrixlight.internal.app.AwtrixNotification;
import org.openhab.binding.mqtt.awtrixlight.internal.discovery.AwtrixLightBridgeDiscoveryService;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwtrixLightBridgeHandler} is responsible for handling commands for an awtrix clock device. It is also
 * responsible for pushing discovery events to the discovery service. It also delegates trigger events to apps when they
 * have been locked onto the clock.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AwtrixLightBridgeHandler extends BaseBridgeHandler implements MqttMessageSubscriber {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private @Nullable MqttBrokerConnection connection;

    private boolean appLock = false;
    private int appLockTimeout = 10;
    private String basetopic = "";
    private String channelPrefix = "";
    private String currentApp = "";
    private boolean discoverDefaultApps = false;
    private boolean deviceOnline = false;
    private int lowBatteryThreshold = 25;

    private Map<String, AwtrixLightAppHandler> appHandlers = new HashMap<String, AwtrixLightAppHandler>();

    private @Nullable AwtrixLightBridgeDiscoveryService discoveryCallback;
    private @Nullable ScheduledFuture<?> scheduledAppLockTimeout;
    private @Nullable ScheduledFuture<?> scheduledFadeBeforeTimeout;

    public AwtrixLightBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} of type {} via channel {}", command, command.getClass(),
                channelUID.getAsString());
        if (command instanceof RefreshType) {
            if (CHANNEL_SCREEN.equals(channelUID.getId())) {
                sendMQTT(this.basetopic + TOPIC_SEND_SCREEN, "", false);
            }
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_AUTO_BRIGHTNESS:
                if (command instanceof OnOffType) {
                    boolean param = OnOffType.ON.equals(command);
                    sendMQTT(this.basetopic + TOPIC_SETTINGS,
                            "{\"" + FIELD_BRIDGE_SET_AUTO_BRIGHTNESS + "\":" + param + "}", false);
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (command instanceof QuantityType quantityCommand) {
                    double brightnessInt = quantityCommand.doubleValue();
                    if (0 <= brightnessInt && brightnessInt <= 100) {
                        long param = Math.round(255 * (brightnessInt / 100));
                        sendMQTT(this.basetopic + TOPIC_SETTINGS, "{\"" + FIELD_BRIDGE_SET_AUTO_BRIGHTNESS
                                + "\":false,\"" + FIELD_BRIDGE_SET_BRIGHTNESS + "\":" + param + "}", false);
                        updateState(new ChannelUID(channelPrefix + CHANNEL_AUTO_BRIGHTNESS), OnOffType.OFF);
                    }
                }
                break;
            case CHANNEL_DISPLAY:
                handleDisplay(command);
                break;
            case CHANNEL_INDICATOR1:
                if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        activateIndicator(1, new int[] { 0, 255, 0 });
                    } else {
                        deactivateIndicator(1);
                    }
                }
                break;
            case CHANNEL_INDICATOR2:
                if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        activateIndicator(2, new int[] { 0, 255, 0 });
                    } else {
                        deactivateIndicator(2);
                    }
                }
                break;
            case CHANNEL_INDICATOR3:
                if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        activateIndicator(3, new int[] { 0, 255, 0 });
                    } else {
                        deactivateIndicator(3);
                    }
                }
                break;
            case CHANNEL_SOUND:
                if (command instanceof StringType) {
                    playSound(command.toString());
                }
                break;
            case CHANNEL_RTTTL:
                if (command instanceof StringType) {
                    playRtttl(command.toString());
                }
                break;
        }
    }

    public String getBaseTopic() {
        return this.basetopic;
    }

    @Override
    public void initialize() {
        BridgeConfigOptions config = getConfigAs(BridgeConfigOptions.class);
        this.basetopic = config.basetopic;
        this.appLockTimeout = config.appLockTimeout;
        this.discoverDefaultApps = config.discoverDefaultApps;
        this.lowBatteryThreshold = config.lowBatteryThreshold;
        this.channelPrefix = thing.getUID() + ":";
        logger.debug(
                "Configured handler with baseTopic {}, channelPrefix {}, appLockTimeout {}, discoverDefaultApps {}",
                this.basetopic, this.channelPrefix, this.appLockTimeout, this.discoverDefaultApps);
        bridgeStatusChanged(getBridgeStatus());
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        String payloadString = new String(payload, StandardCharsets.UTF_8);
        if (topic.endsWith(TOPIC_STATS)) {
            ThingStatusInfo statusInfo = getThing().getStatusInfo();
            if (ThingStatus.UNKNOWN == statusInfo.getStatus()
                    && ThingStatusDetail.CONFIGURATION_PENDING == statusInfo.getStatusDetail()) {
                // Obviously the device is online. We just haven't received a LWT message yet.
                this.deviceOnline = true;
                updateStatus(ThingStatus.ONLINE);
            }
            handleStatsMessage(payloadString);
        } else if (topic.endsWith(TOPIC_LWT)) {
            handleLwtMessage(payloadString);
        } else if (topic.endsWith(TOPIC_STATS_CURRENT_APP)) {
            handleCurrentAppMessage(payloadString);
        } else if (topic.endsWith(TOPIC_SCREEN)) {
            handleScreenMessage(payloadString);
        } else if (topic.endsWith(TOPIC_BUTLEFT)) {
            String event = "1".equals(payloadString) ? "PRESSED" : "RELEASED";
            handleLeftButton(event);
        } else if (topic.endsWith(TOPIC_BUTRIGHT)) {
            String event = "1".equals(payloadString) ? "PRESSED" : "RELEASED";
            handleRightButton(event);
        } else if (topic.endsWith(TOPIC_BUTSELECT)) {
            String event = "1".equals(payloadString) ? "PRESSED" : "RELEASED";
            handleSelectButton(event);
        }
    }

    public ThingStatusInfo getBridgeStatus() {
        Bridge b = getBridge();
        if (b != null) {
            return b.getStatusInfo();
        } else {
            return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            connection = null;
            return;
        }
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        Bridge localBridge = this.getBridge();
        if (localBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Bridge is missing or offline, you need to setup a working MQTT broker first.");
            return;
        }
        ThingHandler handler = localBridge.getHandler();
        if (handler instanceof AbstractBrokerHandler abh) {
            @Nullable
            final MqttBrokerConnection connection;
            try {
                connection = abh.getConnectionAsync().get(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                        "Bridge handler has no valid broker connection!");
                return;
            }
            this.connection = connection;
            if (this.deviceOnline) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for LWT message.");
            }
            connection.subscribe(this.basetopic + TOPIC_LWT, this);
            connection.subscribe(this.basetopic + TOPIC_STATS + "/#", this);
            connection.subscribe(this.basetopic + TOPIC_STATS_CURRENT_APP + "/#", this);
            connection.subscribe(this.basetopic + TOPIC_SCREEN + "/#", this);
            for (Map.Entry<String, AwtrixLightAppHandler> entry : this.appHandlers.entrySet()) {
                connection.subscribe(this.basetopic + "/custom/" + entry.getKey(), entry.getValue());
            }
        }
        return;
    }

    public @Nullable MqttBrokerConnection getBrokerConnection() {
        return connection;
    }

    @Override
    public void dispose() {
        leaveAppControlMode();
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            localConnection.unsubscribe(this.basetopic + TOPIC_LWT, this);
            localConnection.unsubscribe(this.basetopic + TOPIC_STATS + "/#", this);
            localConnection.unsubscribe(this.basetopic + TOPIC_STATS_CURRENT_APP + "/#", this);
            localConnection.unsubscribe(this.basetopic + TOPIC_SCREEN + "/#", this);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof AwtrixLightAppHandler alah) {
            this.appHandlers.put(alah.getAppName(), alah);
            MqttBrokerConnection localConnection = connection;
            if (localConnection != null) {
                localConnection.subscribe(basetopic + "/custom/" + alah.getAppName(), alah);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof AwtrixLightAppHandler alah) {
            this.appHandlers.remove(alah.getAppName());
            MqttBrokerConnection localConnection = connection;
            if (localConnection != null) {
                localConnection.unsubscribe(basetopic + "/custom/" + alah.getAppName(), alah);
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        ArrayList<Class<? extends ThingHandlerService>> services = new ArrayList<Class<? extends ThingHandlerService>>();
        services.add(AwtrixActions.class);
        services.add(AwtrixLightBridgeDiscoveryService.class);
        return services;
    }

    public void reboot() {
        this.sendMQTT(this.basetopic + TOPIC_REBOOT, "", false);
    }

    public void sleep(int seconds) {
        this.sendMQTT(this.basetopic + TOPIC_SLEEP, "{\"sleep\":" + seconds + "}", false);
    }

    public void playSound(String melodyName) {
        this.sendMQTT(this.basetopic + TOPIC_SOUND, "{\"sound\":\"" + melodyName + "\"}", false);
    }

    public void playRtttl(String rtttl) {
        this.sendMQTT(this.basetopic + TOPIC_RTTTL, rtttl, false);
    }

    public void upgrade() {
        this.sendMQTT(this.basetopic + TOPIC_UPGRADE, "", false);
    }

    public void showNotification(boolean hold, boolean wakeUp, boolean stack, @Nullable String rtttl,
            @Nullable String sound, boolean loopSound, Map<String, Object> params) {
        AwtrixNotification notification = new AwtrixNotification();
        notification.updateFields(params);
        notification.setHold(hold);
        notification.setWakeUp(wakeUp);
        notification.setStack(stack);
        if (rtttl != null) {
            notification.setRtttl(rtttl);
        }
        if (sound != null) {
            notification.setSound(sound);
        }
        notification.setLoopSound(loopSound);
        String notificationMessage = notification.getAppConfig();
        this.sendMQTT(this.basetopic + TOPIC_NOTIFY, notificationMessage, false);
    }

    public void setAppDiscoveryCallback(AwtrixLightBridgeDiscoveryService awtrixLightBridgeDiscoveryService) {
        this.discoveryCallback = awtrixLightBridgeDiscoveryService;
    }

    public void removeAppDiscoveryCallback() {
        this.discoveryCallback = null;
    }

    void updateApp(String appName, String payload) {
        sendMQTT(this.basetopic + "/custom/" + appName, payload, true);
    }

    void deleteApp(String appName) {
        logger.debug("Deleting app {}", appName);
        sendMQTT(this.basetopic + "/custom/" + appName, "", true);
    }

    private String getIndicatorTopic(int indicatorId) {
        String indicatorTopic = "";
        if (indicatorId == 1) {
            indicatorTopic = TOPIC_INDICATOR1;
        } else if (indicatorId == 2) {
            indicatorTopic = TOPIC_INDICATOR2;
        } else if (indicatorId == 3) {
            indicatorTopic = TOPIC_INDICATOR3;
        }
        return indicatorTopic;
    }

    public void blinkIndicator(int id, int[] rgb, int blinkInMs) {
        String indicatorTopic = getIndicatorTopic(id);
        if (!"".equals(indicatorTopic) && rgb.length == 3) {
            sendMQTT(this.basetopic + indicatorTopic,
                    "{\"color\":[" + rgb[0] + "," + rgb[1] + "," + rgb[2] + "],\"blink\":" + blinkInMs + "}", false);
        }
    }

    public void fadeIndicator(int id, int[] rgb, int fadeInMs) {
        String indicatorTopic = getIndicatorTopic(id);
        if (!"".equals(indicatorTopic) && rgb.length == 3) {
            sendMQTT(this.basetopic + indicatorTopic,
                    "{\"color\":[" + rgb[0] + "," + rgb[1] + "," + rgb[2] + "],\"fade\":" + fadeInMs + "}", false);
        }
    }

    public void activateIndicator(int id, int[] rgb) {
        String indicatorTopic = getIndicatorTopic(id);
        if (!"".equals(indicatorTopic) && rgb.length == 3) {
            sendMQTT(this.basetopic + indicatorTopic, "{\"color\":[" + rgb[0] + "," + rgb[1] + "," + rgb[2] + "]}",
                    false);
        }
    }

    public void deactivateIndicator(int id) {
        String indicatorTopic = getIndicatorTopic(id);
        if (!"".equals(indicatorTopic)) {
            sendMQTT(this.basetopic + indicatorTopic, "{\"color\":\"0\"}", false);
        }
    }

    private void handleDisplay(Command command) {
        if (command instanceof OnOffType) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("power", OnOffType.ON.equals(command));
            String json = Helper.encodeJson(params);
            sendMQTT(this.basetopic + TOPIC_POWER, json, false);
        }
    }

    private void sendMQTT(String commandTopic, String payload, boolean retain) {
        logger.debug("Will send {} to topic {}", payload, commandTopic);
        byte[] payloadBytes = payload.getBytes();
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            localConnection.publish(commandTopic, payloadBytes, 1, retain);
        }
    }

    private void handleLeftButton(String event) {
        triggerChannel(new ChannelUID(channelPrefix + CHANNEL_BUTLEFT), event);
        if (this.appLock) {
            scheduleAppLockTimeout();
            @Nullable
            AwtrixLightAppHandler alah = this.appHandlers.get(this.currentApp);
            if (alah != null) {
                alah.handleLeftButton(event);
            }
        }
    }

    private void handleRightButton(String event) {
        triggerChannel(new ChannelUID(channelPrefix + CHANNEL_BUTRIGHT), event);
        if (this.appLock) {
            scheduleAppLockTimeout();
            @Nullable
            AwtrixLightAppHandler alah = this.appHandlers.get(this.currentApp);
            if (alah != null) {
                alah.handleRightButton(event);
            }
        }
    }

    private void handleSelectButton(String event) {
        triggerChannel(new ChannelUID(channelPrefix + CHANNEL_BUTSELECT), event);
        @Nullable
        AwtrixLightAppHandler alah = this.appHandlers.get(this.currentApp);
        if (alah != null) {
            if (!this.appLock) {
                if (alah.isButtonControlled()) {
                    if ("RELEASED".equals(event)) {
                        sendMQTT(this.basetopic + TOPIC_SETTINGS, "{\"" + FIELD_BRIDGE_SET_AUTO_TRANSITION
                                + "\":false,\"" + FIELD_BRIDGE_SET_BLOCK_KEYS + "\":true}", false);
                        this.appLock = true;
                        scheduleAppLockTimeout();
                    }
                }
            } else {
                scheduleAppLockTimeout();
                alah.handleSelectButton(event);
            }
        }
    }

    private void scheduleAppLockTimeout() {
        activateIndicator(3, new int[] { 255, 0, 0 });
        Future<?> localSignalSchedule = this.scheduledFadeBeforeTimeout;
        if (localSignalSchedule != null && !localSignalSchedule.isCancelled() && !localSignalSchedule.isDone()) {
            localSignalSchedule.cancel(true);
        }
        Future<?> localSchedule = this.scheduledAppLockTimeout;
        if (localSchedule != null && !localSchedule.isCancelled() && !localSchedule.isDone()) {
            localSchedule.cancel(true);
            scheduleAppLockTimeout();
        } else {
            this.scheduledAppLockTimeout = scheduler.schedule(this::leaveAppControlMode, this.appLockTimeout,
                    TimeUnit.SECONDS);
            if (this.appLockTimeout > 3) {
                this.scheduledFadeBeforeTimeout = scheduler.schedule(this::signalLeaveAppControlMode,
                        this.appLockTimeout - 3, TimeUnit.SECONDS);
            }
        }
    }

    private void leaveAppControlMode() {
        this.appLock = false;
        Future<?> localSignalSchedule = this.scheduledFadeBeforeTimeout;
        if (localSignalSchedule != null && !localSignalSchedule.isCancelled() && !localSignalSchedule.isDone()) {
            localSignalSchedule.cancel(true);
        }
        Future<?> localSchedule = this.scheduledAppLockTimeout;
        if (localSchedule != null && !localSchedule.isCancelled() && !localSchedule.isDone()) {
            localSchedule.cancel(true);
        }
        deactivateIndicator(3);
        sendMQTT(this.basetopic + TOPIC_SETTINGS,
                "{\"" + FIELD_BRIDGE_SET_AUTO_TRANSITION + "\":true,\"" + FIELD_BRIDGE_SET_BLOCK_KEYS + "\":false}",
                false);
    }

    private void signalLeaveAppControlMode() {
        fadeIndicator(3, new int[] { 255, 0, 0 }, 500);
    }

    private void handleScreenMessage(String screenMessage) {
        byte[] bytes = Helper.decodeImage(screenMessage);
        updateState(new ChannelUID(channelPrefix + CHANNEL_SCREEN), new RawType(bytes, "image/png"));
    }

    private void handleCurrentAppMessage(String currentAppMessage) {
        this.currentApp = currentAppMessage;
        ThingHandlerCallback callback = getCallback();
        if (callback != null && callback.isChannelLinked(new ChannelUID(this.channelPrefix + CHANNEL_SCREEN))) {
            sendMQTT(this.basetopic + TOPIC_SEND_SCREEN, "", false);
        }
        if (!this.appHandlers.containsKey(currentAppMessage)) {
            if (this.discoverDefaultApps || !Arrays.stream(DEFAULT_APPS).anyMatch(currentAppMessage::equals)) {
                AwtrixLightBridgeDiscoveryService localDiscoveryCallback = discoveryCallback;
                if (localDiscoveryCallback != null) {
                    localDiscoveryCallback.appDiscovered(this.basetopic, currentAppMessage);
                }
            }
        } else {
            @Nullable
            AwtrixLightAppHandler alah = this.appHandlers.get(currentAppMessage);
            if (alah != null) {
                alah.setActive(true);
            }
        }
    }

    private void handleStatsMessage(String statsMessage) {
        Map<String, Object> params = Helper.decodeStatsJson(statsMessage);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            @Nullable
            String key = entry.getKey();
            @Nullable
            Object value = entry.getValue();
            switch (key) {
                case FIELD_BRIDGE_UID:
                    thing.setProperty(PROP_UNIQUEID, (String) value);
                    break;
                case FIELD_BRIDGE_BATTERY:
                    if (value instanceof Number numberVal) {
                        updateState(new ChannelUID(channelPrefix + CHANNEL_BATTERY),
                                new QuantityType<>(numberVal, Units.PERCENT));
                        OnOffType lowBattery = numberVal.intValue() < this.lowBatteryThreshold ? OnOffType.ON
                                : OnOffType.OFF;
                        updateState(new ChannelUID(channelPrefix + CHANNEL_LOW_BATTERY), lowBattery);
                    }
                    break;
                case FIELD_BRIDGE_BATTERY_RAW:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_FIRMWARE:
                    thing.setProperty(PROP_FIRMWARE, value.toString());
                    break;
                case FIELD_BRIDGE_TYPE:
                    if (value instanceof Number numberVal) {
                        String vendor = numberVal.intValue() == 0 ? "Ulanzi" : "Generic";
                        thing.setProperty(PROP_VENDOR, vendor);
                    }
                    break;
                case FIELD_BRIDGE_LUX:
                    if (value instanceof Number numberVal) {
                        updateState(new ChannelUID(channelPrefix + CHANNEL_LUX),
                                new QuantityType<>(numberVal, Units.LUX));
                    }
                    break;
                case FIELD_BRIDGE_LDR_RAW:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_RAM:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_MATRIX:
                    if (value instanceof Boolean booleanVal) {
                        updateState(new ChannelUID(channelPrefix + CHANNEL_DISPLAY),
                                booleanVal ? OnOffType.ON : OnOffType.OFF);
                    }
                    break;
                case FIELD_BRIDGE_BRIGHTNESS:
                    if (value instanceof Number numberVal) {
                        long brightnessInPercent = Math.round((numberVal.doubleValue() / 255) * 100);
                        updateState(new ChannelUID(channelPrefix + CHANNEL_BRIGHTNESS),
                                new QuantityType<>(brightnessInPercent, Units.PERCENT));
                    }
                    break;
                case FIELD_BRIDGE_TEMPERATURE:
                    if (value instanceof Number numberVal) {
                        updateState(new ChannelUID(channelPrefix + CHANNEL_TEMPERATURE),
                                new QuantityType<>(numberVal, SIUnits.CELSIUS));
                    }
                    break;
                case FIELD_BRIDGE_HUMIDITY:
                    if (value instanceof Number numberVal) {
                        updateState(new ChannelUID(channelPrefix + CHANNEL_HUMIDITY),
                                new QuantityType<>(numberVal, Units.PERCENT));
                    }
                    break;
                case FIELD_BRIDGE_UPTIME:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_WIFI_SIGNAL:
                    if (value instanceof Number numberVal) {
                        updateState(new ChannelUID(channelPrefix + CHANNEL_RSSI),
                                new QuantityType<>(numberVal, Units.DECIBEL_MILLIWATTS));
                    }
                    break;
                case FIELD_BRIDGE_MESSAGES:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_INDICATOR1:
                    if (value instanceof Boolean booleanVal) {
                        OnOffType indicator1 = booleanVal ? OnOffType.ON : OnOffType.OFF;
                        updateState(new ChannelUID(channelPrefix + CHANNEL_INDICATOR1), indicator1);
                    }
                    break;
                case FIELD_BRIDGE_INDICATOR2:
                    if (value instanceof Boolean booleanVal) {
                        OnOffType indicator2 = booleanVal ? OnOffType.ON : OnOffType.OFF;
                        updateState(new ChannelUID(channelPrefix + CHANNEL_INDICATOR2), indicator2);
                    }
                    break;
                case FIELD_BRIDGE_INDICATOR3:
                    if (value instanceof Boolean booleanVal) {
                        OnOffType indicator3 = booleanVal ? OnOffType.ON : OnOffType.OFF;
                        updateState(new ChannelUID(channelPrefix + CHANNEL_INDICATOR3), indicator3);
                    }
                    break;
                case FIELD_BRIDGE_APP:
                    if (value instanceof String stringVal) {
                        updateState(new ChannelUID(channelPrefix + CHANNEL_APP), new StringType(stringVal));
                    }
                    break;
            }
        }
    }

    private void handleLwtMessage(String lwtMessage) {
        this.deviceOnline = "online".equals(lwtMessage);
        updateStatus(this.deviceOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }
}
