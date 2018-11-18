/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * Listener for changes detected in the CanRelay
 *
 * @author Lubos Housa - Initial Contribution
 */
@NonNullByDefault
public interface CanRelayChangeListener {

    /**
     * Change of a state of a given given light switch detected in CanRelay
     *
     * @param nodeID nodeID of the light switch that changed
     * @param state  ON if this light was switched on, OFF otherwise
     */
    void onLightSwitchChanged(int nodeID, OnOffType state);

    /**
     * CanRelay just went offline due to an error
     *
     * @param error description of the error
     */
    void onCanRelayOffline(String error);
}
