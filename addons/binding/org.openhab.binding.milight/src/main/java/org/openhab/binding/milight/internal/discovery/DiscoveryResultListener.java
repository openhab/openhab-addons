/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.discovery;

import java.net.InetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result callback interface.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface DiscoveryResultListener {
    /**
     * A Milight bridge got detected
     *
     * @param addr The IP address
     * @param id The bridge ID
     * @param version The bridge version (either 3 or 6)
     */
    void bridgeDetected(InetAddress addr, String id, int version);
}
