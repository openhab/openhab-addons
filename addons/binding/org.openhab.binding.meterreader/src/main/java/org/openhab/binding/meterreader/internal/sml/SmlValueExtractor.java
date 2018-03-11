/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal.sml;

import java.util.Arrays;

import org.openhab.binding.meterreader.MeterReaderBindingConstants;
import org.openhab.binding.meterreader.internal.MeterValue;
import org.openmuc.jsml.EObis;
import org.openmuc.jsml.EUnit;
import org.openmuc.jsml.structures.ASNObject;
import org.openmuc.jsml.structures.SmlListEntry;

/**
 * Proxy class to encapsulate a openMUC SML_ListEntry-Object to read informations.
 *
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
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

    public MeterValue getSmlValue() {
        return new MeterValue(getObisCode(), getValue(), getUnitName());
    }

    /**
     * Gets the values unit.
     *
     * @return the values unit if available - Integer.MIN_VALUE.
     */
    public int getUnit() {
        int unit = Integer.MIN_VALUE;

        if (smlListEntry != null) {
            unit = smlListEntry.getUnit().getVal();
        }

        return unit;
    }

    /**
     * Gets the values unit.
     *
     * @return the string representation of the values unit - otherwise null.
     */
    public String getUnitName() {
        String unit = null;
        if (smlListEntry != null) {
            EUnit smlUnit = EUnit.from(smlListEntry.getUnit().getVal());
            unit = smlUnit.name();
        }

        return unit;
    }

    /**
     * Gets a human readable name of the OBIS code.
     *
     * @return
     */
    public String getObisName() {
        String obisName = null;
        if (smlListEntry != null) {
            EObis smlUnit = Arrays.asList(EObis.values()).stream()
                    .filter((a) -> a.obisCode().equals(smlListEntry.getObjName())).findAny()
                    .orElseGet(() -> EObis.UNKNOWN);
            obisName = smlUnit.name();
        }

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

        if (smlListEntry != null) {
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

        return null;
    }

    /**
     * Gets the scaler which has to be applied to the value.
     *
     * @return scaler which has to be applied to the value.
     */
    double getScaler() {
        int scaler = 0;

        if (smlListEntry != null && smlListEntry.getScaler().isSelected()) {
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

        String formattedObis = String.format(MeterReaderBindingConstants.OBIS_FORMAT, byteToInt(octetBytes[0]),
                byteToInt(octetBytes[1]), byteToInt(octetBytes[2]), byteToInt(octetBytes[3]), byteToInt(octetBytes[4]));

        return formattedObis;
    }

    public String getObisCode() {
        return getObisAsString(smlListEntry.getObjName().getValue());
    }
}
