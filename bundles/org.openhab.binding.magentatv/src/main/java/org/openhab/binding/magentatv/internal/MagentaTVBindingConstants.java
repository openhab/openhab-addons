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
package org.openhab.binding.magentatv.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MagentaTVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVBindingConstants {

    public static final String BINDING_ID = "magentatv";
    public static final String VENDOR = "Deutsche Telekom";
    public static final String OEM_VENDOR = "HUAWEI";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RECEIVER = new ThingTypeUID(BINDING_ID, "receiver");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_RECEIVER);

    /**
     * Property names for config/status properties
     */
    public static final String PROPERTY_UDN = "udn";
    public static final String PROPERTY_FRIENDLYNAME = "friendlyName";
    public static final String PROPERTY_MODEL_NUMBER = "modelRev";
    public static final String PROPERTY_HOST = "host";
    public static final String PROPERTY_IP = "ipAddress";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_DESC_URL = "descriptionUrl";
    public static final String PROPERTY_PAIRINGCODE = "pairingCode";
    public static final String PROPERTY_VERIFICATIONCODE = "verificationCode";
    public static final String PROPERTY_ACCT_NAME = "accountName";
    public static final String PROPERTY_ACCT_PWD = "accountPassword";
    public static final String PROPERTY_USERID = "userId";
    public static final String PROPERTY_LOCAL_IP = "localIP";
    public static final String PROPERTY_LOCAL_MAC = "localMAC";
    public static final String PROPERTY_TERMINALID = "terminalID";
    public static final String PROPERTY_WAKEONLAN = "wakeOnLAN";

    /**
     * Channel names
     */
    public static final String CHGROUP_CONTROL = "control";
    public static final String CHANNEL_POWER = CHGROUP_CONTROL + "#" + "power";
    public static final String CHANNEL_PLAYER = CHGROUP_CONTROL + "#" + "player";
    public static final String CHANNEL_MUTE = CHGROUP_CONTROL + "#" + "mute";
    public static final String CHANNEL_CHANNEL = CHGROUP_CONTROL + "#" + "channel";
    public static final String CHANNEL_KEY = CHGROUP_CONTROL + "#" + "key";

    public static final String CHGROUP_PROGRAM = "program";
    public static final String CHANNEL_PROG_TITLE = CHGROUP_PROGRAM + "#" + "title";
    public static final String CHANNEL_PROG_TEXT = CHGROUP_PROGRAM + "#" + "text";
    public static final String CHANNEL_PROG_START = CHGROUP_PROGRAM + "#" + "start";
    public static final String CHANNEL_PROG_DURATION = CHGROUP_PROGRAM + "#" + "duration";
    public static final String CHANNEL_PROG_POS = CHGROUP_PROGRAM + "#" + "position";

    public static final String CHGROUP_STATUS = "status";
    public static final String CHANNEL_CHANNEL_CODE = CHGROUP_STATUS + "#" + "channelCode";
    public static final String CHANNEL_RUN_STATUS = CHGROUP_STATUS + "#" + "runStatus";
    public static final String CHANNEL_PLAY_MODE = CHGROUP_STATUS + "#" + "playMode";

    /**
     * Definitions for the control interface
     */
    public static final String CONTENT_TYPE_XML = "text/xml; charset=UTF-8";

    public static final String PAIRING_NOTIFY_URI = "/magentatv/notify";
    public static final String NOTIFY_PAIRING_CODE = "X-pairingCheck:";

    public static final String MODEL_MR400 = "DMS_TPB"; // Old DSL receiver
    public static final String MODEL_MR401B = "MR401B"; // New DSL receiver
    public static final String MODEL_MR601 = "MR601"; // SAT receiver
    public static final String MODEL_MR201 = "MR201"; // sub receiver

    public static final String MR400_DEF_REMOTE_PORT = "49152";
    public static final String MR400_DEF_DESCRIPTION_URL = "/description.xml";
    public static final String MR401B_DEF_REMOTE_PORT = "8081";
    public static final String MR401B_DEF_DESCRIPTION_URL = "/xml/dial.xml";
    public static final String DEF_FRIENDLY_NAME = "PAD:openHAB";

    public static final int DEF_REFRESH_INTERVAL_SEC = 60;
    public static final int NETWORK_TIMEOUT_MS = 3000;

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_HOST = "HOST";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_LANGUAGE = "Accept-Language";
    public static final String HEADER_SOAPACTION = "SOAPACTION";
    public static final String HEADER_CONNECTION = "CONNECTION";
    public static final String HEADER_USER_AGENT = "USER_AGENT";
    public static final String USER_AGENT = "Darwin/16.5.0 UPnP/1.0 HUAWEI_iCOS/iCOS V1R1C00 DLNADOC/1.50";
    public static final String ACCEPT_TYPE = "*/*";

    /**
     * OAuth authentication for Deutsche Telekom MatengaTV portal
     */
    public static final String OAUTH_GET_CRED_URL = "https://slbedmfk11100.prod.sngtv.t-online.de";
    public static final String OAUTH_GET_CRED_PORT = "33428";
    public static final String OAUTH_GET_CRED_URI = "/EDS/JSON/Login?UserID=Guest";
    public static final String OAUTH_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_3 like Mac OS X) AppleWebKit/603.3.8 (KHTML, like Gecko) Mobile/14G60 (400962928)";

    //
    // MR events
    //
    public static final String MR_EVENT_EIT_CHANGE = "EVENT_EIT_CHANGE";

    public static final String MR_EVENT_CHAN_TAG = "\"channel_num\":";

    /**
     * program Info event data
     * EVENT_EIT_CHANGE: for a complete list see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619523.html
     */
    public static final int EV_EITCHG_RUNNING_NONE = 0;
    public static final int EV_EITCHG_RUNNING_NOT_RUNNING = 1;
    public static final int EV_EITCHG_RUNNING_STARTING = 2;
    public static final int EV_EITCHG_RUNNING_PAUSING = 3;
    public static final int EV_EITCHG_RUNNING_RUNNING = 4;

    /**
     * playStatus event data
     * EVENT_PLAYMODE_CHANGE: for a complete list see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619231.html
     */
    public static final int EV_PLAYCHG_STOP = 0; // STOP: stop status.
    public static final int EV_PLAYCHG_PAUSE = 1; // PAUSE: pause status.
    public static final int EV_PLAYCHG_PLAY = 2; // NORMAL_PLAY: normal playback status for non-live content
                                                 // (including TSTV).
    public static final int EV_PLAYCHG_TRICK = 3; // TRICK_MODE: trick play mode, such as fast-forward, rewind,
                                                  // slow-forward, and slow-rewind.
    public static final int EV_PLAYCHG_MC_PLAY = 4; // MULTICAST_CHANNEL_PLAY: live broadcast status of IPTV
                                                    // multicast channels and DVB channels.
    public static final int EV_PLAYCHG_UC_PLAY = 5; // UNICAST_CHANNEL_PLAY: live broadcast status of IPTV unicast
                                                    // channels and OTT channels. //
    public static final int EV_PLAYCHG_BUFFERING = 20; // BUFFERING: playback buffering status, including playing
                                                       // cPVR content during the recording, playing content
                                                       // during the download, playing the OTT content, and no
                                                       // data in the buffer area.

    //
    // MagentaTVControl SOAP requests
    //
    public static final String CHECKDEV_URI = "http://{0}:{1}{2}";

    public static final int PAIRING_TIMEOUT_SEC = 300;
    public static final String PAIRING_CONTROL_URI = "/upnp/service/X-CTC_RemotePairing/Control";
    public static final String PAIRING_SUBSCRIBE = "SUBSCRIBE /upnp/service/X-CTC_RemotePairing/Event HTTP/1.1\r\nHOST: {0}:{1}\r\nCALLBACK: <http://{2}:{3}{4}>\r\nNT: upnp:event\r\nTIMEOUT: Second-{5}\r\nCONNECTION: close\r\n\r\n";
    public static final String CONNECTION_CLOSE = "close";

    public static final String SOAP_ENVELOPE = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>{0}</s:Body></s:Envelope>";
    public static final String PAIRING_SOAP_ACTION = "\"urn:schemas-upnp-org:service:X-CTC_RemotePairing:1#X-pairingRequest\"";
    public static final String PAIRING_SOAP_BODY = "<u:X-pairingRequest xmlns:u=\"urn:schemas-upnp-org:service:X-CTC_RemotePairing:1\"><pairingDeviceID>{0}</pairingDeviceID><friendlyName>{1}</friendlyName><userID>{2}</userID></u:X-pairingRequest>";

    public static final String PAIRCHECK_URI = "/upnp/service/X-CTC_RemotePairing/Control";
    public static final String PAIRCHECK_SOAP_ACTION = "\"urn:schemas-upnp-org:service:X-CTC_RemotePairing:1#X-pairingCheck\"";
    public static final String PAIRCHECK_SOAP_BODY = "<u:X-pairingCheck xmlns:u=\"urn:schemas-upnp-org:service:X-CTC_RemotePairing:1\"><pairingDeviceID>{0}</pairingDeviceID><verificationCode>{1}</verificationCode></u:X-pairingCheck>";

    public static final String SENDKEY_URI = "/upnp/service/X-CTC_RemoteControl/Control";
    public static final String SENDKEY_SOAP_ACTION = "\"urn:schemas-upnp-org:service:X-CTC_RemoteControl:1#X_CTC_RemoteKey\"";
    public static final String SENDKEY_SOAP_BODY = "<u:X_CTC_RemoteKey xmlns:u=\"urn:schemas-upnp-org:service:X-CTC_RemoteControl:1\"><InstanceID>0</InstanceID><KeyCode>keyCode={0}^{1}:{2}^userID:{3}</KeyCode></u:X_CTC_RemoteKey>";

    public static final String HTTP_NOTIFY = "NOTIFY";
    public static final String NOTIFY_SID = "SID: ";

    public static final String HASH_ALGORITHM_MD5 = "MD5";
    public static final String HASH_ALGORITHM_SHA256 = "SHA-256";
}
