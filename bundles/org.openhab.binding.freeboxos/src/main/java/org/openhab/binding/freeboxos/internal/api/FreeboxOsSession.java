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
import org.openhab.binding.freeboxos.internal.api.BaseResponse.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.login.LoginManager;
import org.openhab.binding.freeboxos.internal.api.login.Session;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.config.ApiConfiguration;

/**
 * The {@link FreeboxOsSession} is responsible for sending requests toward
 * a given url and transform the answer in appropriate dto.
 *
 * @author Gaël L'Hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsSession {
    private final Map<Class<? extends RestManager>, RestManager> restManagers = new HashMap<>();
    private final ApiHandler apiHandler;

    private @NonNullByDefault({}) UriBuilder uriBuilder;
    private @Nullable String appToken;
    private @Nullable Session session;

    public FreeboxOsSession(ApiHandler apiHandler) {
        this.apiHandler = apiHandler;
    }

    /**
     * @param configuration
     * @return the app token used to open the session (can have changed if newly granted)
     * @throws FreeboxException
     */
    public String initialize(ApiConfiguration configuration) throws FreeboxException {
        UriBuilder uriBuilder = UriBuilder.fromPath("/").scheme(configuration.getScheme()).port(configuration.getPort())
                .host(configuration.apiDomain);
        ApiVersion version = apiHandler.executeUri(uriBuilder.clone().path("api_version").build(), HttpMethod.GET, null,
                configuration, ApiVersion.class);
        this.appToken = configuration.appToken;
        this.uriBuilder = uriBuilder.path(version.baseUrl());
        return initiateConnection();
    }

    public String initiateConnection() throws FreeboxException {
        try {
            String localToken = appToken;
            if (localToken == null) {
                throw new FreeboxException(BaseResponse.of(ErrorCode.INVALID_TOKEN), null);
            } else {
                session = getManager(LoginManager.class).openSession(localToken);
                return localToken;
            }
        } catch (FreeboxException e) {
            BaseResponse response = e.getResponse();
            if (response != null && response.getErrorCode() == ErrorCode.INVALID_TOKEN) {
                appToken = getManager(LoginManager.class).grant();
                return initiateConnection();
            }
            throw e;
        }
    }

    public void closeSession() throws FreeboxException {
        if (session != null) {
            getManager(LoginManager.class).closeSession();
        }
        session = null;
        appToken = null;
        restManagers.clear();
    }

    <F, T extends Response<F>> F execute(URI url, HttpMethod method, boolean retryAuth, int retryCount,
            @Nullable Class<T> classOfT, @Nullable Object aPayload) throws FreeboxException {
        try {
            T serialized = apiHandler.executeUri(url, method, session != null ? session.getSessionToken() : null,
                    aPayload, classOfT);
            return classOfT != null ? serialized.getResult() : null;
        } catch (FreeboxException e) {
            BaseResponse response = e.getResponse();
            if (response != null) {
                if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
                    return execute(url, method, false, retryCount - 1, classOfT, aPayload);
                } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTHORIZATION_REQUIRED) {
                    initiateConnection();
                    return execute(url, method, false, retryCount, classOfT, aPayload);
                }
            }
            throw e;
        }
    }

    <F, T extends ListResponse<F>> List<F> executeList(URI url, HttpMethod method, boolean retryAuth, int retryCount,
            Class<T> classOfT, @Nullable String aPayload) throws FreeboxException {
        try {
            T serialized = apiHandler.executeUri(url, method, session != null ? session.getSessionToken() : null,
                    aPayload, classOfT);
            return serialized.getResult();
        } catch (FreeboxException e) {
            BaseResponse response = e.getResponse();
            if (response != null) {
                if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
                    return executeList(url, method, false, retryCount - 1, classOfT, aPayload);
                } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTHORIZATION_REQUIRED) {
                    initiateConnection();
                    return executeList(url, method, false, retryCount, classOfT, aPayload);
                }
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends RestManager> T getManager(Class<T> classOfT) throws FreeboxException {
        RestManager manager = restManagers.get(classOfT);
        if (manager == null) {
            try {
                Constructor<T> managerConstructor = classOfT.getConstructor(FreeboxOsSession.class);
                manager = managerConstructor.newInstance(this);
                restManagers.put(classOfT, manager);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new FreeboxException("Unable to call RestManager constructor", e);
            }
        }
        return (T) manager;
    }

    public boolean hasPermission(Permission required) {
        return session != null && session.hasPermission(required);
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }

    public ApiHandler getApiHandler() {
        return apiHandler;
    }
}
