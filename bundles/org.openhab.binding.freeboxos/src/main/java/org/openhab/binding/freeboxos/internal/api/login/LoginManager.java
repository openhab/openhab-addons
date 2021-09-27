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
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.login.Challenge.Status;
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
    private static final String APP_ID = BUNDLE.getSymbolicName();

    public LoginManager(FreeboxOsSession session) {
        super(LOGIN_PATH, session);
    }

    public Session openSession(String appToken) throws FreeboxException {
        String challenge = get(ChallengeResponse.class).getChallenge();
        OpenSessionData payload = new OpenSessionData(APP_ID, appToken, challenge);
        return post(SessionResponse.class, SESSION_PATH, payload);
    }

    public void closeSession() throws FreeboxException {
        post(LOGOUT_PATH);
    }

    private Status trackAuthorize(int trackId) throws FreeboxException {
        if (trackId != 0) {
            return get(ChallengeResponse.class, String.format("%s/%d", AUTHORIZE_PATH, trackId)).getStatus();
        }
        throw new FreeboxException("no trackId");
    }

    public String grant() throws FreeboxException {
        Authorize authorize = post(AuthorizeResponse.class, AUTHORIZE_PATH, new AuthorizeData(APP_ID, BUNDLE));
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
            throw new FreeboxException(e, "Granting process interrupted");
        }
    }

    // Response classes
    private static class ChallengeResponse extends Response<Challenge> {
    }

    private static class SessionResponse extends Response<Session> {
    }

    private static class AuthorizeResponse extends Response<Authorize> {
    }
}
