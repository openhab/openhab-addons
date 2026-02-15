/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * Dto class for the SG Ready Energy Management Settings Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class SgReadyEnergyManagementSettingsBlock {

    public int sgReadyOnOffSwitch;
    public int sgReadyInput1;
    public int sgReadyInput2;

    @Override
    public String toString() {
        return "SgReadyEnergyManagementSettingsBlock {" + "\n  sgReadyOnOffSwitch=" + sgReadyOnOffSwitch
                + ",\n  sgReadyInput1=" + sgReadyInput1 + ",\n  sgReadyInput2=" + sgReadyInput2 + "\n}";
    }
}
