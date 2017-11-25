/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import org.openhab.io.transport.modbus.ModbusManager.PollTask;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Implementation of {@link PollTask} that differentiates tasks using endpoint, request and callbacks.
 *
 * Note: Two differentiate poll tasks are considered unequal if their callbacks are unequal.
 *
 * HashCode and equals should be defined such that two poll tasks considered the same only if their request,
 * maxTries, endpoint and callback are the same.
 *
 * @author Sami Salonen
 *
 */
public class PollTaskImpl implements PollTask {

    static StandardToStringStyle toStringStyle = new StandardToStringStyle();
    static {
        toStringStyle.setUseShortClassName(true);
    }

    private ModbusSlaveEndpoint endpoint;
    private ModbusReadRequestBlueprintImpl request;
    private ModbusReadCallback callback;

    public PollTaskImpl(ModbusSlaveEndpoint endpoint, ModbusReadRequestBlueprintImpl request,
            ModbusReadCallback callback) {
        super();
        this.endpoint = endpoint;
        this.request = request;
        this.callback = callback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModbusReadRequestBlueprint getRequest() {
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModbusSlaveEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModbusReadCallback getCallback() {
        return callback;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(69, 5).append(request).append(getEndpoint()).append(getCallback()).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, toStringStyle).append("request", request).append("endpoint", endpoint)
                .append("callback", getCallback()).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        PollTaskImpl rhs = (PollTaskImpl) obj;
        return new EqualsBuilder().append(request, rhs.request).append(endpoint, rhs.endpoint)
                .append(getCallback(), rhs.getCallback()).isEquals();
    }

}
