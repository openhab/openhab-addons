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
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Implementation of {@link PollTask} that differentiates tasks using endpoint, request and callbacks.
 *
 * Note: Two differentiate poll tasks are considered unequal if their callbacks are unequal.
 *
 * HashCode and equals should be defined such that two poll tasks considered the same only if their request,
 * maxTries, endpoint and callback are the same.
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class BasicPollTaskImpl implements PollTask {

    static StandardToStringStyle toStringStyle = new StandardToStringStyle();
    static {
        toStringStyle.setUseShortClassName(true);
    }

    private ModbusSlaveEndpoint endpoint;
    private BasicModbusReadRequestBlueprint request;
    private @Nullable ModbusReadCallback callback;

    public BasicPollTaskImpl(ModbusSlaveEndpoint endpoint, BasicModbusReadRequestBlueprint request) {
        this(endpoint, request, null);
    }

    public BasicPollTaskImpl(ModbusSlaveEndpoint endpoint, BasicModbusReadRequestBlueprint request,
            @Nullable ModbusReadCallback callback) {
        this.endpoint = endpoint;
        this.request = request;
        this.callback = callback;
    }

    @Override
    public ModbusReadRequestBlueprint getRequest() {
        return request;
    }

    @Override
    public ModbusSlaveEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public @Nullable ModbusReadCallback getCallback() {
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
        BasicPollTaskImpl rhs = (BasicPollTaskImpl) obj;
        return new EqualsBuilder().append(request, rhs.request).append(endpoint, rhs.endpoint)
                .append(getCallback(), rhs.getCallback()).isEquals();
    }

}
