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
package org.openhab.binding.dlinksmarthome.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Formatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.measure.Unit;
import javax.naming.CommunicationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.dlinksmarthome.internal.DLinkSmartHomeBindingConstants;
import org.openhab.binding.dlinksmarthome.internal.DLinkThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link DLinkMotionSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pascal Bies - Initial contribution
 */
public class DLinkSmartPlugHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(DLinkSmartPlugHandler.class);
    private String ip;
    private String pin;
    private String url;
    private static final String USER = "admin";
    private final HttpClient httpClient;
    private ScheduledFuture<?> pollFuture;
    private boolean invalid_configuration = false;
    Authentication authentication;
    private static final int DETECT_POLL_S = 1;
    private final Runnable poller = new Runnable() {
        @Override
        public void run() {
            if (authenticate()) {
                updateStatus(ThingStatus.ONLINE);
                for (final String channelID : DLinkSmartHomeBindingConstants.SMART_PLUG_CHANNEL_IDS) {
                    updateState(getChannelUID(channelID), getNewChannelState(channelID));
                }
            }
        }
    };

    class Authentication {
        public Authentication(final String privateKey, final String cookie) {
            this.privateKey = privateKey;
            this.cookie = cookie;
        }

        public String privateKey;
        public String cookie;
    }

    public DLinkSmartPlugHandler(final Thing thing) {
        super(thing);
        httpClient = new HttpClient();
        url = "http://" + ip + "/HNAP1/";
    }

    @Override
    public void dispose() {
        super.dispose();
        if (pollFuture != null) {
            pollFuture.cancel(true);
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (authenticate()) {
            if (command == RefreshType.REFRESH) {
                updateState(channelUID, getNewChannelState(channelUID.getId()));
            } else if (channelUID.equals(getChannelUID(DLinkSmartHomeBindingConstants.STATE))) {
                try {
                    setState((OnOffType) command);
                } catch (ClassCastException e) {
                    logger.error("Unexpected command type for channel '{}'.", DLinkSmartHomeBindingConstants.STATE);
                }
            }
        }
    }

    @Override
    public void initialize() {
        final DLinkThingConfig config = getConfigAs(DLinkThingConfig.class);
        this.pin = config.pin;
        this.ip = config.ipAddress;
        this.url = "http://" + ip + "/HNAP1/";
        try {
            httpClient.start();
        } catch (Exception e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unexpected internal error.");
            logger.error("Failed to start http client.");
        }
        try {
            pollFuture = scheduler.scheduleWithFixedDelay(poller, 0, DETECT_POLL_S, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Failed to poll state.");
        }
    }

    private State getCurrentConsuption() {
        return getQuantity("GetCurrentPowerConsumption", "CurrentConsumption", "2", SmartHomeUnits.WATT);
    }

    private State getTotalConsumption() {
        return getQuantity("GetPMWarningThreshold", "TotalConsumption", "2", SmartHomeUnits.KILOWATT_HOUR);
    }

    private State getTemperature() {
        return getQuantity("GetCurrentTemperature", "CurrentTemperature", "3", SIUnits.CELSIUS);
    }

    private State getState() {
        try {
            String state = soapAction("GetSocketSettings", "OPStatus", moduleParameters("1")).toLowerCase();
            if ("true".contentEquals(state)) {
                return OnOffType.ON;
            } else if ("false".equals(state)) {
                return OnOffType.OFF;
            } else {
                logger.error("Unexpected state: " + state);
            }
        } catch (CommunicationException e) {
            logger.error("Failed to get state (communication error).");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return UnDefType.UNDEF;
    }

    private void setState(final OnOffType state) {
        try {
            final String encodedState = OnOffType.ON.equals(state) ? "true" : "false";
            final String controlParameters = moduleParameters("1")
                    + "<NickName>Socket 1</NickName><Description>Socket 1</Description>" + "<OPStatus>" + encodedState
                    + "</OPStatus>";
            final String response = soapAction("SetSocketSettings", "SetSocketSettingsResult", controlParameters);
            if (!"ok".equals(response.toLowerCase())) {
                logger.error("Failed to set state.");
            }
        } catch (Exception e) {
            // pass
        }
    }

    private String moduleParameters(final String module) {
        return "<ModuleID>" + module + "</ModuleID>";
    }

    private State getQuantity(final String action, final String name, final String module, final Unit<?> unit) {
        try {
            final String s = soapAction(action, name, moduleParameters(module));
            try {
                if (s == null) {
                    return UnDefType.UNDEF;
                } else {
                    return new QuantityType<>(Double.valueOf(s), unit);
                }
            } catch (NumberFormatException e) {
                logger.error("Unexpected number format: " + s);
            }
        } catch (CommunicationException e) {
            logger.error("Failed to get quantity (communication error).");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return UnDefType.UNDEF;
    }

    private String soapAction(final String action, final String name, final String params)
            throws CommunicationException {
        Document document = post(action, requestBody(action, params), authentication);
        return getElementByTagName(document, name);
    }

    public boolean authenticate() {
        if (invalid_configuration) {
            // If the configuration (i.e., PIN) is invalid, trying to long in does not make sense.
            authentication = null; // it should be null already, but for sake of consistency...
            return false;
        } else if (authentication != null) {
            // We are already authenticated, nothing to do.
            // If you want to force re-authentication, set this.authentication = null.
            return true;
        }

        try {
            Document document = post("Login", INITIAL_AUTH_PAYLOAD);
            final String challenge = getElementByTagName(document, "Challenge");
            final String cookie = getElementByTagName(document, "Cookie");
            final String publicKey = getElementByTagName(document, "PublicKey");
            final String privateKey = calculateHMAC(challenge, publicKey + pin);
            final String loginPassword = calculateHMAC(challenge, privateKey);

            authentication = new Authentication(privateKey, cookie);
            Document document2 = post("Login", authPayload(loginPassword), authentication);
            final String loginStatus = getElementByTagName(document2, "LoginResult").toLowerCase();
            if ("success".equals(loginStatus)) {
                updateStatus(ThingStatus.ONLINE);
                invalid_configuration = false;
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Failed to authenticate.");
                invalid_configuration = true;
                authentication = null;
                return false;
            }
        } catch (CommunicationException e) {
            authentication = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to authenticate.");
            return false;
        }
    }

    private Document post(final String action, final String payload) throws CommunicationException {
        return post(action, payload, null);
    }

    private Document post(final String action, final String payload, final Authentication authentication)
            throws CommunicationException {
        HttpRequest request = (HttpRequest) httpClient.newRequest(url);
        request.timeout(5, TimeUnit.SECONDS);
        request.method("POST");
        request.header("Content-Type", "\"text/xml; charset=utf-8\"");
        final String actionUrl = "\"http://purenetworks.com/HNAP1/" + action + "\"";
        request.header("SOAPAction", actionUrl);
        if (authentication != null) {
            request.header("HNAP_AUTH", computeHnapAuth(actionUrl, authentication.privateKey));
            request.header("Cookie", "uid=" + authentication.cookie);
        }
        request.content(new BytesContentProvider(payload.getBytes()));
        try {
            final ContentResponse response = request.send();
            final byte[] content = response.getContent();
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(content));
        } catch (InterruptedException | TimeoutException | ExecutionException | SAXException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw new CommunicationException("Post request failed: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            logger.error("Failed to create XML parser.");
            return null;
        }
    }

    private String getElementByTagName(final Document document, final String name) throws CommunicationException {
        NodeList items = document.getDocumentElement().getElementsByTagName(name);
        final int n = items.getLength();
        if (n != 1) {
            logger.error("Failed to get element with tag name '{}'.", name);
            authentication = null; // force re-authentication.
            throw new CommunicationException(
                    "Expected exactly one node with name '" + name + "', but got " + n + " many.");
        } else {
            return items.item(0).getTextContent();
        }
    }

    private String computeHnapAuth(final String url, final String privateKey) {
        if (url.endsWith("/Login")) {
            return "\"" + privateKey + "\"";
        } else {
            final String timeStamp = Long.toString(Instant.now().getEpochSecond() / (int) 10e6);
            return calculateHMAC(timeStamp + url, privateKey) + " " + timeStamp;
        }
    }

    private static final String HMAC_MD5 = "HmacMD5";

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02X", b);
        }
        final String hmac = formatter.toString();
        formatter.close();
        return hmac;
    }

    private String calculateHMAC(String data, String key) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_MD5);
        Mac mac;
        try {
            mac = Mac.getInstance(HMAC_MD5);
            mac.init(secretKeySpec);
            return toHexString(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to find hashing algorithm.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unexpected internal error.");
        } catch (InvalidKeyException e) {
            logger.error("Invalid private key.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unexpected internal error.");
        }
        return null;
    }

    private ChannelUID getChannelUID(final String name) {
        Channel channel = thing.getChannel(name);
        if (channel == null) {
            logger.error("Did not find channel '{}'.", name);
            return null;
        } else {
            return channel.getUID();
        }
    }

    public State getNewChannelState(final String channelID) {
        if (thing.getStatus().equals(ThingStatus.ONLINE)) {
            switch (channelID) {
                case DLinkSmartHomeBindingConstants.CURRENT_CONSUMPTION:
                    return getCurrentConsuption();
                case DLinkSmartHomeBindingConstants.TOTAL_CONSUMPTION:
                    return getTotalConsumption();
                case DLinkSmartHomeBindingConstants.TEMPERATURE:
                    return getTemperature();
                case DLinkSmartHomeBindingConstants.STATE:
                    return getState();
                default:
                    logger.error("Unexpected channel: '{}'.", channelID);
                    throw new IllegalArgumentException("Unexpected channel.");
            }
        } else {
            return UnDefType.UNDEF;
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);

        // force re-authentication before the next action.
        authentication = null;

        // the configuration may be correct.
        // if it was incorrect, the `authenticate()`-method will set the flag.
        invalid_configuration = false;
    }

    static String requestBody(final String action, final String params) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Body>" + String.format("<%s xmlns=\"http://purenetworks.com/HNAP1/\">", action) + params
                + String.format("</%s>", action) + "</soap:Body>" + "</soap:Envelope>";
    }

    static String authPayload(final String loginPassword) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soap:Body>"
                + "<Login xmlns=\"http://purenetworks.com/HNAP1/\">" + "<Action>login</Action>" + "<Username>" + USER
                + "</Username>" + "<LoginPassword>" + loginPassword + "</LoginPassword>" + "<Captcha/>" + "</Login>"
                + "</soap:Body>" + "</soap:Envelope>";
    }

    static final String INITIAL_AUTH_PAYLOAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soap:Body>" + "<Login xmlns=\"http://purenetworks.com/HNAP1/\">" + "<Action>request</Action>"
            + "<Username>admin</Username>" + "<LoginPassword/>" + "<Captcha/>" + "</Login>" + "</soap:Body>"
            + "</soap:Envelope>";
}
