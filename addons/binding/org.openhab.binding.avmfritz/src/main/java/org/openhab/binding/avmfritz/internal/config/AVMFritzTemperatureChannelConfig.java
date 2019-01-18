/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.config;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Channel configuration from openHAB.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class AVMFritzTemperatureChannelConfig {

    private int offset;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("offset", getOffset()).toString();
    }
}
