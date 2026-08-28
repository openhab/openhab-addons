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
package org.openhab.binding.plivo.internal.servlet;

import static org.openhab.binding.plivo.internal.PlivoBindingConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FutureResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.plivo.internal.api.PlivoApiClient;
import org.openhab.binding.plivo.internal.api.PlivoSignatureValidator;
import org.openhab.binding.plivo.internal.handler.PlivoAccountHandler;
import org.openhab.binding.plivo.internal.handler.PlivoPhoneHandler;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletAsyncSupported;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlivoCallbackServlet} receives webhook callbacks from Plivo for
 * incoming SMS, voice calls, DTMF input, and status updates. It serves the
 * answer XML for outbound calls (Plivo fetches call-flow XML from an answer URL
 * rather than accepting it inline) and serves media content for MMS and WhatsApp
 * messages via temporary UUID-keyed URLs.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
@HttpWhiteboardServletAsyncSupported
@HttpWhiteboardServletName(SERVLET_PATH)
@HttpWhiteboardServletPattern(SERVLET_PATH + "/*")
@Component(immediate = true, service = { Servlet.class, PlivoCallbackServlet.class })
public class PlivoCallbackServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String CONTENT_TYPE_XML = "application/xml";
    private static final String PLIVO_SIGNATURE_V3_HEADER = "X-Plivo-Signature-V3";
    private static final String PLIVO_SIGNATURE_MA_V3_HEADER = "X-Plivo-Signature-Ma-V3";
    private static final String PLIVO_V3_NONCE_HEADER = "X-Plivo-Signature-V3-Nonce";
    private static final String PLIVO_SIGNATURE_V2_HEADER = "X-Plivo-Signature-V2";
    private static final String PLIVO_SIGNATURE_MA_V2_HEADER = "X-Plivo-Signature-Ma-V2";
    private static final String PLIVO_V2_NONCE_HEADER = "X-Plivo-Signature-V2-Nonce";
    private static final int PROXY_TIMEOUT_SECONDS = 15;
    private static final int CLEANUP_INTERVAL_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(PlivoCallbackServlet.class);

    private final Map<String, PlivoPhoneHandler> handlers = new ConcurrentHashMap<>();
    private final Map<String, MediaEntry> mediaCache = new ConcurrentHashMap<>();
    private final Map<String, CallXmlEntry> callXmlCache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> pendingResponses = new ConcurrentHashMap<>();

    private final HttpClient httpClient;
    private final ScheduledExecutorService cleanupScheduler;
    private @Nullable ScheduledFuture<?> cleanupTask;

    @Activate
    public PlivoCallbackServlet(final @Reference HttpClientFactory httpClientFactory) {
        httpClient = httpClientFactory.createHttpClient(BINDING_ID + "-media");
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start media HttpClient", e);
        }
        cleanupScheduler = Executors
                .newSingleThreadScheduledExecutor(new NamedThreadFactory(BINDING_ID + "-media-cleanup", true));
        cleanupTask = cleanupScheduler.scheduleWithFixedDelay(this::cleanupExpiredEntries, CLEANUP_INTERVAL_SECONDS,
                CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        ScheduledFuture<?> task = cleanupTask;
        if (task != null) {
            task.cancel(true);
        }
        cleanupScheduler.shutdownNow();
        mediaCache.clear();
        callXmlCache.clear();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Error stopping media HttpClient during deactivation: {}", e.getMessage());
        }
    }

    public void registerHandler(String thingUID, PlivoPhoneHandler handler) {
        handlers.put(thingUID, handler);
        logger.debug("Registered Plivo callback handler for {}", thingUID);
    }

    public void unregisterHandler(String thingUID) {
        handlers.remove(thingUID);
        logger.debug("Unregistered Plivo callback handler for {}", thingUID);
    }

    /**
     * Creates a pending response for a call. The servlet will wait on this future
     * before returning XML to Plivo.
     *
     * @param callUuid the Plivo Call UUID
     * @return the CompletableFuture that should be completed with Plivo XML
     */
    public CompletableFuture<String> createPendingResponse(String callUuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingResponses.put(callUuid, future);
        return future;
    }

    /**
     * Gets the pending response future for a call, so a rule action can complete it.
     *
     * @param callUuid the Plivo Call UUID
     * @return the CompletableFuture, or null if no pending response exists
     */
    public @Nullable CompletableFuture<String> getPendingResponse(String callUuid) {
        return pendingResponses.get(callUuid);
    }

    /**
     * Stores answer XML for an outbound call and returns a one-shot token. The token
     * is placed in the call's answer URL; Plivo fetches it when the call is answered.
     *
     * @param xml the answer XML
     * @return the token identifying the stored XML
     */
    public String createCallXmlEntry(String xml) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(MEDIA_EXPIRY_MINUTES * 60L);
        callXmlCache.put(token, new CallXmlEntry(xml, expiresAt));
        logger.debug("Created answer XML entry {} (expires {})", token, expiresAt);
        return token;
    }

    /**
     * Creates a media entry with direct content (bytes + MIME type).
     *
     * @param data the media bytes
     * @param mimeType the MIME type (e.g. "image/jpeg")
     * @return the UUID key for the entry
     */
    public String createMediaEntry(byte[] data, String mimeType) {
        String uuid = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(MEDIA_EXPIRY_MINUTES * 60L);
        mediaCache.put(uuid, new MediaEntry(data, mimeType, null, expiresAt));
        logger.debug("Created media entry {} ({}, {} bytes, expires {})", uuid, mimeType, data.length, expiresAt);
        return uuid;
    }

    /**
     * Creates a media entry that proxies a URL on demand.
     *
     * @param proxyUrl the URL to proxy when requested
     * @return the UUID key for the entry
     */
    public String createProxyEntry(String proxyUrl) {
        String uuid = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(MEDIA_EXPIRY_MINUTES * 60L);
        mediaCache.put(uuid, new MediaEntry(null, null, proxyUrl, expiresAt));
        logger.debug("Created proxy media entry {} -> {} (expires {})", uuid, proxyUrl, expiresAt);
        return uuid;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        logger.trace("GET request: pathInfo={}", pathInfo);

        if (pathInfo == null || pathInfo.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (pathInfo.startsWith("/" + WEBHOOK_MEDIA + "/")) {
            String uuid = pathInfo.substring(("/" + WEBHOOK_MEDIA + "/").length());
            serveMedia(uuid, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        logger.trace("POST request: pathInfo={}", pathInfo);

        if (pathInfo == null || pathInfo.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Missing path");
            return;
        }

        int lastSlash = pathInfo.lastIndexOf('/');
        if (lastSlash <= 0) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid path");
            return;
        }

        String endpoint = pathInfo.substring(lastSlash + 1);
        String thingUID = pathInfo.substring(1, lastSlash);

        PlivoPhoneHandler handler = handlers.get(thingUID);
        if (handler == null) {
            logger.debug("No handler registered for thingUID: {}", thingUID);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown thing");
            return;
        }

        Map<String, String> params = extractParameters(req);
        logger.trace("POST {} endpoint={}, params={}", thingUID, endpoint, params);

        PlivoAccountHandler accountHandler = handler.getAccountHandler();
        PlivoApiClient client = accountHandler != null ? accountHandler.getApiClient() : null;

        if (WEBHOOK_ANSWER.equals(endpoint)) {
            // Plivo signs voice answer callbacks with the V3 signature, so require a valid one
            // before serving (and before consuming the one-shot token), like the other endpoints.
            // The token rides in the URL query, so only the POST body params go to the validator.
            if (client == null) {
                logger.debug("Rejecting answer callback for {}: account not ready to validate signature", thingUID);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Account not ready");
                return;
            }
            String answerUrl = getExternalRequestUrl(req, handler, WEBHOOK_ANSWER, true);
            if (!validateSignature(req, extractBodyParameters(req), answerUrl, client.getAuthToken(), false)) {
                logger.warn("Rejected Plivo answer callback with an invalid or missing signature for {}", answerUrl);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid signature");
                return;
            }
            serveCallXml(req, resp, handler);
            return;
        }

        if (client == null) {
            logger.debug("Rejecting callback for {}: account not ready to validate signature", thingUID);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Account not ready");
            return;
        }
        // The /status endpoint is shared: message-status callbacks carry MessageUUID and use the
        // messaging signature families, while call-status callbacks carry CallUUID and require V3 like
        // the other voice callbacks.
        boolean messaging = WEBHOOK_SMS.equals(endpoint)
                || (WEBHOOK_STATUS.equals(endpoint) && params.containsKey("MessageUUID"));
        String requestUrl = getExternalRequestUrl(req, handler, endpoint, false);
        if (!validateSignature(req, extractBodyParameters(req), requestUrl, client.getAuthToken(), messaging)) {
            logger.warn("Rejected Plivo callback with an invalid signature for {}", requestUrl);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid signature");
            return;
        }

        switch (endpoint) {
            case WEBHOOK_SMS:
                handler.handleIncomingSms(params);
                sendXmlResponse(resp, handler, EMPTY_XML_RESPONSE, endpoint);
                break;
            case WEBHOOK_VOICE:
                handleWithPendingResponse(req, resp, params, handler, handler.getVoiceGreetingXml(),
                        () -> handler.handleIncomingCall(params), endpoint);
                break;
            case WEBHOOK_GATHER:
                handleWithPendingResponse(req, resp, params, handler, handler.getGatherResponseXml(),
                        () -> handler.handleDtmfInput(params), endpoint);
                break;
            case WEBHOOK_STATUS:
                handler.handleStatusCallback(params);
                sendXmlResponse(resp, handler, "", endpoint);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint: " + endpoint);
                return;
        }
    }

    // --- Private helpers ---

    private void serveCallXml(HttpServletRequest req, HttpServletResponse resp, PlivoPhoneHandler handler) {
        String token = req.getParameter(ANSWER_TOKEN_PARAM);
        String xml = EMPTY_XML_RESPONSE;
        if (token != null) {
            CallXmlEntry entry = callXmlCache.remove(token);
            if (entry != null && !entry.isExpired()) {
                xml = entry.xml;
            } else {
                logger.debug("No valid answer XML for token {}", token);
            }
        }
        sendXmlResponse(resp, handler, xml, WEBHOOK_ANSWER);
    }

    /**
     * Handles a voice/gather request using async I/O. Creates a pending response future,
     * fires the handler (which triggers rules), then completes the HTTP response
     * asynchronously when a rule calls respondWithXml or the timeout expires.
     */
    private void handleWithPendingResponse(HttpServletRequest req, HttpServletResponse resp, Map<String, String> params,
            PlivoPhoneHandler handler, String defaultXml, Runnable handlerAction, String endpoint) {
        String callUuid = params.get("CallUUID");
        if (callUuid == null || callUuid.isBlank()) {
            try {
                handlerAction.run();
            } catch (RuntimeException e) {
                logger.debug("Handler action failed for {} callback: {}", endpoint, e.getMessage());
            }
            sendXmlResponse(resp, handler, defaultXml, endpoint);
            return;
        }

        int timeout = handler.getResponseTimeout();
        logger.trace("Creating async pending response for CallUUID={}, timeout={}s", callUuid, timeout);

        AsyncContext asyncContext = req.startAsync(req, resp);
        asyncContext.setTimeout(0);

        CompletableFuture<String> future = createPendingResponse(callUuid);

        // Attach the completion handler BEFORE running the handler action so that a failure in the
        // action (or a timeout) always completes the response and clears the pending entry, rather
        // than leaving the async request open forever.
        future.orTimeout(timeout, TimeUnit.SECONDS).whenComplete((result, ex) -> {
            try {
                String xml;
                if (ex != null) {
                    if (ex instanceof TimeoutException) {
                        logger.debug("No XML response within timeout for CallUUID {}, using default", callUuid);
                    } else {
                        logger.debug("Error waiting for XML for CallUUID {}: {}", callUuid, ex.getMessage());
                    }
                    xml = defaultXml;
                } else {
                    xml = result;
                }
                sendXmlResponse((HttpServletResponse) asyncContext.getResponse(), handler, xml, endpoint);
            } finally {
                // Remove only this request's entry. A retry for the same CallUUID may already have
                // replaced it, and dropping that newer future would leave respondWithXml unable to
                // answer the retried request.
                pendingResponses.remove(callUuid, future);
                asyncContext.complete();
            }
        });

        try {
            handlerAction.run();
        } catch (RuntimeException e) {
            logger.debug("Handler action failed for CallUUID {}: {}", callUuid, e.getMessage());
            future.complete(defaultXml);
        }
    }

    private void sendXmlResponse(HttpServletResponse resp, PlivoPhoneHandler handler, String xmlResponse,
            String endpoint) {
        String xml = handler.replaceXmlPlaceholders(xmlResponse);
        logger.trace("POST response: 200, endpoint={}, xml={}", endpoint, xml);
        resp.setContentType(CONTENT_TYPE_XML);
        resp.setStatus(HttpServletResponse.SC_OK);
        if (!xml.isEmpty()) {
            try {
                PrintWriter writer = resp.getWriter();
                writer.print(xml);
                writer.flush();
            } catch (IOException e) {
                logger.debug("Failed to write XML response: {}", e.getMessage());
            }
        }
    }

    private void serveMedia(String uuid, HttpServletResponse resp) throws IOException {
        MediaEntry entry = mediaCache.get(uuid);
        if (entry == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Media not found");
            return;
        }

        if (entry.isExpired()) {
            mediaCache.remove(uuid);
            resp.sendError(HttpServletResponse.SC_GONE, "Media expired");
            return;
        }

        byte[] data = entry.data;
        String mimeType = entry.mimeType;
        String proxyUrl = entry.proxyUrl;

        if (data != null && mimeType != null) {
            resp.setContentType(mimeType);
            resp.setContentLength(data.length);
            OutputStream out = resp.getOutputStream();
            out.write(data);
            out.flush();
        } else if (proxyUrl != null) {
            proxyMedia(proxyUrl, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid media entry");
        }
    }

    private void proxyMedia(String sourceUrl, HttpServletResponse resp) throws IOException {
        try {
            String scheme = new URI(sourceUrl).getScheme();
            if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                logger.debug("Rejecting proxy request for non-http(s) URL scheme: {}", scheme);
                resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Unsupported URL scheme");
                return;
            }
        } catch (URISyntaxException e) {
            logger.debug("Rejecting proxy request for malformed URL {}: {}", sourceUrl, e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Invalid source URL");
            return;
        }

        try {
            Request request = httpClient.newRequest(sourceUrl) //
                    .method(HttpMethod.GET) //
                    .timeout(PROXY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            FutureResponseListener listener = new FutureResponseListener(request, MAX_PROXY_MEDIA_BYTES);
            request.send(listener);
            ContentResponse proxyResponse = listener.get(PROXY_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            int status = proxyResponse.getStatus();
            if (status < 200 || status >= 300) {
                resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Source returned " + status);
                return;
            }

            byte[] content = proxyResponse.getContent();
            String contentType = proxyResponse.getMediaType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            resp.setContentType(contentType);
            resp.setContentLength(content.length);
            OutputStream out = resp.getOutputStream();
            out.write(content);
            out.flush();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Proxy request interrupted");
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("Failed to proxy media from {}: {}", sourceUrl, e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failed to fetch media");
        }
    }

    private void cleanupExpiredEntries() {
        Iterator<Map.Entry<String, MediaEntry>> media = mediaCache.entrySet().iterator();
        while (media.hasNext()) {
            Map.Entry<String, MediaEntry> entry = media.next();
            if (entry.getValue().isExpired()) {
                media.remove();
            }
        }
        Iterator<Map.Entry<String, CallXmlEntry>> calls = callXmlCache.entrySet().iterator();
        while (calls.hasNext()) {
            Map.Entry<String, CallXmlEntry> entry = calls.next();
            if (entry.getValue().isExpired()) {
                calls.remove();
            }
        }
    }

    private Map<String, String> extractParameters(HttpServletRequest req) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String value = req.getParameter(name);
            if (value != null) {
                params.put(name, value);
            }
        }
        return params;
    }

    /**
     * Returns only the POST body parameters, excluding any that also appear in the query string. The
     * servlet merges query-string and body parameters, but the signed string counts query parameters
     * in the URL portion and body parameters separately, so a parameter present in the query (such as
     * the {@code /answer} token) must not be counted again as a body parameter.
     */
    private Map<String, String> extractBodyParameters(HttpServletRequest req) {
        Map<String, String> params = extractParameters(req);
        String query = req.getQueryString();
        if (query != null && !query.isEmpty()) {
            for (String pair : query.split("&")) {
                int eq = pair.indexOf('=');
                String rawKey = eq >= 0 ? pair.substring(0, eq) : pair;
                String key;
                try {
                    key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    key = rawKey;
                }
                params.remove(key);
            }
        }
        return params;
    }

    /**
     * Returns the externally-visible request URL for signature validation, which must
     * byte-match the URL Plivo signed. That is the webhook URL the binding registered
     * (cloud or publicUrl based), falling back to the raw request URL. When
     * {@code includeQuery} is set, the incoming request's query string is appended, which
     * matters for the {@code /answer} endpoint whose signed URL carries the answer token.
     */
    private String getExternalRequestUrl(HttpServletRequest req, PlivoPhoneHandler handler, String endpoint,
            boolean includeQuery) {
        String registeredUrl = handler.getWebhookUrl(endpoint);
        String base;
        if (registeredUrl != null) {
            base = registeredUrl;
        } else {
            StringBuffer requestUrl = req.getRequestURL();
            base = requestUrl != null ? requestUrl.toString() : "";
        }
        if (includeQuery) {
            String query = req.getQueryString();
            if (query != null && !query.isEmpty()) {
                return base + "?" + query;
            }
        }
        return base;
    }

    /**
     * Validates the request signature by extracting the Plivo signature headers and delegating to the
     * callback-aware {@link PlivoSignatureValidator#validateCallback}.
     */
    private boolean validateSignature(HttpServletRequest req, Map<String, String> params, String url, String authToken,
            boolean messaging) {
        return PlivoSignatureValidator.validateCallback(messaging, url, params, authToken,
                req.getHeader(PLIVO_SIGNATURE_V3_HEADER), req.getHeader(PLIVO_SIGNATURE_MA_V3_HEADER),
                req.getHeader(PLIVO_V3_NONCE_HEADER), req.getHeader(PLIVO_SIGNATURE_V2_HEADER),
                req.getHeader(PLIVO_SIGNATURE_MA_V2_HEADER), req.getHeader(PLIVO_V2_NONCE_HEADER));
    }

    // --- Media Entry ---

    private static class MediaEntry {
        final byte @Nullable [] data;
        final @Nullable String mimeType;
        final @Nullable String proxyUrl;
        final Instant expiresAt;

        MediaEntry(byte @Nullable [] data, @Nullable String mimeType, @Nullable String proxyUrl, Instant expiresAt) {
            this.data = data;
            this.mimeType = mimeType;
            this.proxyUrl = proxyUrl;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    // --- Call Answer XML Entry ---

    private static class CallXmlEntry {
        final String xml;
        final Instant expiresAt;

        CallXmlEntry(String xml, Instant expiresAt) {
            this.xml = xml;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
