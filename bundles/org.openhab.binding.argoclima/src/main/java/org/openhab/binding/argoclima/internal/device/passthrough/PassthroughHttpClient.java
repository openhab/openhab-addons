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
package org.openhab.binding.argoclima.internal.device.passthrough;

import static org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants.BINDING_ID;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.HttpCookieStore;
import org.eclipse.jetty.server.Request;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP client, forwarding (proxy-like) original device's request (downstream) to a remote server
 * (upstream) and passing the response through back to the device (with ability to intercept
 * content and change it - MitM)
 *
 * @implNote The HTTP client is custom (as it needs to simulate the actual Argo device, with all its quirks), hence
 *           using separate instance and threadpool for it.
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class PassthroughHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(PassthroughHttpClient.class);
    private static final String RPC_POOL_NAME = BINDING_ID + "_apiProxy";
    private static final List<String> HEADERS_TO_IGNORE = List.of("content-length", "content-type", "content-encoding",
            "host", "accept-encoding");
    private final HttpClient rawHttpClient;
    private boolean isStarted = false;

    /** The hostname of OEM vendor's (upstream) server to talk to */
    public final String upstreamTargetHost;

    /** The port of OEM vendor's (upstream) server to talk to */
    public final int upstreamTargetPort;

    /**
     * C-tor, creates new HTTP client (requires starting!)
     *
     * @param upstreamIpAddress The hostname of OEM vendor's (upstream) server to talk to
     * @param upstreamPort The port of OEM vendor's (upstream) server to talk to
     * @param clientFactory Framework-provided factory for creating new Jetty's HTTP clients
     */
    public PassthroughHttpClient(String upstreamIpAddress, int upstreamPort, HttpClientFactory clientFactory) {
        // Impl. note: Using Openhab's (globally-configurable) settings for custom threadpool. We technically may need
        // less threads (and longer TTL) here... but not fiddling with thread pool settings post-creation, to avoid
        // corner cases
        this.rawHttpClient = clientFactory.createHttpClient(RPC_POOL_NAME);

        this.rawHttpClient.setFollowRedirects(false);
        this.rawHttpClient.setUserAgentField(null); // The device doesn't set it, and we want to be a transparent proxy
        this.rawHttpClient.setHttpCookieStore(new HttpCookieStore.Empty());

        this.rawHttpClient.setRequestBufferSize(1024);
        this.rawHttpClient.setResponseBufferSize(1024);

        this.upstreamTargetHost = upstreamIpAddress;
        this.upstreamTargetPort = upstreamPort;
    }

    /**
     * Start pass-through HTTP client (simulating the device).
     *
     * @throws Exception In case of startup failure
     */
    public synchronized void start() throws Exception {
        if (this.isStarted) {
            stop();
        }
        this.rawHttpClient.start();
        this.rawHttpClient.getContentDecoderFactories().clear(); // Prevent decoding gzip (device doesn't support it).
                                                                 // Stops sending Accept header
        this.isStarted = true;
    }

    /**
     * Stops the pass-through HTTP client
     *
     * @throws Exception In case of stop failure
     */
    public synchronized void stop() throws Exception {
        this.rawHttpClient.stop();
        this.rawHttpClient.destroy();
        this.isStarted = false;
    }

    /**
     * Pass the downstream HTTP request through to upstream server (as-is)
     *
     * @param downstreamHttpRequest The device-side request to pass on
     * @param downstreamHttpRequestBody The body of the request (provided separately, because the stream has been read
     *            already, as it is also used for sniffing)
     * @return The response from remote side
     * @throws InterruptedException if send thread is interrupted
     * @throws TimeoutException if send times out
     * @throws ExecutionException if execution fails
     */
    public ContentResponse passthroughRequest(Request downstreamHttpRequest, String downstreamHttpRequestBody)
            throws InterruptedException, TimeoutException, ExecutionException {
        String pathQuery = Objects.requireNonNullElse(downstreamHttpRequest.getHttpURI().getPathQuery(),
                downstreamHttpRequest.getHttpURI().getPath());
        var request = this.rawHttpClient.newRequest(this.upstreamTargetHost, this.upstreamTargetPort)
                .method(downstreamHttpRequest.getMethod()).path(pathQuery)
                .version(downstreamHttpRequest.getConnectionMetaData().getHttpVersion())
                .body(new StringRequestContent(downstreamHttpRequestBody))
                .timeout(ArgoClimaBindingConstants.UPSTREAM_PROXY_HTTP_REQUEST_TIMEOUT.toMillis(),
                        TimeUnit.MILLISECONDS);

        // re-add headers from downstream request to this one (except explicitly-ignored list)
        for (var header : downstreamHttpRequest.getHeaders()) {
            String headerName = header.getName();
            if (HEADERS_TO_IGNORE.stream().noneMatch(x -> x.equalsIgnoreCase(headerName))) {
                request.headers(fields -> fields.put(headerName, header.getValue()));
            }
        }

        LOGGER.trace("Pass-through: DEVICE --> UPSTREAM_API: [{} {}], body=[{}]", request.getMethod(), request.getURI(),
                downstreamHttpRequestBody);

        return Objects.requireNonNull(request.send());
    }

    /**
     * Forward upstream server's response back to the device-side (possibly overriding the body)
     *
     * @param response The response received from remote side (vendor's server)
     * @param targetResponse The response to send to the device side (from this interceptor)
     * @param overrideBodyToReturn If provided, replace the response body from upstream with THIS content (useful when
     *            communicating with the device indirectly, and want to "send" it a command (send = let it pool for it
     *            on its own)
     * @throws IOException If response writing fails
     */
    public static void forwardUpstreamResponse(ContentResponse response, HttpServletResponse targetResponse,
            Optional<String> overrideBodyToReturn) throws IOException {
        targetResponse.setContentType(Objects.requireNonNullElse(response.getMediaType(), "text/html"));

        // NOTE: Argo servers send responses **without** charset, whereas Jetty's default includes it.
        // The device seems to be fine w/ it, note though it is a difference in the protocol
        // Merely setting the Encoding to null or overriding the header to MimeTypes.getContentTypeWithoutCharset(x)
        // has no-effect as Jetty overrides it at writer creation. Would require more sophisticated filtering
        // and possibly subclassing org.eclipse.jetty.server.Response to get 1:1 matching w/ remote response, so leaving
        // as-is.
        targetResponse.setCharacterEncoding(Objects.requireNonNullElse(response.getEncoding(), "ASCII"));

        for (var header : response.getHeaders()) {
            if (HEADERS_TO_IGNORE.stream().noneMatch(x -> x.equalsIgnoreCase(header.getName()))) {
                targetResponse.setHeader(Objects.requireNonNull(header.getName()), header.getValue());
            }
        }

        String responseBodyToReturn = overrideBodyToReturn.orElse(response.getContentAsString());
        targetResponse.getWriter().write(responseBodyToReturn);
        targetResponse.setStatus(response.getStatus());
        LOGGER.trace("  [response]: DEVICE <-- UPSTREAM_API: [{} {} {} - {} bytes], body=[{}]", response.getVersion(),
                response.getStatus(), response.getReason(), response.getContent().length, responseBodyToReturn);
    }
}
