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
package org.openhab.binding.twilio.internal.servlet;

import static org.openhab.binding.twilio.internal.TwilioBindingConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.twilio.internal.api.TwilioApiClient;
import org.openhab.binding.twilio.internal.api.TwilioSignatureValidator;
import org.openhab.binding.twilio.internal.handler.TwilioAccountHandler;
import org.openhab.binding.twilio.internal.handler.TwilioPhoneHandler;
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
 * The {@link TwilioCallbackServlet} receives webhook callbacks from Twilio
 * for incoming SMS, voice calls, DTMF input, and status updates. It also
 * serves media content (images, audio, etc.) for MMS and WhatsApp messages
 * via temporary UUID-keyed URLs.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@HttpWhiteboardServletAsyncSupported
@HttpWhiteboardServletName(SERVLET_PATH)
@HttpWhiteboardServletPattern(SERVLET_PATH + "/*")
@Component(immediate = true, service = { Servlet.class, TwilioCallbackServlet.class })
public class TwilioCallbackServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String CONTENT_TYPE_XML = "application/xml";
    private static final String TWILIO_SIGNATURE_HEADER = "X-Twilio-Signature";
    private static final int PROXY_TIMEOUT_SECONDS = 15;
    private static final int CLEANUP_INTERVAL_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(TwilioCallbackServlet.class);

    private final Map<String, TwilioPhoneHandler> handlers = new ConcurrentHashMap<>();
    private final Map<String, MediaEntry> mediaCache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> pendingResponses = new ConcurrentHashMap<>();

    private final HttpClient httpClient;
    private final ScheduledExecutorService cleanupScheduler;
    private @Nullable ScheduledFuture<?> cleanupTask;

    @Activate
    public TwilioCallbackServlet(final @Reference HttpClientFactory httpClientFactory) {
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
        try {
            httpClient.stop();
        } catch (Exception ignored) {
        }
    }

    // --- Handler Registration ---

    public void registerHandler(String thingUID, TwilioPhoneHandler handler) {
        handlers.put(thingUID, handler);
        logger.debug("Registered Twilio callback handler for {}", thingUID);
    }

    public void unregisterHandler(String thingUID) {
        handlers.remove(thingUID);
        logger.debug("Unregistered Twilio callback handler for {}", thingUID);
    }

    // --- Pending TwiML Responses ---

    /**
     * Creates a pending response for a call. The servlet will wait on this future
     * before returning TwiML to Twilio.
     *
     * @param callSid the Twilio Call SID
     * @return the CompletableFuture that should be completed with TwiML
     */
    public CompletableFuture<String> createPendingResponse(String callSid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingResponses.put(callSid, future);
        return future;
    }

    /**
     * Gets the pending response future for a call, so a rule action can complete it.
     *
     * @param callSid the Twilio Call SID
     * @return the CompletableFuture, or null if no pending response exists
     */
    public @Nullable CompletableFuture<String> getPendingResponse(String callSid) {
        return pendingResponses.get(callSid);
    }

    // --- Media Cache ---

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

    // --- HTTP Handlers ---

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        logger.trace("GET request: pathInfo={}", pathInfo);

        if (pathInfo == null || pathInfo.isEmpty()) {
            logger.trace("GET response: 404 (missing path)");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Media path: /media/{uuid}
        if (pathInfo.startsWith("/" + WEBHOOK_MEDIA + "/")) {
            String uuid = pathInfo.substring(("/" + WEBHOOK_MEDIA + "/").length());
            logger.trace("GET media request: uuid={}", uuid);
            serveMedia(uuid, resp);
        } else {
            logger.trace("GET response: 404 (unknown path)");
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
            logger.trace("POST response: 404 (missing path)");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Missing path");
            return;
        }

        // Path format: /{thingUID}/{endpoint}
        // thingUID contains colons (e.g. twilio:phone:myphone), so we need to parse carefully
        // The endpoint is the last segment after the last /
        int lastSlash = pathInfo.lastIndexOf('/');
        if (lastSlash <= 0) {
            logger.trace("POST response: 404 (invalid path)");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid path");
            return;
        }

        String endpoint = pathInfo.substring(lastSlash + 1);
        // thingUID is everything between the first and last slash
        String thingUID = pathInfo.substring(1, lastSlash);

        TwilioPhoneHandler handler = handlers.get(thingUID);
        if (handler == null) {
            logger.debug("No handler registered for thingUID: {}", thingUID);
            logger.trace("POST response: 404 (unknown thing: {})", thingUID);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown thing");
            return;
        }

        // Parse POST parameters
        Map<String, String> params = extractParameters(req);
        logger.trace("POST {} endpoint={}, params={}", thingUID, endpoint, params);

        // Validate signature if enabled
        TwilioAccountHandler accountHandler = handler.getAccountHandler();
        if (accountHandler != null) {
            TwilioApiClient client = accountHandler.getApiClient();
            if (client != null) {
                String signature = req.getHeader(TWILIO_SIGNATURE_HEADER);
                String requestUrl = getExternalRequestUrl(req, accountHandler, handler, endpoint);
                logger.trace("Validating signature: url={}, signature={}", requestUrl, signature);

                if (!TwilioSignatureValidator.validate(requestUrl, params, signature, client.getAuthToken())) {
                    logger.debug("Invalid Twilio signature for request to {}", requestUrl);
                    logger.trace("POST response: 403 (invalid signature)");
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid signature");
                    return;
                }
            }
        }

        // Route to the appropriate handler method and get TwiML response
        switch (endpoint) {
            case WEBHOOK_SMS:
            case WEBHOOK_WHATSAPP:
                handler.handleIncomingSms(params);
                sendTwimlResponse(resp, handler, EMPTY_TWIML_RESPONSE, endpoint);
                break;
            case WEBHOOK_VOICE:
                handleWithPendingResponse(req, resp, params, handler, handler.getVoiceGreetingTwiml(),
                        () -> handler.handleIncomingCall(params), endpoint);
                break;
            case WEBHOOK_GATHER:
                handleWithPendingResponse(req, resp, params, handler, handler.getGatherResponseTwiml(),
                        () -> handler.handleDtmfInput(params), endpoint);
                break;
            case WEBHOOK_STATUS:
                handler.handleStatusCallback(params);
                sendTwimlResponse(resp, handler, "", endpoint);
                break;
            default:
                logger.trace("POST response: 404 (unknown endpoint: {})", endpoint);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint: " + endpoint);
                return;
        }
    }

    // --- Private helpers ---

    /**
     * Handles a voice/gather request using async I/O. Creates a pending response future,
     * fires the handler (which triggers rules), then completes the HTTP response
     * asynchronously when a rule calls respondWithTwiml or the timeout expires.
     */
    private void handleWithPendingResponse(HttpServletRequest req, HttpServletResponse resp, Map<String, String> params,
            TwilioPhoneHandler handler, String defaultTwiml, Runnable handlerAction, String endpoint) {
        String callSid = params.get("CallSid");
        if (callSid == null || callSid.isBlank()) {
            logger.trace("No CallSid in request, using default TwiML");
            handlerAction.run();
            sendTwimlResponse(resp, handler, defaultTwiml, endpoint);
            return;
        }

        int timeout = handler.getResponseTimeout();
        logger.trace("Creating async pending response for CallSid={}, timeout={}s", callSid, timeout);

        AsyncContext asyncContext = req.startAsync(req, resp);
        asyncContext.setTimeout(0); // we manage our own timeout via the future

        CompletableFuture<String> future = createPendingResponse(callSid);

        // Fire the handler — this triggers channels and rules start executing
        handlerAction.run();

        // Complete the response asynchronously when the future resolves or times out
        future.orTimeout(timeout, TimeUnit.SECONDS).whenComplete((result, ex) -> {
            try {
                String twiml;
                if (ex != null) {
                    if (ex instanceof TimeoutException) {
                        logger.debug("No TwiML response within timeout for CallSid {}, using default", callSid);
                    } else {
                        logger.debug("Error waiting for TwiML for CallSid {}: {}", callSid, ex.getMessage());
                    }
                    twiml = defaultTwiml;
                } else {
                    logger.trace("Rule responded with TwiML for CallSid={}: {}", callSid, result);
                    twiml = result;
                }
                sendTwimlResponse((HttpServletResponse) asyncContext.getResponse(), handler, twiml, endpoint);
            } finally {
                pendingResponses.remove(callSid);
                asyncContext.complete();
            }
        });
    }

    private void sendTwimlResponse(HttpServletResponse resp, TwilioPhoneHandler handler, String twimlResponse,
            String endpoint) {
        String twiml = handler.replaceTwimlPlaceholders(twimlResponse);
        logger.trace("POST response: 200, endpoint={}, twiml={}", endpoint, twiml);
        resp.setContentType(CONTENT_TYPE_XML);
        resp.setStatus(HttpServletResponse.SC_OK);
        if (!twiml.isEmpty()) {
            try {
                PrintWriter writer = resp.getWriter();
                writer.print(twiml);
                writer.flush();
            } catch (IOException e) {
                logger.debug("Failed to write TwiML response: {}", e.getMessage());
            }
        }
    }

    private void serveMedia(String uuid, HttpServletResponse resp) throws IOException {
        MediaEntry entry = mediaCache.get(uuid);
        if (entry == null) {
            logger.trace("Media response: 404 (uuid={} not found)", uuid);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Media not found");
            return;
        }

        if (entry.isExpired()) {
            mediaCache.remove(uuid);
            logger.trace("Media response: 410 (uuid={} expired)", uuid);
            resp.sendError(HttpServletResponse.SC_GONE, "Media expired");
            return;
        }

        byte[] data = entry.data;
        String mimeType = entry.mimeType;
        String proxyUrl = entry.proxyUrl;

        if (data != null && mimeType != null) {
            logger.trace("Media response: 200 (uuid={}, direct, mimeType={}, {} bytes)", uuid, mimeType, data.length);
            resp.setContentType(mimeType);
            resp.setContentLength(data.length);
            OutputStream out = resp.getOutputStream();
            out.write(data);
            out.flush();
        } else if (proxyUrl != null) {
            logger.trace("Media response: proxying uuid={} from {}", uuid, proxyUrl);
            proxyMedia(proxyUrl, resp);
        } else {
            logger.trace("Media response: 500 (uuid={} invalid entry)", uuid);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid media entry");
        }
    }

    private void proxyMedia(String sourceUrl, HttpServletResponse resp) throws IOException {
        logger.trace("Proxy fetch: {}", sourceUrl);
        try {
            ContentResponse proxyResponse = httpClient.newRequest(sourceUrl) //
                    .method(HttpMethod.GET) //
                    .timeout(PROXY_TIMEOUT_SECONDS, TimeUnit.SECONDS) //
                    .send();

            int status = proxyResponse.getStatus();
            if (status < 200 || status >= 300) {
                logger.trace("Proxy response: 502 (source returned {})", status);
                resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Source returned " + status);
                return;
            }

            byte[] content = proxyResponse.getContent();
            String contentType = proxyResponse.getMediaType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            logger.trace("Proxy response: 200 (contentType={}, {} bytes)", contentType, content.length);
            resp.setContentType(contentType);
            resp.setContentLength(content.length);
            OutputStream out = resp.getOutputStream();
            out.write(content);
            out.flush();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.trace("Proxy response: 502 (interrupted)");
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Proxy request interrupted");
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("Failed to proxy media from {}: {}", sourceUrl, e.getMessage());
            logger.trace("Proxy response: 502 ({})", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failed to fetch media");
        }
    }

    private void cleanupExpiredEntries() {
        Iterator<Map.Entry<String, MediaEntry>> it = mediaCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, MediaEntry> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
                logger.trace("Cleaned up expired media entry {}", entry.getKey());
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
     * Returns the externally-visible request URL for signature validation.
     * Checks cloud webhook URLs first, then falls back to publicUrl, then to
     * the raw request URL. This handles reverse proxy, TLS termination, and
     * cloud webhook scenarios.
     */
    private String getExternalRequestUrl(HttpServletRequest req, TwilioAccountHandler accountHandler,
            TwilioPhoneHandler handler, String endpoint) {
        // Check for cloud webhook URL first — Twilio signs against exactly the URL
        // it was given, which is the bare cloud webhook URL with no query string.
        // The cloud proxy may add an empty "?" to the local request, so we ignore
        // the query string entirely for cloud webhooks.
        String cloudUrl = handler.getCloudWebhookUrl(endpoint);
        if (cloudUrl != null) {
            return cloudUrl;
        }

        String publicUrl = accountHandler.getAccountConfig().publicUrl;
        String base;
        if (publicUrl != null && !publicUrl.isBlank()) {
            if (publicUrl.endsWith("/")) {
                publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
            }
            // Reconstruct: publicUrl + servlet path + path info
            String servletPath = req.getServletPath();
            String pathInfo = req.getPathInfo();
            base = publicUrl + (servletPath != null ? servletPath : "") + (pathInfo != null ? pathInfo : "");
        } else {
            StringBuffer requestUrl = req.getRequestURL();
            base = requestUrl != null ? requestUrl.toString() : "";
        }
        String queryString = req.getQueryString();
        if (queryString != null) {
            return base + "?" + queryString;
        }
        return base;
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
}
