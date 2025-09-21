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
package org.openhab.binding.modbus.stiebeleltron.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementControl;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementControl.SgReadyEnMgmtFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementSystemInformationBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses modbus SG Ready Energy Management System Information data of a WPM compatible heat pump into a SG
 * Ready Energy Management System Information Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class SgReadyEnergyManagementSystemInformationBlockParser extends AbstractBaseParser {

    public SgReadyEnergyManagementSystemInformationBlock parse(ModbusRegisterArray raw,
            SgReadyEnergyManagementControl control) {
        SgReadyEnergyManagementSystemInformationBlock block = new SgReadyEnergyManagementSystemInformationBlock();

        if (control.featureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SYS_INFO)) {
            block.sgReadyOperatingState = extractUInt16(raw, 0, 0);
            if (block.sgReadyOperatingState == 32768) {
                control.setFeatureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SYS_INFO, false);
            } else {
                block.sgReadyControllerIdentification = extractUInt16(raw, 1, 0);
            }
        }

        return block;
    }
}
