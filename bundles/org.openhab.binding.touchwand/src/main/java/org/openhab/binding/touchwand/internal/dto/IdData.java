/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IdData} implements IdData data class.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class IdData {

    private String status = "";
    private String subType = "";
    private String zone = "";
    private String id = "";
    private boolean hasOff;
    private boolean hasMode;
    private String type = "";
    private boolean isTempSensor;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHasOff() {
        return this.hasOff;
    }

    public boolean getHasOff() {
        return this.hasOff;
    }

    public void setHasOff(boolean hasOff) {
        this.hasOff = hasOff;
    }

    public boolean isHasMode() {
        return this.hasMode;
    }

    public boolean getHasMode() {
        return this.hasMode;
    }

    public void setHasMode(boolean hasMode) {
        this.hasMode = hasMode;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIsTempSensor() {
        return this.isTempSensor;
    }

    public boolean getIsTempSensor() {
        return this.isTempSensor;
    }

    public void setIsTempSensor(boolean isTempSensor) {
        this.isTempSensor = isTempSensor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
