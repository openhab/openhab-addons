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
package org.openhab.binding.freeboxos.internal.api.freeplug;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.FREEPLUG_PATH;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.RestManager;

/**
 * The {@link FreeplugManager} is the Java class used to handle api requests related to freeplugs
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeplugManager extends RestManager {
    private static class NetworksResponse extends Response<List<FreeplugNetwork>> {
    }

    public static class ReceiverResponse extends Response<Freeplug> {
    }

    public FreeplugManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, FREEPLUG_PATH);
    }

    // Most of the users will host only one CPL network on their server, so we hide the network level in the manager
    public List<Freeplug> getPlugs() throws FreeboxException {
        List<Freeplug> plugs = new ArrayList<>();
        List<FreeplugNetwork> networks = get(NetworksResponse.class);
        if (networks != null) {
            networks.forEach(network -> plugs.addAll(network.getMembers()));
        }
        return plugs;
    }

    public Optional<Freeplug> getPlug(String mac) throws FreeboxException {
        return getPlugs().stream().filter(plug -> mac.equals(plug.getId())).findFirst();
    }

    public void reboot(String mac) throws FreeboxException {
        post(mac, "reset");
    }

}
