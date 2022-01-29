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
package org.openhab.binding.network.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains the result or partial result of a presence detection.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class PresenceDetectionValue {
    private double latency;
    private boolean detectionIsFinished;
    private final Set<PresenceDetectionType> reachableByType = new TreeSet<>();
    private final List<Integer> tcpServiceReachable = new ArrayList<>();
    private final String hostAddress;

    /**
     * Returns true if the target is reachable by any means.
     */
    public boolean isReachable() {
        return latency >= 0;
    }

    /**
     * Return the ping latency in ms or -1 if not reachable. Can be 0 if
     * no specific latency is known but the device is still reachable.
     */
    public double getLowestLatency() {
        return latency;
    }

    /**
     * Return a string of comma separated successful presence detection types.
     */
    public String getSuccessfulDetectionTypes() {
        return reachableByType.stream().map(v -> v.name()).collect(Collectors.joining(", "));
    }

    /**
     * Return the reachable tcp ports of the presence detection value.
     * Thread safe.
     */
    public List<Integer> getReachableTCPports() {
        synchronized (tcpServiceReachable) {
            List<Integer> copy = new ArrayList<>();
            copy.addAll(tcpServiceReachable);
            return copy;
        }
    }

    /**
     * Return true if the presence detection is fully completed (no running
     * threads anymore).
     */
    public boolean isFinished() {
        return detectionIsFinished;
    }

    ////// Package private methods //////

    /**
     * Create a new PresenceDetectionValue with an initial latency.
     *
     * @param hostAddress The target IP
     * @param latency The ping latency in ms. Can be <0 if the device is not reachable.
     */
    PresenceDetectionValue(String hostAddress, double latency) {
        this.hostAddress = hostAddress;
        this.latency = latency;
    }

    /**
     * Add a successful PresenceDetectionType.
     *
     * @param type A PresenceDetectionType.
     */
    void addType(PresenceDetectionType type) {
        reachableByType.add(type);
    }

    /**
     * Called by {@see PresenceDetection} by all different means of presence detections.
     * If the given latency is lower than the already stored one, the stored one will be overwritten.
     *
     * @param newLatency The new latency.
     * @return Returns true if the latency was indeed lower and updated the stored one.
     */
    boolean updateLatency(double newLatency) {
        if (newLatency < 0) {
            throw new IllegalArgumentException(
                    "Latency must be >=0. Create a new PresenceDetectionValue for a not reachable device!");
        }
        if (newLatency > 0 && (latency == 0 || newLatency < latency)) {
            latency = newLatency;
            return true;
        }
        return false;
    }

    /**
     * Add a reachable tcp port to this presence detection result value object.
     * Thread safe.
     */
    void addReachableTcpService(int tcpPort) {
        synchronized (tcpServiceReachable) {
            tcpServiceReachable.add(tcpPort);
        }
    }

    /**
     * Mark the result value as final. No modifications should occur after this call.
     */
    void setDetectionIsFinished(boolean detectionIsFinished) {
        this.detectionIsFinished = detectionIsFinished;
    }

    /**
     * Return the host address of the presence detection result object.
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Return true if the target can be reached by ICMP or ARP pings.
     */
    public boolean isPingReachable() {
        return reachableByType.contains(PresenceDetectionType.ARP_PING)
                || reachableByType.contains(PresenceDetectionType.ICMP_PING);
    }

    /**
     * Return true if the target provides open TCP ports.
     */
    public boolean isTCPServiceReachable() {
        return reachableByType.contains(PresenceDetectionType.TCP_CONNECTION);
    }
}
