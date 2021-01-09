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

/**
 * Class {@link QbusMessageBase} used as base class for output from gson for cmd or event feedback from the Qbus server.
 * This class only contains the common base fields required for the deserializer
 * {@link QbusMessageDeserializer} to select the specific formats implemented in {@link QbusMessageMap},
 * {@link QbusMessageListMap}, {@link QbusMessageCmd}.
 * <p>
 *
 * @author Koen Schockaert - Initial Contribution
 */

abstract class QbusMessageBase {

    private String CTD;
    protected String cmd;
    protected String id;
    protected Integer state;
    protected Integer mode;
    protected Double setpoint;
    protected Integer slatState;

    String getSn() {
        return this.CTD;
    }

    void setSn(String CTD) {
        this.CTD = CTD;
    }

    String getCmd() {
        return this.cmd;
    }

    void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public Double getSetPoint() {
        return setpoint;
    }

    public void setSetPoint(Double setpoint) {
        this.setpoint = setpoint;
    }

    public int getSlatState() {
        return slatState;
    }

    public void setSlatState(int slatState) {
        this.slatState = slatState;
    }
}
