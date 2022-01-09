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
package org.openhab.io.neeo.internal.discovery;

import java.net.InetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.models.NeeoSystemInfo;

/**
 * The interface defines the contract for discovery notifications
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public interface DiscoveryListener {

    /**
     * Notification that a new brain was discovered
     *
     * @param brainSysInfo the non-null brain system information
     * @param ipAddress the ip address of the brain
     */
    void discovered(NeeoSystemInfo brainSysInfo, InetAddress ipAddress);

    /**
     * Notification that a brain has a new IP Address
     *
     * @param brainSysInfo the non-null brain system information
     * @param oldIpAddress the non-null old ip address of the brain
     * @param newIpAddress the non-null new ip address of the brain
     */
    void updated(NeeoSystemInfo brainSysInfo, InetAddress oldIpAddress, InetAddress newIpAddress);

    /**
     * Notification that a brain was removed
     *
     * @param brainSysInfo the non-null brain system information
     */
    void removed(NeeoSystemInfo brainSysInfo);
}
