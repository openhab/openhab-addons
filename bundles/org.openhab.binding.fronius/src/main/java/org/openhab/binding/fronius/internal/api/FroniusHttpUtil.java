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
package org.openhab.binding.fronius.internal.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A version of HttpUtil implementation that retries on failure.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class FroniusHttpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FroniusHttpUtil.class);
    private final ReentrantLock requestLock = new ReentrantLock();

    private enum RequestMode {
        CONTROL,
        POLLING
    }

    @FunctionalInterface
    interface RequestExecutor {
        @Nullable
        String execute(HttpMethod httpMethod, String url, @Nullable Properties httpHeaders,
                @Nullable InputStream content, @Nullable String contentType, int timeout) throws IOException;
    }

    /**
     * Issue a HTTP request and retry on failure.
     *
     * @param httpMethod the HTTP method to use
     * @param url the url to execute
     * @param timeout the socket timeout in milliseconds to wait for data
     * @return the response body
     * @throws FroniusCommunicationException when the request execution failed or interrupted
     */
    public String executeUrl(HttpMethod httpMethod, String url, int timeout) throws FroniusCommunicationException {
        return executeUrl(httpMethod, url, null, null, null, timeout);
    }

    /**
     * Issue a HTTP request and retry on failure.
     *
     * @param httpMethod the {@link HttpMethod} to use
     * @param url the url to execute
     * @param httpHeaders optional http request headers which has to be sent within request
     * @param content the content to be sent to the given <code>url</code> or <code>null</code> if no content should be
     *            sent.
     * @param contentType the content type of the given <code>content</code>
     * @param timeout the socket timeout in milliseconds to wait for data
     * @return the response body
     * @throws FroniusCommunicationException when the request execution failed or interrupted
     */
    public String executeUrl(HttpMethod httpMethod, String url, @Nullable Properties httpHeaders,
            @Nullable InputStream content, @Nullable String contentType, int timeout)
            throws FroniusCommunicationException {
        return executeUrl(httpMethod, url, httpHeaders, content, contentType, timeout, this::executeRequest);
    }

    /**
     * Issue a polling HTTP request and skip it when another request for the same bridge is already running.
     *
     * @param httpMethod the HTTP method to use
     * @param url the url to execute
     * @param timeout the socket timeout in milliseconds to wait for data
     * @return the response body
     * @throws FroniusCommunicationException when the request execution failed or interrupted
     */
    public String executePollingUrl(HttpMethod httpMethod, String url, int timeout)
            throws FroniusCommunicationException {
        return executePollingUrl(httpMethod, url, null, null, null, timeout);
    }

    /**
     * Issue a polling HTTP request and skip it when another request for the same bridge is already running.
     *
     * @param httpMethod the HTTP method to use
     * @param url the url to execute
     * @param httpHeaders optional http request headers which has to be sent within request
     * @param content the content to be sent to the given <code>url</code> or <code>null</code> if no content should be
     *            sent.
     * @param contentType the content type of the given <code>content</code>
     * @param timeout the socket timeout in milliseconds to wait for data
     * @return the response body
     * @throws FroniusCommunicationException when the request execution failed or interrupted
     */
    public String executePollingUrl(HttpMethod httpMethod, String url, @Nullable Properties httpHeaders,
            @Nullable InputStream content, @Nullable String contentType, int timeout)
            throws FroniusCommunicationException {
        return executePollingUrl(httpMethod, url, httpHeaders, content, contentType, timeout, this::executeRequest);
    }

    String executeUrl(HttpMethod httpMethod, String url, @Nullable Properties httpHeaders,
            @Nullable InputStream content, @Nullable String contentType, int timeout, RequestExecutor requestExecutor)
            throws FroniusCommunicationException {
        return executeUrl(httpMethod, url, httpHeaders, content, contentType, timeout, RequestMode.CONTROL,
                requestExecutor);
    }

    String executePollingUrl(HttpMethod httpMethod, String url, @Nullable Properties httpHeaders,
            @Nullable InputStream content, @Nullable String contentType, int timeout, RequestExecutor requestExecutor)
            throws FroniusCommunicationException {
        return executeUrl(httpMethod, url, httpHeaders, content, contentType, timeout, RequestMode.POLLING,
                requestExecutor);
    }

    private String executeUrl(HttpMethod httpMethod, String url, @Nullable Properties httpHeaders,
            @Nullable InputStream content, @Nullable String contentType, int timeout, RequestMode requestMode,
            RequestExecutor requestExecutor) throws FroniusCommunicationException {
        acquireLock(requestLock, requestMode, url);
        LOGGER.debug("Executing {} request against {}", requestMode.name().toLowerCase(), url);
        int attemptCount = 1;
        try {
            while (true) {
                Throwable lastException = null;
                String result = null;
                try {
                    result = requestExecutor.execute(httpMethod, url, httpHeaders, content, contentType, timeout);
                } catch (IOException e) {
                    // HttpUtil::executeUrl wraps InterruptedException into IOException.
                    // Unwrap and rethrow it so that we don't retry on InterruptedException
                    Throwable cause = e.getCause();
                    if (cause instanceof InterruptedException interruptException) {
                        throw interruptException;
                    }
                    lastException = e;
                }

                if (result != null) {
                    if (attemptCount > 1) {
                        LOGGER.debug("Attempt #{} successful {}", attemptCount, url);
                    }
                    return result;
                }

                if (attemptCount >= 3) {
                    LOGGER.debug("Failed connecting to {} after {} attempts.", url, attemptCount, lastException);
                    throw new FroniusCommunicationException("Unable to connect", lastException);
                }

                LOGGER.debug("HTTP error on attempt #{} {}", attemptCount, url);
                Thread.sleep(500 * attemptCount);
                attemptCount++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FroniusCommunicationException("Interrupted", e);
        } finally {
            requestLock.unlock();
        }
    }

    private static void acquireLock(ReentrantLock requestLock, RequestMode requestMode, String url)
            throws FroniusCommunicationException {
        try {
            if (requestMode == RequestMode.POLLING) {
                if (!requestLock.tryLock()) {
                    throw new FroniusPollingSkipException("Skipping polling request to '" + url
                            + "' because another request for this Fronius bridge is still running");
                }
            } else {
                requestLock.lockInterruptibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FroniusCommunicationException("Interrupted", e);
        }
    }

    private @Nullable String executeRequest(HttpMethod httpMethod, String url, @Nullable Properties httpHeaders,
            @Nullable InputStream content, @Nullable String contentType, int timeout) throws IOException {
        return HttpUtil.executeUrl(httpMethod.asString(), url, httpHeaders, content, contentType, timeout);
    }
}
