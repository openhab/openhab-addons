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
 * The {@link FerroampMqttCommunication} is responsible for communication with the Ferroamp-systems Mqtt-broker.
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
        if ("extapi/data/ehub".equals(topic)) {
            processIncomingJsonMessageEhub(topic, new String(payload, StandardCharsets.UTF_8));
        }
        if ("extapi/data/sso".equals(topic)) {
            processIncomingJsonMessageSso(topic, new String(payload, StandardCharsets.UTF_8));
        }
        if ("extapi/data/eso".equals(topic)) {
            processIncomingJsonMessageEso(topic, new String(payload, StandardCharsets.UTF_8));
        }
        if ("extapi/data/esm".equals(topic)) {
            processIncomingJsonMessageEsm(topic, new String(payload, StandardCharsets.UTF_8));
        }
    }

    // Prepare actual Json-topic Ehub-message and update values for channels
    void processIncomingJsonMessageEhub(String topic, String messageJsonEhub) {
        String[] ehubChannelPostsValue = new String[86]; // Array for EHUB (Energy Hub) Posts

        JsonObject jsonElementsObject = new Gson().fromJson(new Gson().fromJson(messageJsonEhub, JsonObject.class),
                JsonObject.class);

        Objects.requireNonNull(jsonElementsObject, "JsonObject jsonElementsObject cannot be null");

        String jsonElementsStringTemp = "";
        Gson gson = new Gson();

        // gridfreq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(0)).toString();
        GetGeneralValues gridfreq = new GetGeneralValues();
        gridfreq = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (gridfreq != null) {
            ehubChannelPostsValue[0] = gridfreq.getVal();
        }

        // iace
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(1)).toString();
        GetGeneralLx iace = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (iace != null) {
            ehubChannelPostsValue[1] = iace.getL1();
            ehubChannelPostsValue[2] = iace.getL2();
            ehubChannelPostsValue[3] = iace.getL3();
        }

        // ul
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(2)).toString();
        GetGeneralLx ul = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (ul != null) {
            ehubChannelPostsValue[4] = ul.getL1();
            ehubChannelPostsValue[5] = ul.getL2();
            ehubChannelPostsValue[6] = ul.getL3();
        }

        // il
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(3)).toString();
        GetGeneralLx il = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (il != null) {
            ehubChannelPostsValue[7] = il.getL1();
            ehubChannelPostsValue[8] = il.getL2();
            ehubChannelPostsValue[9] = il.getL3();
        }

        // ild
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(4)).toString();
        GetGeneralLx ild = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (ild != null) {
            ehubChannelPostsValue[10] = ild.getL1();
            ehubChannelPostsValue[11] = ild.getL2();
            ehubChannelPostsValue[12] = ild.getL3();
        }

        // ilq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(5)).toString();
        GetGeneralLx ilq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (ilq != null) {

            ehubChannelPostsValue[13] = ilq.getL1();
            ehubChannelPostsValue[14] = ilq.getL2();
            ehubChannelPostsValue[15] = ilq.getL3();
        }

        // iext
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(6)).toString();
        GetGeneralLx iext = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (iext != null) {
            ehubChannelPostsValue[16] = iext.getL1();
            ehubChannelPostsValue[17] = iext.getL2();
            ehubChannelPostsValue[18] = iext.getL3();
        }

        // iextd
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(7)).toString();
        GetGeneralLx iextd = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (iextd != null) {
            ehubChannelPostsValue[19] = iextd.getL1();
            ehubChannelPostsValue[20] = iextd.getL2();
            ehubChannelPostsValue[21] = iextd.getL3();
        }

        // iextq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(8)).toString();
        GetGeneralLx iextq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (iextq != null) {
            ehubChannelPostsValue[22] = iextq.getL1();
            ehubChannelPostsValue[23] = iextq.getL2();
            ehubChannelPostsValue[24] = iextq.getL3();
        }

        // iloadd
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(9)).toString();
        GetGeneralLx iloadd = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (iloadd != null) {
            ehubChannelPostsValue[25] = iloadd.getL1();
            ehubChannelPostsValue[26] = iloadd.getL2();
            ehubChannelPostsValue[27] = iloadd.getL3();
        }

        // iloadq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(10)).toString();
        GetGeneralLx iloadq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (iloadq != null) {
            ehubChannelPostsValue[28] = iloadq.getL1();
            ehubChannelPostsValue[29] = iloadq.getL2();
            ehubChannelPostsValue[30] = iloadq.getL3();
        }

        // sext
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(11)).toString();
        GetGeneralValues sext = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (sext != null) {
            ehubChannelPostsValue[31] = sext.getVal();
        }

        // pext
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(12)).toString();
        GetGeneralLx pext = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (pext != null) {
            ehubChannelPostsValue[32] = pext.getL1();
            ehubChannelPostsValue[33] = pext.getL2();
            ehubChannelPostsValue[34] = pext.getL3();
        }

        // pextreactive
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(13)).toString();
        GetGeneralLx pextreactive = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (pextreactive != null) {
            ehubChannelPostsValue[35] = pextreactive.getL1();
            ehubChannelPostsValue[36] = pextreactive.getL2();
            ehubChannelPostsValue[37] = pextreactive.getL3();
        }

        // pinv
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(14)).toString();
        GetGeneralLx pinv = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (pinv != null) {
            ehubChannelPostsValue[38] = pinv.getL1();
            ehubChannelPostsValue[39] = pinv.getL2();
            ehubChannelPostsValue[40] = pinv.getL3();
        }

        // pinvreactive
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(15)).toString();
        GetGeneralLx pinvreactive = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (pinvreactive != null) {
            ehubChannelPostsValue[41] = pinvreactive.getL1();
            ehubChannelPostsValue[42] = pinvreactive.getL2();
            ehubChannelPostsValue[43] = pinvreactive.getL3();
        }

        // pload
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(16)).toString();
        GetGeneralLx pload = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (pload != null) {
            ehubChannelPostsValue[44] = pload.getL1();
            ehubChannelPostsValue[45] = pload.getL2();
            ehubChannelPostsValue[46] = pload.getL3();
        }

        // ploadreactive
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(17)).toString();
        GetGeneralLx ploadreactive = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (ploadreactive != null) {
            ehubChannelPostsValue[47] = ploadreactive.getL1();
            ehubChannelPostsValue[48] = ploadreactive.getL2();
            ehubChannelPostsValue[49] = ploadreactive.getL3();
        }

        // ppv
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(18)).toString();
        GetGeneralValues ppv = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(19)).toString();
        GetUdc udc = gson.fromJson(jsonElementsStringTemp, GetUdc.class);
        if (ppv != null) {
            ehubChannelPostsValue[50] = ppv.getVal();
        }
        if (udc != null) {
            ehubChannelPostsValue[51] = udc.getPos();
            ehubChannelPostsValue[52] = udc.getNeg();
        }

        // wextprodq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(20)).toString();
        GetGeneralLx wextprodq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (wextprodq != null) {
            ehubChannelPostsValue[53] = mJTokWh(jsonStripEhub(wextprodq.getL1()));
            ehubChannelPostsValue[54] = mJTokWh(jsonStripEhub(wextprodq.getL2()));
            ehubChannelPostsValue[55] = mJTokWh(jsonStripEhub(wextprodq.getL3()));
        }

        // wextconsq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(21)).toString();
        GetGeneralLx wextconsq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (wextconsq != null) {
            ehubChannelPostsValue[56] = mJTokWh(jsonStripEhub(wextconsq.getL1()));
            ehubChannelPostsValue[57] = mJTokWh(jsonStripEhub(wextconsq.getL2()));
            ehubChannelPostsValue[58] = mJTokWh(jsonStripEhub(wextconsq.getL3()));
        }

        // winvprodq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(22)).toString();
        GetGeneralLx winvprodq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (winvprodq != null) {
            ehubChannelPostsValue[59] = mJTokWh(jsonStripEhub(winvprodq.getL1()));
            ehubChannelPostsValue[60] = mJTokWh(jsonStripEhub(winvprodq.getL2()));
            ehubChannelPostsValue[61] = mJTokWh(jsonStripEhub(winvprodq.getL3()));
        }

        // winvconsq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(23)).toString();
        GetGeneralLx winvconsq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (winvconsq != null) {
            ehubChannelPostsValue[62] = mJTokWh(jsonStripEhub(winvconsq.getL1()));
            ehubChannelPostsValue[63] = mJTokWh(jsonStripEhub(winvconsq.getL2()));
            ehubChannelPostsValue[64] = mJTokWh(jsonStripEhub(winvconsq.getL3()));
        }

        // wloadprodq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(24)).toString();
        GetGeneralLx wloadprodq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (wloadprodq != null) {
            ehubChannelPostsValue[65] = mJTokWh(jsonStripEhub(wloadprodq.getL1()));
            ehubChannelPostsValue[66] = mJTokWh(jsonStripEhub(wloadprodq.getL2()));
            ehubChannelPostsValue[67] = mJTokWh(jsonStripEhub(wloadprodq.getL3()));
        }

        // wloadconsq
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(25)).toString();
        GetGeneralLx wloadconsq = gson.fromJson(jsonElementsStringTemp, GetGeneralLx.class);
        if (wloadconsq != null) {
            ehubChannelPostsValue[68] = mJTokWh(jsonStripEhub(wloadconsq.getL1()));
            ehubChannelPostsValue[69] = mJTokWh(jsonStripEhub(wloadconsq.getL2()));
            ehubChannelPostsValue[70] = mJTokWh(jsonStripEhub(wloadconsq.getL3()));
        }

        // wextprodq_3p
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(26)).toString();
        GetGeneralValues wextprodq3p = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (wextprodq3p != null) {
            ehubChannelPostsValue[71] = mJTokWh(jsonStripOneLiners(wextprodq3p.getVal()));
        }

        // wextconsq_3p
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(27)).toString();
        GetGeneralValues wextconsq3p = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (wextconsq3p != null) {
            ehubChannelPostsValue[72] = mJTokWh(jsonStripOneLiners(wextconsq3p.getVal()));
        }

        // winvprodq_3p
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(28)).toString();
        GetGeneralValues winvprodq3p = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (winvprodq3p != null) {
            ehubChannelPostsValue[73] = mJTokWh(jsonStripOneLiners(winvprodq3p.getVal()));
        }

        // winvconsq_3p
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(29)).toString();
        GetGeneralValues winvconsq3p = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (winvconsq3p != null) {
            ehubChannelPostsValue[74] = mJTokWh(jsonStripOneLiners(winvconsq3p.getVal()));
        }

        // wloadprodq_3p
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(30)).toString();
        GetGeneralValues wloadprodq3p = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (wloadprodq3p != null) {
            ehubChannelPostsValue[75] = mJTokWh(jsonStripOneLiners(wloadprodq3p.getVal()));
        }

        // wloadconsq_3p
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(31)).toString();
        GetGeneralValues wloadconsq3p = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (wloadconsq3p != null) {
            ehubChannelPostsValue[76] = mJTokWh(jsonStripOneLiners(wloadconsq3p.getVal()));
        }

        // wpv
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(32)).toString();
        GetGeneralValues wpv = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (wpv != null) {
            ehubChannelPostsValue[77] = mJTokWh(jsonStripOneLiners(wpv.getVal()));
        }

        // state
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(33)).toString();
        GetGeneralValues state = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (state != null) {
            ehubChannelPostsValue[78] = jsonStripOneLiners(state.getVal());
        }

        // ts
        jsonElementsStringTemp = jsonElementsObject.get(EhubJsonElements.getJsonElementsEhub().get(34)).toString();
        GetGeneralValues ts = gson.fromJson(jsonElementsStringTemp, GetGeneralValues.class);
        if (ts != null) {
            ehubChannelPostsValue[79] = ts.getVal();
        }
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
        Gson gson = new Gson();

        JsonObject jsonElementsObjectSsoS1 = new Gson().fromJson(new Gson().fromJson(messageJsonSso, JsonObject.class),
                JsonObject.class);

        Objects.requireNonNull(jsonElementsObjectSsoS1, "JsonObject jsonElementsObjectSsoS1 cannot be null");

        jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(0)).toString();

        GetGeneralValues idS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
        GetGeneralValues idSso = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);

        if (idSso != null) {
            if (isSsoChecked == false) {
                if (ssoS1IdCheck.isEmpty() && ssoS2IdCheck.isEmpty() && ssoS3IdCheck.isEmpty()
                        && ssoS4IdCheck.isEmpty()) {
                    ssoS1IdCheck = idSso.getVal();
                } else {
                    if (!ssoS1IdCheck.isEmpty() && ssoS2IdCheck.isEmpty() && ssoS3IdCheck.isEmpty()
                            && ssoS4IdCheck.isEmpty()) {
                        ssoS2IdCheck = idSso.getVal();
                    } else {
                        if (!ssoS1IdCheck.isEmpty() && !ssoS2IdCheck.isEmpty() && ssoS3IdCheck.isEmpty()
                                && ssoS4IdCheck.isEmpty()) {
                            ssoS3IdCheck = idSso.getVal();
                        } else {
                            if (!ssoS1IdCheck.isEmpty() && !ssoS2IdCheck.isEmpty() && !ssoS3IdCheck.isEmpty()
                                    && ssoS4IdCheck.isEmpty()) {
                                ssoS4IdCheck = idSso.getVal();
                                isSsoChecked = true;
                            }
                        }
                    }
                }
            }
        }

        if (idS1 != null) {
            if (idS1.getVal().equals(ssoS1IdCheck)) {
                // id
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(0))
                        .toString();
                idS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (idS1 != null) {
                    ssoS1ChannelPostsValue[0] = idS1.getVal();
                }

                // upv
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(1))
                        .toString();
                GetGeneralValues upvS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (upvS1 != null) {
                    ssoS1ChannelPostsValue[1] = upvS1.getVal();
                }

                // ipv
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(2))
                        .toString();
                GetGeneralValues ipvS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (ipvS1 != null) {
                    ssoS1ChannelPostsValue[2] = ipvS1.getVal();
                }

                // wpv
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(3))
                        .toString();
                GetGeneralValues wpvS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (wpvS1 != null) {
                    ssoS1ChannelPostsValue[3] = mJTokWh(jsonStripOneLiners(wpvS1.getVal()));
                }

                // relaystatus
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(4))
                        .toString();
                GetGeneralValues relaystatusS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (relaystatusS1 != null) {
                    ssoS1ChannelPostsValue[4] = relaystatusS1.getVal();
                }

                // temp
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(5))
                        .toString();
                GetGeneralValues tempS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (tempS1 != null) {
                    ssoS1ChannelPostsValue[5] = tempS1.getVal();
                }

                // faultcode
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(6))
                        .toString();
                GetGeneralValues faultcodeS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (faultcodeS1 != null) {
                    ssoS1ChannelPostsValue[6] = faultcodeS1.getVal();
                }

                // udc
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(7))
                        .toString();
                GetGeneralValues udcS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (udcS1 != null) {
                    ssoS1ChannelPostsValue[7] = udcS1.getVal();
                }

                // ts
                jsonElementsStringTempS1 = jsonElementsObjectSsoS1.get(SsoJsonElements.getJsonElementsSso().get(8))
                        .toString();
                GetGeneralValues tsS1 = gson.fromJson(jsonElementsStringTempS1, GetGeneralValues.class);
                if (tsS1 != null) {
                    ssoS1ChannelPostsValue[8] = tsS1.getVal();
                }
                ssoS1ChannelsUpdateValues = ssoS1ChannelPostsValue;
            }
        }

        JsonObject jsonElementsObjectSsoS2 = new Gson().fromJson(new Gson().fromJson(messageJsonSso, JsonObject.class),
                JsonObject.class);

        Objects.requireNonNull(jsonElementsObjectSsoS2, "JsonObject jsonElementsObjectSsoS2 cannot be null");

        jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(0)).toString();
        GetGeneralValues idS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);

        if (idS2 != null) {
            if (idS2.getVal().equals(ssoS2IdCheck)) {
                // id
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(0))
                        .toString();
                idS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (idS2 != null) {
                    ssoS2ChannelPostsValue[0] = idS2.getVal();
                }

                // upv
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(1))
                        .toString();
                GetGeneralValues upvS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (upvS2 != null) {
                    ssoS2ChannelPostsValue[1] = upvS2.getVal();
                }

                // ipv
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(2))
                        .toString();
                GetGeneralValues ipvS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (ipvS2 != null) {
                    ssoS2ChannelPostsValue[2] = ipvS2.getVal();
                }

                // wpv
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(3))
                        .toString();
                GetGeneralValues wpvS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (wpvS2 != null) {
                    ssoS2ChannelPostsValue[3] = mJTokWh(jsonStripOneLiners(wpvS2.getVal()));
                }

                // relaystatus
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(4))
                        .toString();
                GetGeneralValues relaystatusS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (relaystatusS2 != null) {
                    ssoS2ChannelPostsValue[4] = relaystatusS2.getVal();
                }

                // temp
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(5))
                        .toString();
                GetGeneralValues tempS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (tempS2 != null) {
                    ssoS2ChannelPostsValue[5] = tempS2.getVal();
                }

                // faultcode
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(6))
                        .toString();
                GetGeneralValues faultcodeS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (faultcodeS2 != null) {
                    ssoS2ChannelPostsValue[6] = faultcodeS2.getVal();
                }

                // udc
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(7))
                        .toString();
                GetGeneralValues udcS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (udcS2 != null) {
                    ssoS2ChannelPostsValue[7] = udcS2.getVal();
                }

                // ts
                jsonElementsStringTempS2 = jsonElementsObjectSsoS2.get(SsoJsonElements.getJsonElementsSso().get(8))
                        .toString();
                GetGeneralValues tsS2 = gson.fromJson(jsonElementsStringTempS2, GetGeneralValues.class);
                if (tsS2 != null) {
                    ssoS2ChannelPostsValue[8] = tsS2.getVal();
                }
                ssoS2ChannelsUpdateValues = ssoS2ChannelPostsValue;
            }
        }

        JsonObject jsonElementsObjectSsoS3 = new Gson().fromJson(new Gson().fromJson(messageJsonSso, JsonObject.class),
                JsonObject.class);

        Objects.requireNonNull(jsonElementsObjectSsoS3, "JsonObject jsonElementsObjectSsoS3 cannot be null");

        jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(0)).toString();
        GetGeneralValues idS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);

        if (idS3 != null) {
            if (idS3.getVal().equals(ssoS3IdCheck)) {
                // id
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(0))
                        .toString();
                idS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (idS3 != null) {
                    ssoS3ChannelPostsValue[0] = idS3.getVal();
                }

                // upv
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(1))
                        .toString();
                GetGeneralValues upvS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (upvS3 != null) {
                    ssoS3ChannelPostsValue[1] = upvS3.getVal();
                }

                // ipv
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(2))
                        .toString();
                GetGeneralValues ipvS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (ipvS3 != null) {
                    ssoS3ChannelPostsValue[2] = ipvS3.getVal();
                }

                // wpv
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(3))
                        .toString();
                GetGeneralValues wpvS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (wpvS3 != null) {
                    ssoS3ChannelPostsValue[3] = mJTokWh(jsonStripOneLiners(wpvS3.getVal()));
                }

                // relaystatus
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(4))
                        .toString();
                GetGeneralValues relaystatusS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (relaystatusS3 != null) {
                    ssoS3ChannelPostsValue[4] = relaystatusS3.getVal();
                }

                // temp
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(5))
                        .toString();
                GetGeneralValues tempS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (tempS3 != null) {
                    ssoS3ChannelPostsValue[5] = tempS3.getVal();
                }

                // faultcode
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(6))
                        .toString();
                GetGeneralValues faultcodeS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (faultcodeS3 != null) {
                    ssoS3ChannelPostsValue[6] = faultcodeS3.getVal();
                }

                // udc
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(7))
                        .toString();
                GetGeneralValues udcS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (udcS3 != null) {
                    ssoS3ChannelPostsValue[7] = udcS3.getVal();
                }

                // ts
                jsonElementsStringTempS3 = jsonElementsObjectSsoS3.get(SsoJsonElements.getJsonElementsSso().get(8))
                        .toString();
                GetGeneralValues tsS3 = gson.fromJson(jsonElementsStringTempS3, GetGeneralValues.class);
                if (tsS3 != null) {
                    ssoS3ChannelPostsValue[8] = tsS3.getVal();
                }
                ssoS3ChannelsUpdateValues = ssoS3ChannelPostsValue;
            }
        }

        JsonObject jsonElementsObjectSsoS4 = new Gson().fromJson(new Gson().fromJson(messageJsonSso, JsonObject.class),
                JsonObject.class);

        Objects.requireNonNull(jsonElementsObjectSsoS4, "JsonObject jsonElementsObjectSsoS4 cannot be null");

        jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(0)).toString();
        GetGeneralValues idS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);

        if (idS4 != null) {
            if (idS4.getVal().equals(ssoS4IdCheck)) {
                // id
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(0))
                        .toString();
                idS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (idS4 != null) {
                    ssoS4ChannelPostsValue[0] = idS4.getVal();
                }

                // upv
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(1))
                        .toString();
                GetGeneralValues upvS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (upvS4 != null) {
                    ssoS4ChannelPostsValue[1] = upvS4.getVal();
                }

                // ipv
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(2))
                        .toString();
                GetGeneralValues ipvS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (ipvS4 != null) {
                    ssoS4ChannelPostsValue[2] = ipvS4.getVal();
                }

                // wpv
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(3))
                        .toString();
                GetGeneralValues wpvS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (wpvS4 != null) {
                    ssoS4ChannelPostsValue[3] = mJTokWh(jsonStripOneLiners(wpvS4.getVal()));
                }

                // relaystatus
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(4))
                        .toString();
                GetGeneralValues relaystatusS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (relaystatusS4 != null) {
                    ssoS4ChannelPostsValue[4] = relaystatusS4.getVal();
                }

                // temp
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(5))
                        .toString();
                GetGeneralValues tempS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (tempS4 != null) {
                    ssoS4ChannelPostsValue[5] = tempS4.getVal();
                }

                // faultcode
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(6))
                        .toString();
                GetGeneralValues faultcodeS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (faultcodeS4 != null) {
                    ssoS4ChannelPostsValue[6] = faultcodeS4.getVal();
                }

                // udc
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(7))
                        .toString();
                GetGeneralValues udcS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (udcS4 != null) {
                    ssoS4ChannelPostsValue[7] = udcS4.getVal();
                }

                // ts
                jsonElementsStringTempS4 = jsonElementsObjectSsoS4.get(SsoJsonElements.getJsonElementsSso().get(8))
                        .toString();
                GetGeneralValues tsS4 = gson.fromJson(jsonElementsStringTempS4, GetGeneralValues.class);
                if (tsS4 != null) {
                    ssoS4ChannelPostsValue[8] = tsS4.getVal();
                }
                ssoS4ChannelsUpdateValues = ssoS4ChannelPostsValue;
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
        Gson gson = new Gson();

        if (!jsonElementsObjectEso.isEmpty()) {
            // id
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(0))
                    .toString();
            GetGeneralValues idEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (idEso != null) {
                esoChannelPostsValue[0] = idEso.getVal();
            }

            // ubat
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(1))
                    .toString();
            GetGeneralValues ubatEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (ubatEso != null) {
                esoChannelPostsValue[1] = ubatEso.getVal();
            }

            // ibat
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(2))
                    .toString();
            GetGeneralValues ibatEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (ibatEso != null) {
                esoChannelPostsValue[2] = ibatEso.getVal();
            }

            // wbatprod
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(3))
                    .toString();
            GetGeneralValues wbatprodEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (wbatprodEso != null) {
                esoChannelPostsValue[3] = mJTokWh(jsonStripOneLiners(wbatprodEso.getVal()));
            }

            // wbatcons
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(4))
                    .toString();
            GetGeneralValues wbatconsEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (wbatconsEso != null) {
                esoChannelPostsValue[4] = mJTokWh(jsonStripOneLiners(wbatconsEso.getVal()));
            }

            // soc
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(5))
                    .toString();
            GetGeneralValues socEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (socEso != null) {
                esoChannelPostsValue[5] = socEso.getVal();
            }

            // relaystatus
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(6))
                    .toString();
            GetGeneralValues relaystatusEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (relaystatusEso != null) {
                esoChannelPostsValue[6] = relaystatusEso.getVal();
            }

            // temp
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(7))
                    .toString();
            GetGeneralValues tempEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (tempEso != null) {
                esoChannelPostsValue[7] = tempEso.getVal();
            }

            // faultcode
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(8))
                    .toString();
            GetGeneralValues faultcodeEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (faultcodeEso != null) {
                esoChannelPostsValue[8] = faultcodeEso.getVal();
            }

            // udc
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(9))
                    .toString();
            GetGeneralValues udcEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (udcEso != null) {
                esoChannelPostsValue[9] = udcEso.getVal();
            }

            // ts
            jsonElementsStringTempEso = jsonElementsObjectEso.get(EsoJsonElements.getJsonElementsEso().get(10))
                    .toString();
            GetGeneralValues tsEso = gson.fromJson(jsonElementsStringTempEso, GetGeneralValues.class);
            if (tsEso != null) {
                esoChannelPostsValue[10] = tsEso.getVal();
            }

            esoChannelsUpdateValues = esoChannelPostsValue;
        } else {
            return;
        }
    }

    // Prepare actual Json-topic Esm-message and update values for channels
    void processIncomingJsonMessageEsm(String topic, String messageJsonEsm) {
        String[] esmChannelPostsValue = new String[7]; // Array for ESM, Energy Storage Module ) Posts
        JsonObject jsonElementsObjectEsm = new Gson().fromJson(new Gson().fromJson(messageJsonEsm, JsonObject.class),
                JsonObject.class);

        Objects.requireNonNull(jsonElementsObjectEsm, "JsonObject jsonElementsObjectEsm cannot be null");

        String jsonElementsStringTempEsm = "";
        Gson gson = new Gson();

        if (!jsonElementsObjectEsm.isEmpty()) {
            // id
            jsonElementsStringTempEsm = jsonElementsObjectEsm.get(EsmJsonElements.getJsonElementsEsm().get(0))
                    .toString();
            GetGeneralValues idEsm = gson.fromJson(jsonElementsStringTempEsm, GetGeneralValues.class);
            if (idEsm != null) {
                esmChannelPostsValue[0] = idEsm.getVal();
            }

            // soh
            jsonElementsStringTempEsm = jsonElementsObjectEsm.get(EsmJsonElements.getJsonElementsEsm().get(1))
                    .toString();
            GetGeneralValues sohEsm = gson.fromJson(jsonElementsStringTempEsm, GetGeneralValues.class);
            if (sohEsm != null) {
                esmChannelPostsValue[1] = sohEsm.getVal();
            }

            // soc
            jsonElementsStringTempEsm = jsonElementsObjectEsm.get(EsmJsonElements.getJsonElementsEsm().get(2))
                    .toString();
            GetGeneralValues socEsm = gson.fromJson(jsonElementsStringTempEsm, GetGeneralValues.class);
            if (socEsm != null) {
                esmChannelPostsValue[2] = socEsm.getVal();
            }

            // ratedcapacity
            jsonElementsStringTempEsm = jsonElementsObjectEsm.get(EsmJsonElements.getJsonElementsEsm().get(3))
                    .toString();
            GetGeneralValues ratedcapacityEsm = gson.fromJson(jsonElementsStringTempEsm, GetGeneralValues.class);
            if (ratedcapacityEsm != null) {
                esmChannelPostsValue[3] = ratedcapacityEsm.getVal();
            }

            // ratedpower
            jsonElementsStringTempEsm = jsonElementsObjectEsm.get(EsmJsonElements.getJsonElementsEsm().get(4))
                    .toString();
            GetGeneralValues ratedpowerEsm = gson.fromJson(jsonElementsStringTempEsm, GetGeneralValues.class);
            if (ratedpowerEsm != null) {
                esmChannelPostsValue[4] = ratedpowerEsm.getVal();
            }

            // status
            jsonElementsStringTempEsm = jsonElementsObjectEsm.get(EsmJsonElements.getJsonElementsEsm().get(5))
                    .toString();
            GetGeneralValues statusEsm = gson.fromJson(jsonElementsStringTempEsm, GetGeneralValues.class);
            if (statusEsm != null) {
                esmChannelPostsValue[5] = statusEsm.getVal();
            }

            // ts
            jsonElementsStringTempEsm = jsonElementsObjectEsm.get(EsmJsonElements.getJsonElementsEsm().get(6))
                    .toString();
            GetGeneralValues tsEsm = gson.fromJson(jsonElementsStringTempEsm, GetGeneralValues.class);
            if (tsEsm != null) {
                esmChannelPostsValue[6] = tsEsm.getVal();
            }

            esmChannelsUpdateValues = esmChannelPostsValue;
        } else {
            return;
        }
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
