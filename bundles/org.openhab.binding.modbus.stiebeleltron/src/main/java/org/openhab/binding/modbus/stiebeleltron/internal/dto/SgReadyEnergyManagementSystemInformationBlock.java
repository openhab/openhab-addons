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
 * Dto class for the SG Ready Energy Management System Information Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class SgReadyEnergyManagementSystemInformationBlock {

    public int sgReadyOperatingState;
    public int sgReadyControllerIdentification;

    @Override
    public String toString() {
        return "SgReadyEnergyManagementSystemInformationBlock{" + "\n  sgReadyOperatingState=" + sgReadyOperatingState
                + ",\n  sgReadyControllerIdentification=" + sgReadyControllerIdentification + "\n}";
    }
}
