/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.points;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * private class: a generic data point
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public abstract class BasePoint {
    /*
     * note: temperature symbols with a degree sign: the MVN Spotless formatter
     * trashes the "degree" (looks like *) symbol, so we must escape these symbols
     * as octal \260 or unicode \u00B00
     */
    public static final String DEGREES_CELSIUS = "\260C";
    public static final String DEGREES_FAHRENHEIT = "\260F";
    public static final String DEGREES_KELVIN = "K";
    public static final String PERCENT_RELATIVE_HUMIDITY = "%r.H.";

    public static final int UNDEFINED_VALUE = -1;

    @SerializedName("rep")
    protected int rep;
    @SerializedName("type")
    protected int type;
    @SerializedName("write")
    protected boolean write;
    @SerializedName("descr")
    protected @Nullable String descr;
    @SerializedName("limits")
    protected float @Nullable [] limits;
    @SerializedName("descriptionName")
    protected @Nullable String descriptionName;
    @SerializedName("objectName")
    protected @Nullable String objectName;
    @SerializedName("memberName")
    private @Nullable String memberName;
    @SerializedName("hierarchyName")
    private @Nullable String hierarchyName;
    @SerializedName("translated")
    protected boolean translated;
    @SerializedName("presentPriority")
    protected int presentPriority;

    private String @Nullable [] enumVals;
    private boolean enumParsed = false;
    protected boolean isEnum = false;

    /*
     * initialize the enum value list
     */
    private boolean initEnum() {
        if (!enumParsed) {
            String descr = this.descr;
            if (descr != null && descr.contains("*")) {
                enumVals = descr.split("\\*");
                isEnum = true;
            }
        }
        enumParsed = true;
        return isEnum;
    }

    public int getPresentPriority() {
        return presentPriority;
    }

    /*
     * abstract methods => MUST be overridden
     */
    public abstract int asInt();

    public void refreshValueFrom(BasePoint from) {
        presentPriority = from.presentPriority;
    }

    protected boolean isEnum() {
        return (enumParsed ? isEnum : initEnum());
    }

    public State getEnum() {
        if (isEnum()) {
            int index = asInt();
            String[] enumVals = this.enumVals;
            if (index >= 0 && enumVals != null && index < enumVals.length) {
                return new StringType(enumVals[index]);
            }
        }
        return UnDefType.NULL;
    }

    /*
     * property getter for openHAB State => MUST be overridden
     */
    public State getState() {
        return UnDefType.NULL;
    }

    /*
     * property getter for openHAB returns the Units of Measure of the point value
     */
    public Unit<?> getUnit() {
        /*
         * determine the Units of Measure if available; note that other possible units
         * (Ampere, hours, milliseconds, minutes) are currently not implemented
         */
        String descr = this.descr;
        if (descr != null) {
            switch (descr) {
                case DEGREES_CELSIUS: {
                    return SIUnits.CELSIUS;
                }
                case DEGREES_FAHRENHEIT: {
                    return ImperialUnits.FAHRENHEIT;
                }
                case DEGREES_KELVIN: {
                    return Units.KELVIN;
                }
                case PERCENT_RELATIVE_HUMIDITY: {
                    return Units.PERCENT;
                }
            }
        }
        return Units.ONE;
    }

    /*
     * property getter for JSON => MAY be overridden
     */
    public String commandJson(String newVal) {
        if (isEnum()) {
            String[] enumVals = this.enumVals;
            if (enumVals != null) {
                for (int index = 0; index < enumVals.length; index++) {
                    if (enumVals[index].equals(newVal)) {
                        return String.format("{\"value\":%d}", index);
                    }
                }
            }
        }
        return String.format("{\"value\":%s}", newVal);
    }

    public String getMemberName() {
        String memberName = this.memberName;
        return memberName != null ? memberName : "undefined";
    }

    private @Nullable String hierarchyNameSuffix() {
        String fullHierarchyName = this.hierarchyName;
        if (fullHierarchyName != null) {
            int suffixPosition = fullHierarchyName.lastIndexOf("'");
            if (suffixPosition >= 0) {
                return fullHierarchyName.substring(suffixPosition, fullHierarchyName.length());
            }
        }
        return fullHierarchyName;
    }

    public String getPointClass() {
        String shortHierarchyName = hierarchyNameSuffix();
        if (shortHierarchyName != null) {
            return shortHierarchyName;
        }
        return "#".concat(getMemberName());
    }
}
