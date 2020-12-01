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
package org.openhab.binding.sony.internal.transports;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sony.internal.SonyBindingConstants;
import org.openhab.binding.sony.internal.net.Header;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This implementation of a sony transport will simply communicate over HTTP (or HTTPS)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyHttpTransport extends AbstractSonyTransport {
    /** The logger */
    protected Logger logger = LoggerFactory.getLogger(SonyHttpTransport.class);

    /** The HTTP request object to use */
    private final HttpRequest requestor;

    /** GSON used to serialize/deserialize objects */
    private final Gson gson;

    /**
     * Constructs the transport using the specified URL and gson (for serialization)
     * 
     * @param baseUrl a non-null base URL
     * @param gson a non-null GSON to use for serialzation
     * @throws URISyntaxException if the base URL has a bad syntax
     */
    public SonyHttpTransport(final String baseUrl, final Gson gson) throws URISyntaxException {
        super(new URI(baseUrl));
        Objects.requireNonNull(gson, "gson cannot be null");

        requestor = new HttpRequest();

        requestor.addHeader("User-Agent", SonyBindingConstants.NET_USERAGENT);
        requestor.addHeader("X-CERS-DEVICE-INFO", SonyBindingConstants.NET_USERAGENT);
        requestor.addHeader("X-CERS-DEVICE-ID", NetUtil.getDeviceId());
        requestor.addHeader("Connection", "close");

        this.requestor.register(new SonyContentTypeFilter());
        this.requestor.register(new SonyAuthFilter(getBaseUri(), () -> {
            final boolean authNeeded = getOptions(TransportOptionAutoAuth.class).stream()
                    .anyMatch(e -> e == TransportOptionAutoAuth.TRUE);
            return authNeeded;
        }));
        this.setOption(TransportOptionAutoAuth.FALSE);

        this.gson = gson;
    }

    @Override
    public CompletableFuture<? extends TransportResult> execute(final TransportPayload payload,
            final TransportOption... options) {
        Objects.requireNonNull(payload, "payload cannot be null");

        final TransportOptionAutoAuth oldAutoAuth = getOptions(TransportOptionAutoAuth.class).stream().findFirst()
                .orElse(TransportOptionAutoAuth.FALSE);
        final TransportOptionAutoAuth newAutoAuth = getOptions(TransportOptionAutoAuth.class, options).stream()
                .findFirst().orElse(oldAutoAuth);

        try {
            if (oldAutoAuth != newAutoAuth) {
                setOption(newAutoAuth);
            }

            final TransportOptionMethod method = getOptions(TransportOptionMethod.class, options).stream().findFirst()
                    .orElse(TransportOptionMethod.POST_JSON);

            if (method == TransportOptionMethod.GET) {
                if (!(payload instanceof TransportPayloadHttp)) {
                    throw new IllegalArgumentException(
                            "payload must be a TransportPayloadHttp: " + payload.getClass().getName());
                }

                return executeGet((TransportPayloadHttp) payload, options);
            } else if (method == TransportOptionMethod.DELETE) {
                if (!(payload instanceof TransportPayloadHttp)) {
                    throw new IllegalArgumentException(
                            "payload must be a TransportPayloadHttp: " + payload.getClass().getName());
                }

                return executeDelete((TransportPayloadHttp) payload, options);
            } else if (method == TransportOptionMethod.POST_XML) {
                if (!(payload instanceof TransportPayloadHttp)) {
                    throw new IllegalArgumentException(
                            "payload must be a TransportPayloadHttp: " + payload.getClass().getName());
                }

                return executePostXml((TransportPayloadHttp) payload, options);
            } else {
                if (payload instanceof TransportPayloadScalarWebRequest) {
                    return executePostJson((TransportPayloadScalarWebRequest) payload, options).thenApply(r -> {
                        if (r.getResponse().getHttpCode() == HttpStatus.OK_200) {
                            final String content = r.getResponse().getContent();
                            final ScalarWebResult res = gson.fromJson(content, ScalarWebResult.class);
                            return new TransportResultScalarWebResult(res);
                        } else {
                            return new TransportResultScalarWebResult(new ScalarWebResult(r.getResponse()));
                        }
                    });
                } else if (payload instanceof TransportPayloadHttp) {
                    return executePostJson((TransportPayloadHttp) payload, options);
                } else {
                    throw new IllegalArgumentException(
                            "payload must be a TransportPayloadHttp or TransportPayloadScalarWebRequest: "
                                    + payload.getClass().getName());
                }
            }
        } finally {
            if (oldAutoAuth != newAutoAuth) {
                setOption(oldAutoAuth);
            }
        }
    }

    /**
     * A helper method to execute a GET on a specific URL. This helper function simply allows us to call the GET with a
     * string URL (rather than a payload implementation) and return an {@link HttpResponse} (rather than a transport
     * result implementation)
     * 
     * @param url a non-null, non-empty string url to execute
     * @param options any transport options to use
     * @return a non-null {@link HttpResponse}
     */
    public HttpResponse executeGet(final String url, final TransportOption... options) {
        Validate.notEmpty(url, "url cannot be empty");
        return execute(url, append(options, TransportOptionMethod.GET));
    }

    /**
     * A helper method to execute a DELETE on a specific URL. This helper function simply allows us to call the DETELE
     * with a string URL (rather than a payload implementation) and return an {@link HttpResponse} (rather than a
     * transport result implementation)
     * 
     * @param url a non-null, non-empty string url to execute
     * @param options any transport options to use
     * @return a non-null {@link HttpResponse}
     */
    public HttpResponse executeDelete(final String url, final TransportOption... options) {
        Validate.notEmpty(url, "url cannot be empty");
        return execute(url, append(options, TransportOptionMethod.DELETE));
    }

    /**
     * A helper method to execute a POST on a specific URL and a string JSON payload. This helper function simply allows
     * us to call the POST with a string URL and JSON payload (rather than a payload implementation) and return an
     * {@link HttpResponse} (rather than a transport result implementation)
     * 
     * @param url a non-null, non-empty string url to execute
     * @param payload a non-null, non-empty string payload to send
     * @param options any transport options to use
     * @return a non-null {@link HttpResponse}
     */
    public HttpResponse executePostJson(final String url, final String payload, final TransportOption... options) {
        Validate.notEmpty(url, "url cannot be empty");
        Validate.notEmpty(payload, "payload cannot be empty");

        return execute(url, new TransportPayloadHttp(url, payload), append(options, TransportOptionMethod.POST_JSON));
    }

    /**
     * A helper method to execute a POST on a specific URL and a string XML payload. This helper function simply allows
     * us to call the POST with a string URL and XML payload (rather than a payload implementation) and return an
     * {@link HttpResponse} (rather than a transport result implementation)
     * 
     * @param url a non-null, non-empty string url to execute
     * @param payload a non-null, possibly empty string payload to send
     * @param options any transport options to use
     * @return a non-null {@link HttpResponse}
     */
    public HttpResponse executePostXml(final String url, final String payload, final TransportOption... options) {
        Validate.notEmpty(url, "url cannot be empty");
        Objects.requireNonNull(payload, "payload cannot be null");

        return execute(url, new TransportPayloadHttp(url, payload), append(options, TransportOptionMethod.POST_XML));
    }

    /**
     * Execute the give URL with the specified options
     * 
     * @param url a non-null, non-empty string url to execute
     * @param options any transport options to use
     * @return a non-null {@link HttpResponse}
     */
    private HttpResponse execute(final String url, final TransportOption... options) {
        Validate.notEmpty(url, "url cannot be empty");
        return execute(url, new TransportPayloadHttp(url), options);
    }

    /**
     * Execute the give URL with the specified options
     * 
     * @param url a non-null, non-empty string url to execute
     * @param payload a non-null, possibly empty string payload to send
     * @param options any transport options to use
     * @return a non-null {@link HttpResponse}
     */
    private HttpResponse execute(final String url, final TransportPayloadHttp payload,
            final TransportOption... options) {
        Validate.notEmpty(url, "url cannot be empty");
        Objects.requireNonNull(payload, "payload cannot be null");

        try {
            final TransportResult result = execute(payload, options).get(SonyBindingConstants.RSP_WAIT_TIMEOUTSECONDS,
                    TimeUnit.SECONDS);
            if (result instanceof TransportResultHttpResponse) {
                return ((TransportResultHttpResponse) result).getResponse();
            } else {
                return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, "Execution of " + url
                        + " didn't return a TransportResultHttpResponse: " + result.getClass().getName());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,
                    "Execution of " + url + " threw an exception: " + e.getMessage());
        }
    }

    /**
     * Helper method to execute a GET
     * 
     * @param cmd the non-null command (which contains the GET URL to call)
     * @param options any options to use for this specific call
     * @return a future response
     */
    private CompletableFuture<TransportResultHttpResponse> executeGet(final TransportPayloadHttp cmd,
            final TransportOption... options) {
        Objects.requireNonNull(cmd, "cmd cannot be null");

        final String url = cmd.getUrl();
        Validate.notEmpty(url, "url within the cmd cannot be empty");

        final Header[] headers = getHeaders(options);
        return CompletableFuture
                .completedFuture(new TransportResultHttpResponse(requestor.sendGetCommand(url, headers)));
    }

    /**
     * Helper method to execute a DELETE
     * 
     * @param cmd the non-null command (which contains the DELETE URL to call)
     * @param options any options to use for this specific call
     * @return a future response
     */
    private CompletableFuture<TransportResultHttpResponse> executeDelete(final TransportPayloadHttp cmd,
            final TransportOption... options) {
        Objects.requireNonNull(cmd, "cmd cannot be null");

        final String url = cmd.getUrl();
        Validate.notEmpty(url, "url within the cmd cannot be empty");
        final Header[] headers = getHeaders(options);
        return CompletableFuture
                .completedFuture(new TransportResultHttpResponse(requestor.sendDeleteCommand(url, headers)));
    }

    /**
     * Helper method to execute a POST of ScalarWebRequest (which will be json'd)
     * 
     * @param request the non-null scalar web request to send (to the base uri)
     * @param options any options to use for this specific call
     * @return a future response
     */
    private CompletableFuture<TransportResultHttpResponse> executePostJson(
            final TransportPayloadScalarWebRequest request, final TransportOption... options) {
        Objects.requireNonNull(request, "request cannot be null");
        final ScalarWebRequest payload = request.getRequest();

        Objects.requireNonNull(payload, "payload cannot be null");
        final String jsonRequest = gson.toJson(payload);

        return executePostJson(new TransportPayloadHttp(getBaseUri().toString(), jsonRequest), options);
    }

    /**
     * Helper method to execute a POST of JSON text to the specified URL
     * 
     * @param request the non-null request to send
     * @param options any options to use for this specific call
     * @return a future response
     */
    private CompletableFuture<TransportResultHttpResponse> executePostJson(final TransportPayloadHttp request,
            final TransportOption... options) {
        Objects.requireNonNull(request, "request cannot be null");

        final String payload = request.getBody();
        Objects.requireNonNull(payload, "payload cannot be null"); // may be empty however

        final String url = request.getUrl();
        Validate.notEmpty(url, "url within the cmd cannot be empty");

        final Header[] headers = getHeaders(options);

        return CompletableFuture
                .completedFuture(new TransportResultHttpResponse(requestor.sendPostJsonCommand(url, payload, headers)));
    }

    /**
     * Helper method to execute a POST of XML
     * 
     * @param request the non-null request to send
     * @param options any options to use for this specific call
     * @return a future response
     */
    private CompletableFuture<TransportResultHttpResponse> executePostXml(final TransportPayloadHttp request,
            final TransportOption... options) {
        Objects.requireNonNull(request, "request cannot be null");

        final String payload = request.getBody();
        Objects.requireNonNull(payload, "payload cannot be null"); // may be empty however

        final String url = request.getUrl();
        Validate.notEmpty(url, "url within the cmd cannot be empty");

        final Header[] headers = getHeaders(options);
        return CompletableFuture
                .completedFuture(new TransportResultHttpResponse(requestor.sendPostXmlCommand(url, payload, headers)));
    }

    @Override
    public String getProtocolType() {
        return SonyTransportFactory.HTTP;
    }

    @Override
    public void close() {
        logger.debug("Closing http client");
        requestor.close();
    }

    /**
     * Helper method to append a option to an array of options
     * 
     * @param options a non-null, possibly empty list of options
     * @param option a non-null option to append
     * @return a non-null, non-empty list of options
     */
    private static TransportOption[] append(final TransportOption[] options, final TransportOption option) {
        Objects.requireNonNull(options, "options cannot be null");
        Objects.requireNonNull(option, "option cannot be null");

        return (TransportOption[]) ArrayUtils.add(options, option);
    }
}
