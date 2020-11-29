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
package org.openhab.binding.freeboxos.internal.api.phone;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.handler.ApiHandler;

/**
 * The {@link PhoneManager} is the Java class used to handle api requests
 * related to phone and calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneManager extends RestManager {
    public static Permission associatedPermission() {
        return Permission.CALLS;
    }

    public PhoneManager(ApiHandler apiHandler) {
        super(apiHandler);
    }

    public PhoneStatus getStatus() throws FreeboxException {
        return apiHandler.getList("phone/", PhoneStatusResponse.class, true).get(0);
    }

    public PhoneConfig getConfig() throws FreeboxException {
        return apiHandler.get("phone/config", PhoneConfigResponse.class, true);
    }

    public List<CallEntry> getCallEntries(long startTime) throws FreeboxException {
        return apiHandler.getList(String.format("call/log/?_dc=%d", startTime), CallEntriesResponse.class, true);
    }

    public void ring(boolean startIt) throws FreeboxException {
        apiHandler.post(String.format("phone/fxs_ring_%s", (startIt ? "start" : "stop")), null);
    }

    public void activateDect(boolean status) throws FreeboxException {
        PhoneConfig config = getConfig();
        config.setDectEnabled(status);
        apiHandler.put("phone/config", config, PhoneConfigResponse.class);
    }

    public void alternateRing(boolean status) throws FreeboxException {
        PhoneConfig config = getConfig();
        config.setDectRingOnOff(status);
        apiHandler.put("phone/config", config, PhoneConfigResponse.class);
    }

    // Response classes and validity evaluations
    private class CallEntriesResponse extends ListResponse<CallEntry> {
    }

    private class PhoneConfigResponse extends Response<PhoneConfig> {
    }

    private static class PhoneStatusResponse extends ListResponse<PhoneStatus> {
        @Override
        protected @Nullable String internalEvaluate() {
            String error = super.internalEvaluate();
            return error != null ? error : getResult().size() == 0 ? "No phone status in response" : null;
        }
    }
}
