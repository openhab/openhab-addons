/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * extension of the channel class which adds support of QuantityType
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class QuantityChannel extends Channel {

    private final Unit<?> unit;

    /**
     * constructor for channels with write access enabled + unit
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param unit
     * @param writeApiUrl
     * @param validationExpression
     */
    QuantityChannel(String id, String name, ChannelGroup channelGroup, Unit<?> unit, @Nullable String writeApiUrl,
            @Nullable String validationExpression) {
        super(id, name, channelGroup, writeApiUrl, validationExpression);
        this.unit = unit;
    }

    /**
     * constructor for channels without write access
     *
     * @param id
     * @param name
     * @param channelGroup
     * @param unit
     */
    QuantityChannel(String id, String name, ChannelGroup channelGroup, Unit<?> unit) {
        this(id, name, channelGroup, unit, null, null);
    }

    public Unit<?> getUnit() {
        return unit;
    }

}
