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
package org.openhab.binding.loxone.internal;

/**
 * Configuration of a Loxone Miniserver ({@link LxServerHandler})
 *
 * @author Pawel Pieczul - Initial contribution
 *
 */
public class LxBindingConfiguration {
    /**
     * Host address or IP of the Miniserver
     */
    public String host;
    /**
     * Port of HTTP web service of the Miniserver
     */
    public int port;
    /**
     * Port of HTTPS web service of the Miniserver
     */
    public int httpsPort;
    /**
     * User name used to log into the Miniserver
     */
    public String user;
    /**
     * Password used to log into the Miniserver
     */
    public String password;
    /**
     * Authentication token acquired from the Miniserver
     */
    public String authToken;
    /**
     * Time in seconds between binding initialization and first connection attempt
     */
    public int firstConDelay;
    /**
     * Time in seconds between sending two consecutive keep-alive messages
     */
    public int keepAlivePeriod;
    /**
     * Time in seconds between failed websocket connect attempts
     */
    public int connectErrDelay;
    /**
     * Time to wait for Miniserver response to a request sent from the binding
     */
    public int responseTimeout;
    /**
     * Time in seconds between user login error as a result of wrong name/password or no authority and next connection
     * attempt
     */
    public int userErrorDelay;
    /**
     * Time in seconds between connection close (as a result of some communication error) and next connection attempt
     */
    public int comErrorDelay;
    /**
     * Websocket client's max binary message size in kB
     */
    public int maxBinMsgSize;
    /**
     * Websocket client's max text message size in kB
     */
    public int maxTextMsgSize;
    /**
     * Authentication method (0-auto, 1-hash, 2-token)
     */
    public int authMethod;
    /**
     * WebSocket connection type (0-auto, 1-HTTPS, 2-HTTP)
     */
    public int webSocketType;
}
