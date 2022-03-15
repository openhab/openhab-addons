/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.freeboxos.internal.api.Response.ErrorCode;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiHandler} is responsible for sending requests toward
 * a given url and transform the answer in appropriate dto.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ApiHandler.class, configurationPid = "binding.freeboxos")
public class ApiHandler {
    private static final String AUTH_HEADER = "X-Fbx-App-Auth";
    private static final String CONTENT_TYPE = "application/json; charset=" + DEFAULT_CHARSET.name();

    private final Logger logger = LoggerFactory.getLogger(ApiHandler.class);
    private final HttpClient httpClient;
    private final FBDeserializer deserializer;
    private long defaultTimeoutInMs = TimeUnit.SECONDS.toMillis(8);

    @Activate
    public ApiHandler(@Reference HttpClientFactory httpClientFactory, @Reference FBDeserializer deserializer,
            Map<String, Object> config) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.deserializer = deserializer;
        configChanged(config);
    }

    @Modified
    public void configChanged(Map<String, Object> config) {
        String timeout = (String) config.get(TIMEOUT);
        if (timeout != null) {
            defaultTimeoutInMs = TimeUnit.SECONDS.toMillis(Long.parseLong(timeout));
            logger.debug("Timeout set to {} seconds", timeout);
        }
    }

    public synchronized <T> T executeUri(URI uri, HttpMethod method, Class<T> clazz, @Nullable String sessionToken,
            @Nullable Object payload) throws FreeboxException {
        logger.debug("executeUrl {} - {} ", method, uri);

        Request request = httpClient.newRequest(uri).method(method).timeout(defaultTimeoutInMs, TimeUnit.MILLISECONDS)
                .header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE);

        if (sessionToken != null) {
            request.header(AUTH_HEADER, sessionToken);
        }

        if (payload != null) {
            request.content(deserializer.serialize(payload), null);
        }
        try {
            ContentResponse response = request.send();
            Code statusCode = HttpStatus.getCode(response.getStatus());
            String content = new String(response.getContent(), DEFAULT_CHARSET);
            logger.trace("executeUrl {} - {} returned {}", method, uri, content);
            if (statusCode == Code.OK) {
                return deserializer.deserialize(clazz, content);
            } else if (statusCode == Code.FORBIDDEN) {
                logger.debug("Fobidden, serviceReponse was {}, ", content);
                throw new FreeboxException(ErrorCode.AUTHORIZATION_REQUIRED);
            }
            throw new FreeboxException("Error '%s' requesting : %s", statusCode.getMessage(), uri.toString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new FreeboxException(e, "Exception while calling %s", request.getURI());
        }
    }
}
