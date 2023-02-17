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
package org.openhab.binding.lametrictime.internal.api.impl;

import java.util.Base64;

import org.openhab.binding.lametrictime.internal.api.model.Icon;

/**
 * Implementation class for icons.
 *
 * @author Gregory Moyer - Initial contribution
 */
public abstract class AbstractDataIcon implements Icon {
    private volatile Object CONFIGURE_FLAG;

    private String type;
    private byte[] data;

    protected void configure() {
        if (CONFIGURE_FLAG == null) {
            synchronized (this) {
                if (CONFIGURE_FLAG == null) {
                    populateFields();
                }
            }
        }
    }

    protected String getType() {
        configure();
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    protected byte[] getData() {
        configure();
        return data;
    }

    protected void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toRaw() {
        return new StringBuilder().append("data:").append(getType()).append(";base64,")
                .append(Base64.getEncoder().encodeToString(getData())).toString();
    }

    protected abstract void populateFields();
}
