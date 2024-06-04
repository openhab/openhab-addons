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
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
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
 * {@link EnedisBridgeHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class EnedisBridgeHandler extends ApiBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(EnedisBridgeHandler.class);

    private static final String BASE_URL = "https://ext.prod-sandbox.api.enedis.fr/";

    private static final String CONTRACT_URL = BASE_URL + "customers_upc/v5/usage_points/contracts?usage_point_id=%s";
    private static final String IDENTITY_URL = BASE_URL + "customers_i/v5/identity?usage_point_id=%s";
    private static final String CONTACT_URL = BASE_URL + "customers_cd/v5/contact_data?usage_point_id=%s";
    private static final String ADDRESS_URL = BASE_URL + "customers_upa/v5/usage_points/addresses?usage_point_id=%s";
    private static final String MEASURE_DAILY_CONSUMPTION_URL = BASE_URL
            + "metering_data_dc/v5/daily_consumption?usage_point_id=%s&start=%s&end=%s";
    private static final String MEASURE_MAX_POWER_URL = BASE_URL
            + "metering_data_dcmp/v5/daily_consumption_max_power?usage_point_id=%s&start=%s&end=%s";

    public EnedisBridgeHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            final @Reference ThingRegistry thingRegistry, ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, thingRegistry, componentContext, gson);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public String getClientId() {
        return config.clientId;
    }

    @Override
    public String getClientSecret() {
        return config.clientSecret;
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Netatmo API bridge handler.");

        super.dispose();
    }

    @Override
    public String getToken(LinkyHandler handler) throws LinkyException {

        AccessTokenResponse accesToken = getAccessTokenResponse();
        if (accesToken == null) {
            accesToken = getAccessTokenByClientCredentials();
        }

        if (accesToken == null) {
            throw new LinkyException("no token");
        }

        return "Bearer " + accesToken.getAccessToken();
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
        return "";
    }
}
