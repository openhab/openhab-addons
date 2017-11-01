/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlreader.internal.sml;

import java.util.Arrays;

import org.openhab.binding.smlreader.internal.MeterValue;
import org.openmuc.jsml.EObis;
import org.openmuc.jsml.EUnit;
import org.openmuc.jsml.structures.ASNObject;
import org.openmuc.jsml.structures.Integer16;
import org.openmuc.jsml.structures.Integer32;
import org.openmuc.jsml.structures.Integer64;
import org.openmuc.jsml.structures.Integer8;
import org.openmuc.jsml.structures.OctetString;
import org.openmuc.jsml.structures.SmlBoolean;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.Unsigned16;
import org.openmuc.jsml.structures.Unsigned32;
import org.openmuc.jsml.structures.Unsigned64;

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
     */
    public SmlValueExtractor(SmlListEntry listEntry) {
        smlListEntry = listEntry;
    }

    public MeterValue getSmlValue() {
        return new MeterValue(getObisName(), getValue(), getUnitName());
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
            EUnit smlUnit = EUnit.idToEnum(smlListEntry.getUnit().getVal());
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
        String value = null;

        if (smlListEntry != null) {
            org.openmuc.jsml.structures.SmlValue smlValue = smlListEntry.getValue();
            ASNObject choice = smlValue.getChoice();

            if (SmlBoolean.class.isInstance(choice)) {
                value = Boolean.toString(((SmlBoolean) choice).getVal());
            } else if (choice instanceof OctetString) {
                value = new String(((OctetString) choice).toBytes());
            } else if (Integer8.class.isInstance(choice)) {
                value = String.format("0x%02x", ((Integer8) choice).getVal());
            } else if (Integer16.class.isInstance(choice) || Unsigned16.class.isInstance(choice)) {
                value = scaleValue(Short.toString(((Integer16) choice).getVal()));
            } else if (Integer32.class.isInstance(choice) || Unsigned32.class.isInstance(choice)) {
                value = scaleValue(Integer.toString(((Integer32) choice).getVal()));
            } else if (Integer64.class.isInstance(choice) || Unsigned64.class.isInstance(choice)) {
                value = scaleValue(Long.toString(((Integer64) choice).getVal()));
            }
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

        if (smlListEntry != null && smlListEntry.getScaler().isSelected()) {
            byte scalerByte = smlListEntry.getScaler().getVal();

            scaler = Integer.parseInt(String.format("%02x", scalerByte), 16);

            if (scaler == 255) {
                scaler = -1;
            }
        }

        return Math.pow(10, scaler);
    }

    /**
     * Scales the value if necessary
     *
     * @return a string representation of the scaled value.
     */
    String scaleValue(String originalValue) {
        double scaledValue = Double.parseDouble(originalValue);
        scaledValue *= getScaler();

        return Double.toString(scaledValue);
    }

}
