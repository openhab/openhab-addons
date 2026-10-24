/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal.api;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.awattar.internal.AwattarBridgeConfiguration;
import org.openhab.binding.awattar.internal.AwattarPrice;
import org.openhab.binding.awattar.internal.dto.AwattarTimeProvider;
import org.openhab.binding.awattar.internal.handler.TimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Base class for {@link MarketPriceApi} implementations. Handles the HTTP request and the conversion of market prices
 * into {@link AwattarPrice} objects using the bridge configuration.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMarketPriceApi implements MarketPriceApi {
    private static final int REQUEST_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final HttpClient httpClient;
    protected final AwattarTimeProvider timeProvider;
    protected final Gson gson = new Gson();

    private final double vatFactor;
    private final double basePrice;
    private final double serviceFee;

    protected AbstractMarketPriceApi(HttpClient httpClient, AwattarTimeProvider timeProvider,
            AwattarBridgeConfiguration config) {
        this.httpClient = httpClient;
        this.timeProvider = timeProvider;

        vatFactor = 1 + (config.vatPercent / 100);
        basePrice = config.basePrice;
        serviceFee = config.serviceFee;
    }

    /**
     * Convert a market price into an {@link AwattarPrice}, applying base price, service fee and VAT.
     *
     * @param marketPrice the net market price in €/MWh, as returned by the APIs
     * @param timeRange the time range the price is valid for
     *
     * @return the price in €ct/kWh
     */
    protected AwattarPrice toPrice(double marketPrice, TimeRange timeRange) {
        // €/MWh -> €ct/kWh: divide by 10 (100/1000)
        double netMarket = marketPrice / 10.0;
        double grossMarket = netMarket * vatFactor;
        double netTotal = netMarket + basePrice;

        // add service fee (e.g. the aWATTar "Ausgleichskomponente")
        if (serviceFee > 0) {
            netTotal += Math.abs(netTotal) * (serviceFee / 100);
        }

        double grossTotal = netTotal * vatFactor;

        return new AwattarPrice(netMarket, grossMarket, netTotal, grossTotal, timeRange);
    }

    /**
     * Request the given URL and return the response body.
     *
     * @param url the URL to request
     * @param statusErrorKey the i18n key used when the server does not respond with status 200, with the status code
     *            as its first argument
     *
     * @return the response body
     *
     * @throws MarketPriceApiException if the request fails or the response is empty or not OK
     */
    protected String fetch(String url, String statusErrorKey) throws MarketPriceApiException {
        logger.trace("API request: '{}'", url);

        try {
            ContentResponse response = httpClient.newRequest(url).method(GET)
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int httpStatus = response.getStatus();
            String content = response.getContentAsString();
            logger.trace("API response: status = {}, content = '{}'", httpStatus, content);

            if (content == null) {
                throw new MarketPriceApiException("@text/error.empty.data");
            }
            if (httpStatus != OK_200) {
                throw new MarketPriceApiException("@text/" + statusErrorKey + " [\"" + httpStatus + "\"]");
            }
            return content;
        } catch (ExecutionException e) {
            throw new MarketPriceApiException("@text/error.execution");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MarketPriceApiException("@text/error.interrupted");
        } catch (TimeoutException e) {
            throw new MarketPriceApiException("@text/error.timeout");
        }
    }
}
