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
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link FerroampMqttCommunication} is responsible for communication with the Ferroamp-systems Mqtt-broker.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

public class FerroampMqttCommunication implements MqttMessageSubscriber {

    private @Nullable FerroampConfiguration ferroampConfig;
    private @Nullable static MqttBrokerConnection ferroampConnection;

    static String[] ehubChannelsUpdateValues;
    static String[] ssoS0ChannelsUpdateValues;
    static String[] ssoS1ChannelsUpdateValues;
    static String[] ssoS2ChannelsUpdateValues;
    static String[] ssoS3ChannelsUpdateValues;
    static String[] esoChannelsUpdateValues;
    static String[] esmChannelsUpdateValues;

    static boolean isEsoAvailable = false;
    static boolean isEsmAvailable = false;

    private final static Logger logger = LoggerFactory.getLogger(FerroampMqttCommunication.class);

    public FerroampMqttCommunication(Thing thing) {
        super();
    }

    // Handles request topic
    @SuppressWarnings("null")
    void sendMQTT(String payload) {
        MqttBrokerConnection localConfigurationConnection = ferroampConnection;
        if (FerroampHandler.getFerroampConnection() == null) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                logger.debug("Failed during waiting for connection to setup");
            }
        }

        localConfigurationConnection.start();
        localConfigurationConnection.setCredentials(ferroampConfig.userName, ferroampConfig.password);

        if (localConfigurationConnection != null) {
            localConfigurationConnection.publish(FerroampBindingConstants.REQUEST_TOPIC, payload.getBytes(), 1, false);
        }
    }

    // Handles respective topic type
    @SuppressWarnings("null")
    void getMQTT(String topic, FerroampConfiguration ferroampConfig) throws InterruptedException {
        MqttBrokerConnection localSubscribeConnection = FerroampHandler.getFerroampConnection();
        localSubscribeConnection.start();
        localSubscribeConnection.setCredentials(ferroampConfig.userName, ferroampConfig.password);

        if ("ehubTopic".equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.EHUB_TOPIC, this);
        }
        if ("ssoTopic".equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.SSO_TOPIC, this);
        }
        if ("esoTopic".equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.ESO_TOPIC, this);
        }
        if ("esmTopic".equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.ESM_TOPIC, this);
        }
    }

    // Capture actual Json-topic message
    @Override
    public void processMessage(String topic, byte[] payload) {
        processIncomingJsonMessage(topic, new String(payload, StandardCharsets.UTF_8));
    }

    // Prepare actual Json-topic message and update channels
    void processIncomingJsonMessage(String topic, String messageJson) {
        if ("extapi/data/ehub".equals(topic)) {
            String[] ehubChannelPostsValue = new String[86]; // Array for EHUB (Energy Hub) Posts
            JsonObject jsonElementsObject = new Gson().fromJson(new Gson().fromJson(messageJson, JsonObject.class),
                    JsonObject.class);
            Gson gsonElementsObject = new Gson();
            String jsonElementsStringTemp = "";

            int jsonElementsEhubCounter = 0;
            int hasBattery = 0;

            if (FerroampHandler.gethasBattery()) {
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

                    case 35:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[80] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 36:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[81] = mJTokWh(jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString()));
                        jsonElementsEhubCounter++;
                        break;

                    case 37:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[82] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 38:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[83] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 39:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[84] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;

                    case 40:
                        jsonElementsStringTemp = jsonElementsObject
                                .get(EhubJsonElements.getJsonElementsEhub().get(jsonElementsEhubCounter)).toString();
                        ehubChannelPostsValue[85] = jsonStripOneLiners(
                                gsonElementsObject.fromJson(jsonElementsStringTemp, JsonObject.class).toString());
                        jsonElementsEhubCounter++;
                        break;
                }
            }
            ehubChannelsUpdateValues = ehubChannelPostsValue;
        }

        if ("extapi/data/sso".equals(topic)) {
            String[] ssoS0ChannelPostsValue = new String[9]; // Array for SSOS0 ( Solar String Optimizer ) Posts
            String[] ssoS1ChannelPostsValue = new String[9]; // Array for SSOS1 ( Solar String Optimizer ) Posts
            String[] ssoS2ChannelPostsValue = new String[9]; // Array for SSOS2 ( Solar String Optimizer ) Posts
            String[] ssoS3ChannelPostsValue = new String[9]; // Array for SSOS3 ( Solar String Optimizer ) Posts
            JsonObject ssoS0ChannelObject = new Gson().fromJson(messageJson, JsonObject.class);
            JsonObject ssoS1ChannelObject = new Gson().fromJson(messageJson, JsonObject.class);
            JsonObject ssoS2ChannelObject = new Gson().fromJson(messageJson, JsonObject.class);
            JsonObject ssoS3ChannelObject = new Gson().fromJson(messageJson, JsonObject.class);

            int channelValuesCounterSsoS0 = 0;
            for (FerroampChannelConfiguration cConfig : FerroampHandler.getchannelConfigSsoS0()) {
                ssoS0ChannelPostsValue[channelValuesCounterSsoS0] = jsonStripOneLiners(ssoS0ChannelObject
                        .get(SsoParameters.getChannelParametersSso().get(channelValuesCounterSsoS0)).toString());
                String ssoS0Channel = cConfig.id;
                if ("ssoWPvS0".equals(ssoS0Channel)) {
                    ssoS0ChannelPostsValue[channelValuesCounterSsoS0] = mJTokWh(
                            ssoS0ChannelPostsValue[channelValuesCounterSsoS0]);
                }
                channelValuesCounterSsoS0++;
            }
            ssoS0ChannelsUpdateValues = ssoS0ChannelPostsValue;

            int channelValuesCounterSsoS1 = 0;
            for (FerroampChannelConfiguration cConfig : FerroampHandler.getchannelConfigSsoS1()) {
                ssoS1ChannelPostsValue[channelValuesCounterSsoS1] = jsonStripOneLiners(ssoS1ChannelObject
                        .get(SsoParameters.getChannelParametersSso().get(channelValuesCounterSsoS1)).toString());
                String ssoS1Channel = cConfig.id;
                if ("ssoWPvS1".equals(ssoS1Channel)) {
                    ssoS1ChannelPostsValue[channelValuesCounterSsoS1] = mJTokWh(
                            ssoS1ChannelPostsValue[channelValuesCounterSsoS1]);
                }
                channelValuesCounterSsoS1++;
            }
            ssoS1ChannelsUpdateValues = ssoS1ChannelPostsValue;

            int channelValuesCounterSsoS2 = 0;
            for (FerroampChannelConfiguration cConfig : FerroampHandler.getchannelConfigSsoS2()) {
                ssoS2ChannelPostsValue[channelValuesCounterSsoS2] = jsonStripOneLiners(ssoS2ChannelObject
                        .get(SsoParameters.getChannelParametersSso().get(channelValuesCounterSsoS2)).toString());
                String ssoS2Channel = cConfig.id;
                if ("ssoWPvS2".equals(ssoS2Channel)) {
                    ssoS2ChannelPostsValue[channelValuesCounterSsoS2] = mJTokWh(
                            ssoS2ChannelPostsValue[channelValuesCounterSsoS2]);
                }
                channelValuesCounterSsoS2++;
            }
            ssoS2ChannelsUpdateValues = ssoS2ChannelPostsValue;

            int channelValuesCounterSsoS3 = 0;
            for (FerroampChannelConfiguration cConfig : FerroampHandler.getchannelConfigSsoS3()) {
                ssoS3ChannelPostsValue[channelValuesCounterSsoS3] = jsonStripOneLiners(ssoS3ChannelObject
                        .get(SsoParameters.getChannelParametersSso().get(channelValuesCounterSsoS3)).toString());
                String ssoS3Channel = cConfig.id;
                if ("ssoWPvS3".equals(ssoS3Channel)) {
                    ssoS3ChannelPostsValue[channelValuesCounterSsoS3] = mJTokWh(
                            ssoS3ChannelPostsValue[channelValuesCounterSsoS3]);
                }
                channelValuesCounterSsoS3++;
            }
            ssoS3ChannelsUpdateValues = ssoS3ChannelPostsValue;
        }

        if ("extapi/data/eso".equals(topic)) {
            isEsoAvailable = true;
            String[] esoChannelPostsValue = new String[11];
            JsonObject esoChannelObject = new Gson().fromJson(messageJson, JsonObject.class);
            int channelValuesCounterEso = 0;
            for (FerroampChannelConfiguration cConfig : FerroampHandler.getchannelConfigEso()) {
                esoChannelPostsValue[channelValuesCounterEso] = jsonStripOneLiners(esoChannelObject
                        .get(EsoParameters.getChannelParametersEso().get(channelValuesCounterEso)).toString());
                String esoChannel = cConfig.id;
                if ("esoWBatProd".equals(esoChannel)) {
                    esoChannelPostsValue[channelValuesCounterEso] = mJTokWh(
                            esoChannelPostsValue[channelValuesCounterEso]);
                }
                channelValuesCounterEso++;
            }
        }

        if ("extapi/data/esm".equals(topic)) {
            isEsmAvailable = true;
            String[] esmChannelPostsValue = new String[7];
            JsonObject esmChannelObject = new Gson().fromJson(messageJson, JsonObject.class);

            int channelValuesCounterEsm = 0;
            for (FerroampChannelConfiguration cConfig : FerroampHandler.getchannelConfigEsm()) {
                esmChannelPostsValue[channelValuesCounterEsm] = jsonStripOneLiners(esmChannelObject
                        .get(EsmParameters.getChannelParametersEsm().get(channelValuesCounterEsm)).toString());
                channelValuesCounterEsm++;
            }
        }
    }

    public String jsonStripEhub(String jsonStringEhub) {
        return jsonStringEhub.replaceAll("\"", "");
    }

    public String jsonStripOneLiners(String jsonStringOneLiners) {
        return jsonStringOneLiners.replace("{", "").replace("\"", "").replace("val", "").replace(":", "").replace("}",
                "");
    }

    public String mJTokWh(String actualmJ) {
        Double actualkWhD = (Double.parseDouble(actualmJ) / 3600000000.0);
        return actualkWhD.toString();
    }

    public @Nullable static String[] getEhubChannelUpdateValues() {
        try {
            return ehubChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of Ehub channel values");
        }
        return ehubChannelsUpdateValues;
    }

    public @Nullable static String[] getSsoS0ChannelUpdateValues() {
        try {
            return ssoS0ChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of SsoS0 channel values");
        }
        return ssoS0ChannelsUpdateValues;
    }

    public @Nullable static String[] getSsoS1ChannelUpdateValues() {
        try {
            return ssoS1ChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of SsoS1 channel values");
        }
        return ssoS1ChannelsUpdateValues;
    }

    public @Nullable static String[] getSsoS2ChannelUpdateValues() {
        try {
            return ssoS2ChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of SsoS2 channel values");
        }
        return ssoS2ChannelsUpdateValues;
    }

    public @Nullable static String[] getSsoS3ChannelUpdateValues() {
        try {
            return ssoS3ChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of SsoS3 channel values");
        }
        return ssoS3ChannelsUpdateValues;
    }

    public static boolean getIsEsoAvailable() {
        try {
            return isEsoAvailable;
        } catch (Exception e) {
            logger.debug("Failed at check of available Eso");
        }
        return isEsoAvailable;
    }

    public @Nullable static String[] getEsoChannelUpdateValues() {
        try {
            return esoChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of Eso channel values");
        }
        return esoChannelsUpdateValues;
    }

    public static boolean getIsEsmAvailable() {
        try {
            return isEsmAvailable;
        } catch (Exception e) {
            logger.debug("Failed at check of available Esm");
        }
        return isEsmAvailable;
    }

    public @Nullable static String[] getEsmChannelUpdateValues() {
        try {
            return esmChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of Esm channel values");
        }
        return esmChannelsUpdateValues;
    }
}
