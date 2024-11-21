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
import java.util.regex.Matcher;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String ENEDIS_DOMAIN = ".enedis.fr";
    private static final String URL_APPS_LINCS = "https://alex.microapplications" + ENEDIS_DOMAIN;
    private static final String URL_MON_COMPTE = "https://mon-compte" + ENEDIS_DOMAIN;
    private static final String URL_COMPTE_PART = URL_MON_COMPTE.replace("compte", "compte-particulier");
    private static final String URL_ENEDIS_AUTHENTICATE = URL_APPS_LINCS + "/authenticate?target=" + URL_COMPTE_PART;
    private static final String USER_INFO_CONTRACT_URL = URL_APPS_LINCS + "/mon-compte-client/api/private/v1/userinfos";
    private static final String USER_INFO_URL = URL_APPS_LINCS + "/userinfos";
    private static final String PRM_INFO_BASE_URL = URL_APPS_LINCS + "/mes-mesures/api/private/v1/personnes/";
    private static final String PRM_INFO_URL = URL_APPS_LINCS + "/mes-prms-part/api/private/v2/personnes/%s/prms";
    private static final String MEASURE_URL = PRM_INFO_BASE_URL
            + "%s/prms/%s/donnees-%s?dateDebut=%s&dateFin=%s&mesuretypecode=CONS";
    private static final URI COOKIE_URI = URI.create(URL_COMPTE_PART);
    private static final Pattern REQ_PATTERN = Pattern.compile("ReqID%(.*?)%26");

    private final Logger logger = LoggerFactory.getLogger(EnedisHttpApi.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final LinkyConfiguration config;

    private boolean connected = false;

    public EnedisHttpApi(LinkyConfiguration config, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.config = config;
    }

    public void initialize() throws LinkyException {
        logger.debug("Starting login process for user: {}", config.username);

        try {
            addCookie(LinkyConfiguration.INTERNAL_AUTH_ID, config.internalAuthId);
            logger.debug("Step 1: getting authentification");
            String data = getContent(URL_ENEDIS_AUTHENTICATE);

            logger.debug("Reception request SAML");
            Document htmlDocument = Jsoup.parse(data);
            Element el = htmlDocument.select("form").first();
            Element samlInput = el.select("input[name=SAMLRequest]").first();

            logger.debug("Step 2: send SSO SAMLRequest");
            ContentResponse result = httpClient.POST(el.attr("action"))
                    .content(getFormContent("SAMLRequest", samlInput.attr("value"))).send();
            if (result.getStatus() != HttpStatus.FOUND_302) {
                throw new LinkyException("Connection failed step 2");
            }

            logger.debug("Get the location and the ReqID");
            Matcher m = REQ_PATTERN.matcher(getLocation(result));
            if (!m.find()) {
                throw new LinkyException("Unable to locate ReqId in header");
            }

            String reqId = m.group(1);
            String authenticateUrl = URL_MON_COMPTE
                    + "/auth/json/authenticate?realm=/enedis&forward=true&spEntityID=SP-ODW-PROD&goto=/auth/SSOPOST/metaAlias/enedis/providerIDP?ReqID%"
                    + reqId + "%26index%3Dnull%26acsURL%3D" + URL_APPS_LINCS
                    + "/saml/SSO%26spEntityID%3DSP-ODW-PROD%26binding%3Durn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST&AMAuthCookie=";

            logger.debug("Step 3: auth1 - retrieve the template, thanks to cookie internalAuthId user is already set");
            result = httpClient.POST(authenticateUrl).header("X-NoSession", "true").header("X-Password", "anonymous")
                    .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous").send();
            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Connection failed step 3 - auth1: %s", result.getContentAsString());
            }

            AuthData authData = gson.fromJson(result.getContentAsString(), AuthData.class);
            if (authData == null || authData.callbacks.size() < 2 || authData.callbacks.get(0).input.isEmpty()
                    || authData.callbacks.get(1).input.isEmpty() || !config.username
                            .equals(Objects.requireNonNull(authData.callbacks.get(0).input.get(0)).valueAsString())) {
                logger.debug("auth1 - invalid template for auth data: {}", result.getContentAsString());
                throw new LinkyException("Authentication error, the authentication_cookie is probably wrong");
            }

            authData.callbacks.get(1).input.get(0).value = config.password;
            logger.debug("Step 4: auth2 - send the auth data");
            result = httpClient.POST(authenticateUrl).header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header("X-NoSession", "true").header("X-Password", "anonymous")
                    .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous")
                    .content(new StringContentProvider(gson.toJson(authData))).send();
            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Connection failed step 3 - auth2: %s", result.getContentAsString());
            }

            AuthResult authResult = gson.fromJson(result.getContentAsString(), AuthResult.class);
            if (authResult == null) {
                throw new LinkyException("Invalid authentication result data");
            }

            logger.debug("Add the tokenId cookie");
            addCookie("enedisExt", authResult.tokenId);

            logger.debug("Step 5: retrieve the SAMLresponse");
            data = getContent(URL_MON_COMPTE + "/" + authResult.successUrl);
            htmlDocument = Jsoup.parse(data);
            el = htmlDocument.select("form").first();
            samlInput = el.select("input[name=SAMLResponse]").first();

            logger.debug("Step 6: post the SAMLresponse to finish the authentication");
            result = httpClient.POST(el.attr("action")).content(getFormContent("SAMLResponse", samlInput.attr("value")))
                    .send();
            if (result.getStatus() != HttpStatus.FOUND_302) {
                throw new LinkyException("Connection failed step 6");
            }

            logger.debug("Step 7: retrieve cookieKey");
            result = httpClient.GET(USER_INFO_CONTRACT_URL);

            @SuppressWarnings("unchecked")
            HashMap<String, String> hashRes = gson.fromJson(result.getContentAsString(), HashMap.class);

            String cookieKey;
            if (hashRes != null && hashRes.containsKey("cnAlex")) {
                cookieKey = "personne_for_" + hashRes.get("cnAlex");
            } else {
                throw new LinkyException("Connection failed step 7, missing cookieKey");
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
                String location = getLocation(httpClient.GET(URL_APPS_LINCS + "/logout"));
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
            Request request = httpClient.newRequest(url)
                    .agent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
            request = request.method(HttpMethod.GET);
            ContentResponse result = request.send();
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
            return Objects.requireNonNull(gson.fromJson(data, clazz));
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

    private Consumption getMeasures(String userId, String prmId, LocalDate from, LocalDate to, String request)
            throws LinkyException {
        String url = String.format(MEASURE_URL, userId, prmId, request, from.format(API_DATE_FORMAT),
                to.format(API_DATE_FORMAT));
        ConsumptionReport report = getData(url, ConsumptionReport.class);
        return report.firstLevel.consumptions;
    }

    public Consumption getEnergyData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(userId, prmId, from, to, "energie");
    }

    public Consumption getPowerData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(userId, prmId, from, to, "pmax");
    }
}
