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
package org.openhab.binding.nibeheatpump.internal.protocol;

/**
 * The {@link NibeHeatPumpProtocolState} define interface for Nibe protocol state machine.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface NibeHeatPumpProtocolState {
    /**
     * @return true to keep processing, false to read more data.
     */
    boolean process(NibeHeatPumpProtocolContext context);
}
