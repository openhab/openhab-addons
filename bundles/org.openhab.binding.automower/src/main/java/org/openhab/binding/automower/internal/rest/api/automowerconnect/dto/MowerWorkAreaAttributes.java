/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author MikeTheTux - Initial contribution
 */
public class MowerWorkAreaAttributes {
    private Byte cuttingHeight;
    private Boolean enable;
    private String name;
    private Integer orientation;
    private Integer orientationShift;

    public Byte getCuttingHeight() {
        return cuttingHeight;
    }

    public void setCuttingHeight(Byte cuttingHeight) {
        this.cuttingHeight = cuttingHeight;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrientation() {
        return orientation;
    }

    public void setOrientation(Integer orientation) {
        this.orientation = orientation;
    }

    public Integer getOrientationShift() {
        return orientationShift;
    }

    public void setOrientationShift(Integer orientationShift) {
        this.orientationShift = orientationShift;
    }
}
