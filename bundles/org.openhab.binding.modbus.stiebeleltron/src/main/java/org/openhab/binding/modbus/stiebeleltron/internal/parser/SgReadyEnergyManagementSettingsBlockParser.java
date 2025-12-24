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
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementSettingsBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses modbus SG Ready Energy Management Settings data of a WPM/WPM3/WPM3i compatible heat pump into a SG Ready
 * Energy Management Settings Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class SgReadyEnergyManagementSettingsBlockParser extends AbstractBaseParser {

    public SgReadyEnergyManagementSettingsBlock parse(ModbusRegisterArray raw, SgReadyEnergyManagementControl control) {
        SgReadyEnergyManagementSettingsBlock block = new SgReadyEnergyManagementSettingsBlock();

        if (control.featureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS)) {
            block.sgReadyOnOffSwitch = extractUInt16(raw, 0, 0);
            if (block.sgReadyOnOffSwitch == 32768) {
                control.setFeatureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS, false);
            } else {
                block.sgReadyInput1 = extractUInt16(raw, 1, 0);
                if (block.sgReadyInput1 == 32768) {
                    control.setFeatureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS_INPUT1, false);
                }
                block.sgReadyInput2 = extractUInt16(raw, 2, 0);
                if (block.sgReadyInput2 == 32768) {
                    control.setFeatureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS_INPUT2, false);
                }
            }
        }
        return block;
    }
}
