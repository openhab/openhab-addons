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
package org.openhab.io.mcp.internal.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.io.mcp.internal.McpCloudWebhookService;
import org.openhab.io.mcp.internal.auth.McpAuthenticator;
import org.openhab.io.mcp.internal.util.SubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.HttpHeaders;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransport;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.ProtocolVersions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Port of the MCP SDK's HttpServletStreamableServerTransportProvider to
 * javax.servlet 4.0 for openHAB's HTTP whiteboard. Implements the MCP 2025-11-25
 * Streamable HTTP transport: a single endpoint that accepts POST (JSON-RPC) and
 * GET (server-initiated SSE stream). Sessions are tracked via the
 * {@code Mcp-Session-Id} header.
 * <p>
 * Adds openHAB-specific hooks on top of the SDK source: auth gate via
 * {@link McpAuthenticator}, RFC 9728 {@code WWW-Authenticate} challenge on 401,
 * and keep-alive comments to surface dead TCP peers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class JavaxStreamableServerTransportProvider extends HttpServlet
        implements McpStreamableServerTransportProvider {

    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(JavaxStreamableServerTransportProvider.class);

    private static final String MESSAGE_EVENT_TYPE = "message";
    private static final String UTF_8 = "UTF-8";
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_EVENT_STREAM = "text/event-stream";
    private static final String ACCEPT = "Accept";
    private static final long KEEP_ALIVE_INTERVAL_SECONDS = 10;
    private static final int MAX_REQUEST_BODY_BYTES = 16 * 1024 * 1024;

    private final McpJsonMapper jsonMapper;
    private final McpAuthenticator authenticator;
    private final @Nullable McpCloudWebhookService cloudWebhook;

    private McpStreamableServerSession.@Nullable Factory sessionFactory;

    private final ConcurrentHashMap<String, McpStreamableServerSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JavaxStreamableSessionTransport> sessionTransports = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionTokens = new ConcurrentHashMap<>();
    private final AtomicBoolean isClosing = new AtomicBoolean(false);
    private @Nullable ScheduledFuture<?> keepAliveTask;
    private @Nullable SubscriptionManager subscriptionManager;

    public JavaxStreamableServerTransportProvider(McpJsonMapper jsonMapper, McpAuthenticator authenticator,
            @Nullable McpCloudWebhookService cloudWebhook) {
        this.jsonMapper = jsonMapper;
        this.authenticator = authenticator;
        this.cloudWebhook = cloudWebhook;

        ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("mcp");
        this.keepAliveTask = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, KEEP_ALIVE_INTERVAL_SECONDS,
                KEEP_ALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @NonNullByDefault({})
    @Override
    public List<String> protocolVersions() {
        return List.of(ProtocolVersions.MCP_2024_11_05, ProtocolVersions.MCP_2025_03_26,
                ProtocolVersions.MCP_2025_06_18, ProtocolVersions.MCP_2025_11_25);
    }

    @NonNullByDefault({})
    @Override
    public void setSessionFactory(McpStreamableServerSession.Factory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setSubscriptionManager(SubscriptionManager manager) {
        this.subscriptionManager = manager;
    }

    /**
     * Returns the bearer token captured for a session — used by {@link org.openhab.io.mcp.internal.tools.ApiTools}
     * to forward the caller's identity to {@code /rest/*} calls.
     */
    public @Nullable String getSessionToken(String sessionId) {
        return sessionTokens.get(sessionId);
    }

    @NonNullByDefault({})
    @Override
    public Mono<Void> notifyClients(String method, Object params) {
        if (sessions.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(sessions.values())
                .flatMap(session -> session.sendNotification(method, params).onErrorResume(e -> {
                    logger.debug("Failed to send notification to session {}: {}", session.getId(), e.getMessage());
                    return Mono.empty();
                })).then();
    }

    @NonNullByDefault({})
    @Override
    public Mono<Void> notifyClient(String sessionId, String method, Object params) {
        return Mono.defer(() -> {
            McpStreamableServerSession session = sessions.get(sessionId);
            if (session == null) {
                return Mono.empty();
            }
            return session.sendNotification(method, params);
        });
    }

    @NonNullByDefault({})
    @Override
    public Mono<Void> closeGracefully() {
        isClosing.set(true);
        ScheduledFuture<?> task = keepAliveTask;
        if (task != null) {
            task.cancel(false);
            keepAliveTask = null;
        }
        return Flux.fromIterable(sessions.values()).flatMap(McpStreamableServerSession::closeGracefully).then()
                .doOnSuccess(v -> {
                    sessions.clear();
                    sessionTransports.clear();
                });
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest rawRequest, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (rawRequest == null || response == null) {
            return;
        }
        HttpServletRequest request = cacheBody(rawRequest, response);
        if (request == null) {
            return;
        }
        traceRequest(request);
        if (isClosing.get()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is shutting down");
            return;
        }

        if (!authenticator.authenticate(request)) {
            logger.debug("Streamable GET rejected: {} from {}", request.getRequestURI(), request.getRemoteAddr());
            sendUnauthorized(response);
            return;
        }

        List<String> errors = new ArrayList<>();
        String accept = request.getHeader(ACCEPT);
        if (accept == null || !accept.contains(TEXT_EVENT_STREAM)) {
            errors.add("text/event-stream required in Accept header");
        }
        String rawSessionId = request.getHeader(HttpHeaders.MCP_SESSION_ID);
        if (rawSessionId == null || rawSessionId.isBlank()) {
            errors.add("Session ID required in mcp-session-id header");
        }
        if (!errors.isEmpty()) {
            responseError(response, HttpServletResponse.SC_BAD_REQUEST, String.join("; ", errors));
            return;
        }
        final String sessionId = java.util.Objects.requireNonNull(rawSessionId);

        McpStreamableServerSession session = sessions.get(sessionId);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        logger.debug("Streamable GET (listening stream) session={}", sessionId);
        response.setContentType(TEXT_EVENT_STREAM);
        response.setCharacterEncoding(UTF_8);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);

        JavaxStreamableSessionTransport sessionTransport = new JavaxStreamableSessionTransport(sessionId, asyncContext,
                response.getWriter());
        sessionTransports.put(sessionId, sessionTransport);

        McpStreamableServerSession.McpStreamableServerSessionStream listeningStream = session
                .listeningStream(sessionTransport);

        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(@Nullable AsyncEvent event) throws IOException {
                logger.debug("Streamable listening stream completed for session {}", sessionId);
                listeningStream.close();
                sessionTransports.remove(sessionId);
            }

            @Override
            public void onTimeout(@Nullable AsyncEvent event) throws IOException {
                logger.debug("Streamable listening stream timed out for session {}", sessionId);
                listeningStream.close();
                sessionTransports.remove(sessionId);
            }

            @Override
            public void onError(@Nullable AsyncEvent event) throws IOException {
                logger.debug("Streamable listening stream error for session {}", sessionId);
                listeningStream.close();
                sessionTransports.remove(sessionId);
            }

            @Override
            public void onStartAsync(@Nullable AsyncEvent event) throws IOException {
            }
        });
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest rawRequest, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (rawRequest == null || response == null) {
            return;
        }
        HttpServletRequest request = cacheBody(rawRequest, response);
        if (request == null) {
            return;
        }
        traceRequest(request);
        if (isClosing.get()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is shutting down");
            return;
        }

        if (!authenticator.authenticate(request)) {
            logger.debug("Streamable POST rejected: {} from {}", request.getRequestURI(), request.getRemoteAddr());
            sendUnauthorized(response);
            return;
        }

        List<String> errors = new ArrayList<>();
        String accept = request.getHeader(ACCEPT);
        if (accept == null || !accept.contains(TEXT_EVENT_STREAM)) {
            errors.add("text/event-stream required in Accept header");
        }
        if (accept == null || !accept.contains(APPLICATION_JSON)) {
            errors.add("application/json required in Accept header");
        }

        try {
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            String bodyStr = body.toString();
            logger.debug("Streamable POST body: {}", bodyStr);

            McpSchema.JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(jsonMapper, bodyStr);

            // Initialize handshake creates a fresh session and returns init result inline.
            if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest
                    && McpSchema.METHOD_INITIALIZE.equals(jsonrpcRequest.method())) {
                if (!errors.isEmpty()) {
                    responseError(response, HttpServletResponse.SC_BAD_REQUEST, String.join("; ", errors));
                    return;
                }
                McpStreamableServerSession.Factory factory = sessionFactory;
                if (factory == null) {
                    responseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Session factory not set");
                    return;
                }
                McpSchema.InitializeRequest initializeRequest = jsonMapper.convertValue(jsonrpcRequest.params(),
                        new TypeRef<McpSchema.InitializeRequest>() {
                        });
                McpStreamableServerSession.McpStreamableServerSessionInit init = factory
                        .startSession(initializeRequest);
                sessions.put(init.session().getId(), init.session());
                // Capture the bearer token presented on initialize so ApiTools can
                // forward it on tool-invoked /rest/* calls later in the session.
                String initToken = McpAuthenticator.extractToken(request);
                if (initToken != null) {
                    sessionTokens.put(init.session().getId(), initToken);
                }

                McpSchema.InitializeResult initResult = init.initResult().block();
                response.setContentType(APPLICATION_JSON);
                response.setCharacterEncoding(UTF_8);
                response.setHeader(HttpHeaders.MCP_SESSION_ID, init.session().getId());
                response.setStatus(HttpServletResponse.SC_OK);
                String jsonResponse = jsonMapper.writeValueAsString(new McpSchema.JSONRPCResponse(
                        McpSchema.JSONRPC_VERSION, jsonrpcRequest.id(), initResult, null));
                PrintWriter writer = response.getWriter();
                writer.write(jsonResponse);
                writer.flush();
                traceResponse("initialize [session " + init.session().getId() + "]", HttpServletResponse.SC_OK,
                        APPLICATION_JSON, jsonResponse);
                return;
            }

            String rawSessionId = request.getHeader(HttpHeaders.MCP_SESSION_ID);
            if (rawSessionId == null || rawSessionId.isBlank()) {
                errors.add("Session ID required in mcp-session-id header");
            }
            if (!errors.isEmpty()) {
                responseError(response, HttpServletResponse.SC_BAD_REQUEST, String.join("; ", errors));
                return;
            }
            final String sessionId = Objects.requireNonNull(rawSessionId);

            McpStreamableServerSession session = sessions.get(sessionId);
            if (session == null) {
                responseError(response, HttpServletResponse.SC_NOT_FOUND, "Session not found: " + sessionId);
                return;
            }

            // Clients MUST resend Authorization on every POST per OAuth 2.1; refresh
            // the captured token so ApiTools' REST proxy uses the latest one.
            String refreshed = McpAuthenticator.extractToken(request);
            if (refreshed != null) {
                sessionTokens.put(session.getId(), refreshed);
            }

            if (message instanceof McpSchema.JSONRPCResponse jsonrpcResponse) {
                session.accept(jsonrpcResponse).block();
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else if (message instanceof McpSchema.JSONRPCNotification jsonrpcNotification) {
                session.accept(jsonrpcNotification).block();
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else if (message instanceof McpSchema.JSONRPCRequest jsonrpcRequest) {
                response.setContentType(TEXT_EVENT_STREAM);
                response.setCharacterEncoding(UTF_8);
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Connection", "keep-alive");

                AsyncContext asyncContext = request.startAsync();
                asyncContext.setTimeout(0);

                JavaxStreamableSessionTransport sessionTransport = new JavaxStreamableSessionTransport(sessionId,
                        asyncContext, response.getWriter());
                try {
                    session.responseStream(jsonrpcRequest, sessionTransport).block();
                } catch (Exception e) {
                    logger.debug("Streamable response stream failed for session {}: {}", sessionId, e.getMessage());
                    asyncContext.complete();
                }
            } else {
                responseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown message type");
            }
        } catch (IllegalArgumentException | IOException e) {
            logger.debug("Streamable POST parse error: {}", e.getMessage());
            responseError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid message format: " + e.getMessage());
        } catch (Exception e) {
            logger.debug("Streamable POST handler error: {}", e.getMessage(), e);
            try {
                responseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error processing message: " + e.getMessage());
            } catch (IOException ex) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing message");
            }
        }
    }

    @Override
    protected void doDelete(@Nullable HttpServletRequest rawRequest, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (rawRequest == null || response == null) {
            return;
        }
        HttpServletRequest request = cacheBody(rawRequest, response);
        if (request == null) {
            return;
        }
        traceRequest(request);
        if (!authenticator.authenticate(request)) {
            sendUnauthorized(response);
            return;
        }
        String sessionId = request.getHeader(HttpHeaders.MCP_SESSION_ID);
        if (sessionId == null) {
            responseError(response, HttpServletResponse.SC_BAD_REQUEST, "Session ID required in mcp-session-id header");
            return;
        }
        McpStreamableServerSession session = sessions.get(sessionId);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            session.delete().block();
            sessions.remove(sessionId);
            sessionTransports.remove(sessionId);
            sessionTokens.remove(sessionId);
            SubscriptionManager mgr = subscriptionManager;
            if (mgr != null) {
                mgr.onSessionClosed(sessionId);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.debug("Streamable DELETE session {} failed: {}", sessionId, e.getMessage());
            responseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Objects.requireNonNullElse(e.getMessage(), "Unknown error"));
        }
    }

    private void responseError(HttpServletResponse response, int status, String message) throws IOException {
        McpError err = McpError.builder(McpSchema.ErrorCodes.INTERNAL_ERROR).message(message).build();
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);
        response.setStatus(status);
        String body = jsonMapper.writeValueAsString(err);
        PrintWriter writer = response.getWriter();
        writer.write(body);
        writer.flush();
        traceResponse("error", status, APPLICATION_JSON, body);
    }

    /**
     * Sends an RFC 6750 401 challenge. Writes the response manually rather than via
     * {@link HttpServletResponse#sendError} because Jetty's sendError resets the
     * response buffer — which can strip the {@code WWW-Authenticate} header and
     * break OAuth discovery in the MCP client.
     */
    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        McpCloudWebhookService hook = cloudWebhook;
        String hookUrl = hook != null ? hook.getPublicUrl() : null;
        String metadataUrl = hookUrl != null ? hookUrl + "/.well-known/oauth-protected-resource"
                : OAuthMetadataServlet.PATH_PROTECTED_RESOURCE;
        String challenge = "Bearer realm=\"openhab-mcp\", resource_metadata=\"" + metadataUrl + "\"";
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", challenge);
        response.setContentType("text/plain");
        response.setCharacterEncoding(UTF_8);
        response.getWriter().write("Authentication required");
        response.getWriter().flush();
        traceResponse("401 unauth [WWW-Authenticate: " + challenge + "]", HttpServletResponse.SC_UNAUTHORIZED,
                "text/plain", "Authentication required");
    }

    private void sendKeepAlive() {
        if (isClosing.get() || sessionTransports.isEmpty()) {
            return;
        }
        for (Map.Entry<String, JavaxStreamableSessionTransport> entry : sessionTransports.entrySet()) {
            String sessionId = entry.getKey();
            JavaxStreamableSessionTransport transport = entry.getValue();
            try {
                transport.writeKeepAlive();
            } catch (Exception e) {
                logger.debug("Keep-alive failed for streamable session {}: {}", sessionId, e.getMessage());
                transport.close();
                sessionTransports.remove(sessionId);
            }
        }
    }

    /**
     * Buffers the request body so it can be read by {@link #traceRequest} and then
     * again by the handler. Returns {@code null} (and sends a 413 response) when the
     * body exceeds {@link #MAX_REQUEST_BODY_BYTES}.
     */
    private @Nullable HttpServletRequest cacheBody(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (request instanceof CachingRequestWrapper) {
            return request;
        }
        try {
            return new CachingRequestWrapper(request);
        } catch (RequestTooLargeException e) {
            logger.debug("Rejected {} {} — body size {} exceeds cap {}", request.getMethod(), request.getRequestURI(),
                    e.size, MAX_REQUEST_BODY_BYTES);
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "Request body exceeds " + MAX_REQUEST_BODY_BYTES + " bytes");
            return null;
        }
    }

    /**
     * TRACE-level dump of every inbound request before any processing: method, URI,
     * query, remote addr, every header, and the full body. No redaction — TRACE is
     * for raw debugging; Authorization tokens appear in plaintext, so only enable
     * TRACE when triaging.
     */
    private void traceRequest(HttpServletRequest request) {
        if (!logger.isTraceEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("→ ").append(request.getMethod()).append(' ').append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append('?').append(request.getQueryString());
        }
        sb.append(" from ").append(request.getRemoteAddr()).append('\n');
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            sb.append("    ").append(name).append(": ").append(request.getHeader(name)).append('\n');
        }
        if (request instanceof CachingRequestWrapper crw) {
            byte[] body = crw.getCachedBody();
            if (body.length > 0) {
                sb.append("    <body ").append(body.length).append(" bytes>\n");
                sb.append(new String(body, StandardCharsets.UTF_8));
            } else {
                sb.append("    <empty body>");
            }
        }
        logger.trace("Inbound request:\n{}", sb);
    }

    private void traceResponse(String tag, int status, @Nullable String contentType, @Nullable String body) {
        if (!logger.isTraceEnabled()) {
            return;
        }
        String preview = body == null ? "<empty>" : (body.length() > 800 ? body.substring(0, 800) + "…" : body);
        logger.trace("← {} {} Content-Type={} body={}", tag, status, contentType, preview);
    }

    @Override
    public void destroy() {
        closeGracefully().block();
        super.destroy();
    }

    /**
     * Per-session SSE transport for server→client messages. Each in-flight client
     * request that triggers an SSE response gets one, as does a listening stream
     * started via GET.
     */
    @NonNullByDefault({})
    private class JavaxStreamableSessionTransport implements McpStreamableServerTransport {

        private final String sessionId;
        private final AsyncContext asyncContext;
        private final PrintWriter writer;
        private final ReentrantLock lock = new ReentrantLock();
        private volatile boolean closed = false;

        JavaxStreamableSessionTransport(String sessionId, AsyncContext asyncContext, PrintWriter writer) {
            this.sessionId = sessionId;
            this.asyncContext = asyncContext;
            this.writer = writer;
        }

        void writeKeepAlive() throws IOException {
            lock.lock();
            try {
                if (closed) {
                    return;
                }
                writer.write(": keep-alive\n\n");
                writer.flush();
                if (writer.checkError()) {
                    throw new IOException("Client disconnected");
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
            return sendMessage(message, null);
        }

        @Override
        public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message, @Nullable String messageId) {
            return Mono.fromRunnable(() -> {
                if (closed) {
                    return;
                }
                lock.lock();
                try {
                    if (closed) {
                        return;
                    }
                    String jsonText = jsonMapper.writeValueAsString(message);
                    String id = messageId != null ? messageId : sessionId;
                    writer.write("id: " + id + "\n");
                    writer.write("event: " + MESSAGE_EVENT_TYPE + "\n");
                    writer.write("data: " + jsonText + "\n\n");
                    writer.flush();
                    if (writer.checkError()) {
                        throw new IOException("Client disconnected");
                    }
                    logger.debug("← SSE [session {}]: {}", sessionId,
                            jsonText.length() > 800 ? jsonText.substring(0, 800) + "…" : jsonText);
                } catch (Exception e) {
                    logger.debug("Failed to send to session {}: {}", sessionId, e.getMessage());
                    close();
                } finally {
                    lock.unlock();
                }
            });
        }

        @Override
        public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
            return jsonMapper.convertValue(data, typeRef);
        }

        @Override
        public Mono<Void> closeGracefully() {
            return Mono.fromRunnable(this::close);
        }

        @Override
        public void close() {
            lock.lock();
            try {
                if (closed) {
                    return;
                }
                closed = true;
                try {
                    asyncContext.complete();
                } catch (Exception e) {
                    logger.trace("AsyncContext complete for session {}: {}", sessionId, e.getMessage());
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * HttpServletRequest wrapper that pre-reads the body into memory so it can be
     * logged at TRACE and still consumed by the normal request-handling path. Bounded
     * to {@link #MAX_REQUEST_BODY_BYTES} to prevent unbounded heap use from a hostile
     * or misbehaving client — the cap matches the MCP SDK reference.
     */
    private static final class CachingRequestWrapper extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        CachingRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            long declared = request.getContentLengthLong();
            if (declared > MAX_REQUEST_BODY_BYTES) {
                throw new RequestTooLargeException(declared);
            }
            this.cachedBody = readBounded(request.getInputStream(), MAX_REQUEST_BODY_BYTES);
        }

        private static byte[] readBounded(InputStream in, int limit) throws IOException {
            byte[] buf = new byte[Math.min(8192, limit + 1)];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int total = 0;
            int n;
            while ((n = in.read(buf)) != -1) {
                total += n;
                if (total > limit) {
                    throw new RequestTooLargeException(total);
                }
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }

        byte[] getCachedBody() {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            ByteArrayInputStream buffer = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    return buffer.read();
                }

                @Override
                public boolean isFinished() {
                    return buffer.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(@Nullable ReadListener readListener) {
                    // async reads not supported on the replayed body
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    private static final class RequestTooLargeException extends IOException {

        private static final long serialVersionUID = 1L;
        final long size;

        RequestTooLargeException(long size) {
            super("Request body size " + size + " exceeds cap " + MAX_REQUEST_BODY_BYTES);
            this.size = size;
        }
    }
}
