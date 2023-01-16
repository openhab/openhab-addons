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
package org.openhab.binding.freeboxos.internal.api.phone;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;

/**
 * The {@link PhoneManager} is the Java class used to handle api requests related to phone and calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneManager extends ConfigurableRest<PhoneConfig, PhoneManager.ConfigResponse> {
    public class ConfigResponse extends Response<PhoneConfig> {
    }

    private class StatusResponse extends Response<PhoneStatus> {
    }

    private static class StatusesResponse extends Response<PhoneStatus> {
    }

    public PhoneManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.CALLS, ConfigResponse.class, session.getUriBuilder().path(PHONE_SUB_PATH),
                CONFIG_SUB_PATH);
    }

    public List<PhoneStatus> getPhoneStatuses() throws FreeboxException {
        return get(StatusesResponse.class, "");
    }

    public Optional<PhoneStatus> getOptStatus(int id) throws FreeboxException {
        return Optional.ofNullable(getStatus(id));
    }

    public @Nullable PhoneStatus getStatus(int id) throws FreeboxException {
        return getSingle(StatusResponse.class, Integer.toString(id));
    }

    public void ringFxs(boolean startIt) throws FreeboxException {
        post("fxs_ring_%s".formatted(startIt ? "start" : "stop"));
    }

    public void ringDect(boolean startIt) throws FreeboxException {
        post("dect_page_%s".formatted(startIt ? "start" : "stop"));
    }

    public void alternateRing(boolean status) throws FreeboxException {
        PhoneConfig config = getConfig();
        config.setDectRingOnOff(status);
        put(ConfigResponse.class, config, CONFIG_SUB_PATH);
    }

    public void setGainRx(int clientId, int gain) throws FreeboxException {
        PhoneStatus status = getStatus(clientId);
        if (status != null) {
            status.setGainRx(gain);
            put(StatusResponse.class, status, Integer.toString(clientId));
        }
    }

    public void setGainTx(int clientId, int gain) throws FreeboxException {
        PhoneStatus status = getStatus(clientId);
        if (status != null) {
            status.setGainTx(gain);
            put(StatusResponse.class, status, Integer.toString(clientId));
        }
    }

    public boolean getStatus() throws FreeboxException {
        return getConfig().isEnabled();
    }

    public boolean setStatus(boolean enabled) throws FreeboxException {
        PhoneConfig config = getConfig();
        config.setEnabled(enabled);
        return setConfig(config).isEnabled();
    }
}
