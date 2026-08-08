/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.eebus.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Service-level configuration for the EEBus add-on (backed by
 * {@code $OPENHAB_CONF/services/eebus.cfg} and the MainUI "Other Services" page), analogous to
 * {@code org.openhab.io.homekit.internal.HomekitSettings}.
 *
 * @author openHAB EEBus Add-on Contributors - Initial contribution
 */
@NonNullByDefault
public class EEBusSettings {

    public static final String CONFIG_PID = "org.openhab.eebus";

    public String bindAddress = "0.0.0.0";
    public int port = 4712;
    public String wssPath = "/ship/";
    public String serviceDomain = "local.";

    public String deviceId = "d:_i:openHAB:eebus-01";
    public String friendlyName = "openHAB";
    public String deviceType = "GENERIC";
    public String entityType = "CEM";

    public String connectPolicy = "TRUSTED";
    public String trustedSkis = "";
    public boolean autoAcceptPairing = false;

    /**
     * @return true if a change from {@code other} to {@code this} requires tearing down and
     *         rebuilding the SHIP node (network/identity settings), as opposed to something that
     *         can be applied without a restart.
     */
    public boolean requiresRestart(EEBusSettings other) {
        return !Objects.equals(bindAddress, other.bindAddress) || port != other.port
                || !Objects.equals(wssPath, other.wssPath) || !Objects.equals(serviceDomain, other.serviceDomain)
                || !Objects.equals(deviceId, other.deviceId) || !Objects.equals(friendlyName, other.friendlyName)
                || !Objects.equals(deviceType, other.deviceType) || !Objects.equals(entityType, other.entityType);
    }
}
