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
package org.openhab.binding.freeboxos.internal.api.login;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.login.Challenge.Status;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The {@link LoginManager} is the Java class used to handle api requests
 * related to session handling and login
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LoginManager extends RestManager {
    private static Bundle BUNDLE = FrameworkUtil.getBundle(LoginManager.class);
    private static String APP_NAME = BUNDLE.getHeaders().get("Bundle-Name");
    private static String DEVICE_NAME = BUNDLE.getHeaders().get("Bundle-Vendor");
    private static String APP_VERSION = BUNDLE.getVersion().toString();
    private static String APP_ID = "org.openhab.binding.freebox"; // TODO : BUNDLE.getSymbolicName();
    private Map<Permission, @Nullable Boolean> permissions = new HashMap<>();

    public LoginManager(ApiHandler apiHandler) {
        super(apiHandler, "login");
    }

    public String openSession(String appToken) throws FreeboxException {
        String challenge = get(null, ChallengeResponse.class, false).getChallenge();
        if (challenge != null) {
            OpenSessionData payload = new OpenSessionData(APP_ID, appToken, challenge);
            Session session = post("session", payload, SessionResponse.class);
            permissions.clear();
            permissions.putAll(session.getPermissions());
            return session.getSessionToken();
        }
        throw new FreeboxException("No challenge was provided ?!?");
    }

    public void closeSession() throws FreeboxException {
        post("logout", null);
    }

    private Status trackAuthorize(int trackId) throws FreeboxException {
        if (trackId != 0) {
            return get("authorize/" + trackId, ChallengeResponse.class, false).getStatus();
        }
        throw new FreeboxException("no trackId");
    }

    public String grant() throws FreeboxException {
        AuthorizeData payload = new AuthorizeData(APP_ID, APP_NAME, APP_VERSION, DEVICE_NAME);
        Authorize authorize = post("authorize", payload, AuthorizeResponse.class);
        Status track = Status.PENDING;
        try {
            while (track == Status.PENDING) {
                Thread.sleep(2000);
                track = trackAuthorize(authorize.getTrackId());
            }
            if (track == Status.GRANTED) {
                return authorize.getAppToken();
            }
            throw new FreeboxException("Unable to grant session");
        } catch (InterruptedException e) {
            throw new FreeboxException("Granting process interrupted", e);
        }
    }

    public boolean hasPermission(Permission checked) {
        return Boolean.TRUE.equals(permissions.get(checked));
    }

    // Response classes and validity evaluations
    private static class ChallengeResponse extends Response<Challenge> {
        @Override
        protected @Nullable String internalEvaluate() {
            String error = super.internalEvaluate();
            return error != null ? error : getResult().getChallenge() == null ? "No challenge in response" : null;
        }
    }

    private static class SessionResponse extends Response<Session> {
        @Override
        protected @Nullable String internalEvaluate() {
            String error = super.internalEvaluate();
            return error != null ? error : getResult().getSessionToken().isEmpty() ? "No session token" : null;
        }
    }

    private static class AuthorizeResponse extends Response<Authorize> {
    }
}
