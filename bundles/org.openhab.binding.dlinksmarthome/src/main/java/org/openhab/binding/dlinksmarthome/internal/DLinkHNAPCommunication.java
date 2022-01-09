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
package org.openhab.binding.dlinksmarthome.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The {@link DLinkHNAPCommunication} is responsible for communicating with D-Link
 * Smart Home devices using the HNAP interface.
 *
 * This abstract class handles login and authentication which is common between devices.
 *
 * Reverse engineered from Login.html and soapclient.js retrieved from the device.
 *
 * @author Mike Major - Initial contribution
 */
public abstract class DLinkHNAPCommunication {

    // SOAP actions
    private static final String LOGIN_ACTION = "\"http://purenetworks.com/HNAP1/LOGIN\"";

    // Strings used more than once
    private static final String LOGIN = "LOGIN";
    private static final String ACTION = "Action";
    private static final String USERNAME = "Username";
    private static final String LOGINPASSWORD = "LoginPassword";
    private static final String CAPTCHA = "Captcha";
    private static final String ADMIN = "Admin";
    private static final String LOGINRESULT = "LOGINResult";
    private static final String COOKIE = "Cookie";

    /**
     * HNAP XMLNS
     */
    protected static final String HNAP_XMLNS = "http://purenetworks.com/HNAP1";
    /**
     * The SOAP action HTML header
     */
    protected static final String SOAPACTION = "SOAPAction";
    /**
     * OK represents a successful action
     */
    protected static final String OK = "OK";

    /**
     * Use to log connection issues
     */
    private final Logger logger = LoggerFactory.getLogger(DLinkHNAPCommunication.class);

    private URI uri;
    private final HttpClient httpClient;
    private final String pin;
    private String privateKey;

    private DocumentBuilder parser;
    private SOAPMessage requestAction;
    private SOAPMessage loginAction;

    private HNAPStatus status = HNAPStatus.INITIALISED;

    /**
     * Indicates the status of the HNAP interface
     *
     */
    protected enum HNAPStatus {
        /**
         * Ready to start communication with device
         */
        INITIALISED,
        /**
         * Successfully logged in to device
         */
        LOGGED_IN,
        /**
         * Problem communicating with device
         */
        COMMUNICATION_ERROR,
        /**
         * Internal error
         */
        INTERNAL_ERROR,
        /**
         * Error due to unsupported firmware
         */
        UNSUPPORTED_FIRMWARE,
        /**
         * Error due to invalid pin code
         */
        INVALID_PIN
    }

    /**
     * Use {@link #getHNAPStatus()} to determine the status of the HNAP connection
     * after construction.
     *
     * @param ipAddress
     * @param pin
     */
    public DLinkHNAPCommunication(final String ipAddress, final String pin) {
        this.pin = pin;

        httpClient = new HttpClient();

        try {
            uri = new URI("http://" + ipAddress + "/HNAP1");
            httpClient.start();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            parser = dbf.newDocumentBuilder();

            final MessageFactory messageFactory = MessageFactory.newInstance();
            requestAction = messageFactory.createMessage();
            loginAction = messageFactory.createMessage();

            buildRequestAction();
            buildLoginAction();
        } catch (final SOAPException e) {
            logger.debug("DLinkHNAPCommunication - Internal error", e);
            status = HNAPStatus.INTERNAL_ERROR;
        } catch (final URISyntaxException e) {
            logger.debug("DLinkHNAPCommunication - Internal error", e);
            status = HNAPStatus.INTERNAL_ERROR;
        } catch (final ParserConfigurationException e) {
            logger.debug("DLinkHNAPCommunication - Internal error", e);
            status = HNAPStatus.INTERNAL_ERROR;
        } catch (final Exception e) {
            // Thrown by httpClient.start()
            logger.debug("DLinkHNAPCommunication - Internal error", e);
            status = HNAPStatus.INTERNAL_ERROR;
        }
    }

    /**
     * Stop communicating with the device
     */
    public void dispose() {
        try {
            httpClient.stop();
        } catch (final Exception e) {
            // Ignored
        }
    }

    /**
     * This is the first SOAP message used in the login process and is used to retrieve
     * the cookie, challenge and public key used for authentication.
     *
     * @throws SOAPException
     */
    private void buildRequestAction() throws SOAPException {
        requestAction.getSOAPHeader().detachNode();
        final SOAPBody soapBody = requestAction.getSOAPBody();
        final SOAPElement soapBodyElem = soapBody.addChildElement(LOGIN, "", HNAP_XMLNS);
        soapBodyElem.addChildElement(ACTION).addTextNode("request");
        soapBodyElem.addChildElement(USERNAME).addTextNode(ADMIN);
        soapBodyElem.addChildElement(LOGINPASSWORD);
        soapBodyElem.addChildElement(CAPTCHA);

        final MimeHeaders headers = requestAction.getMimeHeaders();
        headers.addHeader(SOAPACTION, LOGIN_ACTION);

        requestAction.saveChanges();
    }

    /**
     * This is the second SOAP message used in the login process and uses a password derived
     * from the challenge, public key and the device's pin code.
     *
     * @throws SOAPException
     */
    private void buildLoginAction() throws SOAPException {
        loginAction.getSOAPHeader().detachNode();
        final SOAPBody soapBody = loginAction.getSOAPBody();
        final SOAPElement soapBodyElem = soapBody.addChildElement(LOGIN, "", HNAP_XMLNS);
        soapBodyElem.addChildElement(ACTION).addTextNode("login");
        soapBodyElem.addChildElement(USERNAME).addTextNode(ADMIN);
        soapBodyElem.addChildElement(LOGINPASSWORD);
        soapBodyElem.addChildElement(CAPTCHA);

        final MimeHeaders headers = loginAction.getMimeHeaders();
        headers.addHeader(SOAPACTION, LOGIN_ACTION);
    }

    /**
     * Sets the password for the second login message based on the data received from the
     * first login message. Also sets the private key used to generate the authentication header.
     *
     * @param challenge
     * @param cookie
     * @param publicKey
     * @throws SOAPException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    private void setAuthenticationData(final String challenge, final String cookie, final String publicKey)
            throws SOAPException, InvalidKeyException, NoSuchAlgorithmException {
        final MimeHeaders loginHeaders = loginAction.getMimeHeaders();
        loginHeaders.setHeader(COOKIE, "uid=" + cookie);

        privateKey = hash(challenge, publicKey + pin);

        final String password = hash(challenge, privateKey);

        loginAction.getSOAPBody().getElementsByTagName(LOGINPASSWORD).item(0).setTextContent(password);
        loginAction.saveChanges();
    }

    /**
     * Used to hash the authentication data such as the login password and the authentication header
     * for the detection message.
     *
     * @param data
     * @param key
     * @return The hashed data
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private String hash(final String data, final String key) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac mac = Mac.getInstance("HMACMD5");
        final SecretKeySpec sKey = new SecretKeySpec(key.getBytes(), "ASCII");

        mac.init(sKey);
        final byte[] bytes = mac.doFinal(data.getBytes());

        final StringBuilder hashBuf = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            final String hex = Integer.toHexString(0xFF & bytes[i]).toUpperCase();
            if (hex.length() == 1) {
                hashBuf.append('0');
            }
            hashBuf.append(hex);
        }

        return hashBuf.toString();
    }

    /**
     * Output unexpected responses to the debug log and sets the FIRMWARE error.
     *
     * @param message
     * @param soapResponse
     */
    private void unexpectedResult(final String message, final Document soapResponse) {
        logUnexpectedResult(message, soapResponse);

        // Best guess when receiving unexpected responses
        status = HNAPStatus.UNSUPPORTED_FIRMWARE;
    }

    /**
     * Get the status of the HNAP interface
     *
     * @return the HNAP status
     */
    protected HNAPStatus getHNAPStatus() {
        return status;
    }

    /**
     * Sends the two login messages and stores the private key used to generate the
     * authentication header required for actions.
     *
     * Use {@link #getHNAPStatus()} to determine the status of the HNAP connection
     * after calling this method.
     *
     * @param timeout - Connection timeout in milliseconds
     */
    protected void login(final int timeout) {
        if (status != HNAPStatus.INTERNAL_ERROR) {
            try {
                Document soapResponse = sendReceive(requestAction, timeout);

                Node result = soapResponse.getElementsByTagName(LOGINRESULT).item(0);

                if (result != null && OK.equals(result.getTextContent())) {
                    final Node challengeNode = soapResponse.getElementsByTagName("Challenge").item(0);
                    final Node cookieNode = soapResponse.getElementsByTagName(COOKIE).item(0);
                    final Node publicKeyNode = soapResponse.getElementsByTagName("PublicKey").item(0);

                    if (challengeNode != null && cookieNode != null && publicKeyNode != null) {
                        setAuthenticationData(challengeNode.getTextContent(), cookieNode.getTextContent(),
                                publicKeyNode.getTextContent());

                        soapResponse = sendReceive(loginAction, timeout);
                        result = soapResponse.getElementsByTagName(LOGINRESULT).item(0);

                        if (result != null) {
                            if ("success".equals(result.getTextContent())) {
                                status = HNAPStatus.LOGGED_IN;
                            } else {
                                logger.debug("login - Check pin is correct");
                                // Assume pin code problem rather than a firmware change
                                status = HNAPStatus.INVALID_PIN;
                            }
                        } else {
                            unexpectedResult("login - Unexpected login response", soapResponse);
                        }
                    } else {
                        unexpectedResult("login - Unexpected request response", soapResponse);
                    }
                } else {
                    unexpectedResult("login - Unexpected request response", soapResponse);
                }
            } catch (final InvalidKeyException e) {
                logger.debug("login - Internal error", e);
                status = HNAPStatus.INTERNAL_ERROR;
            } catch (final NoSuchAlgorithmException e) {
                logger.debug("login - Internal error", e);
                status = HNAPStatus.INTERNAL_ERROR;
            } catch (final InterruptedException e) {
                status = HNAPStatus.COMMUNICATION_ERROR;
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                // Assume there has been some problem trying to send one of the messages
                if (status != HNAPStatus.COMMUNICATION_ERROR) {
                    logger.debug("login - Communication error", e);
                    status = HNAPStatus.COMMUNICATION_ERROR;
                }
            }
        }
    }

    /**
     * Sets the authentication headers for the action message. This should only be called
     * after a successful login.
     *
     * Use {@link #getHNAPStatus()} to determine the status of the HNAP connection
     * after calling this method.
     *
     * @param action - SOAP Action to add headers
     */
    protected void setAuthenticationHeaders(final SOAPMessage action) {
        if (status == HNAPStatus.LOGGED_IN) {
            try {
                final MimeHeaders loginHeaders = loginAction.getMimeHeaders();
                final MimeHeaders actionHeaders = action.getMimeHeaders();

                actionHeaders.setHeader(COOKIE, loginHeaders.getHeader(COOKIE)[0]);

                final String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
                final String auth = hash(timeStamp + actionHeaders.getHeader(SOAPACTION)[0], privateKey) + " "
                        + timeStamp;
                actionHeaders.setHeader("HNAP_AUTH", auth);

                action.saveChanges();
            } catch (final InvalidKeyException e) {
                logger.debug("setAuthenticationHeaders - Internal error", e);
                status = HNAPStatus.INTERNAL_ERROR;
            } catch (final NoSuchAlgorithmException e) {
                logger.debug("setAuthenticationHeaders - Internal error", e);
                status = HNAPStatus.INTERNAL_ERROR;
            } catch (final SOAPException e) {
                // No communication happening so assume system error
                logger.debug("setAuthenticationHeaders - Internal error", e);
                status = HNAPStatus.INTERNAL_ERROR;
            }
        }
    }

    /**
     * Send the SOAP message using Jetty HTTP client. Jetty is used in preference to
     * HttpURLConnection which can result in the HNAP interface becoming unresponsive.
     *
     * @param action - SOAP Action to send
     * @param timeout - Connection timeout in milliseconds
     * @return The result
     * @throws IOException
     * @throws SOAPException
     * @throws SAXException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    protected Document sendReceive(final SOAPMessage action, final int timeout) throws IOException, SOAPException,
            SAXException, InterruptedException, TimeoutException, ExecutionException {
        Document result;

        final Request request = httpClient.POST(uri);
        request.timeout(timeout, TimeUnit.MILLISECONDS);

        final Iterator<?> it = action.getMimeHeaders().getAllHeaders();
        while (it.hasNext()) {
            final MimeHeader header = (MimeHeader) it.next();
            request.header(header.getName(), header.getValue());
        }

        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            action.writeTo(os);
            request.content(new BytesContentProvider(os.toByteArray()));
            final ContentResponse response = request.send();
            try (final ByteArrayInputStream is = new ByteArrayInputStream(response.getContent())) {
                result = parser.parse(is);
            }
        }

        return result;
    }

    /**
     * Output unexpected responses to the debug log.
     *
     * @param message
     * @param soapResponse
     */
    protected void logUnexpectedResult(final String message, final Document soapResponse) {
        // No point formatting for output if debug logging is not enabled
        if (logger.isDebugEnabled()) {
            try {
                final TransformerFactory transFactory = TransformerFactory.newInstance();
                final Transformer transformer = transFactory.newTransformer();
                final StringWriter buffer = new StringWriter();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.transform(new DOMSource(soapResponse), new StreamResult(buffer));
                logger.debug("{} : {}", message, buffer);
            } catch (final TransformerException e) {
                logger.debug("{}", message);
            }
        }
    }
}
