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
package org.openhab.binding.freeboxos.internal.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.freeboxos.internal.api.BaseResponse.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaManager;
import org.openhab.binding.freeboxos.internal.api.call.CallManager;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionManager;
import org.openhab.binding.freeboxos.internal.api.ftp.FtpManager;
import org.openhab.binding.freeboxos.internal.api.lan.LanManager;
import org.openhab.binding.freeboxos.internal.api.lcd.LcdManager;
import org.openhab.binding.freeboxos.internal.api.login.LoginManager;
import org.openhab.binding.freeboxos.internal.api.netshare.NetShareManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.player.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.repeater.RepeaterManager;
import org.openhab.binding.freeboxos.internal.api.system.SystemManager;
import org.openhab.binding.freeboxos.internal.api.upnpav.UPnPAVManager;
import org.openhab.binding.freeboxos.internal.api.vm.VmManager;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiManager;
import org.openhab.binding.freeboxos.internal.config.ApiConfiguration;
import org.openhab.binding.freeboxos.internal.handler.ApiBridgeHandler;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

@NonNullByDefault
public class ApiHandler {
    private final Logger logger = LoggerFactory.getLogger(ApiHandler.class);

    private final Map<String, String> httpHeaders = new HashMap<>(2);
    private static final String AUTH_HEADER = "X-Fbx-App-Auth";
    private static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final int DEFAULT_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(10);
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private @NonNullByDefault({}) UriBuilder uriBuilder;

    private Map<Class<? extends RestManager>, RestManager> managers = new HashMap<>();
    private final HttpClient httpClient;
    private @NonNullByDefault({}) ApiConfiguration configuration;
    private final ApiBridgeHandler apiBridgeHandler;

    public ApiHandler(ApiBridgeHandler apiBridgeHandler, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.apiBridgeHandler = apiBridgeHandler;
        this.httpHeaders.put("Content-Type", CONTENT_TYPE);
    }

    public void openConnection(ApiConfiguration configuration) {
        this.configuration = configuration;
        uriBuilder = UriBuilder.fromPath("api").scheme(configuration.httpsAvailable ? "https" : "http")
                .port(configuration.httpsAvailable ? configuration.httpsPort : 80).host(configuration.apiDomain)
                .path("v" + configuration.apiMajorVersion());
        getLoginManager();
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }

    public synchronized LoginManager getLoginManager() {
        LoginManager manager = (LoginManager) managers.get(LoginManager.class);
        if (manager == null) {
            manager = new LoginManager(this);
            try {
                String sessionToken = manager.openSession(configuration.appToken);
                httpHeaders.put(AUTH_HEADER, sessionToken);
                apiBridgeHandler.pushStatus(ThingStatusDetail.NONE, "");
            } catch (FreeboxException e) {
                BaseResponse response = e.getResponse();
                if (response != null && response.getErrorCode() == ErrorCode.INVALID_TOKEN) {
                    apiBridgeHandler.pushStatus(ThingStatusDetail.CONFIGURATION_PENDING,
                            "Please accept pairing request directly on your freebox");
                    try {
                        String appToken = getLoginManager().grant();
                        apiBridgeHandler.pushAppToken(appToken);
                    } catch (FreeboxException e1) {
                        apiBridgeHandler.pushStatus(ThingStatusDetail.CONFIGURATION_ERROR, e1.getMessage());
                    }
                } else {
                    apiBridgeHandler.pushStatus(ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                }
            }
            managers.put(LoginManager.class, manager);
        }
        return manager;
    }

    public synchronized SystemManager getSystemManager() {
        SystemManager manager = (SystemManager) managers.get(SystemManager.class);
        if (manager == null) {
            manager = new SystemManager(this);
            managers.put(SystemManager.class, manager);
        }
        return manager;
    }

    public synchronized ConnectionManager getConnectionManager() {
        ConnectionManager manager = (ConnectionManager) managers.get(ConnectionManager.class);
        if (manager == null) {
            manager = new ConnectionManager(this);
            managers.put(ConnectionManager.class, manager);
        }
        return manager;
    }

    public synchronized LanManager getLanManager() {
        LanManager manager = (LanManager) managers.get(LanManager.class);
        if (manager == null) {
            manager = new LanManager(this);
            managers.put(LanManager.class, manager);
        }
        return manager;
    }

    public synchronized LcdManager getLcdManager() {
        LcdManager manager = (LcdManager) managers.get(LcdManager.class);
        if (manager == null) {
            manager = new LcdManager(this);
            managers.put(LcdManager.class, manager);
        }
        return manager;
    }

    public synchronized AirMediaManager getAirMediaManager() {
        AirMediaManager manager = (AirMediaManager) managers.get(AirMediaManager.class);
        if (manager == null) {
            manager = new AirMediaManager(this);
            managers.put(AirMediaManager.class, manager);
        }
        return manager;
    }

    public synchronized WifiManager getWifiManager() {
        WifiManager manager = (WifiManager) managers.get(WifiManager.class);
        if (manager == null) {
            manager = new WifiManager(this);
            managers.put(WifiManager.class, manager);
        }
        return manager;
    }

    public synchronized FtpManager getFtpManager() {
        FtpManager manager = (FtpManager) managers.get(FtpManager.class);
        if (manager == null) {
            manager = new FtpManager(this);
            managers.put(FtpManager.class, manager);
        }
        return manager;
    }

    public synchronized UPnPAVManager getuPnPAVManager() {
        UPnPAVManager manager = (UPnPAVManager) managers.get(UPnPAVManager.class);
        if (manager == null) {
            manager = new UPnPAVManager(this);
            managers.put(UPnPAVManager.class, manager);
        }
        return manager;
    }

    public synchronized NetShareManager getNetShareManager() {
        NetShareManager manager = (NetShareManager) managers.get(NetShareManager.class);
        if (manager == null) {
            manager = new NetShareManager(this);
            managers.put(NetShareManager.class, manager);
        }
        return manager;
    }

    public synchronized RepeaterManager getRepeaterManager() {
        RepeaterManager manager = (RepeaterManager) managers.get(RepeaterManager.class);
        if (manager == null) {
            manager = new RepeaterManager(this);
            managers.put(RepeaterManager.class, manager);
        }
        return manager;
    }

    public synchronized CallManager getCallManager() {
        CallManager manager = (CallManager) managers.get(CallManager.class);
        if (manager == null) {
            manager = new CallManager(this);
            managers.put(CallManager.class, manager);
        }
        return manager;
    }

    public synchronized @Nullable PhoneManager getPhoneManager() {
        PhoneManager manager = (PhoneManager) managers.get(PhoneManager.class);
        if (manager == null && getLoginManager().hasPermission(PhoneManager.associatedPermission())) {
            manager = new PhoneManager(this);
            managers.put(PhoneManager.class, manager);
        }
        return manager;
    }

    public synchronized @Nullable VmManager getVmManager() {
        VmManager manager = (VmManager) managers.get(VmManager.class);
        if (manager == null && getLoginManager().hasPermission(VmManager.associatedPermission())) {
            manager = new VmManager(this);
            managers.put(VmManager.class, manager);
        }
        return manager;
    }

    public synchronized @Nullable PlayerManager getPlayerManager() {
        PlayerManager manager = (PlayerManager) managers.get(PlayerManager.class);
        if (manager == null && getLoginManager().hasPermission(PlayerManager.associatedPermission())) {
            manager = new PlayerManager(this, "/api/", configuration.apiMajorVersion());
            managers.put(PlayerManager.class, manager);
        }
        return manager;
    }

    @SuppressWarnings("unchecked")
    public <F, T extends Response<F>> F execute(URI url, HttpMethod method, @Nullable Object aPayload,
            @Nullable Class<T> classOfT, boolean retryAuth) throws FreeboxException {
        Object serialized = executeUrl(url, method, aPayload, classOfT, retryAuth, 3);
        if (classOfT != null) {
            return ((T) serialized).getResult();
        }
        return null;
    }

    <F, T extends ListResponse<F>> List<F> executeList(URI anUrl, HttpMethod method, @Nullable String aPayload,
            Class<T> classOfT, boolean retryAuth) throws FreeboxException {
        T serialized = executeUrl(anUrl, method, aPayload, classOfT, retryAuth, 3);
        return serialized.getResult();
    }

    private synchronized <T extends BaseResponse> T executeUrl(URI url, HttpMethod method, @Nullable Object aPayload,
            @Nullable Class<T> classOfT, boolean retryAuth, int retryCount) throws FreeboxException {
        logger.debug("executeUrl {} - {} ", method, url);
        try {
            Request request = httpClient.newRequest(url).method(method).timeout(DEFAULT_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            httpHeaders.entrySet().forEach(entry -> request.header(entry.getKey(), entry.getValue()));

            if (aPayload != null) {
                String payload = gson.toJson(aPayload);
                InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                try (final InputStreamContentProvider contentProvider = new InputStreamContentProvider(stream)) {
                    request.content(contentProvider, null);
                }
            }
            ContentResponse response = request.send();
            String responseBody = new String(response.getContent(), StandardCharsets.UTF_8);
            T serialized = deserialize(classOfT, responseBody);
            serialized.evaluate();
            return serialized;
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new FreeboxException("Exception while calling " + url, e);
        } catch (FreeboxException e) {
            BaseResponse response = e.getResponse();
            if (response != null) {
                if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
                    return executeUrl(url, method, aPayload, classOfT, retryAuth, retryCount - 1);
                } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTHORIZATION_REQUIRED) {
                    httpHeaders.put(AUTH_HEADER, getLoginManager().openSession(configuration.appToken));
                    return executeUrl(url, method, aPayload, classOfT, false, retryCount);
                }
            }
            throw e;
        }
    }

    private <T extends BaseResponse> T deserialize(@Nullable Class<T> classOfT, String serviceAnswer)
            throws FreeboxException {
        try {
            @Nullable
            T deserialized = gson.fromJson(serviceAnswer, classOfT != null ? classOfT : BaseResponse.class);
            if (deserialized != null) {
                return deserialized;
            }
            throw new FreeboxException("Deserialization lead to null object");
        } catch (JsonSyntaxException e) {
            if (classOfT != null) {
                BaseResponse serialized = gson.fromJson(serviceAnswer, BaseResponse.class);
                throw new FreeboxException("Taking care of unexpected answer from api", e, serialized);
            }
            throw new FreeboxException(String.format("Unexpected error desiralizing '%s'", serviceAnswer), e);
        }
    }

    public void closeSession() {
        try {
            if (httpHeaders.containsKey(AUTH_HEADER)) {
                getLoginManager().closeSession();
            }
        } catch (FreeboxException e) {
            logger.warn("Error closing session : {}", e);
        }
        httpHeaders.remove(AUTH_HEADER);
    }
}
