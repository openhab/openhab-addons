/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyBluHandler} implements the thing handler for the BLU devices
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBluHandler extends ShellyBaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellyBluHandler.class);

    public ShellyBluHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, final ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient);
    }

    @Override
    public void initialize() {
        LOGGER.debug("Thing is using  {}", this.getClass());
        super.initialize();
    }
}
