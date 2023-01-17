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
package org.openhab.binding.smartmeter.internal.sml;

import java.util.Arrays;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.openmuc.jsml.EObis;
import org.openmuc.jsml.EUnit;
import org.openmuc.jsml.structures.ASNObject;
import org.openmuc.jsml.structures.SmlListEntry;

/**
 * Proxy class to encapsulate an openMUC SML_ListEntry-Object to read informations.
 *
 * @author Matthias Steigenberger - Initial contribution
 * @author Mathias Gilhuber - Also-By
 */
@NonNullByDefault
public final class SmlValueExtractor {

    /**
     * Stores the original value object from jSML
     */
    SmlListEntry smlListEntry;

    /**
     * Constructor
     *
     * @param obis
     */
    public SmlValueExtractor(SmlListEntry listEntry) {
        smlListEntry = listEntry;
    }

    public <Q extends Quantity<Q>> MeterValue<Q> getSmlValue() {
        return new MeterValue<Q>(getObisCode(), getValue(), SmlUnitConversion.getUnit(getUnit()));
    }

    /**
     * Gets the values unit.
     *
     * @return the values unit if available - Integer.MIN_VALUE.
     */
    public EUnit getUnit() {
        return EUnit.from(smlListEntry.getUnit().getVal());
    }

    /**
     * Gets the values unit.
     *
     * @return the string representation of the values unit - otherwise null.
     */
    public String getUnitName() {
        EUnit unitEnum = getUnit();
        return unitEnum.name();
    }

    /**
     * Gets a human readable name of the OBIS code.
     *
     * @return The name of the obis code or {@link EObis#UNKNOWN} if not known
     */
    public String getObisName() {
        String obisName = null;
        EObis smlUnit = Arrays.asList(EObis.values()).stream()
                .filter((a) -> a.obisCode().equals(smlListEntry.getObjName())).findAny().orElseGet(() -> EObis.UNKNOWN);
        obisName = smlUnit.name();

        return obisName;
    }

    @Override
    public String toString() {
        return "Value: '" + this.getValue() + "' Unit: '" + this.getUnitName() + "' Scaler:'" + this.getScaler() + "'";
    }

    /**
     * Gets the value
     *
     * @return the value as String if available - otherwise null.
     */
    public String getValue() {
        org.openmuc.jsml.structures.SmlValue smlValue = smlListEntry.getValue();
        ASNObject choice = smlValue.getChoice();
        String value = choice.toString();
        try {
            value = scaleValue(Double.parseDouble(value)) + "";
        } catch (Exception e) {
            // value is no numeric value
        }
        return value;
    }

    /**
     * Gets the scaler which has to be applied to the value.
     *
     * @return scaler which has to be applied to the value.
     */
    double getScaler() {
        int scaler = 0;

        if (smlListEntry.getScaler().isSelected()) {
            byte scalerByte = smlListEntry.getScaler().getVal();

            scaler = Integer.parseInt(String.format("%02x", scalerByte), 16);

            if (scaler > 127) {
                scaler -= 256;
            }
        }

        return Math.pow(10, scaler);
    }

    /**
     * Scales the value if necessary
     *
     * @return a string representation of the scaled value.
     */
    Double scaleValue(Double originalValue) {
        return originalValue * getScaler();
    }

    /**
     * Byte to Integer conversion.
     *
     * @param byte to convert to Integer.
     */
    private static int byteToInt(byte b) {
        return Integer.parseInt(String.format("%02x", b), 16);
    }

    /**
     * Converts hex encoded OBIS to formatted string.
     *
     * @return the hex encoded OBIS code as readable string.
     */
    protected static String getObisAsString(byte[] octetBytes) {
        String formattedObis = String.format(SmartMeterBindingConstants.OBIS_FORMAT_MINIMAL, byteToInt(octetBytes[0]),
                byteToInt(octetBytes[1]), byteToInt(octetBytes[2]), byteToInt(octetBytes[3]), byteToInt(octetBytes[4]));

        return formattedObis;
    }

    public String getObisCode() {
        return getObisAsString(smlListEntry.getObjName().getValue());
    }
}
