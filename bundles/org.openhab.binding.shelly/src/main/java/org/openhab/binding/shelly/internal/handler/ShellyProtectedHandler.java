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
package org.openhab.binding.shelly.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.coap.ShellyCoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.thing.Thing;

/**
 * The {@link ShellyProtectedHandler} implements a dummy handler for password protected devices.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyProtectedHandler extends ShellyBaseHandler {
    /**
     * Constructor
     *
     * @param thing The thing passed by the HandlerFactory
     * @param bindingConfig configuration of the binding
     * @param coapServer coap server instance
     * @param localIP local IP of the openHAB host
     * @param httpPort port of the openHAB HTTP API
     */
    public ShellyProtectedHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, final ShellyCoapServer coapServer, final String localIP,
            int httpPort, final HttpClient httpClient) {
        super(thing, translationProvider, bindingConfig, coapServer, localIP, httpPort, httpClient);
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
