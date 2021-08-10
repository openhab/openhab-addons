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
    private static final String LOGIN_PATH = "login";
    private static final String AUTHORIZE_PATH = "authorize";
    private static final String LOGOUT_PATH = "logout";
    private static final String SESSION_PATH = "session";
    private static final Bundle BUNDLE = FrameworkUtil.getBundle(LoginManager.class);
    private static final String APP_ID = "org.openhab.binding.freebox"; // TODO : BUNDLE.getSymbolicName();

    private @Nullable Session session;

    public LoginManager(ApiHandler apiHandler) {
        super(apiHandler, LOGIN_PATH);
    }

    public String openSession(String appToken) throws FreeboxException {
        String challenge = get(null, ChallengeResponse.class, false).getChallenge();
        if (challenge != null) {
            OpenSessionData payload = new OpenSessionData(APP_ID, appToken, challenge);
            session = post(SESSION_PATH, payload, SessionResponse.class);
            String sessionToken = session.getSessionToken();
            if (sessionToken != null) {
                return sessionToken;
            }
            throw new FreeboxException("No session token provided.");
        }
        throw new FreeboxException("No challenge was provided.");
    }

    public void closeSession() throws FreeboxException {
        post(LOGOUT_PATH);
    }

    private Status trackAuthorize(int trackId) throws FreeboxException {
        if (trackId != 0) {
            return get(String.format("%s/%d", AUTHORIZE_PATH, trackId), ChallengeResponse.class, false).getStatus();
        }
        throw new FreeboxException("no trackId");
    }

    public String grant() throws FreeboxException {
        Authorize authorize = post(AUTHORIZE_PATH, new AuthorizeData(APP_ID, BUNDLE), AuthorizeResponse.class);
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
        return session != null ? Boolean.TRUE.equals(session.getPermissions().get(checked)) : false;
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
            return error != null ? error : getResult().getSessionToken() == null ? "No session token" : null;
        }
    }

    private static class AuthorizeResponse extends Response<Authorize> {
    }
}
