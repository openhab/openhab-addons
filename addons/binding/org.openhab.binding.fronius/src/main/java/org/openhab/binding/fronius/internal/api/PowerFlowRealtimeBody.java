/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PowerFlowRealtimeBody} is responsible for storing
 * the "body" node of the JSON response
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class PowerFlowRealtimeBody {
    @SerializedName("Data")
    private PowerFlowRealtimeBodyData data;

    public PowerFlowRealtimeBodyData getData() {
        return data;
    }

    public void setData(PowerFlowRealtimeBodyData data) {
        this.data = data;
    }
}
