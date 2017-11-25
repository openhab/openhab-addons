/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openhab.io.transport.modbus.ModbusManager.WriteTask;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Simple implementation for Modbus write requests
 *
 * @author Sami Salonen
 *
 */
public class WriteTaskImpl implements WriteTask {

    private static final StandardToStringStyle TO_STRING_STYLE = new StandardToStringStyle();
    static {
        TO_STRING_STYLE.setUseShortClassName(true);
    }

    private ModbusSlaveEndpoint endpoint;
    private ModbusWriteRequestBlueprint request;
    private ModbusWriteCallback callback;

    public WriteTaskImpl(ModbusSlaveEndpoint endpoint, ModbusWriteRequestBlueprint request,
            ModbusWriteCallback callback) {
        super();
        this.endpoint = endpoint;
        this.request = request;
        this.callback = callback;
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
    public ModbusWriteRequestBlueprint getRequest() {
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModbusWriteCallback getCallback() {
        return callback;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, TO_STRING_STYLE).append("request", request).append("endpoint", endpoint)
                .append("callback", getCallback()).toString();
    }
}
