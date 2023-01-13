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
import org.openhab.binding.freeboxos.internal.api.login.LoginResponses.AuthorizeResponse;
import org.openhab.binding.freeboxos.internal.api.login.LoginResponses.ChallengeResponse;
import org.openhab.binding.freeboxos.internal.api.login.LoginResponses.SessionResponse;
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
    private static final Bundle BUNDLE = FrameworkUtil.getBundle(LoginManager.class);
    private static final String APP_ID = BUNDLE.getSymbolicName();

    public LoginManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, LOGIN_PATH);
    }

    public Session openSession(String appToken) throws FreeboxException {
        AuthorizationStatus challengeResponse = get(LoginResponses.ChallengeResponse.class);
        if (challengeResponse != null) {
            String challenge = challengeResponse.getChallenge();
            OpenSessionData payload = new OpenSessionData(APP_ID, appToken, challenge);
            Session result = post(SessionResponse.class, payload, SESSION_PATH);
            if (result != null) {
                return result;
            }
            throw new FreeboxException("result should not be null in openSession");
        }
        throw new FreeboxException("ChallengeResponse should not be null in openSession");
    }

    public void closeSession() throws FreeboxException {
        post(LOGOUT_PATH);
    }

    private TokenStatus trackAuthorize(int trackId) throws FreeboxException {
        AuthorizationStatus challengeResponse = get(ChallengeResponse.class, AUTHORIZE_PATH, Integer.toString(trackId));
        if (challengeResponse != null) {
            return challengeResponse.getStatus();
        }
        throw new FreeboxException("Challenge value should not be null in trackAuthorize");
    }

    public String grant() throws FreeboxException {
        Authorization authorize = post(AuthorizeResponse.class, new AuthorizeData(APP_ID, BUNDLE), AUTHORIZE_PATH);
        if (authorize != null) {
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
        throw new FreeboxException("AuthorizeResponse value should not be null");
    }
}
