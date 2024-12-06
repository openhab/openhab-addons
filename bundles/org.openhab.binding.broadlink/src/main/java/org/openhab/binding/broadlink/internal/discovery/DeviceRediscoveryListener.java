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
package org.openhab.binding.broadlink.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Broadlink discovery implementation.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public interface DeviceRediscoveryListener {
    /**
     * Discovered a device on the supplied ip address *
     *
     * @param newIpAddress
     */
    void onDeviceRediscovered(String newIpAddress);

    /**
     * Method triggered when device discovery fails
     */
    void onDeviceRediscoveryFailure();
}
