/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.touchwand.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link TouchWandShutterSwitchUnitData} implements Shutter and Switch units property.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandShutterSwitchUnitData extends TouchWandUnitData {

    @SerializedName("currStatus")
    @Expose
    private Integer currStatus = 0;

    @Override
    public Integer getCurrStatus() {
        return currStatus;
    }

    public void setCurrStatus(Integer currStatus) {
        this.currStatus = currStatus;
    }
}
