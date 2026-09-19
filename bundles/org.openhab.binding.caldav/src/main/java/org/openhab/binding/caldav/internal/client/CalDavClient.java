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
package org.openhab.binding.caldav.internal.client;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;

/**
 * Bounded HTTP transport with authentication isolated to one account.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Account transport and security limits
 */
@NonNullByDefault
public final class CalDavClient implements DavTransport {
    public static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;
    private final HttpClient client;
    private final URI origin;
    private final int timeout;

    public CalDavClient(HttpClient client, AccountConfiguration configuration) {
        this.client = client;
        this.origin = CalDavUris.validate(URI.create(configuration.url));
        this.timeout = configuration.requestTimeout;
        URI authRoot = origin.resolve("/");
        var store = client.getAuthenticationStore();
        if (!"BASIC".equals(configuration.authType)) {
            store.addAuthentication(new DigestAuthentication(authRoot, Authentication.ANY_REALM, configuration.username,
                    configuration.password));
        }
        if (!"DIGEST".equals(configuration.authType)) {
            store.addAuthentication(new BasicAuthentication(authRoot, Authentication.ANY_REALM, configuration.username,
                    configuration.password));
        }
    }

    @Override
    public String request(String method, URI uri, String body, String depth) throws IOException, InterruptedException {
        URI target = CalDavUris.resolve(origin, uri.toString());
        Request request = client.newRequest(target).method(method).followRedirects(false)
                .timeout(timeout, TimeUnit.SECONDS).header("Depth", depth);
        if (!body.isEmpty()) {
            request.content(new StringContentProvider("application/xml", body, StandardCharsets.UTF_8));
        }
        CompletableFuture<String> result = new CompletableFuture<>();
        request.send(new BufferingResponseListener(MAX_RESPONSE_BYTES) {
            @Override
            public void onComplete(Result response) {
                if (response.isFailed()) {
                    result.completeExceptionally(new IOException("CalDAV transport failed"));
                    return;
                }
                int status = response.getResponse().getStatus();
                String content = java.util.Objects.requireNonNullElse(getContentAsString(StandardCharsets.UTF_8), "");
                if (status < 200 || status >= 300) {
                    boolean invalidToken = false;
                    boolean unsupportedReport = false;
                    if (status == 403) {
                        try {
                            var error = CalDavXml.parse(content).getDocumentElement();
                            if ("DAV:".equals(error.getNamespaceURI()) && "error".equals(error.getLocalName())) {
                                invalidToken = !DavResponse.children(error, "DAV:", "valid-sync-token").isEmpty();
                                unsupportedReport = !DavResponse.children(error, "DAV:", "supported-report").isEmpty();
                            }
                        } catch (Exception ignored) {
                            // A non-XML HTTP error remains an HTTP error, never a token reset.
                        }
                    }
                    result.completeExceptionally(
                            new CalDavHttpException(method, status, invalidToken, unsupportedReport));
                } else {
                    result.complete(content);
                }
            }
        });
        try {
            return result.get(timeout + 1L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            request.abort(e);
            Thread.currentThread().interrupt();
            throw e;
        } catch (TimeoutException e) {
            request.abort(e);
            throw new IOException("CalDAV request timed out", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException failure) {
                throw failure;
            }
            throw new IOException("CalDAV transport failed", e.getCause());
        }
    }
}
