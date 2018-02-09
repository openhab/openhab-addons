/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onebusaway.internal.config;

import static org.openhab.binding.onebusaway.OneBusAwayBindingConstants.CHANNEL_CONFIG_OFFSET;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The {@link ChannelConfig} defines the model for a channel configuration.
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class ChannelConfig {
    private Integer offset = 0;

    /**
     * @return the offset (in seconds).
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Sets the offset (in seconds).
     */
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(CHANNEL_CONFIG_OFFSET, this.getOffset()).toString();
    }
}
