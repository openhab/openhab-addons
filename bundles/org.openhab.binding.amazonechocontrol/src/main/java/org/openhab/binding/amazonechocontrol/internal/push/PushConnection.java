/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.push;

import static org.eclipse.jetty.http.HttpHeader.*;
import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpVersion.HTTP_2;
import static org.openhab.binding.amazonechocontrol.internal.push.PushConnection.State.*;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.ErrorCode;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PingFrame;
import org.eclipse.jetty.http2.frames.ResetFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushCommandTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushMessageTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link PushConnection} handles the HTTP/2 push connection
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class PushConnection implements PushSessionHandler.Listener, PushStreamAdapter.Listener {
    private static final long CONNECTION_TIMEOUT = 10;
    private static final int PING_TIMEOUT = 10;
    private static final String PUSH_STREAM_ID = "push-stream";

    private final Logger logger = LoggerFactory.getLogger(PushConnection.class);
    private final HTTP2Client http2Client;
    private final Gson gson;
    private final Listener listener;
    private final ScheduledExecutorService scheduler;

    private State state = CLOSED;
    private @Nullable Session session;
    private @Nullable ScheduledFuture<?> waitForPing;

    public PushConnection(HTTP2Client http2Client, Gson gson, Listener listener, ScheduledExecutorService scheduler) {
        this.http2Client = http2Client;
        this.gson = gson;
        this.listener = listener;
        this.scheduler = scheduler;
    }

    public State getState() {
        return state;
    }

    private void setState(State newState) {
        this.state = newState;
        listener.onPushConnectionStateChange(state);
    }

    public void open(String amazonSite, String accessToken) {
        cancelWaitForPing();
        Session session = this.session;
        if (state != CLOSED || session != null) {
            logger.warn(
                    "Tried to open a new session, but the the current state is {} - session hash {}. Please enable TRACE logging and report a bug.",
                    state, session != null ? session.hashCode() : "<no session>");
            return;
        }
        setState(State.CONNECTING);

        String host = switch (amazonSite) {
            case "amazon.com" -> "bob-dispatch-prod-na.amazon.com";
            case "amazon.com.br" -> "bob-dispatch-prod-na.amazon.com";
            case "amazon.co.jp" -> "bob-dispatch-prod-fe.amazon.com";
            default -> "bob-dispatch-prod-eu.amazon.com";
        };

        InetSocketAddress address = new InetSocketAddress(host, 443);
        PushSessionHandler sessionHandler = new PushSessionHandler(this);
        Promise.Completable<Session> sessionPromise = new Promise.Completable<>();
        sessionPromise.orTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS).handle((newSession, throwable) -> {
            if (throwable != null) {
                logger.warn("Failed to create session: {}", throwable.getMessage());
                setState(FAILED);
                close();
            } else {
                logger.trace("Created session with hash {}.", newSession.hashCode());
                this.session = newSession;
                openPushStream(newSession, host, accessToken);
            }
            return null;
        });
        http2Client.connect(http2Client.getBean(SslContextFactory.class), address, sessionHandler, sessionPromise);
    }

    public void sendPing() {
        Session session = this.session;
        // we need to be connected, have a non-closes session and no running ping-pong
        if (state == CONNECTED && session != null && !session.isClosed() && waitForPing == null) {
            logger.trace("Sending ping in session {}", session.hashCode());
            waitForPing = scheduler.schedule(this::close, PING_TIMEOUT, TimeUnit.SECONDS);
            session.ping(new PingFrame(false), Callback.NOOP);
        } else if (state != CLOSED && session != null && session.isClosed()) {
            close();
        }
    }

    public void close() {
        cancelWaitForPing();
        setState(State.DISCONNECTING);
        Session session = this.session;
        if (session != null && !session.isClosed()) {
            session.getStreams().stream().filter(s -> s.getAttribute(PUSH_STREAM_ID) != null && !s.isReset()).forEach(
                    s -> s.reset(new ResetFrame(s.getId(), ErrorCode.CANCEL_STREAM_ERROR.code), Callback.NOOP));
            session.close(ErrorCode.NO_ERROR.code, null, Callback.NOOP);
        }
        this.session = null;
        setState(CLOSED);
    }

    private void openPushStream(Session session, String host, String accessToken) {
        HttpFields headerFields = new HttpFields();
        headerFields.put(USER_AGENT, "okhttp/4.3.2-SNAPSHOT");
        headerFields.put(AUTHORIZATION, "Bearer " + accessToken);
        HttpURI uri = new HttpURI("https://" + host + "/v20160207/directives");
        HeadersFrame headers = new HeadersFrame(new MetaData.Request(GET.asString(), uri, HTTP_2, headerFields), null,
                false);

        PushStreamAdapter eventListener = new PushStreamAdapter(gson, session, this);
        Promise.Completable<Stream> streamPromise = new Promise.Completable<>();
        streamPromise.orTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS).handle((stream, throwable) -> {
            if (throwable != null) {
                logger.warn("Failed to open stream in session {}: {}", session.hashCode(), throwable.getMessage());
                setState(FAILED);
            } else {
                logger.debug("Successfully initiated stream for session {}.", session.hashCode());
                stream.setIdleTimeout(0);
                stream.setAttribute(PUSH_STREAM_ID, session);
                setState(State.CONNECTED);
            }
            return null;
        });
        session.newStream(headers, streamPromise, eventListener);
    }

    @Override
    public void onSessionClosed(int sessionHashCode) {
        cancelWaitForPing();
        Session currentSession = session;
        if (currentSession != null && currentSession.hashCode() == sessionHashCode) {
            setState(CLOSED);
            this.session = null;
        } else {
            logger.debug("Received a session closed for session {}, but the current session is {}", sessionHashCode,
                    currentSession != null ? currentSession.hashCode() : "<not set>");
        }
    }

    @Override
    public void onSessionFailed(int sessionHashCode) {
        cancelWaitForPing();
        Session currentSession = session;
        if (currentSession != null && currentSession.hashCode() == sessionHashCode) {
            setState(FAILED);
        } else {
            logger.debug("Received a session failed for session {}, but the current session is {}", sessionHashCode,
                    currentSession != null ? currentSession.hashCode() : "<not set>");
        }
    }

    @Override
    public void onPushMessageReceived(PushMessageTO.RenderingUpdateTO renderingUpdate) {
        PushCommandTO pushCommand = gson.fromJson(renderingUpdate.resourceMetadata, PushCommandTO.class);
        if (pushCommand != null) {
            listener.onPushCommandReceived(pushCommand);
        }
    }

    @Override
    public void onSessionPingReceived() {
        logger.trace("Cancelling pingWaitJob");
        cancelWaitForPing();
    }

    private void cancelWaitForPing() {
        ScheduledFuture<?> waitForPing = this.waitForPing;
        if (waitForPing != null) {
            waitForPing.cancel(true);
            this.waitForPing = null;
        }
    }

    public interface Listener {
        void onPushConnectionStateChange(State state);

        void onPushCommandReceived(PushCommandTO pushCommand);
    }

    public enum State {
        CLOSED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        FAILED
    }
}
