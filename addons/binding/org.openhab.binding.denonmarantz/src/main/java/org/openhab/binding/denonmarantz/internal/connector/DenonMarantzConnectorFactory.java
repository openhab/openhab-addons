/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
            ScheduledExecutorService scheduler, HttpClient httpClient) {
        if (config.isTelnet()) {
            return new DenonMarantzTelnetConnector(config, state, scheduler);
        } else {
            return new DenonMarantzHttpConnector(config, state, scheduler, httpClient);
        }
    }

}
