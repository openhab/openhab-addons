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
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.freeboxos.internal.api.Response.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.login.LoginManager;
import org.openhab.binding.freeboxos.internal.api.login.Session;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.config.FreeboxOsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxOsSession} is responsible for sending requests toward
 * a given url and transform the answer in appropriate dto.
 *
 * @author Gaël L'Hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsSession {
    private final Logger logger = LoggerFactory.getLogger(FreeboxOsSession.class);
    private final Map<Class<? extends RestManager>, RestManager> restManagers = new HashMap<>();

    private final ApiHandler apiHandler;
    private final Validator validator;

    private @NonNullByDefault({}) UriBuilder uriBuilder;
    private @Nullable String appToken;
    private @Nullable Session session;

    public FreeboxOsSession(ApiHandler apiHandler, Validator validator) {
        this.apiHandler = apiHandler;
        this.validator = validator;
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
                ApiVersion.class, null, configuration);
        this.appToken = configuration.appToken;
        this.uriBuilder = uriBuilder.path(version.baseUrl());
        return initiateConnection();
    }

    private String initiateConnection() throws FreeboxException {
        try {
            String localToken = appToken;
            if (localToken != null) {
                session = getManager(LoginManager.class).openSession(localToken);
                return localToken;
            }
            throw new FreeboxException(null, null, Response.of(ErrorCode.INVALID_TOKEN));
        } catch (FreeboxException e) {
            Response<?> response = e.getResponse();
            if (response != null && response.getErrorCode() == ErrorCode.INVALID_TOKEN) {
                appToken = getManager(LoginManager.class).grant();
                return initiateConnection();
            }
            throw e;
        }
    }

    public void closeSession() {
        if (session != null) {
            try {
                getManager(LoginManager.class).closeSession();
            } catch (FreeboxException e) {
                logger.info("Error closing session : {}", e.getMessage());
            }
            session = null;
        }
        appToken = null;
        restManagers.clear();
    }

    private @Nullable String sessionToken() {
        return session != null ? session.getSessionToken() : null;
    }

    private <F, T extends Response<F>> F execute(URI uri, HttpMethod method, Class<T> classOfT, boolean retryAuth,
            int retryCount, @Nullable Object aPayload) throws FreeboxException {
        T response = apiHandler.executeUri(uri, method, classOfT, sessionToken(), aPayload);
        if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
            return execute(uri, method, classOfT, false, retryCount - 1, aPayload);
        } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTHORIZATION_REQUIRED) {
            initiateConnection();
            return execute(uri, method, classOfT, false, retryCount, aPayload);
        }

        Set<ConstraintViolation<Response<?>>> constraintViolations = validator.validate(response);
        if (constraintViolations.size() > 0) {
            ConstraintViolation<Response<?>> violation = constraintViolations.iterator().next();
            throw new FreeboxException(violation.getMessage() + " on request : " + uri.toString(), null, response);
        }
        return response.getResult();
    }

    <F, T extends Response<F>> F execute(URI uri, HttpMethod method, Class<T> classOfT, @Nullable Object aPayload)
            throws FreeboxException {
        boolean retryAuth = sessionToken() != null;
        return execute(uri, method, classOfT, retryAuth, 3, aPayload);
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
                throw new FreeboxException(e, "Unable to call RestManager constructor");
            }
        }
        return (T) manager;
    }

    boolean hasPermission(Permission required) {
        return session != null && session.hasPermission(required);
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }
}
