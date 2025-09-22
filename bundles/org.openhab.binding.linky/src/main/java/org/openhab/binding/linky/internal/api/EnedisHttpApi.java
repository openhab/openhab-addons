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
package org.openhab.binding.linky.internal.api;

import java.net.HttpCookie;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.linky.internal.LinkyConfiguration;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.binding.linky.internal.dto.AuthData;
import org.openhab.binding.linky.internal.dto.AuthResult;
import org.openhab.binding.linky.internal.dto.ConsumptionReport;
import org.openhab.binding.linky.internal.dto.ConsumptionReport.Consumption;
import org.openhab.binding.linky.internal.dto.PrmDetail;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link EnedisHttpApi} wraps the Enedis Webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class EnedisHttpApi {
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String ENEDIS_DOMAIN = ".enedis.fr";
    private static final String BASE_URL = "https://alex.microapplications" + ENEDIS_DOMAIN;
    private static final String URL_MON_COMPTE = "https://mon-compte" + ENEDIS_DOMAIN;
    private static final String URL_COMPTE_PART = URL_MON_COMPTE.replace("compte", "compte-particulier");
    private static final String URL_ENEDIS_AUTHENTICATE = BASE_URL + "/authenticate?target=" + URL_COMPTE_PART;
    private static final String USER_INFO_CONTRACT_URL = BASE_URL + "/mon-compte/api/private/v2/userinfos";
    private static final String USER_INFO_URL = BASE_URL + "/userinfos";
    private static final String PRM_INFO_BASE_URL = BASE_URL + "/mes-mesures-prm/api/private/v2/personnes/";
    private static final String PRM_INFO_URL = BASE_URL + "/mes-prms-part/api/private/v2/personnes/%s/prms";
    private static final String MEASURE_URL = PRM_INFO_BASE_URL
            + "%s/prms/%s/donnees-energetiques?mesuresTypeCode=%s&mesuresCorrigees=false&typeDonnees=CONS&dateDebut=%s&segments=%s";
    private static final URI COOKIE_URI = URI.create(URL_COMPTE_PART);
    private static final Pattern REQ_PATTERN = Pattern.compile("ReqID%(.*?)%26");

    private final Logger logger = LoggerFactory.getLogger(EnedisHttpApi.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final LinkyConfiguration config;

    private boolean connected = false;

    private String idPersonne = "";

    public EnedisHttpApi(LinkyConfiguration config, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.config = config;
    }

    public void removeAllCookie() {
        httpClient.getCookieStore().removeAll();
    }

    public void initialize() throws LinkyException {
        logger.info("Starting login process 2 for user: {}", config.username);

        try {
            ContentResponse result = null;
            String uri = "";
            String gotoUri = "";

            removeAllCookie();

            addCookie(LinkyConfiguration.INTERNAL_AUTH_ID, config.internalAuthId);

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
                        || authData.callbacks.get(1).input.isEmpty() || !config.username.equals(
                                Objects.requireNonNull(authData.callbacks.get(0).input.get(0)).valueAsString())) {
                    logger.debug("auth1 - invalid template for auth data: {}", result.getContentAsString());
                    throw new LinkyException("Authentication error, the authentication_cookie is probably wrong");
                }

                authData.callbacks.get(1).input.get(0).value = config.password;
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

            addCookie("enedisExt", authResult.tokenId);
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
                idPersonne = Objects.requireNonNull(hashRes.get("idPersonne"));
            } else {
                throw new LinkyException("Connection failed step 5, missing cookieKey");
            }

            List<HttpCookie> lCookie = httpClient.getCookieStore().getCookies();
            Optional<HttpCookie> cookie = lCookie.stream().filter(it -> it.getName().contains(cookieKey)).findFirst();

            String cookieVal = cookie.map(HttpCookie::getValue)
                    .orElseThrow(() -> new LinkyException("Connection failed step 7, missing cookieVal"));

            addCookie(cookieKey, cookieVal);

            connected = true;
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonSyntaxException e) {
            throw new LinkyException(e, "Error opening connection with Enedis webservice");
        }
    }

    private String getLocation(ContentResponse response) {
        return response.getHeaders().get(HttpHeader.LOCATION);
    }

    private void disconnect() throws LinkyException {
        if (connected) {
            logger.debug("Logout process");
            connected = false;
            try { // Three times in a row to get disconnected
                String location = getLocation(httpClient.GET(BASE_URL + "/logout"));
                location = getLocation(httpClient.GET(location));
                getLocation(httpClient.GET(location));
                httpClient.getCookieStore().removeAll();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new LinkyException(e, "Error while disconnecting from Enedis webservice");
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void dispose() throws LinkyException {
        disconnect();
    }

    private void addCookie(String key, String value) {
        HttpCookie cookie = new HttpCookie(key, value);
        cookie.setDomain(ENEDIS_DOMAIN);
        cookie.setPath("/");
        httpClient.getCookieStore().add(COOKIE_URI, cookie);
    }

    private FormContentProvider getFormContent(String fieldName, String fieldValue) {
        Fields fields = new Fields();
        fields.put(fieldName, fieldValue);
        return new FormContentProvider(fields);
    }

    private String getContent(String url) throws LinkyException {
        try {
            Request request = httpClient.newRequest(url);

            request = request.agent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
            request = request.method(HttpMethod.GET);
            ContentResponse result = request.send();
            if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                    || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                String loc = result.getHeaders().get("Location");
                String newUrl = "";

                if (loc.startsWith("http://") || loc.startsWith("https://")) {
                    newUrl = loc;
                } else {
                    newUrl = BASE_URL + loc;
                }

                request = httpClient.newRequest(newUrl);
                request = request.method(HttpMethod.GET);
                result = request.send();

                if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                        || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                    loc = result.getHeaders().get("Location");
                    String[] urlParts = loc.split("/");
                    if (urlParts.length < 4) {
                        throw new LinkyException("malformed url : %s", loc);
                    }
                    return urlParts[3];
                }
            }

            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Error requesting '%s': %s", url, result.getContentAsString());
            }
            String content = result.getContentAsString();
            logger.trace("getContent returned {}", content);
            return content;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new LinkyException(e, "Error getting url: '%s'", url);
        }
    }

    private <T> T getData(String url, Class<T> clazz) throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getContent(url);
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", url);
        }
        try {
            T result = Objects.requireNonNull(gson.fromJson(data, clazz));
            logger.trace("getData success {}: {}", clazz.getName(), url);
            return result;
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid JSON response not matching {}: {}", clazz.getName(), data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", url);
        }
    }

    public PrmInfo getPrmInfo(String internId) throws LinkyException {
        String url = PRM_INFO_URL.formatted(internId);
        PrmInfo[] prms = getData(url, PrmInfo[].class);
        if (prms.length < 1) {
            throw new LinkyException("Invalid prms data received");
        }
        return prms[0];
    }

    public PrmDetail getPrmDetails(String internId, String prmId) throws LinkyException {
        String url = PRM_INFO_URL.formatted(internId) + "/" + prmId
                + "?embed=SITALI&embed=SITCOM&embed=SITCON&embed=SYNCON";
        return getData(url, PrmDetail.class);
    }

    public UserInfo getUserInfo() throws LinkyException {
        return getData(USER_INFO_URL, UserInfo.class);
    }

    private Consumption getMeasures(String userId, String prmId, String segment, LocalDate from, LocalDate to,
            String request) throws LinkyException {
        String url = String.format(MEASURE_URL, userId, prmId, request, from.format(API_DATE_FORMAT), segment);
        ConsumptionReport report = getData(url, ConsumptionReport.class);
        return report.consumptions;
    }

    public Consumption getEnergyData(String userId, String prmId, String segment, LocalDate from, LocalDate to)
            throws LinkyException {
        return getMeasures(userId, prmId, segment, from, to, "ENERGIE");
    }

    public Consumption getPowerData(String userId, String prmId, String segment, LocalDate from, LocalDate to)
            throws LinkyException {
        return getMeasures(userId, prmId, segment, from, to, "PMAX");
    }

    public String getIdPersonne() {
        return idPersonne;
    }
}
