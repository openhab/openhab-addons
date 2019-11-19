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
package org.openhab.binding.magentatv.internal;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MagentaTVGson} class implements The MR returns event information every time the program changes. This
 * information is mapped to various Thing channels and also used to catch the power down event for MR400 (there is no
 * way to query power status). This class provides the mapping between event JSON and Java class using Gson.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class MagentaTVGson {
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
    @NonNullByDefault
    public class MREventGson {
        // The following classes are used to map the JSON data into objects using GSon.
        @NonNullByDefault
        public class MRProgramInfoEvent {
            public String type = "";
            @SerializedName("instance_id")
            public Integer instanceId = 0;
            @SerializedName("channel_code")
            public String channelCode = "";
            @SerializedName("channel_num")
            public String channelNum = "";
            public String mediaId = "";
            public ArrayList<MRProgramStatus> program_info = new ArrayList<>();
        }

        @NonNullByDefault
        public class MRProgramStatus {
            @SerializedName("start_time")
            public String startTime = "";
            @SerializedName("event_id")
            public String eventId = "";
            public String duration = "";
            @SerializedName("free_CA_mode")
            public Boolean freeCAMmode = false;
            public Integer running_status = EV_EITCHG_RUNNING_NONE;
            @SerializedName("short_event")
            public ArrayList<MRShortProgramInfo> shortEvent = new ArrayList<>();
        }

        @NonNullByDefault
        public class MRShortProgramInfo {
            @SerializedName("event_name")
            public String eventName = "";
            @SerializedName("language_code")
            public String languageCode = "";
            @SerializedName("text_char")
            public String textChar = "";
        }

        /**
         * playStatus event format (JSON) playContent event, for details see
         * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619231.html
         * sample 1: {"new_play_mode":4,
         * "duration":0,"playBackState":1,"mediaType":1,"mediaCode":"3733","playPostion":0}
         * sample 2: {"new_play_mode":4, "playBackState":1,"mediaType":1,"mediaCode":"3479"}
         */
        @NonNullByDefault
        public class MRPayEvent {
            @SerializedName("new_play_mode")
            public Integer newPlayMode = EV_PLAYCHG_STOP;
            public Integer duration = -1;
            public Integer playBackState = EV_PLAYCHG_STOP;
            public Integer mediaType = 0;
            public String mediaCode = "";
            public Integer playPostion = -1;
        }
    }

    /**
     * Deutsche Telekom uses a OAuth-based authentication to access the EPG portal.
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
    @NonNullByDefault
    public class MROAuthGson {

        @NonNullByDefault
        public class OauthCredentials {
            @Nullable
            public String epghttpsurl = "";
            @Nullable
            public ArrayList<OauthKeyValue> sam3Para = new ArrayList<>();

            /*
             * currently unused by the binding
             *
             * public String enctytoken;
             * public String encryptiontype;
             * public String platformcode;
             * public String epgurl;
             * public String version;
             * public String rootCerAddr;
             * public String upgAddr4IPTV;
             * public String upgAddr4OTT;
             */
        }

        @NonNullByDefault
        public class OauthKeyValue {
            public String key = "";
            public String value = "";
        }

        @NonNullByDefault
        public class OAuthTokenResponse {
            @SerializedName("error_description")
            public String errorDescription = "";
            public String error = "";
            @SerializedName("access_token")
            public String accessToken = "";
        }

        @NonNullByDefault
        public class OAuthAutenhicateResponse {
            public String retcode = "";
            public String desc = "";
            public String epgurl = "";
            public String userID = "";
        }
    }
}
