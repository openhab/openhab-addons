/**
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
package org.openhab.binding.ferroamp.internal;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ferroamp.dto.GetGeneralLx;
import org.openhab.binding.ferroamp.dto.GetGeneralValues;
import org.openhab.binding.ferroamp.dto.GetUdc;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link FerroampMqttCommunication} is responsible for communication with Ferroamp-system's Mqtt-broker.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class FerroampMqttCommunication implements MqttMessageSubscriber {
    static String[] ehubChannelsUpdateValues = new String[0];
    static String[] ssoS1ChannelsUpdateValues = new String[0];
    static String[] ssoS2ChannelsUpdateValues = new String[0];
    static String[] ssoS3ChannelsUpdateValues = new String[0];
    static String[] ssoS4ChannelsUpdateValues = new String[0];
    static String[] esoChannelsUpdateValues = new String[0];
    static String[] esmChannelsUpdateValues = new String[0];

    static boolean isSsoChecked = false;
    static String ssoS1IdCheck = "";
    static String ssoS2IdCheck = "";
    static String ssoS3IdCheck = "";
    static String ssoS4IdCheck = "";

    private final static Logger logger = LoggerFactory.getLogger(FerroampMqttCommunication.class);

    public FerroampMqttCommunication(Thing thing) {
        super();
    }

    // Handles request topic
    static void sendPublishedTopic(String payload, FerroampConfiguration ferroampConfig) {
        MqttBrokerConnection localConfigurationConnection = FerroampHandler.getFerroampConnection();
        Objects.requireNonNull(localConfigurationConnection,
                "MqttBrokerConnection localConfigurationConnection cannot be null");
        localConfigurationConnection.start();
        localConfigurationConnection.setCredentials(ferroampConfig.userName, ferroampConfig.password);
        localConfigurationConnection.publish(FerroampBindingConstants.REQUEST_TOPIC, payload.getBytes(), 1, false);
    }

    // Handles respective topic type
    void getSubscribedTopic(String topic, FerroampConfiguration ferroampConfig) {
        MqttBrokerConnection localSubscribeConnection = FerroampHandler.getFerroampConnection();
        Objects.requireNonNull(localSubscribeConnection,
                "MqttBrokerConnection localSubscribeConnection cannot be null");
        if (FerroampBindingConstants.EHUB_TOPIC.equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.EHUB_TOPIC, this);
        }
        if (FerroampBindingConstants.SSO_TOPIC.equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.SSO_TOPIC, this);
        }
        if (FerroampBindingConstants.ESO_TOPIC.equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.ESO_TOPIC, this);
        }
        if (FerroampBindingConstants.ESM_TOPIC.equals(topic)) {
            localSubscribeConnection.subscribe(FerroampBindingConstants.ESM_TOPIC, this);
        }
    }

    // Capture actual Json-topic message
    @Override
    public void processMessage(String topic, byte[] payload) {
        if (FerroampBindingConstants.EHUB_TOPIC.equals(topic)) {
            processIncomingJsonMessageEhub(topic, new String(payload, StandardCharsets.UTF_8));
        }
        if (FerroampBindingConstants.SSO_TOPIC.equals(topic)) {
            processIncomingJsonMessageSso(topic, new String(payload, StandardCharsets.UTF_8));
        }
        if (FerroampBindingConstants.ESO_TOPIC.equals(topic)) {
            processIncomingJsonMessageEso(topic, new String(payload, StandardCharsets.UTF_8));
        }
        if (FerroampBindingConstants.ESM_TOPIC.equals(topic)) {
            processIncomingJsonMessageEsm(topic, new String(payload, StandardCharsets.UTF_8));
        }
    }

    // Prepare actual Json-topic Ehub-message and update values for channels
    void processIncomingJsonMessageEhub(String topic, String messageJsonEhub) {
        String[] ehubChannelPostsValue = new String[86]; // Array for EHUB (Energy Hub) Posts
        JsonObject jsonElementsObject = new Gson().fromJson(messageJsonEhub, JsonObject.class);
        Objects.requireNonNull(jsonElementsObject, "JsonObject jsonElementsObject cannot be null");

        // gridfreq
        ehubChannelPostsValue[0] = GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(0)).toString());

        // iace
        ehubChannelPostsValue[1] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(1)).toString(), 1);
        ehubChannelPostsValue[2] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(1)).toString(), 2);
        ehubChannelPostsValue[3] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(1)).toString(), 3);

        // ul
        ehubChannelPostsValue[4] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(2)).toString(), 1);
        ehubChannelPostsValue[5] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(2)).toString(), 2);
        ehubChannelPostsValue[6] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(2)).toString(), 3);

        // il
        ehubChannelPostsValue[7] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(3)).toString(), 1);
        ehubChannelPostsValue[8] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(3)).toString(), 2);
        ehubChannelPostsValue[9] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(3)).toString(), 3);

        // ild
        ehubChannelPostsValue[10] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(4)).toString(), 1);
        ehubChannelPostsValue[11] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(4)).toString(), 2);
        ehubChannelPostsValue[12] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(4)).toString(), 3);

        // ilq
        ehubChannelPostsValue[13] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(5)).toString(), 1);
        ehubChannelPostsValue[14] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(5)).toString(), 2);
        ehubChannelPostsValue[15] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(5)).toString(), 3);

        // iext
        ehubChannelPostsValue[16] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(6)).toString(), 1);
        ehubChannelPostsValue[17] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(6)).toString(), 2);
        ehubChannelPostsValue[18] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(6)).toString(), 3);

        // iextd
        ehubChannelPostsValue[19] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(7)).toString(), 1);
        ehubChannelPostsValue[20] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(7)).toString(), 2);
        ehubChannelPostsValue[21] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(7)).toString(), 3);

        // iextq
        ehubChannelPostsValue[22] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(8)).toString(), 1);
        ehubChannelPostsValue[23] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(8)).toString(), 2);
        ehubChannelPostsValue[24] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(8)).toString(), 3);

        // iloadd
        ehubChannelPostsValue[25] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(9)).toString(), 1);
        ehubChannelPostsValue[26] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(9)).toString(), 2);
        ehubChannelPostsValue[27] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(9)).toString(), 3);

        // iloadq
        ehubChannelPostsValue[28] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(10)).toString(), 1);
        ehubChannelPostsValue[29] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(10)).toString(), 2);
        ehubChannelPostsValue[30] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(10)).toString(), 3);

        // sext
        ehubChannelPostsValue[31] = GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(11)).toString());

        // pext
        ehubChannelPostsValue[32] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(12)).toString(), 1);
        ehubChannelPostsValue[33] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(12)).toString(), 2);
        ehubChannelPostsValue[34] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(12)).toString(), 3);

        // pextreactive
        ehubChannelPostsValue[35] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(13)).toString(), 1);
        ehubChannelPostsValue[36] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(13)).toString(), 2);
        ehubChannelPostsValue[37] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(13)).toString(), 3);

        // pinv
        ehubChannelPostsValue[38] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(14)).toString(), 1);
        ehubChannelPostsValue[39] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(14)).toString(), 2);
        ehubChannelPostsValue[40] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(14)).toString(), 3);

        // pinvreactive
        ehubChannelPostsValue[41] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(15)).toString(), 1);
        ehubChannelPostsValue[42] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(15)).toString(), 2);
        ehubChannelPostsValue[43] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(15)).toString(), 3);

        // pload
        ehubChannelPostsValue[44] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(16)).toString(), 1);
        ehubChannelPostsValue[45] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(16)).toString(), 2);
        ehubChannelPostsValue[46] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(16)).toString(), 3);

        // ploadreactive
        ehubChannelPostsValue[47] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(17)).toString(), 1);
        ehubChannelPostsValue[48] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(17)).toString(), 2);
        ehubChannelPostsValue[49] = GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(17)).toString(), 3);

        // ppv
        ehubChannelPostsValue[50] = GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(18)).toString());

        // udc
        GetUdc udc = new Gson().fromJson(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(19)).toString(), GetUdc.class);
        if (udc != null) {
            ehubChannelPostsValue[51] = udc.getPos();
            ehubChannelPostsValue[52] = udc.getNeg();
        }

        // wextprodq
        ehubChannelPostsValue[53] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(20)).toString(), 1)));
        ehubChannelPostsValue[54] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(20)).toString(), 2)));
        ehubChannelPostsValue[55] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(20)).toString(), 3)));

        // wextconsq
        ehubChannelPostsValue[56] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(21)).toString(), 1)));
        ehubChannelPostsValue[57] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(21)).toString(), 2)));
        ehubChannelPostsValue[58] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(21)).toString(), 3)));

        // winvprodq
        ehubChannelPostsValue[59] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(22)).toString(), 1)));
        ehubChannelPostsValue[60] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(22)).toString(), 2)));
        ehubChannelPostsValue[61] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(22)).toString(), 3)));

        // winvconsq
        ehubChannelPostsValue[62] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(23)).toString(), 1)));
        ehubChannelPostsValue[63] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(23)).toString(), 2)));
        ehubChannelPostsValue[64] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(23)).toString(), 3)));

        // wloadprodq
        ehubChannelPostsValue[65] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(24)).toString(), 1)));
        ehubChannelPostsValue[66] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(24)).toString(), 2)));
        ehubChannelPostsValue[67] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(24)).toString(), 3)));

        // wloadconsq
        ehubChannelPostsValue[68] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(25)).toString(), 1)));
        ehubChannelPostsValue[69] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(25)).toString(), 2)));
        ehubChannelPostsValue[70] = mJTokWh(jsonStripEhub(GetGeneralLxHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(25)).toString(), 3)));

        // wextprodq_3p
        ehubChannelPostsValue[71] = mJTokWh(jsonStripOneLiners(GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(26)).toString())));

        // wextconsq_3p
        ehubChannelPostsValue[72] = mJTokWh(jsonStripOneLiners(GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(27)).toString())));

        // winvprodq_3p
        ehubChannelPostsValue[73] = mJTokWh(jsonStripOneLiners(GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(28)).toString())));

        // winvconsq_3p
        ehubChannelPostsValue[74] = mJTokWh(jsonStripOneLiners(GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(29)).toString())));

        // wloadprodq_3p
        ehubChannelPostsValue[75] = mJTokWh(jsonStripOneLiners(GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(30)).toString())));

        // wloadconsq_3p
        ehubChannelPostsValue[76] = mJTokWh(jsonStripOneLiners(GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(31)).toString())));

        // wpv
        ehubChannelPostsValue[77] = mJTokWh(jsonStripOneLiners(GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(32)).toString())));

        // state
        ehubChannelPostsValue[78] = jsonStripOneLiners(GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(33)).toString()));

        // ts
        ehubChannelPostsValue[79] = GetGeneralValueHelperEhub(
                jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(34)).toString());

        ehubChannelsUpdateValues = ehubChannelPostsValue;
    }

    // Prepare actual Json-topic Sso-messages and update values for channels
    void processIncomingJsonMessageSso(String topic, String messageJsonSso) {
        String[] ssoS1ChannelPostsValue = new String[9]; // Array for SSOS1 ( Solar String Optimizer ) Posts
        String[] ssoS2ChannelPostsValue = new String[9]; // Array for SSOS2 ( Solar String Optimizer ) Posts
        String[] ssoS3ChannelPostsValue = new String[9]; // Array for SSOS3 ( Solar String Optimizer ) Posts
        String[] ssoS4ChannelPostsValue = new String[9]; // Array for SSOS4 ( Solar String Optimizer ) Posts
        String jsonElementsStringTempS1 = "";
        String jsonElementsStringTempS2 = "";
        String jsonElementsStringTempS3 = "";
        String jsonElementsStringTempS4 = "";

        // Sort out the respective message for the different Sso's
        JsonObject jsonElementsObjectSsoS1 = new Gson().fromJson(messageJsonSso, JsonObject.class);
        Objects.requireNonNull(jsonElementsObjectSsoS1, "JsonObject jsonElementsObjectSsoS1 cannot be null");
        jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(0)).toString();
        GetGeneralValues idS1 = new Gson().fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
        GetGeneralValues idSso = new Gson().fromJson(jsonElementsStringTempS1, GetGeneralValues.class);

        if (idSso != null) {
            if (isSsoChecked == false) {
                int i = 0;
                while (i <= 3) {
                    if (ssoS1IdCheck.isBlank() && ssoS2IdCheck.isBlank() && ssoS3IdCheck.isBlank()
                            && ssoS4IdCheck.isBlank()) {
                        ssoS1IdCheck = idSso.getVal();
                        break;
                    }
                    if (!ssoS1IdCheck.isBlank() && ssoS2IdCheck.isBlank() && ssoS3IdCheck.isBlank()
                            && ssoS4IdCheck.isBlank()) {
                        if (idSso.getVal().equals(ssoS1IdCheck)) {
                            break;
                        }
                        ssoS2IdCheck = idSso.getVal();
                    }
                    if (!ssoS1IdCheck.isBlank() && !ssoS2IdCheck.isBlank() && ssoS3IdCheck.isBlank()
                            && ssoS4IdCheck.isBlank()) {
                        if (idSso.getVal().equals(ssoS1IdCheck) || idSso.getVal().equals(ssoS2IdCheck)) {
                            break;
                        }
                        ssoS3IdCheck = idSso.getVal();
                    }
                    if (!ssoS1IdCheck.isBlank() && !ssoS2IdCheck.isBlank() && !ssoS3IdCheck.isBlank()
                            && ssoS4IdCheck.isBlank()) {
                        if (idSso.getVal().equals(ssoS1IdCheck) || idSso.getVal().equals(ssoS2IdCheck)
                                || idSso.getVal().equals(ssoS3IdCheck)) {
                            break;
                        }
                        ssoS4IdCheck = idSso.getVal();
                    }
                    i++;
                }
            }

            // Fetch, check and update values for Sso1
            if (idS1 != null) {
                if (idS1.getVal().equals(ssoS1IdCheck)) {
                    int ssoS1Counter = 0;
                    while (ssoS1Counter <= 8) {
                        jsonElementsStringTempS1 = jsonElementsObjectSsoS1
                                .get(SsoJsonElements.getJsonElementsSso().get(ssoS1Counter)).toString();
                        ssoS1ChannelPostsValue[ssoS1Counter] = GetGeneralValueHelperSso(jsonElementsStringTempS1);
                        ssoS1Counter++;
                    }
                    ssoS1ChannelsUpdateValues = ssoS1ChannelPostsValue;
                }
            }

            JsonObject jsonElementsObjectSsoS2 = new Gson()
                    .fromJson(new Gson().fromJson(messageJsonSso, JsonObject.class), JsonObject.class);
            Objects.requireNonNull(jsonElementsObjectSsoS2, "JsonObject jsonElementsObjectSsoS2 cannot be null");
            jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(0))
                    .toString();
            GetGeneralValues idS2 = new Gson().fromJson(jsonElementsStringTempS2, GetGeneralValues.class);

            // Fetch, check and update values for Sso2
            if (idS2 != null) {
                if (idS2.getVal().equals(ssoS2IdCheck)) {
                    int ssoS2Counter = 0;
                    while (ssoS2Counter <= 8) {
                        jsonElementsStringTempS2 = jsonElementsObjectSsoS2
                                .get(SsoJsonElements.getJsonElementsSso().get(ssoS2Counter)).toString();
                        ssoS2ChannelPostsValue[ssoS2Counter] = GetGeneralValueHelperSso(jsonElementsStringTempS2);
                        ssoS2Counter++;
                    }
                    ssoS2ChannelsUpdateValues = ssoS2ChannelPostsValue;
                }
            }

            JsonObject jsonElementsObjectSsoS3 = new Gson()
                    .fromJson(new Gson().fromJson(messageJsonSso, JsonObject.class), JsonObject.class);
            Objects.requireNonNull(jsonElementsObjectSsoS3, "JsonObject jsonElementsObjectSsoS3 cannot be null");
            jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(0))
                    .toString();
            GetGeneralValues idS3 = new Gson().fromJson(jsonElementsStringTempS3, GetGeneralValues.class);

            // Fetch, check and update values for Sso3
            if (idS3 != null) {
                if (idS3.getVal().equals(ssoS3IdCheck)) {
                    int ssoS3Counter = 0;
                    while (ssoS3Counter <= 8) {
                        jsonElementsStringTempS3 = jsonElementsObjectSsoS3
                                .get(SsoJsonElements.getJsonElementsSso().get(ssoS3Counter)).toString();
                        ssoS3ChannelPostsValue[ssoS3Counter] = GetGeneralValueHelperSso(jsonElementsStringTempS3);
                        ssoS3Counter++;
                    }
                    ssoS3ChannelsUpdateValues = ssoS3ChannelPostsValue;
                }
            }

            JsonObject jsonElementsObjectSsoS4 = new Gson()
                    .fromJson(new Gson().fromJson(messageJsonSso, JsonObject.class), JsonObject.class);
            Objects.requireNonNull(jsonElementsObjectSsoS4, "JsonObject jsonElementsObjectSsoS4 cannot be null");
            jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(0))
                    .toString();
            GetGeneralValues idS4 = new Gson().fromJson(jsonElementsStringTempS4, GetGeneralValues.class);

            // Fetch, check and update values for Sso4
            if (idS4 != null) {
                if (idS4.getVal().equals(ssoS4IdCheck)) {
                    int ssoS4Counter = 0;
                    while (ssoS4Counter <= 8) {
                        jsonElementsStringTempS4 = jsonElementsObjectSsoS4
                                .get(SsoJsonElements.getJsonElementsSso().get(ssoS4Counter)).toString();
                        ssoS4ChannelPostsValue[ssoS4Counter] = GetGeneralValueHelperSso(jsonElementsStringTempS4);
                        ssoS4Counter++;
                    }
                    ssoS4ChannelsUpdateValues = ssoS4ChannelPostsValue;
                }
            }
        }
    }

    // Prepare actual Json-topic Eso-message and update values for channels
    void processIncomingJsonMessageEso(String topic, String messageJsonEso) {
        String[] esoChannelPostsValue = new String[11]; // Array for ESO, Energy Storage Optimizer ) Posts
        JsonObject jsonElementsObjectEso = new Gson().fromJson(new Gson().fromJson(messageJsonEso, JsonObject.class),
                JsonObject.class);
        Objects.requireNonNull(jsonElementsObjectEso, "JsonObject jsonElementsObjectEso cannot be null");
        String jsonElementsStringTempEso = "";

        if (!jsonElementsObjectEso.isEmpty()) {
            int esoCounter = 0;
            while (esoCounter <= 10) {
                jsonElementsStringTempEso = jsonElementsObjectEso
                        .get(EsoJsonElements.getJsonElementsEso().get(esoCounter)).toString();
                esoChannelPostsValue[esoCounter] = GetGeneralValueHelperEso(jsonElementsStringTempEso,
                        EsoJsonElements.getJsonElementsEso().get(esoCounter));
                esoCounter++;
            }
            esoChannelsUpdateValues = esoChannelPostsValue;
        } else {
            return;
        }
    }

    // Prepare actual Json-topic Esm-message and update values for channels
    void processIncomingJsonMessageEsm(String topic, String messageJsonEsm) {
        String[] esmChannelPostsValue = new String[7]; // Array for ESM, Energy Storage Module ) Posts
        JsonObject jsonElementsObjectEsm = new Gson().fromJson(messageJsonEsm, JsonObject.class);
        Objects.requireNonNull(jsonElementsObjectEsm, "JsonObject jsonElementsObjectEsm cannot be null");
        String jsonElementsStringTempEsm = "";

        if (!jsonElementsObjectEsm.isEmpty()) {
            int esmCounter = 0;
            while (esmCounter <= 6) {
                jsonElementsStringTempEsm = jsonElementsObjectEsm
                        .get(EsmJsonElements.getJsonElementsEsm().get(esmCounter)).toString();
                esmChannelPostsValue[esmCounter] = GetGeneralValueHelperEsm(jsonElementsStringTempEsm);
                esmCounter++;
            }
            esmChannelsUpdateValues = esmChannelPostsValue;
        } else {
            return;
        }
    }

    public String GetGeneralValueHelperEhub(String jsonElementsStringEhub) {
        String returnValueEhub = "";
        GetGeneralValues objectValueEhub = new Gson().fromJson(jsonElementsStringEhub, GetGeneralValues.class);
        if (objectValueEhub != null) {
            returnValueEhub = objectValueEhub.getVal().toString();
        }
        return returnValueEhub;
    }

    public String GetGeneralLxHelperEhub(String jsonElementsStringLxEhub, int Lx) {
        String returnLxEhub = "";
        GetGeneralLx objectLxEhub = new Gson().fromJson(jsonElementsStringLxEhub, GetGeneralLx.class);
        if (objectLxEhub != null) {
            if (Lx == 1) {
                returnLxEhub = objectLxEhub.getL1().toString();
            }
            if (Lx == 2) {
                returnLxEhub = objectLxEhub.getL2().toString();
            }
            if (Lx == 3) {
                returnLxEhub = objectLxEhub.getL3().toString();
            }
        }
        return returnLxEhub;
    }

    public String GetGeneralValueHelperSso(String jsonElementsStringSso) {
        String returnValueSso = "";
        GetGeneralValues objectSso = new Gson().fromJson(jsonElementsStringSso, GetGeneralValues.class);
        if (objectSso != null) {
            returnValueSso = objectSso.getVal().toString();
        }
        return returnValueSso;
    }

    public String GetGeneralValueHelperEso(String jsonElementsStringEso, String esoParameterName) {
        String returnValueEso = "";
        GetGeneralValues objectEso = new Gson().fromJson(jsonElementsStringEso, GetGeneralValues.class);
        if (objectEso != null) {
            if (esoParameterName.equals(EsoJsonElements.getJsonElementsEso().get(3))
                    || esoParameterName.equals(EsoJsonElements.getJsonElementsEso().get(4))) {
                returnValueEso = mJTokWh(jsonStripOneLiners(objectEso.getVal().toString()));
            } else {
                returnValueEso = objectEso.getVal().toString();
            }
        }
        return returnValueEso;
    }

    public String GetGeneralValueHelperEsm(String jsonElementsStringEsm) {
        String returnValueEsm = "";
        GetGeneralValues objectEsm = new Gson().fromJson(jsonElementsStringEsm, GetGeneralValues.class);
        if (objectEsm != null) {
            returnValueEsm = objectEsm.getVal().toString();
        }
        return returnValueEsm;
    }

    public @Nullable static String[] getEhubChannelUpdateValues() {
        try {
            return ehubChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of Ehub channel values");
        }
        return ehubChannelsUpdateValues;
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

    public @Nullable static String[] getSsoS4ChannelUpdateValues() {
        try {
            return ssoS4ChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of SsoS4 channel values");
        }
        return ssoS4ChannelsUpdateValues;
    }

    public @Nullable static String[] getEsoChannelUpdateValues() {
        try {
            return esoChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of Eso channel values");
        }
        return esoChannelsUpdateValues;
    }

    public @Nullable static String[] getEsmChannelUpdateValues() {
        try {
            return esmChannelsUpdateValues;
        } catch (Exception e) {
            logger.debug("Failed at update of Esm channel values");
        }
        return esmChannelsUpdateValues;
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
}
