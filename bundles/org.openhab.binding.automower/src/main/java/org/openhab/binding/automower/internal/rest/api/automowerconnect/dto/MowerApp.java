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
 * @author Markus Pfleger - Initial contribution
 */
public class MowerApp {
    private Mode mode;
    private Activity activity;
    private InactiveReason inactiveReason;
    private State state;
    private Long workAreaId;
    private int errorCode;
    private long errorCodeTimestamp;
    private Boolean isErrorConfirmable;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public InactiveReason getInactiveReason() {
        return inactiveReason;
    }

    public void setInactiveReason(InactiveReason inactiveReason) {
        this.inactiveReason = inactiveReason;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Long getWorkAreaId() {
        return workAreaId;
    }

    public void setWorkAreaId(Long workAreaId) {
        this.workAreaId = workAreaId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public long getErrorCodeTimestamp() {
        return errorCodeTimestamp;
    }

    public void setErrorCodeTimestamp(long errorCodeTimestamp) {
        this.errorCodeTimestamp = errorCodeTimestamp;
    }

    public Boolean getIsErrorConfirmable() {
        return isErrorConfirmable;
    }

    public void setIsErrorConfirmable(Boolean isErrorConfirmable) {
        this.isErrorConfirmable = isErrorConfirmable;
    }
}
