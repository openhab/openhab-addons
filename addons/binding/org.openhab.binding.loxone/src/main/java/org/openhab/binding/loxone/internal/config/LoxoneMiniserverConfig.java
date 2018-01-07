/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.config;

import org.openhab.binding.loxone.handler.LoxoneMiniserverHandler;

/**
 * Configuration of a Loxone Miniserver ({@link LoxoneMiniserverHandler})
 *
 * @author Pawel Pieczul - Initial contribution
 *
 */
public class LoxoneMiniserverConfig {
    /**
     * Host address or IP of the Miniserver
     */
    public String host;
    /**
     * Port of web service of the Miniserver
     */
    public int port;
    /**
     * User name used to log into the Miniserver
     */
    public String user;
    /**
     * Password used to log into the Miniserver
     */
    public String password;
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
}
