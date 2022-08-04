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
package org.openhab.binding.liqiudcheck.internal.httpClient;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.liqiudcheck.internal.LiqiudCheckConfiguration;
import org.openhab.binding.liqiudcheck.internal.LiqiudCheckHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LiqiudCheckBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class LiquidCheckHttpClient {
    
    private final Logger logger = LoggerFactory.getLogger(LiqiudCheckHandler.class);
    private final HttpClient client;
    private final LiqiudCheckConfiguration config;

    public LiquidCheckHttpClient(LiqiudCheckConfiguration config) {
        this.config = config;
        client = new HttpClient();

        try {
            client.setName("LiquidCheckHttpClient");
            client.start();
        } catch (Exception e) {
            logger.error("Couldn't start Client! Exception: " + e.toString());
            return;
        }
    }
}
