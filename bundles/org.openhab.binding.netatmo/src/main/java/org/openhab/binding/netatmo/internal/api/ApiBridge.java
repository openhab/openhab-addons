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
package org.openhab.binding.netatmo.internal.api;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.SERVICE_PID;
import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.URL_API;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration.Credentials;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiBridge} allows the communication to the various Netatmo rest apis
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ApiBridge.class, configurationPid = "binding.netatmo")
public class ApiBridge {
    private static final int TIMEOUT_MS = 10000;

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(SERVICE_PID);
    private final Logger logger = LoggerFactory.getLogger(ApiBridge.class);
    private final List<ConnectionListener> listeners = new ArrayList<>();

    private final HttpClient httpClient;
    private final AuthenticationApi connectApi;
    private final NADeserializer deserializer;

    private NetatmoBindingConfiguration configuration = new NetatmoBindingConfiguration();
    private Map<Class<? extends RestManager>, RestManager> managers = new HashMap<>();
    private Optional<ScheduledFuture<?>> connectJob = Optional.empty();

    @Activate
    public ApiBridge(@Reference HttpClientFactory factory, @Reference NADeserializer deserializer,
            @Reference OAuthFactory oAuthFactory, Map<String, Object> config) {
        this.connectApi = new AuthenticationApi(this);
        this.httpClient = factory.getCommonHttpClient();
        this.deserializer = deserializer;

        openConnection(config);
    }

    @Modified
    public void openConnection(@Nullable Map<String, Object> config) {
        try {
            if (config != null) {
                configuration.update(config);
            }
            Credentials credentials = configuration.getCredentials();
            logger.debug("Updated binding configuration to {}", credentials);
            if (credentials != null) {
                connectApi.authenticate(credentials);
                notifyListeners();
            }
        } catch (NetatmoException e) {
            prepareReconnection(e);
        }
    }

    @Deactivate
    public void dispose() {
        connectApi.dispose();
        freeConnectJob();
    }

    private void freeConnectJob() {
        connectJob.ifPresent(j -> j.cancel(true));
        connectJob = Optional.empty();
    }

    private void prepareReconnection(NetatmoException e) {
        connectApi.disconnect();
        notifyListeners();
        freeConnectJob();
        connectJob = Optional
                .of(scheduler.schedule(() -> openConnection(null), configuration.reconnectInterval, TimeUnit.SECONDS));
    }

    @SuppressWarnings("unchecked")
    public <T extends RestManager> @Nullable T getRestManager(Class<T> clazz) {
        if (!managers.containsKey(clazz)) {
            try {
                Constructor<T> constructor = clazz.getConstructor(ApiBridge.class);
                managers.put(clazz, constructor.newInstance(this));
            } catch (SecurityException | ReflectiveOperationException e) {
                logger.warn("Error invoking RestManager constructor for class {} : {}", clazz, e.getMessage());
            }
        }
        return (T) managers.get(clazz);
    }

    synchronized <T> T executeUri(URI uri, HttpMethod method, Class<T> clazz, @Nullable String payload)
            throws NetatmoException {
        try {
            logger.trace("executeUri {}  {} ", method.toString(), uri);

            Request request = httpClient.newRequest(uri).method(method).timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .header(HttpHeader.CONTENT_TYPE, String.format("application/%s;charset=UTF-8",
                            URL_API.contains(uri.getHost()) ? "x-www-form-urlencoded" : "json"));

            String auth = connectApi.getAuthorization();
            if (auth != null) {
                request.header(HttpHeader.AUTHORIZATION, auth);
            }

            if (payload != null && (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))) {
                InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                try (InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(stream)) {
                    request.content(inputStreamContentProvider, null);
                }
                if (!URL_API.contains(uri.getHost())) {
                    request.getHeaders().remove(HttpHeader.CONTENT_TYPE);
                    request.header(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8");
                }
            }

            ContentResponse response = request.send();

            Code statusCode = HttpStatus.getCode(response.getStatus());
            String responseBody = new String(response.getContent(), StandardCharsets.UTF_8);
            logger.trace("executeUri returned : {} ", responseBody);

            if (statusCode == Code.OK) {
                return deserializer.deserialize(clazz, responseBody);
            } else {
                NetatmoException exception;
                if (statusCode == Code.BAD_REQUEST) {
                    ApiError error = deserializer.deserialize(ApiError.class, responseBody);
                    exception = new NetatmoException(error.getCode(), error.getMessage());
                } else if (statusCode == Code.FORBIDDEN) {
                    ApiError error = deserializer.deserialize(ApiError.class, responseBody);
                    exception = new NetatmoException(error.getCode(), error.getMessage());
                    prepareReconnection(exception);
                } else {
                    exception = new NetatmoException(statusCode.getCode(), responseBody);
                    prepareReconnection(exception);
                }
                throw exception;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new NetatmoException(e, "Exception while calling %s", uri.toString());
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
        listener.connectionEvent(isConnected());
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        logger.debug("Connection status changed : {}", isConnected());
        listeners.forEach(l -> l.connectionEvent(isConnected()));
    }

    private boolean isConnected() {
        return connectApi.getAuthorization() != null;
    }

    public boolean matchesScopes(Set<Scope> requiredScopes) {
        return requiredScopes.isEmpty() || (isConnected() && connectApi.hasScopes(requiredScopes));
    }
}
