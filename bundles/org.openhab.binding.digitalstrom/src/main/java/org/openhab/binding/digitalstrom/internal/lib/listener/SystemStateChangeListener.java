/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

/**
 * The {@link SystemStateChangeListener} can be implemented to get informed by digitalSTROM system state changes. It
 * has to be registered by supported classes, e.g. the
 * {@link org.openhab.binding.digitalstrom.internal.lib.manager.impl.TemperatureControlManager}
 * or self implemented classes.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface SystemStateChangeListener {

    /**
     * Will be called, if a digitalSTROM system state has changed.
     *
     * @param stateType of the digitalSTROM system state
     * @param newState of the digitalSTROM system state
     */
    void onSystemStateChanged(String stateType, String newState);
}
