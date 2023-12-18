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
public enum DeviceTypeCode {

    SH3K6("SH3K6", 0xD06),
    SH4K6("SH4K6", 0xD07),
    SH5K_20("SH5K-20", 0xD09),
    SH5K_V13("SH5K-V13", 0xD03),
    SH3K6_30("SH3K6-30", 0xD0A),
    SH4K6_30("SH4K6-30", 0xD0B),
    SH5K_30("SH5K-30", 0xD0C),
    SH3_0RS("SH3.0RS", 0xD17),
    SH3_6RS("SH3.6RS", 0xD0D),
    SH4_0RS("SH4.0RS", 0xD18),
    SH5_0RS("SH5.0RS", 0xD0F),
    SH6_0RS("SH6.0RS", 0xD10),
    SH5_0RT("SH5.0RT", 0xE00),
    SH6_0RT("SH6.0RT", 0xE01),
    SH8_0RT("SH8.0RT", 0xE02),
    SH10RT("SH10RT", 0xE03);

    private static final Map<Integer, DeviceTypeCode> CODE_MAPPING = initMapping();

    private static Map<Integer, DeviceTypeCode> initMapping() {
        return Arrays.stream(DeviceTypeCode.values())
                .collect(Collectors.toMap(DeviceTypeCode::getTypeCode, Function.identity()));
    }

    /**
     * Returns the DeviceTypeCodes for the given type code.
     */
    @Nullable
    public static DeviceTypeCode getByTypeCode(int typeCode) {
        return CODE_MAPPING.get(typeCode);
    }

    private final int typeCode;
    private final String model;

    DeviceTypeCode(String model, int typeCode) {
        this.typeCode = typeCode;
        this.model = model;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public String getModel() {
        return model;
    }
}
