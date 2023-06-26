/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
    private final Map<Class<? extends RestManager>, RestManager> restManagers = new HashMap<>();
    private final ApiHandler apiHandler;

    private @NonNullByDefault({}) UriBuilder uriBuilder;
    private @Nullable Session session;
    private String appToken = "";

    public static enum BoxModel {
        FBXGW_R1_FULL, // Freebox Server (v6) revision 1
        FBXGW_R2_FULL, // Freebox Server (v6) revision 2
        FBXGW_R1_MINI, // Freebox Mini revision 1
        FBXGW_R2_MINI, // Freebox Mini revision 2
        FBXGW_R1_ONE, // Freebox One revision 1
        FBXGW_R2_ONE, // Freebox One revision 2
        FBXGW7_R1_FULL, // Freebox v7 revision 1
        UNKNOWN;
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

    /**
     * @param config
     * @return the app token used to open the session (can have changed if newly granted)
     * @throws FreeboxException
     * @throws InterruptedException
     */
    public String initialize(FreeboxOsConfiguration config) throws FreeboxException, InterruptedException {
        ApiVersion version = apiHandler.executeUri(config.getUriBuilder(API_VERSION_PATH).build(), HttpMethod.GET,
                ApiVersion.class, null, null);
        this.uriBuilder = config.getUriBuilder(version.baseUrl());
        this.appToken = config.appToken;
        String result = initiateConnection();

        getManager(NetShareManager.class);
        getManager(LanManager.class);
        getManager(WifiManager.class);
        getManager(FreeplugManager.class);
        getManager(AirMediaManager.class);

        return result;
    }

    private String initiateConnection() throws FreeboxException {
        try {
            if (!appToken.isBlank()) {
                Session newSession = getManager(LoginManager.class).openSession(appToken);
                getManager(WebSocketManager.class).openSession(newSession.sessionToken());
                session = newSession;
                return appToken;
            }
            throw new FreeboxException(ErrorCode.INVALID_TOKEN, "Token is invalid");
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
                logger.warn("Error closing session: {}", e.getMessage());
            }
        }
        appToken = "";
        restManagers.clear();
    }

    private synchronized <F, T extends Response<F>> List<F> execute(URI uri, HttpMethod method, Class<T> clazz,
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
                throw new FreeboxException("Api request failed: %s", response.getMsg());
            }
            return response.getResult();
        } catch (FreeboxException e) {
            if (ErrorCode.AUTH_REQUIRED.equals(e.getErrorCode())) {
                initiateConnection();
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
                if (cause instanceof PermissionException) {
                    throw (PermissionException) cause;
                }
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            } catch (ReflectiveOperationException e) {
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            }
        }
        return (T) manager;
    }

    public <T extends RestManager> T addManager(Class<T> clazz, T manager) {
        restManagers.put(clazz, manager);
        return manager;
    }

    boolean hasPermission(LoginManager.Permission required) {
        Session currentSession = session;
        return currentSession != null ? currentSession.hasPermission(required) : false;
    }

    private @Nullable String getSessionToken() {
        Session currentSession = session;
        return currentSession != null ? currentSession.sessionToken() : null;
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }

    public ApiHandler getApiHandler() {
        return apiHandler;
=======
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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.MissingPermissionException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.Response.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.lan.LanManager;
import org.openhab.binding.freeboxos.internal.api.login.LoginManager;
import org.openhab.binding.freeboxos.internal.api.login.Session;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.netshare.NetShareManager;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiManager;
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
    private String appToken = "";
    private Optional<Session> session = Optional.empty();

    public FreeboxOsSession(ApiHandler apiHandler) {
        this.apiHandler = apiHandler;
    }

    /**
     * @param configuration
     * @return the app token used to open the session (can have changed if newly granted)
     * @throws FreeboxException
     */
    public String initialize(FreeboxOsConfiguration configuration) throws FreeboxException {
        UriBuilder uriBuilder = UriBuilder.fromPath("/").scheme(configuration.getScheme()).port(configuration.getPort())
                .host(configuration.apiDomain);
        ApiVersion version = apiHandler.executeUri(uriBuilder.clone().path("api_version").build(), HttpMethod.GET,
                ApiVersion.class, null, null);
        this.appToken = configuration.appToken;
        this.uriBuilder = uriBuilder.path(version.baseUrl());
        return initiateConnection();
    }

    private String initiateConnection() throws FreeboxException {
        try {
            if (!appToken.isBlank()) {
                session = Optional.of(getManager(LoginManager.class).openSession(appToken));
                getManager(NetShareManager.class);
                getManager(LanManager.class);
                getManager(WifiManager.class);
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
        session.ifPresent(s -> {
            try {
                getManager(LoginManager.class).closeSession();
            } catch (FreeboxException e) {
                logger.info("Error closing session : {}", e.getMessage());
            }
        });
        session = Optional.empty();
        appToken = "";
        restManagers.clear();
    }

    private @Nullable String sessionToken() {
        return session.map(Session::getSessionToken).orElse(null);
    }

    private synchronized <F, T extends Response<F>> @Nullable F execute(URI uri, HttpMethod method, Class<T> clazz,
            boolean retryAuth, int retryCount, @Nullable Object aPayload) throws FreeboxException {
        try {
            T response = apiHandler.executeUri(uri, method, clazz, sessionToken(), aPayload);
            if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
                return execute(uri, method, clazz, false, retryCount - 1, aPayload);
            } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTHORIZATION_REQUIRED) {
                initiateConnection();
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            if (!response.isSuccess()) {
                throw new FreeboxException("Api request failed : %s", response.getMsg());
            }
            return response.getResult();
        } catch (FreeboxException e) {
            if (ErrorCode.AUTHORIZATION_REQUIRED.equals(e.getErrorCode())) {
                initiateConnection();
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            throw e;
        }
    }

    public <F, T extends Response<F>> @Nullable F execute(URI uri, HttpMethod method, Class<T> clazz,
            @Nullable Object aPayload) throws FreeboxException {
        boolean retryAuth = sessionToken() != null;
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
        return session.map(s -> s.hasPermission(required)).orElse(false);
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder.clone();
>>>>>>> 46dadb1 SAT warnings handling
=======
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
    private final Map<Class<? extends RestManager>, RestManager> restManagers = new HashMap<>();
    private final ApiHandler apiHandler;

    private @NonNullByDefault({}) UriBuilder uriBuilder;
    private @Nullable Session session;
    private String appToken = "";

    public static enum BoxModel {
        FBXGW_R1_FULL, // Freebox Server (v6) revision 1
        FBXGW_R2_FULL, // Freebox Server (v6) revision 2
        FBXGW_R1_MINI, // Freebox Mini revision 1
        FBXGW_R2_MINI, // Freebox Mini revision 2
        FBXGW_R1_ONE, // Freebox One revision 1
        FBXGW_R2_ONE, // Freebox One revision 2
        FBXGW7_R1_FULL, // Freebox v7 revision 1
        UNKNOWN;
    }

    public static record ApiVersion(String apiBaseUrl, @Nullable String apiDomain, String apiVersion, BoxModel boxModel,
            @Nullable String boxModelName, String deviceName, String deviceType, boolean httpsAvailable, int httpsPort,
            String uid) {

        /**
         * @return a string like eg : '/api/v8'
         */
        private String baseUrl() {
            return "%sv%s".formatted(apiBaseUrl, apiVersion.split("\\.")[0]);
        }
    }

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

        getManager(NetShareManager.class);
        getManager(LanManager.class);
        getManager(WifiManager.class);
        getManager(FreeplugManager.class);
        getManager(AirMediaManager.class);

        return result;
    }

    private String initiateConnection() throws FreeboxException {
        try {
            if (!appToken.isBlank()) {
                Session newSession = getManager(LoginManager.class).openSession(appToken);
                getManager(WebSocketManager.class).openSession(newSession.sessionToken());
                session = newSession;
                return appToken;
            }
            throw new FreeboxException(ErrorCode.INVALID_TOKEN, "Token is invalid");
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

    private synchronized <F, T extends Response<F>> List<F> execute(URI uri, HttpMethod method, Class<T> clazz,
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
            List<F> result = response.getResult();
            return result == null ? List.of() : result;
        } catch (FreeboxException e) {
            if (ErrorCode.AUTH_REQUIRED.equals(e.getErrorCode())) {
                initiateConnection();
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            throw e;
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
                if (cause instanceof PermissionException) {
                    throw (PermissionException) cause;
                }
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            } catch (ReflectiveOperationException e) {
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            }
        }
        return (T) manager;
    }

    public <T extends RestManager> T addManager(Class<T> clazz, T manager) {
        restManagers.put(clazz, manager);
        return manager;
    }

    boolean hasPermission(LoginManager.Permission required) {
        Session currentSession = session;
        return currentSession != null ? currentSession.hasPermission(required) : false;
    }

    private @Nullable String getSessionToken() {
        Session currentSession = session;
        return currentSession != null ? currentSession.sessionToken() : null;
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }

    public ApiHandler getApiHandler() {
        return apiHandler;
>>>>>>> e4ef5cc Switching to Java 17 records
    }
}
