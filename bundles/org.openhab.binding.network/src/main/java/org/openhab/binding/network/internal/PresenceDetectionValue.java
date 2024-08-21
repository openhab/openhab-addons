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
package org.openhab.binding.network.internal;

import static org.openhab.binding.network.internal.utils.NetworkUtils.durationToMillis;

import java.time.Duration;
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

    public static final Duration UNREACHABLE = Duration.ofMillis(-1);

    private final String hostAddress;
    private Duration latency;

    private final Set<PresenceDetectionType> reachableDetectionTypes = new TreeSet<>();
    private final List<Integer> reachableTcpPorts = new ArrayList<>();

    /**
     * Create a new {@link PresenceDetectionValue} with an initial latency.
     *
     * @param hostAddress The target IP
     * @param latency The ping latency. Can be <0 if the device is not reachable.
     */
    PresenceDetectionValue(String hostAddress, Duration latency) {
        this.hostAddress = hostAddress;
        this.latency = latency;
    }

    /**
     * Return the host address of the presence detection result object.
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Return the ping latency, {@value #UNREACHABLE} if not reachable. Can be 0 if
     * no specific latency is known but the device is still reachable.
     */
    public Duration getLowestLatency() {
        return latency;
    }

    /**
     * Returns <code>true</code> if the target is reachable by any means.
     */
    public boolean isReachable() {
        return !UNREACHABLE.equals(latency);
    }

    /**
     * Called by {@see PresenceDetection} by all different means of presence detections.
     * If the given latency is lower than the already stored one, the stored one will be overwritten.
     *
     * @param newLatency The new latency.
     * @return Returns <code>true</code> if the latency was indeed lower and updated the stored one.
     * @throws IllegalArgumentException when {@code newLatency} is negative
     */
    boolean updateLatency(Duration newLatency) {
        if (newLatency.isNegative()) {
            throw new IllegalArgumentException(
                    "Latency must be >=0. Create a new PresenceDetectionValue for a not reachable device!");
        } else if (newLatency.isZero()) {
            return false;
        } else if (!isReachable() || latency.isZero() || newLatency.compareTo(latency) < 0) {
            latency = newLatency;
            return true;
        }
        return false;
    }

    /**
     * Add a successful {@link PresenceDetectionType}.
     *
     * @param type a {@link PresenceDetectionType}.
     */
    void addReachableDetectionType(PresenceDetectionType type) {
        reachableDetectionTypes.add(type);
    }

    /**
     * Return true if the target can be reached by ICMP or ARP pings.
     */
    public boolean isPingReachable() {
        return reachableDetectionTypes.contains(PresenceDetectionType.ARP_PING)
                || reachableDetectionTypes.contains(PresenceDetectionType.ICMP_PING);
    }

    /**
     * Return true if the target provides open TCP ports.
     */
    public boolean isTcpServiceReachable() {
        return reachableDetectionTypes.contains(PresenceDetectionType.TCP_CONNECTION);
    }

    /**
     * Return a string of comma-separated successful presence detection types.
     */
    public String getSuccessfulDetectionTypes() {
        return reachableDetectionTypes.stream().map(PresenceDetectionType::name).collect(Collectors.joining(", "));
    }

    /**
     * Return the reachable TCP ports of the presence detection value.
     * Thread safe.
     */
    public List<Integer> getReachableTcpPorts() {
        synchronized (reachableTcpPorts) {
            return new ArrayList<>(reachableTcpPorts);
        }
    }

    /**
     * Add a reachable TCP port to this presence detection result value object.
     * Thread safe.
     */
    void addReachableTcpPort(int tcpPort) {
        synchronized (reachableTcpPorts) {
            reachableTcpPorts.add(tcpPort);
        }
    }

    @Override
    public String toString() {
        return "PresenceDetectionValue [hostAddress=" + hostAddress + ", latency=" + durationToMillis(latency)
                + "ms, reachableDetectionTypes=" + reachableDetectionTypes + ", reachableTcpPorts=" + reachableTcpPorts
                + "]";
    }
}
