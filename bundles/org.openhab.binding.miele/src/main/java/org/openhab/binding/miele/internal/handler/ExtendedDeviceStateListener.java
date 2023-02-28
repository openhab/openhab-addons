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
package org.openhab.binding.miele.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Appliance handlers can implement the {@link ExtendedDeviceStateListener} interface
 * to extract additional information from the ExtendedDeviceState property.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface ExtendedDeviceStateListener {
    void onApplianceExtendedStateChanged(byte[] extendedDeviceState);
}
