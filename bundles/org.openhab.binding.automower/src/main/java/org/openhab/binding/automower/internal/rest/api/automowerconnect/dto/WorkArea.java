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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class WorkArea {
    private long workAreaId;
    private String name;
    private int cuttingHeight;
    private Boolean enabled;
    private int progress; // Only available for EPOS mowers and systematic mowing work areas.
    private long lastTimeCompleted;

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

    public int getCuttingHeight() {
        return cuttingHeight;
    }

    public void setCuttingHeight(int cuttingHeight) {
        this.cuttingHeight = cuttingHeight;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getLastTimeCompleted() {
        return lastTimeCompleted;
    }

    public void setLastTimeCompleted(long lastTimeCompleted) {
        this.lastTimeCompleted = lastTimeCompleted;
    }
}
