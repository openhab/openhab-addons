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
package org.openhab.binding.bluetooth.radoneye.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for Radoneye device.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public class RadoneyeConfiguration {
    public String address = "";
    public int fwVersion = 1;
    public int refreshInterval = 300;

    @Override
    public String toString() {
        return "[address=" + address + ", fwVersion=" + fwVersion + ", refreshInterval=" + refreshInterval + "]";
    }
}
