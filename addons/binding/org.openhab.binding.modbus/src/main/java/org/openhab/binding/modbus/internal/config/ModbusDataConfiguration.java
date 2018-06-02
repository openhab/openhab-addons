/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for data thing
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusDataConfiguration {

    @Nullable
    private String readStart;
    @Nullable
    private String readTransform;
    @Nullable
    private String readValueType;
    @Nullable
    private String writeStart;
    @Nullable
    private String writeType;
    @Nullable
    private String writeTransform;
    @Nullable
    private String writeValueType;
    private boolean writeMultipleEvenWithSingleRegisterOrCoil;
    private int writeMaxTries = 3; // backwards compatibility and tests

    public @Nullable String getReadStart() {
        return readStart;
    }

    public void setReadStart(String readStart) {
        this.readStart = readStart;
    }

    public @Nullable String getReadTransform() {
        return readTransform;
    }

    public void setReadTransform(String readTransform) {
        this.readTransform = readTransform;
    }

    public @Nullable String getReadValueType() {
        return readValueType;
    }

    public void setReadValueType(String readValueType) {
        this.readValueType = readValueType;
    }

    public @Nullable String getWriteStart() {
        return writeStart;
    }

    public void setWriteStart(String writeStart) {
        this.writeStart = writeStart;
    }

    public @Nullable String getWriteType() {
        return writeType;
    }

    public void setWriteType(String writeType) {
        this.writeType = writeType;
    }

    public @Nullable String getWriteTransform() {
        return writeTransform;
    }

    public void setWriteTransform(String writeTransform) {
        this.writeTransform = writeTransform;
    }

    public @Nullable String getWriteValueType() {
        return writeValueType;
    }

    public void setWriteValueType(String writeValueType) {
        this.writeValueType = writeValueType;
    }

    public boolean isWriteMultipleEvenWithSingleRegisterOrCoil() {
        return writeMultipleEvenWithSingleRegisterOrCoil;
    }

    public void setWriteMultipleEvenWithSingleRegisterOrCoil(boolean writeMultipleEvenWithSingleRegisterOrCoil) {
        this.writeMultipleEvenWithSingleRegisterOrCoil = writeMultipleEvenWithSingleRegisterOrCoil;
    }

    public int getWriteMaxTries() {
        return writeMaxTries;
    }

    public void setWriteMaxTries(int writeMaxTries) {
        this.writeMaxTries = writeMaxTries;
    }
}
