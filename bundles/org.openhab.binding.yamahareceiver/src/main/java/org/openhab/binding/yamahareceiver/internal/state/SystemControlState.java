/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.state;

/**
 * System AVR state (system power, etc)
 *
 * @author David Graeff - Initial contribution
 *
 */
public class SystemControlState implements Invalidateable {
    public boolean power;
    public boolean partyMode;

    // If we lost the connection, invalidate the state.
    @Override
    public void invalidate() {
        power = false;
        partyMode = false;
    }
}
