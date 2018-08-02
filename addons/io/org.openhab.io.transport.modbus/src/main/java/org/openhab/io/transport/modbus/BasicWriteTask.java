/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Simple implementation for Modbus write requests
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class BasicWriteTask implements WriteTask {

    private static final StandardToStringStyle TO_STRING_STYLE = new StandardToStringStyle();
    static {
        TO_STRING_STYLE.setUseShortClassName(true);
    }

    private ModbusSlaveEndpoint endpoint;
    private ModbusWriteRequestBlueprint request;
    private ModbusWriteCallback callback;

    public BasicWriteTask(ModbusSlaveEndpoint endpoint, ModbusWriteRequestBlueprint request,
            ModbusWriteCallback callback) {
        super();
        this.endpoint = endpoint;
        this.request = request;
        this.callback = callback;
    }

    @Override
    public ModbusSlaveEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public ModbusWriteRequestBlueprint getRequest() {
        return request;
    }

    @Override
    public @Nullable ModbusWriteCallback getCallback() {
        return callback;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, TO_STRING_STYLE).append("request", request).append("endpoint", endpoint)
                .append("callback", getCallback()).toString();
    }
}
