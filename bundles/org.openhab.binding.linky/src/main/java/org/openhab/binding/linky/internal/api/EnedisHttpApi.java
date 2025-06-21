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
package org.openhab.binding.linky.internal.api;

import java.net.HttpCookie;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.linky.internal.dto.ConsumptionReport;
import org.openhab.binding.linky.internal.dto.Contact;
import org.openhab.binding.linky.internal.dto.Contract;
import org.openhab.binding.linky.internal.dto.Identity;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.PrmDetail;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.ResponseContact;
import org.openhab.binding.linky.internal.dto.ResponseContract;
import org.openhab.binding.linky.internal.dto.ResponseIdentity;
import org.openhab.binding.linky.internal.dto.ResponseMeter;
import org.openhab.binding.linky.internal.dto.ResponseTempo;
import org.openhab.binding.linky.internal.dto.UsagePoint;
import org.openhab.binding.linky.internal.dto.UserInfo;
import org.openhab.binding.linky.internal.handler.BridgeRemoteBaseHandler;
import org.openhab.binding.linky.internal.handler.BridgeRemoteEnedisWebHandler;
import org.openhab.binding.linky.internal.handler.ThingBaseRemoteHandler;
import org.openhab.binding.linky.internal.handler.ThingLinkyRemoteHandler;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link EnedisHttpApi} wraps the Enedis Webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
public class EnedisHttpApi {

    private final Logger logger = LoggerFactory.getLogger(EnedisHttpApi.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final BridgeRemoteBaseHandler linkyBridgeHandler;

    public EnedisHttpApi(BridgeRemoteBaseHandler linkyBridgeHandler, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.linkyBridgeHandler = linkyBridgeHandler;
    }

    public FormContentProvider getFormContent(String fieldName, String fieldValue) {
        Fields fields = new Fields();
        fields.put(fieldName, fieldValue);
        return new FormContentProvider(fields);
    }

    public void addCookie(String key, String value) {
        HttpCookie cookie = new HttpCookie(key, value);
        cookie.setDomain(BridgeRemoteEnedisWebHandler.ENEDIS_DOMAIN);
        cookie.setPath("/");
        httpClient.getCookieStore().add(BridgeRemoteEnedisWebHandler.COOKIE_URI, cookie);
    }

    public void removeAllCookie() {
        httpClient.getCookieStore().removeAll();
    }

    public String getLocation(ContentResponse response) {
        return response.getHeaders().get(HttpHeader.LOCATION);
    }

    public String getContent(ThingBaseRemoteHandler handler, String url) throws LinkyException {
        return getContent(logger, linkyBridgeHandler, url, httpClient, linkyBridgeHandler.getToken(handler));
    }

    public String getContent(String url) throws LinkyException {
        return getContent(logger, linkyBridgeHandler, url, httpClient, "");
    }

    private static String getContent(Logger logger, BridgeRemoteBaseHandler linkyBridgeHandler, String url,
            HttpClient httpClient, String token) throws LinkyException {
        try {
            Request request = httpClient.newRequest(url);

            request = request.agent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
            request = request.method(HttpMethod.GET);
            if (!token.isEmpty()) {
                request = request.header("Authorization", "" + token);
                request = request.header("Accept", "application/json");
            }

            ContentResponse result = request.send();
            if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                    || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                String loc = result.getHeaders().get("Location");
                String newUrl = "";

                if (loc.startsWith("http://") || loc.startsWith("https://")) {
                    newUrl = loc;
                } else {
                    newUrl = linkyBridgeHandler.getBaseUrl() + loc.substring(1);
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
            if (result.getStatus() != 200) {
                throw new LinkyException("Error requesting '%s': %s", url, result.getContentAsString());
            }

            String content = result.getContentAsString();
            logger.trace("getContent returned {}", content);
            return content;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new LinkyException(e, "Error getting url: '%s'", url);
        }
    }

    private <T> T getData(ThingBaseRemoteHandler handler, String url, Class<T> clazz) throws LinkyException {
        if (!linkyBridgeHandler.isConnected()) {
            linkyBridgeHandler.initialize();
        }

        int numberRetry = 0;
        LinkyException lastException = null;
        logger.debug("getData begin {}: {}", clazz.getName(), url);

        while (numberRetry < 3) {
            try {
                String data = getContent(handler, url);

                if (!data.isEmpty()) {
                    try {
                        T result = Objects.requireNonNull(gson.fromJson(data, clazz));
                        logger.trace("getData success {}: {}", clazz.getName(), url);
                        return result;
                    } catch (JsonSyntaxException e) {
                        logger.debug("Invalid JSON response not matching {}: {}", clazz.getName(), data);
                        throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", url);
                    }
                }
            } catch (LinkyException ex) {
                lastException = ex;

                logger.debug("getData error {}: {} , retry{}", clazz.getName(), url, numberRetry);

                // try to reinit connection, fail after 3 attemps
                linkyBridgeHandler.connectionInit();
            }
            numberRetry++;
        }

        logger.debug("getData error {}: {} , maxRetry", clazz.getName(), url);

        throw Objects.requireNonNull(lastException);
    }

    public PrmInfo getPrmInfo(ThingLinkyRemoteHandler handler, String internId, String prmId) throws LinkyException {
        String prmInfoUrl = linkyBridgeHandler.getContractUrl().formatted(internId);
        PrmInfo[] prms = getData(handler, prmInfoUrl, PrmInfo[].class);
        if (prms.length < 1) {
            throw new LinkyException("Invalid prms data received");
        }

        if (prmId.isBlank()) {
            return prms[0];
        }

        Optional<PrmInfo> result = Arrays.stream(prms).filter(x -> x.idPrm.equals(prmId)).findFirst();
        if (result.isPresent()) {
            return result.get();
        }

        throw new LinkyException(("PRM with id : %s does not exist").formatted(prmId));
    }

    public PrmDetail getPrmDetails(ThingLinkyRemoteHandler handler, String internId, String prmId)
            throws LinkyException {
        String prmInfoUrl = linkyBridgeHandler.getContractUrl();
        String url = prmInfoUrl.formatted(internId) + "/" + prmId
                + "?embed=SITALI&embed=SITCOM&embed=SITCON&embed=SYNCON";
        return getData(handler, url, PrmDetail.class);
    }

    public UserInfo getUserInfo(ThingLinkyRemoteHandler handler) throws LinkyException {
        String userInfoUrl = linkyBridgeHandler.getContactUrl();
        return getData(handler, userInfoUrl, UserInfo.class);
    }

    public String formatUrl(String apiUrl, String prmId) {
        return apiUrl.formatted(prmId);
    }

    public Contract getContract(ThingLinkyRemoteHandler handler, String prmId) throws LinkyException {
        String contractUrl = linkyBridgeHandler.getContractUrl().formatted(prmId);
        ResponseContract contractResponse = getData(handler, contractUrl, ResponseContract.class);
        return contractResponse.customer.usagePoint[0].contracts;
    }

    public UsagePoint getUsagePoint(ThingLinkyRemoteHandler handler, String prmId) throws LinkyException {
        String addressUrl = linkyBridgeHandler.getAddressUrl().formatted(prmId);
        ResponseContract contractResponse = getData(handler, addressUrl, ResponseContract.class);
        return contractResponse.customer.usagePoint[0].usagePoint;
    }

    public Identity getIdentity(ThingLinkyRemoteHandler handler, String prmId) throws LinkyException {
        String identityUrl = linkyBridgeHandler.getIdentityUrl().formatted(prmId);
        ResponseIdentity customerIdReponse = getData(handler, identityUrl, ResponseIdentity.class);
        String name = customerIdReponse.identity.naturalPerson.lastname;
        String[] nameParts = name.split(" ");
        if (nameParts.length > 1) {
            customerIdReponse.identity.naturalPerson.firstname = name.split(" ")[0];
            customerIdReponse.identity.naturalPerson.lastname = name.split(" ")[1];
        }
        return customerIdReponse.identity.naturalPerson;
    }

    public Contact getContact(ThingLinkyRemoteHandler handler, String prmId) throws LinkyException {
        String contactUrl = linkyBridgeHandler.getContactUrl().formatted(prmId);
        ResponseContact contactResponse = getData(handler, contactUrl, ResponseContact.class);
        return contactResponse.contact;
    }

    private MeterReading getMeasures(ThingLinkyRemoteHandler handler, String apiUrl, String mps, String prmId,
            LocalDate from, LocalDate to) throws LinkyException {
        String dtStart = from.format(linkyBridgeHandler.getApiDateFormat());
        String dtEnd = to.format(linkyBridgeHandler.getApiDateFormat());

        if (handler.supportNewApiFormat()) {
            String url = String.format(apiUrl, prmId, dtStart, dtEnd);
            ResponseMeter meterResponse = getData(handler, url, ResponseMeter.class);
            return meterResponse.meterReading;
        } else {
            String url = String.format(apiUrl, mps, prmId, dtStart, dtEnd);
            ConsumptionReport consomptionReport = getData(handler, url, ConsumptionReport.class);
            return MeterReading.convertFromComsumptionReport(consomptionReport);
        }
    }

    public MeterReading getEnergyData(ThingLinkyRemoteHandler handler, String mps, String prmId, LocalDate from,
            LocalDate to) throws LinkyException {
        return getMeasures(handler, linkyBridgeHandler.getDailyConsumptionUrl(), mps, prmId, from, to);
    }

    public MeterReading getLoadCurveData(ThingLinkyRemoteHandler handler, String mps, String prmId, LocalDate from,
            LocalDate to) throws LinkyException {
        return getMeasures(handler, linkyBridgeHandler.getLoadCurveUrl(), mps, prmId, from, to);
    }

    public MeterReading getPowerData(ThingLinkyRemoteHandler handler, String mps, String prmId, LocalDate from,
            LocalDate to) throws LinkyException {
        return getMeasures(handler, linkyBridgeHandler.getMaxPowerUrl(), mps, prmId, from, to);
    }

    public ResponseTempo getTempoData(ThingBaseRemoteHandler handler, LocalDate from, LocalDate to)
            throws LinkyException {
        String dtStart = from.format(linkyBridgeHandler.getApiDateFormatYearsFirst());
        String dtEnd = to.format(linkyBridgeHandler.getApiDateFormatYearsFirst());

        String url = String.format(linkyBridgeHandler.getTempoUrl(), dtStart, dtEnd);

        if (url.isEmpty()) {
            return new ResponseTempo();
        }

        ResponseTempo responseTempo = getData(handler, url, ResponseTempo.class);
        return responseTempo;
    }
}
