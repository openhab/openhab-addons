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

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.THING_FREEPLUG;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link FreeplugManager} is the Java class used to handle api requests related to freeplugs
 * https://dev.freebox.fr/sdk/os/freeplug/
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeplugManager extends RestManager {
    private static final String RESET_ACTION = "reset";

    private static class Networks extends Response<Network> {
    }

    public static enum NetRole {
        STA, // Freeplug station
        PCO, // Freeplug proxy coordinator
        CCO, // Central Coordinator
        UNKNOWN;
    }

    private enum Status {
        UP, // The ethernet port is up
        DOWN, // The ethernet port is down
        UNKNOWN // The ethernet port state is unknown
    }

    public static record Freeplug(MACAddress id, String netId, // Id of the network holding the plug
            boolean local, // if true the Freeplug is connected directly to the Freebox
            NetRole netRole, // Freeplug network role
            String model, Status ethPortStatus, //
            boolean ethFullDuplex, // ethernet link is full duplex
            boolean hasNetwork, // is connected to the network
            int ethSpeed, // ethernet port speed
            int inactive, // seconds since last activity
            int rxRate, // rx rate (from the freeplugs to the “cco” freeplug) (in Mb/s) -1 if not available
            int txRate) { // tx rate (from the “cco” freeplug to the freeplugs) (in Mb/s) -1 if not available
    }

    private static record Network(MACAddress id, List<Freeplug> members) {
    }

    public FreeplugManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, session.getUriBuilder().path(THING_FREEPLUG));
    }

    // Most of the users will host only one CPL network on their server, so we hide the network level in the manager
    public List<Freeplug> getPlugs() throws FreeboxException {
        return get(Networks.class).stream().map(Network::members).flatMap(List::stream).toList();
    }

    public Optional<Freeplug> getPlug(MACAddress mac) throws FreeboxException {
        return getPlugs().stream().filter(plug -> plug.id.equals(mac)).findFirst();
    }

    public void reboot(MACAddress mac) throws FreeboxException {
        post(mac.toColonDelimitedString(), RESET_ACTION);
    }
}
