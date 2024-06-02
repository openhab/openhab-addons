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

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link PhoneManager} is the Java class used to handle api requests related to phone and calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PhoneManager extends ConfigurableRest<PhoneManager.Config, PhoneManager.ConfigResponse> {
    private static final String DECT_PAGE_ACTION = "dect_page_%s";
    private static final String FXS_RING_ACTION = "fxs_ring_%s";
    private static final String PATH = "phone";

    protected class ConfigResponse extends Response<Config> {
    }

    protected class StatusResponse extends Response<Status> {
    }

    private enum NetworkStatus {
        WORKING,
        UNKNOWN
    }

    public static record Config(NetworkStatus network, boolean dectEcoMode, String dectPin, int dectRingPattern,
            boolean dectRegistration, boolean dectNemoMode, boolean dectEnabled, boolean dectRingOnOff) {
    }

    public enum Type {
        FXS,
        DECT,
        UNKNOWN
    }

    public static record Status(int id, boolean isRinging, boolean onHook, boolean hardwareDefect, Type type,
            @Nullable String vendor, int gainRx, int gainTx) {

        public String vendor() {
            String localVendor = vendor;
            return localVendor != null ? localVendor : "Unknown";
        }
    }

    public PhoneManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.CALLS, ConfigResponse.class, session.getUriBuilder().path(PATH),
                CONFIG_PATH);
    }

    public List<Status> getPhoneStatuses() throws FreeboxException {
        return get(StatusResponse.class, "");
    }

    public Optional<Status> getStatus(int id) throws FreeboxException {
        return Optional.ofNullable(getSingle(StatusResponse.class, Integer.toString(id)));
    }

    public void ringFxs(boolean startIt) throws FreeboxException {
        post(FXS_RING_ACTION.formatted(startIt ? "start" : "stop"));
    }

    public void ringDect(boolean startIt) throws FreeboxException {
        post(DECT_PAGE_ACTION.formatted(startIt ? "start" : "stop"));
    }

    public void setGainRx(int clientId, int gain) throws FreeboxException {
        Optional<Status> result = getStatus(clientId);
        if (result.isPresent()) {
            Status status = result.get();
            Status newStatus = new Status(status.id, status.isRinging, status.onHook, status.hardwareDefect,
                    status.type, status.vendor, gain, status.gainTx);
            put(StatusResponse.class, newStatus, Integer.toString(clientId));
        }
    }

    public void setGainTx(int clientId, int gain) throws FreeboxException {
        Optional<Status> result = getStatus(clientId);
        if (result.isPresent()) {
            Status status = result.get();
            Status newStatus = new Status(status.id, status.isRinging, status.onHook, status.hardwareDefect,
                    status.type, status.vendor, status.gainRx, gain);
            put(StatusResponse.class, newStatus, Integer.toString(clientId));
        }
    }

    public void alternateRing(boolean status) throws FreeboxException {
        Config config = getConfig();
        Config newConfig = new Config(config.network, config.dectEcoMode, config.dectPin, config.dectRingPattern,
                config.dectRegistration, config.dectNemoMode, config.dectEnabled, status);
        put(ConfigResponse.class, newConfig, CONFIG_PATH);
    }

    public boolean setStatus(boolean enabled) throws FreeboxException {
        Config config = getConfig();
        Config newConfig = new Config(config.network, config.dectEcoMode, config.dectPin, config.dectRingPattern,
                config.dectRegistration, config.dectNemoMode, enabled, config.dectRingOnOff);
        return setConfig(newConfig).dectEnabled;
    }
}
