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
package org.openhab.binding.freeboxos.internal.api.call;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.call.CallEntry.CallEntriesResponse;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;

/**
 * The {@link CallManager} is the Java class used to handle api requests
 * related to calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CallManager extends RestManager {
    public static final String CALL_PATH = "call";
    public static final String LOG_SUB_PATH = "log/";

    public CallManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.CALLS, CALL_PATH);
    }

    public List<CallEntry> getCallEntries() throws FreeboxException {
        return getList(CallEntriesResponse.class, LOG_SUB_PATH);
    }
}
