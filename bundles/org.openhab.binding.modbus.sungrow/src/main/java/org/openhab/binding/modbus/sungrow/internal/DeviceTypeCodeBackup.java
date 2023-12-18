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
package org.openhab.binding.modbus.sungrow.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Definition of Sungrow Device codes.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public enum DeviceTypeCodeBackup {

    SG30KTL(0x27, "SG30KTL", 2),
    SG10KTL(0x26, "SG10KTL", 2),
    SG12KTL(0x29, "SG12KTL", 2),
    SG15KTL(0x28, "SG15KTL", 2),
    SG20KTL(0x2A, "SG20KTL", 2),
    SG30KU(0x2C, "SG30KU", 2),
    SG36KTL(0x2D, "SG36KTL", 2),
    SG36KU(0x2E, "SG36KU", 2),
    SG40KTL(0x2F, "SG40KTL", 2),
    SG40KTL_M(0x0135, "SG40KTL-M", 3),
    SG50KTL_M(0x011B, "SG50KTL-M", 4),
    SG60KTL_M(0x0131, "SG60KTL-M", 4),
    SG60KU(0x0136, "SG60KU", 1),
    SG30KTL_M(0x0141, "SG30KTL-M", 3),
    SG30KTL_M_V31(0x70, "SG30KTL-M-V31", 3),
    SG33KTL_M(0x0134, "SG33KTL-M", 3),
    SG36KTL_M(0x74, "SG36KTL-M", 3),
    SG33K3J(0x013D, "SG33K3J", 3),
    SG49K5J(0x0137, "SG49K5J", 4),
    SG34KJ(0x72, "SG34KJ", 2),
    LP_P34KSG(0x73, "LP_P34KSG", 1),
    // Duplicate typeCode: SG50KTL_M_20(0x011B, "SG50KTL-M-20", 4),
    SG60KTL(0x010F, "SG60KTL", 1),
    SG80KTL(0x0138, "SG80KTL", 1),
    // Duplicate typeCode: SG80KTL_20(0x0138, "SG80KTL-20", 1),
    SG60KU_M(0x0132, "SG60KU-M", 4),
    SG5KTL_MT(0x0147, "SG5KTL-MT", 2),
    SG6KTL_MT(0x0148, "SG6KTL-MT", 2),
    SG8KTL_M(0x013F, "SG8KTL-M", 2),
    SG10KTL_M(0x013E, "SG10KTL-M", 2),
    SG10KTL_MT(0x2C0F, "SG10KTL-MT", 2),
    SG12KTL_M(0x013C, "SG12KTL-M", 2),
    SG15KTL_M(0x0142, "SG15KTL-M", 2),
    SG17KTL_M(0x0149, "SG17KTL-M", 2),
    SG20KTL_M(0x0143, "SG20KTL-M", 2),
    SG80KTL_M(0x0139, "SG80KTL-M", 4),
    SG111HV(0x014C, "SG111HV", 1),
    SG125HV(0x013B, "SG125HV", 1),
    SG125HV_20(0x2C03, "SG125HV-20", 1),
    SG30CX(0x2C10, "SG30CX", 3),
    SG33CX(0x2C00, "SG33CX", 3),
    SG36CX_US(0x2C0A, "SG36CX-US", 3),
    SG40CX(0x2C01, "SG40CX", 4),
    SG50CX(0x2C02, "SG50CX", 5),
    SG60CX_US(0x2C0B, "SG60CX-US", 5),
    SG110CX(0x2C06, "SG110CX", 9),
    SG250HX(0x2C0C, "SG250HX", 1),
    SG250HX_US(0x2C11, "SG250HX-US", 1),
    SG100CX(0x2C12, "SG100CX", 1),
    // SG100CX_JP(0x2C12, "SG100CX-JP", 1),
    SG250HX_IN(0x2C13, "SG250HX-IN", 1),
    SG25CX_SA(0x2C15, "SG25CX-SA", 3),
    SG75CX(0x2C22, "SG75CX", 9),
    SG3_0RT(0x243D, "SG3.0RT", 2),
    SG4_0RT(0x243E, "SG4.0RT", 2),
    SG5_0RT(0x2430, "SG5.0RT", 2),
    SG6_0RT(0x2431, "SG6.0RT", 2),
    SG7_0RT(0x243C, "SG7.0RT", 2),
    SG8_0RT(0x2432, "SG8.0RT", 2),
    SG10RT(0x2433, "SG10RT", 2),
    SG12RT(0x2434, "SG12RT", 2),
    SG15RT(0x2435, "SG15RT", 2),
    SG17RT(0x2436, "SG17RT", 2),
    SG20RT(0x2437, "SG20RT", 2);

    private static final Map<Integer, DeviceTypeCodeBackup> CODE_MAPPING = initMapping();

    private static Map<Integer, DeviceTypeCodeBackup> initMapping() {
        return Arrays.stream(DeviceTypeCodeBackup.values())
                .collect(Collectors.toMap(DeviceTypeCodeBackup::getTypeCode, Function.identity()));
    }

    /**
     * Returns the DeviceTypeCodes for the given type code.
     */
    @Nullable
    public static DeviceTypeCodeBackup getByTypeCode(int typeCode) {
        return CODE_MAPPING.get(typeCode);
    }

    private final int typeCode;
    private final String model;
    private final int mpptCount;

    DeviceTypeCodeBackup(int typeCode, String model, int mpptCount) {
        this.typeCode = typeCode;
        this.model = model;
        this.mpptCount = mpptCount;
    }

    public int getMpptCount() {
        return mpptCount;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public String getModel() {
        return model;
    }
}
