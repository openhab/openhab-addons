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
package org.openhab.binding.touchwand.internal.dto;

/**
 * The {@link TouchWandUnitDataWallController} implements WallController unit
 * property.
 *
 * @author Roie Geron - Initial contribution
 */
public class TouchWandUnitDataWallController extends TouchWandUnitData {

    private CurrStatus currStatus = new CurrStatus();

    // currStatus can be null since the object is created by gson fromJson
    // in case the key is null or not exist , the variable will be null.
    // if this is the case , default status is created

    @Override
    public Csc getCurrStatus() {
        if (currStatus == null) {
            currStatus = new CurrStatus();
        }
        return currStatus.getCsc();
    }

    public void setCurrStatus(CurrStatus currStatus) {
        this.currStatus = currStatus;
    }
}
