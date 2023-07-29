/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client.entity.action;

/**
 * Defines the {@link ActionParams} data structure needed to pass parameters within an {@link Action} to the innogy
 * SmartHome backend.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class ActionParams {

    private BooleanActionParam onState;

    private BooleanActionParam value;

    private DoubleActionParam pointTemperature;

    private IntegerActionParam dimLevel;

    private IntegerActionParam shutterLevel;

    private StringActionParam operationMode;

    private StringActionParam rampDirection;

    /**
     * @return the onState
     */
    public BooleanActionParam getOnState() {
        return onState;
    }

    /**
     * @param state the state to set
     */
    public void setOnState(BooleanActionParam state) {
        this.onState = state;
    }

    /**
     * @return the onState
     */
    public BooleanActionParam getValue() {
        return value;
    }

    /**
     * @param state the state to set
     */
    public void setValue(BooleanActionParam state) {
        this.value = state;
    }

    /**
     * @return the pointTemperature
     */
    public DoubleActionParam getPointTemperature() {
        return pointTemperature;
    }

    /**
     * @param pointTemperature the pointTemperature to set
     */
    public void setPointTemperature(DoubleActionParam pointTemperature) {
        this.pointTemperature = pointTemperature;
    }

    /**
     * @return the dimLevel
     */
    public IntegerActionParam getDimLevel() {
        return dimLevel;
    }

    /**
     * @param dimLevel the dimLevel to set
     */
    public void setDimLevel(IntegerActionParam dimLevel) {
        this.dimLevel = dimLevel;
    }

    /**
     * @return the shutterLevel
     */
    public IntegerActionParam getShutterLevel() {
        return shutterLevel;
    }

    /**
     * @param shutterLevel the shutterLevel to set
     */
    public void setShutterLevel(IntegerActionParam shutterLevel) {
        this.shutterLevel = shutterLevel;
    }

    /**
     * @return the operationMode
     */
    public StringActionParam getOperationMode() {
        return operationMode;
    }

    /**
     * @param operationMode the operationMode to set
     */
    public void setOperationMode(StringActionParam operationMode) {
        this.operationMode = operationMode;
    }

    /**
     * @return the rampDirection
     */
    public StringActionParam getRampDirection() {
        return rampDirection;
    }

    /**
     * @param rampDirection the rampDirection to set
     */
    public void setRampDirection(StringActionParam rampDirection) {
        this.rampDirection = rampDirection;
    }
}
