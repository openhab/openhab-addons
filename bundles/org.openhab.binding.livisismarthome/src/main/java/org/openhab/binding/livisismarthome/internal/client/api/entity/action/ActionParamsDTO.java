/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client.api.entity.action;

/**
 * Defines the {@link ActionParamsDTO} data structure needed to pass parameters within an {@link ActionDTO} to the
 * Livisi
 * SmartHome backend.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class ActionParamsDTO {

    private BooleanActionParamDTO onState;
    private BooleanActionParamDTO value;
    private DoubleActionParamDTO pointTemperature;
    private IntegerActionParamDTO dimLevel;
    private IntegerActionParamDTO shutterLevel;
    private StringActionParamDTO operationMode;
    private StringActionParamDTO rampDirection;

    /**
     * @return the onState
     */
    public BooleanActionParamDTO getOnState() {
        return onState;
    }

    /**
     * @param state the state to set
     */
    public void setOnState(BooleanActionParamDTO state) {
        this.onState = state;
    }

    /**
     * @return the onState
     */
    public BooleanActionParamDTO getValue() {
        return value;
    }

    /**
     * @param state the state to set
     */
    public void setValue(BooleanActionParamDTO state) {
        this.value = state;
    }

    /**
     * @return the pointTemperature
     */
    public DoubleActionParamDTO getPointTemperature() {
        return pointTemperature;
    }

    /**
     * @param pointTemperature the pointTemperature to set
     */
    public void setPointTemperature(DoubleActionParamDTO pointTemperature) {
        this.pointTemperature = pointTemperature;
    }

    /**
     * @return the dimLevel
     */
    public IntegerActionParamDTO getDimLevel() {
        return dimLevel;
    }

    /**
     * @param dimLevel the dimLevel to set
     */
    public void setDimLevel(IntegerActionParamDTO dimLevel) {
        this.dimLevel = dimLevel;
    }

    /**
     * @return the shutterLevel
     */
    public IntegerActionParamDTO getShutterLevel() {
        return shutterLevel;
    }

    /**
     * @param shutterLevel the shutterLevel to set
     */
    public void setShutterLevel(IntegerActionParamDTO shutterLevel) {
        this.shutterLevel = shutterLevel;
    }

    /**
     * @return the operationMode
     */
    public StringActionParamDTO getOperationMode() {
        return operationMode;
    }

    /**
     * @param operationMode the operationMode to set
     */
    public void setOperationMode(StringActionParamDTO operationMode) {
        this.operationMode = operationMode;
    }

    /**
     * @return the rampDirection
     */
    public StringActionParamDTO getRampDirection() {
        return rampDirection;
    }

    /**
     * @param rampDirection the rampDirection to set
     */
    public void setRampDirection(StringActionParamDTO rampDirection) {
        this.rampDirection = rampDirection;
    }
}
