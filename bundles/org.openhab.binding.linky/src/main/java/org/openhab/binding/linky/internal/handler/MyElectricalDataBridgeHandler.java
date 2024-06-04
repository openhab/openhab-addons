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

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.LinkyBindingConstants;
import org.openhab.binding.linky.internal.LinkyConfiguration;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
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

    private static final String CONTRACT_URL = BASE_URL + "contracts/%s/cache/";
    private static final String IDENTITY_URL = BASE_URL + "identity/%s/cache/";
    private static final String CONTACT_URL = BASE_URL + "contact/%s/cache/";
    private static final String ADDRESS_URL = BASE_URL + "addresses/%s/cache/";
    private static final String MEASURE_DAILY_CONSUMPTION_URL = BASE_URL
            + "daily_consumption/%s/start/%s/end/%s/cache/";
    private static final String MEASURE_MAX_POWER_URL = BASE_URL
            + "daily_consumption_max_power/%s/start/%s/end/%s/cache/";

    private static final String TEMPO_URL = BASE_URL + "rte/tempo/%s/%s";

    // https://www.myelectricaldata.fr/v1/oauth2/authorize?response_type=code&client_id=&state=linky&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fconnectlinky&scope=am_application_scope+default&user_type=aa&person_id=-1&usage_points_id=aa

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
    public String getClientId() {
        return LinkyBindingConstants.LINKY_MYELECTRICALDATA_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return "";
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        return super.formatAuthorizationUrl("");
    }

    @Override
    public String authorize(String redirectUri, String reqState, String reqCode) throws LinkyException {
        String url = String.format(LinkyBindingConstants.LINKY_MYELECTRICALDATA_API_TOKEN_URL, getClientId(), reqCode);
        EnedisHttpApi enedisApi = getEnedisApi();
        if (enedisApi == null) {
            return "";
        }
        String token = enedisApi.getData(url);

        logger.debug("token: {}", token);

        Collection<Thing> col = this.thingRegistry.getAll();

        for (Thing thing : col) {
            if (LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {
                Configuration config = thing.getConfiguration();
                String prmId = (String) config.get("prmId");

                if (!prmId.equals(reqCode)) {
                    continue;
                }

                config.put("token", token);
                LinkyHandler handler = (LinkyHandler) thing.getHandler();
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
    public String getToken(LinkyHandler handler) throws LinkyException {
        LinkyConfiguration config = handler.getLinkyConfig();
        if (config == null) {
            return "";
        }
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
