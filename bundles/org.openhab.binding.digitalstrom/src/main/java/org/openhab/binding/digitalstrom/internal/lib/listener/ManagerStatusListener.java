/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.listener;

import org.openhab.binding.digitalstrom.internal.lib.listener.stateenums.ManagerStates;
import org.openhab.binding.digitalstrom.internal.lib.listener.stateenums.ManagerTypes;

/**
 * The {@link ManagerStatusListener} is notified, if the state of digitalSTROM-Manager has changed.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface ManagerStatusListener {

    /**
     * This method is called whenever the state of a digitalSTROM-Manager has changed.<br>
     * For that it passes the {@link ManagerTypes} and the new {@link ManagerStates}.
     *
     * @param managerType of the digitalSTROM-Manager
     * @param newState of the digitalSTROM-Manager
     */
    void onStatusChanged(ManagerTypes managerType, ManagerStates newState);
}
