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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.binding.linky.internal.dto.AddressInfo;
import org.openhab.binding.linky.internal.dto.ContactInfo;
import org.openhab.binding.linky.internal.dto.Customer;
import org.openhab.binding.linky.internal.dto.CustomerIdResponse;
import org.openhab.binding.linky.internal.dto.CustomerReponse;
import org.openhab.binding.linky.internal.dto.IdentityInfo;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.MeterResponse;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.TempoResponse;
import org.openhab.binding.linky.internal.dto.UsagePoint;
import org.openhab.binding.linky.internal.handler.ApiBridgeHandler;
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

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String BASE_URL = "https://ext.prod-sandbox.api.enedis.fr/";

    private static final String CONTRACT_URL = BASE_URL + "customers_upc/v5/usage_points/contracts";
    private static final String IDENTITY_URL = BASE_URL + "customers_i/v5/identity";
    private static final String CONTACT_URL = BASE_URL + "customers_cd/v5/contact_data";
    private static final String ADDRESS_URL = BASE_URL + "customers_upa/v5/usage_points/addresses";
    private static final String MEASURE_DAILY_CONSUMPTION_URL = BASE_URL
            + "metering_data_dc/v5/daily_consumption?usage_point_id=%s&start=%s&end=%s";
    private static final String MEASURE_MAX_POWER_URL = BASE_URL
            + "metering_data_dcmp/v5/daily_consumption_max_power?usage_point_id=%s&start=%s&end=%s";

    private static final String TEMPO_URL = BASE_URL + "rte/tempo/%s/%s";

    private static final String TOKEN_URL = BASE_URL
            + "v1/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=na&user_type=na&state=na&person_id=-1&usage_points_id=%s";

    private final Logger logger = LoggerFactory.getLogger(EnedisHttpApi.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private ApiBridgeHandler apiBridgeHandler;

    private boolean connected = false;

    public EnedisHttpApi(ApiBridgeHandler apiBridgeHandler, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.apiBridgeHandler = apiBridgeHandler;
    }

    public void initialize() throws LinkyException {
    }

    private void disconnect() throws LinkyException {
        if (connected) {
            logger.debug("Logout process");
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void dispose() throws LinkyException {
        disconnect();
    }

    private String getData(String url) throws LinkyException {
        return getData(url, httpClient, apiBridgeHandler.getToken());
    }

    private static String getData(String url, HttpClient httpClient, String token) throws LinkyException {
        try {
            Request request = httpClient.newRequest(url);
            request = request.method(HttpMethod.GET);
            if (!("".equals(token))) {
                request = request.header("Authorization", "Bearer " + token);
                request = request.header("Accept", "application/json");
            }

            ContentResponse result = request.send();
            if (result.getStatus() == 307) {
                String loc = result.getHeaders().get("Location");
                String newUrl = BASE_URL + loc.substring(1);
                request = httpClient.newRequest(newUrl);
                request = request.method(HttpMethod.GET);
                result = request.send();

                if (result.getStatus() == 307) {
                    loc = result.getHeaders().get("Location");
                    String res = loc.split("/")[3];
                    return res;
                }
            }
            if (result.getStatus() != 200) {
                throw new LinkyException("Error requesting '%s' : %s", url, result.getContentAsString());
            }
            return result.getContentAsString();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new LinkyException(e, "Error getting url : '%s'", url);
        }
    }

    public PrmInfo getPrmInfo(String prmId) throws LinkyException {
        PrmInfo result = new PrmInfo();
        Customer customer = getCustomer(prmId);
        UsagePoint usagePoint = customer.usagePoints[0];

        result.contractInfo = usagePoint.contracts;
        result.usagePointInfo = usagePoint.usagePoint;
        result.identityInfo = getIdentity(prmId);
        result.addressInfo = getAddress(prmId);
        result.contactInfo = getContact(prmId);

        result.prmId = result.usagePointInfo.usagePointId;
        result.customerId = customer.customerId;

        return result;
    }

    public String formatUrl(String apiUrl, String prmId) {
        // return "%s/%s/cache".formatted(apiUrl, config.prmId);
        return "%s?usage_point_id=%s".formatted(apiUrl, prmId);
    }

    public Customer getCustomer(String prmId) throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(formatUrl(CONTRACT_URL, prmId));
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", CONTRACT_URL);
        }
        try {
            CustomerReponse cResponse = gson.fromJson(data, CustomerReponse.class);
            if (cResponse == null) {
                throw new LinkyException("Invalid customer data received");
            }
            return cResponse.customer;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", CONTRACT_URL);
        }
    }

    public AddressInfo getAddress(String prmId) throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(formatUrl(ADDRESS_URL, prmId));
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", ADDRESS_URL);
        }
        try {
            CustomerReponse cResponse = gson.fromJson(data, CustomerReponse.class);
            if (cResponse == null) {
                throw new LinkyException("Invalid customer data received");
            }
            return cResponse.customer.usagePoints[0].usagePoint.usagePointAddresses;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", ADDRESS_URL);
        }
    }

    public IdentityInfo getIdentity(String prmId) throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(formatUrl(IDENTITY_URL, prmId));
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", IDENTITY_URL);
        }
        try {
            CustomerIdResponse iResponse = gson.fromJson(data, CustomerIdResponse.class);
            if (iResponse == null) {
                throw new LinkyException("Invalid customer data received");
            }
            return iResponse.identity.naturalPerson;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", IDENTITY_URL);
        }
    }

    public ContactInfo getContact(String prmId) throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(formatUrl(CONTACT_URL, prmId));

        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", CONTACT_URL);
        }
        try {
            CustomerIdResponse cResponse = gson.fromJson(data, CustomerIdResponse.class);
            if (cResponse == null) {
                throw new LinkyException("Invalid customer data received");
            }
            return cResponse.contactData;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", CONTACT_URL);
        }
    }

    private MeterReading getMeasures(String apiUrl, String userId, String prmId, LocalDate from, LocalDate to)
            throws LinkyException {
        String dtStart = from.format(API_DATE_FORMAT);
        String dtEnd = to.format(API_DATE_FORMAT);

        String url = String.format(apiUrl, prmId, dtStart, dtEnd);
        if (!connected) {
            initialize();
        }
        String data = getData(url);
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", url);
        }
        logger.trace("getData returned {}", data);
        try {
            MeterResponse meterResponse = gson.fromJson(data, MeterResponse.class);
            if (meterResponse == null) {
                throw new LinkyException("No report data received");
            }
            return meterResponse.meterReading;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching ConsumptionReport.class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", url);
        }
    }

    public MeterReading getEnergyData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(MEASURE_DAILY_CONSUMPTION_URL, userId, prmId, from, to);
    }

    public MeterReading getPowerData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(MEASURE_MAX_POWER_URL, userId, prmId, from, to);
    }

    public String getTempoData() throws LinkyException {
        String url = String.format(TEMPO_URL, "2024-01-01", "2024-01-31");
        if (!connected) {
            initialize();
        }
        String data = getData(url);
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", url);
        }
        logger.trace("getData returned {}", data);

        try {
            TempoResponse tempResponse = gson.fromJson(data, TempoResponse.class);
            if (tempResponse == null) {
                throw new LinkyException("No report data received");
            }

            return "{\"2024-01-20\":\"WHITE\",\"2024-01-21\":\"RED\",\"array\":[\"2024-01-20\",\"2024-01-21\"]}";
            // return tempResponse.tempoDayInfo;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching ConsumptionReport.class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", url);
        }

        // return data;
    }

    public static String getToken(HttpClient httpClient, String clientId, String prmId) {
        try {
            String url = String.format(TOKEN_URL, clientId, prmId);
            String token = getData(url, httpClient, "");
            return token;
        } catch (LinkyException e) {
            return "";
        }
    }
}
