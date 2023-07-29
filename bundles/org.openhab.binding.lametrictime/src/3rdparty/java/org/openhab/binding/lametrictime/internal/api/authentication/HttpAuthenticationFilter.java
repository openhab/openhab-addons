/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.openhab.binding.lametrictime.internal.api.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Http Authentication filter that provides basic and digest authentication (based on RFC 2617).
 *
 * @author Miroslav Fuksa
 */
@Priority(Priorities.AUTHENTICATION)
class HttpAuthenticationFilter implements ClientRequestFilter, ClientResponseFilter {

    /**
     * Authentication type.
     */
    enum Type {
        /**
         * Basic authentication.
         */
        BASIC
    }

    private static final String REQUEST_PROPERTY_FILTER_REUSED = "org.openhab.binding.lametrictime.api.authentication.HttpAuthenticationFilter.reused";
    private static final String REQUEST_PROPERTY_OPERATION = "org.openhab.binding.lametrictime.api.authentication.HttpAuthenticationFilter.operation";

    /**
     * Encoding used for authentication calculations.
     */
    static final Charset CHARACTER_SET = Charset.forName("iso-8859-1");

    private final HttpAuthenticationFeature.Mode mode;

    private final BasicAuthenticator basicAuth;

    /**
     * Create a new filter instance.
     *
     * @param mode Mode.
     * @param basicCredentials Basic credentials (can be {@code null} if this filter does not work in the
     *            basic mode or if no default credentials are defined).
     * @param digestCredentials Digest credentials (can be {@code null} if this filter does not work in the
     *            digest mode or if no default credentials are defined).
     * @param configuration Configuration (non-{@code null}).
     */
    HttpAuthenticationFilter(HttpAuthenticationFeature.Mode mode, Credentials basicCredentials,
            Configuration configuration) {
        this.mode = mode;
        switch (mode) {
            case BASIC_PREEMPTIVE:
            case BASIC_NON_PREEMPTIVE:
                this.basicAuth = new BasicAuthenticator(basicCredentials);
                break;
            case UNIVERSAL:
                this.basicAuth = new BasicAuthenticator(basicCredentials);
                break;
            default:
                throw new IllegalStateException("Not implemented.");
        }
    }

    @Override
    public void filter(ClientRequestContext request) throws IOException {
        if ("true".equals(request.getProperty(REQUEST_PROPERTY_FILTER_REUSED))) {
            return;
        }

        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return;
        }

        Type operation = null;
        if (mode == HttpAuthenticationFeature.Mode.BASIC_PREEMPTIVE) {
            basicAuth.filterRequest(request);
            operation = Type.BASIC;
        } else if (mode == HttpAuthenticationFeature.Mode.BASIC_NON_PREEMPTIVE) {
            // do nothing
        }

        if (operation != null) {
            request.setProperty(REQUEST_PROPERTY_OPERATION, operation);
        }
    }

    @Override
    public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
        if ("true".equals(request.getProperty(REQUEST_PROPERTY_FILTER_REUSED))) {
            return;
        }

        Type result = null; // which authentication is requested: BASIC or DIGEST
        boolean authenticate;

        if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
            String authString = response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE);
            if (authString != null) {
                final String upperCaseAuth = authString.trim().toUpperCase();
                if (upperCaseAuth.startsWith("BASIC")) {
                    result = Type.BASIC;
                } else {
                    // unknown authentication -> this filter cannot authenticate with this method
                    return;
                }
            }
            authenticate = true;
        } else {
            authenticate = false;
        }

        if (mode == HttpAuthenticationFeature.Mode.BASIC_PREEMPTIVE) {
            // do nothing -> 401 will be returned to the client
        } else if (mode == HttpAuthenticationFeature.Mode.BASIC_NON_PREEMPTIVE) {
            if (authenticate && result == Type.BASIC) {
                basicAuth.filterResponseAndAuthenticate(request, response);
            }
        } else if (mode == HttpAuthenticationFeature.Mode.UNIVERSAL) {
            if (authenticate) {
                boolean success = false;

                // now we have the challenge response and we can authenticate
                if (result == Type.BASIC) {
                    success = basicAuth.filterResponseAndAuthenticate(request, response);
                }
            }
        }
    }

    private String getCacheKey(ClientRequestContext request) {
        return request.getUri().toString() + ":" + request.getMethod();
    }

    /**
     * Repeat the {@code request} with provided {@code newAuthorizationHeader}
     * and update the {@code response} with newest response data.
     *
     * @param request Request context.
     * @param response Response context (will be updated with the new response data).
     * @param newAuthorizationHeader {@code Authorization} header that should be added to the new request.
     * @return {@code true} is the authentication was successful ({@code true} if 401 response code was not returned;
     *         {@code false} otherwise).
     */
    static boolean repeatRequest(ClientRequestContext request, ClientResponseContext response,
            String newAuthorizationHeader) {
        Client client = ClientBuilder.newClient(request.getConfiguration());
        String method = request.getMethod();
        MediaType mediaType = request.getMediaType();
        URI lUri = request.getUri();

        WebTarget resourceTarget = client.target(lUri);

        Invocation.Builder builder = resourceTarget.request(mediaType);

        MultivaluedMap<String, Object> newHeaders = new MultivaluedHashMap<String, Object>();

        for (Map.Entry<String, List<Object>> entry : request.getHeaders().entrySet()) {
            if (HttpHeaders.AUTHORIZATION.equals(entry.getKey())) {
                continue;
            }
            newHeaders.put(entry.getKey(), entry.getValue());
        }

        newHeaders.add(HttpHeaders.AUTHORIZATION, newAuthorizationHeader);
        builder.headers(newHeaders);

        builder.property(REQUEST_PROPERTY_FILTER_REUSED, "true");

        Invocation invocation;
        if (request.getEntity() == null) {
            invocation = builder.build(method);
        } else {
            invocation = builder.build(method, Entity.entity(request.getEntity(), request.getMediaType()));
        }
        Response nextResponse = invocation.invoke();

        if (nextResponse.hasEntity()) {
            response.setEntityStream(nextResponse.readEntity(InputStream.class));
        }
        MultivaluedMap<String, String> headers = response.getHeaders();
        headers.clear();
        headers.putAll(nextResponse.getStringHeaders());
        response.setStatus(nextResponse.getStatus());

        return response.getStatus() != Response.Status.UNAUTHORIZED.getStatusCode();
    }

    /**
     * Credentials (username + password).
     */
    static class Credentials {

        private final String username;
        private final byte[] password;

        /**
         * Create a new credentials from username and password as byte array.
         *
         * @param username Username.
         * @param password Password as byte array.
         */
        Credentials(String username, byte[] password) {
            this.username = username;
            this.password = password;
        }

        /**
         * Create a new credentials from username and password as {@link String}.
         *
         * @param username Username.
         * @param password {@code String} password.
         */
        Credentials(String username, String password) {
            this.username = username;
            this.password = password == null ? null : password.getBytes(CHARACTER_SET);
        }

        /**
         * Return username.
         *
         * @return username.
         */
        String getUsername() {
            return username;
        }

        /**
         * Return password as byte array.
         *
         * @return Password string in byte array representation.
         */
        byte[] getPassword() {
            return password;
        }
    }

    private static Credentials extractCredentials(ClientRequestContext request, Type type) {
        String usernameKey = null;
        String passwordKey = null;
        if (type == null) {
            usernameKey = HttpAuthenticationFeature.HTTP_AUTHENTICATION_USERNAME;
            passwordKey = HttpAuthenticationFeature.HTTP_AUTHENTICATION_PASSWORD;
        } else if (type == Type.BASIC) {
            usernameKey = HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME;
            passwordKey = HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD;
        }

        String userName = (String) request.getProperty(usernameKey);
        if (userName != null && !userName.equals("")) {
            byte[] pwdBytes;
            Object password = request.getProperty(passwordKey);
            if (password instanceof byte[]) {
                pwdBytes = ((byte[]) password);
            } else if (password instanceof String) {
                pwdBytes = ((String) password).getBytes(CHARACTER_SET);
            } else {
                throw new RequestAuthenticationException("Passwort invalid.");
            }
            return new Credentials(userName, pwdBytes);
        }
        return null;
    }

    /**
     * Get credentials actual for the current request. Priorities in credentials selection are the following:
     * <ol>
     * <li>Basic/digest specific credentials defined in the request properties</li>
     * <li>Common credentials defined in the request properties</li>
     * <li>{@code defaultCredentials}</li>
     * </ol>
     *
     * @param request Request from which credentials should be extracted.
     * @param defaultCredentials Default credentials (can be {@code null}).
     * @param type Type of requested credentials.
     * @return Credentials or {@code null} if no credentials are found and {@code defaultCredentials} are {@code null}.
     * @throws RequestAuthenticationException in case the {@code username} or {@code password} is invalid.
     */
    static Credentials getCredentials(ClientRequestContext request, Credentials defaultCredentials, Type type) {
        Credentials commonCredentials = extractCredentials(request, type);

        if (commonCredentials != null) {
            return commonCredentials;
        } else {
            Credentials specificCredentials = extractCredentials(request, null);

            return specificCredentials != null ? specificCredentials : defaultCredentials;
        }
    }
}
