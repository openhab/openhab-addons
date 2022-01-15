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
package org.openhab.binding.denonmarantz.internal.connector;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.denonmarantz.internal.DenonMarantzState;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
import org.openhab.binding.denonmarantz.internal.connector.http.DenonMarantzHttpConnector;
import org.openhab.binding.denonmarantz.internal.connector.telnet.DenonMarantzTelnetConnector;

/**
 * Returns the connector based on the configuration.
 * Currently there are 2 types: HTTP and Telnet
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 */
public class DenonMarantzConnectorFactory {

    public DenonMarantzConnector getConnector(DenonMarantzConfiguration config, DenonMarantzState state,
            ScheduledExecutorService scheduler, HttpClient httpClient, String thingUID) {
        if (config.isTelnet()) {
            return new DenonMarantzTelnetConnector(config, state, scheduler, thingUID);
        } else {
            return new DenonMarantzHttpConnector(config, state, scheduler, httpClient);
        }
    }
}
