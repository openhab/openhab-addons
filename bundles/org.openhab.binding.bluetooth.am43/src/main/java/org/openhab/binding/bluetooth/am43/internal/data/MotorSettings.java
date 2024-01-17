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
package org.openhab.binding.bluetooth.am43.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MotorSettings} is contains the settings which are sent in batch to
 * {@link org.openhab.binding.bluetooth.am43.internal.command.SetSettingsCommand}.
 * These settings cannot be changed individually and must be sent together in the same command.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class MotorSettings {

    @Nullable
    private Direction direction;
    @Nullable
    private OperationMode operationMode;

    private boolean topLimitSet;

    private boolean bottomLimitSet;

    private int type = 0;
    // speed by rpm
    private int speed = 0;
    // lengh in mm
    private int length;
    // diameter in mm
    private int diameter;

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Nullable
    public OperationMode getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(OperationMode operationMode) {
        this.operationMode = operationMode;
    }

    public boolean isTopLimitSet() {
        return topLimitSet;
    }

    public void setTopLimitSet(boolean value) {
        this.topLimitSet = value;
    }

    public boolean isBottomLimitSet() {
        return bottomLimitSet;
    }

    public void setBottomLimitSet(boolean value) {
        this.bottomLimitSet = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getDiameter() {
        return diameter;
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }
}
