/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service;

/**
 * Callback for the automatic Refresh
 *
 * @author Marc Mettke - Initial contribution
 */
public interface StateUpdate {
    /**
     * The new reachable state.
     *
     * @param state A ping time in ms, 0 if the device is reachable but no time information is available
     *            or -1 if he device is not reachable.
     */
    public void newState(double state);

    public void invalidConfig();
}
