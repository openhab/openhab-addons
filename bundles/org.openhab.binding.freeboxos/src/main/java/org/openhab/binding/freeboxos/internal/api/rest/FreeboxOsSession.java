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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.PermissionException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.Response.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.rest.LoginManager.Session;
import org.openhab.binding.freeboxos.internal.config.FreeboxOsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxOsSession} is responsible for sending requests toward a given url and transform the answer in
 * appropriate dto.
 *
 * @author Gaël L'Hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsSession {
    private static final String API_VERSION_PATH = "api_version";

    private final Logger logger = LoggerFactory.getLogger(FreeboxOsSession.class);

    // All access must be guarded by "this"
    private final Map<Class<? extends RestManager>, RestManager> restManagers = new HashMap<>();
    private final ApiHandler apiHandler;

    private final Object uriBuilderLock = new Object();

    // All access must be guarded by "uriBuilderLock"
    private @Nullable UriBuilder uriBuilder;

    // All access must be guarded by "this"
    private @Nullable Session session;

    // All access must be guarded by "this"
    private String appToken = "";
    private volatile int wsReconnectInterval;

    // All access must be guarded by "this"
    @Nullable
    private Boolean vmSupported;

    public enum BoxModel {
        FBXGW_R1_FULL, // Freebox Server (v6) revision 1
        FBXGW_R2_FULL, // Freebox Server (v6) revision 2
        FBXGW_R1_MINI, // Freebox Mini revision 1
        FBXGW_R2_MINI, // Freebox Mini revision 2
        FBXGW_R1_ONE, // Freebox One revision 1
        FBXGW_R2_ONE, // Freebox One revision 2
        FBXGW7_R1_FULL, // Freebox v7 revision 1
        UNKNOWN
    }

    public static record ApiVersion(String apiBaseUrl, @Nullable String apiDomain, String apiVersion, BoxModel boxModel,
            @Nullable String boxModelName, String deviceName, String deviceType, boolean httpsAvailable, int httpsPort,
            String uid) {

        /**
         * @return a string like eg: '/api/v8'
         */
        private String baseUrl() {
            return "%sv%s".formatted(apiBaseUrl, apiVersion.split("\\.")[0]);
        }
    }

    public FreeboxOsSession(ApiHandler apiHandler) {
        this.apiHandler = apiHandler;
    }

    public void initialize(FreeboxOsConfiguration config) throws FreeboxException, InterruptedException {
        ApiVersion version = apiHandler.executeUri(config.getUriBuilder(API_VERSION_PATH).build(), HttpMethod.GET,
                ApiVersion.class, null, null);
        synchronized (uriBuilderLock) {
            this.uriBuilder = config.getUriBuilder(version.baseUrl());
        }
        this.wsReconnectInterval = config.wsReconnectInterval;
        this.vmSupported = null;
        getManager(LoginManager.class);
        getManager(SystemManager.class);
        getManager(NetShareManager.class);
        getManager(LanManager.class);
        getManager(WifiManager.class);
        getManager(FreeplugManager.class);
        getManager(AirMediaManager.class);
    }

    public void openSession(String appToken) throws FreeboxException {
        Boolean vmSupported;
        Session session;
        synchronized (this) {
            session = this.session;
            if (session != null) {
                closeSession();
            }
            session = getManager(LoginManager.class).openSession(appToken);
            this.session = session;
            this.appToken = appToken;
            if (this.vmSupported == null) {
                vmSupported = getManager(SystemManager.class).getConfig().modelInfo().hasVm();
                this.vmSupported = vmSupported;
            } else {
                vmSupported = this.vmSupported;
            }
        }
        getManager(WebSocketManager.class).openSession(session.sessionToken(), wsReconnectInterval,
                Boolean.TRUE.equals(vmSupported));
    }

    public String grant() throws FreeboxException {
        return getManager(LoginManager.class).checkGrantStatus();
    }

    public void closeSession() {
        Session currentSession;
        WebSocketManager webSocketManager = null;
        LoginManager loginManager = null;
        synchronized (this) {
            currentSession = session;
            if (currentSession != null) {
                try {
                    webSocketManager = getManager(WebSocketManager.class);
                    loginManager = getManager(LoginManager.class);
                } catch (FreeboxException e) {
                    logger.warn("Error preparing to close session: {}", e.getMessage());
                    logger.trace("", e);
                }
            }
            session = null;
            appToken = "";
            restManagers.clear();
        }
        if (webSocketManager != null) {
            webSocketManager.dispose();
        }
        if (loginManager != null) {
            try {
                loginManager.closeSession();
            } catch (FreeboxException e) {
                if (e.getErrorCode() == ErrorCode.INVALID_SESSION) {
                    // just ignore, it's what we want to do
                } else {
                    logger.warn("Error closing session: {}", e.getMessage());
                    logger.trace("", e);
                }
            }
        }
    }

    private <F, T extends Response<F>> List<F> execute(URI uri, HttpMethod method, Class<T> clazz, boolean retryAuth,
            int retryCount, @Nullable Object aPayload) throws FreeboxException {
        try {
            T response = apiHandler.executeUri(uri, method, clazz, getSessionToken(), aPayload);
            if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
                return execute(uri, method, clazz, false, retryCount - 1, aPayload);
            } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTH_REQUIRED) {
                openSession(getAppToken());
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            if (!response.isSuccess()) {
                throw new FreeboxException("Api request failed: %s", response.getMsg());
            }
            return response.getResult();
        } catch (FreeboxException e) {
            if (ErrorCode.AUTH_REQUIRED.equals(e.getErrorCode())) {
                openSession(getAppToken());
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            throw e;
        } catch (InterruptedException ignored) {
            return List.of();
        }
    }

    public <F, T extends Response<F>> List<F> execute(URI uri, HttpMethod method, Class<T> clazz,
            @Nullable Object aPayload) throws FreeboxException {
        return execute(uri, method, clazz, getSessionToken() != null, 3, aPayload);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends RestManager> T getManager(Class<T> clazz) throws FreeboxException {
        RestManager manager = restManagers.get(clazz);
        if (manager == null) {
            try {
                Constructor<T> managerConstructor = clazz.getConstructor(FreeboxOsSession.class);
                manager = addManager(clazz, managerConstructor.newInstance(this));
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof PermissionException exception) {
                    throw exception;
                }
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            } catch (ReflectiveOperationException e) {
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            }
        }
        return (T) manager;
    }

    public synchronized String getAppToken() {
        return appToken;
    }

    public synchronized <T extends RestManager> T addManager(Class<T> clazz, T manager) {
        restManagers.put(clazz, manager);
        return manager;
    }

    boolean hasPermission(LoginManager.Permission required) {
        Session currentSession;
        synchronized (this) {
            currentSession = session;
        }
        return currentSession != null ? currentSession.hasPermission(required) : false;
    }

    private @Nullable String getSessionToken() {
        Session currentSession;
        synchronized (this) {
            currentSession = session;
        }
        return currentSession != null ? currentSession.sessionToken() : null;
    }

    /**
     * @return The {@link UriBuilder} instance.
     *
     * @throws IllegalStateException if the {@link UriBuilder} hasn't been instantiated.
     */
    public UriBuilder getUriBuilder() {
        UriBuilder uriBuilder;
        synchronized (uriBuilderLock) {
            uriBuilder = this.uriBuilder;
        }
        if (uriBuilder == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " hasn't been initialized yet");
        }
        return uriBuilder.clone();
    }

    public ApiHandler getApiHandler() {
        return apiHandler;
    }
}
