/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_APP;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_BATTERY;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_BRIGHTNESS;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_DISPLAY;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_HUMIDITY;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_INDICATOR1_COLOR;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_INDICATOR2_COLOR;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_INDICATOR3_COLOR;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_LOW_BATTERY;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_LUX;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_RSSI;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_SCREEN;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_APP;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_BATTERY;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_BATTERY_RAW;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_BRIGHTNESS;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_FIRMWARE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_HUMIDITY;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_INDICATOR1;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_INDICATOR2;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_INDICATOR3;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_LDR_RAW;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_LUX;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_MESSAGES;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_RAM;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_TEMPERATURE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_TYPE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_UID;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_UPTIME;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.FIELD_BRIDGE_WIFI_SIGNAL;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.INDICATOR1_TOPIC;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.INDICATOR2_TOPIC;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.INDICATOR3_TOPIC;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.LOW_BAT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.PROP_FIRMWARE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.PROP_UNIQUEID;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.PROP_VENDOR;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.STATS_CURRENT_APP_TOPIC;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.STATS_TOPIC;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.TOPIC_POWER;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.TOPIC_REBOOT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.TOPIC_SCREEN;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.TOPIC_SEND_SCREEN;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.TOPIC_SOUND;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.TOPIC_UPGRADE;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.BridgeConfigOptions;
import org.openhab.binding.mqtt.awtrixlight.internal.Helper;
import org.openhab.binding.mqtt.awtrixlight.internal.action.AwtrixActions;
import org.openhab.binding.mqtt.awtrixlight.internal.discovery.AwtrixLightBridgeDiscoveryService;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.HSBType;
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
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwtrixLightHandler} is responsible for ...
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AwtrixLightBridgeHandler extends BaseBridgeHandler implements MqttMessageSubscriber {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private @Nullable MqttBrokerConnection connection;

    private String basetopic = "";
    private String channelPrefix = "";

    private Map<String, AwtrixLightAppHandler> appHandlers = new HashMap<String, AwtrixLightAppHandler>();

    private @Nullable AwtrixLightBridgeDiscoveryService discoveryCallback;

    public AwtrixLightBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        if (channelUID.getId() == this.channelPrefix + CHANNEL_DISPLAY) {
            handleDisplay(command);
        } else if (channelUID.getId() == this.channelPrefix + CHANNEL_INDICATOR1_COLOR) {
            handleIndicator(command, 1);
        } else if (channelUID.getId() == this.channelPrefix + CHANNEL_INDICATOR2_COLOR) {
            handleIndicator(command, 2);
        } else if (channelUID.getId() == this.channelPrefix + CHANNEL_INDICATOR3_COLOR) {
            handleIndicator(command, 3);
        }
    }

    public String getBaseTopic() {
        return this.basetopic;
    }

    // TODO: Incomplete and not yet tested
    private void handleIndicator(Command command, int indicator) {
        // OnOff, IncreaseDecrease, Percent, HSB, Refresh
        String topic = null;
        if (indicator == 1) {
            topic = INDICATOR1_TOPIC;
        } else if (indicator == 2) {
            topic = INDICATOR2_TOPIC;
        } else if (indicator == 3) {
            topic = INDICATOR3_TOPIC;
        }
        if (topic != null) {
            if (command instanceof HSBType) {
                int[] hsbToRgb = ColorUtil.hsbToRgb((HSBType) command);
                sendMQTT(this.basetopic + INDICATOR1_TOPIC,
                        "{\"color\":[" + hsbToRgb[0] + "," + hsbToRgb[1] + "," + hsbToRgb[2] + "]}", false);
            } else if (command instanceof OnOffType) {
                if (command.equals(OnOffType.OFF)) {
                    sendMQTT(this.basetopic + INDICATOR1_TOPIC, "{\"color\":\"0\"}", false);
                } else {
                    // ???
                }
            }
        }
    }

    private void handleDisplay(Command command) {
        if (command instanceof OnOffType) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("power", OnOffType.ON.equals(command));
            String json = Helper.encodeJson(params);
            sendMQTT(this.basetopic + TOPIC_POWER, "{" + json + "}", false);
        }
    }

    private void sendMQTT(String commandTopic, String payload, boolean retain) {
        logger.debug("Will send {} to topic {}", payload, commandTopic);
        byte[] payloadBytes = payload.getBytes();
        MqttBrokerConnection localConnection = connection;
        if (payloadBytes != null) {
            if (localConnection != null) {
                localConnection.publish(commandTopic, payloadBytes, 1, retain);
            }
        }
    }

    void updateApp(String appName, String payload) {
        sendMQTT(this.basetopic + "/custom/" + appName, payload, true);
    }

    void deleteApp(String appName) {
        logger.debug("Deleting app {}", appName);
        sendMQTT(this.basetopic + "/custom/" + appName, "", true);
    }

    @Override
    public void initialize() {
        BridgeConfigOptions config = getConfigAs(BridgeConfigOptions.class);
        this.basetopic = config.basetopic;
        this.channelPrefix = thing.getUID() + ":";
        logger.debug("Configured handler with baseTopic {} and channelPrefix {}", this.basetopic, this.channelPrefix);
        bridgeStatusChanged(getBridgeStatus());
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        String payloadString = new String(payload, StandardCharsets.UTF_8);
        if (topic.endsWith(STATS_TOPIC)) {
            if (thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            handleStatsMessage(payloadString);
        } else if (topic.endsWith(STATS_CURRENT_APP_TOPIC)) {
            handleCurrentAppMessage(payloadString);
        } else if (topic.endsWith(TOPIC_SCREEN)) {
            handleScreenMessage(payloadString);
        }
    }

    private void handleScreenMessage(String screenMessage) {
        logger.trace("Incoming screen message {}", screenMessage);
        byte[] bytes = Helper.decodeImage(screenMessage);
        updateState(new ChannelUID(channelPrefix + CHANNEL_SCREEN), new RawType(bytes, "image/png"));
    }

    private void handleCurrentAppMessage(String currentAppMessage) {
        logger.trace("Incoming currentApp message {}", currentAppMessage);
        ThingHandlerCallback callback = getCallback();
        if (callback != null && callback.isChannelLinked(new ChannelUID(this.channelPrefix + CHANNEL_SCREEN))) {
            sendMQTT(this.basetopic + TOPIC_SEND_SCREEN, "", false);
        }
        if (!this.appHandlers.containsKey(currentAppMessage)) {
            AwtrixLightBridgeDiscoveryService localDiscoveryCallback = discoveryCallback;
            if (localDiscoveryCallback != null) {
                localDiscoveryCallback.appDiscovered(this.basetopic, currentAppMessage);
            }
        }
    }

    private void handleStatsMessage(String statsMessage) {
        logger.trace("Incoming stats message {}", statsMessage);
        HashMap<String, Object> params = Helper.decodeJson(statsMessage);
        for (HashMap.Entry<String, Object> entry : params.entrySet()) {

            Map<String, String> properties = thing.getProperties();
            Object value = entry.getValue();
            switch (entry.getKey()) {
                case FIELD_BRIDGE_UID:
                    if (!properties.containsValue(PROP_UNIQUEID) || properties.get(PROP_UNIQUEID) == null
                            || !properties.get(PROP_UNIQUEID).equals((String) value)) {
                        thing.setProperty(PROP_UNIQUEID, (String) value);
                    }
                    break;
                case FIELD_BRIDGE_BATTERY:
                    if (value instanceof BigDecimal) {
                        updateState(new ChannelUID(channelPrefix + CHANNEL_BATTERY),
                                new QuantityType<>((BigDecimal) value, Units.PERCENT));
                        OnOffType lowBattery = ((BigDecimal) value).compareTo(LOW_BAT) <= 0 ? OnOffType.ON
                                : OnOffType.OFF;
                        updateState(new ChannelUID(channelPrefix + CHANNEL_LOW_BATTERY), lowBattery);
                    }
                    break;
                case FIELD_BRIDGE_BATTERY_RAW:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_FIRMWARE:
                    if (!properties.containsValue(PROP_FIRMWARE) || properties.get(PROP_FIRMWARE) == null
                            || !properties.get(PROP_FIRMWARE).equals((String) value)) {
                        thing.setProperty(PROP_FIRMWARE, (String) value);
                    }
                    break;
                case FIELD_BRIDGE_TYPE:
                    String vendor = value.equals(0) ? "Ulanzi" : "Generic";
                    if (!properties.containsValue(PROP_VENDOR) || properties.get(PROP_FIRMWARE) == null
                            || !properties.get(PROP_VENDOR).equals(vendor)) {
                        thing.setProperty(PROP_VENDOR, vendor);
                    }
                    break;
                case FIELD_BRIDGE_LUX:
                    updateState(new ChannelUID(channelPrefix + CHANNEL_LUX),
                            new QuantityType<>((BigDecimal) value, Units.LUX));
                    break;
                case FIELD_BRIDGE_LDR_RAW:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_RAM:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_BRIGHTNESS:
                    updateState(new ChannelUID(channelPrefix + CHANNEL_BRIGHTNESS),
                            new QuantityType<>((BigDecimal) value, Units.PERCENT));
                    break;
                case FIELD_BRIDGE_TEMPERATURE:
                    updateState(new ChannelUID(channelPrefix + CHANNEL_TEMPERATURE),
                            new QuantityType<>((BigDecimal) value, SIUnits.CELSIUS));
                    break;
                case FIELD_BRIDGE_HUMIDITY:
                    updateState(new ChannelUID(channelPrefix + CHANNEL_HUMIDITY),
                            new QuantityType<>((BigDecimal) value, Units.PERCENT));
                    break;
                case FIELD_BRIDGE_UPTIME:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_WIFI_SIGNAL:
                    updateState(new ChannelUID(channelPrefix + CHANNEL_RSSI),
                            new QuantityType<>((BigDecimal) value, Units.ONE));
                    break;
                case FIELD_BRIDGE_MESSAGES:
                    // Not mapped to channel atm
                    break;
                case FIELD_BRIDGE_INDICATOR1:
                    OnOffType indicator1 = (Boolean) value ? OnOffType.ON : OnOffType.OFF;
                    updateState(new ChannelUID(channelPrefix + CHANNEL_INDICATOR1_COLOR), indicator1);
                    break;
                case FIELD_BRIDGE_INDICATOR2:
                    OnOffType indicator2 = (Boolean) value ? OnOffType.ON : OnOffType.OFF;
                    updateState(new ChannelUID(channelPrefix + CHANNEL_INDICATOR2_COLOR), indicator2);
                    break;
                case FIELD_BRIDGE_INDICATOR3:
                    OnOffType indicator3 = (Boolean) value ? OnOffType.ON : OnOffType.OFF;
                    updateState(new ChannelUID(channelPrefix + CHANNEL_INDICATOR3_COLOR), indicator3);
                    break;
                case FIELD_BRIDGE_APP:
                    updateState(new ChannelUID(channelPrefix + CHANNEL_APP), new StringType((String) value));
                    break;
            }
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
        if (handler instanceof AbstractBrokerHandler) {
            AbstractBrokerHandler abh = (AbstractBrokerHandler) handler;
            final MqttBrokerConnection connection;
            try {
                connection = abh.getConnectionAsync().get(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                        "Bridge handler has no valid broker connection!");
                return;
            }
            this.connection = connection;
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Waiting for first MQTT message to be received.");
            connection.subscribe(basetopic + STATS_TOPIC + "/#", this);
            connection.subscribe(basetopic + STATS_CURRENT_APP_TOPIC + "/#", this);
            connection.subscribe(basetopic + TOPIC_SCREEN + "/#", this);
        }
        return;
    }

    public @Nullable MqttBrokerConnection getBrokerConnection() {
        return connection;
    }

    @Override
    public void dispose() {
        MqttBrokerConnection localConnection = connection;
        if (localConnection != null) {
            localConnection.unsubscribe(basetopic + STATS_TOPIC + "/#", this);
            localConnection.unsubscribe(basetopic + STATS_CURRENT_APP_TOPIC + "/#", this);
            localConnection.unsubscribe(basetopic + TOPIC_SCREEN + "/#", this);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof AwtrixLightAppHandler) {
            AwtrixLightAppHandler alah = (AwtrixLightAppHandler) childHandler;
            this.appHandlers.put(alah.getAppName(), alah);
            MqttBrokerConnection localConnection = connection;
            if (localConnection != null) {
                localConnection.subscribe(basetopic + "/custom/" + alah.getAppName(), alah);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof AwtrixLightAppHandler) {
            AwtrixLightAppHandler alah = (AwtrixLightAppHandler) childHandler;
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
        this.sendMQTT(TOPIC_REBOOT, "", false);
    }

    public void playSound(String melodyName) {
        this.sendMQTT(TOPIC_SOUND, "{\"sound\":\"" + melodyName + "\"}", false);
    }

    public void upgrade() {
        this.sendMQTT(TOPIC_UPGRADE, "", false);
    }

    public void addAppDiscoveryCallback(AwtrixLightBridgeDiscoveryService awtrixLightBridgeDiscoveryService) {
        this.discoveryCallback = awtrixLightBridgeDiscoveryService;
    }

    public void removeAppDiscoveryCallback() {
        this.discoveryCallback = null;
    }
}
