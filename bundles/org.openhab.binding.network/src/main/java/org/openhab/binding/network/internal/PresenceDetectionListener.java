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
package org.openhab.binding.network.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implement this callback to be notified of a presence detection result.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface PresenceDetectionListener {

    /**
     * This method is called by the {@see PresenceDetectionService}
     * if a device is deemed to be reachable.
     *
     * @param value The partial result of the presence detection process.
     *            A partial result always means that the device is reachable, but not
     *            all methods have returned a value yet.
     */
    public void partialDetectionResult(PresenceDetectionValue value);

    /**
     * This method is called by the {@see PresenceDetectionService}
     * if a new device state is known. The device might be reachable by different means
     * or unreachable.
     *
     * @param value The final result of the presence detection process.
     */
    public void finalDetectionResult(PresenceDetectionValue value);
}
