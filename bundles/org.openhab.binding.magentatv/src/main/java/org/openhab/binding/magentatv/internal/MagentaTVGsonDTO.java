/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.magentatv.internal;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.InstanceCreator;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link MagentaTVGsonDTO} class implements The MR returns event information every time the program changes. This
 * information is mapped to various Thing channels and also used to catch the power down event for MR400 (there is no
 * way to query power status). This class provides the mapping between event JSON and Java class using Gson.
 *
 * @author Markus Michels - Initial contribution
 */
public class MagentaTVGsonDTO {
    /*
     * Program information event is send by the MR when a channel is changed.
     *
     * Sample data:
     * {"type":"EVENT_EIT_CHANGE","instance_id":26,"channel_code":"54","channel_num":"11","mediaId":"1221",
     * "program_info": [ {"start_time":"2018/10/14 10:21:59","event_id":"9581","duration":"00:26:47",
     * "free_CA_mode":false,"running_status":4, "short_event": [{"event_name":"Mysticons","language_code":"DEU",
     * "text_char":"Die Mysticons..." } ]},
     * {"start_time":"2018/10/14 10:48:46","event_id":"12204","duration":"00:23:54","free_CA_mode":false,
     * "running_status":1, "short_event": [ {"event_name":"Winx Club","language_code":"DEU", "text_char":"Daphnes Eltern
     * veranstalten...!" }]} ] }
     */
    // The following classes are used to map the JSON data into objects using GSon.
    public static class MRProgramInfoEvent {
        @SerializedName("type")
        public String type = "";
        @SerializedName("instance_id")
        public Integer instanceId = 0;
        @SerializedName("channel_code")
        public String channelCode = "";
        @SerializedName("channel_num")
        public String channelNum = "";
        @SerializedName("mediaId")
        public String mediaId = "";
        @SerializedName("program_info")
        public ArrayList<MRProgramStatus> programInfo = new ArrayList<>();
    }

    public static class MRProgramInfoEventInstanceCreator implements InstanceCreator<MRProgramInfoEvent> {
        @Override
        public MRProgramInfoEvent createInstance(@Nullable Type type) {
            return new MRProgramInfoEvent();
        }
    }

    public static class MRProgramStatus {
        @SerializedName("start_time")
        public String startTime = "";
        @SerializedName("event_id")
        public String eventId = "";
        @SerializedName("duration")
        public String duration = "";
        @SerializedName("free_CA_mode")
        public Boolean freeCAMmode = false;
        @SerializedName("running_status")
        public Integer runningStatus = EV_EITCHG_RUNNING_NONE;
        @SerializedName("short_event")
        public ArrayList<MRShortProgramInfo> shortEvent = new ArrayList<>();
    }

    public static class MRProgramStatusInstanceCreator implements InstanceCreator<MRProgramStatus> {
        @Override
        public MRProgramStatus createInstance(@Nullable Type type) {
            return new MRProgramStatus();
        }
    }

    public static class MRShortProgramInfo {
        @SerializedName("event_name")
        public String eventName = "";
        @SerializedName("language_code")
        public String languageCode = "";
        @SerializedName("text_char")
        public String textChar = "";
    }

    public static class MRShortProgramInfoInstanceCreator implements InstanceCreator<MRShortProgramInfo> {
        @Override
        public MRShortProgramInfo createInstance(@Nullable Type type) {
            return new MRShortProgramInfo();
        }
    }

    /**
     * playStatus event format (JSON) playContent event, for details see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619231.html
     *
     * sample 1: {"new_play_mode":4,"duration":0,"playBackState":1,"mediaType":1,"mediaCode":"3733","playPostion":0}
     * sample 2: {"new_play_mode":4, "playBackState":1,"mediaType":1,"mediaCode":"3479"}
     */
    public static class MRPayEvent {
        @SerializedName("new_play_mode")
        public Integer newPlayMode = EV_PLAYCHG_STOP;
        public Integer duration = -1;
        public Integer playBackState = EV_PLAYCHG_STOP;
        public Integer mediaType = 0;
        public String mediaCode = "";
        public Integer playPostion = -1;
    }

    public static class MRPayEventInstanceCreator implements InstanceCreator<MRPayEvent> {
        @Override
        public MRPayEvent createInstance(@Nullable Type type) {
            return new MRPayEvent();
        }
    }

    /**
     * Deutsche Telekom uses an OAuth-based authentication to access the EPG portal.
     * The binding automates the login incl. OAuth authentication. This class helps mapping the response to a Java
     * object (using Gson)
     *
     * Sample response:
     * { "enctytoken":"7FA9A6C05EDD873799392BBDDC5B7F34","encryptiontype":"0002",
     * "platformcode":"0200", "epgurl":"http://appepmfk20005.prod.sngtv.t-online.de:33200",
     * "version":"MEM V200R008C15B070", "epghttpsurl":"https://appepmfk20005.prod.sngtv.t-online.de:33207",
     * "rootCerAddr": "http://appepmfk20005.prod.sngtv.t-online.de:33200/EPG/CA/iptv_ca.der",
     * "upgAddr4IPTV":"https://slbedifk11100.prod.sngtv.t-online.de:33428/EDS/jsp/upgrade.jsp",
     * "upgAddr4OTT":"https://slbedmfk11100.prod.sngtv.t-online.de:33428/EDS/jsp/upgrade.jsp,https://slbedmfk11100.prod.sngtv.t-online.de:33428/EDS/jsp/upgrade.jsp",
     * "sam3Para": [
     * {"key":"SAM3ServiceURL","value":"https://accounts.login.idm.telekom.com"},
     * {"key":"OAuthClientSecret","value":"21EAB062-C4EE-489C-BC80-6A65397F3F96"},
     * {"key":"OAuthScope","value":"ngtvepg"},
     * {"key":"OAuthClientId","value":"10LIVESAM30000004901NGTV0000000000000000"} ]
     * }
     */
    public static class OauthCredentials {
        public String epghttpsurl = "";
        public ArrayList<OauthKeyValue> sam3Para = new ArrayList<OauthKeyValue>();
    }

    public static class OauthCredentialsInstanceCreator implements InstanceCreator<OauthCredentials> {
        @Override
        public OauthCredentials createInstance(@Nullable Type type) {
            return new OauthCredentials();
        }
    }

    public static class OauthKeyValue {
        public String key = "";
        public String value = "";
    }

    public static class OAuthTokenResponse {
        @SerializedName("error_description")
        public String errorDescription = "";
        public String error = "";
        @SerializedName("access_token")
        public String accessToken = "";
    }

    public static class OAuthTokenResponseInstanceCreator implements InstanceCreator<OAuthTokenResponse> {
        @Override
        public OAuthTokenResponse createInstance(@Nullable Type type) {
            return new OAuthTokenResponse();
        }
    }

    public static class OAuthAuthenticateResponse {
        public String retcode = "";
        public String desc = "";
        public String epgurl = "";
        public String userID = "";
    }

    public static class OAuthAuthenticateResponseInstanceCreator implements InstanceCreator<OAuthAuthenticateResponse> {
        @Override
        public OAuthAuthenticateResponse createInstance(@Nullable Type type) {
            return new OAuthAuthenticateResponse();
        }
    }
}
