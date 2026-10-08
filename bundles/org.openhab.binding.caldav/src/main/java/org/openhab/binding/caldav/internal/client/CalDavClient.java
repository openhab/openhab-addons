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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * @author Andreas Vilippus - Preemptive BASIC authentication
 * @author Andreas Vilippus - XML response encoding
 * @author Andreas Vilippus - Preserve structured transport failure causes
 */
@NonNullByDefault
public final class CalDavClient implements DavTransport {
    public static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;
    private final HttpClient client;
    private final URI origin;
    private final int timeout;
    private final Authentication.@Nullable Result preemptiveAuthentication;

    public CalDavClient(HttpClient client, AccountConfiguration configuration) {
        this.client = client;
        this.origin = CalDavUris.validate(URI.create(configuration.url));
        this.timeout = configuration.requestTimeout;
        Authentication.@Nullable Result authentication = null;
        if (!configuration.username.isBlank()) {
            URI authRoot = origin.resolve("/");
            if ("BASIC".equals(configuration.authType)) {
                authentication = new BasicAuthentication.BasicResult(authRoot, configuration.username,
                        configuration.password);
            } else {
                var store = client.getAuthenticationStore();
                store.addAuthentication(new DigestAuthentication(authRoot, Authentication.ANY_REALM,
                        configuration.username, configuration.password));
                if (!"DIGEST".equals(configuration.authType)) {
                    store.addAuthentication(new BasicAuthentication(authRoot, Authentication.ANY_REALM,
                            configuration.username, configuration.password));
                }
            }
        }
        this.preemptiveAuthentication = authentication;
    }

    @Override
    public String request(String method, URI uri, String body, String depth) throws IOException, InterruptedException {
        URI target = CalDavUris.resolve(origin, uri.toString());
        Request request = client.newRequest(target).method(method).followRedirects(false)
                .timeout(timeout, TimeUnit.SECONDS).header("Depth", depth);
        if (!body.isEmpty()) {
            request.content(new StringContentProvider("application/xml", body, StandardCharsets.UTF_8));
        }
        Authentication.Result authentication = preemptiveAuthentication;
        if (authentication != null) {
            authentication.apply(request);
        }
        CompletableFuture<String> result = new CompletableFuture<>();
        request.send(new BufferingResponseListener(MAX_RESPONSE_BYTES) {
            @Override
            public void onComplete(Result response) {
                var received = response.getResponse();
                int status = received == null ? 0 : received.getStatus();
                if (response.isFailed()) {
                    result.completeExceptionally(
                            status >= 300 ? new CalDavHttpException(method, status, false, false, response.getFailure())
                                    : new IOException("CalDAV transport failed", response.getFailure()));
                    return;
                }
                String content;
                try {
                    content = decodeContent(this);
                } catch (IOException e) {
                    if (status >= 200 && status < 300) {
                        result.completeExceptionally(e);
                        return;
                    }
                    content = "";
                }
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

    private static String decodeContent(BufferingResponseListener response) throws IOException {
        byte[] bytes = response.getContent();
        if (bytes.length == 0) {
            return "";
        }
        String encoding = response.getEncoding();
        // RFC 7303: a byte-order mark takes precedence over the HTTP charset.
        if (bytes.length >= 4 && bytes[0] == 0 && bytes[1] == 0 && bytes[2] == (byte) 0xFE && bytes[3] == (byte) 0xFF) {
            encoding = "UTF-32BE";
        } else if (bytes.length >= 4 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE && bytes[2] == 0
                && bytes[3] == 0) {
            encoding = "UTF-32LE";
        } else if (bytes.length >= 2 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
            encoding = "UTF-16BE";
        } else if (bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
            encoding = "UTF-16LE";
        } else if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            encoding = "UTF-8";
        }
        if (encoding == null) {
            XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            try {
                var reader = factory.createXMLStreamReader(new ByteArrayInputStream(bytes));
                try {
                    encoding = reader.getEncoding();
                } finally {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                // Calendar GETs and non-XML error bodies default to UTF-8.
            }
        }
        try {
            String content = new String(bytes, encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding));
            return content.startsWith("\uFEFF") ? content.substring(1) : content;
        } catch (IllegalArgumentException e) {
            throw new IOException("Unsupported CalDAV response encoding", e);
        }
    }
}
