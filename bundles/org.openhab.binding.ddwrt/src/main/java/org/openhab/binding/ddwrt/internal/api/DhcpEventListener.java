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
package org.openhab.binding.ddwrt.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener for DHCP syslog events from gateway devices.
 * Handlers register to be notified when DHCP events occur.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public interface DhcpEventListener {

    /**
     * Called when a DHCP event is detected on a gateway device.
     *
     * @param hostname the hostname of the device where the event occurred
     * @param eventMessage the syslog event message
     */
    void onDhcpEvent(String hostname, String eventMessage);
}
