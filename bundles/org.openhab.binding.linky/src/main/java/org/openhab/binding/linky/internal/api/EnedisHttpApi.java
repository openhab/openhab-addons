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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
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
    private static final String USER_INFO_URL = URL_APPS_LINCS + "/userinfos";
    private static final String PRM_INFO_BASE_URL = URL_APPS_LINCS + "/mes-mesures/api/private/v1/personnes/";
    private static final String PRM_INFO_URL = PRM_INFO_BASE_URL + "null/prms";
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
        logger.debug("Starting login process for user : {}", config.username);

        try {
            addCookie(LinkyConfiguration.INTERNAL_AUTH_ID, config.internalAuthId);
            logger.debug("Step 1 : getting authentification");
            String data = getData(URL_ENEDIS_AUTHENTICATE);

            logger.debug("Reception request SAML");
            Document htmlDocument = Jsoup.parse(data);
            Element el = htmlDocument.select("form").first();
            Element samlInput = el.select("input[name=SAMLRequest]").first();

            logger.debug("Step 2 : send SSO SAMLRequest");
            ContentResponse result = httpClient.POST(el.attr("action"))
                    .content(getFormContent("SAMLRequest", samlInput.attr("value"))).send();
            if (result.getStatus() != 302) {
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
            addCookie("enedisExt", authResult.tokenId);

            logger.debug("Step 5 : retrieve the SAMLresponse");
            data = getData(URL_MON_COMPTE + "/" + authResult.successUrl);
            htmlDocument = Jsoup.parse(data);
            el = htmlDocument.select("form").first();
            samlInput = el.select("input[name=SAMLResponse]").first();

            logger.debug("Step 6 : post the SAMLresponse to finish the authentication");
            result = httpClient.POST(el.attr("action")).content(getFormContent("SAMLResponse", samlInput.attr("value")))
                    .send();
            if (result.getStatus() != 302) {
                throw new LinkyException("Connection failed step 6");
            }
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

    private String getData(String url) throws LinkyException {
        try {
            ContentResponse result = httpClient.GET(url);
            if (result.getStatus() != 200) {
                throw new LinkyException("Error requesting '%s' : %s", url, result.getContentAsString());
            }
            return result.getContentAsString();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new LinkyException(e, "Error getting url : '%s'", url);
        }
    }

    public PrmInfo getPrmInfo() throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(PRM_INFO_URL);
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", PRM_INFO_URL);
        }
        try {
            PrmInfo[] prms = gson.fromJson(data, PrmInfo[].class);
            if (prms == null || prms.length < 1) {
                throw new LinkyException("Invalid prms data received");
            }
            return prms[0];
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", PRM_INFO_URL);
        }
    }

    public UserInfo getUserInfo() throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(USER_INFO_URL);
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", USER_INFO_URL);
        }
        try {
            return Objects.requireNonNull(gson.fromJson(data, UserInfo.class));
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching UserInfo.class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", USER_INFO_URL);
        }
    }

    private Consumption getMeasures(String userId, String prmId, LocalDate from, LocalDate to, String request)
            throws LinkyException {
        String url = String.format(MEASURE_URL, userId, prmId, request, from.format(API_DATE_FORMAT),
                to.format(API_DATE_FORMAT));
        if (!connected) {
            initialize();
        }
        String data = getData(url);
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", url);
        }
        logger.trace("getData returned {}", data);
        try {
            ConsumptionReport report = gson.fromJson(data, ConsumptionReport.class);
            if (report == null) {
                throw new LinkyException("No report data received");
            }
            return report.firstLevel.consumptions;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching ConsumptionReport.class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", url);
        }
    }

    public Consumption getEnergyData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(userId, prmId, from, to, "energie");
    }

    public Consumption getPowerData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(userId, prmId, from, to, "pmax");
    }
}
