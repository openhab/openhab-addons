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
public class WorkArea {
    private long workAreaId;
    private String name;
    private String type;
    private byte cuttingHeight;
    private boolean enabled;
    private boolean schedulable;
    private boolean useGlobalCuttingHeight;
    private long lastTimeAbandoned;
    private Byte progress; // Only available for EPOS mowers and systematic mowing work areas.
    private Long lastTimeCompleted; // Only available for EPOS mowers and systematic mowing work areas.
    private Integer orientation; // Only available for pattern based work areas.
    private Integer orientationShift; // Only available for pattern based work areas.

    public long getWorkAreaId() {
        return workAreaId;
    }

    public void setWorkAreaId(long workAreaId) {
        this.workAreaId = workAreaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte getCuttingHeight() {
        return cuttingHeight;
    }

    public void setCuttingHeight(byte cuttingHeight) {
        this.cuttingHeight = cuttingHeight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSchedulable() {
        return schedulable;
    }

    public void setSchedulable(boolean schedulable) {
        this.schedulable = schedulable;
    }

    public boolean isUseGlobalCuttingHeight() {
        return useGlobalCuttingHeight;
    }

    public void setUseGlobalCuttingHeight(boolean useGlobalCuttingHeight) {
        this.useGlobalCuttingHeight = useGlobalCuttingHeight;
    }

    public long getLastTimeAbandoned() {
        return lastTimeAbandoned;
    }

    public void setLastTimeAbandoned(long lastTimeAbandoned) {
        this.lastTimeAbandoned = lastTimeAbandoned;
    }

    public Byte getProgress() {
        return progress;
    }

    public void setProgress(Byte progress) {
        this.progress = progress;
    }

    public Long getLastTimeCompleted() {
        return lastTimeCompleted;
    }

    public void setLastTimeCompleted(Long lastTimeCompleted) {
        this.lastTimeCompleted = lastTimeCompleted;
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
