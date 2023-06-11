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
package org.openhab.binding.digitalstrom.internal.lib.listener.stateenums;

/**
 * The {@link ManagerStates} contains all reachable states of the digitalSTROM-Manager in {@link ManagerTypes}
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ManagerStates {
    RUNNING,
    STOPPED,
    INITIALIZING,
    GENERATING_SCENES
}
