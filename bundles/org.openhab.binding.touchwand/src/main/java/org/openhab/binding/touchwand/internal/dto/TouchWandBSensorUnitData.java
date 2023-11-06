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
 * The {@link TouchWandBSensorUnitData} implements BSensor units property.
 *
 * @author Roie Geron - Initial contribution
 */
public class TouchWandBSensorUnitData extends TouchWandUnitData {

    private String currStatus = "";

    private IdData idData;

    @Override
    public String getCurrStatus() {
        if (currStatus == null) {
            currStatus = new String("");
        }
        return currStatus;
    }

    public void setCurrStatus(String currStatus) {
        this.currStatus = currStatus;
    }

    public IdData getIdData() {
        return idData;
    }

    public void setIdData(IdData idData) {
        this.idData = idData;
    }
}
