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
package org.openhab.binding.magentatv.internal.handler;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;
import static org.openhab.binding.magentatv.internal.MagentaTVUtil.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.magentatv.internal.MagentaTVException;
import org.openhab.binding.magentatv.internal.config.MagentaTVDynamicConfig;
import org.openhab.binding.magentatv.internal.network.MagentaTVHttp;
import org.openhab.binding.magentatv.internal.network.MagentaTVNetwork;
import org.openhab.binding.magentatv.internal.network.MagentaTVOAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVControl} implements the control functions for the
 * receiver.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVControl {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVControl.class);
    private final static HashMap<String, String> KEY_MAP = new HashMap<>();

    private final MagentaTVNetwork network;
    private final MagentaTVHttp http = new MagentaTVHttp();
    private final MagentaTVOAuth oauth;
    private final MagentaTVDynamicConfig config;
    private boolean initialized = false;
    private String thingId = "";

    public MagentaTVControl() {
        config = new MagentaTVDynamicConfig();
        network = new MagentaTVNetwork();
        oauth = new MagentaTVOAuth(new HttpClient());
    }

    public MagentaTVControl(MagentaTVDynamicConfig config, MagentaTVNetwork network, HttpClient httpClient) {
        this.thingId = config.getFriendlyName();
        this.network = network;
        this.oauth = new MagentaTVOAuth(httpClient);
        this.config = config;
        this.config.setTerminalID(computeMD5(network.getLocalMAC().toUpperCase() + config.getUDN()));
        this.config.setLocalIP(network.getLocalIP());
        this.config.setLocalMAC(network.getLocalMAC());
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setThingId(String thingId) {
        this.thingId = thingId;
    }

    /**
     * Returns the thingConfig - the Control class adds various attributes of the
     * discovered device (like the MR model)
     *
     * @return thingConfig
     */
    public MagentaTVDynamicConfig getConfig() {
        return config;
    }

    /**
     * Initiate OAuth authentication
     *
     * @param accountName T-Online user id
     * @param accountPassword T-Online password
     * @return true: successful, false: failed
     *
     * @throws MagentaTVException
     */
    public String getUserId(String accountName, String accountPassword) throws MagentaTVException {
        return oauth.getUserId(accountName, accountPassword);
    }

    /**
     * Retries the device properties. This will result in an Exception if the device
     * is not connected.
     *
     * Response is returned in XMl format, e.g.:
     * <?xml version="1.0"?> <root xmlns="urn:schemas-upnp-org:device-1-0">
     * <specVersion><major>1</major><minor>0</minor></specVersion> <device>
     * <UDN>uuid:70dff25c-1bdf-5731-a283-XXXXXXXX</UDN>
     * <friendlyName>DMS_XXXXXXXXXXXX</friendlyName>
     * <deviceType>urn:schemas-upnp-org:device:tvdevice:1</deviceType>
     * <manufacturer>Zenterio</manufacturer> <modelName>MR401B</modelName>
     * <modelNumber>R01A5</modelNumber> <productVersionNumber>&quot; 334
     * &quot;</productVersionNumber> <productType>stb</productType>
     * <serialNumber></serialNumber> <X_wakeOnLan>0</X_wakeOnLan> <serviceList>
     * <service> <serviceType>urn:dial-multiscreen-org:service:dial:1</serviceType>
     * <serviceId>urn:dial-multiscreen-org:service:dial</serviceId> </service>
     * </serviceList> </device> </root>
     *
     * @return true: device is online, false: device is offline
     * @throws MagentaTVException
     */
    public boolean checkDev() throws MagentaTVException {
        logger.debug("{}: Check device {} ({}:{})", thingId, config.getTerminalID(), config.getIpAddress(),
                config.getPort());

        String url = MessageFormat.format(CHECKDEV_URI, config.getIpAddress(), config.getPort(),
                config.getDescriptionUrl());
        String result = http.httpGet(buildHost(), url, "");
        if (result.contains("<modelName>")) {
            config.setModel(substringBetween(result, "<modelName>", "</modelName>"));
        }
        if (result.contains("<modelNumber>")) {
            config.setHardwareVersion(substringBetween(result, "<modelNumber>", "</modelNumber>"));
        }
        if (result.contains("<X_wakeOnLan>")) {
            String wol = substringBetween(result, "<X_wakeOnLan>", "</X_wakeOnLan>");
            config.setWakeOnLAN(wol);
            logger.debug("{}: Wake-on-LAN is {}", thingId, wol.equals("0") ? "disabled" : "enabled");
        }
        if (result.contains("<productVersionNumber>")) {
            String version;
            if (result.contains("<productVersionNumber>&quot; ")) {
                version = substringBetween(result, "<productVersionNumber>&quot; ", " &quot;</productVersionNumber>");
            } else {
                version = substringBetween(result, "<productVersionNumber>", "</productVersionNumber>");
            }
            config.setFirmwareVersion(version);
        }
        if (result.contains("<friendlyName>")) {
            String friendlyName = result.substring(result.indexOf("<friendlyName>") + "<friendlyName>".length(),
                    result.indexOf("</friendlyName>"));
            config.setFriendlyName(friendlyName);
        }
        if (result.contains("<UDN>uuid:")) {
            String udn = result.substring(result.indexOf("<UDN>uuid:") + "<UDN>uuid:".length(),
                    result.indexOf("</UDN>"));
            if (config.getUDN().isEmpty()) {
                config.setUDN(udn);
            }
        }
        logger.trace("{}: Online status verified for device {}:{}, UDN={}", thingId, config.getIpAddress(),
                config.getPort(), config.getUDN());
        return true;
    }

    /**
     *
     * Sends a SUBSCRIBE request to the MR. This also defines the local callback url
     * used by the MR to return the pairing code and event information.
     *
     * Subscripbe to event channel a) receive the pairing code b) receive
     * programInfo and playStatus events after successful paring
     *
     * SUBSCRIBE /upnp/service/X-CTC_RemotePairing/Event HTTP/1.1\r\n HOST:
     * $remote_ip:$remote_port CALLBACK: <http://$local_ip:$local_port/>\r\n // NT:
     * upnp:event\r\n // TIMEOUT: Second-300\r\n // CONNECTION: close\r\n // \r\n
     *
     * @throws MagentaTVException
     */
    public void subscribeEventChannel() throws MagentaTVException {
        String sid = "";
        logger.debug("{}: Subscribe Event Channel (terminalID={}, {}:{}", thingId, config.getTerminalID(),
                config.getIpAddress(), config.getPort());
        String subscribe = MessageFormat.format(PAIRING_SUBSCRIBE, config.getIpAddress(), config.getPort(),
                network.getLocalIP(), network.getLocalPort(), PAIRING_NOTIFY_URI, PAIRING_TIMEOUT_SEC);
        String response = http.sendData(config.getIpAddress(), config.getPort(), subscribe);
        if (!response.contains("200 OK")) {
            response = substringBefore(response, "SERVER");
            throw new MagentaTVException("Unable to subscribe to pairing channel: " + response);
        }
        if (!response.contains(NOTIFY_SID)) {
            throw new MagentaTVException("Unable to subscribe to pairing channel, SID missing: " + response);
        }

        StringTokenizer tokenizer = new StringTokenizer(response, "\r\n");
        while (tokenizer.hasMoreElements()) {
            String str = tokenizer.nextToken();
            if (!str.isEmpty()) {
                if (str.contains(NOTIFY_SID)) {
                    sid = str.substring("SID: uuid:".length());
                    logger.debug("{}: SUBSCRIBE returned SID {}", thingId, sid);
                    break;
                }
            }
        }
    }

    /**
     * Send Pairing Request to the Media Receiver. The method waits for the
     * response, but the pairing code will be received via the NOTIFY callback (see
     * NotifyServlet)
     *
     * XML format for Pairing Request: <s:Envelope
     * xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"
     * <s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"> <s:Body>\n
     * <u:X-pairingRequest
     * xmlns:u=\"urn:schemas-upnp-org:service:X-CTC_RemotePairing:1\">\n
     * <pairingDeviceID>$pairingDeviceID</pairingDeviceID>\n
     * <friendlyName>$friendlyName</friendlyName>\n <userID>$userID</userID>\n
     * </u:X-pairingRequest>\n </s:Body> </s:Envelope>
     *
     * @returns true: pairing successful
     * @throws MagentaTVException
     */
    public boolean sendPairingRequest() throws MagentaTVException {
        logger.debug("{}: Send Pairing Request (deviceID={}, type={}, userID={})", thingId, config.getTerminalID(),
                DEF_FRIENDLY_NAME, config.getUserId());
        resetPairing();

        String soapBody = MessageFormat.format(PAIRING_SOAP_BODY, config.getTerminalID(), DEF_FRIENDLY_NAME,
                config.getUserId());
        String soapXml = MessageFormat.format(SOAP_ENVELOPE, soapBody);
        String response = http.httpPOST(buildHost(), buildReceiverUrl(PAIRING_CONTROL_URI), soapXml,
                PAIRING_SOAP_ACTION, CONNECTION_CLOSE);

        // pairingCode will be received by the Servlet, is calls onPairingResult()
        // Exception if request failed (response code != HTTP_OK)
        if (!response.contains("X-pairingRequestResponse") || !response.contains("<result>")) {
            throw new MagentaTVException("Unexpected result for pairing response: " + response);
        }

        String result = substringBetween(response, "<result>", "</result>");
        if (!result.equals("0")) {
            throw new MagentaTVException("Pairing failed, result=" + result);
        }

        logger.debug("{}: Pairing initiated (deviceID={}).", thingId, config.getTerminalID());
        return true;
    }

    /**
     * Calculates the verifificationCode to complete pairing. This will be triggered
     * as a result after receiving the pairing code provided by the MR. The
     * verification code is the MD5 hash of <Pairing Code><Terminal-ID><User ID>
     *
     * @param pairingCode Pairing code received from the MR
     * @return true: a new code has been generated, false: the code matches a
     *         previous pairing
     */
    public boolean generateVerificationCode(String pairingCode) {
        if (config.getPairingCode().equals(pairingCode) && !config.getVerificationCode().isEmpty()) {
            logger.debug("{}: Pairing code ({}) refreshed, verificationCode={}", thingId, pairingCode,
                    config.getVerificationCode());
            return false;
        }
        config.setPairingCode(pairingCode);
        String md5Input = pairingCode + config.getTerminalID() + config.getUserId();
        config.setVerificationCode(computeMD5(md5Input).toUpperCase());
        logger.debug("{}: VerificationCode({}): Input={}, code={}", thingId, config.getTerminalID(), md5Input,
                config.getVerificationCode());
        return true;
    }

    /**
     * Send a pairing verification request to the receiver. This is important to
     * complete the pairing process. You should see a message like "Connected to
     * openHAB" on your TV screen.
     *
     * @return true: successful, false: a non-critical error occured, caller handles
     *         this
     * @throws MagentaTVException
     */
    public boolean verifyPairing() throws MagentaTVException {
        logger.debug("{}: Verify pairing (id={}, code={}", thingId, config.getTerminalID(),
                config.getVerificationCode());
        String soapBody = MessageFormat.format(PAIRCHECK_SOAP_BODY, config.getTerminalID(),
                config.getVerificationCode());
        String soapXml = MessageFormat.format(SOAP_ENVELOPE, soapBody);
        String response = http.httpPOST(buildHost(), buildReceiverUrl(PAIRCHECK_URI), soapXml, PAIRCHECK_SOAP_ACTION,
                CONNECTION_CLOSE);

        // Exception if request failed (response code != HTTP_OK)
        if (!response.contains("<pairingResult>")) {
            throw new MagentaTVException("Unexpected result for pairing verification: " + response);
        }

        String result = getXmlValue(response, "pairingResult");
        if (!result.equals("0")) {
            logger.debug("{}: Pairing failed or pairing no longer valid, result={}", thingId, result);
            resetPairing();
            // let the caller decide how to proceed
            return false;
        }

        if (!config.isMR400()) {
            String enable4K = getXmlValue(response, "Enable4K");
            String enableSAT = getXmlValue(response, "EnableSAT");
            logger.debug("{}: Features: Enable4K:{}, EnableSAT:{}", thingId, enable4K, enableSAT);
        }
        return true;
    }

    /**
     *
     * @return true if pairing is completed (verification code was generated)
     */
    public boolean isPaired() {
        // pairing was completed successful if we have the verification code
        return !config.getVerificationCode().isEmpty();
    }

    /**
     * Reset pairing information (e.g. when verification failed)
     */
    public void resetPairing() {
        // pairing no longer valid
        config.setPairingCode("");
        config.setVerificationCode("");
    }

    /**
     * Send key code to the MR (via SOAP request). A key code could be send by it's
     * code (0x.... notation) or with a symbolic namne, which will first be mapped
     * to the key code
     *
     * XML format for Send Key
     *
     * <s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"
     * s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"> <s:Body>\n
     * <u:X_CTC_RemoteKey
     * xmlns:u=\"urn:schemas-upnp-org:service:X-CTC_RemoteControl:1\">\n
     * <InstanceID>0</InstanceID>\n
     * <KeyCode>keyCode=$keyCode^$pairingDeviceID:$verificationCode^userID:$userID</KeyCode>\n
     * </u:X_CTC_RemoteKey>\n </s:Body></s:Envelope>
     *
     * @param keyName
     * @return true: successful, false: failed, e.g. unkown key code
     * @throws MagentaTVException
     */
    public boolean sendKey(String keyName) throws MagentaTVException {
        String keyCode = getKeyCode(keyName);
        logger.debug("{}: Send Key {} (keyCode={}, tid={})", thingId, keyName, keyCode, config.getTerminalID());
        if (keyCode.length() <= "0x".length()) {
            logger.debug("{}: Key {} is unkown!", thingId, keyCode);
            return false;
        }

        String soapBody = MessageFormat.format(SENDKEY_SOAP_BODY, keyCode, config.getTerminalID(),
                config.getVerificationCode(), config.getUserId());
        String soapXml = MessageFormat.format(SOAP_ENVELOPE, soapBody);
        logger.debug("{}: send keyCode={} to {}:{}", thingId, keyCode, config.getIpAddress(), config.getPort());
        logger.trace("{}: sendKey terminalid={}, pairingCode={}, verificationCode={}, userId={}", thingId,
                config.getTerminalID(), config.getPairingCode(), config.getVerificationCode(), config.getUserId());
        http.httpPOST(buildHost(), buildReceiverUrl(SENDKEY_URI), soapXml, SENDKEY_SOAP_ACTION, CONNECTION_CLOSE);
        // Exception if request failed (response code != HTTP_OK)
        // pairingCode will be received by the Servlet, is calls onPairingResult()
        return true;
    }

    /**
     * Select channel for TV
     *
     * @param channel new channel (a sequence of numbers, which will be send one by one)
     * @return true:ok, false: failed
     */
    public boolean selectChannel(String channel) throws MagentaTVException {
        logger.debug("{}: Select channel {}", thingId, channel);
        for (int i = 0; i < channel.length(); i++) {
            if (!sendKey("" + channel.charAt(i))) {
                return false;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    /**
     * Get key code to send to receiver
     *
     * @param key Key for which to get the key code
     * @return
     */
    private String getKeyCode(String key) {
        if (key.contains("0x")) {
            // direct key code
            return key;
        }
        String code = KEY_MAP.get(key);
        return code != null ? code : "";
    }

    /**
     * Map playStatus code to string for a list of codes see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619231.html
     *
     * @param playStatus Integer code parsed form json (see EV_PLAYCHG_XXX)
     * @return playStatus as String
     */
    public String getPlayStatus(int playStatus) {
        switch (playStatus) {
            case EV_PLAYCHG_PLAY:
                return "playing";
            case EV_PLAYCHG_STOP:
                return "stopped";
            case EV_PLAYCHG_PAUSE:
                return "paused";
            case EV_PLAYCHG_TRICK:
                return "tricking";
            case EV_PLAYCHG_MC_PLAY:
                return "playing (MC)";
            case EV_PLAYCHG_UC_PLAY:
                return "playing (UC)";
            case EV_PLAYCHG_BUFFERING:
                return "buffering";
            default:
                return Integer.toString(playStatus);
        }
    }

    /**
     * Map runningStatus code to string for a list of codes see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619523.html
     *
     * @param runStatus Integer code parsed form json (see EV_EITCHG_RUNNING_XXX)
     * @return runningStatus as String
     */
    public String getRunStatus(int runStatus) {
        switch (runStatus) {
            case EV_EITCHG_RUNNING_NOT_RUNNING:
                return "stopped";
            case EV_EITCHG_RUNNING_STARTING:
                return "starting";
            case EV_EITCHG_RUNNING_PAUSING:
                return "paused";
            case EV_EITCHG_RUNNING_RUNNING:
                return "running";
            default:
                return Integer.toString(runStatus);
        }
    }

    /**
     * builds url from the discovered IP address/port and the requested uri
     *
     * @param uri requested URI
     * @return the complete URL
     */
    public String buildReceiverUrl(String uri) {
        return MessageFormat.format("http://{0}:{1}{2}", config.getIpAddress(), config.getPort(), uri);
    }

    /**
     * build host string
     *
     * @return formatted string (<ip_address>:<port>)
     */
    private String buildHost() {
        return config.getIpAddress() + ":" + config.getPort();
    }

    /**
     * Given a string, return the MD5 hash of the String.
     *
     * @param unhashed The string contents to be hashed.
     * @return MD5 Hashed value of the String. Null if there is a problem hashing
     *         the String.
     */
    public static String computeMD5(String unhashed) {
        try {
            byte[] bytesOfMessage = unhashed.getBytes(StandardCharsets.UTF_8);

            MessageDigest md5 = MessageDigest.getInstance(HASH_ALGORITHM_MD5);
            byte[] hash = md5.digest(bytesOfMessage);
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    /**
     * Helper to parse a Xml tag value from string without using a complex XML class
     *
     * @param xml Input string in the format <tag>value</tag>
     * @param tagName The tag to find
     * @return Tag value (between <tag> and </tag>)
     */
    public static String getXmlValue(String xml, String tagName) {
        String open = "<" + tagName + ">";
        String close = "</" + tagName + ">";
        if (xml.contains(open) && xml.contains(close)) {
            return substringBetween(xml, open, close);
        }
        return "";
    }

    /**
     * Initialize key map (key name -> key code)
     * "
     * for a list of valid key codes see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619112.html
     */
    static {
        KEY_MAP.put("POWER", "0x0100");
        KEY_MAP.put("MENU", "0x0110");
        KEY_MAP.put("EPG", "0x0111");
        KEY_MAP.put("TVMENU", "0x0454");
        KEY_MAP.put("VODMENU", "0x0455");
        KEY_MAP.put("TVODMENU", "0x0456");
        KEY_MAP.put("NVODMENU", "0x0458");
        KEY_MAP.put("INFO", "0x010C");
        KEY_MAP.put("TTEXT", "0x0560");
        KEY_MAP.put("0", "0x0030");
        KEY_MAP.put("1", "0x0031");
        KEY_MAP.put("2", "0x0032");
        KEY_MAP.put("3", "0x0033");
        KEY_MAP.put("4", "0x0034");
        KEY_MAP.put("5", "0x0035");
        KEY_MAP.put("6", "0x0036");
        KEY_MAP.put("7", "0x0037");
        KEY_MAP.put("8", "0x0038");
        KEY_MAP.put("9", "0x0039");
        KEY_MAP.put("SPACE", "0x0020");
        KEY_MAP.put("POUND", "0x0069");
        KEY_MAP.put("STAR", "0x006A");
        KEY_MAP.put("UP", "0x0026");
        KEY_MAP.put("DOWN", "0x0028");
        KEY_MAP.put("LEFT", "0x0025");
        KEY_MAP.put("RIGHT", "0x0027");
        KEY_MAP.put("PGUP", "0x0021");
        KEY_MAP.put("PGDOWN", "0x0022");
        KEY_MAP.put("DELETE", "0x0008");
        KEY_MAP.put("ENTER", "0x000D");
        KEY_MAP.put("SEARCH", "0x0451");
        KEY_MAP.put("RED", "0x0113");
        KEY_MAP.put("GREEN", "0x0114");
        KEY_MAP.put("YELLOW", "0x0115");
        KEY_MAP.put("BLUE", "0x0116");
        KEY_MAP.put("OPTION", "0x0460");
        KEY_MAP.put("OK", "0x000D");
        KEY_MAP.put("BACK", "0x0008");
        KEY_MAP.put("EXIT", "0x045D");
        KEY_MAP.put("PORTAL", "0x0110");
        KEY_MAP.put("VOLUP", "0x0103");
        KEY_MAP.put("VOLDOWN", "0x0104");
        KEY_MAP.put("INTER", "0x010D");
        KEY_MAP.put("HELP", "0x011C");
        KEY_MAP.put("SETTINGS", "0x011D");
        KEY_MAP.put("MUTE", "0x0105");
        KEY_MAP.put("CHUP", "0x0101");
        KEY_MAP.put("CHDOWN", "0x0102");
        KEY_MAP.put("REWIND", "0x0109");
        KEY_MAP.put("PLAY", "0x0107");
        KEY_MAP.put("PAUSE", "0x0107");
        KEY_MAP.put("FORWARD", "0x0108");
        KEY_MAP.put("TRACK", "0x0106");
        KEY_MAP.put("LASTCH", "0x045E");
        KEY_MAP.put("PREVCH", "0x010B");
        KEY_MAP.put("NEXTCH", "0x0107");
        KEY_MAP.put("RECORD", "0x0461");
        KEY_MAP.put("STOP", "0x010E");
        KEY_MAP.put("BEGIN", "0x010B");
        KEY_MAP.put("END", "0x010A");
        KEY_MAP.put("REPLAY", "0x045B");
        KEY_MAP.put("SKIP", "0x045C");
        KEY_MAP.put("SUBTITLE", "0x236");
        KEY_MAP.put("RECORDINGS", "0x045F");
        KEY_MAP.put("FAV", "0x0119");
        KEY_MAP.put("SOURCE", "0x0083");
        KEY_MAP.put("SWITCH", "0x0118");
        KEY_MAP.put("IPTV", "0x0081");
        KEY_MAP.put("PC", "0x0082");
        KEY_MAP.put("PIP", "0x0084");
        KEY_MAP.put("MULTIVIEW", "0x0562");
        KEY_MAP.put("F1", "0x0070");
        KEY_MAP.put("F2", "0x0071");
        KEY_MAP.put("F3", "0x0072");
        KEY_MAP.put("F4", "0x0073");
        KEY_MAP.put("F5", "0x0074");
        KEY_MAP.put("F6", "0x0075");
        KEY_MAP.put("F7", "0x0076");
        KEY_MAP.put("F8", "0x0077");
        KEY_MAP.put("F9", "0x0078");
        KEY_MAP.put("F10", "0x0079");
        KEY_MAP.put("F11", "0x007A");
        KEY_MAP.put("F12", "0x007B");
        KEY_MAP.put("F13", "0x007C");
        KEY_MAP.put("F14", "0x007D");
        KEY_MAP.put("F15", "0x007E");
        KEY_MAP.put("F16", "0x007F");

        KEY_MAP.put("PVR", "0x0461");
        KEY_MAP.put("RADIO", "0x0462");

        // Those key codes are missing and not included in the spec
        // KEY_MAP.put("TV", "0x");
        // KEY_MAP.put("RADIO", "0x");
        // KEY_MAP.put("MOVIES", "0x");
    }
}
