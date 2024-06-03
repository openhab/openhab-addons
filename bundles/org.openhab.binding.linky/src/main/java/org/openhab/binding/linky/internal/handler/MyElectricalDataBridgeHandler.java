/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link MyElectricalDataBridgeHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class MyElectricalDataBridgeHandler extends ApiBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(MyElectricalDataBridgeHandler.class);

    private static final String BASE_URL = "https://www.myelectricaldata.fr/";

    private static final String CONTRACT_URL = BASE_URL + "contracts/%s/";
    private static final String IDENTITY_URL = BASE_URL + "identity/%s/";
    private static final String CONTACT_URL = BASE_URL + "contact/%s/";
    private static final String ADDRESS_URL = BASE_URL + "addresses/%s/";
    private static final String MEASURE_DAILY_CONSUMPTION_URL = BASE_URL + "daily_consumption/%s/start/%s/end/%s";
    private static final String MEASURE_MAX_POWER_URL = BASE_URL + "daily_consumption_max_power/%s/start/%s/end/%s";

    private static final String TEMPO_URL = BASE_URL + "rte/tempo/%s/%s";

    public MyElectricalDataBridgeHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            final @Reference ThingRegistry thingRegistry, ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, thingRegistry, componentContext, gson);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Netatmo API bridge handler.");

        super.dispose();
    }

    @Override
    public String getToken() throws LinkyException {
        return config.token;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public String getContactUrl() {
        return CONTACT_URL;
    }

    @Override
    public String getContractUrl() {
        return CONTRACT_URL;
    }

    @Override
    public String getIdentityUrl() {
        return IDENTITY_URL;
    }

    @Override
    public String getAddressUrl() {
        return ADDRESS_URL;
    }

    @Override
    public String getDailyConsumptionUrl() {
        return MEASURE_DAILY_CONSUMPTION_URL;
    }

    @Override
    public String getMaxPowerUrl() {
        return MEASURE_MAX_POWER_URL;
    }

    @Override
    public String getTempoUrl() {
        return TEMPO_URL;
    }
}
