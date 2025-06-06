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
package org.openhab.binding.linky.internal.handler;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.config.LinkyBridgeApiConfiguration;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link BridgeRemoteEnedisHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class BridgeRemoteEnedisHandler extends BridgeRemoteApiHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeRemoteEnedisHandler.class);

    private static final String BASE_URL_PREPROD = "https://gw.ext.prod-sandbox.api.enedis.fr/";
    private static final String ENEDIS_ACCOUNT_URL_PREPROD = "gw.ext.prod-sandbox.api.enedis.fr";

    private static final String BASE_URL_PROD = "https://gw.ext.prod.api.enedis.fr/";
    public static final String ENEDIS_ACCOUNT_URL_PROD = "https://mon-compte-particulier.enedis.fr/";

    private static final String CONTRACT_URL = "customers_upc/v5/usage_points/contracts?usage_point_id=%s";
    private static final String IDENTITY_URL = "customers_i/v5/identity?usage_point_id=%s";
    private static final String CONTACT_URL = "customers_cd/v5/contact_data?usage_point_id=%s";
    private static final String ADDRESS_URL = "customers_upa/v5/usage_points/addresses?usage_point_id=%s";

    private static final String MEASURE_DAILY_CONSUMPTION_URL = "metering_data_dc/v5/daily_consumption?usage_point_id=%s&start=%s&end=%s";
    private static final String MEASURE_MAX_POWER_URL = "metering_data_dcmp/v5/daily_consumption_max_power?usage_point_id=%s&start=%s&end=%s";
    private static final String LOAD_CURVE_CONSUMPTION_URL = "metering_data_clc/v5/consumption_load_curve?usage_point_id=%s&start=%s&end=%s";

    public static final String ENEDIS_AUTHORIZE_URL = "dataconnect/v1/oauth2/authorize?duration=P36M";
    public static final String ENEDIS_API_TOKEN_URL = "oauth2/v3/token";

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter API_DATE_FORMAT_YEAR_FIRST = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String BASE_MYELECT_URL = "https://www.myelectricaldata.fr/";
    private static final String TEMPO_URL = BASE_MYELECT_URL + "rte/tempo/%s/%s";

    public BridgeRemoteEnedisHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, componentContext, gson);
    }

    @Override
    public void initialize() {
        tokenUrl = getBaseUrl() + BridgeRemoteEnedisHandler.ENEDIS_API_TOKEN_URL;
        authorizeUrl = getAccountUrl() + BridgeRemoteEnedisHandler.ENEDIS_AUTHORIZE_URL;

        super.initialize();
    }

    public String getAccountUrl() {
        if (getIsSandbox()) {
            return ENEDIS_ACCOUNT_URL_PREPROD;
        } else {
            return ENEDIS_ACCOUNT_URL_PROD;
        }
    }

    @Override
    public String getClientId() {
        LinkyBridgeApiConfiguration lcConfig = (LinkyBridgeApiConfiguration) config;
        if (lcConfig != null) {
            return lcConfig.clientId;
        }
        return "";
    }

    @Override
    public String getClientSecret() {
        LinkyBridgeApiConfiguration lcConfig = (LinkyBridgeApiConfiguration) config;
        if (lcConfig != null) {
            return lcConfig.clientSecret;
        }
        return "";
    }

    @Override
    public boolean getIsSandbox() {
        LinkyBridgeApiConfiguration lcConfig = (LinkyBridgeApiConfiguration) config;
        return (lcConfig != null) ? lcConfig.isSandbox : false;
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Enedis bridge handler.");

        super.dispose();
    }

    @Override
    public void connectionInit() {
    }

    @Override
    public String getToken(ThingBaseRemoteHandler handler) throws LinkyException {
        AccessTokenResponse accesToken = getAccessTokenResponse();

        // Store token is about to expire, ask for a new one.
        if (accesToken != null && accesToken.isExpired(Instant.now(), 1200)) {
            accesToken = null;
        }

        if (accesToken == null) {
            accesToken = getAccessTokenByClientCredentials();
        }

        if (accesToken == null) {
            throw new LinkyException("no token");
        }

        return "Bearer " + accesToken.getAccessToken();
    }

    @Override
    public double getDivider() {
        return 1000.00;
    }

    @Override
    public String getBaseUrl() {
        if (getIsSandbox()) {
            return BASE_URL_PREPROD;
        } else {
            return BASE_URL_PROD;
        }
    }

    @Override
    public String getContactUrl() {
        return getBaseUrl() + CONTACT_URL;
    }

    @Override
    public String getContractUrl() {
        return getBaseUrl() + CONTRACT_URL;
    }

    @Override
    public String getIdentityUrl() {
        return getBaseUrl() + IDENTITY_URL;
    }

    @Override
    public String getAddressUrl() {
        return getBaseUrl() + ADDRESS_URL;
    }

    @Override
    public String getDailyConsumptionUrl() {
        return getBaseUrl() + MEASURE_DAILY_CONSUMPTION_URL;
    }

    @Override
    public String getMaxPowerUrl() {
        return getBaseUrl() + MEASURE_MAX_POWER_URL;
    }

    @Override
    public String getLoadCurveUrl() {
        return getBaseUrl() + LOAD_CURVE_CONSUMPTION_URL;
    }

    @Override
    public String getTempoUrl() {
        return TEMPO_URL;
    }

    @Override
    public DateTimeFormatter getApiDateFormat() {
        return API_DATE_FORMAT;
    }

    @Override
    public DateTimeFormatter getApiDateFormatYearsFirst() {
        return API_DATE_FORMAT_YEAR_FIRST;
    }
}
