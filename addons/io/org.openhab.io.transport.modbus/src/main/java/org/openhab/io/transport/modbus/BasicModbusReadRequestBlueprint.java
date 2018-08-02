/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable implementation of {@link ModbusReadRequestBlueprint}
 *
 * Equals and hashCode implemented keeping {@link PollTask} in mind: two instances of this class are considered the same
 * if they have
 * the equal parameters (same slave id, start, length, function code and maxTries).
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class BasicModbusReadRequestBlueprint implements ModbusReadRequestBlueprint {
    private static StandardToStringStyle toStringStyle = new StandardToStringStyle();
    static {
        toStringStyle.setUseShortClassName(true);
    }

    private int slaveId;
    private ModbusReadFunctionCode functionCode;
    private int start;
    private int length;
    private int maxTries;

    public BasicModbusReadRequestBlueprint(int slaveId, ModbusReadFunctionCode functionCode, int start, int length,
            int maxTries) {
        super();
        this.slaveId = slaveId;
        this.functionCode = functionCode;
        this.start = start;
        this.length = length;
        this.maxTries = maxTries;
    }

    @Override
    public int getUnitID() {
        return slaveId;
    }

    @Override
    public int getReference() {
        return start;
    }

    @Override
    public ModbusReadFunctionCode getFunctionCode() {
        return functionCode;
    }

    @Override
    public int getDataLength() {
        return length;
    }

    @Override
    public int getMaxTries() {
        return maxTries;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(81, 3).append(slaveId).append(functionCode).append(start).append(length)
                .append(maxTries).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, toStringStyle).append("slaveId", slaveId).append("functionCode", functionCode)
                .append("start", start).append("length", length).append("maxTries", maxTries).toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        BasicModbusReadRequestBlueprint rhs = (BasicModbusReadRequestBlueprint) obj;
        return new EqualsBuilder().append(slaveId, rhs.slaveId).append(functionCode, rhs.functionCode)
                .append(start, rhs.start).append(length, rhs.length).isEquals();
    }

}
