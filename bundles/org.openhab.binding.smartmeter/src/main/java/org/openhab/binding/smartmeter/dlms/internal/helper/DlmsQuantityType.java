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
package org.openhab.binding.smartmeter.dlms.internal.helper;

import java.util.regex.Pattern;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;

/**
 * Extension of {@link QuantityType} that parses values from a DLMS/COSEM meter
 * channel. Example readings are:
 *
 * <li>1-0:1.8.0(12345.678*kWh)
 * <li>1-0:32.7.0(230.0*V)
 * <li>1-0:31.7.0(1.5*A)
 * <li>1-0:16.7.0(0.345*kW)
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DlmsQuantityType<T extends Quantity<T>> extends QuantityType<T> {

    private static final long serialVersionUID = 1305378390275793428L;

    private static final int EXPECTED_PARTS_LENGTH = 2;
    private static final Pattern METER_VALUE_SPLIT_PATTERN = Pattern.compile("\\(");

    public DlmsQuantityType(String meterValue) {
        super(meterStringToUomString(meterValue));
    }

    /**
     * Convert DLMS/COSEM meter reading string to OH UoM string.
     *
     * @param meterValue a meter reading string like '1-0:1.8.0(12345.678*kWh)'
     * @return a UoM string like '12345.678 kWh'
     */
    private static String meterStringToUomString(String meterValue) {
        String[] parts = METER_VALUE_SPLIT_PATTERN.split(meterValue);
        if (parts.length < EXPECTED_PARTS_LENGTH) {
            throw new IllegalArgumentException(
                    "Invalid meter value '%s' - expected format like '1-0:1.8.0(12345.678*kWh)'".formatted(meterValue));
        }
        return parts[1].replace("*", " ").replace(")", "").trim();
    }
}
