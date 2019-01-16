/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * All the supported presence detection types of this binding.
 * Used by {@see PresenceDetectionValue}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public enum PresenceDetectionType {
    ARP_PING,
    ICMP_PING,
    TCP_CONNECTION,
    DHCP_REQUEST
}
