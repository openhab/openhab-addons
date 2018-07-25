/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
