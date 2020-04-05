/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.enera.internal.handler;

import static org.openhab.binding.enera.internal.EneraBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.enera.internal.model.AggregationType;
import org.openhab.binding.enera.internal.model.AuthenticationHeaderValue;
import org.openhab.binding.enera.internal.model.ChannelDataSource;
import org.openhab.binding.enera.internal.model.DeviceValue;
import org.openhab.binding.enera.internal.model.RealtimeDataMessage;
import org.openhab.binding.enera.internal.model.RegistrationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link EneraDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Rahner - Initial contribution
 */
@NonNullByDefault
public class EneraDeviceHandler extends BaseThingHandler implements MqttCallbackExtended {

    private final Logger logger = LoggerFactory.getLogger(EneraDeviceHandler.class);
    // number of seconds for alive detection
    // if no data has been received for this period of time, we will re-register
    private final int LIVEDATA_TIMEOUT = 30;

    private String mqttClientId = "Client" + UUID.randomUUID();
    @Nullable
    private MqttClient mqttClient;
    private LocalTime lastMessageReceived = LocalTime.now();

    // save received values by Key for aggregation
    private Map<String, List<Float>> aggregatedValues = new HashMap<String, List<Float>>();

    private boolean firstMessageAfterInit = true;

    private static Map<String, ChannelDataSource> channelToSourceMapping = new HashMap<String, ChannelDataSource>();
    static {
        channelToSourceMapping.put(CHANNEL_CURRENT_CONSUMPTION,
                new ChannelDataSource(OBIS_LIVE_CONSUMPTION_TOTAL, AggregationType.AVERAGE));
        channelToSourceMapping.put(CHANNEL_METER_READING,
                new ChannelDataSource(OBIS_METER_READING, AggregationType.LAST));
        channelToSourceMapping.put(CHANNEL_METER_READING_OUTBOUND,
                new ChannelDataSource(OBIS_METER_READING_OUTBOUND, AggregationType.LAST));
    }

    @Nullable
    private ScheduledFuture<?> aliveChecker;

    public EneraDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                this.mqttClient = new MqttClient(LIVE_CONSUMPTION_URL, this.mqttClientId);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(LIVE_CONSUMPTION_USERNAME);
                options.setPassword(LIVE_CONSUMPTION_PASSWORD.toCharArray());
                options.setAutomaticReconnect(true);
                if (mqttClient != null) {
                    mqttClient.setCallback(this);
                    mqttClient.connect(options);
                }

            } catch (MqttException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            }

        });
    }

    public void registerForRealtimeApi() {
        MqttMessage message = new MqttMessage(getRegistrationMessage().getBytes(StandardCharsets.UTF_8));
        try {
            this.mqttClient.publish("RegisterForRealtimeApi", message);
            this.firstMessageAfterInit = true;
        } catch (MqttException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    public String getRegistrationMessage() {
        EneraAccountHandler bridgeHandler = (EneraAccountHandler) getBridge().getHandler();
        AuthenticationHeaderValue authHeader = bridgeHandler.getAuthorizationHeader();

        return new Gson().toJson(new RegistrationPayload(getThing().getProperties().get(PROPERTY_EXTERNAL_ID),
                this.mqttClientId, authHeader.getParameter()));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing myself");
        super.dispose();
        aliveChecker.cancel(true);
        scheduler.execute(() -> {
            if (mqttClient != null && mqttClient.isConnected()) {
                try {
                    mqttClient.disconnectForcibly();
                    mqttClient.close(true);
                    logger.debug("mqttClient has been disconnected.");
                } catch (MqttException ex) {
                    logger.warn("Exception while disconnecting the MQTT client!");
                    logger.warn(ex.getMessage());
                }
            }
        });
    }

    @Override
    public void connectComplete(boolean reconnect, @Nullable String serverURI) {
        logger.debug(String.format("Connection to MQTT server established. Is Reconnect: %b", reconnect));

        // subscribe to Live Consumption channel (md) and registration feedback (registration)
        try {
            mqttClient.subscribe("md/" + mqttClientId);
            mqttClient.subscribe("registration/" + mqttClientId);
        } catch (MqttException ex) {
            logger.warn("Exception while subscribing to MQTT channels!");
            logger.warn(ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        }

        registerForRealtimeApi();

        aliveChecker = scheduler.scheduleWithFixedDelay(() -> {
            // no data has been received for LIVEDATA_TIMEOUT seconds
            if (lastMessageReceived.plusSeconds(LIVEDATA_TIMEOUT).isBefore(LocalTime.now())) {
                logger.debug("Timeout exceeded for Live Data, trying to re-register");
                registerForRealtimeApi();
            }
        }, LIVEDATA_TIMEOUT / 3, LIVEDATA_TIMEOUT / 3, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void connectionLost(@Nullable Throwable cause) {
        logger.debug("Lost connection to MQTT server. Cause: {}", cause.getMessage());
        if (aliveChecker != null && !aliveChecker.isCancelled()) {
            aliveChecker.cancel(true);
        }
    }

    @Override
    public void deliveryComplete(@Nullable IMqttDeliveryToken token) {
    }

    @Override
    public void messageArrived(@Nullable String topic, @Nullable MqttMessage message) {
        try {
            int rate = Integer.parseInt(getThing().getConfiguration().get(CONFIG_RATE).toString());

            if (rate < 0) {
                rate = 60;
            }

            if (topic.equals("md/" + this.mqttClientId)) {
                lastMessageReceived = LocalTime.now();

                // live consumption message
                RealtimeDataMessage dataMessage = new Gson()
                        .fromJson(new String(message.getPayload(), StandardCharsets.UTF_8), RealtimeDataMessage.class);
                // this only gets the first message of DataMessages, because we are only subscribed to one single device
                // in the future, if a test account with multiple devices is available, this could be refactored for
                // efficiency
                List<DeviceValue> deviceValues = dataMessage.getDeviceDataItems().get(0).getDeviceValues();

                for (Map.Entry<String, ChannelDataSource> entry : channelToSourceMapping.entrySet()) {
                    Optional<DeviceValue> deviceValue = deviceValues.stream()
                            .filter(value -> value.getObis().equals(entry.getValue().getObisKey())).findFirst();
                    if (deviceValue.isPresent()) {
                        addValueToList(entry.getKey(), deviceValue.get().getValue());
                    }
                }

                // iterate through our value lists and check if we need to send out values to channels yet
                for (Map.Entry<String, List<Float>> entry : aggregatedValues.entrySet()) {
                    String channelName = entry.getKey();
                    List<Float> values = entry.getValue();

                    if (values.size() >= rate || firstMessageAfterInit) {
                        AggregationType aggType = channelToSourceMapping.get(channelName).getAggregationType();

                        Double value;
                        logger.debug("Aggregating values for channel {} as {}", channelName, aggType);
                        if (logger.isTraceEnabled()) {
                            logger.trace(String.format("Raw values are:"));
                            logger.trace(Arrays.toString(values.toArray()));
                        }
                        switch (aggType) {
                            case AVERAGE:
                                value = values.stream().mapToDouble(d -> d).average().getAsDouble();
                                break;
                            case MAX:
                                value = values.stream().mapToDouble(d -> d).max().getAsDouble();
                                break;
                            case LAST:
                                value = values.get(values.size() - 1).doubleValue();
                                break;
                            default:
                                value = 0.;
                                logger.debug("Unsupported Aggregation Type requested: {}",
                                        aggType.toString());
                        }
                        logger.trace(String.format("Resulting value is %.2f", value));
                        updateState(channelName, new DecimalType(value));
                        values.clear();
                    }
                }
                firstMessageAfterInit = false;

            } else if (topic.equals("registration/" + this.mqttClientId)) {
                // registration message
                // this topic only seems to receive error messages regarding registration
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        new String(message.getPayload(), StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            // safety net, because Exceptions in callbacks for MqttClient force disconnection
            logger.warn("Exception in messageArrived handler: {}", ex.getMessage());
        }
    }

    public void addValueToList(String key, float value) {
        if (!this.aggregatedValues.containsKey(key)) {
            this.aggregatedValues.put(key, new ArrayList<Float>());
        }
        this.aggregatedValues.get(key).add(value);
    }

}
