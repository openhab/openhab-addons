/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.omatic.internal.api.model;

/**
 * The {@link OMaticMachineState} state for the OMaticState Machine.
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
public enum OMaticMachineState {

    NOT_STARTED,
    IDLE,
    ACTIVE,
    COMPLETE;

    @Override
    public String toString() {
        return this.name().toUpperCase();
    }
}
