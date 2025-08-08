/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
    private byte cuttingHeight;
    private boolean enabled;
    private Byte progress; // Only available for EPOS mowers and systematic mowing work areas.
    private Long lastTimeCompleted; // Only available for EPOS mowers and systematic mowing work areas.

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
}
