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

import java.net.HttpCookie;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.linky.internal.config.LinkyBridgeWebConfiguration;
import org.openhab.binding.linky.internal.dto.AuthData;
import org.openhab.binding.linky.internal.dto.AuthResult;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link BridgeRemoteEnedisHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public class BridgeRemoteEnedisWebHandler extends BridgeRemoteBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeRemoteEnedisWebHandler.class);

    public static final String ENEDIS_DOMAIN = ".enedis.fr";

    private static final String BASE_URL = "https://alex.microapplications" + ENEDIS_DOMAIN;

    public static final String URL_MON_COMPTE = "https://mon-compte" + ENEDIS_DOMAIN;
    public static final String URL_COMPTE_PART = URL_MON_COMPTE.replace("compte", "compte-particulier");
    public static final URI COOKIE_URI = URI.create(URL_COMPTE_PART);

    private static final String USER_INFO_CONTRACT_URL = BASE_URL + "/mon-compte/api/private/v2/userinfos";
    private static final String USER_INFO_URL = BASE_URL + "/userinfos";
    private static final String PRM_INFO_BASE_URL = BASE_URL + "/mes-mesures-prm/api/private/v1/personnes/";
    private static final String PRM_INFO_URL = BASE_URL + "/mes-prms-part/api/private/v2/personnes/%s/prms";

    private static final String MEASURE_DAILY_CONSUMPTION_URL = PRM_INFO_BASE_URL
            + "%s/prms/%s/donnees-energetiques?mesuresTypeCode=ENERGIE&mesuresCorrigees=false&typeDonnees=CONS";

    private static final String MEASURE_MAX_POWER_URL = PRM_INFO_BASE_URL
            + "%s/prms/%s/donnees-energetiques?mesuresTypeCode=PMAX&mesuresCorrigees=false&typeDonnees=CONS";

    private static final String LOAD_CURVE_CONSUMPTION_URL = PRM_INFO_BASE_URL
            + "%s/prms/%s/donnees-energetiques?mesuresTypeCode=COURBE&mesuresCorrigees=false&typeDonnees=CONS&dateDebut=%s";

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter API_DATE_FORMAT_YEAR_FIRST = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String URL_ENEDIS_AUTHENTICATE = BASE_URL + "/authenticate?target=" + URL_COMPTE_PART;

    private static final String BASE_MYELECT_URL = "https://www.myelectricaldata.fr/";
    private static final String TEMPO_URL = BASE_MYELECT_URL + "rte/tempo/%s/%s";

    public BridgeRemoteEnedisWebHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, componentContext, gson);
    }

    @Override
    public void initialize() {
        super.initialize();

        config = getConfigAs(LinkyBridgeWebConfiguration.class);
        if (!Objects.requireNonNull(config).seemsValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }
    }

    @Override
    public String getToken(ThingBaseRemoteHandler handler) throws LinkyException {
        return "";
    }

    @Override
    public double getDivider() {
        return 1.00;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public String getContactUrl() {
        return USER_INFO_URL;
    }

    @Override
    public String getContractUrl() {
        return PRM_INFO_URL;
    }

    @Override
    public String getIdentityUrl() {
        return USER_INFO_URL;
    }

    @Override
    public String getAddressUrl() {
        return "";
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

    @Override
    public synchronized void connectionInit() throws LinkyException {
        LinkyBridgeWebConfiguration lcConfig = (LinkyBridgeWebConfiguration) config;
        if (lcConfig == null) {
            return;
        }

        logger.debug("Starting login process for user: {}", lcConfig.username);

        try {
            ContentResponse result = null;
            String uri = "";
            String gotoUri = "";

            // has we reconnect, remove all previous cookie to start from fresh session
            enedisApi.removeAllCookie();

            enedisApi.addCookie(LinkyBridgeWebConfiguration.INTERNAL_AUTH_ID, lcConfig.internalAuthId);

            // ======================================================
            logger.debug("Step 1a: getting authentification");
            // ======================================================
            uri = URL_ENEDIS_AUTHENTICATE;
            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 1a - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 1b: ...");
            // ======================================================
            uri = BASE_URL + result.getHeaders().get("Location");
            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 1b - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 1c: ...");
            // ======================================================
            uri = result.getHeaders().get("Location");

            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 1c - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 1d: ...");
            // ======================================================
            uri = result.getHeaders().get("Location");
            int idx = uri.indexOf("goto=");
            gotoUri = uri.substring(idx + 5);

            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 1d - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 1e: ...");
            // ======================================================
            uri = URL_MON_COMPTE + result.getHeaders().get("Location");
            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Connection failed step 1e - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 2: auth1 - retrieve the template, thanks to cookie internalAuthId user is already set");
            // ======================================================
            uri = URL_MON_COMPTE + "/auth/json/authenticate?realm=/enedis&goto=" + gotoUri;

            result = httpClient.POST(uri).header("X-NoSession", "true").header("X-Password", "anonymous")
                    .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous").send();
            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Connection failed step 3 - auth1: %s", result.getContentAsString());
            }

            AuthData authData = gson.fromJson(result.getContentAsString(), AuthData.class);
            if (authData != null) {
                if (authData.callbacks.size() < 2 || authData.callbacks.get(0).input.isEmpty()
                        || authData.callbacks.get(1).input.isEmpty() || !lcConfig.username.equals(
                                Objects.requireNonNull(authData.callbacks.get(0).input.get(0)).valueAsString())) {
                    logger.debug("auth1 - invalid template for auth data: {}", result.getContentAsString());
                    throw new LinkyException("Authentication error, the authentication_cookie is probably wrong");
                }

                authData.callbacks.get(1).input.get(0).value = lcConfig.password;
            }

            // ======================================================
            logger.debug("Step 3: auth2 - send the auth data");
            // ======================================================
            result = httpClient.POST(uri).header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header("X-NoSession", "true").header("X-Password", "anonymous")
                    .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous")
                    .content(new StringContentProvider(gson.toJson(authData))).send();
            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Connection failed step 3 - auth2 : %s", result.getContentAsString());
            }

            AuthResult authResult = gson.fromJson(result.getContentAsString(), AuthResult.class);

            logger.debug("Add the tokenId cookie");
            if (authResult == null) {
                throw new LinkyException("Errors on step3 : authResult=null");
            }

            enedisApi.addCookie("enedisExt", authResult.tokenId);
            // ======================================================
            logger.debug("Step 4a: Confirm login");
            // ======================================================
            uri = authResult.successUrl;
            result = httpClient.GET(uri);
            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 4a - auth2: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 4b:Confirm login");
            // ======================================================
            uri = result.getHeaders().get("Location");
            result = httpClient.GET(uri);
            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 4b - auth2: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 4c: Confirm login");
            // ======================================================
            uri = BASE_URL + "/authenticate?target=https://mon-compte-client.enedis.fr%2Fhub%3FallEspace%3Dfalse";
            // "result.getHeaders().get("Location");

            result = httpClient.GET(uri);
            if (result.getStatus() != HttpStatus.TEMPORARY_REDIRECT_307) {
                throw new LinkyException("Connection failed step 4c - auth2: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ===========================================================
            logger.debug("Step 5: retrieve user information andd cookie");
            // ===========================================================
            result = httpClient.GET(USER_INFO_CONTRACT_URL);

            @SuppressWarnings("unchecked")
            HashMap<String, String> hashRes = gson.fromJson(result.getContentAsString(), HashMap.class);

            String cookieKey;

            if (hashRes != null && hashRes.containsKey("cnAlex")) {
                cookieKey = "personne_for_" + hashRes.get("cnAlex");
            } else {
                throw new LinkyException("Connection failed step 5, missing cookieKey");
            }

            List<HttpCookie> lCookie = httpClient.getCookieStore().getCookies();
            Optional<HttpCookie> cookie = lCookie.stream().filter(it -> it.getName().contains(cookieKey)).findFirst();

            String cookieVal = cookie.map(HttpCookie::getValue)
                    .orElseThrow(() -> new LinkyException("Connection failed step 7, missing cookieVal"));

            enedisApi.addCookie(cookieKey, cookieVal);

            connected = true;
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonSyntaxException e) {
            throw new LinkyException(e, "Error opening connection with Enedis webservice");
        }
    }

    @Override
    public boolean supportNewApiFormat() {
        return false;
    }
}
