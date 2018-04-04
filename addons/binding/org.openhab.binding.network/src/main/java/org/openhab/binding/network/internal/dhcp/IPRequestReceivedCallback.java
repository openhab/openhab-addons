/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.dhcp;

/**
 * Implement this interface to be notified of DHCP IP request messages
 * for a registered IP address. Register to {@see DHCPListenSingleton}.
 *
 * @author David Graeff - Initial contribution
 */
public interface IPRequestReceivedCallback {
    /**
     * The {@see ReceiveDHCPRequestPackets} object could successfully identify
     * a DHCP request message on the network.
     *
     * @param ipAddress The requested IP address.
     */
    void dhcpRequestReceived(String ipAddress);
}
