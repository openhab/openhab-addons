/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.openhab.binding.netatmo.internal.deserialization.NetatmoGson;
import org.openhab.binding.netatmo.internal.utils.BindingUtils;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
    private final Map<HttpHeader, String> httpHeaders = new HashMap<>();

    private final HttpClient httpClient;
    private final AuthenticationApi connectApi;
    private final Gson gson;

    private NetatmoBindingConfiguration configuration = new NetatmoBindingConfiguration();
    private Map<Class<? extends RestManager>, RestManager> managers = new HashMap<>();
    private ConnectionStatus connectionStatus = ConnectionStatus.UNKNOWN;
    private List<Scope> grantedScopes = List.of();

    private @Nullable ScheduledFuture<?> connectJob;

    @Activate
    public ApiBridge(@Reference HttpClientFactory httpClientFactory, @Reference NetatmoGson netatmoGson,
            ComponentContext componentContext) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.httpHeaders.put(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
        this.gson = netatmoGson.getGson();
        this.connectApi = new AuthenticationApi(this, configuration, scheduler);

        openConnection(BindingUtils.ComponentContextToMap(componentContext));
    }

    @Modified
    public void openConnection(@Nullable Map<String, Object> config) {
        try {
            configuration.update(
                    config != null ? new Configuration(config).as(NetatmoBindingConfiguration.class) : configuration);
            logger.debug("Updated binding configuration to {}", configuration);
            connectApi.authenticate();
            setConnectionStatus(ConnectionStatus.SUCCESS);
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
        ScheduledFuture<?> job = connectJob;
        if (job != null) {
            job.cancel(true);
        }
        connectJob = null;
    }

    private void prepareReconnection(NetatmoException e) {
        setConnectionStatus(ConnectionStatus.Failed("Will retry to connect Netatmo API, this call failed : %s", e));
        onAccessTokenResponse(null, List.of());
        freeConnectJob();
        connectJob = scheduler.schedule(() -> openConnection(null), configuration.reconnectInterval, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public <T extends RestManager> @Nullable T getRestManager(Class<T> typeOfRest) {
        if (!managers.containsKey(typeOfRest)) {
            try {
                Constructor<T> constructor = typeOfRest.getConstructor(ApiBridge.class);
                T tentative = constructor.newInstance(this);
                if (!grantedScopes.containsAll(tentative.getRequiredScopes())) {
                    throw new NetatmoException("Required scopes missing to access : " + typeOfRest);
                }
                managers.put(typeOfRest, tentative);
            } catch (SecurityException | ReflectiveOperationException | NetatmoException e) {
                logger.warn("Error invoking RestManager constructor for class {} : {}", typeOfRest, e.getMessage());
            }
        }
        return (T) managers.get(typeOfRest);
    }

    synchronized <T> T executeUri(URI uri, HttpMethod method, Class<T> classOfT, @Nullable String payload)
            throws NetatmoException {
        try {
            logger.trace("executeUri {}  {} ", method.toString(), uri);

            Request request = httpClient.newRequest(uri).method(method).timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS);

            httpHeaders.entrySet().forEach(entry -> request.header(entry.getKey(), entry.getValue()));

            if (payload != null && (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))) {
                InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                try (InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(stream)) {
                    request.content(inputStreamContentProvider, null);
                }
                if (!NetatmoConstants.URL_API.contains(uri.getHost())) {
                    request.getHeaders().remove(HttpHeader.CONTENT_TYPE);
                    request.header(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8");
                }
            }

            ContentResponse response = request.send();

            Code statusCode = HttpStatus.getCode(response.getStatus());
            String responseBody = new String(response.getContent(), StandardCharsets.UTF_8);
            logger.trace("executeUri returned : {} ", responseBody);

            if (statusCode == Code.OK) {
                return deserialize(classOfT, responseBody);
            } else {
                NetatmoException exception;
                if (statusCode == Code.BAD_REQUEST) {
                    ApiError error = deserialize(ApiError.class, responseBody);
                    exception = new NetatmoException(error.getCode(), error.getMessage());
                } else {
                    exception = new NetatmoException(statusCode.getCode(), responseBody);
                    prepareReconnection(exception);
                }
                throw exception;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new NetatmoException(String.format("Exception while calling %s", uri.toString()), e);
        }
    }

    public <T> T deserialize(Class<T> classOfT, String serviceAnswer) throws NetatmoException {
        try {
            @Nullable
            T result = gson.fromJson(serviceAnswer, classOfT);
            if (result != null) {
                return result;
            }
            throw new NetatmoException(String.format("Deserialization of '%s' resulted in null value", serviceAnswer));
        } catch (JsonSyntaxException e) {
            throw new NetatmoException(String.format("Unexpected error deserializing '%s'", serviceAnswer), e);
        }
    }

    void onAccessTokenResponse(@Nullable String accessToken, List<Scope> grantedScopes) {
        this.grantedScopes = grantedScopes;
        if (accessToken != null) {
            httpHeaders.put(HttpHeader.AUTHORIZATION, String.format("Bearer %s", accessToken));
        } else {
            httpHeaders.remove(HttpHeader.AUTHORIZATION);
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
        listener.apiConnectionStatusChanged(connectionStatus);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    private void setConnectionStatus(ConnectionStatus newStatus) {
        connectionStatus = newStatus;
        logger.debug("Connection status changed : {}", connectionStatus.getMessage());
        listeners.forEach(l -> l.apiConnectionStatusChanged(connectionStatus));
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
}
