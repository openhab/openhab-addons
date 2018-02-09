/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.param;

import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * Numeric value param
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class NumericValueParam extends DeviceParam {

    private String unit;
    private boolean graphable = true;

    public NumericValueParam(ParamType type, String unit) {
        super(type);
        setUnit(unit);
    }

    public NumericValueParam(ParamType type, String unit, DecimalType value) {
        this(type, unit);
        setValue(value == null ? 0 : value.doubleValue());
    }

    @Override
    public Number getValue() {
        return (Number) super.getValue();
    }

    public void setValue(Number value) {
        super.setValue(value);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isGraphable() {
        return graphable;
    }

    public void setGraphable(boolean graphable) {
        this.graphable = graphable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NumericValueParam)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NumericValueParam that = (NumericValueParam) o;

        if (graphable != that.graphable) {
            return false;
        }
        return unit != null ? unit.equals(that.unit) : that.unit == null;
    }

    @Override
    public String toString() {
        return "NumericValueParam{" + "super=" + super.toString() + ", unit='" + unit + '\'' + ", graphable="
                + graphable + '}';
    }

}
