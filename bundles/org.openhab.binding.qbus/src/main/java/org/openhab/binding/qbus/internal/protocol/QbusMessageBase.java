/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.qbus.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class {@link QbusMessageBase} used as base class for output from gson for cmd or event feedback from the Qbus server.
 * This class only contains the common base fields required for the deserializer
 * {@link QbusMessageDeserializer} to select the specific formats implemented in {@link QbusMessageMap},
 * {@link QbusMessageListMap}, {@link QbusMessageCmd}.
 * <p>
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
abstract class QbusMessageBase {

    private @Nullable String CTD;
    protected @Nullable String cmd;
    protected @Nullable String id;
    protected @Nullable Integer state;
    protected @Nullable Integer mode;
    protected @Nullable Double setpoint;
    protected @Nullable Integer slatState;

    @Nullable
    String getSn() {
        return this.CTD;
    }

    void setSn(String CTD) {
        this.CTD = CTD;
    }

    @Nullable
    String getCmd() {
        return this.cmd;
    }

    void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public Integer getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Nullable
    public Integer getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Nullable
    public Double getSetPoint() {
        return setpoint;
    }

    public void setSetPoint(Double setpoint) {
        this.setpoint = setpoint;
    }

    @Nullable
    public Integer getSlatState() {
        return slatState;
    }

    public void setSlatState(int slatState) {
        this.slatState = slatState;
    }
}
