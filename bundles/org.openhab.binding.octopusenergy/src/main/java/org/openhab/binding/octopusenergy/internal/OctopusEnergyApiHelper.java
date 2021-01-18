/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.octopusenergy.internal.dto.Accounts;
import org.openhab.binding.octopusenergy.internal.dto.Consumption;
import org.openhab.binding.octopusenergy.internal.dto.ElectricityMeterPoint;
import org.openhab.binding.octopusenergy.internal.dto.Meter;
import org.openhab.binding.octopusenergy.internal.dto.PagedResultSet;
import org.openhab.binding.octopusenergy.internal.dto.Price;
import org.openhab.binding.octopusenergy.internal.exception.ApiException;
import org.openhab.binding.octopusenergy.internal.exception.AuthenticationException;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link OctopusEnergyApiHelper} is a helper class to abstract the Octopus Energy API. It handles authentication
 * and
 * all JSON API calls.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class OctopusEnergyApiHelper {

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyApiHelper.class);

    private static final String API_USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0; SM-G930F Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/64.0.3282.137 Mobile Safari/537.36";

    private static final String API_URL = "https://api.octopus.energy/v1/";
    private static final String ACCOUNTS_URL = API_URL + "accounts/";
    private static final String ELECTRICITY_METER_POINT_URL = API_URL + "electricity-meter-points/";
    private static final String PRODUCT_POINT_URL = API_URL + "products/";
    private static final int DEFAULT_QUERY_PAGE_SIZE = 10; // Query a maximum of 10 results
    private static final int DEFAULT_PRICE_QUERY_PAGE_SIZE = 100; // We want up to 48h half-hourly readings
    private static final int DEFAULT_QUERY_WINDOW = 48; // Most smart meters only communicate sporadically. 48 hours
                                                        // should provide enough time to have at least a small number of
                                                        // readings.

    private static final DateTimeFormatter ZULU_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private String accountNumber = "";
    private String apiKey = "";

    private boolean authenticated = false;

    private @NonNullByDefault({}) HttpClient httpClient;
    private Accounts accounts = new Accounts();

    /**
     * Sets the httpClient object to be used for API calls.
     *
     * @param httpClient the client to be used.
     */
    public void setHttpClient(@Nullable HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sets the account number to be used for API calls.
     *
     * @param accountNumber the user's account number.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Sets the API key to be used for API calls.
     *
     * @param apiKey the user's API key.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the accounts.
     *
     * @return the topology
     */
    public final Accounts getAccounts() {
        return accounts;
    }

    /**
     * Refreshes the accounts, i.e. all meter points, through a call to the Octopus Energy API.
     */
    public synchronized void updateAccounts() throws ApiException {
        try {
            String url = ACCOUNTS_URL + accountNumber + "/";
            Accounts cache = OctopusEnergyBindingConstants.GSON.fromJson(getDataFromApi(url), Accounts.class);
            if (cache != null) {
                accounts = cache;
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Exception caught during accounts cache update: {}", e);
        }
    }

    /**
     * Refreshes the consumption for each meter point
     */
    public synchronized void updateElectricityConsumption() throws ApiException {
        try {
            ZonedDateTime endTime = ZonedDateTime.now();
            ZonedDateTime startTime = endTime.minus(DEFAULT_QUERY_WINDOW, ChronoUnit.HOURS);
            for (ElectricityMeterPoint emp : accounts.getElectricityMeterPoints()) {
                emp.consumptionList = new ArrayList<Consumption>();
                for (Meter m : emp.meters) {
                    StringBuilder url = new StringBuilder(ELECTRICITY_METER_POINT_URL);
                    url.append(emp.mpan);
                    url.append("/meters/");
                    url.append(m.serialNumber);
                    url.append("/consumption/?page_size=");
                    url.append(DEFAULT_QUERY_PAGE_SIZE);
                    url.append("&period_from=");
                    url.append(ZULU_TIME_FORMATTER.format(startTime));
                    url.append("&period_to=");
                    url.append(ZULU_TIME_FORMATTER.format(endTime));
                    url.append("&order_by=-period");
                    Type collectionType = new TypeToken<PagedResultSet<Consumption>>() {
                    }.getType();
                    PagedResultSet<Consumption> cache = OctopusEnergyBindingConstants.GSON
                            .fromJson(getDataFromApi(url.toString()), collectionType);
                    if (cache != null) {
                        m.consumptionList = cache.results;
                        // in most cases, we only have 1 active meter per meter point, but the model allows for having
                        // more than one meter, we therefore aggregate the consumption results by meter point
                        emp.consumptionList = Consumption.aggregate(emp.consumptionList, m.consumptionList);
                    }
                }
                logger.debug("# of consumptions: {}, most recent: {}", emp.consumptionList.size(),
                        emp.consumptionList.get(emp.consumptionList.size() - 1));
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Exception caught during accounts cache update: {}", e);
        }
    }

    /**
     * Refreshes the consumption for each meter point
     */
    public synchronized void updateElectricityPrices() throws ApiException {
        try {
            ZonedDateTime endTime = ZonedDateTime.now().plus(DEFAULT_QUERY_WINDOW, ChronoUnit.HOURS);
            ZonedDateTime startTime = ZonedDateTime.now();
            for (ElectricityMeterPoint emp : accounts.getElectricityMeterPoints()) {
                StringBuilder url = new StringBuilder(PRODUCT_POINT_URL);
                url.append(emp.getProductAsOf(startTime));
                url.append("/electricity-tariffs/");
                url.append(emp.getAgreementAsOf(startTime).tariffCode);
                url.append("/standard-unit-rates/?page_size=");
                url.append(DEFAULT_PRICE_QUERY_PAGE_SIZE);
                url.append("&period_from=");
                url.append(ZULU_TIME_FORMATTER.format(startTime));
                url.append("&period_to=");
                url.append(ZULU_TIME_FORMATTER.format(endTime));
                Type collectionType = new TypeToken<PagedResultSet<Price>>() {
                }.getType();
                PagedResultSet<Price> cache = OctopusEnergyBindingConstants.GSON
                        .fromJson(getDataFromApi(url.toString()), collectionType);
                if (cache != null) {
                    emp.priceList = cache.results;
                    Collections.sort(emp.priceList, Price.INTERVAL_START_ORDER_ASC);
                    logger.debug("# of prices: {}, furthest: {}", emp.priceList.size(),
                            emp.priceList.get(emp.priceList.size() - 1));
                }
            }
        } catch (JsonSyntaxException | RecordNotFoundException e) {
            throw new ApiException("Exception caught during accounts cache update: {}", e);
        }
    }

    /**
     * Return the "data" element of the API result as a JsonElement.
     *
     * @param url The URL of the API call.
     * @return The "data" element of the API result.
     * @throws ApiException
     */
    private JsonElement getDataFromApi(String url) throws ApiException {
        logger.debug("getting data for URL: {}", url);
        if (!authenticated) {
            AuthenticationStore authenticationStore = httpClient.getAuthenticationStore();
            logger.debug("creating BASIC authentication: '{}:'", apiKey);
            BasicAuthentication authentication = new BasicAuthentication(URI.create(API_URL), Authentication.ANY_REALM,
                    apiKey, "");
            authenticationStore.addAuthentication(authentication);
            authenticated = true;
        }
        Request request = httpClient.newRequest(url).method(HttpMethod.GET);
        try {
            request.header(HttpHeader.ACCEPT, "application/json, text/plain, */*");
            request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate");
            request.header(HttpHeader.CONTENT_TYPE, "application/json; utf-8");
            request.header(HttpHeader.USER_AGENT, API_USER_AGENT);
            ContentResponse response = request.send();
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                String responseData = response.getContentAsString();
                logger.debug("API execution successful, response: {}", responseData);

                JsonParser parser = new JsonParser();
                JsonElement object = parser.parse(responseData);
                return object;
            } else if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new AuthenticationException("Invalid account number/API key combination");
            } else {
                logger.debug("HTTP Response: {} - {}", response.getStatus(), response.getReason());
                throw new ApiException(
                        "Unexpected Http response: " + response.getStatus() + " - " + response.getReason());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ApiException("Exception caught during API execution." + e);
        }
    }
}
