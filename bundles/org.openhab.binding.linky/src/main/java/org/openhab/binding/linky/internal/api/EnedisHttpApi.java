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
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.linky.internal.LinkyConfiguration;
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
import org.openhab.binding.linky.internal.dto.UsagePoint;
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

    private static final String BASE_URL = "https://www.myelectricaldata.fr/";
    private static final String CONTRACT_URL = BASE_URL + "contracts";
    private static final String IDENTITY_URL = BASE_URL + "identity";
    private static final String CONTACT_URL = BASE_URL + "contact";
    private static final String ADDRESS_URL = BASE_URL + "addresses";
    private static final String MEASURE_URL = BASE_URL + "%s/%s/start/%s/end/%s/cache";

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
    }

    private String getLocation(ContentResponse response) {
        return response.getHeaders().get(HttpHeader.LOCATION);
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
        try {
            Request request = httpClient.newRequest(url);
            request = request.method(HttpMethod.GET);
            request = request.header("Authorization", config.token);

            ContentResponse result = request.send();
            if (result.getStatus() != 200) {
                throw new LinkyException("Error requesting '%s' : %s", url, result.getContentAsString());
            }
            return result.getContentAsString();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new LinkyException(e, "Error getting url : '%s'", url);
        }
    }

    public PrmInfo getPrmInfo() throws LinkyException {
        PrmInfo result = new PrmInfo();
        Customer customer = getCustomer();
        UsagePoint usagePoint = customer.usagePoints[0];

        result.contractInfo = usagePoint.contracts;
        result.usagePointInfo = usagePoint.usagePoint;
        result.identityInfo = getIdentity();
        result.addressInfo = getAddress();
        result.contactInfo = getContact();

        result.prmId = result.usagePointInfo.usagePointId;
        result.customerId = customer.customerId;

        return result;
    }

    public Customer getCustomer() throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(String.format("%s/%s/cache", CONTRACT_URL, config.prmId));
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

    public AddressInfo getAddress() throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(String.format("%s/%s/cache", ADDRESS_URL, "21454992660003"));
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

    public IdentityInfo getIdentity() throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(String.format("%s/%s/cache", IDENTITY_URL, "21454992660003"));
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

    public ContactInfo getContact() throws LinkyException {
        if (!connected) {
            initialize();
        }
        String data = getData(String.format("%s/%s/cache", CONTACT_URL, "21454992660003"));
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

    private MeterReading getMeasures(String userId, String prmId, LocalDate from, LocalDate to, String request)
            throws LinkyException {

        String dtStart = from.format(API_DATE_FORMAT);
        String dtEnd = to.format(API_DATE_FORMAT);

        String url = String.format(MEASURE_URL, request, prmId, dtStart, dtEnd);
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
        return getMeasures(userId, prmId, from, to, "daily_consumption");
    }

    /*
     * public MeterReading getPowerData(String userId, String prmId, LocalDate from, LocalDate to) throws LinkyException
     * {
     * return getMeasures(userId, prmId, from, to, "daily_consumption_max_power");
     * }
     */
}
