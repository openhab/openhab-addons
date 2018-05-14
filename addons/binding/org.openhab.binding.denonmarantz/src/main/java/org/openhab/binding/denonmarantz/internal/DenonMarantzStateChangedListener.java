/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal;

import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.denonmarantz.handler.DenonMarantzHandler;

/**
 * Interface to notify the {@link DenonMarantzHandler} about state changes.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 *
 */
public interface DenonMarantzStateChangedListener {
    /**
     * Update was received.
     *
     * @param channelID the channel for which its state changed
     * @param state the new state of the channel
     */
    void stateChanged(String channelID, State state);

    /**
     * A connection error occurred
     *
     * @param errorMessage the error message
     */
    void connectionError(String errorMessage);
}
