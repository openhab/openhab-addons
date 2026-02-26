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
package org.openhab.binding.ddwrt.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ddwrt.internal.api.SyslogParser.SyslogEvent;

/**
 * Listener interface for classified syslog events from the SSH log follower.
 * Modeled after the logreader binding's FileReaderListener pattern.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public interface SyslogListener {

    /**
     * Called when a DHCP event is detected (lease, renewal, release).
     * Processes: dnsmasq-dhcp, dhcpd, udhcpc
     */
    void onDhcpEvent(SyslogEvent event);

    /**
     * Called when a wireless association/deassociation event is detected.
     * Processes: hostapd, wpa_supplicant, nas
     */
    void onWirelessEvent(SyslogEvent event);

    /**
     * Called when a warning-level event is detected.
     * Includes firewall IPTABLES log entries and syslog warning priority.
     */
    void onWarningEvent(SyslogEvent event);

    /**
     * Called when an error-level event is detected.
     * Includes syslog error/critical/emergency priority.
     */
    void onErrorEvent(SyslogEvent event);
}
