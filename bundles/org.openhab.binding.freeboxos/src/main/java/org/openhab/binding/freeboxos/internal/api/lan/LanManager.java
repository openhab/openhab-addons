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
package org.openhab.binding.freeboxos.internal.api.lan;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.LanConfig.LanConfigResponse;
import org.openhab.binding.freeboxos.internal.api.lan.LanConfig.NetworkMode;
import org.openhab.binding.freeboxos.internal.api.rest.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;

/**
 * The {@link LanManager} is the Java class used to handle api requests
 * related to lan
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanManager extends ConfigurableRest<LanConfig, LanConfigResponse> {
    public static final String LAN_SUB_PATH = "lan";
    private final NetworkMode networkMode;

    public LanManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LanConfigResponse.class, LAN_SUB_PATH, CONFIG_SUB_PATH);
        this.networkMode = getConfig().getMode();
        session.addManager(LanBrowserManager.class, new LanBrowserManager(session, getUriBuilder()));
    }

    public NetworkMode getNetworkMode() throws FreeboxException {
        return networkMode;
    }
}
