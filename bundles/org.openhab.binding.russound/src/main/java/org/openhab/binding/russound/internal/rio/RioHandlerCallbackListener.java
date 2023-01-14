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
package org.openhab.binding.russound.internal.rio;

import org.openhab.core.types.State;

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
