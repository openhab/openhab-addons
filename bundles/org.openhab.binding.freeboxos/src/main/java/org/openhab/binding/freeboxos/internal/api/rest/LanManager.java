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
package org.openhab.binding.freeboxos.internal.api.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

import inet.ipaddr.IPAddress;

/**
 * The {@link LanManager} is the Java class used to handle api requests related to lan
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanManager extends ConfigurableRest<LanManager.LanConfig, LanManager.Config> {
    private static final String PATH = "lan";

    protected static class Config extends Response<LanConfig> {
    }

    private static enum Mode {
        ROUTER,
        BRIDGE,
        UNKNOWN;
    }

    public static record LanConfig(IPAddress ip, String name, String nameDns, String nameMdns, String nameNetbios,
            Mode mode) {
    }

    public LanManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, Config.class, session.getUriBuilder().path(PATH), CONFIG_PATH);
        session.addManager(LanBrowserManager.class, new LanBrowserManager(session, getUriBuilder()));
    }
}
