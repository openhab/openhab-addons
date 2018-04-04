/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal;

/**
 * Implement this callback to be notified of a presence detection result.
 *
 * @author David Graeff - Initial contribution
 */
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
