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
import java.util.Properties;
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
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.netatmo.internal.api.aircare.AircareApi;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.api.energy.EnergyApi;
import org.openhab.binding.netatmo.internal.api.home.HomeApi;
import org.openhab.binding.netatmo.internal.api.partner.PartnerApi;
import org.openhab.binding.netatmo.internal.api.security.SecurityApi;
import org.openhab.binding.netatmo.internal.api.weather.WeatherApi;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.openhab.binding.netatmo.internal.utils.BindingUtils;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpUtil;
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

    protected final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(SERVICE_PID);
    private final Logger logger = LoggerFactory.getLogger(ApiBridge.class);
    private static final String AUTH_HEADER = "Authorization";
    private NetatmoBindingConfiguration configuration = new NetatmoBindingConfiguration();

    private Map<Class<? extends RestManager>, Object> managers = new HashMap<>();
    private final HttpClient httpClient;
    private ConnectionStatus connectionStatus = ConnectionStatus.Failed("No connection tried");
    private List<Scope> grantedScopes = List.of();
    private final ConnectApi connectApi;
    private final List<ConnectionListener> listeners = new ArrayList<>();

    public static final Properties HTTP_HEADERS;
    static {
        HTTP_HEADERS = new Properties();
        HTTP_HEADERS.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    }

    @Activate
    public ApiBridge(@Reference OAuthFactory oAuthFactory, @Reference HttpClientFactory httpClientFactory,
            ComponentContext componentContext) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.connectApi = new ConnectApi(this, oAuthFactory, configuration);
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
            getPartnerApi().getPartnerDevices();
            setConnectionStatus(ConnectionStatus.Success());
        } catch (NetatmoException e) {
            switch (e.getStatusCode()) {
                case 404: // If no partner station has been associated - likely to happen - we'll have this
                    // error but it means connection to API is OK
                    setConnectionStatus(ConnectionStatus.Success());
                    break;
                case 403: // Forbidden Access maybe too many requests ? Let's wait next cycle
                    scheduler.schedule(() -> this.testConnection(), configuration.reconnectInterval, TimeUnit.SECONDS);
                    break;
                default:
                    setConnectionStatus(ConnectionStatus
                            .Failed(String.format("Unable to connect Netatmo API : %s", e.getMessage())));
                    // notifyListeners(false, );
            }
        }
    }

    private void setConnectionStatus(ConnectionStatus status) {
        connectionStatus = status;
        if (!connectionStatus.isConnected()) {
            onAccessTokenResponse("", List.of());
        }
        listeners.forEach(listener -> listener.pushStatus(status));
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

    public PartnerApi getPartnerApi() {
        PartnerApi partnerApi = (PartnerApi) managers.get(PartnerApi.class);
        if (partnerApi == null) {
            partnerApi = new PartnerApi(this);
            managers.put(PartnerApi.class, partnerApi);
        }
        return partnerApi;
    }

    public HomeApi getHomeApi() {
        HomeApi homeApi = (HomeApi) managers.get(HomeApi.class);
        if (homeApi == null) {
            homeApi = new HomeApi(this);
            managers.put(HomeApi.class, homeApi);
        }
        return homeApi;
    }

    public <T> T post(String anUrl, @Nullable String payload, Class<T> classOfT, boolean baseUrl)
            throws NetatmoException {
        return execute(anUrl, "POST", payload, classOfT, baseUrl);
    }

    public <T> T get(String anUrl, Class<T> classOfT) throws NetatmoException {
        return execute(anUrl, "GET", null, classOfT, true);
    }

    public <T> T execute(String anUrl, String aMethod, @Nullable String aPayload, Class<T> classOfT, boolean baseUrl)
            throws NetatmoException {
        return executeUrl(anUrl, aMethod, aPayload, classOfT, baseUrl);
    }

    private synchronized <T> T executeUrl(String anUrl, String aMethod, @Nullable String payload, Class<T> classOfT,
            boolean baseUrl) throws NetatmoException {
        String url = anUrl.startsWith("http") ? anUrl
                : (baseUrl ? NetatmoConstants.NETATMO_BASE_URL : NetatmoConstants.NETATMO_APP_URL) + anUrl;
        try {
            logger.debug("executeUrl  {} {} ", aMethod, url);

            final HttpMethod method = HttpUtil.createHttpMethod(aMethod);
            final Request request = httpClient.newRequest(url).method(method).timeout(TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);

            for (String httpHeaderKey : HTTP_HEADERS.stringPropertyNames()) {
                request.header(httpHeaderKey, HTTP_HEADERS.getProperty(httpHeaderKey));
            }

            if (payload != null && (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))) {
                InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                try (final InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(
                        stream)) {
                    request.content(inputStreamContentProvider, null);
                }
                if (!baseUrl) {
                    request.getHeaders().remove("Content-Type");
                    request.header("Content-Type", "application/json;charset=utf-8");
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
                            + "' seems to be not available: " + response.getContentAsString());
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
        HTTP_HEADERS.setProperty(AUTH_HEADER, "Bearer " + accessToken);
    }

    public void setConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
        listener.pushStatus(connectionStatus);
    }
}
