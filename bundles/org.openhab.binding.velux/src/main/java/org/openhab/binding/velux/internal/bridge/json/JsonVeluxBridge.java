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
package org.openhab.binding.velux.internal.bridge.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.VeluxBridge;
import org.openhab.binding.velux.internal.bridge.common.BridgeAPI;
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * JSON-based 2nd Level I/O interface towards the <B>Velux</B> bridge.
 * <P>
 * It provides methods for pre- and postcommunication
 * as well as a common method for the real communication.
 * <P>
 * The following class access methods exist:
 * <UL>
 * <LI>{@link VeluxBridge#bridgeLogin} for pre-communication,</LI>
 * <LI>{@link VeluxBridge#bridgeLogout} for post-communication,</LI>
 * <LI>{@link VeluxBridge#bridgeCommunicate} as method for the common communication.</LI>
 * </UL>
 * <P>
 * As root of several inheritance levels it predefines an
 * interfacing method {@link VeluxBridge#bridgeAPI} which
 * has to be implemented by any kind of protocol-specific
 * communication returning the appropriate base (1st) level
 * communication method as well as any other gateway
 * interaction.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class JsonVeluxBridge extends VeluxBridge {
    private final Logger logger = LoggerFactory.getLogger(JsonVeluxBridge.class);

    /**
     * Timestamp of last communication in milliseconds.
     *
     */
    private long lastCommunicationInMSecs = 0;

    /**
     * Timestamp of last successful communication in milliseconds.
     *
     */
    private long lastSuccessfulCommunicationInMSecs = 0;

    /**
     * Handler passing the interface methods to other classes.
     * Can be accessed via method {@link org.openhab.binding.velux.internal.bridge.common.BridgeAPI BridgeAPI}.
     *
     */
    private BridgeAPI bridgeAPI;

    /**
     * Constructor.
     * <P>
     * Inherits the initialization of the binding-wide instance for dealing for common informations and
     * initializes the Velux bridge connectivity settings.
     *
     * @param bridgeInstance refers to the binding-wide instance for dealing for common informations.
     */
    public JsonVeluxBridge(VeluxBridgeHandler bridgeInstance) {
        super(bridgeInstance);
        logger.trace("JsonVeluxBridge(constructor) called.");
        bridgeAPI = new JsonBridgeAPI(bridgeInstance);
        supportedProtocols = new TreeSet<>();
        supportedProtocols.add("http");
        supportedProtocols.add("https");
        logger.trace("JsonVeluxBridge(constructor) done.");
    }

    /**
     * Provides information about the base-level communication method and
     * any kind of available gateway interactions.
     * <P>
     * <B>Note:</B> the implementation within this class {@link JsonVeluxBridge} as inherited from {@link VeluxBridge}
     * will return the protocol-specific class implementations.
     * <P>
     * The information will be initialized by the corresponding API class {@link JsonBridgeAPI}.
     *
     * @return <b>bridgeAPI</b> of type {@link BridgeAPI} contains all possible methods.
     */
    @Override
    public BridgeAPI bridgeAPI() {
        logger.trace("bridgeAPI() called.");
        return bridgeAPI;
    }

    /**
     * <B>Method as implementation of abstract superclass method.</B>
     * <P>
     * Initializes a client/server communication towards <b>Velux</b> veluxBridge
     * based on the Basic I/O interface {@link #io} and parameters
     * passed as arguments (see below).
     *
     * @param communication Structure of interface type {@link JsonBridgeCommunicationProtocol} describing the intended
     *            communication, that is request and response interactions as well as appropriate URL definition.
     * @param useAuthentication boolean flag to decide whether to use authenticated communication.
     * @return <b>success</b> of type boolean which signals the success of the communication.
     *
     */
    @Override
    protected boolean bridgeDirectCommunicate(BridgeCommunicationProtocol communication, boolean useAuthentication) {
        logger.trace("bridgeDirectCommunicate(BCP: {},{}authenticated) called.", communication.name(),
                useAuthentication ? "" : "un");
        return bridgeDirectCommunicate((JsonBridgeCommunicationProtocol) communication, useAuthentication);
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last (potentially faulty) communication.
     *
     * @return timestamp in milliseconds.
     */
    @Override
    public long lastCommunication() {
        return lastCommunicationInMSecs;
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last successful communication.
     *
     * @return timestamp in milliseconds.
     */
    @Override
    public long lastSuccessfulCommunication() {
        return lastSuccessfulCommunicationInMSecs;
    }

    /**
     * Initializes a client/server communication towards <b>Velux</b> veluxBridge
     * based on the Basic I/O interface {@link VeluxBridge} and parameters
     * passed as arguments (see below).
     *
     * @param communication Structure of interface type {@link JsonBridgeCommunicationProtocol} describing the
     *            intended
     *            communication,
     *            that is request and response interactions as well as appropriate URL definition.
     * @param useAuthentication boolean flag to decide whether to use authenticated communication.
     * @return <b>response</b> of type boolean will indicate the success of the communication.
     */
    private synchronized boolean bridgeDirectCommunicate(JsonBridgeCommunicationProtocol communication,
            boolean useAuthentication) {
        logger.trace("bridgeDirectCommunicate({},{}authenticated) called.", communication.name(),
                useAuthentication ? "" : "un");

        String sapURL = this.bridgeInstance.veluxBridgeConfiguration().protocol.concat("://")
                .concat(this.bridgeInstance.veluxBridgeConfiguration().ipAddress).concat(":")
                .concat(Integer.toString(this.bridgeInstance.veluxBridgeConfiguration().tcpPort))
                .concat(communication.getURL());
        logger.trace("bridgeCommunicate(): using SAP {}.", sapURL);
        Object getRequest = communication.getObjectOfRequest();
        Class<?> classOfResponse = communication.getClassOfResponse();
        Object response;

        try {
            if (useAuthentication) {
                response = ioAuthenticated(sapURL, authenticationToken, getRequest, classOfResponse);
            } else {
                response = ioUnauthenticated(sapURL, getRequest, classOfResponse);
            }
            if (response == null) {
                throw new IOException("Failed to create 'response' object");
            }
            communication.setResponse(response);
            logger.trace("bridgeCommunicate(): communication result is {}, returning details.",
                    communication.isCommunicationSuccessful());
            return true;
        } catch (IOException ioe) {
            logger.warn("bridgeCommunicate(): Exception occurred on accessing {}: {}.", sapURL, ioe.getMessage());
            return false;
        } catch (JsonSyntaxException jse) {
            logger.warn("bridgeCommunicate(): Exception occurred on (de-)serialization during accessing {}: {}.",
                    sapURL, jse.getMessage());
            return false;
        }
    }

    /**
     * Base level communication with the <b>Velux</b> bridge.
     *
     * @param <T> This describes the request type parameter.
     * @param <U> This describes the response type parameter.
     * @param url as String describing the Service Access Point location i.e. http://localhost/api .
     * @param authentication as String providing the Authentication token to be passed with the request header.
     * @param request as Object representing the structure of the message request body to be converted into
     *            JSON.
     * @param classOfResponse as Class representing the expected structure of the message response body to be converted
     *            from JSON.
     * @return <b>response</b> of type Object containing all resulting informations, i.e. device status, errors a.s.o.
     *         Will
     *         return
     *         <B>null</B> in case of communication or decoding error.
     * @throws java.io.IOException in case of continuous communication I/O failures.
     * @throws JsonSyntaxException in case of unusual communication failures.
     */
    private <T, U> T io(String url, String authentication, U request, Class<T> classOfResponse)
            throws JsonSyntaxException, IOException {
        /** Local handles */
        int retryCount = 0;

        lastCommunicationInMSecs = System.currentTimeMillis();
        do {
            try {
                Gson gson = new Gson();
                String jsonRequest = gson.toJson(request);
                logger.trace("io() to {} using request {}.", url, jsonRequest);

                Properties headerItems = new Properties();
                if (authentication.length() > 0) {
                    headerItems.setProperty("Authorization", String.format("Bearer %s", authentication));
                }
                InputStream content = new ByteArrayInputStream(jsonRequest.getBytes(StandardCharsets.UTF_8));

                String jsonResponse = HttpUtil.executeUrl("PUT", url, headerItems, content, "application/json",
                        this.bridgeInstance.veluxBridgeConfiguration().timeoutMsecs);
                if (jsonResponse == null) {
                    throw new IOException("transport error");
                }
                logger.trace("io(): wait time {} msecs.", this.bridgeInstance.veluxBridgeConfiguration().timeoutMsecs);
                // Give the bridge some time to breathe
                try {
                    Thread.sleep(this.bridgeInstance.veluxBridgeConfiguration().timeoutMsecs);
                } catch (InterruptedException ie) {
                    logger.trace("io() wait interrupted.");
                }
                logger.trace("io() got response {}.", jsonResponse.replaceAll("\\p{C}", "."));
                jsonResponse = jsonResponse.replaceAll("^.+,\n", "");
                logger.trace("io() cleaned response {}.", jsonResponse);
                T response = gson.fromJson(jsonResponse, classOfResponse);
                lastCommunicationInMSecs = lastSuccessfulCommunicationInMSecs = System.currentTimeMillis();
                return response;
            } catch (IOException ioe) {
                logger.trace("io(): Exception occurred during I/O: {}.", ioe.getMessage());
                // Error Retries with Exponential Backoff
                long waitTime = ((long) Math.pow(2, retryCount)
                        * this.bridgeInstance.veluxBridgeConfiguration().timeoutMsecs);
                logger.trace("io(): wait time {} msecs.", waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    logger.trace("io() wait interrupted.");
                }
            } catch (JsonSyntaxException jse) {
                logger.info("io(): Exception occurred on deserialization: {}, aborting.", jse.getMessage());
                throw jse;
            }
        } while (retryCount++ < this.bridgeInstance.veluxBridgeConfiguration().retries);
        throw new IOException(String.format("io(): socket I/O failed (%d times).",
                this.bridgeInstance.veluxBridgeConfiguration().retries));
    }

    /**
     * Initializes an authenticated communication with the {@link JsonVeluxBridge <b>Velux</b> bridge}.
     *
     * @param <T> This describes the request type parameter.
     * @param <U> This describes the response type parameter.
     * @param url as String describing the Service Access Point location i.e. http://localhost/api .
     * @param authentication as String providing the Authentication token to be passed with the request header.
     * @param request as Object representing the structure of the message request body to be converted into
     *            JSON.
     * @param classOfResponse as Class representing the expected structure of the message response body to be converted
     *            from JSON.
     * @return <b>response</b> of type T containing all resulting informations, i.e. device status, errors a.s.o. Will
     *         return
     *         <B>null</B> in case of communication or decoding error.
     * @throws java.io.IOException in case of continuous communication I/O failures.
     * @throws JsonSyntaxException in case of unusual communication failures.
     */
    private <T, U> T ioAuthenticated(String url, String authentication, U request, Class<T> classOfResponse)
            throws JsonSyntaxException, IOException {
        return io(url, authentication, request, classOfResponse);
    }

    /**
     * Initializes an unauthenticated communication with the {@link JsonVeluxBridge <b>Velux</b> bridge}.
     *
     * @param <T> This describes the request type parameter.
     * @param <U> This describes the response type parameter.
     * @param url as String describing the Service Access Point location i.e. http://localhost/api .
     * @param request as Object representing the structure of the message request body to be converted into
     *            JSON.
     * @param classOfResponse as Class representing the expected structure of the message response body to be converted
     *            from JSON.
     * @return <b>response</b> of type Object containing all resulting informations, i.e. device status, errors a.s.o.
     *         Will
     *         return
     *         <B>null</B> in case of communication or decoding error.
     * @throws java.io.IOException in case of continuous communication I/O failures.
     * @throws JsonSyntaxException in case of unusual communication failures.
     */
    private <T, U> T ioUnauthenticated(String url, U request, Class<T> classOfResponse)
            throws JsonSyntaxException, IOException {
        return io(url, "", request, classOfResponse);
    }
}
