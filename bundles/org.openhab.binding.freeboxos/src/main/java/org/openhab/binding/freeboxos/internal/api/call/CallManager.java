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
package org.openhab.binding.freeboxos.internal.api.call;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.RestManager;

/**
 * The {@link CallManager} is the Java class used to handle api requests related to calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CallManager extends RestManager {
    public class EntriesResponse extends Response<List<CallEntry>> {
    }

    public CallManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.CALLS, CALL_PATH);
    }

    // Retrieves a sorted list of all call entries
    public Stream<CallEntry> getCallEntries() throws FreeboxException {
        return getList(EntriesResponse.class, CALL_LOG_SUB_PATH).stream()
                .sorted(Comparator.comparing(CallEntry::getDatetime));
    }

    public void emptyQueue() throws FreeboxException {
        post(CALL_LOG_SUB_PATH, DELETE_ALL);
    }
}
