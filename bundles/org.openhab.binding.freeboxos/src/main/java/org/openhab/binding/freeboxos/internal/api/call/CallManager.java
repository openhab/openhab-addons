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
package org.openhab.binding.freeboxos.internal.api.call;

import java.time.ZonedDateTime;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

/**
 * The {@link CallManager} is the Java class used to handle api requests
 * related to phone and calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CallManager extends RestManager {
    public final static String CALL_PATH = "call";
    public final static String LOG_SUB_PATH = "log";
    private final UriBuilder logUriBuilder;

    public CallManager(FreeboxOsSession session) throws FreeboxException {
        super(CALL_PATH, session, Permission.CALLS);
        logUriBuilder = getUriBuilder().path(LOG_SUB_PATH);
    }

    public List<CallEntry> getCallEntries(ZonedDateTime startTime) throws FreeboxException {
        UriBuilder localBuilder = logUriBuilder.clone().queryParam("_dc", startTime.toInstant().toEpochMilli());
        return getList(CallEntriesResponse.class, localBuilder.build());
    }

    // Response classes
    private class CallEntriesResponse extends Response<List<CallEntry>> {
    }
}
