/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio;

import org.eclipse.smarthome.core.types.State;

/**
 * Interface definition for any listener to state changes in a {@link RioHandlerCallback}
 *
 * @author Tim Roberts - Initial contribution
 */
public interface RioHandlerCallbackListener {
    /**
     * Called when the state has change
     *
     * @param channelId a non null, non-empty channel id that changed
     * @param state a non-null new state
     */
    void stateUpdate(String channelId, State state);
}
