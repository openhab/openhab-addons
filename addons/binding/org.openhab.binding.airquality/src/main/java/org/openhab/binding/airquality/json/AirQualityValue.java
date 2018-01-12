/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.json;

import com.google.gson.annotations.SerializedName;

/**
 * Wrapper type around values reported by aqicn index values.
 *
 * @author Łukasz Dywicki
 */
public class AirQualityValue<T extends Number> {

    @SerializedName("v")
    private T value;

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
