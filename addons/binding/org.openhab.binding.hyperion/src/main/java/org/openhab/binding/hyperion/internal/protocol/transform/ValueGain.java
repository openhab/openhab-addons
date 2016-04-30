/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.transform;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ValueGain} is a POJO for an value gain transformation on the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ValueGain implements Transform {

    @SerializedName("valueGain")
    protected Object value;

    public ValueGain(double value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

}
