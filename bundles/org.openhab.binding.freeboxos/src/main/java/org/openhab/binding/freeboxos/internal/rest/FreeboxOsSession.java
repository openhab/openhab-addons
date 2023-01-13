/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.rest;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.API_VERSION_PATH;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.MissingPermissionException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaManager;
import org.openhab.binding.freeboxos.internal.api.freeplug.FreeplugManager;
import org.openhab.binding.freeboxos.internal.api.lan.LanManager;
import org.openhab.binding.freeboxos.internal.api.login.ApiVersion;
import org.openhab.binding.freeboxos.internal.api.login.LoginManager;
import org.openhab.binding.freeboxos.internal.api.login.Session;
import org.openhab.binding.freeboxos.internal.api.netshare.NetShareManager;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiManager;
import org.openhab.binding.freeboxos.internal.api.ws.WebSocketManager;
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
    private final Logger logger = LoggerFactory.getLogger(FreeboxOsSession.class);
    private final Map<Class<? extends RestManager>, RestManager> restManagers = new HashMap<>();
    private final ApiHandler apiHandler;

    private @NonNullByDefault({}) UriBuilder uriBuilder;
    private @Nullable Session session;
    private String appToken = "";

    public FreeboxOsSession(ApiHandler apiHandler) {
        this.apiHandler = apiHandler;
    }

    /**
     * @param config
     * @return the app token used to open the session (can have changed if newly granted)
     * @throws FreeboxException
     */
    public String initialize(FreeboxOsConfiguration config) throws FreeboxException {
        ApiVersion version = apiHandler.executeUri(config.getUriBuilder(API_VERSION_PATH).build(), HttpMethod.GET,
                ApiVersion.class, null, null);
        this.uriBuilder = config.getUriBuilder(version.baseUrl());
        this.appToken = config.appToken;
        String result = initiateConnection();
        loadBasicManagers();
        return result;
    }

    private String initiateConnection() throws FreeboxException {
        try {
            if (!appToken.isBlank()) {
                Session newSession = getManager(LoginManager.class).openSession(appToken);
                getManager(WebSocketManager.class).openSession(newSession.getSessionToken());
                session = newSession;
                return appToken;
            }
            throw new FreeboxException(ErrorCode.INVALID_TOKEN);
        } catch (FreeboxException e) {
            if (ErrorCode.INVALID_TOKEN.equals(e.getErrorCode())) {
                appToken = getManager(LoginManager.class).grant();
                return initiateConnection();
            }
            throw e;
        }
    }

    public void closeSession() {
        Session currentSession = session;
        if (currentSession != null) {
            try {
                getManager(WebSocketManager.class).closeSession();
                getManager(LoginManager.class).closeSession();
                session = null;
            } catch (FreeboxException e) {
                logger.info("Error closing session : {}", e.getMessage());
            }
        }
        appToken = "";
        restManagers.clear();
    }

    private void loadBasicManagers() throws FreeboxException {
        getManager(NetShareManager.class);
        getManager(LanManager.class);
        getManager(WifiManager.class);
        getManager(FreeplugManager.class);
        getManager(AirMediaManager.class);
    }

    private synchronized <F, T extends Response<F>> @Nullable F execute(URI uri, HttpMethod method, Class<T> clazz,
            boolean retryAuth, int retryCount, @Nullable Object aPayload) throws FreeboxException {
        try {
            T response = apiHandler.executeUri(uri, method, clazz, getSessionToken(), aPayload);
            if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
                return execute(uri, method, clazz, false, retryCount - 1, aPayload);
            } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTH_REQUIRED) {
                initiateConnection();
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            if (!response.isSuccess()) {
                throw new FreeboxException("Api request failed : %s", response.getMsg());
            }
            return response.getResult();
        } catch (FreeboxException e) {
            if (ErrorCode.AUTH_REQUIRED.equals(e.getErrorCode())) {
                initiateConnection();
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            throw e;
        }
    }

    public <F, T extends Response<F>> @Nullable F execute(URI uri, HttpMethod method, Class<T> clazz,
            @Nullable Object aPayload) throws FreeboxException {
        boolean retryAuth = getSessionToken() != null;
        return execute(uri, method, clazz, retryAuth, 3, aPayload);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends RestManager> T getManager(Class<T> clazz) throws FreeboxException {
        RestManager manager = restManagers.get(clazz);
        if (manager == null) {
            try {
                Constructor<T> managerConstructor = clazz.getConstructor(FreeboxOsSession.class);
                manager = managerConstructor.newInstance(this);
                restManagers.put(clazz, manager);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof MissingPermissionException) {
                    throw (MissingPermissionException) cause;
                }
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            } catch (SecurityException | ReflectiveOperationException e) {
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            }
        }
        return (T) manager;
    }

    public <T extends RestManager> void addManager(Class<T> clazz, RestManager manager) {
        restManagers.put(clazz, manager);
    }

    boolean hasPermission(Permission required) {
        Session currentSession = session;
        return currentSession != null ? currentSession.hasPermission(required) : false;
    }

    private @Nullable String getSessionToken() {
        Session currentSession = session;
        return currentSession != null ? currentSession.getSessionToken() : null;
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }

    public ApiHandler getApiHandler() {
        return apiHandler;
    }
}
