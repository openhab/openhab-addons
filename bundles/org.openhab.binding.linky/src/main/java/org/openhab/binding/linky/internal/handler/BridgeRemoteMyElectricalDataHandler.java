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

import java.time.format.DateTimeFormatter;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.binding.linky.internal.config.LinkyThingRemoteConfiguration;
import org.openhab.binding.linky.internal.constants.LinkyBindingConstants;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link BridgeRemoteMyElectricalDataHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class BridgeRemoteMyElectricalDataHandler extends BridgeRemoteApiHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeRemoteMyElectricalDataHandler.class);

    private static final String BASE_URL = "https://www.myelectricaldata.fr/";

    private static final String CONTRACT_URL = BASE_URL + "contracts/%s/cache/";
    private static final String IDENTITY_URL = BASE_URL + "identity/%s/cache/";
    private static final String CONTACT_URL = BASE_URL + "contact/%s/cache/";
    private static final String ADDRESS_URL = BASE_URL + "addresses/%s/cache/";
    private static final String MEASURE_DAILY_CONSUMPTION_URL = BASE_URL + "daily_consumption/%s/start/%s/end/%s/cache";
    private static final String MEASURE_MAX_POWER_URL = BASE_URL
            + "daily_consumption_max_power/%s/start/%s/end/%s/cache";
    private static final String LOAD_CURVE_CONSUMPTION_URL = BASE_URL
            + "consumption_load_curve/%s/start/%s/end/%s/cache";

    // List of Linky services related urls, information
    public static final String LINKY_MYELECTRICALDATA_ACCOUNT_URL = "https://www.myelectricaldata.fr/";
    public static final String LINKY_MYELECTRICALDATA_AUTHORIZE_URL = BridgeRemoteEnedisHandler.ENEDIS_ACCOUNT_URL_PROD
            + BridgeRemoteEnedisHandler.ENEDIS_AUTHORIZE_URL;
    public static final String LINKY_MYELECTRICALDATA_API_TOKEN_URL = LINKY_MYELECTRICALDATA_ACCOUNT_URL
            + "v1/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=na&user_type=na&state=na&person_id=-1&usage_points_id=%s";

    public static final String LINKY_MYELECTRICALDATA_CLIENT_ID = "_h7zLaRr2INxqBI8jhDUQXsa_G4a";

    private static final String TEMPO_URL = BASE_URL + "rte/tempo/%s/%s";

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter API_DATE_FORMAT_YEAR_FIRST = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // https://www.myelectricaldata.fr/v1/oauth2/authorize?response_type=code&client_id=&state=linky&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fconnectlinky&scope=am_application_scope+default&user_type=aa&person_id=-1&usage_points_id=aa

    public BridgeRemoteMyElectricalDataHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, componentContext, gson);
    }

    @Override
    public void connectionInit() {
        connected = true;
    }

    @Override
    public void initialize() {
        tokenUrl = BridgeRemoteMyElectricalDataHandler.LINKY_MYELECTRICALDATA_API_TOKEN_URL;
        authorizeUrl = BridgeRemoteMyElectricalDataHandler.LINKY_MYELECTRICALDATA_AUTHORIZE_URL;

        super.initialize();
    }

    @Override
    public String getClientId() {
        return BridgeRemoteMyElectricalDataHandler.LINKY_MYELECTRICALDATA_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return "";
    }

    @Override
    public boolean getIsSandbox() {
        return false;
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        return super.formatAuthorizationUrl("");
    }

    @Override
    public String authorize(String redirectUri, String reqState, String reqCode) throws LinkyException {
        String url = String.format(BridgeRemoteMyElectricalDataHandler.LINKY_MYELECTRICALDATA_API_TOKEN_URL,
                getClientId(), reqCode);
        EnedisHttpApi enedisApi = getEnedisApi();
        if (enedisApi == null) {
            return "";
        }
        String token = enedisApi.getContent(url);

        logger.debug("token: {}", token);

        Collection<Thing> col = this.getThing().getThings();

        for (Thing thing : col) {
            if (LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {
                Configuration config = thing.getConfiguration();
                String prmId = (String) config.get("prmId");

                if (!prmId.equals(reqCode)) {
                    continue;
                }

                config.put("token", token);
                ThingLinkyRemoteHandler handler = (ThingLinkyRemoteHandler) thing.getHandler();
                if (handler != null) {
                    handler.saveConfiguration(config);
                }
            }
        }
        return token;
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Netatmo API bridge handler.");

        super.dispose();
    }

    @Override
    public String getToken(ThingBaseRemoteHandler handler) throws LinkyException {
        if (handler.getLinkyConfig() instanceof LinkyThingRemoteConfiguration config) {
            return config.token;
        }
        return "";
    }

    @Override
    public double getDivider() {
        return 1000.00;
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
    public String getLoadCurveUrl() {
        return LOAD_CURVE_CONSUMPTION_URL;
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
