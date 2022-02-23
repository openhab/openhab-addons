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
package org.openhab.binding.freeboxos.internal.api.phone;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneConfig.PhoneConfigResponse;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus.PhoneStatusResponse;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;

/**
 * The {@link PhoneManager} is the Java class used to handle api requests
 * related to phone and calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneManager extends ActivableRest<PhoneConfig, PhoneConfigResponse> {
    private static final String PHONE_SUB_PATH = "phone";

    public PhoneManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.CALLS, PhoneConfigResponse.class, PHONE_SUB_PATH, CONFIG_SUB_PATH);
    }

    public List<PhoneStatus> getPhoneStatuses() throws FreeboxException {
        return getList(PhoneStatusResponse.class, "");
    }

    public Optional<PhoneStatus> getStatus(int id) throws FreeboxException {
        List<PhoneStatus> statuses = getPhoneStatuses();
        return statuses.stream().filter(status -> status.getId() == id).findFirst();
    }

    public void ring(boolean startIt) throws FreeboxException {
        post(String.format("fxs_ring_%s", (startIt ? "start" : "stop")));
    }

    public void alternateRing(boolean status) throws FreeboxException {
        PhoneConfig config = getConfig();
        config.setDectRingOnOff(status);
        put(PhoneConfigResponse.class, config, CONFIG_SUB_PATH);
    }
}
