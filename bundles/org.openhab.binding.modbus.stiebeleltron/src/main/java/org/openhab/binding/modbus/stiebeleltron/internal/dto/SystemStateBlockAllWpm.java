/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.stiebeleltron.internal.dto;

/**
 * Dto class for the System State Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class SystemStateBlockAllWpm extends SystemStateBlock {

    public int powerOff; // clearance by power supply company
    public int operatingStatus;
    public int faultStatus;
    public short busStatus;
    public int defrostInitiated;
    public int activeError;

    @Override
    public String toString() {
        return "System State Block {" + "\n  state=" + state + "\n  powerOff=" + powerOff + "\n  operatingStatus="
                + operatingStatus + "\n  faultStatus=" + faultStatus + "\n  busStatus=" + busStatus
                + "\n  defrostInitiated=" + defrostInitiated + "\n  activeError=" + activeError + "\n}";
    }
}
