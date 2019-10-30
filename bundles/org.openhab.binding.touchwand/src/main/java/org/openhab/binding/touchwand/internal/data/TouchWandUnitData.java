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

/**
 * The {@link TouchWandUnitData} implements unit property.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandUnitData {

    private String name;
    private String type;
    private String nodeId;
    private int epId;
    private String connectivity;
    private String status;
    private int errorCode;
    private String idData;
    private String currStatus;

    public TouchWandUnitData(String id, String name) {
        nodeId = id;
        type = "";
        epId = 0;
        connectivity = "";
        status = "";
        errorCode = 0;
        idData = "";
        currStatus = "";
        this.name = name;
    }

    public String getUnitId() {
        return nodeId;
    }

    public String getUnitName() {
        return name;
    }

    public String getConnectivity() {
        return connectivity;
    }

    public String getIdData() {
        return idData;
    }

    public void setIdData(String idData) {
        this.idData = idData;
    }

    public int getEpId() {
        return epId;
    }

    public void setEpId(int epId) {
        this.epId = epId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getCurrStatus() {
        return currStatus;
    }

    public void setCurrStatus(String currStatus) {
        this.currStatus = currStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
