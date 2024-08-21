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
package org.openhab.binding.ventaair.internal.message.action;

/**
 * Action to power on/off the device
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class PowerAction implements Action {
    @SuppressWarnings("unused")
    private boolean Power;

    public PowerAction(boolean on) {
        this.Power = on;
    }
}
