/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.repository;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.Session;
import org.openhab.binding.hive.internal.client.exception.*;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public interface SessionRepository {
    /**
     * Open a new session using a username and password.
     *
     * @param username
     *      The user's Hive username (email).
     *
     * @param password
     *      The user's Hive password.
     *
     * @return
     *      The the newly created Session.
     *
     * @throws HiveApiAuthenticationException
     *      If the provided username and password are not valid.
     *
     * @throws HiveApiUnknownException
     *      If the call to the Hive API fails for an unknown reason.
     *
     * @throws HiveClientResponseException
     *      If the call the the Hive API was a success but we do not understand
     *      the response.
     */
    Session createSession(String username, String password) throws HiveException;

    /**
     * Delete a session with a given ID.
     *
     * <p>
     *     N.B. This method does not seem to work how I expect.
     *     The API returns 200 but the session still works.
     * </p>
     *
     * @param session
     *      The session to delete.
     *
     * @throws HiveApiNotAuthorisedException
     *      If you are not authorised to delete the provided session.
     *
     * @throws HiveApiUnknownException
     *      If the call to the Hive API fails for an unknown reason.
     */
    void deleteSession(Session session) throws HiveException;

    /**
     * Checks if a session is valid.
     *
     * @param session
     *      The session to check.
     *
     * @return
     *      {@code true} if the session is valid or {@code false} if it is not.
     *
     * @throws HiveApiUnknownException
     *      If the call to the Hive API fails for an unknown reason.
     */
    boolean isValidSession(@Nullable Session session) throws HiveException;
}
