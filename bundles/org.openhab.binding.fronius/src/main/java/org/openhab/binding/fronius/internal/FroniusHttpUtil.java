/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A version of HttpUtil implementation that retries on failure
 *
 * @author Jimmy Tanagra - Initial contribution
 * 
 */
@NonNullByDefault
public class FroniusHttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(FroniusHttpUtil.class);

    /**
     * Issue a HTTP GET request and retry on failure
     *
     * @param url the url to execute
     * @param timeout the socket timeout in milliseconds to wait for data
     * @return the response body
     * @throws FroniusCommunicationException when the request execution failed or interrupted
     */
    public static synchronized String executeUrl(String url, int timeout) throws FroniusCommunicationException {
        int attemptCount = 1;
        try {
            while (true) {
                Throwable lastException = null;
                String result = null;
                try {
                    result = HttpUtil.executeUrl("GET", url, timeout);
                } catch (IOException e) {
                    // HttpUtil::executeUrl wraps InterruptedException into IOException.
                    // Unwrap and rethrow it so that we don't retry on InterruptedException
                    if (e.getCause() instanceof InterruptedException) {
                        throw (InterruptedException) e.getCause();
                    }
                    lastException = e;
                }

                if (result != null) {
                    if (attemptCount > 1) {
                        logger.debug("Attempt #{} successful {}", attemptCount, url);
                    }
                    return result;
                }

                if (attemptCount >= 3) {
                    logger.debug("Failed connecting to {} after {} attempts.", url, attemptCount, lastException);
                    throw new FroniusCommunicationException("Unable to connect", lastException);
                }

                logger.debug("HTTP error on attempt #{} {}", attemptCount, url);
                Thread.sleep(500 * attemptCount);
                attemptCount++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FroniusCommunicationException("Interrupted", e);
        }
    }
}
