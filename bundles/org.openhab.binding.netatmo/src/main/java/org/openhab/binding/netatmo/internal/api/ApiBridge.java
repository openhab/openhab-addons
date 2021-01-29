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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
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
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.openhab.binding.netatmo.internal.utils.BindingUtils;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link ApiBridge} allows the communication to the various Husqvarna rest apis like the
 * AutomowerConnectApi or the AuthenticationApi
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

    private NetatmoBindingConfiguration configuration = new NetatmoBindingConfiguration();
    private Map<Class<? extends RestManager>, Object> managers = new HashMap<>();
    private ConnectionStatus connectionStatus = ConnectionStatus.Unknown();
    private List<Scope> grantedScopes = List.of();

    @Activate
    public ApiBridge(@Reference OAuthFactory oAuthFactory, @Reference HttpClientFactory httpClientFactory,
            ComponentContext componentContext) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.httpHeaders.put(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
        this.connectApi = new AuthenticationApi(this, oAuthFactory, configuration, scheduler);

        modified(BindingUtils.ComponentContextToMap(componentContext));
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        configuration.update(new Configuration(config).as(NetatmoBindingConfiguration.class));
        logger.debug("Updated binding configuration to {}", configuration);
        testConnection();
    }

    private void testConnection() {
        try {
            configuration.checkIfValid();
            connectApi.authenticate();
            // Just a sample call to ensure connection is fine
            getHomeApi().getHomeData();
            setConnectionStatus(ConnectionStatus.Success());
        } catch (NetatmoException e) {
            if (e.getStatusCode() != 403) { // Forbidden Access maybe too many requests ? Let's wait next cycle
                setConnectionStatus(ConnectionStatus.Failed("Unable to connect Netatmo API : %s", e));
            } else {
                setConnectionStatus(ConnectionStatus.Failed("Connection failed, retrying"));
                scheduler.schedule(() -> testConnection(), configuration.reconnectInterval, TimeUnit.SECONDS);
            }
        }
    }

    private void setConnectionStatus(ConnectionStatus status) {
        connectionStatus = status;
        if (!connectionStatus.isConnected()) {
            onAccessTokenResponse("", List.of());
        }
        listeners.forEach(listener -> listener.notifyStatusChange(status));
    }

    @SuppressWarnings("unchecked")
    public <T extends RestManager> @Nullable T getRestManager(Class<T> typeOfRest) {
        if (!managers.containsKey(typeOfRest)) {
            try {
                Constructor<?> constructor = typeOfRest.getConstructor(ApiBridge.class);
                T tentative = (T) constructor.newInstance(new Object[] { this });
                if (grantedScopes.containsAll(tentative.getRequiredScopes())) {
                    managers.put(typeOfRest, tentative);
                } else {
                    logger.warn("Required scopes missing to access {}", typeOfRest);
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                logger.error("Error invoking RestManager constructor for class {} : {}", typeOfRest, e.getMessage());
            }

        }
        return (T) managers.get(typeOfRest);
    }

    public Optional<WeatherApi> getWeatherApi() {
        return Optional.ofNullable(getRestManager(WeatherApi.class));
    }

    public Optional<EnergyApi> getEnergyApi() {
        return Optional.ofNullable(getRestManager(EnergyApi.class));
    }

    public Optional<AircareApi> getAirCareApi() {
        return Optional.ofNullable(getRestManager(AircareApi.class));
    }

    public Optional<SecurityApi> getSecurityApi() {
        return Optional.ofNullable(getRestManager(SecurityApi.class));
    }

    public HomeApi getHomeApi() {
        HomeApi homeApi = (HomeApi) managers.get(HomeApi.class);
        if (homeApi == null) {
            homeApi = new HomeApi(this);
            managers.put(HomeApi.class, homeApi);
        }
        return homeApi;
    }

    synchronized <T> T executeUrl(String anUrl, HttpMethod method, @Nullable String payload, Class<T> classOfT,
            boolean baseUrl) throws NetatmoException {
        String url = anUrl.startsWith("http") ? anUrl
                : (baseUrl ? NetatmoConstants.NETATMO_BASE_URL : NetatmoConstants.NETATMO_APP_URL) + anUrl;
        try {
            logger.debug("executeUrl  {} {} ", method.toString(), url);

            final Request request = httpClient.newRequest(url).method(method).timeout(TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);

            httpHeaders.entrySet().forEach(entry -> request.header(entry.getKey(), entry.getValue()));

            if (payload != null && (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))) {
                InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                try (final InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(
                        stream)) {
                    request.content(inputStreamContentProvider, null);
                }
                if (!baseUrl) {
                    request.getHeaders().remove(HttpHeader.CONTENT_TYPE);
                    request.header(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8");
                }
            }

            ContentResponse response = request.send();

            int statusCode = response.getStatus();
            if (statusCode >= 200 && statusCode < 300) {
                String responseBody = new String(response.getContent(), StandardCharsets.UTF_8);
                T deserialized = deserialize(classOfT, responseBody);
                return deserialized;
            }

            switch (statusCode) {
                case HttpStatus.NOT_FOUND_404:
                    throw new NetatmoException(statusCode, "Target '" + response.getRequest().getURI()
                            + "' seems unavailable : " + response.getContentAsString());
                case HttpStatus.FORBIDDEN_403:
                case HttpStatus.UNAUTHORIZED_401:
                    throw new NetatmoException(statusCode,
                            "Authorization exception : " + response.getContentAsString());
                default:
                    throw new NetatmoException(statusCode, response.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new NetatmoException("Exception while calling " + anUrl, e);
        }
    }

    private <T> T deserialize(Class<T> classOfT, String serviceAnswer) throws NetatmoException {
        try {
            T deserialized = NETATMO_GSON.fromJson(serviceAnswer, classOfT);
            return deserialized;
        } catch (JsonSyntaxException e) {
            throw new NetatmoException(String.format("Unexpected error deserializing '%s'", serviceAnswer), e);
        }
    }

    public void onAccessTokenResponse(String accessToken, List<Scope> grantedScopes) {
        this.grantedScopes = grantedScopes;
        httpHeaders.put(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
    }

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
        listener.notifyStatusChange(connectionStatus);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }
}
