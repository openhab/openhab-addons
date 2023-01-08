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
package org.openhab.binding.neeo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration used by {@link org.openhab.binding.neeo.internal.handler.NeeoBrainHandler}
 *
 * @author Tim Roberts - initial contribution
 */
@NonNullByDefault
public class NeeoBrainConfig {

    /** The ip address */
    @Nullable
    private String ipAddress;

    /** Whether to enable forward actions */
    private boolean enableForwardActions;

    /** The forward actions chain (comma delimited) */
    @Nullable
    private String forwardChain;

    /** Whether to discover empty rooms or not */
    private boolean discoverEmptyRooms;

    /** The check status interval (in seconds) */
    private int checkStatusInterval;

    /**
     * Gets the ip address.
     *
     * @return the ip address
     */
    @Nullable
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the ip address.
     *
     * @param ipAddress the new ip address
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Determines if forward actions is enabled
     *
     * @return true for enabled, false otherwise
     */
    public boolean isEnableForwardActions() {
        return enableForwardActions;
    }

    /**
     * Sets whether to enable forward actions
     *
     * @param enableForwardActions true to enable, false otherwise
     */
    public void setEnableForwardActions(boolean enableForwardActions) {
        this.enableForwardActions = enableForwardActions;
    }

    /**
     * Get's the forward chain
     *
     * @return the forward chain
     */
    @Nullable
    public String getForwardChain() {
        return forwardChain;
    }

    /**
     * Sets the forward change
     *
     * @param forwardChain the forward chain
     */
    public void setForwardChain(String forwardChain) {
        this.forwardChain = forwardChain;
    }

    /**
     * Whether empty rooms should be discovered or not
     *
     * @return true to discover empty rooms, false otherwise
     */
    public boolean isDiscoverEmptyRooms() {
        return discoverEmptyRooms;
    }

    /**
     * Set's whether to discover empty rooms
     *
     * @param discoverEmptyRooms true to discover, false otherwise
     */
    public void setDiscoverEmptyRooms(boolean discoverEmptyRooms) {
        this.discoverEmptyRooms = discoverEmptyRooms;
    }

    /**
     * Gets the interval (in seconds) to check the brain status
     *
     * @return the check status interval (negative to disable)
     */
    public int getCheckStatusInterval() {
        return checkStatusInterval;
    }

    /**
     * Sets the interval (in seconds) to check the brain status
     *
     * @param checkStatusInterval return the check status interval (negative to disable)
     */
    public void setCheckStatusInterval(int checkStatusInterval) {
        this.checkStatusInterval = checkStatusInterval;
    }

    @Override
    public String toString() {
        return "NeeoBrainConfig{" + "ipAddress='" + ipAddress + '\'' + ", enableForwardActions=" + enableForwardActions
                + ", forwardChain='" + forwardChain + '\'' + ", discoverEmptyRooms=" + discoverEmptyRooms
                + ", checkStatusInterval=" + checkStatusInterval + '}';
    }
}
