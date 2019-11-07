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
package org.openhab.binding.magentatv.internal.handler;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.magentatv.internal.MagentaTVConfiguration;
import org.openhab.binding.magentatv.internal.MagentaTVException;
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

    private final MagentaTVNetwork network;
    private final MagentaTVHttp http = new MagentaTVHttp();
    private final MagentaTVOAuth oauth = new MagentaTVOAuth();
    private final MagentaTVConfiguration thingConfig;
    private final HashMap<String, String> keyMap = new HashMap<String, String>();

    public MagentaTVControl(MagentaTVConfiguration config, MagentaTVNetwork network) {
        this.network = network;
        thingConfig = config;
        thingConfig.setTerminalID(computeMD5(network.getLocalMAC().toUpperCase() + thingConfig.getUDN()).toUpperCase());
        thingConfig.setLocalIP(network.getLocalIP());
        thingConfig.setLocalMAC(network.getLocalMAC());
        initializeKeyMap();
    }

    /**
     * Returns the thingConfig - the Control class adds various attributes of the
     * discovered device (like the MR model)
     *
     * @return thingConfig
     */
    public MagentaTVConfiguration getConfig() {
        return thingConfig;
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
    public String authenticateUser(String accountName, String accountPassword) throws MagentaTVException {
        if (accountName.equals("***") || accountPassword.equals("***")) {
            throw new MagentaTVException("Credentials missing or invalid!");
        }
        return oauth.getOAuthCredentials(accountName, accountPassword);
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
        logger.debug("Check device {} ({}:{})", thingConfig.getTerminalID(), thingConfig.getIpAddress(),
                thingConfig.getPort());

        String url = MessageFormat.format(CHECKDEV_URI, thingConfig.getIpAddress(), thingConfig.getPort(),
                thingConfig.getDescriptionUrl());
        String result = http.httpGet(buildHost(), url, "");
        if (result.contains("<modelName>")) {
            {
                thingConfig.setModel(StringUtils.substringBetween(result, "<modelName>", "</modelName>"));
            }
        }
        if (result.contains("<modelNumber>")) {
            {
                thingConfig.setHardwareVersion(StringUtils.substringBetween(result, "<modelNumber>", "</modelNumber>"));
            }
        }
        if (result.contains("<X_wakeOnLan>")) {
            {
                String wol = StringUtils.substringBetween(result, "<X_wakeOnLan>", "</X_wakeOnLan>");
                thingConfig.setWakeOnLAN(wol);
                logger.debug("Wake-on-LAN is {}", wol.equals("0") ? "disabled" : "enabled");
            }
        }
        if (result.contains("<productVersionNumber>")) {
            {
                String version;
                if (result.contains("<productVersionNumber>&quot; ")) {
                    version = StringUtils.substringBetween(result, "<productVersionNumber>&quot; ",
                            " &quot;</productVersionNumber>");
                } else {
                    version = StringUtils.substringBetween(result, "<productVersionNumber>", "</productVersionNumber>");

                }
                thingConfig.setFirmwareVersion(version);
            }
        }
        if (result.contains("<friendlyName>")) {
            String friendlyName = result.substring(result.indexOf("<friendlyName>") + "<friendlyName>".length(),
                    result.indexOf("</friendlyName>"));
            thingConfig.setFriendlyName(friendlyName);
        }
        if (result.contains("<UDN>uuid:")) {
            String udn = result.substring(result.indexOf("<UDN>uuid:") + "<UDN>uuid:".length(),
                    result.indexOf("</UDN>"));
            if (thingConfig.getUDN().isEmpty()) {
                thingConfig.setUDN(udn);
            }
        }
        logger.trace("Online status verified for device {} ({}:{}, UDN={})", thingConfig.getFriendlyName(),
                thingConfig.getIpAddress(), thingConfig.getPort(), thingConfig.getUDN());
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
        logger.debug("Subscribe Event Channel for device {} (deviceID={}, {}:{})", thingConfig.getFriendlyName(),
                thingConfig.getTerminalID(), thingConfig.getIpAddress(), thingConfig.getPort());
        String subscribe = MessageFormat.format(PAIRING_SUBSCRIBE, thingConfig.getIpAddress(), thingConfig.getPort(),
                network.getLocalIP(), network.getLocalPort(), PAIRING_NOTIFY_URI, PAIRING_TIMEOUT);
        String response = http.sendData(thingConfig.getIpAddress(), thingConfig.getPort(), subscribe);
        if (!response.contains("200 OK")) {
            response = StringUtils.substringBefore(response, "SERVER");
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
                    logger.debug("SUBSCRIBE returned SID {}", sid);
                    continue;
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
        logger.debug("{}: Send Pairing Request (deviceID={}, type={}, userID={})", thingConfig.getFriendlyName(),
                thingConfig.getTerminalID(), DEF_FRIENDLY_NAME, thingConfig.getUserID());
        resetPairing();

        String soapBody = MessageFormat.format(PAIRING_SOAP_BODY, thingConfig.getTerminalID(), DEF_FRIENDLY_NAME,
                thingConfig.getUserID());
        String soapXml = MessageFormat.format(SOAP_ENVELOPE, soapBody);
        String response = http.httpPOST(buildHost(), buildReceiverUrl(PAIRING_CONTROL_URI), soapXml,
                PAIRING_SOAP_ACTION, CONNECTION_CLOSE);

        // pairingCode will be received by the Servlet, is calls onPairingResult()
        // Exception if request failed (response code != HTTP_OK)
        if (!response.contains("X-pairingRequestResponse") || !response.contains("<result>")) {
            throw new MagentaTVException("Unexpected result for pairing response: " + response);
        }

        String result = StringUtils.substringBetween(response, "<result>", "</result>");
        if (!result.equals("0")) {
            throw new MagentaTVException("Pairing failed, result=" + result);
        }

        logger.debug("{}: Pairing initiated (deviceID={}).", thingConfig.getFriendlyName(),
                thingConfig.getTerminalID());
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
        if (thingConfig.getPairingCode().equals(pairingCode) && !thingConfig.getVerificationCode().isEmpty()) {
            logger.debug("{}: Pairing code ({}) refreshed, verificationCode={}", thingConfig.getFriendlyName(),
                    pairingCode, thingConfig.getVerificationCode());
            return false;
        }
        thingConfig.setPairingCode(pairingCode);
        String md5Input = pairingCode + thingConfig.getTerminalID() + thingConfig.getUserID();
        thingConfig.setVerificationCode(computeMD5(md5Input).toUpperCase());
        logger.debug("{}: VerificationCode({}): Input={}, code={}", thingConfig.getFriendlyName(),
                thingConfig.getTerminalID(), md5Input, thingConfig.getVerificationCode());
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
        logger.debug("{}: Verify pairing (id={}, code={}", thingConfig.getFriendlyName(), thingConfig.getTerminalID(),
                thingConfig.getVerificationCode());
        String soapBody = MessageFormat.format(PAIRCHECK_SOAP_BODY, thingConfig.getTerminalID(),
                thingConfig.getVerificationCode());
        String soapXml = MessageFormat.format(SOAP_ENVELOPE, soapBody);
        String response = http.httpPOST(buildHost(), buildReceiverUrl(PAIRCHECK_URI), soapXml, PAIRCHECK_SOAP_ACTION,
                CONNECTION_CLOSE);

        // Exception if request failed (response code != HTTP_OK)
        if (!response.contains("<pairingResult>")) {
            throw new MagentaTVException("Unexpected result for pairing verification: " + response);
        }

        String result = getXmlValue(response, "pairingResult");
        if (!result.equals("0")) {
            logger.info("{}: Pairing failed or pairing no longer valid, result={}", thingConfig.getFriendlyName(),
                    result);
            resetPairing();
            // let the caller decide how to proceed
            return false;
        }

        if (!thingConfig.isMR400()) {
            String enable4K = getXmlValue(response, "Enable4K");
            String enableSAT = getXmlValue(response, "EnableSAT");
            logger.debug("{}: Features: Enable4K:{}, EnableSAT:{}", thingConfig.getFriendlyName(), enable4K, enableSAT);
        }
        return true;
    }

    /**
     *
     * @return true if pairing is completed (verification code was generated)
     */
    public boolean isPaired() {
        // pairing was completed successful if we have the verification code
        return !thingConfig.getVerificationCode().isEmpty();
    }

    /**
     * Reset pairing information (e.g. when verification failed)
     */
    public void resetPairing() {
        // pairing no longer valid
        thingConfig.setPairingCode("");
        thingConfig.setVerificationCode("");
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
        logger.debug("{}: Send Key {} (keyCode={}, tid={})", thingConfig.getFriendlyName(), keyName, keyCode,
                thingConfig.getTerminalID());
        if (keyCode.length() <= "0x".length()) {
            logger.info("{}: Key {} is unkown!", thingConfig.getFriendlyName(), keyCode);
            return false;
        }

        String soapBody = MessageFormat.format(SENDKEY_SOAP_BODY, keyCode, thingConfig.getTerminalID(),
                thingConfig.getVerificationCode(), thingConfig.getUserID());
        String soapXml = MessageFormat.format(SOAP_ENVELOPE, soapBody);
        logger.debug("{}: send keyCode={} to {}:{}", thingConfig.getFriendlyName(), keyCode, thingConfig.getIpAddress(),
                thingConfig.getPort());
        logger.trace("sendKey terminalid={}, pairingCode={}, verificationCode={}, userId={}",
                thingConfig.getTerminalID(), thingConfig.getPairingCode(), thingConfig.getVerificationCode(),
                thingConfig.getUserID());
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
        logger.info("Select channel {}", channel);
        for (int i = 0; i < channel.length(); i++) {
            if (!sendKey("" + channel.charAt(i))) {
                return false;
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
    @SuppressWarnings("null")
    private String getKeyCode(String key) {
        if (key.contains("0x")) {
            // direct key code
            return key;
        }
        if (keyMap.isEmpty()) {
            initializeKeyMap();
        }
        if (keyMap.containsKey(key)) {
            return keyMap.get(key);
        }
        return "";
    }

    /**
     * Map playStatus code to string for a list of codes see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619231.html
     *
     * @param playStatus Integer code parsed form json (see EV_PLAYCHG_XXX)
     * @return playStatus as String
     */
    public String getPlayStatus(int playStatus) {
        if (playStatus == EV_PLAYCHG_PLAY) {
            return "playing";
        } else if (playStatus == EV_PLAYCHG_STOP) {
            return "stopped";
        } else if (playStatus == EV_PLAYCHG_PAUSE) {
            return "paused";
        } else if (playStatus == EV_PLAYCHG_TRICK) {
            return "tricking";
        } else if (playStatus == EV_PLAYCHG_MC_PLAY) {
            return "playing (MC)";
        } else if (playStatus == EV_PLAYCHG_UC_PLAY) {
            return "playing (UC)";
        } else if (playStatus == EV_PLAYCHG_BUFFERING) {
            return "buffering";
        } else {
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
        if (runStatus == EV_EITCHG_RUNNING_NOT_RUNNING) {
            return "stopped";
        } else if (runStatus == EV_EITCHG_RUNNING_STARTING) {
            return "starting";
        } else if (runStatus == EV_EITCHG_RUNNING_PAUSING) {
            return "paused";
        } else if (runStatus == EV_EITCHG_RUNNING_RUNNING) {
            return "running";
        }
        return Integer.toString(runStatus);
    }

    /**
     * builds url from the discovered IP address/port and the requested uri
     *
     * @param uri requested URI
     * @return the complete URL
     */
    public String buildReceiverUrl(String uri) {
        return MessageFormat.format("http://{0}:{1}{2}", thingConfig.getIpAddress(), thingConfig.getPort(), uri);
    }

    /**
     * build host string
     *
     * @return formatted string (<ip_address>:<port>)
     */
    private String buildHost() {
        return thingConfig.getIpAddress() + ":" + thingConfig.getPort();
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
            byte[] bytesOfMessage = unhashed.getBytes(CHARSET_UTF8);

            MessageDigest md5 = MessageDigest.getInstance(HASH_ALGORITHM_MD5);
            byte[] hash = md5.digest(bytesOfMessage);
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | RuntimeException e) {
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
            return StringUtils.substringBetween(xml, open, close);
        }
        return "";
    }

    /**
     * Initialize key map (key name -> key code)
     *
     * for a list of valid key codes see
     * http://support.huawei.com/hedex/pages/DOC1100366313CEH0713H/01/DOC1100366313CEH0713H/01/resources/dsv_hdx_idp/DSV/en/en-us_topic_0094619112.html
     */
    private void initializeKeyMap() {
        keyMap.put("POWER", "0x0100");
        keyMap.put("TV", "0x");
        keyMap.put("RADIO", "0x");
        keyMap.put("MOVIES", "0x");
        keyMap.put("MEDIA", "0x0462");
        keyMap.put("TTEXT", "0x0560");
        keyMap.put("1", "0x0031");
        keyMap.put("2", "0x0032");
        keyMap.put("3", "0x0033");
        keyMap.put("4", "0x0034");
        keyMap.put("5", "0x0035");
        keyMap.put("6", "0x0036");
        keyMap.put("7", "0x0037");
        keyMap.put("8", "0x0038");
        keyMap.put("9", "0x0039");
        keyMap.put("0", "0x0030");
        keyMap.put("SPACE", "0x0030");
        keyMap.put("DELETE", "0x0008");
        keyMap.put("ENTER", "0x000D");
        keyMap.put("SEARCH", "0x0451");
        keyMap.put("RED", "0x0113");
        keyMap.put("GREEN", "0x0114");
        keyMap.put("YELLOW", "0x0115");
        keyMap.put("BLUE", "0x0116");
        keyMap.put("EPG", "0x0111");
        keyMap.put("OPTION", "0x0460");
        keyMap.put("UP", "0x0026");
        keyMap.put("DOWN", "0x0028");
        keyMap.put("LEFT", "0x0025");
        keyMap.put("RIGHT", "0x0027");
        keyMap.put("PGUP", "0x0021");
        keyMap.put("PGDOWN", "0x0022");
        keyMap.put("OK", "0x000D");
        keyMap.put("BACK", "0x0008");
        keyMap.put("EXIT", "0x045D");
        keyMap.put("MENU", "0x0110");
        keyMap.put("VOLUP", "0x0103");
        keyMap.put("VOLDOWN", "0x0104");
        keyMap.put("INFO", "0x010C");
        keyMap.put("MUTE", "0x0105");
        keyMap.put("CHUP", "0x0101");
        keyMap.put("CHDOWN", "0x0102");
        keyMap.put("REWIND", "0x0109");
        keyMap.put("PLAY", "0x0107");
        keyMap.put("PAUSE", "0x0107");
        keyMap.put("FORWARD", "0x0108");
        keyMap.put("TRACK", "0x0106");
        keyMap.put("PREVCH", "0x010B");
        keyMap.put("NEXTCH", "0x0107");
        keyMap.put("RECORD", "0x0461");
        keyMap.put("STOP", "0x010E");
        keyMap.put("SPACE", "0x0020");
        keyMap.put("POUND", "0x0069");
        keyMap.put("STAR", "0x006A");
        keyMap.put("FAV", "0x0119");
        keyMap.put("SOURCE", "0x0083");
        keyMap.put("PIP", "0x0084");
        keyMap.put("F1", "0x0070");
        keyMap.put("F2", "0x0071");
        keyMap.put("PORTAL", "0x0110");
    }

}
