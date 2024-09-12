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
package org.openhab.binding.lametrictime.internal.api.impl;

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.dto.Icon;

/**
 * Implementation class for icons.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractDataIcon implements Icon {
    @Nullable
    private volatile Object CONFIGURE_FLAG;

    @Nullable
    private String type;

    private byte @Nullable [] data;

    protected void configure() {
        if (CONFIGURE_FLAG == null) {
            synchronized (this) {
                if (CONFIGURE_FLAG == null) {
                    populateFields();
                }
            }
        }
    }

    protected @Nullable String getType() {
        configure();
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    protected byte @Nullable [] getData() {
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
