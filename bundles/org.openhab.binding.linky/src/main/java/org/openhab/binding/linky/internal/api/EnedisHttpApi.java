/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

/**
 * {@link EnedisHttpApi} wraps the Enedis Webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class EnedisHttpApi {
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String URL_APPS_LINCS = "https://apps.lincs.enedis.fr";
    private static final String URL_MON_COMPTE = "https://mon-compte.enedis.fr";
    private static final String URL_ENEDIS_AUTHENTICATE = URL_APPS_LINCS
            + "/authenticate?target=https://mon-compte-particulier.enedis.fr/suivi-de-mesure/";
    private static final String URL_COOKIE = "https://mon-compte-particulier.enedis.fr";

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
        httpClient.getSslContextFactory().setExcludeCipherSuites(new String[0]);
        httpClient.setFollowRedirects(false);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new LinkyException("Unable to start Jetty HttpClient", e);
        }
        connect();
    }

    private void connect() throws LinkyException {
        addCookie(LinkyConfiguration.INTERNAL_AUTH_ID, config.internalAuthId);

        logger.debug("Starting login process for user : {}", config.username);

        try {
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
            Pattern p = Pattern.compile("ReqID%(.*?)%26");
            Matcher m = p.matcher(getLocation(result));
            if (!m.find()) {
                throw new LinkyException("Unable to locate ReqId in header");
            }

            String reqId = m.group(1);
            String url = URL_MON_COMPTE
                    + "/auth/json/authenticate?realm=/enedis&forward=true&spEntityID=SP-ODW-PROD&goto=/auth/SSOPOST/metaAlias/enedis/providerIDP?ReqID%"
                    + reqId
                    + "%26index%3Dnull%26acsURL%3Dhttps://apps.lincs.enedis.fr/saml/SSO%26spEntityID%3DSP-ODW-PROD%26binding%3Durn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST&AMAuthCookie=";

            logger.debug(
                    "Step 3 : auth1 - retrieve the template, thanks to cookie internalAuthId, user is already set");
            result = httpClient.POST(url).send();
            if (result.getStatus() != 200) {
                throw new LinkyException("Connection failed step 3 - auth1 : " + result.getContentAsString());
            }

            AuthData authData = gson.fromJson(result.getContentAsString(), AuthData.class);
            if (authData.callbacks.size() < 2 || authData.callbacks.get(0).input.size() == 0
                    || authData.callbacks.get(1).input.size() == 0
                    || !config.username.contentEquals(authData.callbacks.get(0).input.get(0).valueAsString())) {
                throw new LinkyException("Authentication error, the authentication_cookie is probably wrong");
            }

            authData.callbacks.get(1).input.get(0).value = config.password;
            url = "https://mon-compte.enedis.fr/auth/json/authenticate?realm=/enedis&spEntityID=SP-ODW-PROD&goto=/auth/SSOPOST/metaAlias/enedis/providerIDP?ReqID%"
                    + reqId
                    + "%26index%3Dnull%26acsURL%3Dhttps://apps.lincs.enedis.fr/saml/SSO%26spEntityID%3DSP-ODW-PROD%26binding%3Durn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST&AMAuthCookie=";

            logger.debug("Step 3 : auth2 - send the auth data");
            result = httpClient.POST(url).header(HttpHeader.CONTENT_TYPE, "application/json")
                    .content(new StringContentProvider(gson.toJson(authData))).send();
            if (result.getStatus() != 200) {
                throw new LinkyException("Connection failed step 3 - auth2 : " + result.getContentAsString());
            }

            AuthResult authResult = gson.fromJson(result.getContentAsString(), AuthResult.class);
            logger.debug("Add the tokenId cookie");
            addCookie("enedisExt", authResult.tokenId);

            logger.debug("Step 4 : retrieve the SAMLresponse");
            data = getData(URL_MON_COMPTE + "/" + authResult.successUrl);
            htmlDocument = Jsoup.parse(data);
            el = htmlDocument.select("form").first();
            samlInput = el.select("input[name=SAMLResponse]").first();

            logger.debug("Step 5 : post the SAMLresponse to finish the authentication");
            result = httpClient.POST(el.attr("action")).content(getFormContent("SAMLResponse", samlInput.attr("value")))
                    .send();
            if (result.getStatus() != 302) {
                throw new LinkyException("Connection failed step 5");
            }
            connected = true;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new LinkyException("Error opening connection with Enedis webservice", e);
        }
    }

    public String getLocation(ContentResponse response) {
        return response.getHeaders().get(HttpHeader.LOCATION);
    }

    public void disconnect() throws LinkyException {
        if (connected) {
            try { // Three times in a row to get disconnected
                String location = getLocation(httpClient.GET(URL_APPS_LINCS + "/logout"));
                location = getLocation(httpClient.GET(location));
                location = getLocation(httpClient.GET(location));
                CookieStore cookieStore = httpClient.getCookieStore();
                cookieStore.removeAll();
                connected = false;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new LinkyException("Error while disconnecting from Enedis webservice", e);
            }
        }
    }

    public void dispose() throws LinkyException {
        try {
            disconnect();
            httpClient.stop();
        } catch (Exception e) {
            throw new LinkyException("Error stopping Jetty client", e);
        }
    }

    private void addCookie(String key, String value) {
        CookieStore cookieStore = httpClient.getCookieStore();
        HttpCookie cookie = new HttpCookie(key, value);
        cookie.setDomain(".enedis.fr");
        cookie.setPath("/");
        cookieStore.add(URI.create(URL_COOKIE), cookie);
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
                throw new LinkyException(String.format("Error requesting '%s' : %s", url, result.getContentAsString()));
            }
            return result.getContentAsString();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new LinkyException(String.format("Error getting url : '%s'", url), e);
        }
    }

    public PrmInfo getPrmInfo() throws LinkyException {
        final String prm_info_url = URL_APPS_LINCS + "/mes-mesures/api/private/v1/personnes/null/prms";
        String data = getData(prm_info_url);
        PrmInfo[] prms = gson.fromJson(data, PrmInfo[].class);
        return prms[0];
    }

    public UserInfo getUserInfo() throws LinkyException {
        final String user_info_url = URL_APPS_LINCS + "/userinfos";
        String data = getData(user_info_url);
        return gson.fromJson(data, UserInfo.class);
    }

    private Consumption getMeasures(String userId, String prmId, LocalDate from, LocalDate to, String request)
            throws LinkyException {
        final String measure_url = URL_APPS_LINCS
                + "/mes-mesures/api/private/v1/personnes/%s/prms/%s/donnees-%s?dateDebut=%s&dateFin=%s&mesuretypecode=CONS";
        String url = String.format(measure_url, userId, prmId, request, from.format(API_DATE_FORMAT),
                to.format(API_DATE_FORMAT));
        String data = getData(url);
        ConsumptionReport report = gson.fromJson(data, ConsumptionReport.class);
        return report.firstLevel.consumptions;
    }

    public Consumption getEnergyData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(userId, prmId, from, to, "energie");
    }

    public Consumption getPowerData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(userId, prmId, from, to, "pmax");
    }
}
