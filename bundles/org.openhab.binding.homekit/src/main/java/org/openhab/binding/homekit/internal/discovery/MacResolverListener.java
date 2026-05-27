/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MacResolverListener} is a listener interface for receiving notifications from a
 * MacResolver about changes in MAC address.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface MacResolverListener {

    /**
     * This method is called when a MAC address has been resolved for a given device.
     *
     * @param ip the IP address of the device for which the MAC address has been resolved
     * @param mac the resolved MAC address of the device
     */
    void macAddressResolved(String ip, String mac);
}
