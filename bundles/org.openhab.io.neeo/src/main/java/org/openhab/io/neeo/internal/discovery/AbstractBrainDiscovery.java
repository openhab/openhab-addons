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
package org.openhab.io.neeo.internal.discovery;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.models.NeeoSystemInfo;

/**
 * This abstract implementation of {@link BrainDiscovery} will provide the listener functionality to discovery
 * implementations.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
abstract class AbstractBrainDiscovery implements BrainDiscovery {

    /** The listeners */
    private final List<DiscoveryListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(DiscoveryListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        listeners.add(listener);
    }

    @Override
    public void removeListener(DiscoveryListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        listeners.remove(listener);
    }

    /**
     * Fires the {@link DiscoveryListener#discovered(NeeoSystemInfo, InetAddress)} method on all listeners
     *
     * @param sysInfo the non-null {@link NeeoSystemInfo}
     * @param ipAddress the ip address of the brain
     */
    protected void fireDiscovered(NeeoSystemInfo sysInfo, InetAddress ipAddress) {
        Objects.requireNonNull(sysInfo, "sysInfo cannot be null");
        Objects.requireNonNull(ipAddress, "ipAddress cannot be null");
        for (DiscoveryListener listener : listeners) {
            listener.discovered(sysInfo, ipAddress);
        }
    }

    /**
     * Fires the {@link DiscoveryListener#updated(NeeoSystemInfo, InetAddress, InetAddress)} method on all listeners
     *
     * @param sysInfo the non-null {@link NeeoSystemInfo}
     * @param oldIpAddress the non-null old ip address of the brain
     * @param newIpAddress the non-null new ip address of the brain
     */
    protected void fireUpdated(NeeoSystemInfo sysInfo, InetAddress oldIpAddress, InetAddress newIpAddress) {
        Objects.requireNonNull(sysInfo, "sysInfo cannot be null");
        Objects.requireNonNull(oldIpAddress, "oldIpAddress cannot be null");
        Objects.requireNonNull(newIpAddress, "newIpAddress cannot be null");
        for (DiscoveryListener listener : listeners) {
            listener.updated(sysInfo, oldIpAddress, newIpAddress);
        }
    }

    /**
     * Fires the {@link DiscoveryListener#removed(NeeoSystemInfo)} method on all listeners
     *
     * @param sysInfo the non-null {@link NeeoSystemInfo}
     */
    protected void fireRemoved(NeeoSystemInfo sysInfo) {
        Objects.requireNonNull(sysInfo, "sysInfo cannot be null");
        for (DiscoveryListener listener : listeners) {
            listener.removed(sysInfo);
        }
    }

    @Override
    public boolean removeDiscovered(String servletUrl) {
        return false;
    }

    @Override
    public void close() {
    }
}
