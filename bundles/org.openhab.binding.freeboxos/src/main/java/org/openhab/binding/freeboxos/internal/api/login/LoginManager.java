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
package org.openhab.binding.freeboxos.internal.api.login;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.TokenStatus;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.RestManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The {@link LoginManager} is the Java class used to handle api requests related to session handling and login
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LoginManager extends RestManager {
    private static class AuthStatus extends Response<AuthorizationStatus> {
    }

    private static class AuthResponse extends Response<Authorization> {
    }

    private static class SessionResponse extends Response<Session> {
    }

    private static final Bundle BUNDLE = FrameworkUtil.getBundle(LoginManager.class);
    private static final String APP_ID = BUNDLE.getSymbolicName();

    public LoginManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, session.getUriBuilder().path(LOGIN_PATH));
    }

    public Session openSession(String appToken) throws FreeboxException {
        AuthorizationStatus authorization = getSingle(AuthStatus.class);
        OpenSessionData payload = new OpenSessionData(APP_ID, appToken, authorization.getChallenge());
        return postSingle(payload, SessionResponse.class, SESSION_PATH);
    }

    public void closeSession() throws FreeboxException {
        post(LOGOUT_PATH);
    }

    private TokenStatus trackAuthorize(int trackId) throws FreeboxException {
        return getSingle(AuthStatus.class, AUTHORIZE_PATH, Integer.toString(trackId)).getStatus();
    }

    public String grant() throws FreeboxException {
        Authorization authorize = postSingle(new AuthorizeData(APP_ID, BUNDLE), AuthResponse.class, AUTHORIZE_PATH);
        TokenStatus track = TokenStatus.PENDING;
        try {
            while (TokenStatus.PENDING.equals(track)) {
                Thread.sleep(2000);
                track = trackAuthorize(authorize.getTrackId());
            }
            if (TokenStatus.GRANTED.equals(track)) {
                return authorize.getAppToken();
            }
            throw new FreeboxException("Unable to grant session");
        } catch (InterruptedException e) {
            throw new FreeboxException(e, "Granting process interrupted");
        }
    }
}
