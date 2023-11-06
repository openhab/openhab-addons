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
package org.openhab.binding.network.internal.dhcp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implement this interface to be notified of DHCP IP request messages
 * for a registered IP address. Register to {@see DHCPListenSingleton}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface IPRequestReceivedCallback {
    /**
     * The {@see ReceiveDHCPRequestPackets} object could successfully identify
     * a DHCP request message on the network.
     *
     * @param ipAddress The requested IP address.
     */
    void dhcpRequestReceived(String ipAddress);
}
