/**
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
package org.openhab.binding.freeboxos.internal.api.login;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.login.Authorize.AuthorizeResponse;
import org.openhab.binding.freeboxos.internal.api.login.Challenge.ChallengeResponse;
import org.openhab.binding.freeboxos.internal.api.login.Challenge.Status;
import org.openhab.binding.freeboxos.internal.api.login.Session.SessionResponse;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The {@link LoginManager} is the Java class used to handle api requests related to session handling and login
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
        super(session, LOGIN_PATH);
    }

    public Session openSession(String appToken) throws FreeboxException {
        Challenge challengeResponse = get(ChallengeResponse.class);
        if (challengeResponse != null) {
            String challenge = challengeResponse.getChallenge();
            if (challenge != null) {
                OpenSessionData payload = new OpenSessionData(APP_ID, appToken, challenge);
                Session result = post(SessionResponse.class, payload, SESSION_PATH);
                if (result != null) {
                    return result;
                } else {
                    throw new FreeboxException("result should not be null in openSession");
                }
            } else {
                throw new FreeboxException("Challenge should not be null in openSession");
            }
        } else {
            throw new FreeboxException("ChallengeResponse should not be null in openSession");
        }
    }

    public void closeSession() throws FreeboxException {
        post(LOGOUT_PATH);
    }

    private Status trackAuthorize(int trackId) throws FreeboxException {
        Challenge challengeResponse = get(ChallengeResponse.class, AUTHORIZE_PATH, Integer.toString(trackId));
        if (challengeResponse != null) {
            return challengeResponse.getStatus();
        }
        throw new FreeboxException("Challenge value should not be null in trackAuthorize");
    }

    public String grant() throws FreeboxException {
        Authorize authorize = post(AuthorizeResponse.class, new AuthorizeData(APP_ID, BUNDLE), AUTHORIZE_PATH);
        if (authorize != null) {
            Status track = Status.PENDING;
            try {
                while (Status.PENDING.equals(track)) {
                    Thread.sleep(2000);
                    track = trackAuthorize(authorize.getTrackId());
                }
                if (Status.GRANTED.equals(track)) {
                    String appToken = authorize.getAppToken();
                    if (appToken != null) {
                        return appToken;
                    }
                    throw new FreeboxException("Empty appToken");
                }
                throw new FreeboxException("Unable to grant session");
            } catch (InterruptedException e) {
                throw new FreeboxException(e, "Granting process interrupted");
            }
        }
        throw new FreeboxException("AuthorizeResponse value should not be null");
    }
}
