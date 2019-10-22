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

/**
 * The {@link TouchWandUnitData} implements unit property.
 *
 * @author Roie Geron - Initial contribution
 */

public class TouchWandUnitData {

    private String name;
    private String type;
    private String nodeId;
    private int epId;
    private String connectivity;
    private String status;
    private int errorCode;
    private int idData;
    private String currStatus;

    public TouchWandUnitData(String id, String name) {
        nodeId = id;
        this.name = name;
    }

    public String getUnitId() {
        return nodeId;
    }

    public String getUnitName() {
        return name;
    }

}
