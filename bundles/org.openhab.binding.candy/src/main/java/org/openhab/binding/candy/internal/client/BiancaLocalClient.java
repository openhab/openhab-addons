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
package org.openhab.binding.candy.internal.client;

import static org.openhab.binding.candy.internal.protocol.CandyProtocolException.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.openhab.binding.candy.internal.BiancaConfiguration;
import org.openhab.binding.candy.internal.protocol.BiancaSnapshot;
import org.openhab.binding.candy.internal.protocol.BiancaStatusParser;
import org.openhab.binding.candy.internal.protocol.CandyCodec;
import org.openhab.binding.candy.internal.protocol.CandyInvalidHostException;
import org.openhab.binding.candy.internal.protocol.CandyProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronous, cancellable local status and command requests using a framework-owned HTTP client.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class BiancaLocalClient {
    private final Logger logger = LoggerFactory.getLogger(BiancaLocalClient.class);
    private final HttpClient httpClient;
    private final BiancaStatusParser parser = new BiancaStatusParser();

    public BiancaLocalClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<BiancaSnapshot> read(BiancaConfiguration configuration) {
        return read(configuration, List.of(configuration.encryptionKey));
    }

    /** Matches account keys against one local response and retains only the key that decodes usable washer status. */
    public CompletableFuture<BiancaSnapshot> read(BiancaConfiguration configuration, List<String> candidateKeys) {
        Optional<URI> statusUri = configuration.statusUri();
        if (statusUri.isEmpty()) {
            logger.debug("Skipping local status read: invalid appliance host");
            return CompletableFuture.failedFuture(new CandyInvalidHostException());
        }
        CompletableFuture<BiancaSnapshot> future = new CompletableFuture<>();
        List<String> keys = List.copyOf(candidateKeys);
        String host = statusUri.get().getHost();
        logger.debug("Reading local washer status from {} with {} candidate keys (timeout {} s)", host, keys.size(),
                configuration.requestTimeout);
        Request request;
        try {
            request = httpClient.newRequest(statusUri.get()).param("encrypted", "1").followRedirects(false)
                    .timeout(configuration.requestTimeout, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            logger.debug("Cannot create local status request: HTTP client unavailable", e);
            return CompletableFuture.failedFuture(new CandyProtocolException(CLIENT_UNAVAILABLE, e));
        }
        future.whenComplete((snapshot, failure) -> {
            if (future.isCancelled()) {
                logger.debug("Cancelling local status request for {}", host);
                request.abort(new CancellationException("Status read cancelled"));
            }
        });
        BufferingResponseListener listener = new BufferingResponseListener(CandyCodec.MAX_WIRE_BYTES) {
            @Override
            public void onComplete(Result result) {
                if (future.isDone()) {
                    logger.debug("Ignoring completed or cancelled local status response from {}", host);
                    return;
                }
                if (result.isFailed()) {
                    Throwable failure = result.getFailure();
                    String message;
                    if (!httpClient.isRunning()) {
                        message = CLIENT_UNAVAILABLE;
                    } else {
                        message = failure instanceof TimeoutException ? TIMEOUT : TRANSPORT;
                    }
                    logger.debug("Local status request for {} failed: {}", host, message, failure);
                    future.completeExceptionally(new CandyProtocolException(message, failure));
                } else if (result.getResponse().getStatus() != 200) {
                    logger.debug("Local status request for {} returned HTTP {}", host,
                            result.getResponse().getStatus());
                    future.completeExceptionally(new CandyProtocolException(HTTP_STATUS));
                } else {
                    try {
                        byte[] content = getContent();
                        if (content == null) {
                            logger.debug("Local status response from {} was empty", host);
                            future.completeExceptionally(new CandyProtocolException("@text/protocol-error-empty"));
                            return;
                        }
                        logger.debug("Received {} bytes of local status from {}", content.length, host);
                        String wire = new String(content, StandardCharsets.US_ASCII);
                        for (String key : keys) {
                            try {
                                BiancaSnapshot snapshot = parser.parse(CandyCodec.decode(wire, key));
                                if (!future.isDone()) {
                                    if (configuration.encryptionKey.isEmpty()) {
                                        configuration.encryptionKey = key;
                                        logger.debug("Matched an account key to {}; retaining it for local polling",
                                                host);
                                    }
                                    logger.debug("Decoded usable local status from {}", host);
                                    future.complete(snapshot);
                                }
                                return;
                            } catch (CandyProtocolException e) {
                                if (keys.size() == 1) {
                                    throw e;
                                }
                            }
                        }
                        logger.debug("None of the {} candidate keys decoded usable local status from {}", keys.size(),
                                host);
                        future.completeExceptionally(new CandyProtocolException("@text/account-error-key-match"));
                    } catch (CandyProtocolException e) {
                        logger.debug("Rejected local status from {}", host, e);
                        future.completeExceptionally(e);
                    }
                }
            }
        };
        try {
            request.send(listener);
        } catch (IllegalStateException e) {
            logger.debug("Cannot send local status request for {}: HTTP client unavailable", host, e);
            future.completeExceptionally(new CandyProtocolException(CLIENT_UNAVAILABLE, e));
        }
        return future;
    }

    /** Sends exactly one local write; neither ambiguous failures nor redirects are replayed. */
    public CompletableFuture<Integer> write(BiancaConfiguration configuration, String payload) {
        Optional<URI> statusUri = configuration.statusUri();
        if (statusUri.isEmpty()) {
            return CompletableFuture.failedFuture(new CandyInvalidHostException());
        }
        CompletableFuture<Integer> future = new CompletableFuture<>();
        Request request;
        try {
            request = httpClient.newRequest(statusUri.get().resolve("/http-write.json")).param("encrypted", "1")
                    .param("data", CandyCodec.encode(payload, configuration.encryptionKey)).followRedirects(false)
                    .timeout(configuration.requestTimeout, TimeUnit.SECONDS);
        } catch (CandyProtocolException e) {
            return CompletableFuture.failedFuture(e);
        } catch (IllegalStateException e) {
            return CompletableFuture.failedFuture(new CandyProtocolException(CLIENT_UNAVAILABLE, e));
        }
        future.whenComplete((ignored, failure) -> {
            if (future.isCancelled()) {
                request.abort(new CancellationException("Command cancelled"));
            }
        });
        logger.debug("Sending one local command to {}", statusUri.get().getHost());
        try {
            request.send(new BufferingResponseListener(CandyCodec.MAX_WIRE_BYTES) {
                @Override
                public void onComplete(Result result) {
                    if (future.isDone()) {
                        return;
                    }
                    if (result.isFailed()) {
                        Throwable failure = result.getFailure();
                        future.completeExceptionally(new CandyProtocolException(
                                failure instanceof TimeoutException ? TIMEOUT : TRANSPORT, failure));
                    } else {
                        int status = result.getResponse().getStatus();
                        logger.debug("Local command HTTP response: {}", status);
                        if (status >= 200 && status < 300) {
                            // The app accepts HTTP success; only subsequent status can confirm the command's effect.
                            future.complete(status);
                        } else {
                            future.completeExceptionally(new CandyProtocolException(HTTP_STATUS));
                        }
                    }
                }
            });
        } catch (IllegalStateException e) {
            future.completeExceptionally(new CandyProtocolException(CLIENT_UNAVAILABLE, e));
        }
        return future;
    }
}
