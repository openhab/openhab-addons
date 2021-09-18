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
package org.openhab.binding.freeboxos.internal.api.phone;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

/**
 * The {@link PhoneManager} is the Java class used to handle api requests
 * related to phone and calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneManager extends RestManager {

    public PhoneManager(FreeboxOsSession session) throws FreeboxException {
        super("phone", session, Permission.CALLS);
    }

    public PhoneStatus getStatus() throws FreeboxException {
        List<PhoneStatus> statuses = getList(PhoneStatusResponse.class, true);
        if (statuses.size() > 0) {
            return statuses.get(0);
        }
        throw new FreeboxException("No phone status in response");
    }

    public PhoneConfig getConfig() throws FreeboxException {
        return get("config", PhoneConfigResponse.class, true);
    }

    public void ring(boolean startIt) throws FreeboxException {
        post(String.format("fxs_ring_%s", (startIt ? "start" : "stop")));
    }

    public void activateDect(boolean status) throws FreeboxException {
        PhoneConfig config = getConfig();
        config.setDectEnabled(status);
        put(PhoneConfigResponse.class, "config", config);
    }

    public void alternateRing(boolean status) throws FreeboxException {
        PhoneConfig config = getConfig();
        config.setDectRingOnOff(status);
        put(PhoneConfigResponse.class, "config", config);
    }

    private class PhoneConfigResponse extends Response<PhoneConfig> {
    }

    private static class PhoneStatusResponse extends ListResponse<PhoneStatus> {
        // @Override
        // protected @Nullable String internalEvaluate() {
        // String error = super.internalEvaluate();
        // return error != null ? error : getResult().size() == 0 ? "No phone status in response" : null;
        // }
    }
}
