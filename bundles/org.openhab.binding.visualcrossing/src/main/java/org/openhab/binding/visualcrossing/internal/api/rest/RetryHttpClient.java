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
package org.openhab.binding.visualcrossing.internal.api.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApiException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingAuthException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingRateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class RetryHttpClient implements RestClient {
    private final Logger logger = LoggerFactory.getLogger(RetryHttpClient.class);
    private final RestClient restClient;
    private final int maxRetries;

    public RetryHttpClient(RestClient restClient, int maxRetries) {
        this.restClient = restClient;
        if (maxRetries <= 0) {
            throw new IllegalArgumentException("maxRetries cannot be lower or equal to 0, but was " + maxRetries);
        }
        this.maxRetries = maxRetries;
    }

    @Override
    public @Nullable String get(String url, @Nullable Header... headers)
            throws VisualCrossingApiException, VisualCrossingAuthException, VisualCrossingRateException {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return restClient.get(url, headers);
            } catch (VisualCrossingApiException ex) {
                if (i < maxRetries - 1) {
                    logger.debug("Error while calling GET {}. Retrying {}/{}...", url, i + 1, maxRetries, ex);
                } else {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Should not happen!");
    }

    @Override
    public @Nullable String post(String url, Content content, @Nullable Header... headers)
            throws VisualCrossingAuthException, VisualCrossingApiException, VisualCrossingRateException {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return restClient.post(url, content, headers);
            } catch (VisualCrossingApiException ex) {
                if (i < maxRetries - 1) {
                    logger.debug("Error while calling POST {}. Retrying {}/{}...", url, i + 1, maxRetries, ex);
                } else {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Should not happen!");
    }
}
