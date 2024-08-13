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

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.linky.internal.LinkyConfiguration;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.binding.linky.internal.dto.AuthData;
import org.openhab.binding.linky.internal.dto.AuthResult;
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
import com.google.gson.JsonSyntaxException;

/**
 * {@link EnedisBridgeHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public class EnedisWebBridgeHandler extends ApiBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(EnedisWebBridgeHandler.class);

    private static final String BASE_URL = "https://ext.prod-sandbox.api.enedis.fr/";

    private static final String CONTRACT_URL = BASE_URL + "customers_upc/v5/usage_points/contracts?usage_point_id=%s";
    private static final String IDENTITY_URL = BASE_URL + "customers_i/v5/identity?usage_point_id=%s";
    private static final String CONTACT_URL = BASE_URL + "customers_cd/v5/contact_data?usage_point_id=%s";
    private static final String ADDRESS_URL = BASE_URL + "customers_upa/v5/usage_points/addresses?usage_point_id=%s";
    private static final String MEASURE_DAILY_CONSUMPTION_URL = BASE_URL
            + "metering_data_dc/v5/daily_consumption?usage_point_id=%s&start=%s&end=%s";
    private static final String MEASURE_MAX_POWER_URL = BASE_URL
            + "metering_data_dcmp/v5/daily_consumption_max_power?usage_point_id=%s&start=%s&end=%s";

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final String URL_APPS_LINCS = "https://alex.microapplications" + EnedisHttpApi.ENEDIS_DOMAIN;
    private static final String URL_ENEDIS_AUTHENTICATE = URL_APPS_LINCS + "/authenticate?target="
            + EnedisHttpApi.URL_COMPTE_PART;
    private static final String USER_INFO_URL = URL_APPS_LINCS + "/userinfos";
    private static final String PRM_INFO_BASE_URL = URL_APPS_LINCS + "/mes-mesures/api/private/v1/personnes/";
    private static final String PRM_INFO_URL = PRM_INFO_BASE_URL + "null/prms";
    private static final String MEASURE_URL = PRM_INFO_BASE_URL
            + "%s/prms/%s/donnees-%s?dateDebut=%s&dateFin=%s&mesuretypecode=CONS";
    private static final Pattern REQ_PATTERN = Pattern.compile("ReqID%(.*?)%26");

    private boolean connected = false;

    public EnedisWebBridgeHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            final @Reference ThingRegistry thingRegistry, ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, thingRegistry, componentContext, gson);
    }

    @Override
    public void initialize() {
        super.initialize();

        try {
            doAuth();
        } catch (LinkyException ex) {
            logger.debug("Exception: {}", ex.getMessage());
        }
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
        try {
            enedisApi.disconnect();
        } catch (LinkyException ex) {
            logger.debug("Exception: {}", ex.getMessage());
        }
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

    public void doAuth() throws LinkyException {
        logger.debug("Starting login process for user : {}", config.username);

        try {
            enedisApi.addCookie(LinkyConfiguration.INTERNAL_AUTH_ID, config.internalAuthId);
            logger.debug("Step 1 : getting authentification");
            String data = enedisApi.getData(URL_ENEDIS_AUTHENTICATE);

            logger.debug("Reception request SAML");
            Document htmlDocument = Jsoup.parse(data);
            Element el = htmlDocument.select("form").first();
            Element samlInput = el.select("input[name=SAMLRequest]").first();

            logger.debug("Step 2 : send SSO SAMLRequest");
            ContentResponse result = httpClient.POST(el.attr("action"))
                    .content(enedisApi.getFormContent("SAMLRequest", samlInput.attr("value"))).send();
            if (result.getStatus() != 302) {
                throw new LinkyException("Connection failed step 2");
            }

            logger.debug("Get the location and the ReqID");
            Matcher m = REQ_PATTERN.matcher(enedisApi.getLocation(result));
            if (!m.find()) {
                throw new LinkyException("Unable to locate ReqId in header");
            }

            String reqId = m.group(1);
            String authenticateUrl = EnedisHttpApi.URL_MON_COMPTE
                    + "/auth/json/authenticate?realm=/enedis&forward=true&spEntityID=SP-ODW-PROD&goto=/auth/SSOPOST/metaAlias/enedis/providerIDP?ReqID%"
                    + reqId + "%26index%3Dnull%26acsURL%3D" + URL_APPS_LINCS
                    + "/saml/SSO%26spEntityID%3DSP-ODW-PROD%26binding%3Durn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST&AMAuthCookie=";

            logger.debug("Step 3 : auth1 - retrieve the template, thanks to cookie internalAuthId user is already set");
            result = httpClient.POST(authenticateUrl).header("X-NoSession", "true").header("X-Password", "anonymous")
                    .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous").send();
            if (result.getStatus() != 200) {
                throw new LinkyException("Connection failed step 3 - auth1 : %s", result.getContentAsString());
            }

            AuthData authData = gson.fromJson(result.getContentAsString(), AuthData.class);
            if (authData == null || authData.callbacks.size() < 2 || authData.callbacks.get(0).input.isEmpty()
                    || authData.callbacks.get(1).input.isEmpty() || !config.username
                            .equals(Objects.requireNonNull(authData.callbacks.get(0).input.get(0)).valueAsString())) {
                logger.debug("auth1 - invalid template for auth data: {}", result.getContentAsString());
                throw new LinkyException("Authentication error, the authentication_cookie is probably wrong");
            }

            authData.callbacks.get(1).input.get(0).value = config.password;
            logger.debug("Step 4 : auth2 - send the auth data");
            result = httpClient.POST(authenticateUrl).header(HttpHeader.CONTENT_TYPE, "application/json")
                    .header("X-NoSession", "true").header("X-Password", "anonymous")
                    .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous")
                    .content(new StringContentProvider(gson.toJson(authData))).send();
            if (result.getStatus() != 200) {
                throw new LinkyException("Connection failed step 3 - auth2 : %s", result.getContentAsString());
            }

            AuthResult authResult = gson.fromJson(result.getContentAsString(), AuthResult.class);
            if (authResult == null) {
                throw new LinkyException("Invalid authentication result data");
            }

            logger.debug("Add the tokenId cookie");
            enedisApi.addCookie("enedisExt", authResult.tokenId);

            logger.debug("Step 5 : retrieve the SAMLresponse");
            data = enedisApi.getData(EnedisHttpApi.URL_MON_COMPTE + "/" + authResult.successUrl);
            htmlDocument = Jsoup.parse(data);
            el = htmlDocument.select("form").first();
            samlInput = el.select("input[name=SAMLResponse]").first();

            logger.debug("Step 6 : post the SAMLresponse to finish the authentication");
            result = httpClient.POST(el.attr("action"))
                    .content(enedisApi.getFormContent("SAMLResponse", samlInput.attr("value"))).send();
            if (result.getStatus() != 302) {
                throw new LinkyException("Connection failed step 6");
            }
            connected = true;
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonSyntaxException e) {
            throw new LinkyException(e, "Error opening connection with Enedis webservice");
        }
    }
}
