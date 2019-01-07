/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.netutils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deconz.internal.dto.SensorState;

/**
 * Informs about updated sensor states
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface ValueUpdateListener {
    /**
     * A sensor state was updated.
     *
     * @param sensorID The sensor ID (API endpoint)
     * @param newState The new state
     */
    void websocketUpdate(String sensorID, SensorState newState);
}
