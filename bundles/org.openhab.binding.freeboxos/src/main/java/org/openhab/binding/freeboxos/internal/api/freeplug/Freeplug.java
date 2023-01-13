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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ConnectionState;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.PlugRole;

/**
 * The {@link Freeplug} is the Java class used to map the "Freeplug" structure used by the available
 * Freeplug API
 *
 * https://dev.freebox.fr/sdk/os/freeplug/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Freeplug {
    private @Nullable String id; // MAC Address of the plug
    private @Nullable String netId; // Id of the network holding the plug
    private boolean local; // if true the Freeplug is connected directly to the Freebox
    private PlugRole netRole = PlugRole.UNKNOWN; // Freeplug network role
    private @Nullable String model;
    private ConnectionState ethPortStatus = ConnectionState.UNKNOWN;
    private boolean ethFullDuplex; // ethernet link is full duplex
    private boolean hasNetwork; // is connected to the network
    private int ethSpeed; // ethernet port speed
    private int inactive; // seconds since last activity
    private int rxRate; // rx rate (from the freeplugs to the “cco” freeplug) (in Mb/s) -1 if not available
    private int txRate; // tx rate (from the “cco” freeplug to the freeplugs) (in Mb/s) -1 if not available

    public String getId() {
        return Objects.requireNonNull(id).toLowerCase();
    }

    public boolean isLocal() {
        return local;
    }

    public PlugRole getNetRole() {
        return netRole;
    }

    public String getModel() {
        return Objects.requireNonNull(model);
    }

    public ConnectionState getEthPortStatus() {
        return ethPortStatus;
    }

    public boolean isEthFullDuplex() {
        return ethFullDuplex;
    }

    public boolean hasNetwork() {
        return hasNetwork;
    }

    public int getEthSpeed() {
        return ethSpeed;
    }

    public int getInactive() {
        return inactive;
    }

    public int getRxRate() {
        return rxRate;
    }

    public int getTxRate() {
        return txRate;
    }

    public String getNetId() {
        return Objects.requireNonNull(netId).toLowerCase();
    }
}
