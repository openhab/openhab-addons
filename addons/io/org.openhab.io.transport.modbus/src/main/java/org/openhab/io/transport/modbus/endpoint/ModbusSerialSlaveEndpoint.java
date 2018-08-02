/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.endpoint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import net.wimpi.modbus.util.SerialParameters;

/**
 * Serial endpoint. Endpoint differentiates different modbus slaves only by the serial port.
 * port.
 *
 * Endpoint contains SerialParameters which should be enough to establish the connection.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusSerialSlaveEndpoint implements ModbusSlaveEndpoint {

    private SerialParameters serialParameters;
    private static StandardToStringStyle toStringStyle = new StandardToStringStyle();

    static {
        toStringStyle.setUseShortClassName(true);
    }

    public ModbusSerialSlaveEndpoint(String portName, int baudRate, int flowControlIn, int flowControlOut, int databits,
            int stopbits, int parity, String encoding, boolean echo, int receiveTimeoutMillis) {
        this(new SerialParameters(portName, baudRate, flowControlIn, flowControlOut, databits, stopbits, parity,
                encoding, echo, receiveTimeoutMillis));
    }

    public ModbusSerialSlaveEndpoint(String portName, int baudRate, String flowControlIn, String flowControlOut,
            int databits, String stopbits, String parity, String encoding, boolean echo, int receiveTimeoutMillis) {
        SerialParameters parameters = new SerialParameters();
        parameters.setPortName(portName);
        parameters.setBaudRate(baudRate);
        parameters.setFlowControlIn(flowControlIn);
        parameters.setFlowControlOut(flowControlOut);
        parameters.setDatabits(databits);
        parameters.setStopbits(stopbits);
        parameters.setParity(parity);
        parameters.setEncoding(encoding);
        parameters.setEcho(echo);
        parameters.setReceiveTimeoutMillis(receiveTimeoutMillis);
        this.serialParameters = parameters;
    }

    private ModbusSerialSlaveEndpoint(SerialParameters serialParameters) {
        this.serialParameters = serialParameters;
    }

    public SerialParameters getSerialParameters() {
        return serialParameters;
    }

    @Override
    public <R> R accept(ModbusSlaveEndpointVisitor<R> factory) {
        return factory.visit(this);
    }

    public String getPortName() {
        return serialParameters.getPortName();
    }

    @Override
    public int hashCode() {
        // hashcode & equal is determined purely by port name
        return serialParameters.getPortName().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        // equals is determined purely by port name
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ModbusSerialSlaveEndpoint rhs = (ModbusSerialSlaveEndpoint) obj;
        return new EqualsBuilder().append(serialParameters.getPortName(), rhs.serialParameters.getPortName())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, toStringStyle).append("portName", serialParameters.getPortName()).toString();
    }
}
