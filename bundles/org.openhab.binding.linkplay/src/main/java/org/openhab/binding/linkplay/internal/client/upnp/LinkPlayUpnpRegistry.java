/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.upnp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A registry for LinkPlay UPnP devices.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface LinkPlayUpnpRegistry {
    /**
     * Add a device listener for a given UDN.
     * 
     * @param udn the UDN of the device to add the listener for
     * @param listener the listener to add
     */
    void addDeviceListener(String udn, LinkPlayUpnpDeviceListener listener);

    /**
     * Remove a device listener for a given UDN.
     * 
     * @param udn the UDN of the device to remove the listener for
     */
    void removeDeviceListener(String udn);
}
