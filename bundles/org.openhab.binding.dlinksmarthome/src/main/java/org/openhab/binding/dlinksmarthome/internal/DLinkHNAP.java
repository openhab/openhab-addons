package org.openhab.binding.dlinksmarthome.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Formatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.measure.Unit;
import javax.naming.CommunicationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link DLinkHNAP} class encapsulates the HNAP implementation for DLink thing handlers.
 *
 * @author Pascal Bies - Initial contribution
 */
public abstract class DLinkHNAP {

    abstract public void poll();

    abstract public void handleCommandAuthenticated(final ChannelUID channelUID, final Command command);

    protected void updateStatus(ThingStatus status) {
        updateStatus(status, ThingStatusDetail.NONE);
    }

    protected void updateStatus(ThingStatus status, ThingStatusDetail detail) {
        updateStatus(status, detail, null);
    }

    protected abstract void updateStatus(ThingStatus status, ThingStatusDetail detail, final String message);

    private Logger logger = LoggerFactory.getLogger(DLinkHNAP.class);
    private String pin;
    private String url;
    private static final String USER = "admin";
    private final HttpClient httpClient;
    private ScheduledFuture<?> pollFuture;
    private boolean invalidConfiguration = false;
    Authentication authentication;
    MessageFactory soapMessageFactory;
    SOAPFactory soapFactory;
    private static final int DETECT_POLL_S = 1;

    private final Runnable poller = new Runnable() {
        @Override
        public void run() {
            if (authenticate()) {
                updateStatus(ThingStatus.ONLINE);
                poll();
            }
        }
    };

    private static class Authentication {
        public Authentication(final String privateKey, final String cookie) {
            this.privateKey = privateKey;
            this.cookie = cookie;
        }

        public String privateKey;
        public String cookie;
    }

    public DLinkHNAP() {
        httpClient = new HttpClient();
    }

    public void start(ScheduledExecutorService scheduler) {
        try {
            httpClient.start();
            pollFuture = scheduler.scheduleWithFixedDelay(poller, 0, DETECT_POLL_S, TimeUnit.SECONDS);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unexpected internal error.");
            logger.error("Failed to start http client or scheduler.");
        }
    }

    public void stop() {
        if (pollFuture != null) {
            pollFuture.cancel(true);
        }
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.error("Failed to stop http client.");
        }
    }

    public void reset() {
        // force re-authentication before the next action.
        authentication = null;

        // the configuration may be correct.
        // if it was incorrect, the `authenticate()`-method will set the flag.
        invalidConfiguration = false;
    }

    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (authenticate()) {
            handleCommandAuthenticated(channelUID, command);
        }
    }

    public void setPin(final String pin) {
        this.pin = pin;
    }

    public void setIp(final String ip) {
        this.url = "http://" + ip + "/HNAP1/";
    }

    private SOAPElement moduleParameters(final String moduleID) throws SOAPException {
        return soapFactory.createElement("ModuleID").addTextNode(moduleID);
    }

    public String queryStringState(final String action, final String name, final String module) {
        try {
            final Document document = post(action, newMessage(action, moduleParameters(module)), authentication);
            return getElementByTagName(document, name);
        } catch (CommunicationException e) {
            logger.error("Failed to get quantity (communication error).");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (SOAPException e) {
            logger.error("Unexpected internal error.");
            updateStatus(ThingStatus.OFFLINE);
        }
        return "";
    }

    public State queryOnOffState(final String action, final String name, final String module) {
        final String state = queryStringState(action, name, module);
        if ("true".contentEquals(state)) {
            return OnOffType.ON;
        } else if ("false".equals(state)) {
            return OnOffType.OFF;
        } else {
            logger.error("Unexpected state: {}", state);
        }
        return UnDefType.UNDEF;
    }

    public State queryUnitState(final String action, final String name, final String module, final Unit<?> unit) {
        final String s = queryStringState(action, name, module);
        try {
            if (s == null) {
                return UnDefType.UNDEF;
            } else {
                return new QuantityType<>(Double.valueOf(s), unit);
            }
        } catch (NumberFormatException e) {
            logger.error("Unexpected number format: {}", s);
        }
        return UnDefType.UNDEF;
    }

    public void setState(final String action, final String name, final String moduleID, final String nick,
            final String description, final OnOffType state) {
        setState(action, name, moduleID, nick, description, OnOffType.ON.equals(state) ? "true" : "false");
    }

    public void setState(final String action, final String name, final String moduleID, final String nick,
            final String description, final String state) {
        try {
            final SOAPMessage message = newMessage(action, moduleParameters(moduleID),
                    soapFactory.createElement("NickName").addTextNode(nick),
                    soapFactory.createElement("Description").addTextNode(description),
                    soapFactory.createElement("OPStatus").addTextNode(state));
            final Document document = post(action, message, authentication);
            final String response = getElementByTagName(document, name);
            if (!"ok".equals(response.toLowerCase())) {
                logger.error("Failed to set state.");
            }
        } catch (SOAPException e) {
            logger.error("Unexpected internal error.");
            updateStatus(ThingStatus.OFFLINE);
        } catch (Exception e) {
            // pass
        }
    }

    private SOAPMessage newMessage(final String action, final SOAPElement... params) throws SOAPException {
        if (soapMessageFactory == null) {
            soapMessageFactory = MessageFactory.newInstance();
        }
        final SOAPMessage message = soapMessageFactory.createMessage();
        message.getSOAPHeader().detachNode();
        final SOAPElement actionElement = message.getSOAPBody().addChildElement(action, "",
                "http://purenetworks.com/HNAP1/");
        for (SOAPElement param : params) {
            actionElement.addChildElement(param);
        }
        return message;
    }

    private SOAPElement newSOAPElement(final String name) throws SOAPException {
        return newSOAPElement(name, null);
    }

    private SOAPElement newSOAPElement(final String name, final String text) throws SOAPException {
        if (soapFactory == null) {
            soapFactory = SOAPFactory.newInstance();
        }
        SOAPElement element = soapFactory.createElement(name);
        if (text != null) {
            element.addTextNode(text);
        }
        return element;
    }

    private boolean authenticate() {
        if (invalidConfiguration) {
            // If the configuration (i.e., PIN) is invalid, trying to long in does not make sense.
            authentication = null; // it should be null already, but for sake of consistency...
            return false;
        } else if (authentication != null) {
            // We are already authenticated, nothing to do.
            // If you want to force re-authentication, set this.authentication = null.
            return true;
        }

        try {
            final SOAPMessage initialAuthPayload = newMessage("Login", newSOAPElement("Action", "request"),
                    newSOAPElement("Username", USER), newSOAPElement("LoginPassword"), newSOAPElement("Captcha"));

            final Document challengeDocument = post("Login", initialAuthPayload);
            final String challenge = getElementByTagName(challengeDocument, "Challenge");
            final String cookie = getElementByTagName(challengeDocument, "Cookie");
            final String publicKey = getElementByTagName(challengeDocument, "PublicKey");
            final String privateKey = calculateHMAC(challenge, publicKey + pin);
            final String loginPassword = calculateHMAC(challenge, privateKey);

            authentication = new Authentication(privateKey, cookie);
            final SOAPMessage payload = newMessage("Login", newSOAPElement("Action", "login"),
                    newSOAPElement("Username", USER), newSOAPElement("LoginPassword", loginPassword),
                    newSOAPElement("Captcha"));
            final Document loginDocument = post("Login", payload, authentication);
            final String loginStatus = getElementByTagName(loginDocument, "LoginResult").toLowerCase();
            if ("success".equals(loginStatus)) {
                updateStatus(ThingStatus.ONLINE);
                invalidConfiguration = false;
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Failed to authenticate.");
                invalidConfiguration = true;
                authentication = null;
                return false;
            }
        } catch (CommunicationException e) {
            authentication = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to authenticate.");
            return false;
        } catch (SOAPException e) {
            authentication = null;
            logger.error("Unexpected internal error.");
            updateStatus(ThingStatus.OFFLINE);
            return false;
        }
    }

    private Document post(final String action, final SOAPMessage message) throws CommunicationException {
        return post(action, message, null);
    }

    private Document post(final String action, final SOAPMessage message, final Authentication authentication)
            throws CommunicationException {
        Request request = httpClient.newRequest(url);
        request.timeout(5, TimeUnit.SECONDS);
        request.method("POST");
        request.header("Content-Type", "\"text/xml; charset=utf-8\"");
        final String actionUrl = "\"http://purenetworks.com/HNAP1/" + action + "\"";
        request.header("SOAPAction", actionUrl);
        if (authentication != null) {
            request.header("HNAP_AUTH", computeHnapAuth(actionUrl, authentication.privateKey));
            request.header("Cookie", "uid=" + authentication.cookie);
        }
        try {
            request.content(new BytesContentProvider(encodeSOAPMessage(message)));
            final ContentResponse response = request.send();
            final byte[] content = response.getContent();
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(content));
        } catch (InterruptedException | TimeoutException | ExecutionException | SAXException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw new CommunicationException("Post request failed: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            logger.error("Failed to create XML parser.");
            updateStatus(ThingStatus.OFFLINE);
            return null;
        }
    }

    private byte[] encodeSOAPMessage(final SOAPMessage message) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            message.writeTo(stream);
        } catch (SOAPException | IOException e) {
            logger.error("Unexpected internal Error.");
            updateStatus(ThingStatus.OFFLINE);
        }
        return stream.toByteArray();
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
}
