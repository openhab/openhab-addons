/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model.property;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Gardena complex property value for the irrigation control.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class IrrigationControlWateringProperty extends BaseProperty {
    private IrrigationControlWateringValue value = new IrrigationControlWateringValue();

    public IrrigationControlWateringProperty(String name, int duration, int valveId) {
        super(name);

        value.state = duration == 0 ? "idle" : "manual";
        value.duration = duration;
        value.valveId = valveId;
    }

    @Override
    public String getValue() {
        return String.valueOf(value.duration);
    }

    @SuppressWarnings("unused")
    private class IrrigationControlWateringValue {
        public String state;
        public int duration;
        @SerializedName("valve_id")
        public int valveId;
    }
}
