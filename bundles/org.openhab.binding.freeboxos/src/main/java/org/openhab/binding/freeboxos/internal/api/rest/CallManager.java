/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.THING_CALL;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link CallManager} is the Java class used to handle api requests related to calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CallManager extends RestManager {
    private static final String LOG_SUB_PATH = "log/";
    private static final String DELETE_ACTION = "delete_all";

    private static class Calls extends Response<Call> {
    }

    public enum Type {
        ACCEPTED,
        MISSED,
        OUTGOING,
        INCOMING,
        UNKNOWN
    }

    public static record Call(Type type, //
            ZonedDateTime datetime, // Call creation timestamp.
            String number, // Calling or called number
            int duration, // Call duration in seconds.
            String name) {

        public @Nullable String name() {
            return name.equals(number) ? null : name;
        }
    }

    public CallManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.CALLS, session.getUriBuilder().path(THING_CALL));
    }

    // Retrieves a sorted list of all call entries
    public List<Call> getCallEntries() throws FreeboxException {
        List<Call> callList = new ArrayList<>(
                get(Calls.class, LOG_SUB_PATH).stream().sorted(Comparator.comparing(Call::datetime)).toList());
        Call last = callList.get(callList.size() - 1);
        // The INCOMING type call can only be set on the last call if its duration is 0;
        if (last.type == Type.MISSED && last.duration == 0) {
            callList.remove(callList.size() - 1);
            callList.add(new Call(Type.INCOMING, last.datetime, last.number, 0, last.name));
        }
        return callList;
    }

    public void emptyQueue() throws FreeboxException {
        post(LOG_SUB_PATH, DELETE_ACTION);
    }
}
