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
package org.openhab.io.neeo.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This interface defines the contract for brain discovery implementations
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public interface BrainDiscovery extends AutoCloseable {

    /**
     * Adds a lister for discovery notifications
     *
     * @param listener the non-null listener
     */
    void addListener(DiscoveryListener listener);

    /**
     * Removes the listener from discovery notifications
     *
     * @param listener the non-null listener
     */
    void removeListener(DiscoveryListener listener);

    /**
     * Adds the specified system information as discovery
     *
     * @param address a non-null, non-empty IP address for the system
     * @return true if added, false otherwise
     */
    boolean addDiscovered(String address);

    /**
     * Removes any discovered information associated with the servlet URL
     *
     * @param servletUrl a non-null, non-empty servlet URL
     * @return true if removed, false otherwise
     */
    boolean removeDiscovered(String servletUrl);

    /**
     * Start the discovery process
     */
    void startDiscovery();

    /**
     * Ends the discovery process
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close();
}
