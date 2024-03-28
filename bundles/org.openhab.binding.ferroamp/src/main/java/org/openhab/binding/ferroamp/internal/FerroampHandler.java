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
package org.openhab.binding.ferroamp.internal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link FerroampHandler} is responsible for handling of values sent to and from the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */
@NonNullByDefault
public class FerroampHandler extends BaseThingHandler implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(FerroampHandler.class);

    private @Nullable FerroampConfiguration ferroampConfig;
    private @Nullable MqttBrokerConnection ferroampConnection;

    private List<FerroampChannelConfiguration> channelConfigEhub = new ArrayList<>();
    private List<FerroampChannelConfiguration> channelConfigSsoS0 = new ArrayList<>();
    private List<FerroampChannelConfiguration> channelConfigSsoS1 = new ArrayList<>();
    private List<FerroampChannelConfiguration> channelConfigSsoS2 = new ArrayList<>();
    private List<FerroampChannelConfiguration> channelConfigSsoS3 = new ArrayList<>();
    private List<FerroampChannelConfiguration> channelConfigEso = new ArrayList<>();
    private List<FerroampChannelConfiguration> channelConfigEsm = new ArrayList<>();

    int ssoIdSet = 0;
    boolean establishedConnection = false;

    public FerroampHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String transId = UUID.randomUUID().toString();
        String valueConfiguration = command.toString();

        if (FerroampBindingConstants.CHANNEL_REQUESTCHARGE.equals(channelUID.getId())) {
            String requestCmdJsonCharge = "{\"" + "transId" + "\":\"" + transId
                    + "\",\"cmd\":{\"name\":\"charge\",\"arg\":\"" + valueConfiguration + "\"}}";
            sendMQTT(requestCmdJsonCharge);
        }
        if (FerroampBindingConstants.CHANNEL_REQUESTDISCHARGE.equals(channelUID.getId())) {
            String requestCmdJsonDisCharge = "{\"" + "transId" + "\":\"" + transId
                    + "\",\"cmd\":{\"name\":\"discharge\",\"arg\":\"" + valueConfiguration + "\"}}";
            sendMQTT(requestCmdJsonDisCharge);
        }
        if (FerroampBindingConstants.CHANNEL_AUTO.equals(channelUID.getId())) {
            String requestCmdJsonAuto = "{\"" + "transId" + "\":\"" + transId + "\",\"cmd\":{\"name\":\"auto\"}}";
            sendMQTT(requestCmdJsonAuto);
        }
    }

    @Override
    public void initialize() {
        // Set channel configuration parameters
        channelConfigEhub = FerroampChannelConfiguration.getChannelConfigurationEhub();
        channelConfigSsoS0 = FerroampChannelConfiguration.getChannelConfigurationSsoS0();
        channelConfigSsoS1 = FerroampChannelConfiguration.getChannelConfigurationSsoS1();
        channelConfigSsoS2 = FerroampChannelConfiguration.getChannelConfigurationSsoS2();
        channelConfigSsoS3 = FerroampChannelConfiguration.getChannelConfigurationSsoS3();
        channelConfigEso = FerroampChannelConfiguration.getChannelConfigurationEso();
        channelConfigEsm = FerroampChannelConfiguration.getChannelConfigurationEsm();

        ferroampConfig = getConfigAs(FerroampConfiguration.class);
        @SuppressWarnings("null")
        final MqttBrokerConnection ferroampConnection = new MqttBrokerConnection(ferroampConfig.hostName,
                FerroampBindingConstants.BROKER_PORT, false, false, ferroampConfig.userName);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable = true;

            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                refresh();
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        this.ferroampConnection = ferroampConnection;
    }

    private void refresh() {
        getMQTT("ehubTopic");
        getMQTT("ssoTopic");
        getMQTT("esoTopic");
        getMQTT("esmTopic");
    }

    // Handles request topic
    @SuppressWarnings("null")
    private void sendMQTT(String payload) {
        MqttBrokerConnection localConfigurationConnection = ferroampConnection;
        localConfigurationConnection.start();
        localConfigurationConnection.setCredentials(ferroampConfig.userName, ferroampConfig.password);

        if (localConfigurationConnection != null) {
            localConfigurationConnection.publish(FerroampBindingConstants.REQUEST_TOPIC, payload.getBytes(), 1, false);
        }
    }

    // Handles respective topic type
    @SuppressWarnings("null")
    private void getMQTT(String topic) {
        MqttBrokerConnection localSubscribeConnection = ferroampConnection;
        localSubscribeConnection.start();
        localSubscribeConnection.setCredentials(ferroampConfig.userName, ferroampConfig.password);

        MqttConnectionState state = localSubscribeConnection.connectionState();

        if ("ehubTopic".equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.EHUB_TOPIC, this);
            if (state.toString().matches("CONNECTED") || establishedConnection) {
                establishedConnection = true;
            }
        }
        if ("ssoTopic".equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.SSO_TOPIC, this);
            if (state.toString().matches("CONNECTED") || establishedConnection) {
                establishedConnection = true;
            }
        }
        if ("esoTopic".equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.ESO_TOPIC, this);
            if (state.toString().matches("CONNECTED") || establishedConnection) {
                establishedConnection = true;
            }
        }
        if ("esmTopic".equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.ESM_TOPIC, this);
            if (state.toString().matches("CONNECTED") || establishedConnection) {
                establishedConnection = true;
            }
        }

        // Wait for broker to get ready
        try {
            Thread.sleep(10000);
        } catch (InterruptedException brokerWaitException) {
            logger.debug("Thread.sleep error '{}'", brokerWaitException.getMessage());
        }

        if (establishedConnection) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    // Capture actual Json-topic message
    @Override
    public void processMessage(String topic, byte[] payload) {
        processIncomingJsonMessage(topic, new String(payload, StandardCharsets.UTF_8));
    }

    // Prepare actual Json-topic message and update channels
    private void processIncomingJsonMessage(String topic, String messageJson) {
        if ("extapi/data/ehub".equals(topic)) {
            String[] ehubChannelPostsValue = new String[86]; // Array for EHUB (Energy Hub) Posts
            JsonObject jsonElementsObject = new Gson().fromJson(new Gson().fromJson(messageJson, JsonObject.class),
                    JsonObject.class);
            Gson gsonElementsObject = new Gson();
            String jsonElementsStringTemp = "";
            int jsonElementsEhubCounter = 0;
            int hasBattery = 0;
            if (ferroampConfig.hasBattery) {
                hasBattery = 40;
            } else {
                hasBattery = 35;
            }
            while (jsonElementsEhubCounter < hasBattery) {
                switch (jsonElementsEhubCounter) {
                    case 0:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[0] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString()));
                        ehubChannelPostsValue[1] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString()));
                        ehubChannelPostsValue[2] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 1:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[3] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[4] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[5] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 2:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[6] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 3:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[7] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString()));
                        ehubChannelPostsValue[8] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString()));
                        ehubChannelPostsValue[9] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 4:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[10] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 5:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[11] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString()));
                        ehubChannelPostsValue[12] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString()));
                        ehubChannelPostsValue[13] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 6:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[14] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[15] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[16] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 7:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[17] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[18] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[19] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 8:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[20] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 9:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[21] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[22] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[23] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 10:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[24] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[25] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[26] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 11:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[27] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[28] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[29] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 12:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[30] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 13:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[31] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[32] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[33] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 14:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[34] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 15:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[35] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString()));
                        ehubChannelPostsValue[36] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString()));
                        ehubChannelPostsValue[37] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 16:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[38] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 17:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[39] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[40] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[41] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 18:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[42] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[43] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[44] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 19:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[45] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[46] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[47] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 20:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[48] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString()));
                        ehubChannelPostsValue[49] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString()));
                        ehubChannelPostsValue[50] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 21:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[51] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 22:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[52] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[53] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[54] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 23:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[55] = gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("pos").toString()
                                .replace("\"", "");
                        ehubChannelPostsValue[56] = gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("neg").toString()
                                .replace("\"", "");
                        jsonElementsEhubCounter++;
                        break;

                    case 24:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[57] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 25:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[58] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[59] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[60] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 26:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[61] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 27:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[62] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[63] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[64] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 28:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[65] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 29:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[66] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 30:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[67] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[68] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[69] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 31:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[70] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 32:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[71] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[72] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[73] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 33:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[74] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString()));
                        ehubChannelPostsValue[75] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString()));
                        ehubChannelPostsValue[76] = mJTokWh(jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 34:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[77] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L1").toString());
                        ehubChannelPostsValue[78] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L2").toString());
                        ehubChannelPostsValue[79] = jsonStripEhub(gsonElementsObject
                                .fromJson(jsonElementsStringTemp, JsonObject.class).get("L3").toString());
                        jsonElementsEhubCounter++;
                        break;
                }

                if (ferroampConfig.hasBattery) {
                    switch (jsonElementsEhubCounter) {
                        case 35:
                            jsonElementsStringTemp = jsonElementsObject
                                    .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter))
                                    .toString();
                            ehubChannelPostsValue[80] = mJTokWh(jsonStripOneLiners(
                                    gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                            jsonElementsEhubCounter++;
                            break;

                        case 36:
                            jsonElementsStringTemp = jsonElementsObject
                                    .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter))
                                    .toString();
                            ehubChannelPostsValue[81] = mJTokWh(jsonStripOneLiners(
                                    gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                            jsonElementsEhubCounter++;
                            break;

                        case 37:
                            jsonElementsStringTemp = jsonElementsObject
                                    .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter))
                                    .toString();
                            ehubChannelPostsValue[82] = jsonStripOneLiners(
                                    gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                            jsonElementsEhubCounter++;
                            break;

                        case 38:
                            jsonElementsStringTemp = jsonElementsObject
                                    .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter))
                                    .toString();
                            ehubChannelPostsValue[83] = jsonStripOneLiners(
                                    gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                            jsonElementsEhubCounter++;
                            break;

                        case 39:
                            jsonElementsStringTemp = jsonElementsObject
                                    .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter))
                                    .toString();
                            ehubChannelPostsValue[84] = jsonStripOneLiners(
                                    gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                            jsonElementsEhubCounter++;
                            break;

                        case 40:
                            jsonElementsStringTemp = jsonElementsObject
                                    .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter))
                                    .toString();
                            ehubChannelPostsValue[85] = jsonStripOneLiners(
                                    gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                            jsonElementsEhubCounter++;
                            break;
                    }
                }
            }

            int channelValuesCounterEhub = 0;
            for (FerroampChannelConfiguration cConfig : channelConfigEhub) {
                String channel = cConfig.id;
                State state = StringType.valueOf(ehubChannelPostsValue[channelValuesCounterEhub]);
                updateState(channel, state);
                channelValuesCounterEhub++;
            }
        }

        if ("extapi/data/sso".equals(topic)) {
            String[] ssoChannelPostsValue = new String[9]; // Array for SSO ( Solar String Optimizer ) Posts
            JsonObject ssoChannelObject = new Gson().fromJson(messageJson, JsonObject.class);

            String ssoS0Id = "";
            String ssoS1Id = "";
            String ssoS2Id = "";
            String ssoS3Id = "";

            if (ssoIdSet == 0) {
                ssoS0Id = jsonStripOneLiners(ssoChannelObject.get("id").toString());
                ssoIdSet = 1;
            } else if (ssoIdSet == 1) {
                ssoS1Id = jsonStripOneLiners(ssoChannelObject.get("id").toString());
                ssoIdSet = 2;
            } else if (ssoIdSet == 2) {
                ssoS2Id = jsonStripOneLiners(ssoChannelObject.get("id").toString());
                ssoIdSet = 3;
            } else if (ssoIdSet == 3) {
                ssoS3Id = jsonStripOneLiners(ssoChannelObject.get("id").toString());
                ssoIdSet = 0;
            }
            String ssoId = jsonStripOneLiners(ssoChannelObject.get("id").toString());

            if (ssoS0Id.equals(ssoId)) {
                int channelValuesCounterSso = 0;
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS0) {
                    ssoChannelPostsValue[channelValuesCounterSso] = jsonStripOneLiners(ssoChannelObject
                            .get(SsoParameters.getChannelParametersSso().get(channelValuesCounterSso)).toString());
                    String channel = cConfig.id;
                    if ("ssoWPvS0".equals(channel)) {
                        ssoChannelPostsValue[channelValuesCounterSso] = mJTokWh(
                                ssoChannelPostsValue[channelValuesCounterSso]);
                    }
                    State state = StringType.valueOf(ssoChannelPostsValue[channelValuesCounterSso]);
                    updateState(channel, state);
                    channelValuesCounterSso++;
                }
            }

            if (ssoS1Id.equals(ssoId)) {
                int channelValuesCounterSso = 0;
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS1) {
                    ssoChannelPostsValue[channelValuesCounterSso] = jsonStripOneLiners(ssoChannelObject
                            .get(SsoParameters.getChannelParametersSso().get(channelValuesCounterSso)).toString());
                    String channel = cConfig.id;
                    if ("ssoWPvS1".equals(channel)) {
                        ssoChannelPostsValue[channelValuesCounterSso] = mJTokWh(
                                ssoChannelPostsValue[channelValuesCounterSso]);
                    }
                    State state = StringType.valueOf(ssoChannelPostsValue[channelValuesCounterSso]);
                    updateState(channel, state);
                    channelValuesCounterSso++;
                }
            }

            if (ssoS2Id.equals(ssoId)) {
                int channelValuesCounterSso = 0;
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS2) {
                    ssoChannelPostsValue[channelValuesCounterSso] = jsonStripOneLiners(ssoChannelObject
                            .get(SsoParameters.getChannelParametersSso().get(channelValuesCounterSso)).toString());
                    String channel = cConfig.id;
                    if ("ssoWPvS2".equals(channel)) {
                        ssoChannelPostsValue[channelValuesCounterSso] = mJTokWh(
                                ssoChannelPostsValue[channelValuesCounterSso]);
                    }
                    State state = StringType.valueOf(ssoChannelPostsValue[channelValuesCounterSso]);
                    updateState(channel, state);
                    channelValuesCounterSso++;
                }
            }

            if (ssoS3Id.equals(ssoId)) {
                int channelValuesCounterSso = 0;
                for (FerroampChannelConfiguration cConfig : channelConfigSsoS3) {
                    ssoChannelPostsValue[channelValuesCounterSso] = jsonStripOneLiners(ssoChannelObject
                            .get(SsoParameters.getChannelParametersSso().get(channelValuesCounterSso)).toString());
                    String channel = cConfig.id;
                    if ("ssoWPvS3".equals(channel)) {
                        ssoChannelPostsValue[channelValuesCounterSso] = mJTokWh(
                                ssoChannelPostsValue[channelValuesCounterSso]);
                    }
                    State state = StringType.valueOf(ssoChannelPostsValue[channelValuesCounterSso]);
                    updateState(channel, state);
                    channelValuesCounterSso++;
                }
            }
        }
        if ("extapi/data/eso".equals(topic)) {
            String[] esoChannelPostsValue = new String[11]; // Array for ESO ( DC/DC ) Posts
            JsonObject esoChannelObject = new Gson().fromJson(messageJson, JsonObject.class);

            int channelValuesCounterEso = 0;
            for (FerroampChannelConfiguration cConfig : channelConfigEso) {
                esoChannelPostsValue[channelValuesCounterEso] = jsonStripOneLiners(esoChannelObject
                        .get(EsoParameters.getChannelParametersEso().get(channelValuesCounterEso)).toString());
                String channel = cConfig.id;
                if ("esoWBatCons".equals(channel) || "esoWBatProd".equals(channel)) {
                    esoChannelPostsValue[channelValuesCounterEso] = mJTokWh(
                            esoChannelPostsValue[channelValuesCounterEso]);
                }
                State state = StringType.valueOf(esoChannelPostsValue[channelValuesCounterEso]);
                updateState(channel, state);
                channelValuesCounterEso++;
            }
        }

        if ("extapi/data/esm".equals(topic)) {
            String[] esmChannelPostsValue = new String[7]; // Array for ESM ( Energy Storage Module ) Posts
            JsonObject esmChannelObject = new Gson().fromJson(messageJson, JsonObject.class);
            int channelValuesCounterEsm = 0;
            for (FerroampChannelConfiguration cConfig : channelConfigEsm) {
                esmChannelPostsValue[channelValuesCounterEsm] = jsonStripOneLiners(esmChannelObject
                        .get(EsmParameters.getChannelParametersEsm().get(channelValuesCounterEsm)).toString());
                String channel = cConfig.id;
                State state = StringType.valueOf(esmChannelPostsValue[channelValuesCounterEsm]);
                updateState(channel, state);
                channelValuesCounterEsm++;
            }
        }
    }

    public String jsonStripEhub(String jsonStringEhub) {
        String jsonStringStrippedEhub = jsonStringEhub.replaceAll("\"", "");
        return jsonStringStrippedEhub;
    }

    public String jsonStripOneLiners(String jsonStringOneLiners) {
        String jsonStringStrippedOneLiners = jsonStringOneLiners.replace("{", "").replace("\"", "").replace("val", "")
                .replace(":", "").replace("}", "");
        return jsonStringStrippedOneLiners;
    }

    public String mJTokWh(String actualmJ) {
        Double actualkWhD = (Double.parseDouble(actualmJ) / 3600000000.0);
        String actualkWh = actualkWhD.toString();
        return actualkWh;
    }
}
